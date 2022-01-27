package com.learncorda.tododist.contracts

import com.learncorda.tododist.states.ToDoState
import net.corda.v5.ledger.contracts.CommandData
import net.corda.v5.ledger.contracts.Contract
import net.corda.v5.ledger.contracts.requireSingleCommand
import net.corda.v5.ledger.contracts.requireThat
import net.corda.v5.ledger.transactions.LedgerTransaction
import net.corda.v5.ledger.transactions.outputsOfType

class ToDoContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        @JvmStatic
        val ID = "com.learncorda.tododist.contracts.ToDoContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        //val output = tx.outputsOfType<ToDoState>().single()
        when (command.value) {
            is Commands.CreateToDo -> requireThat {
                "No inputs should be consumed when sending the Hello-World message.".using(tx.inputStates.isEmpty())
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class CreateToDo : Commands
        class AssignToDo : Commands
    }
}