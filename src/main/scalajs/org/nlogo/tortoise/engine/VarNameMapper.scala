package org.nlogo.tortoise.engine

object EngineVariableNames {

  object CommonE {
    val HiddenKeyE = "hidden"
    val IDKeyE     = "id"
  }

  object PatchE {
    val PLabelColorKeyE = "plabelcolor"
    val PXCorKeyE       = "pxcor"
    val PYCorKeyE       = "pycor"
    val PColorKeyE      = "pcolor"
    val PLabelKeyE      = "plabel"
  }

  object TurtleE {
    val BreedKeyE      = "breed"
    val ColorKeyE      = "color"
    val HeadingKeyE    = "heading"
    val LabelKeyE      = "label"
    val LabelColorKeyE = "labelcolor"
    val PenModeKeyE    = "penmode"
    val PenSizeKeyE    = "pensize"
    val ShapeKeyE      = "shape"
    val SizeKeyE       = "size"
    val XCorKeyE       = "xcor"
    val YCorKeyE       = "ycor"
  }

}

private[engine] object ViewVariableNames {

  object CommonV {
    val HiddenKeyV = "HIDDEN?"
    val IDKeyV     = "WHO"
  }

  object PatchV {
    val PLabelColorKeyV = "PLABEL-COLOR"
  }

  object TurtleV {
    val LabelColorKeyV = "LABEL-COLOR"
    val PenModeKeyV    = "PEN-MODE"
    val PenSizeKeyV    = "PEN-SIZE"
  }

}

object VarNameMapper {
  import EngineVariableNames._, ViewVariableNames._, CommonE._, PatchE._, TurtleE._, CommonV._, PatchV._, TurtleV._
  def apply(engineName: String): String =
    engineName match {
      case PLabelColorKeyE => PLabelColorKeyV
      case LabelColorKeyE  => LabelColorKeyV
      case PenSizeKeyE     => PenSizeKeyV
      case PenModeKeyE     => PenModeKeyV
      case HiddenKeyE      => HiddenKeyV
      case IDKeyE          => IDKeyV
      case _               => engineName.toUpperCase
    }
}
