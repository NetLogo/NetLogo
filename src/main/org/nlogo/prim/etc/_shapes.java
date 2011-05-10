package org.nlogo.prim.etc;

import java.util.List;

import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Shape;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _shapes
    extends Reporter {
  @Override
  public Syntax syntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_LIST);
  }

  @Override
  public Object report(Context context) {
    List<Shape> shapes = world.turtleShapeList().getShapes();
    LogoListBuilder result = new LogoListBuilder();
    for (Shape shape : shapes) {
      result.add(shape.getName());
    }
    return result.toLogoList();
  }
}
