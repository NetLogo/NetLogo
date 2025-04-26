// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import java.util.{ Collections, List => JList }

import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Reporter }

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.BufferHasAsJava

class _shuffle extends Reporter {
  def report(context: Context): AnyRef = {
    val result: JList[AnyRef] = ListBuffer(argEvalList(context, 0).toVector: _*).asJava
    Collections.shuffle(result, context.job.random)
    LogoList.fromJava(result)
  }

  def report_1(context: Context, l0: LogoList): LogoList = {
    val result: JList[AnyRef] = ListBuffer(l0.toVector: _*).asJava
    Collections.shuffle(result, context.job.random)
    LogoList.fromJava(result)
  }
}
