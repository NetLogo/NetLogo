// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.connection

object ConnectionTypes{
  val COMP_CONNECTION = "COMPUTER"
}

/*
 * The different roles that clients could have when logging in to a HubNet activity.
 * 1. Participant - regular clients (most often students).
 * 2. Controller - also known as teacher clients, or server-view clients. These clients
 *                 should see all the server controls, and should have the ability
 *                 to start/stop the activity, kick other clients, and so on.
 */
object ClientRole {
  val Participant = ClientRole("Participant")
  val Controller = ClientRole("Controller")
}

case class ClientRole(name:String)


