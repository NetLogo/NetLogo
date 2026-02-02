// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

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
}

