// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.middle

import org.nlogo.prim._

import org.nlogo.core.Dialect
import org.nlogo.{ api => nlogoApi }
import org.nlogo.agent.Patch
import org.nlogo.nvm.{ Command, Instruction, Reporter }
import org.nlogo.prim._
import org.nlogo.compile.api.{ CommandMunger,
  DefaultAstVisitor, Match, ReporterApp,
  ReporterMunger, RewritingCommandMunger, RewritingReporterMunger, Statement }

import scala.reflect.ClassTag

// These optimizations are shared between GUI and headless. Any optimizations specific to either
// should go in the appropriate org.nlogo.compile.optimize package
package optimize {
  /// now for the individual optimizations
  object Fd1 extends RewritingCommandMunger {
    val clazz = classOf[_fd]
    def munge(root: Match) {
      if (root.matchArg(0, classOf[_constdouble]).reporter.asInstanceOf[_constdouble].primitiveValue == 1) {
        root.strip()
        root.replace(classOf[_fd1])
      }
    }
  }
  object FdLessThan1 extends RewritingCommandMunger {
    val clazz = classOf[_fd]
    def munge(root: Match) {
      val d = root.matchArg(0, classOf[_constdouble]).reporter.asInstanceOf[_constdouble].primitiveValue
      if(d < 1 && d > -1) {
        root.replace(classOf[_jump])
      }
    }
  }
  object HatchFast extends RewritingCommandMunger {
    val clazz = classOf[_hatch]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_hatchfast],
                   (root.command.asInstanceOf[_hatch]).breedName)
    }
  }
  object SproutFast extends RewritingCommandMunger {
    val clazz = classOf[_sprout]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_sproutfast],
                   (root.command.asInstanceOf[_sprout]).breedName)
    }
  }
  object CrtFast extends RewritingCommandMunger {
    val clazz = classOf[_createturtles]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_crtfast],
                   (root.command.asInstanceOf[_createturtles]).breedName)
    }
  }
  object CroFast extends RewritingCommandMunger {
    val clazz = classOf[_createorderedturtles]
    def munge(root: Match) {
      root.matchEmptyCommandBlockIsLastArg
      root.removeLastArg()
      root.replace(classOf[_crofast],
                   (root.command.asInstanceOf[_createorderedturtles]).breedName)
    }
  }
  object PatchAt extends RewritingReporterMunger {
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
  object With extends RewritingReporterMunger {
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
  object OneOfWith extends RewritingReporterMunger {
    val clazz = classOf[_oneof]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_with])
      root.strip()
      root.replace(classOf[_oneofwith])
      root.graftArg(arg0.matchArg(0))
      root.graftArg(arg0.matchArg(1))
    }
  }
  object Nsum extends RewritingReporterMunger {
    val clazz = classOf[_sum]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_patchvariableof])
      arg0.matchArg(0, classOf[_neighbors])
      root.strip()
      root.replace(classOf[_nsum],
                   arg0.reporter.asInstanceOf[_patchvariableof].vn)
    }
  }
  object Nsum4 extends RewritingReporterMunger {
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
  object CountWith extends RewritingReporterMunger {
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
  object OtherWith extends RewritingReporterMunger {
    val clazz = classOf[_other]
    def munge(root: Match) {
      val arg0 = root.matchArg(0, classOf[_with])
      root.strip()
      root.replace(classOf[_otherwith])
      root.graftArg(arg0.matchArg(0))
      root.graftArg(arg0.matchArg(1))
    }
  }
  object WithOther extends RewritingReporterMunger {
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
  object AnyOther extends RewritingReporterMunger {
    val clazz = classOf[_any]
    def munge(root: Match) {
      val arg = root.matchArg(0, classOf[_other])
      root.strip()
      root.replace(classOf[_anyother])
      root.graftArg(arg.matchArg(0))
    }
  }
  // _any(_otherwith(*, *)) => _anyotherwith(*, *)
  object AnyOtherWith extends RewritingReporterMunger {
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
  object CountOther extends RewritingReporterMunger {
    val clazz = classOf[_count]
    def munge(root: Match) {
      val arg = root.matchArg(0, classOf[_other])
      root.strip()
      root.replace(classOf[_countother])
      root.graftArg(arg.matchArg(0))
    }
  }
  // _count(_otherwith(*, *)) => _countotherwith(*, *)
  object CountOtherWith extends RewritingReporterMunger {
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
  object AnyWith1 extends RewritingReporterMunger {
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
  object AnyWith2 extends RewritingReporterMunger {
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
  object AnyWith3 extends RewritingReporterMunger {
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
  object AnyWith4 extends RewritingReporterMunger {
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
  object AnyWith5 extends RewritingReporterMunger {
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
  object PatchVariableDouble extends RewritingReporterMunger {
    val clazz = classOf[_patchvariable]
    def isDoubleVariable(vn: Int) =
      vn == nlogoApi.AgentVariableNumbers.VAR_PXCOR ||
      vn == nlogoApi.AgentVariableNumbers.VAR_PYCOR
    def munge(root: Match) {
      val vn = root.reporter.asInstanceOf[_patchvariable].vn
      if(isDoubleVariable(vn)) {
        root.replace(classOf[_patchvariabledouble])
        root.reporter.asInstanceOf[_patchvariabledouble].vn = vn
      }
    }
  }
  // _turtlevariable => _turtlevariabledouble
  object TurtleVariableDouble extends RewritingReporterMunger {
    val clazz = classOf[_turtlevariable]
    val isDoubleVariable: Int => Boolean = {
      import nlogoApi.AgentVariableNumbers._
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
  object RandomConst extends RewritingReporterMunger {
    val clazz = classOf[_random]
    def munge(root: Match) {
      val d = root.matchArg(0, classOf[_constdouble])
                  .reporter.asInstanceOf[_constdouble].primitiveValue
      if(d > 0 && d == d.toLong && nlogoApi.Numbers.isValidLong(d)) {
        root.replace(classOf[_randomconst], d.toLong)
        root.strip()
      }
    }
  }
}
