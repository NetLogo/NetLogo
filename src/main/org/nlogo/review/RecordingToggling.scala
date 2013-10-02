// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.util.SimplePublisher

case class RecordingTogglingEvent(val enabled: Boolean)

trait RecordingToggling {
  private var _modelRunRecordingEnabled = false
  val recordingTogglingEventPublisher = new SimplePublisher[RecordingTogglingEvent]()
  def recordingEnabled = _modelRunRecordingEnabled
  def recordingEnabled_=(enabled: Boolean): Unit = {
    _modelRunRecordingEnabled = enabled
    recordingTogglingEventPublisher.publish(RecordingTogglingEvent(enabled))
  }
}
