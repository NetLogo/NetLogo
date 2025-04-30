// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import org.nlogo.core.{ CompilerException, LiteralParser }

/**
 * Turns an Model into a NetLogo source fragment
 * Parse from stocks out, calculating only the dependencies.  Build a
 * concise textual explanation of the execution model as a comment in
 * Code tab. */
class Translator(model: Model, compiler: LiteralParser) {
  val stocks = new collection.mutable.ListBuffer[Stock]
  val rates = new collection.mutable.ListBuffer[Rate]
  val converters = new collection.mutable.ListBuffer[Converter]
  val constantConverters = new collection.mutable.ListBuffer[Converter]
  val dt = model.dt.toString
  for(element <- model.elements; if !element.name.isEmpty)
    element match {
      case _: Reservoir => // ignore
      case s: Stock => stocks += s
      case r: Rate => if(!r.expression.isEmpty) rates += r
      case c: Converter =>
        if(!c.expression.isEmpty)
          if(isConstant(c.expression.toUpperCase))
            constantConverters += c
          else converters += c
      case _ => // ignore
    }
  def source = {
    val sortedStocks = stocks.toArray
    java.util.Arrays.sort(sortedStocks, new StockComparator)
    var globals = ""
    var procedures = ""
    var plots = ""
    globals += ";; System dynamics model globals\nglobals [\n"
    procedures += ";; Initializes the system dynamics model.\n;; Call this in your model's SETUP procedure.\n"
    procedures += "to system-dynamics-setup\n  reset-ticks\n  set " + "dt " + dt + "\n"
    if(!constantConverters.isEmpty) {
      globals += "  ;; constants\n"
      procedures += "  ;; initialize constant values\n"
      for(cc <- constantConverters) {
        globals += "  " + cc.name + "\n"
        procedures += initialValueExpressionForConverter(cc)
      }
    }
    if(!stocks.isEmpty) {
      globals += "  ;; stock values\n"
      procedures += "  ;; initialize stock values\n"
      for(s <- sortedStocks) {
        globals += "  " + s.name + "\n"
        procedures += initialValueExpressionForStock(s)
      }
    }
    // add the dt global
    globals += "  ;; size of each step, see SYSTEM-DYNAMICS-GO\n" + "  " + "dt\n"
    globals += "]\n\n"
    procedures += "end\n\n"
    procedures += ";; Step through the system dynamics model by performing next iteration of Euler's method.\n"
    procedures += ";; Call this in your model's GO procedure.\n"
    procedures += "to system-dynamics-go\n"
    plots += ";; Plot the current state of the system dynamics model's stocks\n"
    plots += ";; Call this procedure in your plot's update commands.\n"
    plots += "to system-dynamics-do-plot\n"
    if(!converters.isEmpty || !rates.isEmpty) {
      procedures += "\n  ;; compute variable and flow values once per step\n"
      for(c <- converters)
        procedures += "  let local-" + c.name + " " + c.name + "\n"
      for(r <- rates)
        procedures += "  let local-" + r.name + " " + r.name + "\n"
    }
    if(!stocks.isEmpty) {
      procedures += "\n  ;; update stock values\n" +
        "  ;; use temporary variables so order of computation doesn't affect result.\n"
      for(s <- sortedStocks) {
        procedures += updateStockExpression(s)
        plots += "  if plot-pen-exists? \"" + s.name + "\" [\n"
        plots += "    set-current-plot-pen \"" + s.name + "\"\n"
        plots += "    plotxy ticks " + s.name + "\n"
        plots += "  ]\n"
      }
      for(s <- sortedStocks)
        procedures += "  set " + s.name + " new-" + s.name + "\n"
    }
    procedures += "\n  tick-advance dt\nend\n\n"
    plots += "end\n\n"
    // each rate is a reporter based on the expression entered by the user
    for(r <- rates)
      procedures += procedureForRate(r)
    for(c <- converters )
      procedures += procedureForConverter(c)
    globals + procedures + plots
  }
  private def procedureForRate(r: Rate) =
    ";; Report value of flow\n" +
      "to-report " + r.name + "\n" + "  report ( " +
      (if(r.expression == null) "0" else r.expression) +
      "\n  ) * " + "dt" + "\n" +
      "end\n\n"
  def procedureForConverter(c: Converter) =
    ";; Report value of variable\n" +
    "to-report " + c.name + "\n" + "  report " +
    (if(c.expression == null) "0" else c.expression) +
    "\n" + "end\n\n"
  def initialValueExpressionForStock(s: Stock) =
    "  set " + s.name + " " +
    (if(s.initialValueExpression != null) s.initialValueExpression else "0") +
    "\n"
  def initialValueExpressionForConverter(c: Converter) =
    "  set " + c.name + " " +
    (if(c.expression != null) c.expression else "0") +
    "\n"
  // each stock may be affected by more than one rate so check the sink and source of each rate.
  def updateStockExpression(s: Stock) = {
    var expr = "  let new-" + s.name +
      (if(s.nonNegative) " max( list 0 ( " else " ( ") +
      s.name + " "
    for(r <- rates) {
      if(r.source.name == s.name)
        expr += "- " + "local-" + r.name + " "
      if(r.sink.name == s.name)
        expr += "+ " + "local-" + r.name + " "
    }
    if(s.nonNegative)
      expr += ") "
    expr + ")\n"
  }
  def isConstant(s: String) =
    try { compiler.readFromString(s); true }
    catch { case _: CompilerException => false }
  class StockComparator extends java.util.Comparator[Stock] {
    // Stock ordering is as follows: constants first, then alphabetical by name.  Ideally we should
    // also order depending upon which expressions refer to other stocks.  -- 11/09/05 CLB
    def compare(s1: Stock, s2: Stock) = {
      val sname1 = s1.name.toUpperCase
      val sname2 = s2.name.toUpperCase
      val sexp1 = s1.initialValueExpression.toUpperCase
      val sexp2 = s2.initialValueExpression.toUpperCase
      if(isConstant(sexp1))
        if(isConstant(sexp2))
          // both constant, compare names
          sname1.compareTo(sname2)
        else -1  // only stock1 is constant
      else if(isConstant(sexp2))
        1  // only stock2 is constant
      else // both not constant, compare names
        sname1.compareTo(sname2)
    }
  }
}
