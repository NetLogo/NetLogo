// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.api.{ LogoException, ReporterTask }
import org.nlogo.core.Syntax
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter, Task }

class _sortby extends Reporter {
  // see issue #172
  private val Java7SoPicky =
    "Comparison method violates its general contract!"

  override def report(context: Context) = {
    val task = argEvalReporterTask(context, 0)
    if (task.syntax.minimum > 2)
      throw new EngineException(context, this, Task.missingInputs(task, 2))
    val obj = args(1).report(context)
    val input = obj match {
      case list: LogoList =>
        // must copy the list, because Collections.sort() works in place - ST 7/31/04, 1/12/06
        new java.util.ArrayList[AnyRef](list.toJava)
      case agents: AgentSet =>
        val list = new java.util.ArrayList[AnyRef]
        val it = agents.shufflerator(context.job.random)
        while(it.hasNext)
          list.add(it.next())
        list
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ListType | Syntax.AgentsetType, obj)
    }
    try {
      java.util.Collections.sort(input, new MyComparator(context, task))
      LogoList.fromJava(input)
    }
    catch {
      case e: IllegalArgumentException if e.getMessage == Java7SoPicky =>
        throw new EngineException(
          context, this, "predicate is not a strictly-less-than or strictly-greater than relation")
      case e: WrappedLogoException => throw e.ex
    }
  }

  class MyComparator(context: Context, task: ReporterTask)
  extends java.util.Comparator[AnyRef] {
    def die(o: AnyRef) =
      throw new ArgumentTypeException(
        context, _sortby.this, 0, Syntax.BooleanType, o)
    override def compare(o1: AnyRef, o2: AnyRef) =
      try task.report(context, Array(o2, o1)) match {
            case b: java.lang.Boolean =>
              if(b.booleanValue) 1
              else task.report(context, Array(o1, o2)) match {
                case b: java.lang.Boolean =>
                  if(b.booleanValue) -1
                  else 0
                case o => die(o)
              }
            case o => die(o)
      }
      catch {
        case ex: LogoException =>
          throw new WrappedLogoException(ex)
      }
  }

  class WrappedLogoException(val ex: LogoException) extends RuntimeException

}
