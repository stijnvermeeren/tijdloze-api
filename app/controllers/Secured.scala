package controllers

import java.io.FileInputStream

import com.typesafe.config.Config
import javax.inject.Inject
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.mvc.Results._
import play.api.mvc._
import java.security.cert.CertificateFactory

import model.db.User
import model.db.dao.UserDAO
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class OptionallyAuthenticatedRequest[A](val user: Option[User], val userId: Option[String], request: Request[A]) extends WrappedRequest[A](request)

class OptionallyAuthenticate @Inject()(config: Config, userDAO: UserDAO)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, OptionallyAuthenticatedRequest] {
  val logger = Logger(getClass)

  def refine[A](request: Request[A]): Future[Either[Result, OptionallyAuthenticatedRequest[A]]] = {
    val jwtResult = request.headers.get("Authorization") match {
      case Some(header) =>
        val fileReader = new FileInputStream(config.getString("tijdloze.auth0.publickey.path"))
        val fact = CertificateFactory.getInstance("X.509")
        val pubKey = fact.generateCertificate(fileReader).getPublicKey

        val decodedJwt = JwtJson.decode(
          token = header.drop("Bearer ".length),
          key = pubKey,
          algorithms = Seq(JwtAlgorithm.RS256)
        )
        decodedJwt match {
          case Success(jwt) =>
            Some(jwt)
          case Failure(e) =>
            logger.warn(s"Invalid access token: ${e.getMessage}")
            None
        }
      case _ =>
        None
    }

    val userResultFuture = jwtResult.flatMap(_.subject) match {
      case Some(userId) =>
        userDAO.get(userId) map (user => (user, Some(userId)))
      case None =>
        Future.successful((None, None))
    }
    userResultFuture map { case (user, userId) =>
      Right(new OptionallyAuthenticatedRequest(user, userId, request))
    }
  }
}

class AuthenticatedRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

class Authenticate @Inject() (
  optionallyAuthenticate: OptionallyAuthenticate
)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, AuthenticatedRequest] {

  val logger = Logger(getClass)

  def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    optionallyAuthenticate.refine(request) map { result =>
      result flatMap { optionallyAuthenticatedRequest =>
        optionallyAuthenticatedRequest.user match {
          case Some(user) if user.isBlocked =>
            logger.warn(s"Attempted request to ${request.path} by blocked user with id ${user.id}.")
            Left(Unauthorized)
          case Some(user) if !user.isBlocked =>
            Right(new AuthenticatedRequest(user, request))
          case None =>
            logger.warn(s"Request to ${request.path} lacked authorization.")
            Left(Unauthorized)
        }
      }
    }
  }
}

class AuthenticateAdmin @Inject() (authenticate: Authenticate)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, AuthenticatedRequest] {

  val logger = Logger(getClass)

  def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    authenticate.refine(request) map {
      case Right(authenticatedRequest) =>
        if (authenticatedRequest.user.isAdmin) {
          Right(authenticatedRequest)
        } else {
          logger.warn(s"Attempted admin request to ${request.path} by unprivileged user with id ${authenticatedRequest.user.id}.")
          Left(Unauthorized)
        }
      case Left(error) =>
        Left(error)
    }
  }
}
