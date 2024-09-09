// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{Component, Insets, GridBagConstraints, Dimension, GridBagLayout, BorderLayout}

import javax.swing.{JLabel, JPanel, ToolTipManager}

import org.nlogo.core.{ CompilerException, I18N, LogoList, Nobody }
import org.nlogo.api.{ CompilerServices, Editable, ExternalResourceManager, Property }
import org.nlogo.editor.Colorizer
import org.nlogo.swing.OptionDialog
import org.nlogo.window.WidgetWrapperInterface

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

// This is the contents of an EditDialog, except for the buttons at the bottom (OK/Apply/Cancel).
class EditPanel(val target: Editable, val compiler: CompilerServices, colorizer: Colorizer,
                resourceManager: ExternalResourceManager, useTooltips: Boolean = false)
  extends JPanel {

  val oldDelay = ToolTipManager.sharedInstance.getDismissDelay()
  ToolTipManager.sharedInstance.setDismissDelay(30000)

  val liveUpdate =
    // OK, it's a big hack that we're hardcoding these next checks, but it doesn't seem worth the
    // effort for now to do it the right way - ST 12/16/01, 11/29/07
    !(target.isInstanceOf[org.nlogo.window.ChooserWidget] ||
      // Sliders: we were getting weirdness with the "value" property involving
      // out of bounds values, so rather than fool with it, the easiest
      // thing to do was just not live update - ST 11/29/07
      target.isInstanceOf[org.nlogo.window.SliderWidget] ||
      target.isInstanceOf[org.nlogo.window.WorldViewSettings] ||
      target.isInstanceOf[org.nlogo.window.PlotWidget])

  val propertyEditors = collection.mutable.ArrayBuffer[PropertyEditor[_]]()
  private var getsFirstFocus: PropertyEditor[_] = null
  var (originalSize, originalPreferredSize) = wrapperOption match {
    case Some(wrapper) => (wrapper.getSize, wrapper.getPreferredSize)
    case None => (null, null)
  }
  def init(): PropertyEditor[_] = {
    val properties = target.propertySet
    val layout = new GridBagLayout()
    setLayout(layout)
    getsFirstFocus = {
      import collection.JavaConverters._
      addProperties(this, properties.asScala, layout)
    }
    getsFirstFocus
  }
  def addProperties(editorPanel: JPanel, properties: Iterable[Property],
                    layout: GridBagLayout) = {
    var claimsFirstFocus: PropertyEditor[_] = null
    for(property <- properties) {
      val editor = getEditor(property, target, useTooltips && property.notes != null && property.notes.trim != "")
      val panel = new JPanel{
        setLayout(new BorderLayout)
        add(editor, BorderLayout.CENTER)
        if (property.notes != null && property.notes.trim != "")
          if (useTooltips)
            editor.setTooltip(property.notes)
          else
            add(new JLabel(property.notes){ setFont(getFont.deriveFont(9.0f)) }, BorderLayout.SOUTH)
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
      editor.setBackground(property.backgroundColor)
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
      val prefSize = wrapper.getPreferredSize
      // Some widgets have no max size, so we use an very large one
      val maxSize = Option(wrapper.getMaximumSize).getOrElse(new Dimension(10000, 10000))
      if (wrapper.isNew) {
        val currentSize = wrapper.getSize
        val gridSnap = wrapper.gridSnap
        if (prefSize.width != currentSize.width)
          prefSize.width = (prefSize.width / gridSnap) * gridSnap
        if (prefSize.height != currentSize.height)
          prefSize.height = (prefSize.height / gridSnap) * gridSnap
        wrapper.setSize(prefSize)
      }
      else if (originalPreferredSize != prefSize) {
        var width = maxSize.width min (prefSize.width max originalSize.width)
        var height = maxSize.height min (if (wrapper.verticallyResizable)
                                           prefSize.height max originalSize.height
                                         else prefSize.height)
        val currentSize = wrapper.getSize
        val gridSnap = wrapper.gridSnap
        if (width != currentSize.width)
          width = (width / gridSnap) * gridSnap
        if (prefSize.height != currentSize.height)
          height = (height / gridSnap) * gridSnap
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
          OptionDialog.showMessage(this,
            I18N.gui.get("edit.general.invalidSettings"),
            I18N.gui.getN("edit.general.invalidValue", editor.accessor.displayName),
            Array(I18N.gui.get("common.buttons.ok")))
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
                .asScala
                .filter(_.accessString == name)
                .map(_.name)
                .headOption
                .getOrElse(name)
                s"${displayName}: ${error}"
        }.mkString("\n", "\n", "")
      val invalidMessage = I18N.gui.getN("edit.general.invalidValues", allInvalidations)
      OptionDialog.showMessage(this,
        I18N.gui.get("edit.general.invalidSettings"),
        invalidMessage,
        Array(I18N.gui.get("common.buttons.ok")))
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
        new OptionsEditor[String](accessor, useTooltips) with Changed
      case Property.BigString =>
        new BigStringEditor(accessor, useTooltips) with Changed
      case Property.Boolean =>
        new BooleanEditor(accessor, useTooltips) with Changed
      case Property.MetricsBoolean =>
        new MetricsBooleanEditor(accessor, useTooltips, propertyEditors)
      case Property.Color =>
        new ColorEditor(accessor, useTooltips, frame) with Changed
      case Property.Commands =>
        new CodeEditor(accessor, useTooltips, colorizer, collapsible, collapseByDefault) with Changed
      case Property.Double =>
        new DoubleEditor(accessor, useTooltips) with Changed
      case Property.Error =>
        new RuntimeErrorDisplay(accessor, useTooltips) with Changed
      case Property.StrictlyPositiveDouble =>
        new DoubleEditor(accessor, useTooltips) with Changed
        { override def get = super.get.filter(_ > 0) }
      case Property.Identifier =>
        new StringEditor(accessor, useTooltips) with Changed
        { override def get = super.get.map(_.trim).filter(compiler.isValidIdentifier) }
      case Property.InputBoxOptions =>
        new InputBoxEditor(accessor, useTooltips) with Changed
      case Property.Integer =>
        new IntegerEditor(accessor, useTooltips) with Changed
      case Property.Key =>
        new KeyEditor(accessor, useTooltips) with Changed
      case Property.LogoListString =>
        new CodeEditor(accessor, useTooltips, colorizer, false, false) with Changed {
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
        new IntegerEditor(accessor, useTooltips) with Changed
        { override def get = super.get.filter(_ <= 0) }
      case Property.NonEmptyString =>
        new StringEditor(accessor, useTooltips) with Changed
        { override def get = super.get.filter(_.nonEmpty) }
      case Property.PlotOptions =>
        new OptionsEditor[org.nlogo.plot.Plot](accessor, useTooltips) with Changed
      case Property.PlotPens =>
        new PlotPensEditor(
          new PropertyAccessor[List[org.nlogo.plot.PlotPen]](r, property.name, property.accessString), useTooltips, colorizer)
      case Property.PositiveInteger =>
        new IntegerEditor(accessor, useTooltips) with Changed
        { override def get = super.get.filter(_ >= 0) }
      case Property.Reporter =>
        new CodeEditor(accessor, useTooltips, colorizer, collapsible, collapseByDefault) with Changed
        { override def get = super.get.map(_.trim).filter(_.nonEmpty) }
      case Property.ReporterOrEmpty =>
        new CodeEditor(accessor, useTooltips, colorizer, collapsible, collapseByDefault) with Changed
      case Property.ReporterLine =>
        new ReporterLineEditor(accessor, useTooltips, colorizer, property.optional) with Changed
      case Property.String =>
        new StringEditor(accessor, useTooltips) with Changed
      case Property.FilePath(suggestedFile) =>
        new FilePathEditor(accessor, useTooltips, this, suggestedFile) with Changed
      case Property.ResourcePath =>
        new ResourcePathEditor(accessor, useTooltips, this, resourceManager) with Changed
      case Property.Label =>
        new Label(accessor, useTooltips) with Changed
    }
  }

  trait Changed { self: PropertyEditor[_] =>
    override def changed() {
      if (get.isDefined) {
        if (liveUpdate) {
          apply()
          EditPanel.this.changed()
        }
        previewChanged(accessor.accessString, get)
      }
    }
  }

  private def frame = org.nlogo.awt.Hierarchy.getFrame(this)

}
