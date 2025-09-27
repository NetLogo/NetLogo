// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet

import java.lang.{ Long => JLong }

import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.{ CommandRunnable, Dump, HubNetInterface, PerspectiveJ, TypeNames }
import org.nlogo.core.{ AgentKind, LogoList, PlotPenInterface, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Command, Context, Instruction, Reporter, RuntimePrimitiveException }

import scala.collection.mutable.{ ArrayBuffer, HashMap }

trait HubNetPrim extends Instruction {
  protected lazy val hubNetManager: Option[HubNetInterface] = workspace.getHubNetManager
}

class _hubnetmessage extends Reporter with HubNetPrim {
  override def report(context: Context): AnyRef =
    hubNetManager.map(_.getMessage).get
}

class _hubnetmessagesource extends Reporter with HubNetPrim {
  override def report(context: Context): String =
    hubNetManager.map(_.getMessageSource).get
}

class _hubnetmessagetag extends Reporter with HubNetPrim {
  override def report(context: Context): String =
    hubNetManager.map(_.getMessageTag).get
}

class _hubnetmessagewaiting extends Reporter with HubNetPrim {
  override def report(context: Context): AnyRef =
    hubNetManager.map(_.messageWaiting.asInstanceOf[AnyRef]).get
}

class _hubnetentermessage extends Reporter with HubNetPrim {
  override def report(context: Context): AnyRef =
    hubNetManager.map(_.enterMessage.asInstanceOf[AnyRef]).get
}

class _hubnetexitmessage extends Reporter with HubNetPrim {
  override def report(context: Context): AnyRef =
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
  override def report(context: Context): AnyRef =
    hubNetManager.map(_.getInQueueSize.toDouble.asInstanceOf[AnyRef]).get
}

class _hubnetoutqsize extends Reporter with HubNetPrim {
  override def report(context: Context): AnyRef =
    hubNetManager.map(_.getOutQueueSize.toDouble.asInstanceOf[AnyRef]).get
}

class _hubnetcreateclient extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    hubNetManager.map(_.newClient(false, 0)).get
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

    val (ok, numConnected) = hubNetManager.map(_.waitForClients(numClients, timeout)).get

    if (!ok) {
      throw new RuntimePrimitiveException(context, this,
        "waited " + timeout + "ms for " + numClients +
                " clients, but only got " + numConnected)
    }

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

    val (ok, numReceived) = hubNetManager.map(_.waitForMessages(numMessages, timeout)).get

    if (!ok) {
      throw new RuntimePrimitiveException(context, this,
        "waited " + timeout + "ms for " + numMessages +
                " messages, but only got " + numReceived)
    }

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
    workspace.waitFor(new CommandRunnable {
      override def run(): Unit = {
        hubNetManager.foreach(_.reset())
      }
    })

    context.ip = next
  }
}

class _hubnetresetperspective extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val client = argEvalString(context, 0)
    val agent = world.observer.targetAgent
    val agentKind = Option(agent).map(_.kind).getOrElse(AgentKind.Observer)
    val id = Option(agent).map(_.id).getOrElse(0L)

    workspace.waitFor(new CommandRunnable {
      override def run(): Unit = {
        hubNetManager.foreach(_.sendAgentPerspective(
          client, world.observer.perspective.`export`,
          agentKind, id, ((world.worldWidth - 1).toDouble / 2), true))
      }
    })

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

    hubNetManager.foreach(_.broadcast(Dump.logoObject(data) + "\n"))

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

    workspace.waitFor(new CommandRunnable {
      override def run(): Unit = {
        hubNetManager.foreach(_.clearOverrideLists(client))
      }
    })

    context.ip = next
  }
}

class _hubnetclearoverride extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val client = argEvalString(context, 0)
    val target = args(1).report(context)
    val varName = argEvalString(context, 2)
    val set = target match {
      case agent: Agent => AgentSet.fromAgent(agent)
      case set: AgentSet => set
      case _ => throw new IllegalStateException
    }

    if (!hubNetManager.exists(_.isOverridable(set.kind, varName))) {
      throw new RuntimePrimitiveException(context, this,
        "you cannot override " + varName)
    }

    val overrides = new ArrayBuffer[JLong](set.count)
    val iter = set.iterator

    while (iter.hasNext)
      overrides += Long.box(iter.next().id)

    workspace.waitFor(new CommandRunnable {
      override def run(): Unit = {
        hubNetManager.foreach(_.clearOverride(
          client, set.kind, varName, overrides.toSeq))
      }
    })

    context.ip = next
  }
}

class _hubnetclearplot extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.clearPlot(name))
      }
    })

    context.ip = next;
  }
}

class _hubnetmakeplotnarrowcast extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        if (!hubNetManager.exists(_.addNarrowcastPlot(name))) {
          throw new RuntimePrimitiveException(context, _hubnetmakeplotnarrowcast.this,
                                              "no such plot: \"" + name + "\"")
        }
      }
    })

    context.ip = next;
  }
}

class _hubnetplot extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)
    val n = argEvalDoubleValue(context, 1)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.plot(name, n))
      }
    })

    context.ip = next;
  }
}

class _hubnetplotpendown extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.plotPenDown(name, true))
      }
    })

    context.ip = next;
  }
}

class _hubnetplotpenup extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.plotPenDown(name, false))
      }
    })

    context.ip = next;
  }
}

class _hubnetplotxy extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)
    val x = argEvalDoubleValue(context, 1)
    val y = argEvalDoubleValue(context, 2)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.plot(name, x, y))
      }
    })

    context.ip = next;
  }
}

class _hubnetsend extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val nodesArg = args(0).report(context)
    val tag = argEvalString(context, 1)
    val message = args(2).report(context)
    val hubnetManager = hubNetManager.get
    val nodes = new ArrayBuffer[String]

    nodesArg match {
      case list: LogoList =>
        for (node <- list.scalaIterator) {
          node match {
            case s: String =>
              nodes += s

            case _ =>
              throw new RuntimePrimitiveException(
                context, this,
                "HUBNET-SEND expected " + TypeNames.aName(Syntax.StringType | Syntax.ListType)
                + " of strings as the first input, but one item is the "
                + TypeNames.name(node) + " " + Dump.logoObject(node) + " instead")
          }
        }

      case s: String =>
        nodes += s

      case _ =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ListType | Syntax.StringType, nodesArg)
    }

    message match {
      case m: Serializable => hubnetManager.send(nodes.toSeq, tag, m)
      case _ =>
        throw new RuntimePrimitiveException(
          context, this,
          s"""|HUBNET-SEND is unable to send the message $message
              |of type ${TypeNames.name(message)} because it could not be
              |transmitted over the network""".stripMargin.linesIterator.mkString(" "))
    }

    context.ip = next
  }
}

class _hubnetsendclearoutput extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val clients = args(0).report(context)

    val nodes: Seq[String] = clients match {
      case list: LogoList =>
        list.collect {
          case str: String =>
            str

          case item =>
            throw new RuntimePrimitiveException
              (context, this, "HUBNET-SEND expected "
                + TypeNames.aName(Syntax.StringType | Syntax.ListType)
                + " of strings as the first input, but one item is the "
                + TypeNames.name(item) + " " +
                Dump.logoObject(item)
                + " instead")
        }

      case str: String =>
        Seq(str)

      case _ =>
        throw new ArgumentTypeException(context, this, 0, Syntax.ListType | Syntax.StringType, clients)

    }

    hubNetManager.foreach(_.clearText(nodes))

    context.ip = next
  }
}

class _hubnetsendfollow extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val client = argEvalString(context, 0)
    val agent = argEvalAgent(context, 1)
    val radius = argEvalDoubleValue(context, 2)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.sendAgentPerspective(client, PerspectiveJ.FOLLOW, agent.kind, agent.id, radius, false))
      }
    })

    context.ip = next
  }
}

class _hubnetsendmessage extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val clients = args(0).report(context)
    val data = args(1).report(context)

    val nodes: Seq[String] = clients match {
      case list: LogoList =>
        list.collect {
          case str: String =>
            str

          case item =>
            throw new RuntimePrimitiveException
              (context, this, "HUBNET-SEND expected "
                + TypeNames.aName(Syntax.StringType | Syntax.ListType)
                + " of strings as the first input, but one item is the "
                + TypeNames.name(item) + " " +
                Dump.logoObject(item)
                + " instead")
        }

      case str: String =>
        Seq(str)

      case _ =>
        throw new ArgumentTypeException(context, this, 0, Syntax.ListType | Syntax.StringType, clients)
    }

    hubNetManager.foreach(_.sendText(nodes, Dump.logoObject(data) + "\n"))

    context.ip = next
  }
}

class _hubnetsendoverride extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val client = argEvalString(context, 0)
    val target = args(1).report(context)
    val varName = argEvalString(context, 2)

    val set = target match {
      case a: Agent => AgentSet.fromAgent(a)
      case as: AgentSet => as
      case _ => throw new IllegalStateException("cant happen...")
    }

    if (!hubNetManager.exists(_.isOverridable(set.kind, varName)))
      throw new RuntimePrimitiveException(context, this, "you cannot override " + varName)

    val freshContext = new Context(context, set)

    args(3).checkAgentSetClass(set, context)

    // ugh..set.iterator is not a real iterator.
    val overrides = new HashMap[JLong, AnyRef]
    val it = set.iterator

    while (it.hasNext) {
      val agent = it.next()

      overrides(agent.id) = {
        val value = freshContext.evaluateReporter(agent, args(3))
        // gross to special case this, and not even clear where to put the special-case
        // code, but I guess it'll have to do until this all gets redone someday - ST 2/7/12
        if (varName.equalsIgnoreCase("LABEL") || varName.equalsIgnoreCase("PLABEL")) {
          Dump.logoObject(value)
        } else {
          value
        }
      }
    }

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.sendOverrideList(client, set.kind, varName, overrides.toMap))
      }
    })

    context.ip = next
  }
}

class _hubnetsendusermessage extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val clients = args(0).report(context)
    val data = args(1).report(context)

    val nodes: Seq[String] = clients match {
      case list: LogoList =>
        list.collect {
          case str: String =>
            str

          case item =>
            throw new RuntimePrimitiveException
              (context, this, "HUBNET-SEND expected "
                + TypeNames.aName(Syntax.StringType | Syntax.ListType)
                + " of strings as the first input, but one item is the "
                + TypeNames.name(item) + " " +
                Dump.logoObject(item)
                + " instead")
        }

      case str: String =>
        Seq(str)

      case _ =>
        throw new ArgumentTypeException(context, this, 0, Syntax.ListType | Syntax.StringType, clients)
    }

    hubNetManager.foreach(_.sendUserMessage(nodes, Dump.logoObject(data)))

    context.ip = next
  }
}

class _hubnetsendwatch extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val client = argEvalString(context, 0)
    val agent = argEvalAgent(context, 1)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.sendAgentPerspective(client, PerspectiveJ.WATCH, agent.kind, agent.id,
                                                     ((world.worldWidth - 1) / 2), false))
      }
    })

    context.ip = next
  }
}

class _hubnetsethistogramnumbars extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)
    val num = argEvalIntValue(context, 1)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.setHistogramNumBars(name, num))
      }
    })

    context.ip = next
  }
}

class _hubnetsetplotpeninterval extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)
    val interval = argEvalDoubleValue(context, 1)

    workspace.waitFor(new CommandRunnable {
      def run(): Unit = {
        hubNetManager.foreach(_.setPlotPenInterval(name, interval))
      }
    })

    context.ip = next
  }
}

class _hubnetsetplotpenmode extends Command with HubNetPrim {
  override def perform(context: Context): Unit = {
    val name = argEvalString(context, 0)
    val mode = argEvalIntValue(context, 1)
    workspace.waitFor(new CommandRunnable {
      override def run(): Unit = {
        if (mode < PlotPenInterface.MinMode || mode > PlotPenInterface.MaxMode) {
          throw new RuntimePrimitiveException(
            context, _hubnetsetplotpenmode.this,
            mode.toString + " is not a valid plot pen mode (valid modes are 0, 1, and 2)")
        }

        hubNetManager.foreach(_.setPlotPenMode(name, mode))
      }
    })

    context.ip = next
  }
}
