package net.corda.c5template.flows

import com.nhaarman.mockito_kotlin.*
import net.corda.c5template.contracts.TemplateContract
import net.corda.c5template.states.TemplateState
import net.corda.systemflows.CollectSignaturesFlow
import net.corda.systemflows.FinalityFlow
import net.corda.testing.flow.utils.flowTest
import net.corda.v5.application.flows.RpcStartFlowRequestParameters
import net.corda.v5.application.identity.CordaX500Name
import net.corda.v5.application.services.json.parseJson
import net.corda.v5.ledger.contracts.Command
import net.corda.v5.ledger.contracts.CommandData
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test

class ExampleUnitTest {

    @Test
    fun `flow signs state`() {
        flowTest<TemplateFlow> {

            // NOTE: this probably should be set up in flowTest
            val mockNode = CordaX500Name.parse("O=MockNode, L=London, C=GB, OU=Template")

            /*The inputParams does not carry actually purpose when running the test.
             *It is there only to help the flow execute.
             *All of the returned value is populated with the doReturn() methods */
            val inputParams = "{\"msg\": \"Hello-World\", \"receiver\": \"${mockNode}\"}"
            createFlow { TemplateFlow(RpcStartFlowRequestParameters(inputParams)) }

            //Set the return value of the flow
            doReturn(notary)
                    .whenever(flow.notaryLookup)
                    .getNotary(CordaX500Name.parse("O=notary, L=London, C=GB"))
            doReturn(mockNode)
                .whenever(otherSide)
                .name
            doReturn(otherSide)
                .whenever(flow.identityService)
                .partyFromName(mockNode)

            doReturn(signedTransactionMock)
                .whenever(flow.flowEngine)
                .subFlow(any<CollectSignaturesFlow>())

            doReturn(signedTransactionMock)
                .whenever(flow.flowEngine)
                .subFlow(any<FinalityFlow>())

            doReturn(
                    mapOf(
                            "msg" to "Hello-World",
                            "receiver" to otherSide.name.toString()
                    )
            )
                .whenever(flow.jsonMarshallingService)
                .parseJson<Map<String, String>>(inputParams)

            flow.call()

            // verify notary is set
            verify(transactionBuilderMock).setNotary(notary)

            // verify the correct output state is created
            argumentCaptor<TemplateState>().apply {
                verify(transactionBuilderMock).addOutputState(capture(), eq(TemplateContract.ID))
                assertSoftly {
                    it.assertThat(firstValue.sender).isEqualTo(ourIdentity)
                    it.assertThat(firstValue.receiver).isEqualTo(otherSide)
                    it.assertThat(firstValue.msg).isEqualTo("Hello-World")
                }
            }

            // verify command is added
            argumentCaptor<Command<CommandData>>().apply {
                verify(transactionBuilderMock).addCommand(capture())
                assertThat(firstValue.value).isInstanceOf(TemplateContract.Commands.Create::class.java)
                assertThat(firstValue.signers).contains(ourIdentity.owningKey)
                assertThat(firstValue.signers).contains(otherSide.owningKey)
            }
        }
    }
}
