// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.api.{ AnonymousReporter, LogoException }
import org.nlogo.core.{ LogoList, Syntax }
import org.nlogo.nvm.{ AnonymousProcedure, ArgumentTypeException, Context, Reporter, RuntimePrimitiveException }

import scala.collection.mutable.ListBuffer
import scala.math.Ordering

class _sortby extends Reporter {

  // see issue #172
  private val Java7SoPicky =
    "Comparison method violates its general contract!"

  override def report(context: Context): LogoList = {
    val task = argEvalAnonymousReporter(context, 0)
    if (task.syntax.minimum > 2)
      throw new RuntimePrimitiveException(
        context, this, AnonymousProcedure.missingInputs(task, 2))
    val obj = args(1).report(context)
    val input = obj match {
      case list: LogoList => list
      case agents: AgentSet =>
        val list = ListBuffer[AnyRef]()
        val it = agents.shufflerator(context.job.random)
        while (it.hasNext)
          list += it.next()
        list
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ListType | Syntax.AgentsetType, obj)
    }
    try {
      val sorted = input.sortBy(identity)(using new SortOrdering(context, task))
      LogoList.fromIterator(sorted.iterator)
    }
    catch {
      case e: IllegalArgumentException if e.getMessage == Java7SoPicky =>
        throw new RuntimePrimitiveException(
          context, this, "predicate is not a strictly-less-than or strictly-greater than relation")
      case e: WrappedLogoException => throw e.ex
    }
  }

  class SortOrdering(context: Context, task: AnonymousReporter) extends Ordering[AnyRef] {
    def die(o: AnyRef) =
      throw new ArgumentTypeException(
        context, _sortby.this, 0, Syntax.BooleanType, o)

    def compare(o1: AnyRef, o2: AnyRef): Int =
      try task.report(context, Array(o2, o1)) match {
            case b: java.lang.Boolean =>
              if (b.booleanValue) 1
              else task.report(context, Array(o1, o2)) match {
                case bool: java.lang.Boolean =>
                  if (bool.booleanValue) -1
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
