package part1recap

import scala.concurrent.Future

object AdvancedRecap extends App {

  //partial functions
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }


  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val function: (Int => Int) = partialFunction

  val modifiedList = List(1,2,3) map {
    case 1 => 42
    case _ => 0
  }

  // lifting
  val lifted = partialFunction.lift //total function Int => Option[Int]
  lifted(2) // Some(65)
  lifted(5000) // None
  println(lifted(6000))

  // orElse
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }

  println(pfChain(60))


  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("hello")
    case _ => println("confused")
  }

  //implicits
  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  setTimeout(() => println("timeout")) // extra parameter omitted

  //implicit conversions
  // 1: implicit defs
  case class Person(name: String) {
    def greet = s"hi my name is $name"
  }

  implicit def fromStringToPerson(string: String): Person = Person(string)

  println("Perter".greet)

  // implicit classes
  implicit class Dog(name: String) {
    def bark = println("bark")
  }

  "Lassie".bark


  // organize
  // local scope
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  println(List(1,2,3).sorted)

  //imported scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("hello future")
  }

  // companion objects of the types included in the calls
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  println(List(Person("Bob"), Person("Alice")).sorted)

}
