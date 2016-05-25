// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait FrontEndProcedure {
  var agentClassString = "OTPL"
  def procedureDeclaration: StructureDeclarations.Procedure
  def name: String
  def isReporter: Boolean
  def displayName: String
  def filename: String
  def nameToken: Token
  def argTokens: Seq[Token]
  var args = Vector[String]()
  var topLevel = false
  def dump: String
  def syntax: Syntax = {
    val right = List.fill(argTokens.size)(Syntax.WildcardType)
    if (isReporter)
      Syntax.reporterSyntax(right = right, ret = Syntax.WildcardType)
    else
      Syntax.commandSyntax(right = right)
  }
}
