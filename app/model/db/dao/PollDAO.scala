package model.db.dao

import javax.inject.{Inject, Singleton}
import model.{PollAnswerId, PollId}
import model.api.{PollAnswerUpdate, PollCreate, PollUpdate}
import model.db.{Poll, PollAnswer, PollVote}
import model.db.dao.table.{PollAnswerTable, PollTable, PollVoteTable}
import play.api.cache.AsyncCacheApi
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PollDAO @Inject()(configProvider: DatabaseConfigProvider, cache: AsyncCacheApi) {
  val dbConfig = configProvider.get[JdbcProfile]
  private val db = dbConfig.db
  import dbConfig.profile.api._

  val pollTable = TableQuery[PollTable]
  val pollAnswerTable = TableQuery[PollAnswerTable]
  val pollVoteTable = TableQuery[PollVoteTable]

  def getPoll(pollId: PollId): Future[Option[(Poll, Seq[PollAnswer])]] = {
    cache.getOrElseUpdate(s"poll-${pollId.value}") {
      for {
        pollOption <- db run {
          pollTable.filter(_.id === pollId).result.headOption
        }
        answers <- db run {
          pollAnswerTable.filter(_.pollId === pollId).result
        }
      } yield {
        pollOption map { poll =>
          (poll, answers)
        }
      }
    }

  }

  def getLatest(): Future[Option[(Poll, Seq[PollAnswer])]] = {
    cache.getOrElseUpdate(s"poll-latest") {
      db run {
        pollTable.sortBy(_.id.desc).filterNot(_.isDeleted).result.headOption
      } flatMap {
        case Some(poll) =>
          db run {
            pollAnswerTable.filter(_.pollId === poll.id).result
          } map { answers =>
            Some(poll, answers)
          }
        case None =>
          Future.successful(None)
      }
    }
  }

  def list(): Future[Seq[(Poll, Seq[PollAnswer])]] = {
    for {
      polls <- db run {
        pollTable.sortBy(_.id.desc).result
      }
      answers <- db run {
        pollAnswerTable.sortBy(_.id).result
      }
    } yield {
      polls map { poll =>
        (poll, answers.filter(_.pollId == poll.id))
      }
    }
  }

  def createPoll(data: PollCreate): Future[PollId] = {
    val newPoll = Poll(
      question = data.question,
      year = data.year
    )

    db run {
      (pollTable returning pollTable.map(_.id)) += newPoll
    } flatMap { pollId =>
      val answers = data.answers map { answer =>
        PollAnswer(
          pollId = pollId,
          answer = answer
        )
      }

      db run {
        pollAnswerTable ++= answers
      } flatMap { _ =>
        cache.remove("poll-latest")
      } map (_ => pollId)
    }
  }

  def vote(userId: String, pollId: PollId, answerId: PollAnswerId): Future[Unit] = {
    val update = db run {
      pollVoteTable
        .filter(_.pollId === pollId)
        .filter(_.userId === userId)
        .map(_.answerId)
        .update(answerId)
    }

    val insert = update flatMap { updatedRows =>
      if (updatedRows < 1) {
        db run {
          pollVoteTable += PollVote(
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
        pollVoteTable.filter(_.answerId === answerId).length.result
      }
    }

    count flatMap { voteCount =>
      db run {
        pollAnswerTable
          .filter(_.id === answerId)
          .map(_.voteCount)
          .update(voteCount)
      }
    } flatMap { _ =>
      cleanCache(pollId)
    }
  }

  private def cleanCache(pollId: PollId): Future[Unit] = {
    getLatest() map { latest =>
      if (latest.exists(_._1.id == pollId)) {
        cache.remove(s"poll-latest")
      }
      cache.remove(s"poll-${pollId.value}")
    }
  }

  def myVotes(userId: String): Future[Seq[PollVote]] = {
    db run {
      pollVoteTable.filter(_.userId === userId).result
    }
  }

  def updatePoll(pollId: PollId, pollUpdate: PollUpdate): Future[Unit] = {
    db run {
      pollTable
        .filter(_.id === pollId)
        .map(_.question)
        .update(pollUpdate.question)
    } flatMap { _ =>
      cleanCache(pollId)
    }
  }

  def updatePollAnswer(pollId: PollId, pollAnswerId: PollAnswerId, pollAnswerUpdate: PollAnswerUpdate): Future[Unit] = {
    db run {
      pollAnswerTable
        .filter(_.id === pollAnswerId)
        .map(_.answer)
        .update(pollAnswerUpdate.answer)
    } flatMap { _ =>
      cleanCache(pollId)
    }
  }

  def setDeleted(pollId: PollId, isDeleted: Boolean): Future[Unit] = {
    db run {
      pollTable
        .filter(_.id === pollId)
        .map(_.isDeleted)
        .update(isDeleted)
    } map (_ => ()) flatMap { _ =>
      cleanCache(pollId)
    }
  }
}
