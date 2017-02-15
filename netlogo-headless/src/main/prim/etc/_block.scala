package org.nlogo.prim.etc

import org.nlogo.api.Argument
import org.nlogo.core.{Syntax, Pure}
import org.nlogo.nvm.{Reporter, Context}
import scala.collection.JavaConverters._

class _block extends Reporter with Pure {

  override def report(context: Context): AnyRef =
    argEvalCodeBlock(context, 0).asScala.map(_.text).mkString(" ")
}
