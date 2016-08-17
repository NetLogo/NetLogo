package org.nlogo.prim.etc

import org.nlogo.api.Argument
import org.nlogo.core.{Syntax, Pure}
import org.nlogo.nvm.{Reporter, Context}

class _block extends Reporter with Pure {

  override def report(context: Context): AnyRef =
    argEvalCodeBlock(context, 0).map(_.text).mkString(" ")
}
