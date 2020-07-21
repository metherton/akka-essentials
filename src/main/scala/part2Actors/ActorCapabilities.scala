package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

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

}
