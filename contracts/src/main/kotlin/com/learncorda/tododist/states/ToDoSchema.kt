package com.learncorda.tododist.states

import net.corda.v5.base.annotations.CordaSerializable
import net.corda.v5.ledger.UniqueIdentifier
import net.corda.v5.ledger.schemas.PersistentState
import net.corda.v5.persistence.MappedSchema
import net.corda.v5.persistence.UUIDConverter
import java.util.*
import javax.persistence.*

object ToDoSchema

/**
 * A todo schema.
 */
object ToDoSchemaV1 : MappedSchema(
    schemaFamily = ToDoSchema.javaClass,
    version = 1,
    mappedTypes = listOf(ToDoModel::class.java)
) {
    override val migrationResource: String
        get() = "todo.changelog-master"

    @Entity
    @Table(name = "todo_states")
    class ToDoModel(
        @Column(name = "task")
        var task: String,

        @Column(name = "id")
        @Convert(converter = UUIDConverter::class)
        var linearId: UUID
    ) : PersistentState() {
        constructor(): this("", UniqueIdentifier().id)
    }
}

