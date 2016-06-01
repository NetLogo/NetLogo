// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ ExtensionManager, FrontEndProcedure, StructureResults }
import scala.collection.immutable.ListMap
import org.nlogo.{ core, api, nvm },
  nvm.Procedure

import scala.collection.JavaConverters._

object CompilerBridge {

  def apply(structureResults: StructureResults,
    extensionManager: ExtensionManager,
    oldProcedures: ListMap[String, Procedure],
    topLevelDefs: Seq[core.ProcedureDefinition]): Seq[ProcedureDefinition] = {
      val newProcedures = (structureResults.procedures -- oldProcedures.keys).map {
        case (k, p) => k -> fromApiProcedure(p)
      }
      val backifier = new Backifier(
        structureResults.program, extensionManager, oldProcedures ++ newProcedures)
      val astBackifier = new ASTBackifier(backifier)
      val procedureMap: Iterable[(nvm.Procedure, core.ProcedureDefinition)] =
        newProcedures.values
          .map(p =>
            p -> topLevelDefs.find(_.procedure.name.equalsIgnoreCase(p.name)).get)
      val procedures = procedureMap.map((astBackifier.backifyProcedure _).tupled).toSeq
      // lambda-lift
      val allDefs = {
        val taskNumbers = Iterator.from(1)
        procedures.flatMap { procdef =>
          val lifter = new LambdaLifter(taskNumbers)
          procdef.accept(lifter)
          procdef +: lifter.children
        }
      }
      allDefs
  }

  private def fromApiProcedure(p: FrontEndProcedure): nvm.Procedure = {
    val proc = new Procedure(
      isReporter = p.isReporter,
      nameToken = p.nameToken,
      name = p.name,
      _displayName = if (p.displayName == "") None else Some(p.displayName),
      parent = null,
      argTokens = p.argTokens,
      initialArgs = p.args)
    proc.usableBy = p.agentClassString
    proc.topLevel = p.topLevel
    proc
  }
}
