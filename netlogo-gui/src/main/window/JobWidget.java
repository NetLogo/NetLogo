// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.agent.Agent;

public abstract strictfp class JobWidget
    extends SingleErrorWidget
    implements
    org.nlogo.api.JobOwner,
    org.nlogo.window.Events.CompiledEvent.Handler {

  public JobWidget(org.nlogo.util.MersenneTwisterFast random) {
    this.random = random;
  }

  protected Class<? extends Agent> agentClass = null;

  public Class<? extends Agent> agentClass() {
    return agentClass;
  }

  public void agentClass(Class<? extends Agent> agentClass) {
    this.agentClass = agentClass;
  }

  public final org.nlogo.util.MersenneTwisterFast random;

  public org.nlogo.util.MersenneTwisterFast random() {
    return random;
  }

  public boolean useAgentClass() {
    // agent monitors will override to return false - ST 11/5/03
    return true;
  }

  protected org.nlogo.agent.AgentSet agents = null;

  public org.nlogo.agent.AgentSet agents() {
    return agents;
  }

  public void agents(org.nlogo.agent.AgentSet agents) {
    this.agents = agents;
  }

  private org.nlogo.nvm.Procedure procedure = null;

  public org.nlogo.nvm.Procedure procedure() {
    return procedure;
  }

  public void procedure(org.nlogo.nvm.Procedure procedure) {
    this.procedure = procedure;
  }

  ///

  public boolean ownsPrimaryJobs() {
    return true;  // override in subclasses
  }

  public boolean isCommandCenter() {
    return false;  // override in subclasses
  }

  ///

  public void handle(org.nlogo.window.Events.CompiledEvent e) {
    if (e.sourceOwner == this) {
      procedure(e.procedure);  // use setter method, so subclasses can catch
      error(e.error);
    }

    if (error() == null) {
      setForeground(java.awt.Color.BLACK);
      //setEnabled ( true ) ;
    } else {
      setForeground(java.awt.Color.RED);
      //setEnabled ( false ) ;
    }
  }

  ///

  @Override
  public void removeNotify() {
    // This is a little kludgy.  Normally removeNotify would run on the
    // event thread, but in an applet context, when the applet
    // shuts down, removeNotify can run on some other thread. But
    // actually this stuff doesn't need to happen in the applet,
    // so we can just skip it in that context. - ST 10/12/03, 10/16/03
    if (java.awt.EventQueue.isDispatchThread()) {
      new org.nlogo.window.Events.RemoveJobEvent(this).raise(this);
    }
    super.removeNotify();
  }

  @Override
  public int sourceOffset() {
    String source = headerSource();
    return source == null ? 0 : source.length();
  }

  ///

  protected String innerSource = "";

  public String innerSource() {
    return innerSource;
  }

  public void innerSource(String innerSource) {
    this.innerSource = innerSource;
  }

  protected String headerSource = "";

  public String headerSource() {
    return headerSource;
  }

  public void headerSource(String headerSource) {
    this.headerSource = headerSource;
  }

  protected String footerSource = "";

  public String footerSource() {
    return footerSource;
  }

  public void footerSource(String footerSource) {
    this.footerSource = footerSource;
  }

  private boolean suppressRecompiles;
  protected boolean recompilePending;

  public void suppressRecompiles(boolean suppressRecompiles) {
    this.suppressRecompiles = suppressRecompiles;
    if (!suppressRecompiles && recompilePending) {
      recompilePending = false;
      new org.nlogo.window.Events.CompileMoreSourceEvent(this)
          .raise(this);
    }
  }

  public void source(String headerSource, String innerSource, String footerSource) {
    this.headerSource = headerSource;
    this.innerSource = innerSource;
    this.footerSource = footerSource;
    if (suppressRecompiles) {
      recompilePending = true;
    } else {
      new org.nlogo.window.Events.CompileMoreSourceEvent(this)
          .raise(this);
    }
  }

  public String source() {
    StringBuilder b = new StringBuilder();
    b.append(headerSource());
    b.append(innerSource());
    b.append(footerSource());
    return b.toString();
  }

}
