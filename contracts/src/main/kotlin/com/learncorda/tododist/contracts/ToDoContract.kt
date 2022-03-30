package com.learncorda.tododist.contracts

import com.learncorda.tododist.states.ToDoState
import net.corda.v5.ledger.contracts.CommandData
import net.corda.v5.ledger.contracts.Contract
import net.corda.v5.ledger.contracts.requireSingleCommand
import net.corda.v5.ledger.contracts.requireThat
import net.corda.v5.ledger.transactions.LedgerTransaction

class ToDoContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        @JvmStatic
        val ID = "com.learncorda.tododist.contracts.ToDoContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val commandData = tx.commands[0].value

        val toDoOutput = tx.outputsOfType(ToDoState::class.java).single()

        when (commandData) {
            is Commands.CreateToDoCommand -> requireThat {
                println("ToDoContract CreateToDoCommand's verify() method has been called")
                "Task description should not be blank".using(toDoOutput.taskDescription.isNotBlank())
                "Task description is too long".using(toDoOutput.taskDescription.length < 25)
                null
            }
            is Commands.AssignToDoCommand -> requireThat {
                println("ToDoContract AssignToDoCommand's verify() method has been called")
                val toDoInput = tx.inputsOfType(ToDoState::class.java).single()
                "Already assigned to party".using(toDoInput.assignedTo != toDoOutput.assignedTo)
                null
            }
        }
        tx.commands.requireSingleCommand<Commands>()
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class CreateToDoCommand : Commands
        class AssignToDoCommand : Commands
        class MarkCompleteToDoCommand : Commands
    }
}