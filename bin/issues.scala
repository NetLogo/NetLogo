#!/usr/bin/env sbt -Dsbt.version=0.13.12 -Dsbt.main.class=sbt.ScriptMain --error

// This generates Markdown suitable for pasting into
// https://github.com/NetLogo/NetLogo/wiki/Release-notes

// running this is tricky because the sbt script mode stuff isn't currently maintained in sync with
// conscript so you have fiddle with it or it doesn't work. instructions:
// - install conscript if you don't have it already:
//   curl https://raw.github.com/n8han/conscript/master/setup.sh | sh
// - install sbt (and the scalas script) through conscript:
//   cs sbt/sbt --branch 0.12.0
// - edit ~/.conscript/sbt/sbt/scalas/launchconfig
//   and change the Scala version from `auto` to `2.9.2`
//   and change the cross-versioned settings from `true` to `false`
// - ensure that ~/bin is included in your `PATH` environment variable
//   (this is where Conscript places the scripts that it manages)
// - note: if you run the script for the first time and it appears non-responsive, do not fear;
//         wait at least a few minutes for it to try downloading the dependencies before panicking!

/***
scalaVersion := "2.11.8"

onLoadMessage := ""

scalacOptions ++= Seq(
  "-deprecation", "-unchecked", "-feature", "-Xfatal-warnings")

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.0",
  "org.slf4j" % "slf4j-nop" % "1.7.6")
*/

import dispatch._, Defaults._
import org.json4s.JsonAST._
import org.json4s.native.JsonParser

object Issue {
  def fromJson(j: JValue): Issue = {
    val JInt(n) = j \ "number"
    val JString(title) = j \ "title"
    val JArray(labels) = j \ "labels"
    Issue(n.toInt, title,
          labels.map(_ \ "name").collect{case JString(s) => s})
  }
}
case class Issue(number: Int, title: String, labels: List[String])

val host = :/("api.github.com").secure
val base = host / "repos" / "NetLogo" / "NetLogo" / "issues"

def getIssues(state: String): List[Issue] = {
  val req = base <<? Map("milestone" -> "19",
                         "state" -> state,
                         "per_page" -> "1000")
  // println(req.build.getRawUrl)  // useful for debugging
  val response = Http(req OK as.json4s.Json)
  import concurrent.Await, concurrent.duration._
  val JArray(array) = Await.result(response, 30.seconds)
  for (item <- array)
  yield Issue.fromJson(item)
}

def report(state: String) {
  val issues = getIssues(state)
  println(issues.size + " issues with state = " + state)
  for(Issue(n, title, labels) <- issues.sortBy(_.number).sortBy(_.labels.mkString)) {
    val labelsString =
      if (labels.isEmpty) ""
      else labels.mkString("", ", ", ": ")
    println(" * " + labelsString + title + " ([#" + n + "]" +
            "(https://github.com/NetLogo/NetLogo/issues/" + n + "))")
  }
}

report("closed")
println()
report("open")

Http.shutdown()
