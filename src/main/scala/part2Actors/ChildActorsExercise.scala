package part2Actors

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2Actors.ChildActorsExercise.WordCounterMaster.{Initialize, WordCountReply, WordCountTask}

object ChildActorsExercise extends App {


  // Distributed word counting

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }


  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case WordCountTask(id, text) => {
        println(s"${self.path} I received task $id with $text")
        sender() ! WordCountReply(id, text.split(" ").length)
      }
    }
  }

  class WordCounterMaster extends Actor {

    override def receive: Receive = {
      case Initialize(nCounters) => {
        println("[master] initializing...")
        val childrenRefs = for (i <- 1 to nCounters) yield context.actorOf(Props[WordCounterWorker], s"wcw_$i")
        context.become(withChildren(childrenRefs, 0, 0, Map()))
      }
    }


    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case text: String => {
        println(s"[master] I have received: $text - I will send it to $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, text)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      }
      case WordCountReply(id, count) => {
        // problem - sender ??
        println(s"[master] I have received a reply for task $id with $count")
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
      }
    }
  }



  /*
      create WordCounterMaster
      send Initialize(10) to WordCounterMaster
      send "Akka is awesome" to wordCounterMaster
        wcm will send a WordCountTask("...") to one of its children
          child replies with a WordCountTaskReply(3) to the master
        master replies with 3 to the sender


      requestor -> wcm -> wcw
      requestor <- wcm <-
   */
    // round robin logic
  // 1,2,3,4,5 children ... 7 tasks
  // schedule 1,2,3,4,5,1,2

  // HINT: You might need to pass in some extra information to WordCountTask and WordCountReply

//  val wcm = system.actorOf(Props[WordCounterMaster], "master")
//  wcm ! Initialize(3)
//  wcm ! "Akka is awesome"



  class TestActor extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case "go" => {
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)
        val texts = List("I love akka", "Scala is super dope", "yes", "me too")
        texts.foreach(text => master ! text)
      }
      case count: Int =>
        println(s"[test actor] I have received a reply: $count")
    }
  }

  val system = ActorSystem("roundRobinWordCountExercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "go"

}
