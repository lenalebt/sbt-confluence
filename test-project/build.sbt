name := "test-project"

organization := "de.lenabrueder"

enablePlugins(ConfluencePlugin)

confluenceSettings := ConfluenceSettings(user="admin", password="admin", base="http://localhost:8090")

import scala.io.Source
val parent = ConfluencePage(Source.fromFile("README.md"), space="TST")
confluencePages += parent
confluencePages += ConfluencePage(Source.fromString("# Testpage\n\nThis is just a simple test page"), space="TST", ancestor=Some(parent))
