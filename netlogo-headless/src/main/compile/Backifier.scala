// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.core.{Instantiator, BreedIdentifierHandler, Program}
import org.nlogo.{ api => nlogoApi, core, nvm, prim => nvmprim },
  nvm.Procedure.ProceduresMap
import org.nlogo.compile.api.{ Backifier => BackifierInterface }

// This is seriously gross and horrible. - ST 4/11/14

class Backifier(
  program: Program,
  extensionManager: nlogoApi.ExtensionManager) extends BackifierInterface {

  private def backifyName(name: String): String =
    name.replaceFirst("\\.core\\.", ".")

  private def fallback[T1 <: core.Instruction, T2 <: nvm.Instruction](i: T1): T2 =
    BreedIdentifierHandler.process(i.token.copy(value = i.token.text.toUpperCase)(), program) match {
      case None =>
        Instantiator.newInstance[T2](
          Class.forName(backifyName(i.getClass.getName)))
      case Some((className, breedName, _)) =>
        Instantiator.newInstance[T2](
          Class.forName("org.nlogo.prim." + className), breedName)
    }

  def apply(procedures: ProceduresMap, c: core.Command): nvm.Command = {
    val result = c match {
      case core.prim._extern(_) =>
        new nvmprim._extern(
          extensionManager.replaceIdentifier(c.token.text.toUpperCase)
            .asInstanceOf[nlogoApi.Command])

      case core.prim._call(proc) =>
        new nvmprim._call(procedures(proc.name))

      case core.prim._let(Some(let), _) =>
        new nvmprim._let(let)

      case core.prim._multilet(lets) =>
        new nvmprim._multiassign("LET", lets.size)

      case core.prim._multiassignnest(prim, totalNeeded) =>
        new nvmprim._multiassignnest(prim, totalNeeded)

      case core.prim._multiset(sets) =>
        new nvmprim._multiassign("SET", sets.size)

      case cc: core.prim._carefully =>
        new nvmprim._carefully(cc.let)

      case bk: core.prim._bk =>
        new nvmprim._bk(c.token)

      case fd: core.prim._fd =>
        new nvmprim._fd(c.token)

      case rep: core.prim._repeat =>
        new nvmprim._repeat(c.token)

      case _ =>
        fallback[core.Command, nvm.Command](c)
    }
    result.token = c.token
    result.agentClassString = c.agentClassString
    result
  }

  def apply(procedures: ProceduresMap, r: core.Reporter): nvm.Reporter = {
    val result = r match {

      case core.prim._letvariable(let) =>
        new nvmprim._letvariable(let)

      case core.prim._const(value) =>
        new nvmprim._const(value)

      case core.prim._commandlambda(args, closedVariables, source) =>
        new nvmprim._commandlambda(args, null, closedVariables, source.getOrElse("")) // LambdaLifter will fill in

      case core.prim._reporterlambda(args, closedVariables, source) =>
        new nvmprim._reporterlambda(args, closedVariables, source.getOrElse(""))

      case core.prim._externreport(_) =>
        new nvmprim._externreport(
          extensionManager.replaceIdentifier(r.token.text.toUpperCase)
            .asInstanceOf[nlogoApi.Reporter])

      case core.prim._breedvariable(varName) =>
        new nvmprim._breedvariable(varName)
      case core.prim._linkbreedvariable(varName) =>
        new nvmprim._linkbreedvariable(varName)

      case core.prim._procedurevariable(vn, name) =>
        new nvmprim._procedurevariable(vn, name)
      case core.prim._lambdavariable(name, _) =>
        new nvmprim._lambdavariable(name)

      case core.prim._observervariable(vn, _) =>
        new nvmprim._observervariable(vn)
      case core.prim._turtlevariable(vn, _) =>
        new nvmprim._turtlevariable(vn)
      case core.prim._linkvariable(vn, _) =>
        new nvmprim._linkvariable(vn)
      case core.prim._patchvariable(vn, _) =>
        new nvmprim._patchvariable(vn)
      case core.prim._turtleorlinkvariable(varName, _) =>
        new nvmprim._turtleorlinkvariable(varName)

      case core.prim._callreport(proc) =>
        new nvmprim._callreport(procedures(proc.name))

      case core.prim._errormessage(Some(let)) =>
        new nvmprim._errormessage(let)
      case core.prim._errormessage(None) =>
        throw new Exception("Parse error - errormessage not matched with carefully")

      case core.prim._constcodeblock(toks) =>
        new nvmprim._constcodeblock(toks)

      case s: core.prim._symbol =>
        new nvmprim._constsymbol(s.token)

      // diabolical special case: if we have e.g. `breed [fish]` with no singular,
      // then the singular defaults to `turtle`, which will cause BreedIdentifierHandler
      // to interpret "turtle" as _breedsingular - ST 4/12/14
      case core.prim._turtle() =>
        new nvmprim._turtle()

      case _ =>
        fallback[core.Reporter, nvm.Reporter](r)

    }
    result.token = r.token
    result.agentClassString = r.agentClassString
    result
  }

}
