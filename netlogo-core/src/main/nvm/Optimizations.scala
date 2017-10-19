// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

object Optimizations {
  type OptimizationList = Seq[(OptimizationType, String)]

  sealed trait OptimizationType

  case object Reporter extends OptimizationType
  case object Command  extends OptimizationType
  // indicates a reporter optimization which requires a dialect to construct
  case object DialectReporter extends OptimizationType

  val empty = Seq.empty[(OptimizationType, String)]

  val fdOptimizations: OptimizationList =
    optList(Command, Seq("Fd1", "FdLessThan1"))

  val emptyBlockCreateOptimizations: OptimizationList =
    optList(Command, Seq("CroFast", "CrtFast", "HatchFast", "SproutFast"))

  val optTraversalOptimizations: OptimizationList =
    optList(Reporter,
      Seq("AnyOther", "AnyOtherWith",
        "AnyWith1", "AnyWith2", "AnyWith3", "AnyWith4", "AnyWith5",
        "CountOther", "CountOtherWith", "CountWith",
        "OneOfWith", "OtherWith", "WithOther"))

  val miscellaneousOptimizations: OptimizationList =
    optList(Reporter, Seq(
        "Nsum",        // optimizes summing neighbor values
        "Nsum4",       // optimizes summing neighbor4 values
        "PatchAt",     // optimizes patch at offsets
        "RandomConst", // inlines const argument to random
        "With"))       // optimizes "with" to patch-col / patch-row

  val standardOptimizations =
    fdOptimizations ++
    emptyBlockCreateOptimizations ++
    optTraversalOptimizations ++
    miscellaneousOptimizations

  val guiOptimizations: OptimizationList =
    standardOptimizations ++ Seq(
      DialectReporter -> "org.nlogo.compile.optimize.DialectPatchVariableDouble",
      DialectReporter -> "org.nlogo.compile.optimize.DialectTurtleVariableDouble")

  val gui3DOptimizations: OptimizationList =
    guiOptimizations.filterNot(o => o == ((Reporter, "org.nlogo.compile.middle.optimize.With")))

  val headlessOptimizations: OptimizationList =
    standardOptimizations ++ Seq(
      Reporter -> toOptimizerClassName("PatchVariableDouble"),
      Reporter -> toOptimizerClassName("TurtleVariableDouble"),
      Reporter -> "org.nlogo.compile.optimize.Constants",
      Reporter -> "org.nlogo.compile.optimize.InRadiusBoundingBox")

  private def optList(tpe: OptimizationType, opts: Seq[String]): OptimizationList =
    opts.map(klass => tpe -> toOptimizerClassName(klass))

  private def toOptimizerClassName(klass: String) =
    s"org.nlogo.compile.middle.optimize.$klass"
}
