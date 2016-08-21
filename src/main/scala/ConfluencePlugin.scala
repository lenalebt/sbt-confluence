import sbt._
import sbt.Keys.{ streams }
import scala.language.implicitConversions

/**
 * This plugin will make it possible to publish your documentation as confluence pages,
 * similar to the gh-pages plugin.
 */
object ConfluencePlugin extends AutoPlugin {
  object autoImport {
    lazy val confluencePublish = taskKey[Unit]("Publishes your documentation as a confluence page")
    lazy val confluenceSettings = settingKey[ConfluenceSettings]("The settings that should be used for confluence")
    lazy val confluencePages = settingKey[Seq[ConfluencePage]]("The confluence pages you want to publish")
  }

  import autoImport._
  import play.api.libs.json._
  import org.asynchttpclient._

  def armourMarkdown(markdown:String): String = (<ac:macro ac:name='markdown'><ac:plain-text-body>{scala.xml.PCData(markdown)}</ac:plain-text-body></ac:macro>).toString

  implicit def confluencePage2confluencePageWithSettings(confluencePage:ConfluencePage)(implicit settings:ConfluenceSettings) =
    ConfluencePageWithSettings(confluencePage, settings)
  implicit def confluencePageWithSettings2confluencePage(page:ConfluencePageWithSettings) =
    page.underlying

  def createCommand(page: ConfluencePage)(implicit settings: ConfluenceSettings): JsObject = {
    Json.obj(
      "type" -> "page",
      "title" -> page.title,
      "space" -> Json.obj("key" -> page.space),
      "body" -> Json.obj(
        "storage" -> Json.obj(
          "value" -> armourMarkdown(page.content),
          "representation" -> "storage"
        )
      )
    ) deepMerge (page.ancestor.flatMap(_.id) match {
      case None       => Json.obj()
      case Some(text) => Json.obj("ancestors" -> JsArray(Seq(Json.obj("id" -> text))))
    })
  }

  def doCreate(page: ConfluencePage)(implicit settings:ConfluenceSettings): Boolean = {
    val url = s"${settings.base}/rest/api/content/"

    val client = new DefaultAsyncHttpClient()
    val command = createCommand(page).toString
    val response = client.preparePost(url)
      .setRealm(settings.toRealm)
      .addHeader("Content-Type", "application/json")
      .setBody(command)
      .execute().get()  //do block. It's only in the build.

    response.getStatusCode match {
      case 200 => true
      case _   => false
    }
  }

  def updateCommand(page: ConfluencePageWithSettings)(implicit settings: ConfluenceSettings): JsObject = {
    createCommand(page) deepMerge Json.obj(
      "id" -> page.id.get,
      "version" -> Json.obj("number" -> (page.version.get + 1))
    )
  }

  def doUpdate(page: ConfluencePage)(implicit settings:ConfluenceSettings): Boolean = {
    val updateUrl = s"${settings.base}/rest/api/content/${page.id.get}"

    val command = updateCommand(page).toString
    val client = new DefaultAsyncHttpClient()
    val updateResponse = client.preparePut(updateUrl)
      .setRealm(settings.toRealm)
      .addHeader("Content-Type", "application/json")
      .setBody(command)
      .execute().get()  //do block. It's only in the build.

      updateResponse.getStatusCode match {
        case 200 => true
        case _   => false
      }
  }

  def doPublish(pages: Seq[ConfluencePage])(implicit settings:ConfluenceSettings) = {
    for {page <- pages} yield {
      doCreate(page) || doUpdate(page)
    }
  }

  override lazy val projectSettings = Seq(
    confluencePublish := {
      val response = doPublish(confluencePages.value)(confluenceSettings.value)
      streams.value.log.info(s"response was: $response")
    },
    //confluenceSettings := ConfluenceSettings("admin", "admin", "http://localhost:8090"),
    confluencePages := Seq.empty
  )
}
