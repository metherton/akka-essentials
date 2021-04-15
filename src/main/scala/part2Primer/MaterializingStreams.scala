package part2Primer

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.Future
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


  val longSource: Source[Int, NotUsed] = Source(List(1, 2))
  longSource.runWith(sink)

  val bla = source.grouped(2).to(Sink.foreach(println))
  bla.run()

  sumFuture.onComplete {
    case Success(value) => println(s"The sum of all element is: $value")
    case Failure(ex) => println(s"the sum of the int could not be computed: $ex")
  }

  // choosing materialized values
  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach[Int](println)
  // when in doubt always use viaMat and toMat...it gives you control over which materialized value you get at the end
  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right) //  simpleSource.viaMat(simpleFlow)((sourceMat, flowMat) => flowMat)
  graph.run().onComplete {
    case Success(_) => "Stream processing finished"
    case Failure(ex) => println(s"Stream processing finished with: $ex")
  }

  // sugars
  val sum: Future[Int] = Source(1 to 10).runWith(Sink.reduce(_ + _)) // Source(1 to 10).to(Sink.reduce)(Keep.right)
  Source(1 to 10).runReduce(_ + _)

  // backwards
  Sink.foreach[Int](println).runWith(Source.single(42)) // source(..).to(sink..).run()

  // both ways
  Flow[Int].map(x => 2 * x).runWith(simpleSource, simpleSink)

  /**
    * Exercises..
    * return the last element out of a source (use Sink.last)
    * Compute the word count out of a stream of sentences
    * use map op on flow, fold op on flow and sink, reduce op on flow and sink
    */

  val f1 = Source(1 to 10).toMat(Sink.last)(Keep.right).run()
  val f2 = Source(1 to 10).runWith(Sink.last)

  val sentenceSource = Source(List(
    "Akka is awesome",
    "I love streams",
    "Materialized values are killing me"
  ))
  val wordCountSink = Sink.fold[Int, String](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g1 = sentenceSource.toMat(wordCountSink)(Keep.right).run()
  val g2 = sentenceSource.runWith(wordCountSink)
  val g3 = sentenceSource.runFold(0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)

  val wordCountFlow = Flow[String].fold[Int](0)((currentWords, newSentence) => currentWords + newSentence.split(" ").length)
  val g4 = sentenceSource.via(wordCountFlow).toMat(Sink.head)(Keep.right).run()
  val g5 = sentenceSource.viaMat(wordCountFlow)(Keep.left).toMat(Sink.head)(Keep.right).run()
  val g6 = sentenceSource.via(wordCountFlow).runWith(Sink.head)
  val g7 = wordCountFlow.runWith(sentenceSource, Sink.head)._2

  // this is my solution
  val mappedSource = sentenceSource.map(sentence => sentence.split(" "))
  val flowFoldCount = Flow[Array[String]].fold(0)((count, sentenceWords) => count + sentenceWords.length)
  val wordCountFlow2 = mappedSource.viaMat(flowFoldCount)(Keep.left).runWith(Sink.head)
  wordCountFlow2.onComplete {
    case Success(value) => println(s"wordCount is : $value")
    case Failure(ex) => println(s"Unable to get word count: $ex")
  }
}
