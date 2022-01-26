package com.learncorda.tododist.flows

import com.learncorda.tododist.contracts.ToDoContract
import com.learncorda.tododist.states.ToDoState
import net.corda.systemflows.CollectSignaturesFlow
import net.corda.systemflows.FinalityFlow
import net.corda.systemflows.ReceiveFinalityFlow
import net.corda.systemflows.SignTransactionFlow
import net.corda.v5.application.flows.*
import net.corda.v5.application.flows.flowservices.FlowEngine
import net.corda.v5.application.flows.flowservices.FlowIdentity
import net.corda.v5.application.flows.flowservices.FlowMessaging
import net.corda.v5.application.identity.CordaX500Name
import net.corda.v5.application.injection.CordaInject
import net.corda.v5.application.services.json.JsonMarshallingService
import net.corda.v5.application.services.json.parseJson
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.contracts.Command
import net.corda.v5.ledger.contracts.requireThat
import net.corda.v5.ledger.services.NotaryLookupService
import net.corda.v5.ledger.transactions.SignedTransaction
import net.corda.v5.ledger.transactions.SignedTransactionDigest
import net.corda.v5.ledger.transactions.TransactionBuilderFactory


@InitiatingFlow
@StartableByRPC
class CreateToDoFlow @JsonConstructor constructor(private val params: RpcStartFlowRequestParameters) : Flow<SignedTransactionDigest> {

    @CordaInject
    lateinit var flowEngine: FlowEngine
    @CordaInject
    lateinit var flowIdentity: FlowIdentity
    @CordaInject
    lateinit var flowMessaging: FlowMessaging
    @CordaInject
    lateinit var transactionBuilderFactory: TransactionBuilderFactory
    @CordaInject
    lateinit var notaryLookup: NotaryLookupService
    @CordaInject
    lateinit var jsonMarshallingService: JsonMarshallingService

    @Suspendable
    override fun call(): SignedTransactionDigest {

        // parse parameters
        val mapOfParams: Map<String, String> = jsonMarshallingService.parseJson(params.parametersInJson)

        val task = with(mapOfParams["task"] ?: throw BadRpcStartFlowRequestException("ToDo State Parameter \"task\" missing.")) {
            this
        }

        val notary = notaryLookup.getNotary(CordaX500Name.parse("O=notary, L=London, C=GB"))!!

        // Stage 1.
        // Generate an unsigned transaction.
        val ourself = flowIdentity.ourIdentity
        val toDoState = ToDoState(ourself, ourself, task)
        val txCommand = Command(ToDoContract.Commands.CreateToDo(), listOf(ourself.owningKey))
        val txBuilder = transactionBuilderFactory.create()
                .setNotary(notary)
                .addOutputState(toDoState, ToDoContract.ID)
                .addCommand(txCommand)


        // Stage 2.
        // Verify that the transaction is valid.
        txBuilder.verify()

        // Stage 3.
        // Sign the transaction.
        val partSignedTx = txBuilder.sign()

        // Stage 4.
        // Send the state to the counterparty, and receive it back with their signature.
        val otherPartySession = flowMessaging.initiateFlow(ourself)
        val fullySignedTx = flowEngine.subFlow(
                CollectSignaturesFlow(
                        partSignedTx, setOf(otherPartySession),
                )
        )

        // Stage 5.
        // Notarise and record the transaction in both parties' vaults.
        val notarisedTx = flowEngine.subFlow(
                FinalityFlow(
                        fullySignedTx, setOf(otherPartySession),
                )
        )

        return SignedTransactionDigest(
                notarisedTx.id,
                notarisedTx.tx.outputStates.map { output -> jsonMarshallingService.formatJson(output) },
                notarisedTx.sigs
        )
    }

}

@InitiatedBy(CreateToDoFlow::class)
class ToDoFlowAcceptor(val otherPartySession: FlowSession) : Flow<SignedTransaction> {
    @CordaInject
    lateinit var flowEngine: FlowEngine

    // instead, for now, doing this so it can be unit tested separately:
    fun isValid(stx: SignedTransaction) {
        requireThat {
            val output = stx.tx.outputs.single().data
            "This must be an ToDoState transaction." using (output is ToDoState)
        }
    }

    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = isValid(stx)
        }
        val txId = flowEngine.subFlow(signTransactionFlow).id
        return flowEngine.subFlow(ReceiveFinalityFlow(otherPartySession, expectedTxId = txId))
    }
}