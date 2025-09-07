// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// this class provides information about which variety of headless a Workspace is. appGUI indicates whether or not
// the application was started in a GUI context (eg. App vs Main), whereas workspaceGUI indicates whether the
// Workspace itself is a GUI workspace (eg. GUIWorkspace vs HeadlessWorkspace). this makes it easier to tell whether
// certain GUI functionality will be available or not, for example when an extension needs to add menu options or
// show an error dialog, without needing the relevant classes to be present in the classpath. (Isaac B 9/3/25)
case class WorkspaceContext(appGUI: Boolean, workspaceGUI: Boolean)
