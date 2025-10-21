package com.akkalearning.examples

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.akkalearning.persistence.ShoppingCartActor

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

/**
 * Example demonstrating shopping cart with snapshots
 */
object ShoppingCartExample extends App {

  println("=" * 60)
  println("Shopping Cart with Event Sourcing & Snapshots")
  println("=" * 60)
  println("\nThis example demonstrates:")
  println("- Complex state management with multiple event types")
  println("- Snapshot optimization for faster recovery")
  println("- PostgreSQL persistence")
  println("=" * 60)

  val system = ActorSystem(Behaviors.setup[String] { context =>
    
    implicit val ec: ExecutionContext = context.executionContext
    import akka.actor.typed.scaladsl.AskPattern._
    import akka.util.Timeout
    implicit val timeout: Timeout = 5.seconds
    implicit val scheduler = context.system.scheduler

    // Create a shopping cart
    val cartId = s"user-${System.currentTimeMillis()}"
    println(s"\nCreating shopping cart for: $cartId")
    val cart = context.spawn(ShoppingCartActor(cartId), s"cart-$cartId")

    println("\n" + "-" * 60)
    println("Adding items to cart...")
    println("-" * 60)

    // Add items
    cart.ask(ShoppingCartActor.AddItem("book-1", "Akka in Action", 1, 39.99, _))
      .onComplete {
        case Success(response) => println(s"✓ $response")
        case Failure(ex)       => println(s"✗ Failed: ${ex.getMessage}")
      }
    Thread.sleep(300)

    cart.ask(ShoppingCartActor.AddItem("book-2", "Reactive Design Patterns", 2, 44.99, _))
      .onComplete {
        case Success(response) => println(s"✓ $response")
        case Failure(ex)       => println(s"✗ Failed: ${ex.getMessage}")
      }
    Thread.sleep(300)

    cart.ask(ShoppingCartActor.AddItem("course-1", "Scala Course", 1, 99.00, _))
      .onComplete {
        case Success(response) => println(s"✓ $response")
        case Failure(ex)       => println(s"✗ Failed: ${ex.getMessage}")
      }
    Thread.sleep(300)

    // Update quantity
    println("\nUpdating item quantity...")
    cart.ask(ShoppingCartActor.UpdateQuantity("book-2", 3, _))
      .onComplete {
        case Success(response) => println(s"✓ $response")
        case Failure(ex)       => println(s"✗ Failed: ${ex.getMessage}")
      }
    Thread.sleep(300)

    // Get cart contents
    println("\n" + "-" * 60)
    println("Current cart contents:")
    println("-" * 60)
    cart.ask(ShoppingCartActor.GetCart)
      .onComplete {
        case Success(ShoppingCartActor.CartContent(items, total, itemCount)) =>
          items.foreach { item =>
            println(f"  ${item.name}%-30s x${item.quantity} @ $$${item.price}%.2f = $$${item.total}%.2f")
          }
          println("-" * 60)
          println(f"  Total items: $itemCount")
          println(f"  Total amount: $$${total}%.2f")
        case Failure(ex) =>
          println(s"✗ Failed to get cart: ${ex.getMessage}")
      }
    Thread.sleep(500)

    // Checkout
    println("\n" + "-" * 60)
    println("Processing checkout...")
    println("-" * 60)
    cart.ask(ShoppingCartActor.Checkout)
      .onComplete {
        case Success(response) => println(s"✓ $response")
        case Failure(ex)       => println(s"✗ Failed: ${ex.getMessage}")
      }
    Thread.sleep(500)

    // Try to add item after checkout (should fail)
    println("\nTrying to add item to checked out cart...")
    cart.ask(ShoppingCartActor.AddItem("book-3", "Another Book", 1, 29.99, _))
      .onComplete {
        case Success(response) => println(s"  $response")
        case Failure(ex)       => println(s"✗ Failed: ${ex.getMessage}")
      }
    Thread.sleep(500)

    println("\n" + "=" * 60)
    println("Key Features Demonstrated:")
    println("=" * 60)
    println("✓ Multiple event types (add, remove, update, checkout)")
    println("✓ Complex state with item collections")
    println("✓ Business logic validation")
    println("✓ Snapshots (configured every 20 events)")
    println("✓ State machine (open → checked out)")
    println("=" * 60)

    Behaviors.receiveMessage { msg =>
      println(s"Guardian received: $msg")
      Behaviors.same
    }
  }, "ShoppingCartExampleSystem")

  // Keep system alive then shutdown
  Thread.sleep(4000)
  println("\nShutting down...")
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
