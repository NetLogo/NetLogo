// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.nio.file.Path

import org.nlogo.core.{ CompilationEnvironment, Dialect, ExtensionManager,
  LiteralParser, Model, Plot, Pen, SourceRewriter }
import org.nlogo.api.AutoConvertable

object PlotConverter {
  def allPlotNames(model: Model): Seq[String] =
    model.plots.flatMap(_.display).filterNot(_.isEmpty)

  def allLocalPenNames(model: Model): Seq[Seq[String]] =
    model.plots.map(_.pens.toSeq)
      .map(_.map(_.display))

  def allPenNames(model: Model): Seq[String] =
    model.plots.flatMap(_.pens).collect {
      case pen: Pen => pen
    }.map(_.display)

  private def determineRenames(names: Seq[String]): Seq[(String, String)] = {
    names.groupBy(_.toUpperCase)
      .toSeq
      .map {
        case (upper, originals) => upper -> originals.distinct
      }
      .flatMap {
        case (upper, originals) if originals.length > 1 =>
          // Put lower-case names before uppercase names
          val sortedOriginals = originals.sorted.reverse
          sortedOriginals.zipWithIndex.map {
            case (original, index) if index == 0 => (original, original)
            case (original, index) => (original, s"${original}_${index}")
          }
        case _ => Seq.empty[(String, String)]
      }
  }

  private def clarifyProcedureBody(renamePairs: Seq[(String, String)]): String = {
    val nameMap = renamePairs.map(p => s"""["${p._1}" "${p._2}"]""").mkString("[", " ", "]")
    s"""|  let name-map ${nameMap}
        |  let replacement filter [ rename -> first rename = name] name-map
        |  let reported-name name
        |  if not empty? replacement [
        |    set reported-name item 1 replacement
        |  ]
        |  report reported-name""".stripMargin
  }

  private def buildConversionSet(
    description: String,
    reporterName: String,
    renames: Seq[(String, String)],
    conversionTarget: String
  ): Seq[ConversionSet] = {
    if (renames.nonEmpty) {
      val codeTabTransformation: SourceRewriter => String =
        _.addReporterProcedure(reporterName, Seq("name"), clarifyProcedureBody(renames))
      val sharedTransformations = Seq[SourceRewriter => String](
        _.replaceToken(conversionTarget, s"$conversionTarget $reporterName"))
      Seq(ConversionSet(description,
        codeTabTransformation +: sharedTransformations,
        sharedTransformations,
        Seq(conversionTarget)))
    } else
      Seq.empty[ConversionSet]
  }

  def plotCodeConversions(model: Model): Seq[ConversionSet] = {
    val plotConversions =
      buildConversionSet("Make plots case-insensitive",
        "_clarify-duplicate-plot-name",
        determineRenames(allPlotNames(model)),
        "set-current-plot")

    val penConversions =
      buildConversionSet("Make pens case-insensitive",
        "_clarify-duplicate-plot-pen-name",
        determineRenames(allPenNames(model)),
        "set-current-plot-pen")

    plotConversions ++ penConversions
  }

}

class PlotConverter(
  extensionManager:      ExtensionManager,
  compilationEnv:        CompilationEnvironment,
  literalParser:         LiteralParser,
  baseDialect:           Dialect,
  components:            Seq[AutoConvertable]) extends
  ModelConverter(
    extensionManager,
    compilationEnv,
    literalParser,
    baseDialect,
    components,
    PlotConverter.plotCodeConversions _) {
      import PlotConverter._

      override def apply(model: Model, modelPath: Path): ConversionResult = {
        val plotRenames = determineRenames(allPlotNames(model))
        val penRenames = allLocalPenNames(model).flatMap(determineRenames(_))
        if (plotRenames.isEmpty && penRenames.isEmpty) {
          super.apply(model, modelPath)
        } else {
          val conversion = super.apply(model, modelPath)
          conversion.updateModel(convertPlotAndPenNames(conversion.model, plotRenames.toMap, penRenames.toMap))
        }
      }

      private def convertPlotAndPenNames(
        model:       Model,
        plotRenames: Map[String, String],
        penRenames:  Map[String, String]): Model = {
          val updatedWidgets = model.widgets.map {
            case p: Plot =>
              val newDisplay = p.display.map(currentName => plotRenames.getOrElse(currentName, currentName))
              val newPens = p.pens.map {
                pen =>
                  val newPenName = penRenames.getOrElse(pen.display, pen.display)
                  pen.copy(display = newPenName)
              }
              p.copy(display = newDisplay, pens = newPens)
            case w => w
          }
          model.copy(widgets = updatedWidgets)
    }
}
