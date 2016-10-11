// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Dialect, Femto, SourceRewriter }

case class ConversionSet(
  conversionName:       String,
  codeTabConversions:   Seq[SourceRewriter => String] = Seq(),
  otherCodeConversions: Seq[SourceRewriter => String] = Seq(),
  targets:              Seq[String] = Seq(),
  conversionDialect:    Dialect => Dialect = identity)


object AutoConversionList {
  type ConversionList = Seq[(String, ConversionSet)]

  def changeAllCode(name: String, changes: Seq[SourceRewriter => String], targets: Seq[String]): ConversionSet = {
    ConversionSet(name, changes, changes, targets)
  }

  lazy val conversions: ConversionList = Seq(
    "NetLogo 5.2" -> {
      changeAllCode("hsb correction", Seq(_.replaceToken("hsb", "__hsb-old")), Seq("hsb"))
    },
    "NetLogo 5.2" -> {
      changeAllCode("extract-hsb correction", Seq(_.replaceToken("extract-hsb", "__extract-hsb-old")), Seq("extract-hsb"))
    },
    "NetLogo 5.2" -> {
      changeAllCode("approximate-hsb correction", Seq(_.replaceToken("approximate-hsb", "__approximate-hsb-old")), Seq("approximate-hsb"))
    },
    "NetLogo 6.0-M9" -> {
      val CommandReplacements = Seq(
        "movie-cancel"         -> "vid:reset-recorder",
        "movie-close"          -> "vid:save-recording _recording-save-file-name",
        "movie-grab-view"      -> "vid:record-view",
        "movie-grab-interface" -> "vid:record-interface")

      val targets = CommandReplacements.map(_._1) ++ Seq("movie-start", "movie-status")

      val sharedTransformations =
        CommandReplacements
          .map(replacement => ((rewriter: SourceRewriter) => rewriter.replaceCommand(replacement))) ++
          Seq[SourceRewriter => String](
            _.remove("movie-set-frame-rate"),
            _.addCommand("movie-start"       -> "set _recording-save-file-name {0}"),
            _.replaceCommand("movie-start"   -> "vid:start-recorder"),
            _.replaceReporter("movie-status" -> "vid:recorder-status"))

      val codeTabOnlyReplacements = Seq[SourceRewriter => String](
        _.addGlobal("_recording-save-file-name"),
        _.addExtension("vid"))

      ConversionSet("movie prims to vid extension", codeTabOnlyReplacements ++ sharedTransformations, sharedTransformations, targets)
    },
    "NetLogo 6.0-M9" -> {
      changeAllCode("remove hubnet-set-client-interface", Seq(_.remove("hubnet-set-client-interface")), Seq("hubnet-set-client-interface"))
    },
    "NetLogo 6.0-RC1" -> {
      val targets = Seq("task", "?", "?1", "?2", "?3", "?4", "?5", "?6", "?7", "?8", "?9")
      val conversions = Seq[SourceRewriter => String](
        _.replaceToken("is-reporter-task?", "is-anonymous-reporter?"),
        _.replaceToken("is-command-task?",  "is-anonymous-command?"),
        _.customRewrite("org.nlogo.parse.Lambdaizer"))
      ConversionSet("replace tasks with anonymous procedures", conversions, conversions, targets, (d: Dialect) => Femto.get[Dialect]("org.nlogo.parse.LambdaConversionDialect", d))
    }
    )
}
