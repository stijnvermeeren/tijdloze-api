package controllers

import com.typesafe.config.Config
import util.Mailer
import javax.inject._
import model.api.ContactForm
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws._
import play.api.mvc._

@Singleton
class ContactController @Inject() (ws: WSClient, config: Config) extends InjectedController {
  def post() = {
    Action(parse.json) { request: Request[JsValue] =>
      Json.fromJson[ContactForm](request.body) match {
        case JsSuccess(form, _) =>
          Mailer.send(
            fromEmail = form.email getOrElse "anonymous@example.com",
            fromName = form.name,
            to = config.getString("tijdloze.contact.recipient"),
            subject = "De Tijdloze Website: contact",
            message = form.message
          )

          Ok
        case JsError(errors) =>
          println(errors)
          BadRequest
      }
    }
  }
}
