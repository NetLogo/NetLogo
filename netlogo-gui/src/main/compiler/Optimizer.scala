// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.Instantiator
import org.nlogo.agent.Patch
import org.nlogo.api.{ LogoException, Version }
import org.nlogo.nvm.{ Command, Instruction, Reporter }
import org.nlogo.prim._

// "asInstanceOf" is everywhere here. Could I make it more type-safe? - ST 1/28/09

private class Optimizer(is3D: Boolean) extends DefaultAstVisitor {

  override def visitProcedureDefinition(defn: ProcedureDefinition) {
    if(Version.useOptimizer)
      super.visitProcedureDefinition(defn)
  }
  override def visitStatement(stmt: Statement) {
    super.visitStatement(stmt)
    val oldCommand = stmt.command
    commandMungers.filter(_.clazz eq oldCommand.getClass)
      .find{munger => munger.munge(stmt); stmt.command != oldCommand}
  }
  override def visitReporterApp(app: ReporterApp) {
    super.visitReporterApp(app)
    val oldReporter = app.reporter
    reporterMungers.filter(_.clazz eq oldReporter.getClass)
      .find{munger => munger.munge(app); app.reporter != oldReporter}
  }

  private val commandMungers =
    List[CommandMunger](Fd1, FdLessThan1, FastHatch, FastSprout, FastCrt, FastCro)
  private val reporterMungers =
    List[ReporterMunger](PatchAt, With, OneOfWith, Nsum, Nsum4,
         CountWith, OtherWith, WithOther, AnyOther, AnyOtherWith, CountOther, CountOtherWith,
         AnyWith1, AnyWith2, AnyWith3, AnyWith4, AnyWith5,
         PatchVariableDouble, TurtleVariableDouble, RandomConst)

  private class MatchFailedException extends Exception
  private abstract class CommandMunger { val clazz: Class[_ <: Command]; def munge(stmt: Statement) }
  private abstract class ReporterMunger { val clazz: Class[_ <: Reporter]; def munge(app: ReporterApp) }

  private abstract class RewritingCommandMunger extends CommandMunger {
    def munge(stmt: Statement) {
      try munge(new Match(stmt))
      catch { case _: MatchFailedException => }
    }
    def munge(root: Match)
  }
  private abstract class RewritingReporterMunger extends ReporterMunger {
    def munge(app: ReporterApp) {
      try munge(new Match(app))
      catch { case _: MatchFailedException => }
    }
    def munge(root: Match)
  }

  private class Match(val node: AstNode) {
    def matchit(theClass: Class[_ <: Instruction]) =
      node match {
        case app: ReporterApp if theClass.isInstance(app.reporter) => this
        case stmt: Statement if theClass.isInstance(stmt.command) => this
        case _ => throw new MatchFailedException
      }
    def command =
      node match {
        case stmt: Statement => stmt.command
        case _ => throw new MatchFailedException
      }
    def reporter =
      node match {
        case app: ReporterApp => app.reporter
        case _ => throw new MatchFailedException
      }
    def matchEmptyCommandBlockIsLastArg =
      node match {
        case stmt: Statement if !stmt.args.isEmpty =>
          stmt.args.last match {
            case block: CommandBlock if block.statements.body.size == 0 => new Match(block)
            case _ => throw new MatchFailedException
          }
        case _ => throw new MatchFailedException
      }
    def matchArg(index: Int) = {
      val args = node match {
                   case stmt: Statement => stmt.args
                   case app: ReporterApp => app.args
                   case _ => throw new MatchFailedException
                 }
      if(index >= args.size) throw new MatchFailedException
      args(index) match {
        case app: ReporterApp => new Match(app)
        case block: ReporterBlock => new Match(block)
        case _ => throw new MatchFailedException
      }
    }
    def matchArg(index: Int, classes: Class[_ <: Instruction]*) = {
      val args = node match {
                   case stmt: Statement => stmt.args
                   case app: ReporterApp => app.args
                   case _ => throw new MatchFailedException
                 }
      if(index >= args.size) throw new MatchFailedException
      args(index) match {
        case app: ReporterApp if classes.exists(_.isInstance(app.reporter)) => new Match(app)
        case _ => throw new MatchFailedException
      }
    }
    def matchReporterBlock() = {
      node match {
        case block: ReporterBlock => new Match(block.app)
        case _ => throw new MatchFailedException
      }
    }
    def matchOneArg(theClass: Class[_ <: Instruction]) = {
      try matchArg(0, theClass)
      catch { case _: MatchFailedException => matchArg(1, theClass) }
    }
    def matchOtherArg(alreadyMatched: Match, classes: Class[_ <: Instruction]*): Match = {
      val result: Match =
        try matchArg(0, classes: _*)
        catch { case _: MatchFailedException => return matchArg(1, classes: _*) }
      if(result.node eq alreadyMatched.node) matchArg(1, classes: _*)
      else result
    }
    def report =
      try node.asInstanceOf[ReporterApp].reporter.report(null)
      catch { case ex: LogoException => throw new IllegalStateException(ex) }
    def strip() {
      node match {
        case app: ReporterApp =>
          while(!app.args.isEmpty) app.removeArgument(0)
        case stmt: Statement =>
          while(!stmt.args.isEmpty) stmt.removeArgument(0)
      }
    }
    def graftArg(newArg: Match) {
      node match {
        case app: ReporterApp => app.addArgument(newArg.node.asInstanceOf[Expression])
        case stmt: Statement => stmt.addArgument(newArg.node.asInstanceOf[Expression])
      }
    }
    def removeLastArg() {
      node match {
        case app: ReporterApp => app.removeArgument(app.args.size - 1)
        case stmt: Statement => stmt.removeArgument(stmt.args.size - 1)
      }
    }
    def replace(theClass: Class[_ <: Instruction], constructorArgs: AnyRef*) {
      val newGuy = Instantiator.newInstance[Instruction](theClass, constructorArgs: _*)
      node match {
        case app: ReporterApp =>
          newGuy.copyMetadataFrom(app.reporter)
          app.reporter = newGuy.asInstanceOf[Reporter]
        case stmt: Statement =>
          newGuy.copyMetadataFrom(stmt.command)
          stmt.command = newGuy.asInstanceOf[Command]
      }
    }
    def addArg(theClass: Class[_ <: Reporter], original: ReporterApp): Match = {
      val newGuy = Instantiator.newInstance[Reporter](theClass)
      newGuy.copyMetadataFrom(original.reporter)
      val result = new Match(new ReporterApp(original.coreReporter, newGuy, original.start, original.end, original.file))
      graftArg(result)
      result
    }
  }

  /// now for the individual optimizations
  private object Fd1 extends RewritingCommandMunger {
    val clazz = classOf[_fd]
    def munge(root: Match) {
      if(root.matchArg(0, classOf[_constdouble]).reporter.asInstanceOf[_constdouble].primitiveValue == 1) {
        root.strip()
        root.replace(classOf[_fd1])
      }
    }
  }
  private object FdLessThan1 extends RewritingCommandMunger {
    val clazz = classOf[_fd]
    def munge(root: Match) {
      val d = root.matchArg(0, classOf[_constdouble]).reporter.asInstanceOf[_constdouble].primitiveValue
      if(d < 1 && d > -1) {
        root.replace(classOf[_jump])
      }
    }
  }
  private object FastHatch extends RewritingCommandMunger {
    val clazz = classOf[_hatch]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_fasthatch],
                   (root.command.asInstanceOf[_hatch]).breedName)
    }
  }
  private object FastSprout extends RewritingCommandMunger {
    val clazz = classOf[_sprout]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_fastsprout],
                   (root.command.asInstanceOf[_sprout]).breedName)
    }
  }
  private object FastCrt extends RewritingCommandMunger {
    val clazz = classOf[_createturtles]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_fastcreateturtles],
                   (root.command.asInstanceOf[_createturtles]).breedName)
    }
  }
  private object FastCro extends RewritingCommandMunger {
    val clazz = classOf[_createorderedturtles]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_fastcreateorderedturtles],
                   (root.command.asInstanceOf[_createorderedturtles]).breedName)
    }
  }
  private object PatchAt extends RewritingReporterMunger {
    val clazz = classOf[_patchat]
    def munge(root: Match) {
      val x = root.matchArg(0, classOf[_constdouble]).reporter.asInstanceOf[_constdouble].primitiveValue
      val y = root.matchArg(1, classOf[_constdouble]).reporter.asInstanceOf[_constdouble].primitiveValue
      val newClass = (x, y) match {
        case ( 0,  0) => classOf[_patchhereinternal]
        case ( 0, -1) => classOf[_patchsouth]
        case ( 0,  1) => classOf[_patchnorth]
        case (-1,  0) => classOf[_patchwest]
        case (-1, -1) => classOf[_patchsw]
        case (-1,  1) => classOf[_patchnw]
        case ( 1,  0) => classOf[_patcheast]
        case ( 1, -1) => classOf[_patchse]
        case ( 1,  1) => classOf[_patchne]
        case _ => return
      }
      root.strip()
      root.replace(newClass)
    }
  }
  // _with(_patches, _equal(_constdouble, _px/ycor)) => _patchcol/_patchrow
  private object With extends RewritingReporterMunger {
    val clazz = classOf[_with]
    def munge(root: Match) {
      // this optimization doesn't work in 3D, we could fix it but not now - ev 6/27/07, ST 3/3/08
      if(is3D) return
      root.matchArg(0, classOf[_patches])
      val arg1 = root.matchArg(1).matchReporterBlock().matchit(classOf[_equal])
      val pcor = arg1.matchOneArg(classOf[_patchvariabledouble])
      val value = arg1.matchOtherArg(pcor, classOf[_constdouble], classOf[_procedurevariable],
                                     classOf[_observervariable])
      val vn = pcor.reporter.asInstanceOf[_patchvariabledouble].vn
      val newClass = vn match {
        case Patch.VAR_PXCOR => classOf[_patchcol]
        case Patch.VAR_PYCOR => classOf[_patchrow]
        case _ => return
      }
      root.strip()
      root.replace(newClass)
      root.graftArg(value)
    }
  }
  // _oneof(_with) => _oneofwith
  private object OneOfWith extends RewritingReporterMunger {
    val clazz = classOf[_oneof]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_with])
      root.strip()
      root.replace(classOf[_oneofwith])
      root.graftArg(arg0.matchArg(0))
      root.graftArg(arg0.matchArg(1))
    }
  }
  private object Nsum extends RewritingReporterMunger {
    val clazz = classOf[_sum]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_patchvariableof])
      arg0.matchArg(0, classOf[_neighbors])
      root.strip()
      root.replace(classOf[_nsum])
      root.reporter.asInstanceOf[_nsum].vn =
        arg0.reporter.asInstanceOf[_patchvariableof].vn
    }
  }
  private object Nsum4 extends RewritingReporterMunger {
    val clazz = classOf[_sum]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_patchvariableof])
      arg0.matchArg(0, classOf[_neighbors4])
      root.strip()
      root.replace(classOf[_nsum4])
      root.reporter.asInstanceOf[_nsum4].vn =
        arg0.reporter.asInstanceOf[_patchvariableof].vn
    }
  }
  // _count(_with) => _countwith
  private object CountWith extends RewritingReporterMunger {
    val clazz = classOf[_count]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_with])
      root.strip()
      root.replace(classOf[_countwith])
      root.graftArg(arg0.matchArg(0))
      root.graftArg(arg0.matchArg(1))
    }
  }
  // _other(_with(*, *)) => _otherwith(*, *)
  // _with(_other(*), *) => _otherwith(*, *)
  private object OtherWith extends RewritingReporterMunger {
    val clazz = classOf[_other]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_with])
      root.strip()
      root.replace(classOf[_otherwith])
      root.graftArg(arg0.matchArg(0))
      root.graftArg(arg0.matchArg(1))
    }
  }
  private object WithOther extends RewritingReporterMunger {
    val clazz = classOf[_with]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_other])
      val arg1 = root.matchArg(1)
      root.replace(classOf[_otherwith])
      root.strip()
      root.graftArg(arg0.matchArg(0))
      root.graftArg(arg1)
    }
  }
  // _any(_other(*)) => _anyother(*)
  private object AnyOther extends RewritingReporterMunger {
    val clazz = classOf[_any]
    def munge(root: Match) {
      val arg = root.matchArg(0, classOf[_other])
      root.strip()
      root.replace(classOf[_anyother])
      root.graftArg(arg.matchArg(0))
    }
  }
  // _any(_otherwith(*, *)) => _anyotherwith(*, *)
  private object AnyOtherWith extends RewritingReporterMunger {
    val clazz = classOf[_any]
    def munge(root: Match) {
      val arg = root.matchArg(0, classOf[_otherwith])
      root.strip()
      root.replace(classOf[_anyotherwith])
      root.graftArg(arg.matchArg(0))
      root.graftArg(arg.matchArg(1))
    }
  }
  // _count(_other(*)) => _countother(*)
  private object CountOther extends RewritingReporterMunger {
    val clazz = classOf[_count]
    def munge(root: Match) {
      val arg = root.matchArg(0, classOf[_other])
      root.strip()
      root.replace(classOf[_countother])
      root.graftArg(arg.matchArg(0))
    }
  }
  // _count(_otherwith(*, *)) => _countotherwith(*, *)
  private object CountOtherWith extends RewritingReporterMunger {
    val clazz = classOf[_count]
    def munge(root: Match) {
      val arg = root.matchArg(0, classOf[_otherwith])
      root.strip()
      root.replace(classOf[_countotherwith])
      root.graftArg(arg.matchArg(0))
      root.graftArg(arg.matchArg(1))
    }
  }
  // _any(_with) => _anywith
  private object AnyWith1 extends RewritingReporterMunger {
    val clazz = classOf[_any]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_with])
      root.strip()
      root.replace(classOf[_anywith])
      root.graftArg(arg0.matchArg(0))
      root.graftArg(arg0.matchArg(1))
    }
  }
  // _notequal(_countwith(*, *), _constdouble: 0.0) => _anywith(*, *)
  private object AnyWith2 extends RewritingReporterMunger {
    val clazz = classOf[_notequal]
    def munge(root: Match) {
      val count = root.matchOneArg(classOf[_countwith])
      if(root.matchOtherArg(count, classOf[_constdouble]).reporter.asInstanceOf[_constdouble]
           .primitiveValue == 0)
      {
        root.strip()
        root.replace(classOf[_anywith])
        root.graftArg(count.matchArg(0))
        root.graftArg(count.matchArg(1))
      }
    }
  }
  // _greaterthan(_countwith(*, *), _constdouble: 0.0) => _anywith(*, *)
  private object AnyWith3 extends RewritingReporterMunger {
    val clazz = classOf[_greaterthan]
    def munge(root: Match) {
      val count = root.matchArg(0, classOf[_countwith])
      if(root.matchArg(1, classOf[_constdouble]).reporter.asInstanceOf[_constdouble]
           .primitiveValue == 0)
      {
        root.strip()
        root.replace(classOf[_anywith])
        root.graftArg(count.matchArg(0))
        root.graftArg(count.matchArg(1))
      }
    }
  }
  // _lessthan(_constdouble: 0.0, _countwith(*, *)) => _anywith(*, *)
  private object AnyWith4 extends RewritingReporterMunger {
    val clazz = classOf[_lessthan]
    def munge(root: Match) {
      val count = root.matchArg(1, classOf[_countwith])
      if(root.matchArg(0, classOf[_constdouble]).reporter.asInstanceOf[_constdouble]
           .primitiveValue == 0)
      {
        root.strip()
        root.replace(classOf[_anywith])
        root.graftArg(count.matchArg(0))
        root.graftArg(count.matchArg(1))
      }
    }
  }
  // _equal(_countwith(*, *), _constdouble: 0.0) => _not(_anywith(*, *))
  private object AnyWith5 extends RewritingReporterMunger {
    val clazz = classOf[_equal]
    def munge(root: Match) {
      val count = root.matchOneArg(classOf[_countwith])
      if(root.matchOtherArg(count, classOf[_constdouble]).reporter.asInstanceOf[_constdouble]
           .primitiveValue == 0)
      {
        val oldRoot = root.node.asInstanceOf[ReporterApp]
        root.strip()
        root.replace(classOf[_not])
        val anywith = root.addArg(classOf[_anywith], oldRoot)
        anywith.graftArg(count.matchArg(0))
        anywith.graftArg(count.matchArg(1))
      }
    }
  }
  // _patchvariable => _patchvariabledouble
  private object PatchVariableDouble extends RewritingReporterMunger {
    val clazz = classOf[_patchvariable]
    def munge(root: Match) {
      val vn = root.reporter.asInstanceOf[_patchvariable].vn
      if(org.nlogo.api.AgentVariables.isDoublePatchVariable(vn, is3D)) {
        root.replace(classOf[_patchvariabledouble])
        root.reporter.asInstanceOf[_patchvariabledouble].vn = vn
      }
    }
  }
  // _turtlevariable => _turtlevariabledouble
  private object TurtleVariableDouble extends RewritingReporterMunger {
    val clazz = classOf[_turtlevariable]
    def munge(root: Match) {
      val vn = root.reporter.asInstanceOf[_turtlevariable].vn
      if(org.nlogo.api.AgentVariables.isDoubleTurtleVariable(vn, is3D)) {
        root.replace(classOf[_turtlevariabledouble])
        root.reporter.asInstanceOf[_turtlevariabledouble].vn = vn
      }
    }
  }
  // _random(_constdouble) => _randomconst  (if argument is a positive integer)
  private object RandomConst extends RewritingReporterMunger {
    val clazz = classOf[_random]
    def munge(root: Match) {
      val d = root.matchArg(0, classOf[_constdouble])
                  .reporter.asInstanceOf[_constdouble].primitiveValue
      if(d > 0 && d == d.toLong && Instruction.isValidLong(d)) {
        root.replace(classOf[_randomconst])
        root.strip()
        root.reporter.asInstanceOf[_randomconst].n = d.toLong
      }
    }
  }

}
