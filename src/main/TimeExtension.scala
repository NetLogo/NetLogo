package org.nlogo.extensions.time

import org.nlogo.extensions.time.datatypes.LogoSchedule
import org.nlogo.extensions.time.primitives.DiscreteEventSchedulerPrimitives
import org.nlogo.extensions.time.primitives.TimePrimitives
//import org.nlogo.extensions.time.primitives.TimeSeriesPrimitives
import org.nlogo.api.DefaultClassManager
import org.nlogo.api.Context

object TimeExtension {
  val schedule: LogoSchedule = new LogoSchedule()
  var context: Context = null
  var nextEvent: Long = 0
  var debug: Boolean = false
}

class TimeExtension extends DefaultClassManager {

  def load(primManager: org.nlogo.api.PrimitiveManager): Unit = {
    /*
     Time and formatting Primitives
     */
    primManager.addPrimitive("is-before?", new TimePrimitives.IsBefore())
    primManager.addPrimitive("is-after?", new TimePrimitives.IsAfter())
    primManager.addPrimitive("is-equal?", new TimePrimitives.IsEqual())
    primManager.addPrimitive("is-between?", new TimePrimitives.IsBetween())
    primManager.addPrimitive("show", new TimePrimitives.Show())
    primManager.addPrimitive("get", new TimePrimitives.Get())
    primManager.addPrimitive("anchor-to-ticks", new TimePrimitives.Anchor())
    primManager.addPrimitive("plus", new TimePrimitives.Plus())
    primManager.addPrimitive("difference-between", new TimePrimitives.DifferenceBetween())
    primManager.addPrimitive("create", new TimePrimitives.NewLogoTime())
    primManager.addPrimitive("create-with-format", new TimePrimitives.CreateWithFormat())
    primManager.addPrimitive("copy", new TimePrimitives.Copy())
    /*
     TimeSeries Primitives:
     */
    // not included in beta. May re-include later
    // primManager.addPrimitive("ts-create", new TimeSeriesPrimitives.TimeSeriesCreate())
    // primManager.addPrimitive("ts-add-row", new TimeSeriesPrimitives.TimeSeriesAddRow())
    // primManager.addPrimitive("ts-load", new TimeSeriesPrimitives.TimeSeriesLoad())
    // primManager.addPrimitive("ts-load-with-format", new TimeSeriesPrimitives.TimeSeriesLoadWithFormat())
    // primManager.addPrimitive("ts-write", new TimeSeriesPrimitives.TimeSeriesWrite())
    // primManager.addPrimitive("ts-get", new TimeSeriesPrimitives.TimeSeriesGet())
    // primManager.addPrimitive("ts-get-interp", new TimeSeriesPrimitives.TimeSeriesGetInterp())
    // primManager.addPrimitive("ts-get-exact", new TimeSeriesPrimitives.TimeSeriesGetExact())
    // primManager.addPrimitive("ts-get-range", new TimeSeriesPrimitives.TimeSeriesGetRange())
    /*
     DiscreteEventscheduler primitives
     */
    primManager.addPrimitive("schedule-event", new DiscreteEventSchedulerPrimitives.AddEvent())
    primManager.addPrimitive("schedule-event-shuffled", new DiscreteEventSchedulerPrimitives.AddEventShuffled())
    primManager.addPrimitive("schedule-repeating-event", new DiscreteEventSchedulerPrimitives.RepeatEvent())
    primManager.addPrimitive("schedule-repeating-event-shuffled", new DiscreteEventSchedulerPrimitives.RepeatEventShuffled())
    primManager.addPrimitive("schedule-repeating-event-with-period", new DiscreteEventSchedulerPrimitives.RepeatEventWithPeriod())
    primManager.addPrimitive("schedule-repeating-event-shuffled-with-period", new DiscreteEventSchedulerPrimitives.RepeatEventShuffledWithPeriod())
    primManager.addPrimitive("size-of-schedule", new DiscreteEventSchedulerPrimitives.GetSize())
    primManager.addPrimitive("anchor-schedule",new DiscreteEventSchedulerPrimitives.AnchorSchedule())
    primManager.addPrimitive("go", new DiscreteEventSchedulerPrimitives.Go())
    primManager.addPrimitive("go-until", new DiscreteEventSchedulerPrimitives.GoUntil())
    primManager.addPrimitive("clear-schedule", new DiscreteEventSchedulerPrimitives.ClearSchedule())
    primManager.addPrimitive("show-schedule", new DiscreteEventSchedulerPrimitives.ShowSchedule())
  }

  override def clearAll(): Unit = {
    TimeExtension.schedule.clear()
 }
}
