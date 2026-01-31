// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt._
import java.awt.event._
import javax.swing._
import javax.swing.event._
import scala.language.implicitConversions

object Implicits {
  implicit def thunk2windowAdapter[T](fn: () => T): java.awt.event.WindowAdapter = new WindowAdapter {
    override def windowClosing(e: WindowEvent) = fn()
  }
  implicit def JComboBoxToIterable[T](jcb: JComboBox[T]): IndexedSeq[T] =
    (0 until jcb.getItemCount).map(jcb.getItemAt)
  implicit def thunk2documentListener[T](fn: () => T): DocumentListener = thunk2documentListener(_ => fn())
  implicit def thunk2documentListener[T](fn: DocumentEvent => T): javax.swing.event.DocumentListener = new DocumentListener {
    def changedUpdate(e: DocumentEvent) = fn(e)
    def insertUpdate(e: DocumentEvent)  = fn(e)
    def removeUpdate(e: DocumentEvent)  = fn(e)
  }
  implicit def thunk2keyListener[T](fn: () => T): KeyListener = thunk2keyListener(_ => fn())
  implicit def thunk2keyListener[T](fn: KeyEvent => T): java.awt.event.KeyListener = new KeyListener {
    def keyReleased(e: KeyEvent) = fn(e)
    def keyTyped(e: KeyEvent)    = fn(e)
    def keyPressed(e: KeyEvent)  = fn(e)
  }

  implicit def EnrichComboBox[T](combo: JComboBox[T]): org.nlogo.swing.RichJComboBox[T] = RichJComboBox[T](combo)
  implicit def EnrichContainer(c: Container): org.nlogo.swing.RichComponent = new RichComponent(c)
}

class RichComponent(c:Container){
  def addAll(comps:Component*): Unit ={
    for(c2<-comps) c.add(c2)
  }
}

object RichAction{
  def apply(name:String)(f: ActionEvent => Unit): AbstractAction = new AbstractAction(name){
    def actionPerformed(e: ActionEvent): Unit = { f(e) }
  }
  def apply(f: ActionEvent => Unit): AbstractAction = new AbstractAction(){
    def actionPerformed(e: ActionEvent): Unit = { f(e) }
  }
}

case class RichJComboBox[T](combo: JComboBox[T]) {
  class PossibleSelection(t: T) {
    def becomesSelected(f: => Unit): Unit = {
      combo.addItemListener(new ItemListener {
        def itemStateChanged(e: ItemEvent): Unit = {
          if (e.getItem == t && e.getStateChange == ItemEvent.SELECTED) { f }
        }
      })
    }
  }
  def when(t: T) = new PossibleSelection(t)
  def containsItem(t:T) = {
    (0 until combo.getModel.getSize).toList.exists((i:Int) => { combo.getItemAt(i) == t })
  }
}
