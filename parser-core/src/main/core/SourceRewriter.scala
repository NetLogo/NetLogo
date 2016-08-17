// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait SourceRewriter {
  def addGlobal(global: String): String
  def addExtension(extension: String): String
  def remove(commandName: String): String
  def replaceToken(originalToken: String, replaceToken:String): String
  def addCommand(sourceAndNewCommand: (String, String)): String
  def replaceCommand(sourceAndDestCommand: (String, String)): String
  def replaceReporter(sourceAndDestReporter: (String, String)): String
}
