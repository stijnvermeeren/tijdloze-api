package util

import scala.concurrent.{ExecutionContext, Future}

object FutureUtil {
  def traverseSequentially[S, T](collection: Seq[T])(fn: T => Future[S])(implicit ec: ExecutionContext): Future[Seq[S]] = {
    collection.foldLeft(Future.successful(Seq.empty[S])) { case (result, item) =>
      for {
        resultItems <- result
        newItem <- fn(item)
      } yield resultItems :+ newItem
    }
  }
}
