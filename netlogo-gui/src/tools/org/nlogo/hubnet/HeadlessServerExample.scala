// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet

import org.nlogo.headless.HeadlessWorkspace

object HeadlessServerExample {

  def main(args: Array[String]): Unit = {
    val workspace = HeadlessWorkspace.newInstance
    val commandLine = new CommandLineThread(workspace)
    workspace.open("models/HubNet Activities/Code Examples/Template.nlogox")
    workspace.command("hubnet-reset")
    workspace.command("setup")
    while(true) {
      workspace.command("go")
      commandLine.runAll()
    }
    workspace.dispose()
  }

  class CommandLineThread(workspace: HeadlessWorkspace) extends Thread {
    val queuedCommands = new java.util.concurrent.ArrayBlockingQueue[String](10)
    start()
    override def run {
      print("enter command> ")
      while(true){
        queuedCommands put scala.io.StdIn.readLine()
      }
    }
    def runAll(): Unit = {
      import scala.jdk.CollectionConverters.ListHasAsScala
      queuedCommands.asScala.foreach { c =>
        println("executing command: " + c)
        try workspace.command(c)
        catch { case e: Throwable => e.printStackTrace() }
        print("enter command> ")
      }
      queuedCommands.clear()
    }
  }
}
