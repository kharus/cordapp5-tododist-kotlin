package net.corda.c5template.contracts

import net.corda.c5template.states.TemplateState
import net.corda.v5.ledger.contracts.CommandData
import net.corda.v5.ledger.contracts.Contract
import net.corda.v5.ledger.contracts.requireSingleCommand
import net.corda.v5.ledger.contracts.requireThat
import net.corda.v5.ledger.transactions.LedgerTransaction
import net.corda.v5.ledger.transactions.outputsOfType

class TemplateContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        @JvmStatic
        val ID = "net.corda.c5template.contracts.TemplateContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        val output = tx.outputsOfType<TemplateState>().single()
        when (command.value) {
            is Commands.Create -> requireThat {
                "No inputs should be consumed when sending the Hello-World message.".using(tx.inputStates.isEmpty())
                "The message must be Hello-World".using(output.msg == "Hello-World")
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
    }
}