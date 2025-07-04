// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.nio.file.Path
import java.util.Locale

import org.nlogo.core.{ CompilationEnvironment, Dialect, ExtensionManager,
  LibraryManager, LiteralParser, Model, Plot, SourceRewriter }
import org.nlogo.api.AutoConvertable

object PlotConverter {
  def allPlotNames(model: Model): Seq[String] =
    model.plots.flatMap(_.display).filterNot(_.isEmpty)

  def allKeyedPenNames(model: Model): Seq[(String, Seq[String])] = {
    model.plots
      .map((plot: Plot) => (plot.display.getOrElse(""), plot.pens.toSeq.map(_.display)))
      .filterNot(_._1.isEmpty)
  }

  private[fileformat] def generateRenameMappings(groupedNames: Seq[(String, Seq[String])]):  Seq[(String, String)] = {
    groupedNames
      .map {
        case (upper, originals) => upper -> originals
      }
      .flatMap {
        case (upper, originals) if originals.length > 1 =>
          // Put lower-case names before uppercase names
          val sortedOriginals = originals.sorted.reverse
          sortedOriginals.zipWithIndex.map {
            case (original, index) if index == 0 => (original, original)
            case (original, index) => {
              var new_name = s"${original}_${index}"
              var new_index = index
              while(groupedNames.map(_._1).contains(new_name.toUpperCase(Locale.ENGLISH))){
                new_index = new_index + 1
                new_name  = s"${original}_${new_index}"
              }
              (original, s"${original}_${new_index}")
            }
          }
        case _ => Seq.empty[(String, String)]
      }
  }

  private[fileformat] def determinePenSubstitutions(names: Seq[(String, Seq[String])]): Seq[(String, Seq[(String, String)])] = {
    names.map(plotAndPens =>
        (plotAndPens._1, generateRenameMappings(plotAndPens._2.groupBy(_.toUpperCase(Locale.ENGLISH)).toSeq)))
                                                              .filterNot(_._2.isEmpty)
  }

  private[fileformat] def determinePlotSubstitutions(names: Seq[String]): Seq[(String, String)] = {
    generateRenameMappings(names.groupBy(_.toUpperCase(Locale.ENGLISH)).toSeq)
  }

  private def modifyPlotNameKeys(groupedPlotAndPens: Seq[(String, Seq[(String, String)])], nameMappings: Seq[(String, String)]): Seq[(String, Seq[(String, String)])] =
    groupedPlotAndPens
      .map(element => (nameMappings.toMap.getOrElse(element._1, element._1), element._2))

  private def clarifyPlotProcedureBody(renamePairs: Seq[(String, String)]): String = {
    val nameMap = renamePairs.map(p => s"""["${p._1}" "${p._2}"]""").mkString("[", " ", "]")
    s"""|  let name-map ${nameMap}
        |  let replacement filter [ rename -> first rename = name ] name-map
        |  let reported-name name
        |  if not empty? replacement [
        |    set reported-name item 1 (item 0 replacement)
        |  ]
        |  report reported-name""".stripMargin
  }

  private def clarifyPensProcedureBody(renamePensPairs: Seq[(String, Seq[(String, String)])]): String = {
    val penNameMapping = renamePensPairs.flatMap(renamePair => renamePair._2.map(p => s"""[ "${renamePair._1}" "${p._1}" "${p._2}" ]""")).mkString("[", " ", "]")
    s"""|  let name-map ${penNameMapping}
        |  let replacement filter [ rename -> first rename = plot-name and item 1 rename = name ] name-map
        |  let reported-name name
        |  if not empty? replacement [
        |    set reported-name item 2 (item 0 replacement)
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
        _.addReporterProcedure(reporterName, Seq("name"), clarifyPlotProcedureBody(renames))
      val sharedTransformations = Seq[SourceRewriter => String](
        _.replaceToken(conversionTarget, s"$conversionTarget $reporterName"))
      Seq(ConversionSet(description,
        codeTabTransformation +: sharedTransformations,
        sharedTransformations,
        Seq(conversionTarget)))
    } else
      Seq.empty[ConversionSet]
  }

  private def buildPensConversionSet(
    description: String,
    reporterName: String,
    renames: Seq[(String, Seq[(String, String)])],
    conversionTarget: String
  ): Seq[ConversionSet] = {
    if (renames.nonEmpty) {
      val codeTabTransformation: SourceRewriter => String =
        _.addReporterProcedure(reporterName, Seq("name"), clarifyPensProcedureBody(renames))
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
    val plotSubstitutions = determinePlotSubstitutions(allPlotNames(model))
    val plotConversions =
      buildConversionSet("Make plots case-insensitive",
        "_clarify-duplicate-plot-name",
        plotSubstitutions,
        "set-current-plot")

    val penConversions =
      buildPensConversionSet("Make pens case-insensitive",
        "_clarify-duplicate-plot-pen-name",
        modifyPlotNameKeys(determinePenSubstitutions(allKeyedPenNames(model)), plotSubstitutions),
        "set-current-plot-pen")

    plotConversions ++ penConversions
  }
}

class PlotConverter(
  extensionManager:      ExtensionManager,
  libManager:            LibraryManager,
  compilationEnv:        CompilationEnvironment,
  literalParser:         LiteralParser,
  baseDialect:           Dialect,
  components:            Seq[AutoConvertable]) extends
  ModelConverter(
    extensionManager,
    libManager,
    compilationEnv,
    literalParser,
    baseDialect,
    components,
    PlotConverter.plotCodeConversions) {

  import PlotConverter._

  override def apply(model: Model, modelPath: Path): ConversionResult = {
    val plotRenames = determinePlotSubstitutions(allPlotNames(model))
    val penRenames: Seq[(String, Seq[(String,String)])] = determinePenSubstitutions(allKeyedPenNames(model))
    (plotRenames, penRenames) match {
      case (Seq(), Seq()) => super.apply(model, modelPath)
      case _ => { val conversion = super.apply(model, modelPath)
        val convertPandPN: Model = convertPlotAndPenNames(conversion.model, plotRenames.toMap, penRenames.toMap)
        conversion.updateModel(convertPandPN)
      }
    }
  }

  private def convertPlotAndPenNames(
    model:       Model,
    plotRenames: Map[String, String],
    penRenames:  Map[String, Seq[(String, String)]]): Model = {
    val updatedWidgets = model.widgets.map{
      case p: Plot => {
        val newDisplay = p.display.map(currentName => plotRenames.getOrElse(currentName, currentName))
        val localRenames = penRenames.getOrElse((p.display.getOrElse("")), Seq()).toMap
        val newPens = p.pens.map {
          pen =>
            val newPenName = localRenames.getOrElse(pen.display, pen.display)
            pen.copy(display = newPenName)
        }
        p.copy(display = newDisplay, pens = newPens)
      }
      case w => w
    }
    model.copy(widgets = updatedWidgets)
  }
}
