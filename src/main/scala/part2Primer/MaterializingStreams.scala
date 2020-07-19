package part2Primer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object MaterializingStreams extends App {

  implicit val system = ActorSystem("MaterializingStreams")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val simpleGraph = Source(1 to 10).to(Sink.foreach(println))
 // val simpleMaterializedValue = simpleGraph.run()

  val source = Source(1 to 10)
  val sink = Sink.reduce[Int]((a, b) => a + b)
  val sumFuture = source.runWith(sink)

  sumFuture.onComplete {
    case Success(value) => println(s"The sum of all element is: $value")
    case Failure(ex) => println(s"the sum of the int could not be computed: $ex")
  }

  // choosing materialized values
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach[Int](println)
  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right) //  simpleSource.viaMat(simpleFlow)((sourceMat, flowMat) => flowMat)
  graph.run().onComplete {
    case Success(_) => "Stream processing finished"
    case Failure(ex) => println(s"Stream processing finished with: $ex")
  }

}
