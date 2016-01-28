package org.nlogo.prim;

import org.nlogo.api.{Syntax, Token}
import org.nlogo.nvm.{Context, Pure, Reporter}
import java.util.List
import scala.collection.JavaConverters._

class _constcodeblock(value: List[Token]) extends Reporter with Pure {
  override def syntax = Syntax.reporterSyntax(Syntax.CodeBlockType)
  override def toString = s"${super.toString}: [${value.asScala.map(_.name).mkString(" ")}]"
  override def report(ctx: Context) = value
}
