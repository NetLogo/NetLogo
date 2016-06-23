// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Button, Model, Monitor, Plot, Slider, Widget }

import org.nlogo.api.{ AutoConvertable, AutoConverter }

import scala.util.Try

trait WidgetConverter extends AutoConvertable {
  override def requiresAutoConversion(original: Model, needsConversion: String => Boolean): Boolean =
    original.widgets.exists(widgetNeedsConversion(needsConversion))

  override def autoConvert(original: Model, autoConverter: AutoConverter): Model =
    original.copy(widgets = original.widgets.map(convertWidget(autoConverter)))

  private def widgetNeedsConversion(needsConversion: String => Boolean)(w: Widget): Boolean =
    w match {
      case b: Button =>
        b.source.exists(needsConversion)
      case s: Slider =>
        Seq(s.min, s.max, s.step).exists(needsConversion)
      case m: Monitor =>
        m.source.exists(needsConversion)
      case p: Plot =>
        (Seq(p.setupCode, p.updateCode) ++ p.pens.flatMap(pen => Seq(pen.setupCode, pen.updateCode))).exists(needsConversion)
      case _ => false
    }

  private def convertWidget(autoConverter: AutoConverter)(w: Widget): Widget = {
    Try {
      w match {
        case cWidget@(_: Button | _: Plot) =>
          cWidget.convertSource(autoConverter.convertStatement _)
        case rWidget@(_: Monitor | _: Slider) =>
          rWidget.convertSource(autoConverter.convertReporterExpression _)
        case _ => w
      }
    } getOrElse w
  }
}
