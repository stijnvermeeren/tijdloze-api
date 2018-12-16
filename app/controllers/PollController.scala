package controllers

import javax.inject._
import model.api.{Poll, PollCreate}
import model.db.dao.PollDAO
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PollController @Inject()(
  authenticate: Authenticate,
  authenticateAdmin: AuthenticateAdmin,
  pollDAO: PollDAO
) extends InjectedController {

  def createPoll() = {
    (Action andThen authenticateAdmin).async(parse.json) { request =>
      val data = request.body.validate[PollCreate]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        pollCreate => {
          pollDAO.createPoll(pollCreate) flatMap { pollId =>
            pollDAO.getPoll(pollId) map {
              case Some((dbPoll, dbAnswers)) =>
                Ok(Json.toJson(Poll.fromDb(dbPoll, dbAnswers)))
              case None =>
                InternalServerError("Error while saving poll to the database.")
            }
          }
        }
      )
    }
  }
}
