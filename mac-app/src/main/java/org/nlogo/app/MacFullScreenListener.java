// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app;

import java.awt.Window;

import com.apple.eawt.FullScreenListener;
import com.apple.eawt.AppEvent.FullScreenEvent;
import com.apple.eawt.FullScreenUtilities;

import org.nlogo.app.tools.AgentMonitorManager;

public final class MacFullScreenListener {
  private MacFullScreenListener() {}

  // This is a workaround for an issue with agent monitor windows on macOS.  After
  // switching those windows to be plain `JDialog` instances that do not have a parent
  // declared, an issue popped up with them and full screen mode (#1989).  This is
  // a workaround that hides the windows when full screen mode starts and restores them
  // once it's done so they don't wind up "stuck" in their pre-full screen locations.
  // Jeremy B December 2021
  public static void addFullScreenListener(Window window, AgentMonitorManager manager) {
    final FullScreenListener listener = new FullScreenListener() {
      private boolean shouldShow = false;
      public void windowEnteringFullScreen(final FullScreenEvent e) {
        shouldShow = manager.areAnyVisible();
        manager.hideAll();
      }

      public void windowEnteredFullScreen(final FullScreenEvent e) {
        if (shouldShow) {
          manager.showAll();
        }
      }

      public void windowExitingFullScreen(final FullScreenEvent e) {}
      public void windowExitedFullScreen(final FullScreenEvent e) {}
    };

    FullScreenUtilities.addFullScreenListenerTo(window, listener);
  }
}
