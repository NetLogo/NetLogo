// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet

import org.nlogo.api.{ CommandRunnable, Dump, HubNetInterface }
import org.nlogo.core.{ AgentKind, LogoList }
import org.nlogo.nvm.{ Command, Context, Instruction, Reporter, RuntimePrimitiveException }

trait HubNetPrim extends Instruction {
  def hubNetManager: Option[HubNetInterface] = workspace.getComponent(classOf[HubNetInterface])
}

// these are for java interop, scala classes can just mix-in the trait
abstract class HubNetCommand extends Command with HubNetPrim
abstract class HubNetReporter extends Reporter with HubNetPrim

class _hubnetmessage extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.getMessage).get
}

class _hubnetmessagesource extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.getMessageSource).get
}

class _hubnetmessagetag extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.getMessageTag).get
}

class _hubnetmessagewaiting extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.messageWaiting.asInstanceOf[AnyRef]).get
}

class _hubnetentermessage extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.enterMessage.asInstanceOf[AnyRef]).get
}

class _hubnetexitmessage extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.exitMessage.asInstanceOf[AnyRef]).get
}

class _hubnetclientslist extends Reporter with HubNetPrim {

  override def report(context: Context): AnyRef =
    LogoList(hubNetManager.map(_.clients.toSeq.map(_.asInstanceOf[AnyRef])).get*)
}

class _hubnetkickclient extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    hubNetManager.foreach(_.kick(argEvalString(context, 0)))
    context.ip = next
  }
}

class _hubnetkickallclients extends Command with HubNetPrim {
  switches = true



  override def perform(context: Context): Unit = {
    hubNetManager.foreach(_.kickAll())
    context.ip = next
  }
}

class _hubnetinqsize extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.getInQueueSize.toDouble.asInstanceOf[AnyRef]).get
}

class _hubnetoutqsize extends Reporter with HubNetPrim {

  override def report(context: Context) =
    hubNetManager.map(_.getOutQueueSize.toDouble.asInstanceOf[AnyRef]).get
}

class _hubnetcreateclient extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    hubNetManager.map(_.newClient(false,0)).get
    context.ip = next
  }
}

class _hubnetsendfromlocalclient extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    val clientId = argEvalString(context, 0)
    val messageTag = argEvalString(context, 1)
    val payload = args(2).report(context)
    // todo: check if we got an error back here!
    hubNetManager.foreach(_.sendFromLocalClient(clientId, messageTag, payload))
    context.ip = next
  }
}

class _hubnetwaitforclients extends Command with HubNetPrim {
  // two args:
  //   1) number of clients to wait for.
  //   2) timeout (milliseconds)

  override def perform(context: Context): Unit = {
    val numClients = argEvalDoubleValue(context, 0).toInt
    val timeout = argEvalDoubleValue(context, 1).toLong
    val (ok, numConnected) =
      hubNetManager.map(_.waitForClients(numClients, timeout)).get
    if(! ok)
      throw new RuntimePrimitiveException(context, this,
        "waited " + timeout + "ms for " + numClients +
                " clients, but only got " + numConnected)
    context.ip = next
  }
}

class _hubnetwaitformessages extends Command with HubNetPrim {
  // two args:
  //   1) number of messages to wait for.
  //   2) timeout (milliseconds)

  override def perform(context: Context): Unit = {
    val numMessages = argEvalDoubleValue(context, 0).toInt
    val timeout = argEvalDoubleValue(context, 1).toLong
    val (ok, numReceived) =
      hubNetManager.map(_.waitForMessages(numMessages, timeout)).get
    if(! ok)
      throw new RuntimePrimitiveException(context, this,
        "waited " + timeout + "ms for " + numMessages +
                " messages, but only got " + numReceived)
    context.ip = next
  }
}

class _hubnetsetviewmirroring extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    hubNetManager.foreach(_.setViewMirroring(argEvalBooleanValue(context, 0)))
    context.ip = next
  }
}

class _hubnetsetplotmirroring extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    hubNetManager.foreach(_.setPlotMirroring(argEvalBooleanValue(context, 0)))
    context.ip = next
  }
}

class _hubnetfetchmessage extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    hubNetManager.foreach(_.fetchMessage())
    context.ip = next
  }
}

class _hubnetreset extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    workspace.waitFor(
      new CommandRunnable {
        override def run(): Unit = {
          hubNetManager.foreach(_.reset())
        }})
    context.ip = next
  }
}

class _hubnetresetperspective extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    val client = argEvalString(context, 0)
    val agent = world.observer.targetAgent
    val agentKind =
      Option(agent).map(_.kind).getOrElse(AgentKind.Observer)
    val id = Option(agent).map(_.id).getOrElse(0L)
    workspace.waitFor(
      new CommandRunnable {
        override def run(): Unit = {
          hubNetManager.foreach(_.sendAgentPerspective(
            client, world.observer.perspective.`export`,
            agentKind, id, ((world.worldWidth - 1).toDouble / 2), true))
        }})
    context.ip = next
  }
}

class _hubnetbroadcast extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    val variableName = argEvalString(context, 0)
    val data = args(1).report(context)
    hubNetManager.foreach(_.broadcast(variableName, data))
    context.ip = next
  }
}

class _hubnetbroadcastclearoutput extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    hubNetManager.foreach(_.broadcastClearText())
    context.ip = next
  }
}

class _hubnetbroadcastmessage extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    val data = args(0).report(context)
    hubNetManager.foreach(_.broadcast(Dump.logoObject(data)))
    context.ip = next
  }
}

class _hubnetbroadcastusermessage extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    val data = args(0).report(context)
    hubNetManager.foreach(_.broadcastUserMessage(Dump.logoObject(data)))
    context.ip = next
  }
}

class _hubnetroboclient extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    hubNetManager.foreach(_.newClient(true, argEvalIntValue(context, 0)))
    context.ip = next
  }
}

class _hubnetclearoverrides extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    val client = argEvalString(context, 0)
    workspace.waitFor(
      new CommandRunnable {
        override def run(): Unit = {
          hubNetManager.foreach(_.clearOverrideLists(client))
        }})
    context.ip = next
  }
}

class _hubnetclearoverride extends Command with HubNetPrim {

  override def perform(context: Context): Unit = {
    import org.nlogo.agent.{ Agent, AgentSet }
    val client = argEvalString(context, 0)
    val target = args(1).report(context)
    val varName = argEvalString(context, 2)
    val set = target match {
      case agent: Agent => AgentSet.fromAgent(agent)
      case set: AgentSet => set
      case _ => throw new IllegalStateException
    }
    if(!hubNetManager.exists(_.isOverridable(set.kind, varName)))
      throw new RuntimePrimitiveException(context, this,
        "you cannot override " + varName)
    val overrides = new collection.mutable.ArrayBuffer[java.lang.Long](set.count)
    val iter = set.iterator
    while(iter.hasNext)
      overrides += Long.box(iter.next().id)
    workspace.waitFor(
      new CommandRunnable() {
        override def run(): Unit = {
          hubNetManager.foreach(_.clearOverride(
            client, set.kind, varName, overrides.toSeq))}})
    context.ip = next
  }
}
