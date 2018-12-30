package filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class CacheControlFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  /**
    * Disable caching by header. There should be a better solution for this...
    */
  def apply(nextFilter: RequestHeader => Future[Result])
    (requestHeader: RequestHeader): Future[Result] = {

    val newRequestHeader = requestHeader.withHeaders(
      requestHeader.headers
        .add("Cache-Control" -> "no-cache")
        .remove("If-None-Match")
    )

    nextFilter(newRequestHeader).map { result =>
      result.withHeaders("Cache-Control" -> "max-age=0")
    }
  }
}
