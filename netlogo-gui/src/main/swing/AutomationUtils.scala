// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, EventQueue, Toolkit }
import java.awt.event.{ InputEvent, KeyEvent, MouseEvent }
import java.util.concurrent.TimeoutException

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.{ Duration, SECONDS }

// various useful methods for automated GUI testing (Isaac B 11/8/25)
object AutomationUtils {
  private lazy val eventQueue: EventQueue = Toolkit.getDefaultToolkit.getSystemEventQueue

  def waitFor[T](function: () => T, seconds: Int = 5): Option[T] =
    timedFunction(function, seconds)

  def waitForGUI[T](function: () => T, seconds: Int = 5): Option[T] = {
    timedFunction(() => {
      var result: Option[T] = None

      EventQueue.invokeAndWait(() => {
        result = Option(function())
      })

      result
    }, seconds).flatten
  }

  def waitUntil(test: () => Boolean, seconds: Int = 5): Boolean = {
    timedFunction(() => {
      while (!test())
        Thread.sleep(250)
    }, seconds).isDefined
  }

  def waitUntilGUI(test: () => Boolean, seconds: Int = 5): Boolean = {
    timedFunction(() => {
      while {
        var result = false

        EventQueue.invokeAndWait(() => {
          result = test()
        })

        !result
      } do {
        Thread.sleep(250)
      }
    }, seconds).isDefined
  }

  def sendKey(comp: Component, key: Int, ctrl: Boolean = false): Boolean = {
    if (!comp.hasFocus) {
      comp.requestFocus()

      // make sure all focus-related events are processed (Isaac B 11/9/25)
      if (!waitUntil(comp.hasFocus))
        return false
    }

    val mods: Int = {
      if (ctrl) {
        InputEvent.CTRL_DOWN_MASK
      } else {
        0
      }
    }

    eventQueue.postEvent(new KeyEvent(comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis, mods, key,
                                      KeyEvent.CHAR_UNDEFINED))

    eventQueue.postEvent(new KeyEvent(comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis, mods, key,
                                      KeyEvent.CHAR_UNDEFINED))

    // wait for key events to be processed (Isaac B 11/9/25)
    EventQueue.invokeAndWait(() => {})

    true
  }

  def sendChars(comp: Component, text: String): Boolean = {
    if (!comp.hasFocus) {
      comp.requestFocus()

      // make sure all focus-related events are processed (Isaac B 11/6/25)
      if (!waitUntil(comp.hasFocus))
        return false
    }

    text.foreach { char =>
      eventQueue.postEvent(new KeyEvent(comp, KeyEvent.KEY_TYPED, System.currentTimeMillis, 0,
                                        KeyEvent.VK_UNDEFINED, char))
    }

    // wait for key events to be processed (Isaac B 11/9/25)
    EventQueue.invokeAndWait(() => {})

    true
  }

  def sendLine(comp: Component, text: String): Boolean =
    sendChars(comp, text) && sendKey(comp, KeyEvent.VK_ENTER)

  def sendClick(comp: Component, x: Int, y: Int): Unit = {
    eventQueue.postEvent(new MouseEvent(comp, MouseEvent.MOUSE_MOVED, System.currentTimeMillis, 0, x, y, 0, false,
                                        MouseEvent.NOBUTTON))

    eventQueue.postEvent(new MouseEvent(comp, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis, 0, x, y, 1, false,
                                        MouseEvent.BUTTON1))

    eventQueue.postEvent(new MouseEvent(comp, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis, 0, x, y, 1, false,
                                        MouseEvent.BUTTON1))

    eventQueue.postEvent(new MouseEvent(comp, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis, 0, x, y, 1, false,
                                        MouseEvent.BUTTON1))

    // wait for mouse events to be processed (Isaac B 11/9/25)
    EventQueue.invokeAndWait(() => {})
  }

  def sendDrag(comp: Component, x1: Int, y1: Int, x2: Int, y2: Int): Unit = {
    eventQueue.postEvent(new MouseEvent(comp, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis, 0, x1, y1, 1, false,
                                        MouseEvent.BUTTON1))

    eventQueue.postEvent(new MouseEvent(comp, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis,
                                        InputEvent.BUTTON1_DOWN_MASK, x2, y2, 1, false, MouseEvent.BUTTON1))

    eventQueue.postEvent(new MouseEvent(comp, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis, 0, x2, y2, 1, false,
                                        MouseEvent.BUTTON1))

    // wait for mouse events to be processed (Isaac B 11/9/25)
    EventQueue.invokeAndWait(() => {})
  }

  private def timedFunction[T](function: () => T, seconds: Int): Option[T] = {
    try {
      Await.result(Future {
        Option(function())
      }(using ExecutionContext.global), Duration(seconds, SECONDS))
    } catch {
      case _: TimeoutException =>
        None
    }
  }
}
