// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.LogoException
import org.nlogo.nvm.{EngineException,LabInterface,Workspace}

// This is used when running headless. - ST 3/3/09

class Lab(loader: ProtocolLoader)
  extends LabInterface
{
  var protocols: List[Protocol] = Nil
  def names = protocols.map(_.name)
  def load(s: String) =
    if(s.trim.isEmpty) Nil
    else protocols = loader.loadAll(s)
  def newWorker(protocolName: String) =
    protocols.find(_.name == protocolName) match {
      case None =>
        throw new IllegalArgumentException(
          "experiment '" + protocolName + "' not found")
      case Some(protocol) =>
        new Worker(protocol)
    }
  def newWorker(setupFile: java.io.File) =
    new Worker(loader.loadOne(setupFile))
  def newWorker(protocolName: String, setupFile: java.io.File) =
    new Worker(loader.loadOne(setupFile, protocolName))
  def run(settings: LabInterface.Settings, fn: ()=>Workspace) {
    import settings._
    // pool of workspaces, same size as thread pool
    val workspaces = (1 to threads).map(_ => fn.apply).toList
    val queue = new collection.mutable.Queue[Workspace]
    workspaces.foreach(queue.enqueue(_))
    try {
      queue.foreach(w => dims.foreach(w.setDimensions _))
      def modelDims = queue.head.world.getDimensions
      val worker =
        (setupFile, experiment) match {
          case (Some(file), Some(name)) => newWorker(name, file)
          case (Some(file), None) => newWorker(file)
          case (None, Some(name)) => newWorker(name)
          case (None, None) => throw new IllegalArgumentException
        }
      tableWriter.foreach(
        worker.addTableWriter(model, dims.getOrElse(modelDims), _))
      spreadsheetWriter.foreach(
        worker.addSpreadsheetWriter(model, dims.getOrElse(modelDims), _))
      worker.addListener(
        new LabInterface.ProgressListener {
          override def runCompleted(w: Workspace, runNumber: Int, step: Int) {
            queue.synchronized { queue.enqueue(w) }
          }
          override def runtimeError(w: Workspace, runNumber: Int, t: Throwable) {
            if (!suppressErrors)
              t match {
                case ee: EngineException =>
                  val msg = ee.context.buildRuntimeErrorMessage(ee.instruction, ee)
                  System.err.println("Run #" + runNumber + ", RUNTIME ERROR: " + msg)
                  ee.printStackTrace(System.err)
                case _: LogoException =>
                  System.err.println("Run #" + runNumber + ", RUNTIME ERROR: " + t.getMessage)
                  t.printStackTrace(System.err)
                case _ =>
                  System.err.println("Run #" + runNumber + ", JAVA EXCEPTION: " + t.getMessage)
                  t.printStackTrace(System.err)
              }
          } } )
      def nextWorkspace = queue.synchronized { queue.dequeue() }
      worker.run(workspaces.head, nextWorkspace _, threads)
    }
    finally { workspaces.foreach(_.dispose()) }
  }
}
