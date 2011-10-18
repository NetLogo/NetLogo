// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import collection.JavaConverters._
import org.scalatest.FunSuite
import org.nlogo.api.{FileIO, Version}

class AppletSaverTests extends FunSuite {
  val mockConverter = new ProceduresToHtmlInterface {def convert(code: String) = ""}

  def test(path: String, actual: String) {
    val f = FileIO.file2String("models/test/applet/" + path)
            .replaceAll("\r\n","\n")
            .replaceAll("@@@VERSION_HERE@@@", Version.version)
    assert(f === actual.replaceAll("\r\n","\n"))
  }

  test("Making Applet Of Logistic Model") {
    val info =
      """|WHAT IS IT?
         |-----------
         |This is a model of a 2-D cellular automaton.
         |
         |
         |## HOW IT WORKS
         |This a test of http://hyper.link.ing.
         |
         |
         |## HOW TO USE IT
         |This is a test of
         |
         |     preformatted
         |                 text
         |ooh yeah!
         |""".stripMargin
    val code =
      """|to charge  ;; test <weird characters>
         |  ask turtles [ fd 10 die ]
         |end""".stripMargin
    val buf = new StringBuilder
    new AppletSaver(mockConverter, buf).build(
      "logistic","logistic.nlogo",805,422,info,code,List[String]().asJava, List[String]().asJava)
    test("logistic.html",buf.toString)
  }

  test("Making Applet Of Matrix Extension") {
    val buf = new StringBuilder
    new AppletSaver(mockConverter, buf).build(
      "MatrixExample","MatrixExample.nlogo",670,480,"","",
      List("matrix/matrix.jar","matrix/Jama-1.0.2.jar").asJava,
      List[String]().asJava)
    test("MatrixExample.html",buf.toString)
  }


  test("successive runs of applet saver must produce same output") {
    val buf = new StringBuilder
    val appletSaver = new AppletSaver(mockConverter, buf)
    appletSaver.build(
      "MatrixExample","MatrixExample.nlogo",670,480,"","",
      List("matrix/matrix.jar","matrix/Jama-1.0.2.jar").asJava,
      List[String]().asJava)
    test("MatrixExample.html",buf.toString)
    appletSaver.build(
      "MatrixExample","MatrixExample.nlogo",670,480,"","",
      List("matrix/matrix.jar","matrix/Jama-1.0.2.jar").asJava,
      List[String]().asJava)
    test("MatrixExample.html",buf.toString)
  }

  test("Local Links and Images") {
    val infoText = """## Local Link

[bitmap](file:./bitmap.html)

## Local Image

![weathermap](file:./weathermap.jpg)"""
    
    val buf = new StringBuilder
    new AppletSaver(mockConverter, buf).build(
      "LocalLinksAndImages","LocalLinksAndImages.nlogo",675,480,infoText,"",
      List[String]().asJava,
      List[String]().asJava)
    test("LocalLinksAndImages.html",buf.toString)
  }

}
