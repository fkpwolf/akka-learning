package com.akkalearning.basic

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.akkalearning.CborSerializable

/**
 * Counter actor demonstrating stateful behavior
 */
object CounterActor {

  sealed trait Command extends CborSerializable
  final case class Increment(amount: Int) extends Command
  final case class Decrement(amount: Int) extends Command
  final case class GetValue(replyTo: ActorRef[Value]) extends Command
  final case class Reset(replyTo: ActorRef[Value]) extends Command

  final case class Value(value: Int) extends CborSerializable

  def apply(): Behavior[Command] = counter(0)

  private def counter(currentValue: Int): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      message match {
        case Increment(amount) =>
          val newValue = currentValue + amount
          context.log.info("Incremented by {} to {}", amount, newValue)
          counter(newValue)

        case Decrement(amount) =>
          val newValue = currentValue - amount
          context.log.info("Decremented by {} to {}", amount, newValue)
          counter(newValue)

        case GetValue(replyTo) =>
          replyTo ! Value(currentValue)
          Behaviors.same

        case Reset(replyTo) =>
          context.log.info("Reset counter from {} to 0", currentValue)
          replyTo ! Value(0)
          counter(0)
      }
    }
  }
}
