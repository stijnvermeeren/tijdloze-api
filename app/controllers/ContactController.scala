package controllers

import com.typesafe.config.Config
import util.Mailer
import javax.inject._
import model.api.ContactForm
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws._
import play.api.mvc._

@Singleton
class ContactController @Inject() (optionallyAuthenticate: OptionallyAuthenticate, ws: WSClient, config: Config) extends InjectedController {
  def post() = {
    (Action andThen optionallyAuthenticate)(parse.json) { request =>
      Json.fromJson[ContactForm](request.body) match {
        case JsSuccess(form, _) =>
          val footer1 = request.user.map(_.id) match {
            case Some(userId) => s" --- Message from verified user with id $userId."
            case None => " --- Message from unverified user."
          }
          val footer2 = form.email match {
            case Some(email) => s" --- Reply-to email address: $email"
            case None => " --- No reply-to email address provided."
          }
          val footer = Seq(footer1, footer2).mkString("\n")

          val recipients: Seq[String] = config.getString("tijdloze.contact.recipients")
            .split(';')
            .map(_.trim)
            .filter(_.nonEmpty)

          Mailer.send(
            fromEmail = form.email,
            fromName = form.name,
            to = recipients,
            subject = "tijdloze.rocks: contact",
            message = s"${form.message}\n\n$footer"
          )

          Ok
        case JsError(errors) =>
          println(errors)
          BadRequest
      }
    }
  }
}
