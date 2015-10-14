// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import org.scalatest.FunSuite
import org.apache.log4j.xml.DOMConfigurator
import org.nlogo.api.CompilerException

class LoggerTests extends FunSuite {
  org.apache.log4j.helpers.LogLog.setQuietMode(true)
  val logger = new Logger("esther")
  DOMConfigurator.configure("dist/netlogo_logging.xml")
  new java.io.File("tmp").mkdir()
  new java.io.File("tmp/LoggerTests").mkdir()
  logger.changeLogDirectory(new java.io.File("tmp/LoggerTests").getCanonicalPath)
  test("logger") {
    logger.modelOpened("models/Sample Models/Biology/Ants.nlogo")
    Logger.logSpeedSlider(20)
    logger.close()
    assertResult(1)(logger.filenames.size)
    val file = logger.filenames.get(0)
    val log = org.nlogo.api.FileIO.file2String(file).replaceAll("\r\n", "\n" )
    val timestamp = log.substring(log.indexOf("timestamp=\"") + 11, log.indexOf("\" level"))
    val expected =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE eventSet PUBLIC \"netlogo_logging.dtd\" \"" +
      file + "\">\n<eventSet username=\""+ System.getProperty( "user.name" ) +
      "\" name=\"esther\" ipaddress=\"" + logger.getIPAddress() +
      "\" modelName=\"models/Sample Models/Biology/Ants.nlogo\" version=\"" +
      org.nlogo.api.Version.version + "\">\n  <event logger=\"org.nlogo.log.Logger.SPEED\" " +
      "timestamp=\""+ timestamp + "\" level=\"INFO\" type=\"speed\">\n    " +
      "<value>20.0</value>\n  </event>\n</eventSet>\n"
    assertResult(expected)(log)
  }
  test("tick counter") {
    logger.modelOpened("models/Sample Models/Biology/Ants.nlogo")
    logger.tickCounterChanged(50)
    logger.close()
    assertResult(1)(logger.filenames.size)
    val file = logger.filenames.get(0)
    val log = org.nlogo.api.FileIO.file2String(file).replaceAll("\r\n", "\n")
    val timestamp = log.substring(log.indexOf("timestamp=\"" ) + 11, log.indexOf( "\" level"))
    val expected =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE eventSet PUBLIC \"netlogo_logging.dtd\" \"" +
      file + "\">\n<eventSet username=\""+ System.getProperty( "user.name" ) +
      "\" name=\"esther\" ipaddress=\"" + logger.getIPAddress() +
      "\" modelName=\"models/Sample Models/Biology/Ants.nlogo\" version=\"" +
      org.nlogo.api.Version.version + "\">\n  <event logger=\"org.nlogo.log.Logger.GLOBALS\" " +
      "timestamp=\""+ timestamp + "\" level=\"INFO\" type=\"ticks\">\n    " +
      "<name>ticks</name>\n    <value>50.0</value>\n  </event>\n</eventSet>\n"
    assertResult(expected)(log)
  }
  test("code tab error") {
    logger.modelOpened("models/Sample Models/Biology/Ants.nlogo")
    logger.codeTabCompiled("foo", new CompilerException("error!!!", 99, 999, "blargh"))
    logger.close()
    assertResult(1)(logger.filenames.size)
    val file = logger.filenames.get(0)
    val log = org.nlogo.api.FileIO.file2String(file).replaceAll("\r\n", "\n")
    val timestamp = log.substring(log.indexOf("timestamp=\"" ) + 11, log.indexOf( "\" level"))
    val expected =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE eventSet PUBLIC \"netlogo_logging.dtd\" \"" +
      file + "\">\n<eventSet username=\""+ System.getProperty( "user.name" ) +
      "\" name=\"esther\" ipaddress=\"" + logger.getIPAddress() +
      "\" modelName=\"models/Sample Models/Biology/Ants.nlogo\" version=\"" +
      org.nlogo.api.Version.version + "\">\n  <event logger=\"org.nlogo.log.Logger.CODE\" " +
      "timestamp=\""+ timestamp + "\" level=\"INFO\" type=\"compiled\">\n    " +
      "<code>foo</code>\n    <errorMessage startPos=\"99\" endPos=\"999\">error!!!</errorMessage>\n  </event>\n</eventSet>\n"
    assertResult(expected)(log)
  }
  test("code tab no error") {
    logger.modelOpened("models/Sample Models/Biology/Ants.nlogo")
    logger.codeTabCompiled("bar", null)
    logger.close()
    assertResult(1)(logger.filenames.size)
    val file = logger.filenames.get(0)
    val log = org.nlogo.api.FileIO.file2String(file).replaceAll("\r\n", "\n")
    val timestamp = log.substring(log.indexOf("timestamp=\"" ) + 11, log.indexOf( "\" level"))
    val expected =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE eventSet PUBLIC \"netlogo_logging.dtd\" \"" +
      file + "\">\n<eventSet username=\""+ System.getProperty( "user.name" ) +
      "\" name=\"esther\" ipaddress=\"" + logger.getIPAddress() +
      "\" modelName=\"models/Sample Models/Biology/Ants.nlogo\" version=\"" +
      org.nlogo.api.Version.version + "\">\n  <event logger=\"org.nlogo.log.Logger.CODE\" " +
      "timestamp=\""+ timestamp + "\" level=\"INFO\" type=\"compiled\">\n    " +
      "<code>bar</code>\n    <errorMessage startPos=\"0\" endPos=\"0\">success</errorMessage>\n  </event>\n</eventSet>\n"
    assertResult(expected)(log)
  }
}
