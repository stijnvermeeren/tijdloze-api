package model.db.dao

import javax.inject.{Inject, Singleton}
import model.{PollAnswerId, PollId}
import model.api.{PollAnswerUpdate, PollCreate, PollUpdate}
import model.db.{Poll, PollAnswer, PollVote}
import model.db.dao.table.AllTables

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PollDAO @Inject()(allTables: AllTables) {
  import allTables._
  private val db = dbConfig.db
  import dbConfig.profile.api._

  def getPoll(pollId: PollId): Future[Option[(Poll, Seq[PollAnswer])]] = {
    for {
      pollOption <- db run {
        PollTable.filter(_.id === pollId).result.headOption
      }
      answers <- db run {
        PollAnswerTable.filter(_.pollId === pollId).result
      }
    } yield {
      pollOption map { poll =>
        (poll, answers)
      }
    }
  }

  def getLatest(): Future[Option[(Poll, Seq[PollAnswer])]] = {
    db run {
      PollTable.sortBy(_.id.desc).result.headOption
    } flatMap {
      case Some(poll) =>
        db run {
          PollAnswerTable.filter(_.pollId === poll.id).result
        } map { answers =>
          Some(poll, answers)
        }
      case None =>
        Future.successful(None)
    }
  }

  def list(): Future[Seq[(Poll, Seq[PollAnswer])]] = {
    for {
      polls <- db run {
        PollTable.sortBy(_.id.desc).result
      }
      answers <- db run {
        PollAnswerTable.sortBy(_.id).result
      }
    } yield {
      polls map { poll =>
        (poll, answers.filter(_.pollId == poll.id))
      }
    }
  }

  def createPoll(data: PollCreate): Future[PollId] = {
    val newPoll = Poll(
      question = data.question
    )

    db run {
      (PollTable returning PollTable.map(_.id)) += newPoll
    } flatMap { pollId =>
      val answers = data.answers map { answer =>
        PollAnswer(
          pollId = pollId,
          answer = answer
        )
      }

      db run {
        PollAnswerTable ++= answers
      } map (_ => pollId)
    }
  }

  def vote(userId: String, pollId: PollId, answerId: PollAnswerId): Future[Unit] = {
    val update = db run {
      PollVoteTable
        .filter(_.pollId === pollId)
        .filter(_.userId === userId)
        .map(_.answerId)
        .update(answerId)
    }

    val insert = update flatMap { updatedRows =>
      if (updatedRows < 1) {
        db run {
          PollVoteTable += PollVote(
            userId = Some(userId),
            pollId = pollId,
            answerId = answerId
          )
        } map (_ => ())
      } else {
        Future.successful(())
      }
    }

    val count = insert flatMap { _ =>
      db run {
        PollVoteTable.filter(_.answerId === answerId).length.result
      }
    }

    count flatMap { voteCount =>
      db run {
        PollAnswerTable
          .filter(_.id === answerId)
          .map(_.voteCount)
          .update(voteCount)
      }
    } map (_ => ())
  }

  def myVotes(userId: String): Future[Seq[PollVote]] = {
    db run {
      PollVoteTable.filter(_.userId === userId).result
    }
  }

  def updatePoll(pollId: PollId, pollUpdate: PollUpdate): Future[Unit] = {
    db run {
      PollTable
        .filter(_.id === pollId)
        .map(_.question)
        .update(pollUpdate.question)
    } map (_ => ())
  }

  def updatePollAnswer(pollAnswerId: PollAnswerId, pollAnswerUpdate: PollAnswerUpdate): Future[Unit] = {
    db run {
      PollAnswerTable
        .filter(_.id === pollAnswerId)
        .map(_.answer)
        .update(pollAnswerUpdate.answer)
    } map (_ => ())
  }

  def setDeleted(pollId: PollId, isDeleted: Boolean): Future[Unit] = {
    db run {
      PollTable
        .filter(_.id === pollId)
        .map(_.isDeleted)
        .update(isDeleted)
    } map (_ => ())
  }
}
