// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ AWTKeyStroke, Component, Container, ContainerOrderFocusTraversalPolicy, KeyboardFocusManager }
import java.awt.event.{ FocusAdapter, FocusEvent, InputEvent, KeyEvent }

import scala.jdk.CollectionConverters.SetHasAsJava

trait FocusRoot extends Container {
  private val traversalPolicy = new TraversalPolicy

  private var canFocus = true

  setFocusCycleRoot(true)
  setFocusTraversalPolicy(traversalPolicy)

  addFocusListener(new FocusAdapter {
    override def focusGained(e: FocusEvent): Unit = {
      // moving to the parent focus cycle can result in a keyboard trap if the parent component is
      // not intended to be focused. this check detects the problematic case and refocuses the
      // component that initiated the upwards traversal after the cycle changes. (Isaac B 3/4/26)
      if (e.getCause == FocusEvent.Cause.TRAVERSAL_UP && !canFocus)
        e.getOppositeComponent.requestFocus(FocusEvent.Cause.TRAVERSAL_FORWARD)
    }
  })

  override def transferFocusDownCycle(): Unit = {
    super.transferFocusDownCycle()

    getDefaultComponent.foreach(_.requestFocus(FocusEvent.Cause.TRAVERSAL_DOWN))
  }

  protected def setCanFocus(enabled: Boolean): Unit = {
    canFocus = enabled
  }

  protected def setImplicitDownCycleTraversal(enabled: Boolean): Unit = {
    traversalPolicy.setImplicitDownCycleTraversal(enabled)

    if (enabled) {
      setFocusTraversalKeys(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS, null)
      setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, null)
    } else {
      setFocusTraversalKeys(KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS,
                            Set(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_ENTER, 0)).asJava)

      setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS,
                            Set(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK)).asJava)
    }
  }

  protected def getDefaultComponent: Option[Component] =
    None

  // this method defines a partial ordering for focus traversal, mapping a component to its
  // previous and next components. only exceptions to Java's default ordering should be
  // specified in this method. (Isaac B 3/4/26)
  protected def getFocusOrder: Map[Component, (Component, Component)] =
    Map()

  private class TraversalPolicy extends ContainerOrderFocusTraversalPolicy {
    override def getDefaultComponent(root: Container): Component = {
      if (getImplicitDownCycleTraversal) {
        FocusRoot.this.getDefaultComponent.getOrElse(super.getDefaultComponent(root))
      } else {
        super.getDefaultComponent(root)
      }
    }

    override def getComponentBefore(root: Container, component: Component): Component =
      getFocusOrder.get(component).flatMap((c, _) => Option(c)).getOrElse(super.getComponentBefore(root, component))

    override def getComponentAfter(root: Container, component: Component): Component =
      getFocusOrder.get(component).flatMap((_, c) => Option(c)).getOrElse(super.getComponentAfter(root, component))
  }
}
