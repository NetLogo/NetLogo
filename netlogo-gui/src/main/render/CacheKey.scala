// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.render

import org.nlogo.api.Shape

private case class CacheKey(color: Int, angleIndex: Int, shape: Shape, size: Double)
