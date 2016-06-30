// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.SourceRewriter

case class ConversionSet(
  codeTabConversions:   Seq[SourceRewriter => String] = Seq(),
  otherCodeConversions: Seq[SourceRewriter => String] = Seq(),
  targets:              Seq[String] = Seq())


object AutoConversionList {
  type ConversionList = Seq[(String, ConversionSet)]

  def changeAllCode(changes: Seq[SourceRewriter => String], targets: Seq[String]): ConversionSet = {
    ConversionSet(changes, changes, targets)
  }

  lazy val conversions: ConversionList = Seq(
    "NetLogo 5.2" -> {
      changeAllCode(Seq(_.replaceToken("hsb", "__hsb-old")), Seq("hsb"))
    },
    "NetLogo 5.2" -> {
      changeAllCode(Seq(_.replaceToken("extract-hsb", "__extract-hsb-old")), Seq("extract-hsb"))
    },
    "NetLogo 5.2" -> {
      changeAllCode(Seq(_.replaceToken("approximate-hsb", "__approximate-hsb-old")), Seq("approximate-hsb"))
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

      ConversionSet(codeTabOnlyReplacements ++ sharedTransformations, sharedTransformations, targets)
    },
    "NetLogo 6.0-M9" -> {
      changeAllCode(Seq(_.remove("hubnet-set-client-interface")), Seq("hubnet-set-client-interface"))
    }
    )
}
