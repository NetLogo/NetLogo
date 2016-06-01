// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ prim, nvm }

/**
 * Converts _of(_turtlevariable,...) to _turtlevariableof(...)
 * and _of(_patchvariable,...) to _patchvariableof(...)
 * for better efficiency.
 */
// This could be done in Optimizer instead, but this code is nice and simple and we already have it,
// so why not keep it? - ST 2/8/09
class SimpleOfVisitor extends DefaultAstVisitor {
  override def visitReporterApp(app: ReporterApp) {
    if(app.reporter.isInstanceOf[prim._of])
      process(app)
    super.visitReporterApp(app)
  }
  private def process(app: ReporterApp) {
    for(r <- convert(app.args(0).asInstanceOf[ReporterBlock].app.reporter)) {
      r.copyMetadataFrom(app.reporter)
      app.reporter = r
      app.removeArgument(0)
    }
  }
  private def convert(reporter: nvm.Reporter): Option[nvm.Reporter] =
    reporter match {
      case v: prim._patchvariable        => Some(new prim._patchvariableof(v.vn))
      case v: prim._turtlevariable       => Some(new prim._turtlevariableof(v.vn))
      case v: prim._linkvariable         => Some(new prim._linkvariableof(v.vn))
      case v: prim._turtleorlinkvariable => Some(new prim._turtleorlinkvariableof(v.varName))
      case v: prim._breedvariable        => Some(new prim._breedvariableof(v.name))
      case v: prim._linkbreedvariable    => Some(new prim._linkbreedvariableof(v.name))
      case _ => None
    }
}
