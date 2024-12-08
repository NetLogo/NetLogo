// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ Color, Component, FileDialog => JFileDialog }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, BorderFactory, JButton, JLabel, JPanel, JPopupMenu }

import org.nlogo.api.ExternalResourceManager
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ ExternalResource, I18N }
import org.nlogo.swing.{ FileDialog, OptionDialog }

abstract class ResourcePathEditor(accessor: PropertyAccessor[ExternalResource.Location], useTooltip: Boolean,
                                  parent: Component, resourceManager: ExternalResourceManager)
  extends PropertyEditor(accessor, useTooltip) {

  private class ResourceMenu extends JPanel {
    private val label = new JLabel(emptyName)

    private var resource: ExternalResource.Location = ExternalResource.None

    setBorder(BorderFactory.createLineBorder(Color.BLACK))

    add(label)
    add(new JLabel("v")) // placeholder until gui redesign is merged

    addMouseListener(new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        val menu = new JPopupMenu

        if (resourceManager.getResources.isEmpty)
          menu.add(I18N.gui("noFiles")).setEnabled(false)
        else {
          resourceManager.getResources.foreach(resource => menu.add(new AbstractAction(resource.name) {
            def actionPerformed(e: ActionEvent) {
              set(ExternalResource.Existing(resource.name))
            }
          }))
        }

        menu.show(ResourceMenu.this, 0, ResourceMenu.this.getHeight)
      }
    })

    def setSelection(resource: ExternalResource.Location) {
      label.setText(ExternalResourceManager.getName(resource).getOrElse(emptyName))

      this.resource = resource
    }

    def getSelection: ExternalResource.Location =
      resource
  }

  implicit val i18nPrefix = I18N.Prefix("property.resourcePath")

  private val emptyName = I18N.gui("empty")

  private val resourceMenu = new ResourceMenu

  add(new JLabel(accessor.displayName + ":"))
  add(resourceMenu)
  add(new JButton(new AbstractAction(I18N.gui("import")) {
    def actionPerformed(e: ActionEvent) {
      try {
        val path = FileDialog.showFiles(parent, I18N.gui("import"), JFileDialog.LOAD, null)
        val name = ExternalResourceManager.getName(path)

        if (resourceManager.getResource(name).isDefined)
          OptionDialog.showMessage(parent, I18N.gui("importError"), I18N.gui("exists", name),
                                   Array(I18N.gui.get("common.buttons.ok")))
        else
          set(ExternalResource.New(path))
      }

      catch {
        case e: UserCancelException =>
      }
    }
  }))

  def get: Option[ExternalResource.Location] = {
    resourceMenu.getSelection match {
      case ExternalResource.None => None
      case selection: ExternalResource.Location => Some(selection)
    }
  }

  def set(value: ExternalResource.Location) {
    resourceMenu.setSelection(value)
  }
}
