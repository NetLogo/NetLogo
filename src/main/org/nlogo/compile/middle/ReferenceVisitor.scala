// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ api, prim }

private class ReferenceVisitor extends DefaultAstVisitor {
  override def visitStatement(stmt: Statement) {
    super.visitStatement(stmt)
    val index = stmt.command.syntax.right.indexWhere(_ == api.Syntax.ReferenceType)
    // at present the GIS extension is expecting its _reference arguments not to
    // be removed, so exempt _extern - ST 2/15/11
    if(index != -1 && !stmt.command.isInstanceOf[prim._extern]) {
      stmt.command.reference =
        stmt(index).asInstanceOf[ReporterApp].reporter
          .asInstanceOf[prim._reference].reference
      stmt.removeArgument(index)
    }
  }
}
