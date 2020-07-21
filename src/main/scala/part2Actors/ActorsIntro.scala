package part2Actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  //part1 - actor system
  val actorSystem = ActorSystem("FirstActorSystem")
  println(actorSystem.name)

  // part 2 - create actors
  // word count actor
  class WordCountActor extends Actor {
    //internal data
    var totalWords = 0
    def receive: PartialFunction[Any, Unit] = {

      case message: String =>
        println(s"I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter]I cannot understand ${msg.toString}")
    }
  }

  // part 3 - instantiate actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")
  wordCounter ! "I am learning akka and it is cool"
  anotherWordCounter ! "A different message"
  // asynchronous


  // best practise for defining actor objects with properties...use companion object like this
  object Person {
    def props(name: String) = Props(new Person(name))
  }
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi my name is $name")
      case _ =>
    }
  }


  val person = actorSystem.actorOf(Person.props("Bob"))
  person ! "hi"
}
