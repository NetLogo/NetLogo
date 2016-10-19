// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{Action, KeyStroke}
import javax.swing.text.TextAction

import org.nlogo.editor.{ Colorizer, EditorArea, EditorConfiguration }

class CodeEditor(configuration: EditorConfiguration) extends EditorArea(configuration)
