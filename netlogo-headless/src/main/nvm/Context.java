// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.api.MersenneTwisterFast;
import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.core.Let;
import org.nlogo.api.LogoException;

public final strictfp class Context implements org.nlogo.api.Context {

  // these are information about our execution environment
  public final Job job;
  public Agent myself; // non-final so we needn't always fill it in until requested
  public Agent agent;  // non-final to allow Contexts to be re-used within the same Job
  public int agentBit;

  // these are information about our current state
  public int ip;
  public Activation activation;
  public boolean waiting = false; // are we waiting on a child job?
  private Workspace workspace;
  private boolean inReporterProcedure = false;

  // Reverting to old way of initializing an empty List because
  // the Eclipse java compiler gets trumped by a simple List.empty()
  // and thinks the method call is ambiguous. NP 2012-11-28.
  @SuppressWarnings("unchecked") // Java doesn't know about variance
  public scala.collection.immutable.List<LetBinding> letBindings =
    (scala.collection.immutable.List<LetBinding>) ((Object) scala.collection.immutable.Nil$.MODULE$);

  /**
   * It is necessary for each Context to have its own stopping flag
   * in order to support the hack where if the last procedure call
   * in a forever button's code exits via "stop", then the forever button
   * stops too.  This is different from the stopping flag in Job,
   * which is used for when the user stops a forever button by clicking
   * on it.
   */
  public boolean stopping = false; // for stopping forever buttons

  public boolean finished = false;

  // This constructor is used when a Context spawns a Job which
  // in turn spawns Contexts, such as with _ask. - ST 6/12/06
  public Context(Job job, Agent agent, int ip,
                 Activation activation) {
    this.job = job;
    this.agent = agent;
    if (agent != null) {
      agentBit = agent.agentBit();
    }
    this.ip = ip;
    this.activation = activation;
  }

  // ...while these constructors are used when one Context spawns
  // another Context directly without an intervening Job, such as
  // with _with. - ST 6/12/06
  public Context(Context context, AgentSet agents) {
    job = context.job;
    activation = context.activation;
    letBindings = context.letBindings;
    myself = context.agent;
    agentBit = agents.agentBit();
  }

  public Context(Context context, Agent agent) {
    job = context.job;
    activation = context.activation;
    letBindings = context.letBindings;
    myself = context.agent;
    agentBit = agent.agentBit();
  }

  public boolean makeChildrenExclusive() {
    return inReporterProcedure || job.exclusive();
  }

  // this method runs only until a command switches
  void stepConcurrent() {
    if (agent.id() == -1) // is our agent dead?
    {
      finished = true;
      return;
    }
    Command command = null;
    try {
      do {
        command = activation.procedure().code()[ip];
        if ((agentBit & command.agentBits) == 0) {
          command.throwAgentClassException(this, agent.kind());
        }
        command.perform(this);
        if (command.world.comeUpForAir) {
          comeUpForAir(command);
        }
      } while (!command.switches && !finished);
    } catch (LogoException ex) {
      EngineException.rethrow(ex, this, command);
    } catch (StackOverflowError ex) {
      throw new EngineException
          (this, "stack overflow (recursion too deep)");
    }
  }

  // this method runs until the context is finished
  void runExclusive() {
    if (agent.id() == -1) // is our agent dead?
    {
      finished = true;
      return;
    }
    Command command = null;
    try {
      do {
        command = activation.procedure().code()[ip];
        if ((agentBit & command.agentBits) == 0) {
          command.throwAgentClassException(this, agent.kind());
        }
        command.perform(this);
        if (command.world.comeUpForAir) {
          comeUpForAir(command);
        }
      } while (!finished);
    } catch (LogoException ex) {
      EngineException.rethrow(ex, this, command);
    }
  }

  public boolean hasParentContext() {
    return job.parentContext != null;
  }

  public Agent myself() {
    // myself will be non-null if this Context was spawned
    // directly from another Context, e.g. _with - ST 6/12/06
    if (myself == null) {
      if (job.parentContext == null) {
        // let the caller figure out what to do about it - ST 4/16/13
        return null;
      }
      // ...but if this Context was spawned by a Job (e.g. _ask)
      // then (to save time) we won't have bothered to fill in
      // the myself field in this Context, so we go to the
      // parent context and fill it now that it has been
      // asked for . - ST 6/12/06
      myself = job.parentContext.agent;
    }
    return myself;
  }

  /**
   * used for determining whether we are inside an ask inside
   * the current procedure or not
   */
  public boolean atTopActivation() {
    return job.parentContext == null
        || activation != job.parentContext.activation;
  }

  public void runExclusiveJob(AgentSet agentset, int address) {
    new ExclusiveJob
        (job.owner, agentset, activation.procedure(), address, this, workspace, job.random)
        .run();
    // this next check is here to handle an obscure special case:
    // check if the child has (gasp!) killed its parent
    // - ST 6/27/05, 1/10/07
    if (agent.id() == -1) {
      finished = true;
    }
  }

  public Job makeConcurrentJob(AgentSet agentset) {
    return new ConcurrentJob(job.owner, agentset, null, ip + 1, this, workspace, job.random);
  }

  public void returnFromProcedure() {
    ip = activation.returnAddress();
    activation = activation.parent().nonEmpty() ? activation.parent().get() : null;
  }

  public void stop() {
    if (activation.procedure().isTask()) {
      throw NonLocalExit$.MODULE$;
    }
    if (activation.procedure().topLevel()) {
      // In the BehaviorSpace case, there are two cases: stop
      // used inside a procedure called from the go commands,
      // and stop used in the go commands themselves.  (People
      // probably shouldn't be doing the latter, since they
      // could just use a stop condition, but you know somebody
      // will try...)  If stop is used in the go commands
      // themselves, then the call to returnFromProcedure below
      // means that __experimentstepend won't run.  Thus we need
      // to set job.stopping to true ourselves, if we just
      // returned from a top level procedure.  If I've analyzed
      // this correctly, setting it to true will have no effect
      // in other settings besides BehaviorSpace.
      // - ST 3/8/06, 8/30/07
      job.stopping = true;
      finished = true;
    }
    returnFromProcedure();
    // this is so that we can stop our enclosing forever
    // button if we're immediately inside one.  __foreverbuttonend
    // will be looking for this to be true and if it is, will
    // stop the button.  this flag is reset to false by
    // _return and _report, so the next time a procedure
    // returns normally, the forever button will no longer
    // stop since "stop" wasn't used within a procedure called
    // directly by the button. - ST
    // It's also used to stop a BehaviorSpace run, using
    // __experimentstepend. - ST 3/8/06
    stopping = true;
  }

  ///

  // use only with Contexts created directly by other Contexts
  // (no intervening Job);
  // caller should call reporter.checkAgentClass() or
  // reporter.checkAgentSetClass() beforehand
  public Object evaluateReporter(Agent agent,
                                 Reporter reporter) {
    this.agent = agent;
    return reporter.report(this);
  }

  public Object callReporterProcedure(Activation newActivation) {
    boolean oldInReporterProcedure = inReporterProcedure;
    Command command = null;
    inReporterProcedure = true; // so use of "ask" will create an exclusive job
    activation = newActivation;
    ip = 0;
    try {
      do {
        command = activation.procedure().code()[ip];
        if ((agentBit & command.agentBits) == 0) {
          command.throwAgentClassException(this, agent.kind());
        }
        command.perform(this);
        if (command.world.comeUpForAir) {
          comeUpForAir(command);
        }
      }
      while (!finished && job.result == null);
    } catch (NonLocalExit$ e) {
      // do nothing
    } catch (LogoException ex) {
      EngineException.rethrow(ex, this, command);
    } finally {
      inReporterProcedure = oldInReporterProcedure;
    }
    ip = activation.returnAddress();
    activation = activation.parent().get();
    Object result = job.result;
    job.result = null;
    return result;
  }

  /// stuff for "let"

  public void let(Let let, Object value) {
    letBindings = letBindings.$colon$colon(new LetBinding(let, value));
  }

  // typecasts in getLet and setLet necessary because Java's type
  // system can't fully grok Scala collections stuff - ST 7/7/12,
  // 11/1/12, 9/25/13
  public Object getLet(Let let) {
    scala.collection.immutable.List<LetBinding> rest = letBindings;
    while ((Object) rest != scala.collection.immutable.Nil$.MODULE$)
    {
      LetBinding binding = rest.head();
      if (let == binding.let()) {
        return binding.value();
      }
      rest = ((scala.collection.immutable.$colon$colon<LetBinding>) rest).tail();
    }
    return job.parentContext.getLet(let);
  }
  public void setLet(Let let, Object value) {
    scala.collection.immutable.List<LetBinding> rest = letBindings;
    while ((Object) rest != scala.collection.immutable.Nil$.MODULE$)
    {
      LetBinding binding = rest.head();
      if (let == binding.let()) {
        binding.value_$eq(value);
        return;
      }
      rest = ((scala.collection.immutable.$colon$colon<LetBinding>) rest).tail();
    }
    job.parentContext.setLet(let, value);
  }

  public scala.collection.immutable.List<LetBinding> allLets() {
    // fast path
    if(job.parentContext == null) {
      return letBindings;
    }
    // slow path
    else {
      scala.collection.mutable.ListBuffer<LetBinding> buf =
        new scala.collection.mutable.ListBuffer<LetBinding>();
      Context walk = this;
      while(walk != null && activation == walk.activation) {
        buf.$plus$plus$eq(walk.letBindings);
        walk = walk.job.parentContext;
      }
      return buf.toList();
    }
  }

  ///

  // this had to be made public so that workspace.Evaluator could call it when
  // running command thunks. - JC 6/11/10
  public void runtimeError(Exception ex) {
    try {
      Instruction instruction = null;
      Context context = null;
      if (ex instanceof EngineException) {
        instruction = ((EngineException) ex).instruction();
        context = ((EngineException) ex).context();
      }
      if (instruction == null) {
        instruction = activation.procedure().code()[ip];
      }
      if (context == null) {
        context = this;
      }
      activation.procedure().code()[ip].workspace
          .runtimeError(job.owner, context, instruction, ex);
    } catch (RuntimeException ex2) {
      // well we tried to report the original exception to the user,
      // but a new exception happened. so we'll report the original
      // using plan B. - ST 8/29/07
      org.nlogo.api.Exceptions.handle(ex);
    }
  }

  public String buildRuntimeErrorMessage(Instruction instruction, Throwable throwable) {
    return buildRuntimeErrorMessage(instruction, throwable, null);
  }

  public String buildRuntimeErrorMessage(Instruction instruction, Throwable throwable, String message) {
    if(throwable instanceof EngineException &&
       ((EngineException) throwable).cachedRuntimeErrorMessage().isDefined()) {
      return ((EngineException) throwable).cachedRuntimeErrorMessage().get();
    }
    return StackTraceBuilder.build(
      activation, agent, instruction, scala.Option.apply(throwable), message);
  }

  /// coming up for air

  private void comeUpForAir(Command command)
      throws HaltException {
    if (command.switches && job.owner.ownsPrimaryJobs()) {
      command.workspace.breathe(this);
    }
    if (Thread.currentThread().isInterrupted()) {
      command.world.comeUpForAir = false;
      finished = true;
      throw new HaltException(true);
    }
  }

  // api.Context methods
  public Workspace workspace() {
    return workspace;
  }

  public Activation activation() {
    return activation;
  }

  public MersenneTwisterFast getRNG() {
    return job.random;
  }

  public String attachCurrentDirectory(String path)
      throws java.net.MalformedURLException {
    return workspace.fileManager().attachPrefix(path);
  }

  public void importPcolors(java.awt.image.BufferedImage image, boolean asNetLogoColors) {
    org.nlogo.agent.ImportPatchColors.doImport(image, workspace.world(), asNetLogoColors);
  }

  public java.awt.image.BufferedImage getDrawing() {
    return workspace.getAndCreateDrawing();
  }

  public org.nlogo.api.World world() {
    return workspace.world();
  }

  public org.nlogo.api.Agent getAgent() {
    return agent;
  }
}
