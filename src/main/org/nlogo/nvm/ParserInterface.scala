// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

// ought to be in the api package, except oops, it depends on nvm.Procedure - ST 2/23/09

import org.nlogo.api, api.{ Program, ExtensionManager, World }

object ParserInterface {
  // use ListMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04, 8/3/12
  import collection.immutable.ListMap
  type ProceduresMap = ListMap[String, Procedure]
  val NoProcedures: ProceduresMap = ListMap()
}

trait ParserInterface {
  import ParserInterface.ProceduresMap
  def readFromString(source: String): AnyRef
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.api.File, world: World, extensionManager: ExtensionManager): AnyRef
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager): Boolean
}
