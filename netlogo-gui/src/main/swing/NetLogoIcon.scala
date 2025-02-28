// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Window

import org.nlogo.api.Version
import org.nlogo.awt.Images

trait NetLogoIcon extends Window {
  if (Version.is3D)
    setIconImage(Images.loadImageResource("/images/netlogo3d.png"))
  else
    setIconImage(Images.loadImageResource("/images/netlogo.png"))
}
