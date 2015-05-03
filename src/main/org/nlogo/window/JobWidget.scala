// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, EventQueue }
import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.api.{ AgentKind, JobOwner }
import org.nlogo.nvm.Procedure
import org.nlogo.util.MersenneTwisterFast

abstract class JobWidget(val random: MersenneTwisterFast)
    extends SingleErrorWidget with JobOwner with Events.CompiledEventHandler {
  private var _kind: AgentKind = null
  def kind = _kind
  def kind_=(__kind: AgentKind) = _kind = __kind
  def useKind = true // agent monitors will override to return false - ST 11/5/03
  var agents: AgentSet = null
  private var _procedure: Procedure = null
  def procedure = _procedure
  def procedure_=(__procedure: Procedure) = _procedure = __procedure

  ///

  def ownsPrimaryJobs = true // override in subclasses
  def isCommandCenter = false // override in subclasses

  ///

  def handle(e: Events.CompiledEvent) = {
    if(e.sourceOwner == this) {
      procedure = e.procedure
      error(e.error)
    }

    if(error == null)
      setForeground(Color.BLACK)
      //setEnabled(true)
    else
      setForeground(Color.RED)
      //setEnabled(false)
  }

  ///

  override def removeNotify() = {
    // This is a little kludgy.  Normally removeNotify would run on the
    // event thread, but in an applet context, when the applet
    // shuts down, removeNotify can run on some other thread. But
    // actually this stuff doesn't need to happen in the applet,
    // so we can just skip it in that context. - ST 10/12/03, 10/16/03
    if(EventQueue.isDispatchThread) new Events.RemoveJobEvent(this).raise(this)
    super.removeNotify()
  }

  override def sourceOffset = if(headerSource == null) 0 else headerSource.length

  ///

  var headerSource = ""
  private var _innerSource = ""
  def innerSource = _innerSource
  def innerSource_=(__innerSource: String) = _innerSource = __innerSource
  var footerSource = ""

  private var _suppressRecompiles = false
  protected var recompilePending = false

  def suppressRecompiles_=(__suppressRecompiles: Boolean) = {
    _suppressRecompiles = __suppressRecompiles
    if(!_suppressRecompiles && recompilePending) {
      recompilePending = false
      new Events.CompileMoreSourceEvent(this).raise(this)
    }
  }

  def source(_headerSource: String, _innerSource: String, _footerSource: String) = {
    headerSource = _headerSource
    innerSource = _innerSource
    footerSource = _footerSource
    if(_suppressRecompiles)
      recompilePending = true
    else
      new Events.CompileMoreSourceEvent(this).raise(this)
  }

  def source = headerSource + innerSource + footerSource
}
