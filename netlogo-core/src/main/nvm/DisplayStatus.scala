// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

/**
 * A brief bit of history on DisplayStatus:
 *
 * Before DisplayStatus, we stored two boolean values which had to be combined to figure
 * out what to do about rendering the view. One of these `world.displayOn` lived on the world
 * and was toggled on and off by the `display` and `no-display` primitives.
 * The other of these was the status of the display switch (looked up in several different ways).
 *
 * When designing this, it was recognized that more than two display states were needed.
 *
 * The first of these is DisplayOff, which corresponds to the display switch being turned off
 * in the prior schema. The display is never rendered, and is in fact blanked on its next render.
 *
 * The next of these is DisplayAlways, which corresponds to the display switch being on
 * and `world.displayOn` being true in the prior schema. The display is renderered whenever
 * requested, either by the GUI or by NetLogo.
 *
 * The third of these modes is DisplayWhenForced, which corresponds to the display switch being on
 * in the prior schema, but with `display-off` having been called. The display is rendered only
 * when requested by NetLogo.
 */
sealed trait DisplayStatus {
  def shouldRender(force: Boolean): Boolean
  def trackSkippedFrames: Boolean
  def switchOn: Boolean
  // NOTE: the view can be rendered as gray for reasons beside the DisplayStatus.
  // When this is true, the view *should* be rendered as gray.
  def renderAsGray: Boolean
  def switchSet(on: Boolean): DisplayStatus
  def codeSet(on: Boolean): DisplayStatus
}

sealed case class DisplayOff(alwaysWhenSwitchedOn: Boolean) extends DisplayStatus {
  def switchOn: Boolean = false
  def renderAsGray: Boolean = ! alwaysWhenSwitchedOn
  def trackSkippedFrames: Boolean = alwaysWhenSwitchedOn
  def codeSet(on: Boolean): DisplayStatus = DisplayOff(on)
  def switchSet(on: Boolean): DisplayStatus =
    if (on && alwaysWhenSwitchedOn) DisplayAlways
    else if (on)                    DisplayOnForce
    else                            this
  def shouldRender(force: Boolean): Boolean = false
}

case object DisplayOnForce extends DisplayStatus {
  def switchOn: Boolean = true
  def renderAsGray: Boolean = true
  def trackSkippedFrames: Boolean = false
  def codeSet(on: Boolean): DisplayStatus =
    if (on) DisplayAlways
    else    this
  def switchSet(on: Boolean): DisplayStatus =
    if (on) this
    else    DisplayOff(false)
  def shouldRender(force: Boolean): Boolean = force
}

case object DisplayAlways extends DisplayStatus {
  def switchOn: Boolean = true
  def renderAsGray: Boolean = false
  def trackSkippedFrames: Boolean = true
  def codeSet(on: Boolean): DisplayStatus =
    if (on) this
    else    DisplayOnForce
  def switchSet(on: Boolean): DisplayStatus =
    if (on) this
    else    DisplayOff(true)
  def shouldRender(force: Boolean) = true
}
