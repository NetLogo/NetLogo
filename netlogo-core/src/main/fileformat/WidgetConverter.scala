// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Button, Model, Monitor, Plot, Slider, Widget }

import org.nlogo.api.{ AutoConvertable, AutoConverter }

import scala.util.{ Failure, Success, Try }

trait WidgetConverter extends AutoConvertable {
  def componentDescription: String = "NetLogo interface"

  override def requiresAutoConversion(original: Model, needsConversion: String => Boolean): Boolean =
    original.widgets.exists(widgetNeedsConversion(needsConversion))

  override def autoConvert(original: Model, autoConverter: AutoConverter): Either[(Model, Seq[Exception]), Model] = {
    val (widgets, errors) =
      original.widgets.foldLeft((Seq.empty[Widget], Seq.empty[Exception])) {
        case ((ws, es), w) => convertWidget(autoConverter)(w) match {
          case Failure(e: Exception) => (ws :+ w, es :+ e)
          case Failure(t)            => throw t
          case Success(w)            => (ws :+ w, es)
        }
      }
    if (errors.isEmpty)
      Right(original.copy(widgets = widgets))
    else
      Left((original.copy(widgets = widgets), errors))
  }

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

  private def convertWidget(autoConverter: AutoConverter)(w: Widget): Try[Widget] = {
    Try {
      w match {
        case cWidget@(_: Button | _: Plot) =>
          cWidget.convertSource(autoConverter.convertStatement _)
        case rWidget@(_: Monitor | _: Slider) =>
          rWidget.convertSource(autoConverter.convertReporterExpression _)
        case _ => w
      }
    }
  }
}

object WidgetConverter extends WidgetConverter
