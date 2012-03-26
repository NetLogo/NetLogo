// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.hubnet;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;
import org.nlogo.api.TypeNames;
import org.nlogo.nvm.EngineException;

import java.util.Iterator;

import static scala.collection.JavaConversions.asScalaBuffer;

public final strictfp class _hubnetsendclearoutput
    extends org.nlogo.nvm.Command {
  @Override
  public void perform(final org.nlogo.nvm.Context context) throws LogoException {
    Object clients = args[0].report(context);

    java.util.List<String> nodes = new java.util.ArrayList<String>();
    if (clients instanceof LogoList) {
      for (Iterator<Object> nodesIter = ((LogoList) clients).iterator();
           nodesIter.hasNext();) {
        Object node = nodesIter.next();
        if (!(node instanceof String)) {
          throw new EngineException
              (context, this, "HUBNET-SEND expected "
                  + TypeNames.aName(Syntax.StringType() | Syntax.ListType())
                  + " of strings as the first input, but one item is the "
                  + TypeNames.name(node) + " " +
                  Dump.logoObject(node)
                  + " instead");
        }
        nodes.add((String) node);
      }
    } else if (clients instanceof String) {
      nodes.add((String) clients);
    } else {
      throw new org.nlogo.nvm.ArgumentTypeException
          (context, this, 0, Syntax.ListType() | Syntax.StringType(), clients);
    }

    workspace.getHubNetManager().clearText(asScalaBuffer(nodes));
    context.ip = next;
  }

  @Override
  public Syntax syntax() {
    int[] right = {Syntax.StringType() | Syntax.ListType()};
    return Syntax.commandSyntax(right);
  }
}
