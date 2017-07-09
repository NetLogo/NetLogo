package org.nlogo.prim.etc

import scala.collection.JavaConverters._
import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _block extends Reporter with Pure {

  override def report(context: Context): AnyRef =
    argEvalCodeBlock(context, 0).asScala.map(_.text).mkString(" ")
}
