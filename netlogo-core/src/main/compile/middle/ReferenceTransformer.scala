// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.middle

import org.nlogo.core
import org.nlogo.core.Fail._
import org.nlogo.nvm.Referencer
import org.nlogo.compile.api.{ AstTransformer, ReporterApp, Statement }

class ReferenceTransformer extends AstTransformer {
  override def visitStatement(stmt: Statement): Statement = {
    val newStmt = super.visitStatement(stmt)
    newStmt.command match {
      case cmd: Referencer =>
        stmt.args(cmd.referenceIndex).asInstanceOf[ReporterApp].coreReporter match {
          case referenceable: core.Referenceable =>
            val args = stmt.args
            val newArgs = args.slice(0, cmd.referenceIndex).toSeq ++
              args.slice(cmd.referenceIndex + 1 min args.length, args.length)
            stmt.copy(command = cmd.applyReference(referenceable.makeReference), args = newArgs)
          case _ =>
            exception("Expected a patch variable here.", stmt.args(cmd.referenceIndex))
        }
      case cmd => newStmt
    }
  }
}
