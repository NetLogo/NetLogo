package org.nlogo.prim.hubnet

import org.nlogo.api.{CommandRunnable, LogoList}
import org.nlogo.nvm.{EngineException, Context, Reporter, Syntax}
import Syntax._

class _hubnetmessage extends Reporter {
  override def syntax = reporterSyntax(TYPE_WILDCARD)
  override def report(context: Context) =
    workspace.getHubNetManager.getMessage
}

class _hubnetmessagesource extends Reporter {
  override def syntax = reporterSyntax(TYPE_STRING)
  override def report(context: Context) =
    workspace.getHubNetManager.getMessageSource
}

class _hubnetmessagetag extends Reporter {
  override def syntax = reporterSyntax(TYPE_STRING)
  override def report(context: Context) =
    workspace.getHubNetManager.getMessageTag
}

class _hubnetmessagewaiting extends Reporter {
  override def syntax = reporterSyntax(TYPE_BOOLEAN)
  override def report(context: Context) =
    workspace.getHubNetManager.messageWaiting.asInstanceOf[AnyRef]
}

class _hubnetentermessage extends Reporter {
  override def syntax = reporterSyntax(TYPE_BOOLEAN)
  override def report(context: Context) =
    workspace.getHubNetManager.enterMessage.asInstanceOf[AnyRef]
}

class _hubnetexitmessage extends Reporter {
  override def syntax = reporterSyntax(TYPE_BOOLEAN)
  override def report(context: Context) =
    workspace.getHubNetManager.exitMessage.asInstanceOf[AnyRef]
}

class _hubnetclientslist extends Reporter {
  override def syntax = reporterSyntax(TYPE_LIST)
  override def report(context: Context): AnyRef =
    LogoList(workspace.getHubNetManager.clients.toSeq.map(_.asInstanceOf[AnyRef]): _*)
}

class _hubnetkickclient extends org.nlogo.nvm.Command {
  override def syntax = commandSyntax(Array(TYPE_STRING), "OTPL")
  override def perform(context: Context) {
    workspace.getHubNetManager.kick(argEvalString(context, 0))
    context.ip = next
  }
}

class _hubnetkickallclients extends org.nlogo.nvm.Command {
  override def syntax = commandSyntax("OTPL", true)
  override def perform(context: Context) {
    workspace.getHubNetManager.kickAll()
    context.ip = next
  }
}

class _hubnetinqsize extends Reporter {
  override def syntax = reporterSyntax(TYPE_NUMBER)
  override def report(context: Context) =
    workspace.getHubNetManager.getInQueueSize.toDouble.asInstanceOf[AnyRef]
}

class _hubnetoutqsize extends Reporter {
  override def syntax = reporterSyntax(TYPE_NUMBER)
  override def report(context: Context) =
    workspace.getHubNetManager.getOutQueueSize.toDouble.asInstanceOf[AnyRef]
}

class _hubnetcreateclient extends org.nlogo.nvm.Command {
  override def syntax = commandSyntax("O---", false)
  override def perform(context: Context) {
    workspace.getHubNetManager.newClient(false,0)
    context.ip = next
  }
}

class _hubnetsendfromlocalclient extends org.nlogo.nvm.Command {
  override def syntax = commandSyntax(Array(TYPE_STRING, TYPE_STRING, TYPE_WILDCARD))
  override def perform(context: Context) {
    val clientId = argEvalString(context, 0)
    val messageTag = argEvalString(context, 1)
    val payload = args(2).report(context)
    // todo: check if we got an error back here!
    workspace.getHubNetManager.sendFromLocalClient(clientId, messageTag, payload)
    context.ip = next
  }
}

class _hubnetwaitforclients extends org.nlogo.nvm.Command {
  // two args:
  //   1) number of clients to wait for.
  //   2) timeout (milliseconds)
  override def syntax = commandSyntax(Array(TYPE_NUMBER, TYPE_NUMBER))
  override def perform(context: Context) {
    val numClients = argEvalDoubleValue(context, 0).toInt
    val timeout = argEvalDoubleValue(context, 1).toLong
    val (ok, numConnected) =
      workspace.getHubNetManager.waitForClients(numClients, timeout)
    if(! ok)
      throw new EngineException(context, this,
        "waited " + timeout + "ms for " + numClients +
                " clients, but only got " + numConnected)
    context.ip = next
  }
}

class _hubnetwaitformessages extends org.nlogo.nvm.Command {
  // two args:
  //   1) number of messages to wait for.
  //   2) timeout (milliseconds)
  override def syntax = commandSyntax(Array(TYPE_NUMBER, TYPE_NUMBER))
  override def perform(context: Context) {
    val numMessages = argEvalDoubleValue(context, 0).toInt
    val timeout = argEvalDoubleValue(context, 1).toLong
    val (ok, numReceived) =
      workspace.getHubNetManager.waitForMessages(numMessages, timeout)
    if(! ok)
      throw new EngineException(context, this,
        "waited " + timeout + "ms for " + numMessages +
                " messages, but only got " + numReceived)
    context.ip = next
  }
}

class _hubnetsetviewmirroring extends org.nlogo.nvm.Command {
  override def syntax = commandSyntax(Array(TYPE_BOOLEAN))
  override def perform(context: Context) {
    workspace.getHubNetManager.setViewMirroring(argEvalBooleanValue(context, 0))
    context.ip = next
  }
}

class _hubnetsetplotmirroring extends org.nlogo.nvm.Command {
  override def syntax = commandSyntax(Array(TYPE_BOOLEAN))
  override def perform(context: Context) {
    workspace.getHubNetManager.setPlotMirroring(argEvalBooleanValue(context, 0))
    context.ip = next
  }
}

class _hubnetsetclientinterface extends org.nlogo.nvm.Command {
  def syntax = commandSyntax(Array[Int](TYPE_STRING, TYPE_LIST), "O---", false)
  def perform(context: Context) {
    val interfaceType = argEvalString(context, 0)
    val interfaceInfo = argEvalList(context, 1)
    workspace.waitFor(new CommandRunnable {
      override def run() {
        workspace.getHubNetManager.setClientInterface(interfaceType, interfaceInfo.toIterable)
      }
    })
    context.ip = next
  }
}
