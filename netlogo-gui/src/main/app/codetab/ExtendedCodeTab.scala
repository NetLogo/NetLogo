// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.app.{ Tabs }
import org.nlogo.workspace.AbstractWorkspace
// This class should be instantiated by extensions such as ls that
// want to create their own CodeTabs
// This class excludes the subclasses used internally by NetLogo code - MainCodeTab and TemporaryCodeTab
abstract class ExtendedCodeTab(workspace: AbstractWorkspace, tabs: Tabs)
  extends CodeTab(workspace, tabs) {
}
