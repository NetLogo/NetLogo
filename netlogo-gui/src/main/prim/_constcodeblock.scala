package org.nlogo.prim;

import java.util.{ List => JList }

import org.nlogo.core.{ Pure, Token }
import org.nlogo.nvm.{ Context, Reporter }

import scala.jdk.CollectionConverters.{ ListHasAsScala, SeqHasAsJava }

class _constcodeblock(value: JList[Token]) extends Reporter with Pure {
  def this(value: Seq[Token]) = this(value.asJava)

  override def toString =
    s"${super.toString}: [${value.asScala.map((t: Token) => t.text).mkString(" ")}]"

  override def report(ctx: Context) = value
}
