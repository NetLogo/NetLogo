package org.nlogo.prim;

import org.nlogo.core.{Syntax, Token}
import org.nlogo.nvm.{Context, Pure, Reporter}
import java.util.List
import scala.collection.JavaConverters._

class _constcodeblock(value: List[Token]) extends Reporter with Pure {
  def this(value: Seq[Token]) = this(value.asJava)

  override def toString =
    s"${super.toString}: [${value.asScala.map((t: Token) => t.text).mkString(" ")}]"

  override def report(ctx: Context) = value
}
