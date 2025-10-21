package com.akkalearning.persistence

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import com.akkalearning.CborSerializable

import scala.concurrent.duration._

/**
 * Shopping Cart Actor with Event Sourcing and Snapshots
 * 
 * Demonstrates:
 * - Event sourcing for state recovery
 * - Snapshots for performance optimization
 * - Multiple event types
 * - Complex state management
 */
object ShoppingCartActor {

  // Commands
  sealed trait Command extends CborSerializable
  final case class AddItem(itemId: String, name: String, quantity: Int, price: Double, replyTo: ActorRef[Response]) 
    extends Command
  final case class RemoveItem(itemId: String, replyTo: ActorRef[Response]) extends Command
  final case class UpdateQuantity(itemId: String, quantity: Int, replyTo: ActorRef[Response]) extends Command
  final case class Checkout(replyTo: ActorRef[Response]) extends Command
  final case class GetCart(replyTo: ActorRef[Response]) extends Command

  // Events
  sealed trait Event extends CborSerializable
  final case class ItemAdded(itemId: String, name: String, quantity: Int, price: Double) extends Event
  final case class ItemRemoved(itemId: String) extends Event
  final case class QuantityUpdated(itemId: String, quantity: Int) extends Event
  final case class CheckedOut(totalAmount: Double, timestamp: Long) extends Event

  // State
  final case class CartItem(itemId: String, name: String, quantity: Int, price: Double) extends CborSerializable {
    def total: Double = quantity * price
  }

  final case class State(
      items: Map[String, CartItem] = Map.empty,
      checkedOut: Boolean = false,
      checkoutTimestamp: Option[Long] = None
  ) extends CborSerializable {
    
    def totalAmount: Double = items.values.map(_.total).sum
    def itemCount: Int = items.values.map(_.quantity).sum

    def applyEvent(event: Event): State = event match {
      case ItemAdded(itemId, name, quantity, price) =>
        val existingItem = items.get(itemId)
        val newItem = existingItem match {
          case Some(item) => item.copy(quantity = item.quantity + quantity)
          case None       => CartItem(itemId, name, quantity, price)
        }
        copy(items = items + (itemId -> newItem))

      case ItemRemoved(itemId) =>
        copy(items = items - itemId)

      case QuantityUpdated(itemId, quantity) =>
        items.get(itemId) match {
          case Some(item) => copy(items = items + (itemId -> item.copy(quantity = quantity)))
          case None       => this
        }

      case CheckedOut(_, timestamp) =>
        copy(checkedOut = true, checkoutTimestamp = Some(timestamp))
    }
  }

  // Responses
  sealed trait Response extends CborSerializable
  final case class CartContent(items: List[CartItem], total: Double, itemCount: Int) extends Response
  final case class OperationSuccess(message: String) extends Response
  final case class OperationFailure(reason: String) extends Response

  def apply(cartId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(s"shopping-cart-$cartId"),
      emptyState = State(),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    ).withRetention(
      // Take snapshot every 20 events and keep 2 snapshots
      RetentionCriteria.snapshotEvery(numberOfEvents = 20, keepNSnapshots = 2)
    )
  }

  private val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    if (state.checkedOut && !command.isInstanceOf[GetCart]) {
      Effect.reply(command match {
        case AddItem(_, _, _, _, replyTo)       => replyTo
        case RemoveItem(_, replyTo)             => replyTo
        case UpdateQuantity(_, _, replyTo)      => replyTo
        case Checkout(replyTo)                  => replyTo
        case GetCart(replyTo)                   => replyTo
      })(OperationFailure("Cart is already checked out"))
    } else {
      command match {
        case AddItem(itemId, name, quantity, price, replyTo) =>
          if (quantity <= 0) {
            Effect.reply(replyTo)(OperationFailure("Quantity must be positive"))
          } else if (price <= 0) {
            Effect.reply(replyTo)(OperationFailure("Price must be positive"))
          } else {
            Effect
              .persist(ItemAdded(itemId, name, quantity, price))
              .thenReply(replyTo)(_ => OperationSuccess(s"Added $quantity x $name"))
          }

        case RemoveItem(itemId, replyTo) =>
          if (!state.items.contains(itemId)) {
            Effect.reply(replyTo)(OperationFailure(s"Item $itemId not in cart"))
          } else {
            Effect
              .persist(ItemRemoved(itemId))
              .thenReply(replyTo)(_ => OperationSuccess(s"Removed item $itemId"))
          }

        case UpdateQuantity(itemId, quantity, replyTo) =>
          if (!state.items.contains(itemId)) {
            Effect.reply(replyTo)(OperationFailure(s"Item $itemId not in cart"))
          } else if (quantity <= 0) {
            Effect.reply(replyTo)(OperationFailure("Quantity must be positive"))
          } else {
            Effect
              .persist(QuantityUpdated(itemId, quantity))
              .thenReply(replyTo)(_ => OperationSuccess(s"Updated quantity to $quantity"))
          }

        case Checkout(replyTo) =>
          if (state.items.isEmpty) {
            Effect.reply(replyTo)(OperationFailure("Cannot checkout empty cart"))
          } else {
            val total = state.totalAmount
            val timestamp = System.currentTimeMillis()
            Effect
              .persist(CheckedOut(total, timestamp))
              .thenReply(replyTo)(_ => OperationSuccess(f"Checked out: $$${total}%.2f"))
          }

        case GetCart(replyTo) =>
          Effect.reply(replyTo)(
            CartContent(state.items.values.toList, state.totalAmount, state.itemCount)
          )
      }
    }
  }

  private val eventHandler: (State, Event) => State = { (state, event) =>
    state.applyEvent(event)
  }
}
