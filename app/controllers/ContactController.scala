package controllers

import util.Mailer

import javax.inject._

import model.api.ContactForm
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws._
import play.api.mvc._

@Singleton
class ContactController @Inject() (ws: WSClient) extends InjectedController {
  def post() = {
    Action(parse.json) { request: Request[JsValue] =>
      Json.fromJson[ContactForm](request.body) match {
        case JsSuccess(form, _) =>
          Mailer.send(
            fromEmail = form.email getOrElse "anoniem@stijnshome.be",
            fromName = form.name,
            to = "stijn@stijnvermeeren.be",
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
