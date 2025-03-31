// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }
import javax.swing.JLabel

import org.nlogo.api.CompilerServices
import org.nlogo.core.{ CompilerException, I18N, LogoList, Nobody }
import org.nlogo.editor.Colorizer
import org.nlogo.theme.InterfaceColors

class ChooserEditPanel(target: ChooserWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.chooser.globalVar"),
        () => target.name,
        target.setNameWrapper(_),
        () => apply()),
      compiler)

  private val choicesWrapper =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.chooser.choices"),
        () => target.choicesWrapper,
        target.setChoicesWrapper(_),
        () => apply()),
      colorizer) {

      private def nobodyFree(a: AnyRef): Boolean = {
        a match {
          case Nobody => false
          case list: LogoList => list.forall(nobodyFree)
          case _ => true
        }
      }

      override def get: Option[String] = {
        super.get.filter { code =>
          try {
            compiler.readFromString(s"[ $code ]") match {
              case list: LogoList => list.nonEmpty && list.forall(nobodyFree)
              case _ => false
            }
          } catch {
            case _: CompilerException => false
          }
        }
      }
    }

  private val choicesLabel = new JLabel(I18N.gui.get("edit.chooser.example")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply))

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(nameWrapper, c)

    c.insets = new Insets(0, 6, 3, 6)

    add(choicesWrapper, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(choicesLabel, c)
    add(oldSize, c)

    nameWrapper.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(nameWrapper, choicesWrapper, oldSize)

  override def isResizable: Boolean = true

  override def syncTheme(): Unit = {
    nameWrapper.syncTheme()
    choicesWrapper.syncTheme()
    oldSize.syncTheme()

    choicesLabel.setForeground(InterfaceColors.dialogText)
  }
}
