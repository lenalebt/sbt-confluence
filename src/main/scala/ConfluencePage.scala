case class ConfluencePage(source: scala.io.Source, space:String, ancestor:Option[ConfluencePage]=None) {
  lazy val content:String = source.getLines.mkString("\n")
  /**Title will be the first line, without leading '#' or spaces*/
  lazy val title: String = content.dropWhile(List('#', ' ').contains).takeWhile(_ != '\n')
}

case class ConfluencePageWithSettings(underlying:ConfluencePage, settings:ConfluenceSettings) {
  def content = underlying.content
  def title = underlying.title
  def space = underlying.space
  def ancestor = underlying.ancestor
  def source = underlying.source

  lazy val id: Option[String] = result flatMap {value => (value \ "id").asOpt[String]}
  lazy val version: Option[Int] = result flatMap {value => (value \ "version" \ "number").asOpt[Int]}

  import play.api.libs.json._
  private lazy val result: Option[JsValue] = {
    scala.util.Try {
      import org.asynchttpclient._
      val client = new DefaultAsyncHttpClient()
      val getPageIdResponse = client.prepareGet(s"${settings.base}/rest/api/content/")
        .setRealm(settings.toRealm)
        .addHeader("Content-Type", "application/json")
        .addQueryParam("title", title)
        .addQueryParam("space", space)
        .addQueryParam("expand", "version")
        .execute().get()  //do block. It's only in the build.

      val array = (Json.parse(getPageIdResponse.getResponseBody) \ "results").as[Seq[JsValue]]
      array(0)
    }.toOption
  }
}
