// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.{LogoException, LabProtocol, LabPostProcessorInputFormat}
import org.nlogo.nvm.{EngineException,LabInterface,Workspace}

// This is used when running headless. - ST 3/3/09

class Lab extends LabInterface {
  def newWorker(protocol: LabProtocol) =
    new Worker(protocol)
  def run(settings: LabInterface.Settings, worker: LabInterface.Worker, fn: () => Workspace): Unit = {
    import settings._
    // pool of workspaces, same size as thread pool
    // unless there are fewer runs than threads (Isaac B 6/27/25)
    val workspaces = (1 to threads.min(worker.protocol.countRuns)).map(_ => fn.apply()).toList
    val queue = new collection.mutable.Queue[Workspace]
    workspaces.foreach(queue.enqueue)
    try {
      queue.foreach(w => dims.foreach(w.setDimensions))
      def modelDims = queue.head.world.getDimensions
      tableWriter.foreach(
        worker.addTableWriter(modelPath, dims.getOrElse(modelDims), _))
      spreadsheetWriter.foreach(
        worker.addSpreadsheetWriter(modelPath, dims.getOrElse(modelDims), _))
      statsWriter.foreach(x =>
        worker.addStatsWriter(modelPath, dims.getOrElse(modelDims), x._1,
          {
            if (tableWriter != None) LabPostProcessorInputFormat.Table(x._2)
            else LabPostProcessorInputFormat.Spreadsheet(x._2)
          }
        )
      )
      if (tableWriter.isDefined) {
        listsWriter.foreach(x =>
          worker.addListsWriter(modelPath, dims.getOrElse(modelDims), x._1,
          LabPostProcessorInputFormat.Table(x._2)))
      }
      else {
        listsWriter.foreach(x =>
          worker.addListsWriter(modelPath, dims.getOrElse(modelDims), x._1,
          LabPostProcessorInputFormat.Spreadsheet(x._2)))
      }
      worker.addListener(
        new LabInterface.ProgressListener {
          override def runCompleted(w: Workspace, runNumber: Int, step: Int): Unit = {
            queue.synchronized { queue.enqueue(w) }
          }
          override def runtimeError(w: Workspace, runNumber: Int, t: Throwable): Unit = {
            if (!suppressErrors)
              t match {
                case ee: EngineException =>
                  val msg = ee.runtimeErrorMessage
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
      def nextWorkspace = queue.synchronized { if (queue.isEmpty) null else queue.dequeue() }
      worker.run(workspaces.head, () => nextWorkspace, threads)
    }
    finally { workspaces.foreach(_.dispose()) }
  }
}
