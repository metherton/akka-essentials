package part1recap

object GeneralRecap extends App {


  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(42)

  val anonymousIncrementer = (x: Int) => x + 1

  println(List(1,2,3).map(incrementer))

  // for comprehensions
  val pairs = for {
    num <- List(1,2,3,4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char

  // List(1,2,3,4).flatMap(num => List('a','b','c','d').map(char => num + "-" + char))

  val fm = List(1,2,3,4).flatMap(x => Seq(x + 1))
  println(fm)
}
