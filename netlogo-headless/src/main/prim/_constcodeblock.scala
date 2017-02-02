package org.nlogo.prim;

import org.nlogo.core.{Syntax, Pure, Token}
import org.nlogo.nvm.{Context, Reporter}
import scala.collection.JavaConverters._
import java.util.{ List => JList }

class _constcodeblock(value: JList[Token]) extends Reporter with Pure {
  def this(value: Seq[Token]) = this(value.asJava)

  override def toString =
    s"${super.toString}: [${value.asScala.map((t: Token) => t.text).mkString(" ")}]"

  override def report(ctx: Context) = value
}
