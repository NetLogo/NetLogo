// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Dimension, Font, Frame, Graphics, GridBagConstraints, GridBagLayout, Insets,
                  LinearGradientPaint }
import java.awt.event.{ ActionEvent, ActionListener, FocusEvent, FocusListener, KeyEvent, MouseEvent, MouseAdapter,
                        WindowAdapter, WindowEvent }
import javax.swing.{ AbstractAction, JButton, JDialog, JLabel, JPanel, ScrollPaneConstants }
import javax.swing.KeyStroke.getKeyStroke
import javax.swing.text.EditorKit

import org.nlogo.api.{ CompilerServices, Dump, Editable, Exceptions, LogoException, Options, ValueConstraint }
import org.nlogo.api.Approximate.approximate
import org.nlogo.api.Color.{ getColor, getColorNameByIndex, modulateDouble }
import org.nlogo.agent.InputBoxConstraint
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.awt.{ Hierarchy, Positioning }
import org.nlogo.core.{ BoxedValue, CompilerException, I18N, InputBox => CoreInputBox, NumericInput, StringInput }
import org.nlogo.editor.AbstractEditorArea
import org.nlogo.swing.{ Button, ButtonPanel, OptionPane, RoundedBorderPanel, ScrollPane, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

object InputBox {
  val MinWidth  = 50
  val MinHeight = 60
}

abstract class InputBox(textArea: AbstractEditorArea, editDialogTextArea: AbstractEditorArea,
                        compiler: CompilerServices, nextComponent: Component)
  extends SingleErrorWidget with Editable with Events.InputBoxLoseFocusEvent.Handler {
  type WidgetModel = CoreInputBox

  import InputBox._

  private var hover = false

  protected class ColorButton extends JButton with RoundedBorderPanel with ThemeSync {
    private var color = Color.black

    setBorder(null)
    setFont(getFont.deriveFont(9.0f))

    addActionListener(new SelectColorActionListener)

    addMouseListener(new MouseAdapter {
      override def mouseEntered(e: MouseEvent): Unit = {
        if (isVisible) {
          hover = true

          getParent.repaint()
        }
      }

      override def mouseExited(e: MouseEvent): Unit = {
        if (isVisible && !contains(e.getPoint)) {
          hover = false

          getParent.repaint()
        }
      }
    })

    override def paintComponent(g: Graphics): Unit = {
      setBackgroundColor(color)
      setDiameter(6 * zoomFactor)

      super.paintComponent(g)
    }

    def setColor(color: Color): Unit = {
      this.color = color
    }

    override def syncTheme(): Unit = {
      setBorderColor(InterfaceColors.inputBorder)
    }
  }

  protected class InputScrollPane(textArea: AbstractEditorArea) extends JPanel with RoundedBorderPanel with ThemeSync {
    val scrollPane = new ScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
      setBorder(null)
    }

    textArea.setOpaque(false)
    textArea.setBackground(InterfaceColors.Transparent)

    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.weightx = 1
    c.weighty = 1
    c.fill = GridBagConstraints.BOTH
    c.insets = new Insets(3, 3, 3, 3)

    add(scrollPane, c)

    addMouseListener(new MouseAdapter {
      override def mouseEntered(e: MouseEvent): Unit = {
        if (isVisible) {
          hover = true

          getParent.repaint()
        }
      }

      override def mouseExited(e: MouseEvent): Unit = {
        if (isVisible && !contains(e.getPoint)) {
          hover = false

          getParent.repaint()
        }
      }
    })

    override def paintComponent(g: Graphics): Unit = {
      // this mostly fixes some weird horizontal scrollbar issues (Isaac B 8/7/24)
      textArea.setSize(scrollPane.getWidth - 10, scrollPane.getHeight)

      setDiameter(6 * zoomFactor)

      super.paintComponent(g)
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.displayAreaBackground)
      setBorderColor(InterfaceColors.inputBorder)

      textArea.setForeground(InterfaceColors.displayAreaText)
      textArea.setCaretColor(InterfaceColors.displayAreaText)

      scrollPane.setBackground(InterfaceColors.displayAreaBackground)
    }
  }

  /// be editable
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.input")
  protected val widgetLabel = new JLabel(I18N.gui.get("edit.input.previewName"))
  protected var dialog: InputDialog = null
  private var _hasFocus = false
  // grab the current editor kit from the editor area
  // everyone but string will use it but we need to
  // keep it around so we know what to set it to.
  private val codeEditorKit: EditorKit = textArea.getEditorKit
  protected var inputType: InputType = new StringInputType()
  private val constraint: InputBoxConstraint = new InputBoxConstraint(inputType.baseName, inputType.defaultValue)
  protected val changeButton = new Button("Change", () => {
    if (dialog == null || !dialog.isVisible) {
      editing = true
      dialog = new InputDialog(Hierarchy.getFrame(InputBox.this), name, `inputType`, editDialogTextArea)
      dialog.setVisible(true)
      editDialogTextArea.setText(textArea.getText)
      editDialogTextArea.selectAll()
    }
  })
  protected val colorSwatch = new ColorButton

  private val scroller = new InputScrollPane(textArea)

  // most of the time text and value will be exactly the same
  // however, for numbers there will be a Double rather than
  // a String in the value field.  ev 8/13/06
  protected var text = ""
  protected var oldText = ""
  protected var value: Option[AnyRef] = Option.empty[AnyRef]
  def valueText = text
  def valueObject = value.orNull
  def valueObject(value: AnyRef): Unit = {valueObject(value, false)}
  def valueObject(value: Any, raiseEvent: Boolean): Unit = {
    oldText = text
    text = Dump.logoObject(toAnyRef(value))
    this.value = Option(toAnyRef(value))
    if (text != textArea.getText) textArea.setText(text)
  }

  protected def toAnyRef(value: Any): AnyRef = {
    value match {
      case d: Double => Double.box(d)
      case a: AnyRef => a
    }
  }

  protected def inputText(input: Object): Unit = {
    if (input != null) valueObject(input, true)
  }

  // multiline property
  protected var multiline = false
  def multiline(multiline: Boolean): Unit = {
    this.multiline = multiline
    changeButton.setVisible(inputType.changeVisible)
    editing = false

    // Multiline determines whether what keybindings are present, so we set bindings here.
    // - BCH 05/13/2018
    textArea.getInputMap.put(getKeyStroke(KeyEvent.VK_ESCAPE, 0), new CancelAction())
    textArea.getInputMap.put(getKeyStroke(KeyEvent.VK_ENTER, 0),
      if(multiline) null else new TransferFocusAction())
    textArea.getInputMap.put(getKeyStroke(KeyEvent.VK_TAB, 0), new TransferFocusAction())
  }

  def updateKeyBindings(): Unit = {
  }

  var errorShowing = false
  var typeOptions = new org.nlogo.api.Options[InputType]()
  var name = ""
  var nameChanged = false

  /// name needs a wrapper because we don't want to recompile until editFinished()
  def name(name: String): Unit = {this.name(name, true)}
  def name(name: String, sendEvent: Boolean): Unit = {
    this.name = name
    // I don't think anyone ever uses the display name, but let's keep it in sync
    // with the real name, just in case - ST 6/3/02
    displayName(name)
    widgetLabel.setText(name)
  }

  def nameWrapper = name
  def nameWrapper(name: String): Unit = {
    nameChanged = name != this.name || nameChanged
    this.name(name, false)
  }

  protected var _oldSize = false

  def oldSize: Boolean = _oldSize
  def oldSize_=(value: Boolean): Unit = {
    _oldSize = value
    initGUI()
    repaint()
  }

  protected var editing = false
  protected def stopEdit(): Unit = {
    editing = false
    transferFocus()
    nextComponent.requestFocus()
  }

  InputType.addTypeOptions(typeOptions)
  typeOptions.selectValue(inputType)
  textArea.setEditorKit(inputType.getEditorKit)
  textArea.setFont(inputType.getFont)
  textArea.enableBracketMatcher(inputType.enableBracketMatcher)

  multiline(multiline)

  setLayout(new GridBagLayout)

  initGUI()

  colorSwatch.setVisible(false)

  // focus listener for in place editing
  textArea.addFocusListener(
    new FocusListener() {
      def focusGained(e: FocusEvent): Unit = {
        _hasFocus = true
        editing = true
      }
      def focusLost(e: FocusEvent): Unit = {
        _hasFocus = false
        if (editing) {
          try inputText(inputType.readValue(InputBox.this.textArea.getText))
          catch {
            case ex@(_:LogoException|_:CompilerException|_:ValueConstraint.Violation) =>
              inputText(oldText)
          }
          editing = false
        }
      }
    })

  // this allows the layout to be reorganized when the oldSize property changes (Isaac B 2/17/25)
  private def initGUI(): Unit = {
    removeAll()

    val c = new GridBagConstraints

    c.gridx = 0
    c.gridy = 0
    c.weightx = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.insets =
      if (_oldSize) {
        new Insets(3, 6, 6, 6)
      } else {
        new Insets(6, 12, 6, 12)
      }

    add(widgetLabel, c)

    c.gridx = 1
    c.weightx = 0
    c.anchor = GridBagConstraints.EAST
    c.insets =
      if (_oldSize) {
        new Insets(3, 0, 6, 6)
      } else {
        new Insets(6, 0, 6, 12)
      }

    add(changeButton, c)

    c.gridx = 0
    c.gridy += 1
    c.weighty = 1
    c.weightx = 1
    c.gridwidth = GridBagConstraints.REMAINDER
    c.fill = GridBagConstraints.BOTH
    c.anchor = GridBagConstraints.WEST
    c.insets =
      if (_oldSize) {
        new Insets(0, 6, 6, 6)
      } else {
        new Insets(0, 12, 6, 12)
      }

    add(scroller, c)
    add(colorSwatch, c)
  }

  override def paintComponent(g: Graphics) = {
    setBackgroundColor(InterfaceColors.inputBackground)

    widgetLabel.setForeground(InterfaceColors.widgetText)

    super.paintComponent(g)

    if (widgetLabel.getPreferredSize.width > widgetLabel.getWidth) {
      widgetLabel.setToolTipText(widgetLabel.getText)
    } else {
      widgetLabel.setToolTipText(null)
    }

    if (hover) {
      val g2d = Utils.initGraphics2D(g)

      if (colorSwatch.isVisible) {
        g2d.setPaint(new LinearGradientPaint(colorSwatch.getX.toFloat, colorSwatch.getY + 3, colorSwatch.getX.toFloat,
                                            colorSwatch.getY + colorSwatch.getHeight + 3, Array(0f, 1f),
                                            Array(InterfaceColors.widgetHoverShadow, InterfaceColors.Transparent)))
        g2d.fillRoundRect(colorSwatch.getX, colorSwatch.getY + 3, colorSwatch.getWidth, colorSwatch.getHeight, 6, 6)
      } else {
        g2d.setPaint(new LinearGradientPaint(scroller.getX.toFloat, scroller.getY + 3, scroller.getX.toFloat,
                                            scroller.getY + scroller.getHeight + 3, Array(0f, 1f),
                                            Array(InterfaceColors.widgetHoverShadow, InterfaceColors.Transparent)))
        g2d.fillRoundRect(scroller.getX, scroller.getY + 3, scroller.getWidth, scroller.getHeight, 6, 6)
      }
    }
  }

  override def syncTheme(): Unit = {
    colorSwatch.syncTheme()
    scroller.syncTheme()
    changeButton.syncTheme()

    if (dialog != null)
      dialog.syncTheme()
  }

  private class SelectColorActionListener extends ActionListener {
    def actionPerformed(e: ActionEvent): Unit = {
      val colorDialog = new ColorDialog(org.nlogo.awt.Hierarchy.getFrame(InputBox.this), true)
      valueObject(colorDialog.showInputBoxDialog(
        if (value.exists(_.isInstanceOf[Double])) {
          modulateDouble(value.get.asInstanceOf[Double])
        } else {
          0d
        }
      ).asInstanceOf[AnyRef], true)
    }
  }

  private def showError(ex: Exception): Unit = {
    val frame = org.nlogo.awt.Hierarchy.getFrame(this)
    if (frame != null) {
      var msg = ex.getMessage
      if (msg.startsWith("REPORT expected 1 input."))
        msg = I18N.gui.get("edit.input.invalid.message")
      new OptionPane(frame, I18N.gui.getN("edit.input.invalid.title", inputType), msg, OptionPane.Options.Ok,
                     OptionPane.Icons.Error)
    }
  }

  def handle(e:Events.InputBoxLoseFocusEvent){
    if(_hasFocus) transferFocus()
  }

  private class CancelAction extends AbstractAction {
    def actionPerformed(e:ActionEvent){
      textArea.setText(text)
      stopEdit()
    }
  }

  private class TransferFocusAction extends AbstractAction {
    def actionPerformed(e:ActionEvent): Unit = {
      transferFocus()
      nextComponent.requestFocus()
    }
  }

  override def updateConstraints(): Unit = {
    if (name.length > 0) new org.nlogo.window.Events.AddInputBoxConstraintEvent(name, constraint).raise(this)
  }

  override def editFinished() = {
    super.editFinished()
    name(this.name, nameChanged)
    nameChanged = false
    try inputText(constraint.coerceValue(inputType.readValue(text)))
    catch {
      case ex: LogoException => throw new IllegalStateException(ex)
      case ex@(_: CompilerException | _: ValueConstraint.Violation) =>
        showError(ex.asInstanceOf[Exception])
    }
    true
  }

  def typeOptions(typeOptions: org.nlogo.api.Options[InputType]): Unit = {
    this.typeOptions = typeOptions
    if (inputType.displayName != typeOptions.chosenValue.displayName) {
      inputType = typeOptions.chosenValue
      constraint.setType(inputType.baseName, inputType.defaultValue)
      // if the current value doesn't comply with the new constraint
      // set it to a default value ev 12/14/06
      try constraint.assertConstraint(toAnyRef(value))
      catch {
        case v: ValueConstraint.Violation => valueObject(inputType.defaultValue, true)
      }
      textArea.setEditorKit(inputType.getEditorKit)
      textArea.setFont(inputType.getFont)
      textArea.enableBracketMatcher(inputType.enableBracketMatcher)
      changeButton.setVisible(inputType.changeVisible)
      inputType.colorPanel(colorSwatch)
    }
    scroller.scrollPane.setHorizontalScrollBarPolicy(
      if (inputType.multiline) {
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
      } else {
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
      })
    multiline(inputType.multiline)
    if (dialog != null) {
      dialog.dispose()
      dialog = null
    }
  }

  override def getMinimumSize = {
    if (_oldSize) {
      new Dimension(MinWidth, MinHeight)
    } else {
      new Dimension(100, 60)
    }
  }

  override def getPreferredSize = {
    if (_oldSize) {
      val result = super.getPreferredSize
      val insets = getInsets
      // add 4 because apparently we need a few extra pixels to make sure
      // that we don't get a horizontal scroll bar at the default size. ev 9/28/06
      result.width =
              textArea.getPreferredSize.width + insets.left + insets.right +
              textArea.getInsets.right + textArea.getInsets.left + 4
      new Dimension(MinWidth.max(result.width), MinHeight.max(result.height))
    } else {
      new Dimension(250, 60)
    }
  }

  override def load(model: WidgetModel): AnyRef = {
    name(model.varName)
    multiline(model.multiline)

    def setType(i: BoxedValue): Unit = {
      this.inputType = InputType.create(i)
      textArea.setEditorKit(this.inputType.getEditorKit)
      textArea.setFont(this.inputType.getFont)
      textArea.enableBracketMatcher(this.inputType.enableBracketMatcher)
      typeOptions.selectByName(inputType.displayName)
      constraint.setType(this.inputType.baseName, this.inputType.defaultValue)
      changeButton.setVisible(this.inputType.changeVisible)
    }

    def setValue(i: BoxedValue): Unit = {
      i match {
        case NumericInput(value, _) => valueObject(value, true)
        case StringInput(value, _, _) => valueObject(value, true)
      }
    }

    setType(model.boxedValue)

    try setValue(model.boxedValue)
    catch{
      case e@(_:CompilerException|_:ValueConstraint.Violation|_:LogoException) =>
        setValue(model.boxedValue.default)
    }
    oldSize = model.oldSize
    setSize(model.width, model.height)
    this
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    val boxedValue = inputType.boxValue(text)
    CoreInputBox(
      x          = b.x, y = b.y, width = b.width, height = b.height,
      oldSize = _oldSize,
      variable   = name.potentiallyEmptyStringToOption,
      boxedValue = boxedValue)
  }

  override def exportable = true
  override def getDefaultExportName = "export.txt"
  override def hasContextMenu = true
  override def getMaximumSize = null

  protected class InputDialog(parent: Frame, title: String, inputType: InputType, textArea: AbstractEditorArea)
    extends JDialog(parent, title) with ThemeSync {

    private val label = new JLabel(inputType.toString)
    private val scrollPane = new InputScrollPane(textArea)

    private val okButton = new Button(I18N.gui.get("common.buttons.ok"), () => {
      try{
        val value = inputType.readValue(textArea.getText)
        inputText(value)
        editing = false
        dispose()
        dialog = null
      }
      catch {
        case ex@(_:LogoException | _:CompilerException | _:ValueConstraint.Violation) =>
          showError(ex.asInstanceOf[Exception])
      }
    })

    private val cancelAction = new AbstractAction(I18N.gui.get("common.buttons.cancel")) {
      def actionPerformed(e: ActionEvent): Unit = {
        dispose()
        editing = false
        dialog = null
      }
    }

    private val cancelButton = new Button(cancelAction)

    private val applyButton = new Button(I18N.gui.get("common.buttons.apply"), () => {
      try inputText(inputType.readValue(textArea.getText))
      catch {
        case ex@(_:LogoException | _:CompilerException | _:ValueConstraint.Violation) =>
          showError(ex.asInstanceOf[Exception])
      }
    })

    locally {
      setResizable(true)
      textArea.setEditorKit(inputType.getEditorKit)
      textArea.setFont(inputType.getFont)
      textArea.enableBracketMatcher(inputType.enableBracketMatcher)

      getContentPane.setLayout(new GridBagLayout)

      val c = new GridBagConstraints

      c.insets = new Insets(3, 3, 3, 3)
      c.gridwidth = GridBagConstraints.REMAINDER
      c.anchor = GridBagConstraints.WEST

      getContentPane.add(label, c)

      c.weightx = 1
      c.weighty = 1
      c.fill = GridBagConstraints.BOTH

      getContentPane.add(scrollPane, c)

      c.gridy = 2
      c.anchor = GridBagConstraints.EAST
      c.weightx = 0
      c.weighty = 0

      getContentPane.add(new ButtonPanel(Array(okButton, applyButton, cancelButton)), c)

      Utils.addEscKeyAction(this, cancelAction)

      pack()
      Positioning.center(this, parent)
      addWindowListener(new WindowAdapter() {
        override def windowClosing(e: WindowEvent): Unit = {
          dispose()
          editing = false
          dialog = null
        }
      })
    }

    syncTheme()

    def setText(text: String): Unit = {
      textArea.setText(text)
      textArea.selectAll()
    }

    override def syncTheme(): Unit = {
      getContentPane.setBackground(InterfaceColors.dialogBackground)

      label.setForeground(InterfaceColors.dialogText)

      textArea.setBackground(InterfaceColors.textAreaBackground)
      textArea.setForeground(InterfaceColors.textAreaText)
      textArea.setCaretColor(InterfaceColors.textAreaText)

      scrollPane.syncTheme()

      okButton.syncTheme()
      applyButton.syncTheme()
      cancelButton.syncTheme()
    }
  }

  object InputType {
    def create(boxedValue: BoxedValue) =
      boxedValue match {
        case NumericInput(num,   CoreInputBox.NumberLabel)      =>
          new NumberInputType(codeEditorKit)
        case NumericInput(color, CoreInputBox.ColorLabel)       =>
          new ColorInputType(codeEditorKit)
        case StringInput(cmd,    CoreInputBox.CommandLabel, _)  =>
          new CommandInputType(codeEditorKit)
        case StringInput(rep,    CoreInputBox.ReporterLabel, _) =>
          new ReporterInputType(codeEditorKit)
        case StringInput(str,    _, _)   =>
          new StringInputType()
      }

    def addTypeOptions(options:Options[InputType]){
      BoxedValue.Defaults.map { tpe =>
        val t = create(tpe)
        options.addOption(t.displayName, t)
      }
    }
  }

  case class InputType(baseName: String, i18nKey: String, editorKit: EditorKit, font: Font) {
    def defaultValue: AnyRef = ""
    def multiline = InputBox.this.multiline
    def multiline(newMultiline: Boolean): Unit = {InputBox.this.multiline(newMultiline)}
    override def toString = displayName
    def saveName = baseName
    def displayName = I18N.gui.get("edit.input.type." + i18nKey)
    def getEditorKit = editorKit
    def getFont = font
    def colorPanel(panel: ColorButton): Unit = {
      panel.setVisible(false)
      scroller.setVisible(true)
    }
    @throws(classOf[ValueConstraint.Violation])
    @throws(classOf[LogoException])
    @throws(classOf[CompilerException])
    def readValue(text: String): Object = {
      constraint.assertConstraint(text)
      return text
    }
    def changeVisible = multiline
    def enableMultiline = true
    def enableBracketMatcher = false
    override def equals(a:Any) = { a match {
      case it@InputType(bn, _, _, _) => bn == baseName
      case _ => false
    }}
    def boxValue(text: String): BoxedValue =
      StringInput(text, StringInput.StringLabel, multiline)
  }

  private class StringInputType extends InputType(
    "String", "string",
    textArea.getEditorKitForContentType("String"),
    javax.swing.UIManager.getFont("Label.font").deriveFont(12.0f)){}

  def plainFont = new Font(platformMonospacedFont, Font.PLAIN, 12)

  private class ReporterInputType(kit: EditorKit) extends InputType("String (reporter)", "string.reporter", kit, plainFont) {
    override def defaultValue = "0"
    override def enableBracketMatcher = true
    @throws(classOf[ValueConstraint.Violation])
    @throws(classOf[CompilerException])
    override def readValue(text: String) = {
      constraint.assertConstraint(text)
      Exceptions.ignoring(classOf[CompilerException]) {
        compiler.checkReporterSyntax(text)
      }
      text
    }

    override def boxValue(text: String): BoxedValue =
      StringInput(text, StringInput.ReporterLabel, multiline)
  }

  private class CommandInputType(kit: EditorKit) extends InputType("String (commands)", "string.commands", kit, plainFont) {
    override def enableBracketMatcher = true
    @throws(classOf[ValueConstraint.Violation])
    @throws(classOf[CompilerException])
    override def readValue(text: String) = {
      constraint.assertConstraint(text)
      Exceptions.ignoring(classOf[CompilerException]) {
        compiler.checkCommandSyntax(text)
      }
      text
    }

    override def boxValue(text: String): BoxedValue =
      StringInput(text, StringInput.CommandLabel, multiline)
  }

  private class NumberInputType(kit: EditorKit) extends InputType("Number", "number", kit, plainFont) {
    @throws(classOf[CompilerException])
    override def readValue(text: String) = compiler.readNumberFromString(text)
    override def boxValue(text: String): BoxedValue = {
      val num = compiler.readNumberFromString(text).asInstanceOf[java.lang.Double]
      NumericInput(num.doubleValue, NumericInput.NumberLabel)
    }
    override def multiline = false
    override def enableMultiline = false
    override def defaultValue = org.nlogo.agent.World.Zero
  }

  private class ColorInputType(kit: EditorKit) extends InputType("Color", "color", kit, plainFont) {
    @throws(classOf[CompilerException])
    override def readValue(text: String) =
      compiler.readNumberFromString(text)

    override def boxValue(text: String): BoxedValue = {
      val num = compiler.readNumberFromString(text).asInstanceOf[java.lang.Double]
      NumericInput(num.doubleValue, NumericInput.ColorLabel)
    }

    override def colorPanel(panel: ColorButton): Unit = {
      panel.setVisible(true)
      scroller.setVisible(false)

      val (colorval, c) =
        if (value.exists(_.isInstanceOf[Double])) {
          val cv = modulateDouble(value.get.asInstanceOf[Double]): java.lang.Double
          (cv, getColor(cv))
        }
        else (0d: java.lang.Double, Color.BLACK)

      panel.setColor(c)
      panel.setForeground(if ((colorval % 10) > 5) Color.BLACK else Color.WHITE)
      panel.setText(colorval match {
        // this logic is duplicated in ColorEditor; black and white are special cases
        case d: java.lang.Double if d.doubleValue == 0.0 => "0 (black)"
        case d: java.lang.Double if d.doubleValue == 9.9 => "9.9 (white)"
        case c =>
          val index = (c / 10).toInt
          val baseColor = index * 10 + 5
          Dump.number(c) + " (" + getColorNameByIndex(index) + (
            if (c > baseColor) {
              " + " + Dump.number(approximate(c - baseColor, 1))
            } else if (c < baseColor) {
              " - "
            } + Dump.number(approximate(baseColor - c, 1))
            else {
              ""
            }
          ) + ")"
      })
    }
    override def changeVisible = false
    override def enableMultiline = false
    override def defaultValue = org.nlogo.agent.World.Zero
  }
}
