package part2Primer

import java.nio.file.attribute.{FileTime, PosixFileAttributes, PosixFilePermissions}
import java.nio.file.{Files, Path, Paths}
import java.time.Instant

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}

object FirstPrincipals extends App {

  implicit val system = ActorSystem("FirstPrincipals")

  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val files = Source(List(("/Users/martin/source.txt", "/Users/martin/dest.txt"), ("/Users/martin/source2.txt", "/Users/martin/dest2.txt")))
//  files.to(Sink.foreach[(String, String)](n => {
//    val s1 = Paths.get(n._1)
//    val d1 = Paths.get(n._2)
//    import java.nio.file.Files
//    Files.copy(s1, d1)
//    Files.setLastModifiedTime(Paths.get(n._2), FileTime.from(Instant.ofEpochSecond(9000000000L)))
//    Files.setPosixFilePermissions(d1, PosixFilePermissions.fromString("r--r--r--"))
//  })).run()

  val flow = Flow[(String, String)].map(t => t._2)

  val sink = Sink.foreach[String](println)
  val result = files.viaMat(flow)(Keep.right).toMat(sink)(Keep.right).run()
  result.onComplete {
    case Success(value) => println(value)
    case Failure(exception) => println(exception)
  }


  val source = Source(1 to 10)
  val sink1 = Sink.fold[Int, Int](0)(_ + _)

  val forEachSink = Sink.foreach[Int](x => {
    println(x)
  })
  val bla = Source(1 to 10).runWith(forEachSink)

  // materialize the flow, getting the Sinks materialized value
  val sum = source.runWith(sink1)


  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => x + 1)
  val simpleSink = Sink.foreach[Int](println)

  val graph = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)

  system.terminate()

}
