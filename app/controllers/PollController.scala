package controllers

import javax.inject._
import model.{PollAnswerId, PollId}
import model.api._
import model.db.dao.PollDAO
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import util.currentlist.CurrentListUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PollController @Inject()(
  authenticate: Authenticate,
  authenticateAdmin: AuthenticateAdmin,
  pollDAO: PollDAO,
  currentList: CurrentListUtil
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
                val apiPoll = Poll.fromDb(dbPoll, dbAnswers)
                currentList.updateLatestPoll()
                Ok(Json.toJson(apiPoll))
              case None =>
                InternalServerError("Error while saving poll to the database.")
            }
          }
        }
      )
    }
  }

  def updatePoll(pollId: PollId) = {
    (Action andThen authenticateAdmin).async(parse.json) { request =>
      val data = request.body.validate[PollUpdate]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        pollUpdate => {
          pollDAO.updatePoll(pollId, pollUpdate) flatMap { _ =>
            pollDAO.getPoll(pollId) map {
              case Some((dbPoll, dbAnswers)) =>
                currentList.updateLatestPoll()
                Ok(Json.toJson(Poll.fromDb(dbPoll, dbAnswers)))
              case None =>
                InternalServerError("Error while saving poll to the database.")
            }
          }
        }
      )
    }
  }

  def updatePollAnswer(pollId: PollId, pollAnswerId: PollAnswerId) = {
    (Action andThen authenticateAdmin).async(parse.json) { request =>
      val data = request.body.validate[PollAnswerUpdate]
      data.fold(
        errors => {
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        pollAnswerUpdate => {
          pollDAO.updatePollAnswer(pollAnswerId, pollAnswerUpdate) flatMap { _ =>
            pollDAO.getPoll(pollId) map {
              case Some((dbPoll, dbAnswers)) =>
                currentList.updateLatestPoll()
                Ok(Json.toJson(Poll.fromDb(dbPoll, dbAnswers)))
              case None =>
                InternalServerError("Error while saving poll to the database.")
            }
          }
        }
      )
    }
  }

  def hidePoll(pollId: PollId) = {
    (Action andThen authenticateAdmin).async { request =>
      pollDAO.setDeleted(pollId, isDeleted = true) map { _ =>
        currentList.updateLatestPoll()
        Ok("")
      }
    }
  }

  def showPoll(pollId: PollId) = {
    (Action andThen authenticateAdmin).async { request =>
      pollDAO.setDeleted(pollId, isDeleted = false) map { _ =>
        currentList.updateLatestPoll()
        Ok("")
      }
    }
  }

  def vote(pollId: PollId, pollAnswerId: PollAnswerId) = {
    (Action andThen authenticate).async { request =>
      pollDAO.vote(request.user.id, pollId, pollAnswerId) map { _ =>
        Ok("")
      }
    }
  }

  def myVotes() = {
    (Action andThen authenticate).async { request =>
      pollDAO.myVotes(request.user.id) map { votes =>
        Ok(Json.toJson(PollVoteList(votes map PollVote.fromDb)))
      }
    }
  }

  def get(pollId: PollId) = {
    Action.async { request =>
      pollDAO.getPoll(pollId) map {
        case Some((dbPoll, dbAnswers)) =>
          Ok(Json.toJson(Poll.fromDb(dbPoll, dbAnswers)))
        case None =>
          InternalServerError(s"Poll with id ${pollId} not found.")
      }
    }
  }

  def getLatest() = {
    Action.async { request =>
      pollDAO.getLatest() map {
        case Some((dbPoll, dbAnswers)) =>
          Ok(Json.toJson(Poll.fromDb(dbPoll, dbAnswers)))
        case None =>
          InternalServerError(s"No polls found.")
      }
    }
  }

  def list() = {
    Action.async { request =>
      pollDAO.list() map { polls =>
        val data = polls map {
          case (dbPoll, dbAnswers) =>
            Poll.fromDb(dbPoll, dbAnswers)
        }
        Ok(Json.toJson(data))
      }
    }
  }
}
