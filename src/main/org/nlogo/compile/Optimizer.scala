// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ api, parse }, api.LogoException
import org.nlogo.agent.Patch
import org.nlogo.nvm.{ Command, Instruction, Reporter }
import org.nlogo.prim._

// "asInstanceOf" is everywhere here. Could I make it more type-safe? - ST 1/28/09

private object Optimizer extends parse.DefaultAstVisitor {

  override def visitStatement(stmt: parse.Statement) {
    super.visitStatement(stmt)
    val oldCommand = stmt.command
    commandMungers.filter(_.clazz eq oldCommand.getClass)
      .find{munger => munger.munge(stmt); stmt.command != oldCommand}
  }

  override def visitReporterApp(app: parse.ReporterApp) {
    super.visitReporterApp(app)
    val oldReporter = app.reporter
    reporterMungers.filter(_.clazz eq oldReporter.getClass)
      .find{munger => munger.munge(app); app.reporter != oldReporter}
  }

  private val commandMungers =
    List[CommandMunger](Fd1, FdLessThan1, HatchFast, SproutFast, CrtFast, CroFast)
  private val reporterMungers =
    List[ReporterMunger](PatchAt, With, OneOfWith, Nsum, Nsum4,
         CountWith, OtherWith, WithOther, AnyOther, AnyOtherWith, CountOther, CountOtherWith,
         AnyWith1, AnyWith2, AnyWith3, AnyWith4, AnyWith5,
         PatchVariableDouble, TurtleVariableDouble, RandomConst)

  private class MatchFailedException extends Exception
  private abstract class CommandMunger { val clazz: Class[_ <: Command]; def munge(stmt: parse.Statement) }
  private abstract class ReporterMunger { val clazz: Class[_ <: Reporter]; def munge(app: parse.ReporterApp) }

  private abstract class RewritingCommandMunger extends CommandMunger {
    def munge(stmt: parse.Statement) {
      try munge(new Match(stmt))
      catch { case _: MatchFailedException => }
    }
    def munge(root: Match)
  }
  private abstract class RewritingReporterMunger extends ReporterMunger {
    def munge(app: parse.ReporterApp) {
      try munge(new Match(app))
      catch { case _: MatchFailedException => }
    }
    def munge(root: Match)
  }

  private class Match(val node: parse.AstNode) {
    def matchit(theClass: Class[_ <: Instruction]) =
      node match {
        case app: parse.ReporterApp if theClass.isInstance(app.reporter) => this
        case stmt: parse.Statement if theClass.isInstance(stmt.command) => this
        case _ => throw new MatchFailedException
      }
    def command =
      node match {
        case stmt: parse.Statement => stmt.command
        case _ => throw new MatchFailedException
      }
    def reporter =
      node match {
        case app: parse.ReporterApp => app.reporter
        case _ => throw new MatchFailedException
      }
    def matchEmptyCommandBlockIsLastArg =
      node match {
        case stmt: parse.Statement if !stmt.args.isEmpty =>
          stmt.args.last match {
            case block: parse.CommandBlock if block.statements.size == 0 =>
              new Match(block)
            case _ =>
              throw new MatchFailedException
          }
        case _ => throw new MatchFailedException
      }
    def matchArg(index: Int) = {
      val args = node match {
                   case stmt: parse.Statement => stmt.args
                   case app: parse.ReporterApp => app.args
                   case _ => throw new MatchFailedException
                 }
      if(index >= args.size) throw new MatchFailedException
      args(index) match {
        case app: parse.ReporterApp =>
          new Match(app)
        case block: parse.ReporterBlock =>
          new Match(block)
        case _ =>
          throw new MatchFailedException
      }
    }
    def matchArg(index: Int, classes: Class[_ <: Instruction]*) = {
      val args = node match {
                   case stmt: parse.Statement => stmt.args
                   case app: parse.ReporterApp => app.args
                   case _ => throw new MatchFailedException
                 }
      if(index >= args.size) throw new MatchFailedException
      args(index) match {
        case app: parse.ReporterApp if classes.exists(_.isInstance(app.reporter)) => new Match(app)
        case _ => throw new MatchFailedException
      }
    }
    def matchReporterBlock() = {
      node match {
        case block: parse.ReporterBlock =>
          new Match(block.app)
        case _ =>
          throw new MatchFailedException
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
      try node.asInstanceOf[parse.ReporterApp].reporter.report(null)
      catch { case ex: LogoException =>
          throw new IllegalStateException(ex) }
    def strip() {
      node match {
        case app: parse.ReporterApp =>
          while(!app.args.isEmpty) app.removeArgument(0)
        case stmt: parse.Statement =>
          while(!stmt.args.isEmpty) stmt.removeArgument(0)
      }
    }
    def graftArg(newArg: Match) {
      node match {
        case app: parse.ReporterApp =>
          app.addArgument(newArg.node.asInstanceOf[parse.Expression])
        case stmt: parse.Statement =>
          stmt.addArgument(newArg.node.asInstanceOf[parse.Expression])
      }
    }
    def removeLastArg() {
      node match {
        case app: parse.ReporterApp => app.removeArgument(app.args.size - 1)
        case stmt: parse.Statement => stmt.removeArgument(stmt.args.size - 1)
      }
    }
    def replace(theClass: Class[_ <: Instruction], constructorArgs: Any*) {
      val newGuy = parse.Instantiator.newInstance[Instruction](theClass, constructorArgs: _*)
      node match {
        case app: parse.ReporterApp =>
          newGuy.token(app.reporter.token)
          app.reporter = newGuy.asInstanceOf[Reporter]
        case stmt: parse.Statement =>
          newGuy.token(stmt.command.token)
          stmt.command = newGuy.asInstanceOf[Command]
      }
    }
    def addArg(theClass: Class[_ <: Reporter], original: parse.ReporterApp): Match = {
      val newGuy = parse.Instantiator.newInstance[Reporter](theClass)
      newGuy.token(original.reporter.token)
      val result = new Match(new parse.ReporterApp(
        newGuy, original.start, original.end, original.file))
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
  private object HatchFast extends RewritingCommandMunger {
    val clazz = classOf[_hatch]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_hatchfast],
                   (root.command.asInstanceOf[_hatch]).breedName)
    }
  }
  private object SproutFast extends RewritingCommandMunger {
    val clazz = classOf[_sprout]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_sproutfast],
                   (root.command.asInstanceOf[_sprout]).breedName)
    }
  }
  private object CrtFast extends RewritingCommandMunger {
    val clazz = classOf[_createturtles]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_crtfast],
                   (root.command.asInstanceOf[_createturtles]).breedName)
    }
  }
  private object CroFast extends RewritingCommandMunger {
    val clazz = classOf[_createorderedturtles]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_crofast],
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
      root.replace(classOf[_nsum],
                   arg0.reporter.asInstanceOf[_patchvariableof].vn)
    }
  }
  private object Nsum4 extends RewritingReporterMunger {
    val clazz = classOf[_sum]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_patchvariableof])
      arg0.matchArg(0, classOf[_neighbors4])
      root.strip()
      root.replace(classOf[_nsum4],
                   arg0.reporter.asInstanceOf[_patchvariableof].vn)
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
        val oldRoot = root.node.asInstanceOf[parse.ReporterApp]
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
    def isDoubleVariable(vn: Int) =
      vn == api.AgentVariableNumbers.VAR_PXCOR ||
      vn == api.AgentVariableNumbers.VAR_PYCOR
    def munge(root: Match) {
      val vn = root.reporter.asInstanceOf[_patchvariable].vn
      if(isDoubleVariable(vn)) {
        root.replace(classOf[_patchvariabledouble])
        root.reporter.asInstanceOf[_patchvariabledouble].vn = vn
      }
    }
  }
  // _turtlevariable => _turtlevariabledouble
  private object TurtleVariableDouble extends RewritingReporterMunger {
    val clazz = classOf[_turtlevariable]
    val isDoubleVariable: Double => Boolean = {
      import api.AgentVariableNumbers._
      Set(VAR_WHO, VAR_HEADING, VAR_XCOR, VAR_YCOR, VAR_SIZE, VAR_PENSIZE)
    }
    def munge(root: Match) {
      val vn = root.reporter.asInstanceOf[_turtlevariable].vn
      if(isDoubleVariable(vn)) {
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
      if(d > 0 && d == d.toLong && api.Numbers.isValidLong(d)) {
        root.replace(classOf[_randomconst], d.toLong)
        root.strip()
      }
    }
  }

}
