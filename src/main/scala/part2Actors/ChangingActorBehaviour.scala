package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChangingActorBehaviour.Counter.{Decrement, Increment, Print}
import part2Actors.ChangingActorBehaviour.FussyKid.{KidAccept, KidReject}
import part2Actors.ChangingActorBehaviour.Mom.{Ask, CHOCOLATE, Food, MomStart, VEGETABLE}

object ChangingActorBehaviour extends App {


  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    //internal state of kid
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }


  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)// change my receive handler to sadReceive
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)// stay sad
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocalate"
  }
  class Mom extends Actor {
    import Mom._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test our interaction
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("do you want to play ?")
      case KidAccept => println("Yay my kid is happy")
      case KidReject => println("My kid is sad, but at least he's healthy")

    }
  }

  val system = ActorSystem("changingActorBehaviourDemo")
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  mom ! MomStart(statelessFussyKid)

  /*
      mom receives MomStart
        kid receives Food(veg) => kid will change the handler to sadreceive
        kid receives Ask(play?)
        kid responds with the sadreceive handler =>
      mom receives KidReject
   */

  /*

  context.become

      Food(veg) -> message handler turns to sadReceive - stack.push(sadReceive)
      Food(chocolate) -> message handler turns to happyReceive - stack.push(happyReceive)

      Stack:
        1. happyReceive
        2. sadReceive
        3. happyRecieve


   */

  /*
      new behaviour

      Food(veg)
      Food(veg)
      Food(chocalate)

      Stack:
      1. happyReceive

   */

  /*
      Exercises
      1 - recreate Counter actor with context.become and no mutable state
   */

  // DOMAIN of the COUNTER
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment =>
        println(s"countReceive($currentCount) incrementing")
        context.become(countReceive(currentCount + 1))
      case Decrement =>
        println(s"countReceive($currentCount) decrementing")
        context.become(countReceive(currentCount - 1))
      case Print => println(s"[counter] my current Counter value is: $currentCount")
      case _ => println("unknown message")
    }

  }

  val counterActor = system.actorOf(Props[Counter], "counterActor")
  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print



  /*
      Exercises
      2 - simplified voting system
   */

  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = ??? // TODO
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {
    override def receive: Receive = ??? // TODO
  }

//  val alice = system.actorOf(Props[Citizen])
//  val bob = system.actorOf(Props[Citizen])
//  val charlie = system.actorOf(Props[Citizen])
//  val daniel = system.actorOf(Props[Citizen])
//
//  alice ! Vote("Martin")
//  bob ! Vote("Jonas")
//  charlie ! Vote("Roland")
//  daniel ! Vote("Roland")
//
//  val voteAggregator = system.actorOf(Props[VoteAggregator])
//  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /*
      Print the status of votes

      Martin -> 1
      Jonas -> 1
      Roland -> 2

   */


}
