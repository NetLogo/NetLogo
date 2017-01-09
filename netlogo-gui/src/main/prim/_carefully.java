// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim;

import org.nlogo.agent.AgentSet;
import org.nlogo.core.Let;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Binding;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;

public final strictfp class _carefully
    extends Command
    implements org.nlogo.nvm.CustomAssembled {
  public Let let = null;

  public _carefully() { }

  public _carefully(Let let) {
    this.let = let;
  }

  @Override
  public String toString() {
    return super.toString() + ":+" + offset;
  }

  @Override
  public void perform(final Context context) {
    perform_1(context);
  }

  public void perform_1(final Context context) {
    AgentSet agentset = AgentSet.fromAgent(context.agent);
    Binding oldBinding = context.activation.binding.copy();
    try {
      // start new job that skips over the _goto command
      context.runExclusiveJob(agentset, next + 1);
      // move on to the _goto command, which will skip to the end.
      context.ip = next;
    } catch (LogoException ex) {
      context.activation.binding = oldBinding;
      context.activation.binding.let(let, ex);
      context.ip = offset; // jump to error handler
    }
  }

  public void assemble(org.nlogo.nvm.AssemblerAssistant a) {
    a.add(this);
    a.goTo();
    a.block(0);
    a.done();
    a.resume();
    a.block(1);
    a.comeFrom();
  }
}
