// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Shape;
import org.nlogo.api.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;

import java.util.List;

public final strictfp class _linkshapes
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.ListType());
  }

  @Override
  public Object report(Context context) {
    List<Shape> shapes = world.linkShapeList().getShapes();
    LogoListBuilder result = new LogoListBuilder();
    for (Shape shape : shapes) {
      result.add(shape.getName());
    }
    return result.toLogoList();
  }
}
