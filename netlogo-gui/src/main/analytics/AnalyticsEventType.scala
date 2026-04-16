// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

// Add only to the end!  --Jason B. (4/16/26)
enum AnalyticsEventType {
  case
    AppStart
  , AppExit
  , PreferenceChange
  , SDMOpen
  , BehaviorSpaceOpen
  , BehaviorSpaceRun
  , Open3DView
  , TurtleShapeEditorOpen
  , TurtleShapeEdit
  , LinkShapeEditorOpen
  , LinkShapeEdit
  , ColorPickerOpen
  , HubNetEditorOpen
  , HubNetClientOpen
  , GlobalsMonitorOpen
  , TurtleMonitorOpen
  , PatchMonitorOpen
  , LinkMonitorOpen
  , ModelCodeHash
  , PrimitiveUsage
  , KeywordUsage
  , IncludeExtension
  , LoadOldSizeWidgets
  , ModelingCommonsOpen
  , ModelingCommonsUpload
  , SaveAsNetLogoWeb
  , PreviewCommandsOpen
  , AnnouncementClicked
}

