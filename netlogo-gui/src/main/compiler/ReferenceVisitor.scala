// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.Syntax
import org.nlogo.prim._

private class ReferenceVisitor extends DefaultAstVisitor {
  override def visitStatement(stmt: Statement) {
    super.visitStatement(stmt)
    val index = stmt.command.syntax.right.indexWhere(_ == Syntax.ReferenceType)
    // at present the GIS extension is expecting its _reference arguments not to
    // be removed, so exempt _extern - ST 2/15/11
    if(index != -1 && !stmt.command.isInstanceOf[_extern]) {
      stmt.command.reference =
        stmt(index).asInstanceOf[ReporterApp].reporter.asInstanceOf[_reference].reference
      stmt.removeArgument(index)
    }
  }
}
