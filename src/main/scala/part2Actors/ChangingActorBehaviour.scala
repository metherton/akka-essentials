package part2Actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChangingActorBehaviour.FussyKid.{KidAccept, KidReject}
import part2Actors.ChangingActorBehaviour.Mom.MomStart

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
      case Food(VEGETABLE) => // stay sad
      case Food(CHOCOLATE) => context.become(happyReceive, false)
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
      Food(veg) -> message handler turns to sadReceive - stack.push(sadReceive)
      Food(chocolate) -> message handler turns to happyReceive - stack.push(happyReceive)

      Stack:
        1. happyReceive
        2. sadReceive
        3. happyRecieve


   */

}
