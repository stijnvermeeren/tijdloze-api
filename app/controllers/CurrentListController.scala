package controllers

import util.CurrentListUtil
import akka.stream.Materializer
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

@Singleton
class CurrentListController @Inject()(
  currentList: CurrentListUtil
)(implicit mat: Materializer) extends InjectedController {

  // TODO secure with same origin check
  // TODO authenticate
  def ws(): WebSocket = {
    WebSocket.accept[JsValue, JsValue] { requestHeader =>
      currentList.currentListFlow
    }
  }
}
