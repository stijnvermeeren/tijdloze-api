package util

object Mailer {
  import org.apache.commons.mail._

  def send(fromEmail: String, fromName: String, to: String, subject: String, message: String): Unit = {
    val email = new SimpleEmail()
    email.setHostName("localhost")

    email
      .addTo(to)
      .setFrom(fromEmail, fromName)
      .setSubject(subject)
      .setMsg(message)
      .send()
  }
}
