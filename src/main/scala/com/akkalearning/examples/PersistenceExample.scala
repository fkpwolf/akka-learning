package com.akkalearning.examples

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.akkalearning.persistence.BankAccountActor

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

/**
 * Example demonstrating persistent actors with event sourcing
 */
object PersistenceExample extends App {

  println("=" * 60)
  println("Event Sourcing & Persistence Example")
  println("=" * 60)
  println("\nThis example uses PostgreSQL to persist actor state.")
  println("Make sure PostgreSQL is running: docker-compose up -d")
  println("=" * 60)

  val system = ActorSystem(Behaviors.setup[String] { context =>
    
    implicit val ec: ExecutionContext = context.executionContext
    import akka.actor.typed.scaladsl.AskPattern._
    import akka.util.Timeout
    implicit val timeout: Timeout = 5.seconds
    implicit val scheduler = context.system.scheduler

    // Create a bank account actor with a unique ID
    val accountId = s"account-${System.currentTimeMillis()}"
    println(s"\nCreating bank account: $accountId")
    val account = context.spawn(BankAccountActor(accountId), accountId)

    // Perform operations
    println("\n" + "-" * 60)
    println("Performing operations on account...")
    println("-" * 60)

    // Deposit
    val deposit1 = account.ask(BankAccountActor.Deposit(100.0, _))
    deposit1.onComplete {
      case Success(response) => println(s"✓ Deposit: $response")
      case Failure(ex)       => println(s"✗ Deposit failed: ${ex.getMessage}")
    }
    Thread.sleep(500)

    // Another deposit
    val deposit2 = account.ask(BankAccountActor.Deposit(50.0, _))
    deposit2.onComplete {
      case Success(response) => println(s"✓ Deposit: $response")
      case Failure(ex)       => println(s"✗ Deposit failed: ${ex.getMessage}")
    }
    Thread.sleep(500)

    // Withdraw
    val withdraw1 = account.ask(BankAccountActor.Withdraw(30.0, _))
    withdraw1.onComplete {
      case Success(response) => println(s"✓ Withdraw: $response")
      case Failure(ex)       => println(s"✗ Withdraw failed: ${ex.getMessage}")
    }
    Thread.sleep(500)

    // Check balance
    val balance1 = account.ask(BankAccountActor.GetBalance)
    balance1.onComplete {
      case Success(BankAccountActor.CurrentBalance(balance)) =>
        println(s"✓ Current balance: $${balance}")
      case Failure(ex) =>
        println(s"✗ Failed to get balance: ${ex.getMessage}")
    }
    Thread.sleep(500)

    // Try to withdraw more than balance (should fail)
    val withdraw2 = account.ask(BankAccountActor.Withdraw(200.0, _))
    withdraw2.onComplete {
      case Success(response) => println(s"✓ Withdraw: $response")
      case Failure(ex)       => println(s"✗ Withdraw failed: ${ex.getMessage}")
    }
    Thread.sleep(500)

    println("\n" + "-" * 60)
    println("Key Concepts Demonstrated:")
    println("-" * 60)
    println("✓ Event Sourcing: All events (deposits/withdrawals) are stored")
    println("✓ State Recovery: Actor can recover state from persisted events")
    println("✓ Command Validation: Invalid operations are rejected")
    println("✓ PostgreSQL Storage: Events stored in database for durability")
    println("=" * 60)

    println("\nTip: Restart this app to see the actor recover from persisted events!")
    println("     Check PostgreSQL tables: journal, snapshot")

    Behaviors.receiveMessage { msg =>
      println(s"Guardian received: $msg")
      Behaviors.same
    }
  }, "PersistenceExampleSystem")

  // Keep system alive for a bit then shutdown
  Thread.sleep(5000)
  println("\nShutting down...")
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
