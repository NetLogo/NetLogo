// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// we use reference equality, not structural equality, to compare these, since multiple lets
// can have the same variable name but are not the same

// some lets are "synthetic" and don't have a name, so that's the null

case class Let(name: String = null)
