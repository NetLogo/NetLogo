// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color

import org.nlogo.core.I18N
import org.nlogo.theme.ColorTheme

class EditableColor(val key: String, var value: Color) {
  def name: String =
    I18N.gui.get(s"menu.tools.themeEditor.$key")
}

trait EditableTheme(base: ColorTheme) {
  protected var name: String = base.name
  protected var isDark: Boolean = base.isDark

  protected val colors: Seq[EditableColor] = base.colors.map((key, color) => new EditableColor(key, color)).toSeq

  protected def getStaticTheme: ColorTheme =
    ColorTheme(name, isDark, false, colors.map(color => (color.key, color.value)).toMap)
}
