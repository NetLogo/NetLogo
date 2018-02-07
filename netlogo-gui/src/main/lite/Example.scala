package org.nlogo.lite

import org.nlogo.workspace.ModelsLibrary
import scala.App

/* This class is an example of how to embed a NetLogo panel in your application */
object Example extends App {
  val frame = new javax.swing.JFrame
  val comp = new InterfaceComponent(frame)
  wait {
    frame.setSize(1000, 700)
    frame.add(comp)
    frame.setVisible(true)
    comp.open(ModelsLibrary.modelsRoot + "/Sample Models/Earth Science/Fire.nlogo")
  }
  comp.command("set density 62")
  comp.command("random-seed 0")
  comp.command("setup")
  comp.command("repeat 50 [ go ]")
  println(comp.report("burned-trees"))

  def wait(block: => Unit): Unit = {
    java.awt.EventQueue.invokeAndWait(new Runnable() {
      def run(): Unit = {
        block
      }
    })
  }
}
