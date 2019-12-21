import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Source}
import scala.concurrent.ExecutionContext.Implicits.global

object Test extends App {
  val system = ActorSystem.create("test")
  implicit val mat = ActorMaterializer.create(system)

  // chat room many clients -> merge hub -> broadcasthub -> many clients
  private val (chatSink, chatSource) = {
    // Don't log MergeHub$ProducerFailed as error if the client disconnects.
    // recoverWithRetries -1 is essentially "recoverWith"
    val source = MergeHub.source[Int]
      .log("source")
      .recoverWithRetries(-1, { case _: Exception => Source.empty })

    val sink = BroadcastHub.sink[Int]
    source.toMat(sink)(Keep.both).run()
  }

  Source
    .repeat(1)
    .scan(0)(_ + _)
    .to(chatSink)
    .run()

  chatSource.take(4).runForeach(println) flatMap { _ =>
    println("hoho")
    chatSource.take(4).runForeach(println)
  }

}
