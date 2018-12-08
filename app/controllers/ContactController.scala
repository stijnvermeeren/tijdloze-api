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
          val footer = request.userId match {
            case Some(userId) => s" --- Message from verified user with id $userId."
            case None => s" --- Message from unverified user."
          }

          println(footer)

          Mailer.send(
            fromEmail = form.email getOrElse "anonymous@example.com",
            fromName = form.name,
            to = config.getString("tijdloze.contact.recipient"),
            subject = "De Tijdloze Website: contact",
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
