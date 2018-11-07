package controllers

import java.io.FileInputStream

import com.typesafe.config.Config
import javax.inject.Inject
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.mvc.Results._
import play.api.mvc._
import java.security.cert.CertificateFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthenticatedRequest[A](val userId: Option[String], request: Request[A]) extends WrappedRequest[A](request)

class AuthenticatedAction @Inject()(parser: BodyParsers.Default, config: Config)(implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser)
  with ActionBuilder[AuthenticatedRequest, AnyContent]
  with ActionRefiner[Request, AuthenticatedRequest] {

  def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    request.headers.get("Authorization") match {
      case Some(header) =>
        val fileReader = new FileInputStream("/Users/vermeeren/Downloads/stijnvermeeren.pem")
        val fact = CertificateFactory.getInstance("X.509")
        val pubKey = fact.generateCertificate(fileReader).getPublicKey

        val decodedJwt = JwtJson.decode(
          token = header.drop("Bearer ".length),
          key = pubKey,
          algorithms = Seq(JwtAlgorithm.RS256)
        )
        decodedJwt match {
          case Success(jwt) =>
            println(jwt)
            Future.successful(Right(new AuthenticatedRequest(jwt.subject, request)))
          case Failure(e) =>
            println(s"Invalid access token: ${e.getMessage}")
            Future.successful(Left(Unauthorized))
        }
      case _ =>
        println(s"No Authorization header provided.")
        Future.successful(Left(Unauthorized))
    }
  }
}
