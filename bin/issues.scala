#!/usr/bin/env sbt -Dsbt.version=1.1.4 -Dsbt.main.class=sbt.ScriptMain -error

// This generates Markdown suitable for pasting into
// https://github.com/NetLogo/NetLogo/wiki/Release-notes

// if you run the script for the first time and it appears non-responsive,
// don't panic; it may take a few minutes to download dependencies

/***
scalaVersion := "2.12.6"
onLoadMessage := ""
scalacOptions ++= Seq(
  "-deprecation", "-unchecked", "-feature", "-Xfatal-warnings")
libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.0",
  "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.12.0",
  "org.slf4j" % "slf4j-nop" % "1.7.10")
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
