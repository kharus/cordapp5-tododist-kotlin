package com.learncorda.tododist.states

import com.google.gson.Gson
import com.learncorda.tododist.contracts.ToDoContract
import net.corda.v5.application.identity.AbstractParty
import net.corda.v5.application.identity.Party
import net.corda.v5.application.utilities.JsonRepresentable
import net.corda.v5.ledger.UniqueIdentifier
import net.corda.v5.ledger.contracts.BelongsToContract
import net.corda.v5.ledger.contracts.LinearState


@BelongsToContract(ToDoContract::class)
data class ToDoState (
    val assignedBy: Party,
    val assignedTo: Party,
    val taskDescription: String,
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val participants: List<AbstractParty> = listOf(assignedBy,assignedTo)
) : LinearState, JsonRepresentable{

    fun changeOwner(assignedTo: Party): ToDoState {
        return ToDoState(assignedBy, assignedTo, taskDescription, linearId)
    }

    fun toDto(): TemplateStateDto {
        return TemplateStateDto(
                assignedBy.name.toString(),
                assignedTo.name.toString(),
                taskDescription
            )
    }

    override fun toJsonString(): String {
        return Gson().toJson(this.toDto())
    }

}
data class TemplateStateDto(
        val assignedBy: String,
        val assignedTo: String,
        val taskDescription: String
)
