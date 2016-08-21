import org.asynchttpclient._

case class ConfluenceSettings(user: String, password: String, base: String) {
  def toRealm = new Realm.Builder(user, password)
    .setUsePreemptiveAuth(true).setScheme(Realm.AuthScheme.BASIC)
    .build()
}
