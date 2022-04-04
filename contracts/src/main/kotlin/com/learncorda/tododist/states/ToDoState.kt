package com.learncorda.tododist.states

import com.google.gson.Gson
import com.learncorda.tododist.contracts.ToDoContract
import net.corda.v5.application.identity.AbstractParty
import net.corda.v5.application.identity.Party
import net.corda.v5.application.utilities.JsonRepresentable
import net.corda.v5.ledger.UniqueIdentifier
import net.corda.v5.ledger.contracts.BelongsToContract
import net.corda.v5.ledger.contracts.LinearState
import net.corda.v5.ledger.schemas.PersistentState
import net.corda.v5.ledger.schemas.QueryableState
import net.corda.v5.persistence.MappedSchema


@BelongsToContract(ToDoContract::class)
data class ToDoState (
    val assignedBy: Party,
    val assignedTo: Party,
    val taskDescription: String,
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val participants: List<AbstractParty> = listOf(assignedBy,assignedTo)
) : LinearState, JsonRepresentable, QueryableState {

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

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is ToDoSchemaV1 -> ToDoSchemaV1.ToDoModel(
                this.taskDescription,
                this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(ToDoSchemaV1)

}
data class TemplateStateDto(
        val assignedBy: String,
        val assignedTo: String,
        val taskDescription: String
)
