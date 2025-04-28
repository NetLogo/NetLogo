// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, Component, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JLabel, JPanel, ToolTipManager }

import org.nlogo.api.{ CompilerServices, Editable, Property }
import org.nlogo.core.{ CompilerException, I18N, LogoList, Nobody }
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ OptionPane, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.WidgetWrapperInterface

import scala.reflect.ClassTag

// This is the contents of an EditDialog, except for the buttons at the bottom (OK/Apply/Cancel).
class EditPanel(val target: Editable, val compiler: CompilerServices, colorizer: Colorizer, useTooltips: Boolean = false)
  extends JPanel with Transparent with ThemeSync {

  val oldDelay = ToolTipManager.sharedInstance.getDismissDelay()
  ToolTipManager.sharedInstance.setDismissDelay(30000)

  val propertyEditors = collection.mutable.ArrayBuffer[PropertyEditor[_]]()
  private var getsFirstFocus: PropertyEditor[_] = null
  var (originalSize, originalPreferredSize) = wrapperOption match {
    case Some(wrapper) => (wrapper.getSize, wrapper.getPreferredSize)
    case None => (null, null)
  }

  def init(): PropertyEditor[_] = {
    val layout = new GridBagLayout()
    setLayout(layout)
    getsFirstFocus = addProperties(this, target.propertySet, layout)
    getsFirstFocus
  }

  protected def addProperties(editorPanel: JPanel, properties: Seq[Property],
                              layout: GridBagLayout): PropertyEditor[_] = {
    var claimsFirstFocus: PropertyEditor[_] = null
    for (property <- properties) {
      val editor = getEditor(property, target, useTooltips && property.notes != null && property.notes.trim != "")
      val panel = new JPanel(new BorderLayout) with Transparent {
        add(editor, BorderLayout.CENTER)
        if (property.notes != null && property.notes.trim != "")
          if (useTooltips) {
            editor.setTooltip(property.notes)
          } else {
            add(new JLabel(property.notes) {
              setFont(getFont.deriveFont(9.0f))
              setForeground(InterfaceColors.dialogText())
            }, BorderLayout.SOUTH)
          }
      }

      val c = editor.getConstraints
      c.anchor = GridBagConstraints.WEST
      c.insets = new Insets(3, 3, 3, 3)
      c.gridwidth = property.gridWidth
      layout.setConstraints(panel, c)
      editorPanel.add(panel)
      propertyEditors += editor
      editor.refresh()
      editor.setEnabled(property.enabled)
      editor.setBorder(new javax.swing.border.EmptyBorder(property.borderSize, property.borderSize,
                                                          property.borderSize, property.borderSize))

      if (property.focus) {
        assert(claimsFirstFocus == null)
        claimsFirstFocus = editor
      }
    }
    claimsFirstFocus
  }
  private def wrapperOption = target match {
    case comp: Component =>
      comp.getParent match {
        case wrapper: WidgetWrapperInterface => Some(wrapper)
        case _ => None
      }
    case _ => None
  }
  override def requestFocus() {
    if (getsFirstFocus != null)
      getsFirstFocus.requestFocus()
  }
  def previewChanged(field: String, value: Option[Any]) { }  // overridden in WorldEditPanel
  def isResizable() =
    // Wow!  This sure is kludgy! - ST 5/25/04
    propertyEditors.exists {
      case _: BigStringEditor | _: CodeEditor => true
      case _ => false
    }
  def changed() {
    // this is kinda kludgy because of the need to deal with the WidgetWrapperInterface rather than
    // with the widget itself, but the alternative is to make a new event just to handle this, but
    // that would be kludgy in itself, and a great deal less simple... - ST 12/22/01
    for(wrapper <- wrapperOption) {
      val borderPad = wrapper.getBorderSize * 2
      val prefSize = wrapper.getPreferredSize
      if (wrapper.isNew) {
        val currentSize = wrapper.getSize
        if (prefSize.width != currentSize.width)
          prefSize.width = wrapper.snapToGrid(prefSize.width - borderPad) + borderPad
        if (prefSize.height != currentSize.height)
          prefSize.height = wrapper.snapToGrid(prefSize.height - borderPad) + borderPad
        wrapper.setSize(prefSize)
      }
      else if (originalPreferredSize != prefSize) {
        var width = 10000 min (prefSize.width max originalSize.width)
        var height = 10000 min (if (wrapper.verticallyResizable)
                                           prefSize.height max originalSize.height
                                         else prefSize.height)
        val currentSize = wrapper.getSize
        if (width != currentSize.width)
          width = wrapper.snapToGrid(width - borderPad) + borderPad
        if (prefSize.height != currentSize.height)
          height = wrapper.snapToGrid(height - borderPad) + borderPad
        wrapper.setSize(width, height)
        originalPreferredSize = wrapper.getPreferredSize
      }
      wrapper.widgetChanged()
    }
  }

  def valid() = {
    def valid(editor: PropertyEditor[_]) = {
      // Exclude runMetricsCondition from checking if the box is empty because an empty input is valid
      if (editor.accessor.accessString == "runMetricsCondition") {
        true
      } else {
      // plot editor handles its errors when you press the ok button.
      // that calls into editor.get. if there is an error, plot editor
      // pops up an error message. therefore, we cannot call get twice here.
      // so do not inline the call to editor.get. it will cause
      // the error to pop up twice. - JC 4/9/10
        val value = editor.get
        if (!value.isDefined && !editor.handlesOwnErrors)
          new OptionPane(this, I18N.gui.get("edit.general.invalidSettings"),
                         I18N.gui.getN("edit.general.invalidValue", editor.accessor.displayName),
                         OptionPane.Options.Ok, OptionPane.Icons.Error)
        value.isDefined
      }
    }
    propertyEditors.forall(valid) && targetValid()
  }

  def apply() {
    applyProperties()
    changed()
    ToolTipManager.sharedInstance.setDismissDelay(oldDelay)
  }

  def revert() {
    revertProperties()
    for(wrapper <- wrapperOption)
      wrapper.setSize(originalSize)
    ToolTipManager.sharedInstance.setDismissDelay(oldDelay)
  }

  private def applyProperties(): Unit = {
    propertyEditors.foreach(_.apply)
  }

  private def revertProperties(): Unit = {
    for(editor <- propertyEditors) {
      editor.revert()
      editor.refresh()
    }
  }

  private def targetValid(): Boolean = {
    propertyEditors.foreach(_.apply)
    val isValid = target.invalidSettings.isEmpty
    if (! isValid) {
      val allInvalidations =
        target.invalidSettings.map {
          case (name, error) =>
            val displayName =
              target.propertySet
                .filter(_.accessString == name)
                .map(_.name)
                .headOption
                .getOrElse(name)
                s"${displayName}: ${error}"
        }.mkString("\n", "\n", "")
      val invalidMessage = I18N.gui.getN("edit.general.invalidValues", allInvalidations)
      new OptionPane(this, I18N.gui.get("edit.general.invalidSettings"), invalidMessage, OptionPane.Options.Ok,
                     OptionPane.Icons.Error)
    }
    isValid
  }

  ////

  private def getEditor(property: Property, r: Editable, useTooltips: Boolean) = {
    import property._

    // Lets you specify other property editors to refresh when a different property
    // is changed.  This probably isn't the best place for this functionality to live
    // but hey.  -Jeremy B April 2022
    val noOp: () => Unit = () => {}
    val notifyOnChange: () => Unit =
      if (property.dependentPropertyNames.isEmpty) {
        noOp
      } else {
        () => {
          val dependentEditors = propertyEditors.filter( (editor) =>
            property.dependentPropertyNames.contains(editor.accessor.accessString)
          )
          dependentEditors.foreach(_.refresh)
          //changed()
        }
      }

    def accessor[T : ClassTag] =
      new PropertyAccessor[T](r, name, accessString, notifyOnChange)

    tpe match {
      case Property.StringOptions =>
        new OptionsEditor[String](accessor) with Changed
      case Property.BigString =>
        new BigStringEditor(accessor) with Changed
      case Property.Boolean =>
        new BooleanEditor(accessor) with Changed
      case Property.MetricsBoolean =>
        new MetricsBooleanEditor(accessor, propertyEditors)
      case Property.Color =>
        new ColorEditor(accessor, frame) with Changed
      case Property.Commands =>
        new CodeEditor(accessor, colorizer, collapsible, collapseByDefault) with Changed
      case Property.Double =>
        new DoubleEditor(accessor) with Changed
      case Property.Error =>
        new RuntimeErrorDisplay(accessor) with Changed
      case Property.StrictlyPositiveDouble =>
        new DoubleEditor(accessor) with Changed
        { override def get = super.get.filter(_ > 0) }
      case Property.Identifier =>
        new StringEditor(accessor) with Changed
        { override def get = super.get.map(_.trim).filter(compiler.isValidIdentifier) }
      case Property.InputBoxOptions =>
        new InputBoxEditor(accessor) with Changed
      case Property.Integer =>
        new IntegerEditor(accessor) with Changed
      case Property.Key =>
        new KeyEditor(accessor) with Changed
      case Property.LogoListString =>
        new CodeEditor(accessor, colorizer, false, false) with Changed {
          private def nobodyFree(a: AnyRef): Boolean = {
            a match {
              case Nobody       => false
              case ll: LogoList => ll.forall(nobodyFree)
              case _            => true
            }
          }

          override def get = super.get.filter{x =>
            try compiler.readFromString("[ " + x + " ]") match {
              case list: LogoList => !list.isEmpty && list.forall(nobodyFree)
              case _ => false
            }
            catch { case _: CompilerException => false }
        }}
      case Property.NegativeInteger =>
        new IntegerEditor(accessor) with Changed
        { override def get = super.get.filter(_ <= 0) }
      case Property.NonEmptyString =>
        new StringEditor(accessor) with Changed
        { override def get = super.get.filter(_.nonEmpty) }
      case Property.PlotOptions =>
        new OptionsEditor[org.nlogo.plot.Plot](accessor) with Changed
      case Property.PlotPens =>
        new PlotPensEditor(
          new PropertyAccessor[List[org.nlogo.plot.PlotPen]](r, property.name, property.accessString), colorizer)
      case Property.PositiveInteger =>
        new IntegerEditor(accessor) with Changed
        { override def get = super.get.filter(_ >= 0) }
      case Property.Reporter =>
        new CodeEditor(accessor, colorizer, collapsible, collapseByDefault) with Changed
        { override def get = super.get.map(_.trim).filter(_.nonEmpty) }
      case Property.ReporterOrEmpty =>
        new CodeEditor(accessor, colorizer, collapsible, collapseByDefault) with Changed
      case Property.ReporterLine =>
        new ReporterLineEditor(accessor, colorizer, property.optional) with Changed
      case Property.String =>
        new StringEditor(accessor) with Changed
      case Property.FilePath(suggestedFile) =>
        new FilePathEditor(accessor, this, suggestedFile) with Changed
      case Property.Label =>
        new Label(accessor) with Changed
    }
  }

  trait Changed { self: PropertyEditor[_] =>
    override def changed() {
      if (get.isDefined) {
        if (target.liveUpdate) {
          apply()
          EditPanel.this.changed()
        }
      }
      previewChanged(accessor.accessString, get)
    }
  }

  private def frame = org.nlogo.awt.Hierarchy.getFrame(this)

  override def syncTheme(): Unit = {
    propertyEditors.foreach(_.syncTheme())
  }
}
