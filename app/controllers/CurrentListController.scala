package controllers

import util.currentlist.CurrentListUtil
import akka.stream.Materializer

import javax.inject._
import play.api.libs.json._
import play.api.mvc._

@Singleton
class CurrentListController @Inject()(
  currentList: CurrentListUtil
)(implicit mat: Materializer) extends InjectedController {

  def ws(): WebSocket = {
    WebSocket.accept[JsValue, JsValue] { requestHeader =>
      currentList.currentListFlow()
    }
  }
}
