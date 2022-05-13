// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import java.util.List;

import org.nlogo.core.LogoList$;
import org.nlogo.core.Shape;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

import scala.collection.Iterator;

public final class _linkshapes
    extends Reporter {


  @Override
  public Object report(Context context) {
    Iterator<Shape> shapes = world.linkShapeList().shapes().iterator();
    LogoListBuilder b = new LogoListBuilder();
    while (shapes.hasNext()) {
      b.add(shapes.next().name());
    }
    return b.toLogoList();
  }
}
