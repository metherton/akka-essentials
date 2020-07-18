package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {

  val aThread = new Thread(() => println("I'm running in parallel"))
  aThread.start()
  aThread.join()

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("goodbye")))

  threadHello.start()
  threadGoodbye.start()

  //different runs produces different results


  // Scala futures
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    // long computation
    43
  }
  future.onComplete {
    case Success(43) => println("Meaning of life")
    case Failure(_) => println("something happened")
  }
}
