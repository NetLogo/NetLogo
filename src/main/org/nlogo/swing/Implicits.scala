// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.event.DocumentListener
import javax.swing._
import java.awt.event._
import java.awt.{Container, Component}
import language.implicitConversions

object Implicits {
  implicit def thunk2runnable(fn: () => Unit): Runnable =
    new Runnable { def run() { fn() }}
  implicit def thunk2action(fn: () => Unit): Action =
    new AbstractAction() {
      override def actionPerformed(e: ActionEvent) { fn() } }
  implicit def thunk2actionListener[T](fn: () => T): ActionListener =
    new ActionListener() { override def actionPerformed(e: ActionEvent) { fn() } }
  implicit def thunk2WindowAdapter[T](fn: () => T): WindowAdapter =
    new WindowAdapter() { override def windowClosing(e: WindowEvent) {fn()} }
  implicit def JComboBoxToIterable[T](jcb: JComboBox[T]) =
    for(i <- 0 until jcb.getItemCount)
    yield jcb getItemAt i
  implicit def thunk2documentListener[T](fn: () => T): DocumentListener =
    new javax.swing.event.DocumentListener() {
      def changedUpdate(e: javax.swing.event.DocumentEvent) { fn() }
      def insertUpdate(e: javax.swing.event.DocumentEvent) { fn() }
      def removeUpdate(e: javax.swing.event.DocumentEvent) { fn() }
    }
  implicit def thunk2itemListener[T](fn: () => T): ItemListener =
    new java.awt.event.ItemListener() {
      override def itemStateChanged(e: java.awt.event.ItemEvent) { fn() } }

  implicit def EnrichComboBox[T](combo: JComboBox[T]) = RichJComboBox[T](combo)
  implicit def EnrichContainer(c:Container) = new RichComponent(c)
}

object RichJButton{
  def apply(name:String)(f: => Unit) = {
    new JButton(name){
      addActionListener(new ActionListener{
        def actionPerformed(e:ActionEvent) { f }
      })
    }
  }
  def apply(icon:ImageIcon)(f: => Unit) = {
    new JButton(icon){
      addActionListener(new ActionListener{
        def actionPerformed(e:ActionEvent) { f }
      })
    }
  }
  def apply(action:AbstractAction)(f: => Unit) = {
    new JButton(action){
      addActionListener(new ActionListener{
        def actionPerformed(e:ActionEvent) { f }
      })
    }
  }
}

class RichComponent(c:Container){
  def addAll(comps:Component*){
    for(c2<-comps) c.add(c2)
  }
}

object RichAction{
  def apply(name:String)(f: ActionEvent => Unit): AbstractAction = new AbstractAction(name){
    def actionPerformed(e: ActionEvent) { f(e) }
  }
  def apply(f: ActionEvent => Unit): AbstractAction = new AbstractAction(){
    def actionPerformed(e: ActionEvent) { f(e) }
  }
}

// open question
// can we use structural typing to add this method to anything with an addActionListener method?
object RichJMenuItem {
  def apply(name:String)(f: => Unit) = {
    new JMenuItem(name){
      addActionListener(new ActionListener{
        def actionPerformed(e:ActionEvent) { f }
      })
    }
  }
}

case class RichJComboBox[T](combo: JComboBox[T]) {
  class PossibleSelection(t: T) {
    def becomesSelected(f: => Unit) {
      combo.addItemListener(new ItemListener {
        def itemStateChanged(e: ItemEvent) {
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
