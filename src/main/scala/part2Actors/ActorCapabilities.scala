package part2Actors

import akka.actor.FSM.Failure
import akka.actor.Status.Success
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ActorCapabilities.BankAccount.{Deposit, Statement, Withdraw}
import part2Actors.ActorCapabilities.Counter.{Decrement, Increment, Print}
import part2Actors.ActorCapabilities.Person.LiveTheLife

import scala.collection.mutable.ListBuffer

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender ! "Hello there"
      case message: String => println(s"${self} I have received $message")
      case number: Int => println(s"[SimpleActor] I have received a number $number")
      case SpecialMessage(content) => println(s"[SimpleActor] I have received something special $content")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) => ref ! "Hi!" // alice is being passed as the sender
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s")
    }
  }

  val system = ActorSystem("actorCapabilities")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  simpleActor ! "hello actor"

  // 1 - messages can be of any type
  // a - messages must be immutable
  // b - messages must be serializable

  // in practise use case classes and case objects
  simpleActor ! 42 // who is the sender

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 actors have information about their context and about themselves
  // context.self === this

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and i am proud of it")

  // 3 Actors can reply
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi" // reply to me

  // 5 - forwarding messages
  // D -> A -> B
  // forwarding - sending a message with the original sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob) // nosender

  /**
    * Exercises
    *
    * 1. Create a counter actor
    *   Increment
    *   Decrement
    *   Print
    *
    *
    * 2. a BankAccount as actor
    *   - deposit an amount // design the messages themselves..
    *   both the receiving and the replies
    *   and the logic for the bank actor itself...
    *   probably some internal variable to hold the funds
    *
    *   take care of edge cases
    *
    *   - withdraw an amount
    *   - Statement
    *   replies with
    *   - Success
    *   - Failure
    *
    *  TIP: Interact with some other kind of actor..
    *  which will send withdraw / deposit message to bankaccount
    *  will interpret and print success failure message from bankaccount
    */


  // DOMAIN of the COUNTER
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._
    var count = 0

    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"Counter value is: ${count}")
      case _ => println("unknown message")
    }
  }

  val counterActor = system.actorOf(Props[Counter], "counterActor")
  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  object BankAccount {
    case class Withdraw(amount: Int)
    case class Deposit(amount: Int)
    case object Statement
    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }


  class BankAccount extends Actor {
    import BankAccount._
    var funds = 0

    override def receive: Receive = {
      case Withdraw(money) =>
        if (money < 0) sender ! TransactionFailure("invalid transaction account")
        else if (money > funds)
          sender ! TransactionFailure("insufficient funds")
        else {
          funds -= money
          sender ! TransactionSuccess(s"successfully withdrew ${money}")
        }
      case Deposit(money) =>
        if (money < 0) sender ! TransactionFailure("invalid transaction account")
        else {
          funds += money
          sender ! TransactionSuccess(s"successfully deposited ${money}")
        }
      case Statement => sender ! s"Your balance is ${funds}"
    }
  }



  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {
    import Person._
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

  val account = system.actorOf(Props[BankAccount], "bankAccount")
  val person = system.actorOf(Props[Person], "billionaire")

  person ! LiveTheLife(account)

}
