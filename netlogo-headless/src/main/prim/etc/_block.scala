package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

import scala.jdk.CollectionConverters.ListHasAsScala

class _block extends Reporter with Pure {

  override def report(context: Context): AnyRef =
    argEvalCodeBlock(context, 0).asScala.map(_.text).mkString(" ")
}
