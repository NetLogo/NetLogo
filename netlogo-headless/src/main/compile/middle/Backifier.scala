// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.{Instantiator, BreedIdentifierHandler, Program}
import org.nlogo.{ api, core, nvm, prim },

  nvm.Procedure.ProceduresMap

// This is seriously gross and horrible. - ST 4/11/14

class Backifier(
  program: Program,
  extensionManager: api.ExtensionManager,
  procedures: ProceduresMap) {

  private def backifyName(name: String): String =
    name.replaceFirst("\\.core\\.", ".")

  private def fallback[T1 <: core.Instruction, T2 <: nvm.Instruction](i: T1): T2 =
    BreedIdentifierHandler.process(i.token.copy(value = i.token.text.toUpperCase), program) match {
      case None =>
        Instantiator.newInstance[T2](
          Class.forName(backifyName(i.getClass.getName)))
      case Some((className, breedName, _)) =>
        Instantiator.newInstance[T2](
          Class.forName("org.nlogo.prim." + className), breedName)
    }

  def apply(c: core.Command): nvm.Command = {
    val result = c match {
      case core.prim._extern(_) =>
        new prim._extern(
          extensionManager.replaceIdentifier(c.token.text.toUpperCase)
            .asInstanceOf[api.Command])
      case core.prim._call(proc) =>
        new prim._call(procedures(proc.name))
      case core.prim._let(let) =>
        new prim._let(let)
      case cc: core.prim._carefully =>
        new prim._carefully(cc.let)
      case _ =>
        fallback[core.Command, nvm.Command](c)
    }
    result.token = c.token
    result.agentClassString = c.agentClassString
    result
  }

  def apply(r: core.Reporter): nvm.Reporter = {
    val result = r match {

      case core.prim._letvariable(let) =>
        new prim._letvariable(let)

      case core.prim._const(value) =>
        new prim._const(value)

      case core.prim._commandtask(argcount) =>
        new prim._commandtask(argcount, null)  // LambdaLifter will fill in

      case core.prim._reportertask(argcount) =>
        new prim._reportertask(argcount)

      case core.prim._externreport(_) =>
        new prim._externreport(
          extensionManager.replaceIdentifier(r.token.text.toUpperCase)
            .asInstanceOf[api.Reporter])

      case core.prim._breedvariable(varName) =>
        new prim._breedvariable(varName)
      case core.prim._linkbreedvariable(varName) =>
        new prim._linkbreedvariable(varName)

      case core.prim._procedurevariable(vn, name) =>
        new prim._procedurevariable(vn, name)
      case core.prim._taskvariable(vn) =>
        new prim._taskvariable(vn)

      case core.prim._observervariable(vn, _) =>
        new prim._observervariable(vn)
      case core.prim._turtlevariable(vn, _) =>
        new prim._turtlevariable(vn)
      case core.prim._linkvariable(vn, _) =>
        new prim._linkvariable(vn)
      case core.prim._patchvariable(vn, _) =>
        new prim._patchvariable(vn)
      case core.prim._turtleorlinkvariable(varName, _) =>
        new prim._turtleorlinkvariable(varName)

      case core.prim._callreport(proc) =>
        new prim._callreport(procedures(proc.name))

      case core.prim._errormessage(Some(let)) =>
        new prim._errormessage(let)
      case core.prim._errormessage(None) =>
        throw new Exception("Parse error - errormessage not matched with carefully")

      // diabolical special case: if we have e.g. `breed [fish]` with no singular,
      // then the singular defaults to `turtle`, which will cause BreedIdentifierHandler
      // to interpret "turtle" as _breedsingular - ST 4/12/14
      case core.prim._turtle() =>
        new prim._turtle()

      case _ =>
        fallback[core.Reporter, nvm.Reporter](r)

    }
    result.token = r.token
    result.agentClassString = r.agentClassString
    result
  }

}
