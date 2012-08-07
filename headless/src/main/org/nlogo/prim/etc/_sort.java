// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public final strictfp class _sort
    extends Reporter
    implements Pure {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax
        (new int[]{Syntax.ListType() | Syntax.AgentsetType()},
            Syntax.ListType());
  }

  @Override
  public Object report(final Context context)
      throws LogoException {
    return report_1(context, args[0].report(context));
  }

  public Object report_1(final Context context, Object obj)
      throws LogoException {
    if (obj instanceof AgentSet) {
      return ((AgentSet) obj).toLogoList();
    } else if (obj instanceof LogoList) {
      LogoList input = (LogoList) obj;
      ArrayList<Double> numbers = new ArrayList<Double>();
      ArrayList<String> strings = new ArrayList<String>();
      ArrayList<Agent> agents = new ArrayList<Agent>();
      for (Iterator<Object> it = input.iterator(); it.hasNext();) {
        Object elt = it.next();
        if (elt instanceof Double) {
          numbers.add((Double) elt);
        } else if (elt instanceof String) {
          strings.add((String) elt);
        } else if (elt instanceof Agent) {
          agents.add((Agent) elt);
        }
      }
      if (!numbers.isEmpty()) {
        Collections.sort(numbers);
        return LogoList.fromJava(numbers);
      } else if (!strings.isEmpty()) {
        Collections.sort(strings);
        return LogoList.fromJava(strings);
      } else {
        Collections.sort(agents);
        return LogoList.fromJava(agents);
      }
    }
    throw new ArgumentTypeException
        (context, this, 0, Syntax.ListType() | Syntax.AgentsetType(), obj);
  }

  public LogoList report_2(Context context, AgentSet agents) {
    return agents.toLogoList();
  }

  public LogoList report_3(Context context, LogoList input) {
    ArrayList<Double> numbers = new ArrayList<Double>();
    ArrayList<String> strings = new ArrayList<String>();
    ArrayList<Agent> agents = new ArrayList<Agent>();
    for (Object elt : input) {
      if (elt instanceof Double) {
        numbers.add((Double) elt);
      } else if (elt instanceof String) {
        strings.add((String) elt);
      } else if (elt instanceof Agent) {
        agents.add((Agent) elt);
      }
    }
    if (!numbers.isEmpty()) {
      Collections.sort(numbers);
      return LogoList.fromJava(numbers);
    } else if (!strings.isEmpty()) {
      Collections.sort(strings);
      return LogoList.fromJava(strings);
    } else {
      Collections.sort(agents);
      return LogoList.fromJava(agents);
    }
  }
}
