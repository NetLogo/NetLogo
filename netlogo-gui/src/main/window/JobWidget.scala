// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.AgentSet
import org.nlogo.api.{ JobOwner, MersenneTwisterFast }
import org.nlogo.core.AgentKind
import org.nlogo.nvm.Procedure
import org.nlogo.window.Events.{ CompiledEvent, CompileMoreSourceEvent, RemoveJobEvent }
import java.awt.EventQueue

abstract class JobWidget(val random: MersenneTwisterFast)
  extends SingleErrorWidget with JobOwner with CompiledEvent.Handler {

  private var suppressRecompiles: Boolean = false

  private var recompilePending: Boolean = false

  var agentKind: AgentKind = null

  var _procedure: Procedure = null

  def procedure: Procedure = _procedure

  def procedure_=(procedure: Procedure) =
    _procedure = procedure

  var agents: AgentSet = null

  def kind: AgentKind = agentKind

  def agentKind(agentKind: AgentKind): Unit = {
    this.agentKind = agentKind
  }

  // agent monitors will override to return false - ST 11/5/03
  def useAgentClass: Boolean = true

  def agents(as: AgentSet): Unit = {
    this.agents = as
  }

  // override in subclasses
  def ownsPrimaryJobs: Boolean = true

  // override in subclasses
  def isCommandCenter: Boolean = false;

  ///

  def handle(e: CompiledEvent): Unit = {
    if (e.sourceOwner eq this) {
      // use setter method, so subclasses can catch
      procedure = e.procedure
      error(e.error)
    }

    if (error().isEmpty) {
      setForeground(java.awt.Color.BLACK)
    } else {
      setForeground(java.awt.Color.RED)
    }
  }

  ///

  override def removeNotify(): Unit = {
    // This is a little kludgy.  Normally removeNotify would run on the
    // event thread, but in an applet context, when the applet
    // shuts down, removeNotify can run on some other thread. But
    // actually this stuff doesn't need to happen in the applet,
    // so we can just skip it in that context. - ST 10/12/03, 10/16/03
    if (EventQueue.isDispatchThread()) {
      new RemoveJobEvent(this).raise(this)
    }
    super.removeNotify()
  }

  override def sourceOffset: Int =
    Option(headerSource).map(_.length).getOrElse(0)

  ///

  var _innerSource = ""

  def innerSource: String = _innerSource

  def innerSource_=(s: String): Unit = {
    _innerSource = s
  }

  var headerSource = ""

  def headerSource(headerSource: String): Unit = {
    this.headerSource = headerSource
  }

  var footerSource = ""

  def footerSource(footerSource: String): Unit = {
    this.footerSource = footerSource
  }

  def recompilePending(recompile: Boolean): Unit = {
    recompilePending = recompile
  }

  def suppressRecompiles(suppressRecompiles: Boolean): Unit = {
    this.suppressRecompiles = suppressRecompiles
    if (!suppressRecompiles && recompilePending) {
      recompilePending = false
      new CompileMoreSourceEvent(this).raise(this)
    }
  }

  def source(headerSource: String, innerSource: String, footerSource: String): Unit = {
    this.headerSource = headerSource
    this.innerSource = innerSource
    this.footerSource = footerSource
    if (suppressRecompiles) {
      recompilePending = true
    } else {
      new CompileMoreSourceEvent(this).raise(this)
    }
  }

  def source: String =
    headerSource + innerSource + footerSource
}
