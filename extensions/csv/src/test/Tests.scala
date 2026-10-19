// (C) Uri Wilensky. https://github.com/NetLogo/CSV-Extension

package org.nlogo.extensions.csv

import org.nlogo.headless.TestLanguage

class Tests extends TestLanguage(Seq(new java.io.File("tests.txt").getCanonicalFile))
