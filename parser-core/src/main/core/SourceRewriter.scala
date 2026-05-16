// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait SourceRewriter {
  def renameBreedSingular(breed: String, replacement: String): String
  def renameBreedPlural(breed: String, replacement: String): String
  def reorderDeclarations(): String
  def addGlobal(global: String): String
  def addExtension(extension: String): String
  def removeExtension(extension: String): String
  def addReporterProcedure(name: String, args: Seq[String], body: String): String
  def remove(command: String): String
  def replaceToken(originalToken: String, replaceToken:String): String
  def addCommand(command: String, addition: String): String
  def replace(primitive: String, replacement: String): String
  def lambdaize(): String
}
