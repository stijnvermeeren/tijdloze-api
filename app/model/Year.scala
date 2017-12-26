package model

object Year {
  val all: Seq[Int] = {
    (1987 to 2016).toSeq diff Seq(1989)
  }
}
