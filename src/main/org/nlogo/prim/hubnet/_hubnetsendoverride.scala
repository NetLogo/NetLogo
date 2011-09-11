package org.nlogo.prim.hubnet

import org.nlogo.agent.{ ArrayAgentSet, Agent, AgentSet }
import org.nlogo.api.{ CommandRunnable, Syntax }
import org.nlogo.nvm.{ Command, Context, EngineException }

class _hubnetsendoverride extends Command {

  override def syntax = Syntax.commandSyntax(
    Array(Syntax.StringType, Syntax.AgentsetType | Syntax.AgentType,
          Syntax.StringType,
          Syntax.ReporterBlockType),
    "OTPL", "?", false)

  override def perform(context: Context) {
    val client = argEvalString(context, 0)
    val target = args(1).report(context)
    val varName = argEvalString(context, 2)

    val set = target match {
      case a: Agent =>
        val aas = new ArrayAgentSet(a.getAgentClass(), 1, false, world)
        aas.add(a)
        aas
      case as: AgentSet => as
      case _ => throw new IllegalStateException("cant happen...")
    }

    if(org.nlogo.hubnet.mirroring.OverrideList.getOverrideIndex(
        org.nlogo.hubnet.mirroring.Agent.AgentType.fromAgentClass(set.`type`),
        varName) == -1)
      throw new EngineException(context, this,
        "you cannot override " + varName)
    
    val freshContext = new Context(context, set)
    args(3).checkAgentSetClass(set, context)

    // ugh..set.iterator is not a real iterator.
    val overrides = new collection.mutable.HashMap[java.lang.Long,AnyRef]()
    val it = set.iterator
    while(it.hasNext) {
      val agent = it.next
      overrides(agent.id) = freshContext.evaluateReporter(agent, args(3))
    }

    workspace.waitFor(new CommandRunnable() {
      def run() { workspace.getHubNetManager.sendOverrideList(client, set.`type`(), varName, overrides.toMap) }
    })
    context.ip = next
  }

}
