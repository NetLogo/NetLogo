// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.hubnet.mirroring.HubNetPlotPoint
import org.nlogo.workspace.AbstractWorkspaceScala
import org.nlogo.plot.Plot
import scala.collection.mutable.{ Publisher, Subscriber }
import org.nlogo.api.PlotAction, PlotAction._ 

// This should definitely be all redone.
// It sends messages out on every single little change.
// We really ought to only send bunched diffs the way we do with view updates.
// - JOSH COUGH 8/21/10, 12/28/10
class ServerPlotManager(workspace: AbstractWorkspaceScala, connectionManager: ConnectionManager,
                        plots: => List[Plot], currentPlot: => Plot) extends Subscriber[PlotAction, Publisher[PlotAction]] {
  private val narrowcastPlots = new collection.mutable.ListBuffer[String]()

  private def broadcastToClients(a: Any) { broadcastToClients(a, currentPlot.name) }
  private def broadcastToClients(a: Any, plotName: String) {
    if (broadcastEnabled && isBroadcast(plotName)) connectionManager.broadcastPlotControl(a,plotName)
  }
  private def broadcastWidgetToClients(a: Any, widgetName: String) {
    if (broadcastEnabled && isBroadcast(widgetName)) connectionManager.broadcast(widgetName, a)
  }

  private def sendToClient(clientId: String, a: Any) {
    if (workspace.hubNetRunning && isNarrowcast(currentPlot.name)) {
      connectionManager.sendPlotControl(clientId, a, currentPlot.name)
    }
  }

  private def broadcastEnabled = workspace.hubNetRunning && HubNetUtils.plotMirroring
  private def isNarrowcast(plotName: String) = narrowcastPlots.contains(plotName) && connectionManager.isValidTag(plotName)
  private def isBroadcast(plotName: String) = (!narrowcastPlots.contains(plotName)) && connectionManager.isValidTag(plotName)

  // called when plot mirroring is enabled to send all existing data
  // to all existing clients
  def broadcastPlots() {
    for(p<-plots; if (isBroadcast(p.name))) connectionManager.broadcast(p)
  }

  // called when a new client logs in and needs to be brought up to date
  def sendPlots(client:String) {
    if (broadcastEnabled) for(p<-plots; if (isBroadcast(p.name))) connectionManager.sendPlot(client, p)
  }

  /**
   * designates a particular plot as narrow-cast (by name).
   * @return true if the given plot exists, false otherwise.
   */
  def addNarrowcastPlot(plotName: String) = plots.find(_.name == plotName) match {
    case Some(plot) =>
      narrowcastPlots += plotName
      true
    case None => false
  }

  @Override def notify(pub: Publisher[PlotAction], action: PlotAction) : Unit = {
    action match {
      case PlotY(plotName, penName, y) => {
        val x = plots.find(_.name.equalsIgnoreCase(plotName)).flatMap(_.getPen(penName)).get.state.x
        broadcastToClients(penName, plotName)
        broadcastToClients(new HubNetPlotPoint(x, y), plotName)
      }
      case ClearPlot(plotName) => { broadcastToClients('c', plotName) }
      case PlotXY(plotName, penName, x, y) => {
        broadcastToClients(new HubNetPlotPoint(x, y), plotName)
      }
      case AutoPlot(plotName, on) => {
        if (on) broadcastToClients('n', plotName) else broadcastToClients('f', plotName)
      }
      case SetRange(plotName, isX, min, max) => {
        if(isX) {
          broadcastToClients(List('x', min, max), plotName)
        } else {
          broadcastToClients(List('y', min, max), plotName)
        }
      }
      case PenDown(plotName, penName, down) => {
        broadcastToClients(penName, plotName)
        broadcastToClients(down, plotName)
      }
      case HidePen(plotName, penName, hidden) => { throw new RuntimeException("HidePen not implemented for plot mirroring.") }
      case HardResetPen(plotName, penName) => {
        broadcastToClients(penName, plotName)
        broadcastToClients('r', plotName)
      }
      case SoftResetPen(plotName, penName) => {
        broadcastToClients(penName, plotName)
        broadcastToClients('p', plotName)
      }
      case SetPenInterval(plotName, penName, interval) => {
        broadcastToClients(penName, plotName)
        broadcastToClients(interval, plotName);
      }
      case SetPenMode(plotName, penName, mode) => {
        broadcastToClients(penName, plotName)
        broadcastToClients(mode.toShort, plotName);
      }
      case SetPenColor(plotName, penName, color) => {
        broadcastToClients(penName, plotName)
        broadcastToClients(color, plotName)
      }
      case CreateTemporaryPen(plotName, penName) => {
        // We swallow this case since if the pen isn't there on the client, it just makes it  FD 1/21/2015
      }

    }
  }
}
