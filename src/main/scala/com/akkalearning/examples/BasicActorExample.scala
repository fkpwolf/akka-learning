package com.akkalearning.examples

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.akkalearning.basic.{CounterActor, HelloWorldActor, HelloWorldBot}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

/**
 * Example demonstrating basic actor patterns
 */
object BasicActorExample extends App {

  println("=" * 60)
  println("Basic Actor Model Examples")
  println("=" * 60)

  val system = ActorSystem(Behaviors.setup[String] { context =>
    
    // Example 1: Hello World Pattern
    println("\n1. Hello World Actor Pattern")
    println("-" * 40)
    val greeter = context.spawn(HelloWorldActor(), "greeter")
    val bot = context.spawn(HelloWorldBot(max = 3), "bot")
    greeter ! HelloWorldActor.Greet("World", bot)
    
    Thread.sleep(1000)

    // Example 2: Counter Actor (Stateful)
    println("\n2. Stateful Counter Actor")
    println("-" * 40)
    val counter = context.spawn(CounterActor(), "counter")
    
    implicit val ec: ExecutionContext = context.executionContext
    
    // Use ask pattern to get responses
    import akka.actor.typed.scaladsl.AskPattern._
    import akka.util.Timeout
    implicit val timeout: Timeout = 3.seconds
    implicit val scheduler = context.system.scheduler
    
    counter ! CounterActor.Increment(5)
    counter ! CounterActor.Increment(3)
    counter ! CounterActor.Decrement(2)
    
    Thread.sleep(500)
    
    val valueFuture = counter.ask(CounterActor.GetValue)
    valueFuture.onComplete {
      case Success(CounterActor.Value(value)) =>
        println(s"Final counter value: $value")
      case Failure(ex) =>
        println(s"Failed to get value: ${ex.getMessage}")
    }
    
    Thread.sleep(1000)

    println("\n" + "=" * 60)
    println("Basic examples completed!")
    println("=" * 60)

    Behaviors.receiveMessage { msg =>
      println(s"Guardian received: $msg")
      Behaviors.same
    }
  }, "BasicActorExampleSystem")

  // Keep system alive for a bit then shutdown
  Thread.sleep(3000)
  println("\nShutting down...")
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
