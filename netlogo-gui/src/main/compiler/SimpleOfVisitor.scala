// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler
import org.nlogo.prim._
import org.nlogo.nvm.Reporter
/**
 * Converts _of(_turtlevariable,...) to _turtlevariableof(...)
 * and _of(_patchvariable,...) to _patchvariableof(...)
 * for better efficiency.
 */
// This could be done in Optimizer instead, but this code is nice and simple and we already have it,
// so why not keep it? - ST 2/8/09
private class SimpleOfVisitor extends DefaultAstVisitor {
  override def visitReporterApp(app:ReporterApp) {
    if(app.reporter.isInstanceOf[_of]) process(app)
    super.visitReporterApp(app)
  }
  private def process(app:ReporterApp) {
    for(r <- convert(app.args(0).asInstanceOf[ReporterBlock].app.reporter)) {
      r.copyMetadataFrom(app.reporter)
      app.reporter = r
      app.removeArgument(0)
    }
  }
  private def convert(reporter:Reporter):Option[Reporter] =
    reporter match {
      case v:_patchvariable => Some(new _patchvariableof(v.vn))
      case v:_turtlevariable => Some(new _turtlevariableof(v.vn))
      case v:_linkvariable => Some(new _linkvariableof(v.vn))
      case v:_turtleorlinkvariable => Some(new _turtleorlinkvariableof(v.varName))
      case v:_breedvariable => Some(new _breedvariableof(v.name))
      case v:_linkbreedvariable => Some(new _linkbreedvariableof(v.name))
      case _ => None
    }
}
