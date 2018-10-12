package model

object Year {
  val all: Seq[Int] = {
    (1987 to 2017).toSeq diff Seq(1989)
  }
}
