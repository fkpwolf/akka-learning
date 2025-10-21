package com.akkalearning.persistence

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.akkalearning.CborSerializable

/**
 * Persistent Bank Account Actor demonstrating Event Sourcing
 * 
 * This actor maintains account balance by persisting all events (deposits, withdrawals)
 * and rebuilding state from these events on recovery.
 */
object BankAccountActor {

  // Commands - requests to change state
  sealed trait Command extends CborSerializable
  final case class Deposit(amount: Double, replyTo: ActorRef[Response]) extends Command
  final case class Withdraw(amount: Double, replyTo: ActorRef[Response]) extends Command
  final case class GetBalance(replyTo: ActorRef[Response]) extends Command
  final case class CloseAccount(replyTo: ActorRef[Response]) extends Command

  // Events - facts about what happened
  sealed trait Event extends CborSerializable
  final case class Deposited(amount: Double) extends Event
  final case class Withdrawn(amount: Double) extends Event
  final case class AccountClosed() extends Event

  // Responses
  sealed trait Response extends CborSerializable
  final case class CurrentBalance(balance: Double) extends Response
  final case class OperationSuccess(message: String) extends Response
  final case class OperationFailure(reason: String) extends Response

  // State - current state of the account
  final case class State(balance: Double, isOpen: Boolean) extends CborSerializable {
    def applyEvent(event: Event): State = event match {
      case Deposited(amount) => copy(balance = balance + amount)
      case Withdrawn(amount) => copy(balance = balance - amount)
      case AccountClosed()   => copy(isOpen = false)
    }
  }

  def apply(accountId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(accountId),
      emptyState = State(balance = 0.0, isOpen = true),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
  }

  private val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case Deposit(amount, replyTo) =>
        if (!state.isOpen) {
          Effect.reply(replyTo)(OperationFailure("Account is closed"))
        } else if (amount <= 0) {
          Effect.reply(replyTo)(OperationFailure("Deposit amount must be positive"))
        } else {
          Effect
            .persist(Deposited(amount))
            .thenReply(replyTo)(_ => OperationSuccess(s"Deposited $$${amount}"))
        }

      case Withdraw(amount, replyTo) =>
        if (!state.isOpen) {
          Effect.reply(replyTo)(OperationFailure("Account is closed"))
        } else if (amount <= 0) {
          Effect.reply(replyTo)(OperationFailure("Withdrawal amount must be positive"))
        } else if (amount > state.balance) {
          Effect.reply(replyTo)(OperationFailure("Insufficient funds"))
        } else {
          Effect
            .persist(Withdrawn(amount))
            .thenReply(replyTo)(_ => OperationSuccess(s"Withdrawn $$${amount}"))
        }

      case GetBalance(replyTo) =>
        Effect.reply(replyTo)(CurrentBalance(state.balance))

      case CloseAccount(replyTo) =>
        if (!state.isOpen) {
          Effect.reply(replyTo)(OperationFailure("Account already closed"))
        } else {
          Effect
            .persist(AccountClosed())
            .thenReply(replyTo)(_ => OperationSuccess("Account closed"))
        }
    }
  }

  private val eventHandler: (State, Event) => State = { (state, event) =>
    state.applyEvent(event)
  }
}
