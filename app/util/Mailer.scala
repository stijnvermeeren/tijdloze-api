package util

import javax.mail.internet.InternetAddress
import scala.jdk.CollectionConverters._

object Mailer {
  import org.apache.commons.mail._

  def send(fromEmail: Option[String], fromName: String, to: Seq[String], subject: String, message: String): Unit = {
    val email = new SimpleEmail()
    email.setHostName("localhost")

    fromEmail match {
      case Some(value) => email.setReplyTo(List(new InternetAddress(value, fromName)).asJava)
      case None =>
    }

    email
      .addTo(to:_*)
      .setFrom("stijn@stijnvermeeren.be", s"$fromName (tijdloze.rocks)")
      .setSubject(subject)
      .setMsg(message)
      .send()
  }
}
