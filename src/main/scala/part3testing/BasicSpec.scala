package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import scala.concurrent.duration._

object BasicSpec {

  class SimpleActor extends Actor {
    override def receive: Receive = {

      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    override def receive: Receive = {
      case message: String => sender() ! message.toUpperCase
    }
  }

}

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "a SimpleActor" should {
    "should back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message
      expectMsg(message)  // akka.test.single-expert-default..we can tweak this configuration
    }
  }

  "a BlackHole" should {
    "should send back some messga" in {
      val blackhole = system.actorOf(Props[BlackHole])
      val message = "hello, test"
      blackhole ! message
      expectNoMessage(1 second)
    }
  }

  // message assertions
  "A lab test actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into upper case" in {
      labTestActor ! "I love akka"
      val reply = expectMsgType[String]
      assert( reply == "I LOVE AKKA")
    }
  }


}
