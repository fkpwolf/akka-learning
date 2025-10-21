package com.akkalearning.basic

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.akkalearning.CborSerializable

/**
 * A simple Hello World actor demonstrating basic actor patterns
 */
object HelloWorldActor {

  // Messages this actor can receive
  sealed trait Command extends CborSerializable
  final case class Greet(whom: String, replyTo: ActorRef[Greeted]) extends Command
  final case class Greeted(whom: String, from: ActorRef[Greet]) extends CborSerializable

  def apply(): Behavior[Command] = Behaviors.receive { (context, message) =>
    message match {
      case Greet(whom, replyTo) =>
        context.log.info("Hello {}!", whom)
        replyTo ! Greeted(whom, context.self)
        Behaviors.same
    }
  }
}

/**
 * An actor that initiates greetings
 */
object HelloWorldBot {
  def apply(max: Int): Behavior[HelloWorldActor.Greeted] = {
    bot(0, max)
  }

  private def bot(greetingCounter: Int, max: Int): Behavior[HelloWorldActor.Greeted] =
    Behaviors.receive { (context, message) =>
      val n = greetingCounter + 1
      context.log.info("Greeting {} for {}", n, message.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        message.from ! HelloWorldActor.Greet(message.whom, context.self)
        bot(n, max)
      }
    }
}
