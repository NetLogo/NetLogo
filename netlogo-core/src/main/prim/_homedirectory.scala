// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }

class _homedirectory extends Reporter {

  override def report(context: Context) =
    report_1(context)
  def report_1(context: Context) =
       try {
          System.getProperty("user.home")
        } catch {
          case ex: SecurityException =>
            throw new RuntimePrimitiveException(
              context, this, "NetLogo did not have permission to read system property for the user's home directory , please check that permissions are properly set.")
          case ex: Throwable =>
            throw new RuntimePrimitiveException(
              context, this, "NetLogo had a problem reading the user's home directory. " + ex.getMessage)

        }
}
