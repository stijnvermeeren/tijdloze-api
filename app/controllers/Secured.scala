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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class OptionallyAuthenticatedRequest[A](val userId: Option[String], request: Request[A]) extends WrappedRequest[A](request)

class OptionallyAuthenticate @Inject()(config: Config)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, OptionallyAuthenticatedRequest] {

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
            println(s"Invalid access token: ${e.getMessage}")
            None
        }
      case _ =>
        None
    }

    val userIdResult = jwtResult.flatMap(_.subject)
    val optionallyAuthenticatedRequest = new OptionallyAuthenticatedRequest(userIdResult, request)
    Future.successful(Right(optionallyAuthenticatedRequest))
  }
}

class AuthenticatedRequest[A](val userId: String, request: Request[A]) extends WrappedRequest[A](request)

class Authenticate @Inject() (optionallyAuthenticate: OptionallyAuthenticate)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, AuthenticatedRequest] {

  def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    optionallyAuthenticate.refine(request) map { result =>
      result flatMap { optionallyAuthenticatedRequest =>
        optionallyAuthenticatedRequest.userId match {
          case Some(userId) =>
            Right(new AuthenticatedRequest(userId, request))
          case None =>
            println("No valid access token.")
            Left(Unauthorized)
        }
      }
    }
  }
}


class UserRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

class AuthenticateAdmin @Inject() (authenticate: Authenticate, userDAO: UserDAO)(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[Request, UserRequest] {

  def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
    authenticate.refine(request) flatMap {
      case Right(authenticatedRequest) =>
        userDAO.get(authenticatedRequest.userId) map {
          case Some(user) if user.isAdmin =>
            Right(new UserRequest(user, request))
          case _ =>
            println("Insufficient access rights.")
            Left(Unauthorized)
        }
      case Left(error) =>
        Future.successful(Left(error))
    }
  }
}
