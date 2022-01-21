package net.corda.c5template.states

import com.google.gson.Gson
import net.corda.c5template.contracts.TemplateContract
import net.corda.v5.application.identity.AbstractParty
import net.corda.v5.application.identity.Party
import net.corda.v5.application.utilities.JsonRepresentable
import net.corda.v5.ledger.contracts.BelongsToContract
import net.corda.v5.ledger.contracts.ContractState


@BelongsToContract(TemplateContract::class)
data class TemplateState (
        val msg: String,
        val sender: Party,
        val receiver: Party,
) : ContractState, JsonRepresentable{

    override val participants: List<AbstractParty> get() = listOf(sender,receiver)

    fun toDto(): TemplateStateDto {
        return TemplateStateDto(
                msg,
                sender.name.toString(),
                receiver.name.toString()
        )
    }

    override fun toJsonString(): String {
        return Gson().toJson(this.toDto())
    }

}
data class TemplateStateDto(
        val msg: String,
        val sender: String,
        val receiver: String
)
