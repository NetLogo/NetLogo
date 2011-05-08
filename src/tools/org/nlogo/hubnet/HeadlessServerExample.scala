package org.nlogo.hubnet

import org.nlogo.headless.HeadlessWorkspace

object HeadlessServerExample {

  def main(args: Array[String]) {
    val workspace = org.nlogo.headless.HeadlessWorkspace.newInstance
    val commandLine = new CommandLineThread(workspace)
    workspace.open("models/HubNet Activities/Code Examples/Template.nlogo")
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
        queuedCommands put readLine()
      }
    }
    def runAll() {
      import org.nlogo.util.JCL._
      queuedCommands.foreach { c =>
        println("executing command: " + c)
        try workspace.command(c)
        catch { case e => e.printStackTrace() }
        print("enter command> ")
      }
      queuedCommands.clear()
    }
  }
}
