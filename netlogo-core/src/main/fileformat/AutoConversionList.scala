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

  lazy val preConversions = Seq(
    // this conversion exists to work around a bug in older versions of NetLogo where creating a breed
    // whose name conflicts with an existing is-<type>? reporter would just shadow the new is-<breed>?
    // reporter instead of throwing a compiler error. (Isaac B 5/15/26)
    ConversionSet("rename conflicting breeds", Seq(
      _.renameBreedSingular("agent", "an-agent"),
      _.renameBreedSingular("agentset", "an-agentset"),
      _.renameBreedSingular("boolean", "a-boolean"),
      _.renameBreedSingular("directed-link", "a-directed-link"),
      _.renameBreedSingular("link", "a-link"),
      _.renameBreedSingular("link-set", "a-link-set"),
      _.renameBreedSingular("list", "a-list"),
      _.renameBreedSingular("number", "a-number"),
      _.renameBreedSingular("patch-set", "a-patch-set"),
      _.renameBreedSingular("string", "a-string"),
      _.renameBreedSingular("turtle-set", "a-turtle-set"),
      _.renameBreedSingular("undirected-link", "an-undirected-link"),
      _.renameBreedPlural("agent", "agents"),
      _.renameBreedPlural("agentset", "agentsets"),
      _.renameBreedPlural("boolean", "booleans"),
      _.renameBreedPlural("directed-link", "directed-links"),
      _.renameBreedPlural("link", "links"),
      _.renameBreedPlural("link-set", "link-sets"),
      _.renameBreedPlural("list", "lists"),
      _.renameBreedPlural("number", "numbers"),
      _.renameBreedPlural("patch-set", "patch-sets"),
      _.renameBreedPlural("string", "strings"),
      _.renameBreedPlural("turtle-set", "turtle-sets"),
      _.renameBreedPlural("undirected-link", "undirected-links")
    )),
    // this conversion exists to work around a bug in older versions of NetLogo where declarations could
    // be placed after the procedures without incurring a compiler error. (Isaac B 5/15/26)
    ConversionSet("reorder declarations", Seq(_.reorderDeclarations()))
  )

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

      val targets = CommandReplacements.map(_._1) ++ Seq("movie-set-frame-rate", "movie-start", "movie-status")

      val sharedTransformations =
        CommandReplacements
          .map((original, replacement) => ((rewriter: SourceRewriter) => rewriter.replace(original, replacement))) ++
          Seq[SourceRewriter => String](
            _.remove("movie-set-frame-rate"),
            _.addCommand("movie-start", "set _recording-save-file-name {0}"),
            _.replace("movie-start", "vid:start-recorder"),
            _.replace("movie-status", "vid:recorder-status"))

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
        _.lambdaize())
      ConversionSet("replace tasks with anonymous procedures", conversions, conversions, targets, (d: Dialect) => Femto.get[Dialect]("org.nlogo.parse.LambdaConversionDialect", d))
    },
    "NetLogo 6.0-BETA2" -> {
      changeAllCode("rename range to _range", Seq(_.replaceToken("range", "_range")), Seq("range"))
    },
    "NetLogo 6.1" -> {
      changeAllCode("remove CF extension; replace with build-in primitives",
        Seq(
          _.removeExtension("cf"),
          _.replaceToken("cf:ifelse", "ifelse"),
          _.replaceToken("cf:ifelse-value", "ifelse-value"),
        ),
        Seq("cf")
      )
    },
    "NetLogo 7.0" -> {
      changeAllCode("Promote and rename experimental `__change-topology` to canonical `set-topology`",
        Seq(
          _.replaceToken("__change-topology", "set-topology"),
        ),
        Seq("__change-topology")
      )
    }
  )
}
