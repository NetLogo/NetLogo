package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.api.{ LogoException, LogoList, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException , Context , EngineException, Reporter , ReporterLambda  }

class _sortby extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.ReporterTaskType,
                                Syntax.ListType | Syntax.AgentsetType),
                          Syntax.ListType, "OTPL", "?")

  override def report(context: Context) = {
    val lambda = argEvalReporterLambda(context, 0)
    if(lambda.formals.size > 2)
      throw new EngineException(
        context, this, lambda.missingInputs(2))
    val obj = args(1).report(context)
    val input = obj match {
      case list: LogoList =>
        // must copy the list, because Collections.sort() works in place - ST 7/31/04, 1/12/06
        new java.util.ArrayList[AnyRef](list)
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
      java.util.Collections.sort(input, new MyComparator(context, lambda))
      LogoList.fromJava(input)
    }
    catch {
      case e: WrappedLogoException => throw e.ex
    }
  }

  class MyComparator(context: Context, lambda: ReporterLambda)
  extends java.util.Comparator[AnyRef] {
    def die(o: AnyRef) =
      throw new ArgumentTypeException(
        context, _sortby.this, 0, Syntax.BooleanType, o)
    override def compare(o1: AnyRef, o2: AnyRef) =
      try lambda.report(context, Array(o1, o2)) match {
            case b: java.lang.Boolean =>
              if(b.booleanValue) -1
              else lambda.report(context, Array(o2, o1)) match {
                case b: java.lang.Boolean =>
                  if(b.booleanValue) 1
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
