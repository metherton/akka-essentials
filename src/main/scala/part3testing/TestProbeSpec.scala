package part3testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class TestProbeSpec extends TestKit(ActorSystem("TestProbeSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master actor" should {
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }

    "send the work to the slave actor" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workloadstring = "I love akka"
      master ! Work(workloadstring)

      // the interaction between the master and slave actors
      slave.expectMsg(SlaveWork(workloadstring, testActor))

      slave.reply(WorkCompleted(3, testActor))

      expectMsg(Report(3)) // test actor receives the Report(3)
    }

    "aggregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workloadstring = "I love akka"
      master ! Work(workloadstring)
      master ! Work(workloadstring)

      // in the meantime i don't have a slave actor
      slave.receiveWhile() {
        case SlaveWork(`workloadstring`, `testActor`) => slave.reply(WorkCompleted(3, testActor))
      }

      expectMsg(Report(3)) // test actor receives the Report(3)
      expectMsg(Report(6)) // test actor receives the Report(6)

    }
  }

}

object TestProbeSpec {

  // scenario
  /*
      word counting actor hierarchy master-slave

      send some work to master
        - master sends slave piece of work
        - slave processes the work and replies to master
        - master aggregates the result

      master sends the total count to the original requestor

   */

  case class Work(text: String)
  case class Register(slaveRef: ActorRef)
  case class SlaveWork(text: String, originalRequestor: ActorRef)
  case class WorkCompleted(count: Int, originalRequestor: ActorRef)
  case class Report(totalCount: Int)
  case object RegistrationAck

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0))
      case _ => // ignore
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequestor) =>
        val newTotalWordCount = totalWordCount + count
        originalRequestor ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))
    }
  }

  // class Slave extends Actor

}
