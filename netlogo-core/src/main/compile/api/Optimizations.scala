// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.api

object Optimizations {
  def none = new Optimizations(Seq(), Seq())
}

case class Optimizations(commandOptimizations: Seq[CommandMunger], reporterOptimizations: Seq[ReporterMunger]) {
  def nonEmpty = commandOptimizations.nonEmpty || reporterOptimizations.nonEmpty
}
