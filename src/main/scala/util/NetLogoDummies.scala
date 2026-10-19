package org.nlogo.extensions.vid.util

import java.awt.image.BufferedImage
import java.io.File
import java.lang.{ Boolean => JBoolean, Double => JDouble }
import java.util.{ List => JList }

import org.nlogo.api.{ Activation, Agent, AgentSet, AnonymousCommand, AnonymousReporter, Argument, Context
                     , ExtensionException, Link, MersenneTwisterFast, Patch, Turtle, Workspace, World }
import org.nlogo.core.{ LogoList, Token }

class CurrentDirContext extends Context {
  def attachCurrentDirectory(path: String): String =
    new File(path).getCanonicalPath
  def attachModelDir(filePath: String): String = ???
  def getAgent: Agent = ???
  def getDrawing: BufferedImage = ???
  def getRNG: MersenneTwisterFast = ???
  def importPcolors(image: BufferedImage,asNetLogoColors: Boolean): Unit = ???
  def logCustomGlobals(nameValuePairs: Seq[(String, String)]): Unit = ???
  def logCustomMessage(msg: String): Unit = ???
  def activation: Activation = ???
  def workspace: Workspace = ???
  def world: World = ???
}

class FakeArgument(val underlying: AnyRef) extends Argument {
  def get: AnyRef = ???
  def getAgent: Agent = ???
  def getAgentSet: AgentSet = ???
  def getBoolean: JBoolean = ???
  def getBooleanValue: Boolean = ???
  def getCode: JList[Token] = ???
  def getCommand: AnonymousCommand = ???
  def getDouble: JDouble = ???
  def getDoubleValue: Double = underlying match {
    case d: JDouble => d.doubleValue
    case _ => throw new ExtensionException(s"expected a double, got $underlying")
  }
  def getIntValue: Int = getDoubleValue.toInt
  def getLink: Link = ???
  def getList: LogoList = ???
  def getPatch: Patch = ???
  def getReporter: AnonymousReporter = ???
  def getString: String =
    underlying match {
      case s: String => s
      case _ => throw new ExtensionException(s"expected a string, but got $underlying")
    }
  def getCodeBlock: List[org.nlogo.core.Token] = ???
  def getSymbol: Token = ???
  def getTurtle: Turtle = ???
}
