// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import Version.is3D

object VersionHistory {

  def olderThan13pre1(version: String) =
    version.startsWith("NetLogo 1.0") ||
    version.startsWith("NetLogo 1.1") ||
    version.startsWith("NetLogo 1.2")

  def olderThan20alpha1(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.0pre")

  def olderThan20beta5(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.0pre") ||
    version.startsWith("NetLogo 2.0alpha") ||
    version.startsWith("NetLogo 2.0beta1") ||
    version.startsWith("NetLogo 2.0beta2") ||
    version.startsWith("NetLogo 2.0beta3") ||
    version.startsWith("NetLogo 2.0beta4")

  def olderThan21beta3(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.0") ||
    version.startsWith("NetLogo 2.1pre") ||
    version.startsWith("NetLogo 2.1beta1") ||
    version.startsWith("NetLogo 2.1beta2")

  def olderThan22pre3(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.0") ||
    version.startsWith("NetLogo 2.1") ||
    version.startsWith("NetLogo 2.2pre1") ||
    version.startsWith("NetLogo 2.2pre2")

  def olderThan30pre5(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0pre1") ||
    version.startsWith("NetLogo 3.0pre2") ||
    version.startsWith("NetLogo 3.0pre3") ||
    version.startsWith("NetLogo 3.0pre4")

  def olderThan30beta1(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0pre")

  def olderThan30beta2(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0pre") ||
    version.startsWith("NetLogo 3.0beta1")

  def olderThan30beta4(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0pre") ||
    version.startsWith("NetLogo 3.0beta1") ||
    version.startsWith("NetLogo 3.0beta2") ||
    version.startsWith("NetLogo 3.0beta3")

  def olderThan31pre1(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0")

  def olderThan31pre2(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0") ||
    version.startsWith("NetLogo 3.1pre1")

  def olderThan31beta5(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0") ||
    version.startsWith("NetLogo 3.1pre") ||
    version.startsWith("NetLogo 3.1beta1") ||
    version.startsWith("NetLogo 3.1beta2") ||
    version.startsWith("NetLogo 3.1beta3") ||
    version.startsWith("NetLogo 3.1beta4")

  def olderThan32pre2(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0") ||
    version.startsWith("NetLogo 3.1") ||
    version.startsWith("NetLogo 3.2pre1")

  def olderThan32pre3(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0") ||
    version.startsWith("NetLogo 3.1") ||
    version.startsWith("NetLogo 3.2pre1") ||
    version.startsWith("NetLogo 3.2pre2")

  def olderThan32pre4(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0") ||
    version.startsWith("NetLogo 3.1") ||
    version.startsWith("NetLogo 3.2pre1") ||
    version.startsWith("NetLogo 3.2pre2") ||
    version.startsWith("NetLogo 3.2pre3")

  def olderThan32pre5(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.0") ||
    version.startsWith("NetLogo 3.1") ||
    version.startsWith("NetLogo 3.2pre1") ||
    version.startsWith("NetLogo 3.2pre2") ||
    version.startsWith("NetLogo 3.2pre3") ||
    version.startsWith("NetLogo 3.2pre4")

  def olderThan40pre1(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.")

  def olderThan40pre3(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.") ||
    version.startsWith("NetLogo 4.0pre1") ||
    version.startsWith("NetLogo 4.0pre2")

  def olderThan40pre4(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.") ||
    version.startsWith("NetLogo 4.0pre1") ||
    version.startsWith("NetLogo 4.0pre2") ||
    version.startsWith("NetLogo 4.0pre3")

  def olderThan40alpha3(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.") ||
    version.startsWith("NetLogo 4.0pre") ||
    version.startsWith("NetLogo 4.0alpha1") ||
    version.startsWith("NetLogo 4.0alpha2")

  def olderThan40beta2(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.") ||
    version.startsWith("NetLogo 4.0pre") ||
    version.startsWith("NetLogo 4.0alpha") ||
    version.startsWith("NetLogo 4.0beta1")

  def olderThan40beta4(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.") ||
    version.startsWith("NetLogo 4.0pre") ||
    version.startsWith("NetLogo 4.0alpha") ||
    version.startsWith("NetLogo 4.0beta1") ||
    version.startsWith("NetLogo 4.0beta2") ||
    version.startsWith("NetLogo 4.0beta3")

  def olderThan40beta5(version: String) =
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.") ||
    version.startsWith("NetLogo 4.0pre") ||
    version.startsWith("NetLogo 4.0alpha") ||
    version.startsWith("NetLogo 4.0beta1") ||
    version.startsWith("NetLogo 4.0beta2") ||
    version.startsWith("NetLogo 4.0beta3") ||
    version.startsWith("NetLogo 4.0beta4")

  def olderThan42pre1(version: String) =
    version.contains("Preview") ||
    version.startsWith("NetLogo 1.") ||
    version.startsWith("NetLogo 2.") ||
    version.startsWith("NetLogo 3.") ||
    version.startsWith("NetLogo 4.0") ||
    remove3d(version).startsWith("NetLogo 4.1")

  def olderThan42pre2(version: String) =
    olderThan42pre1(version) ||
    remove3d(version) == "NetLogo 4.2pre1"

  def olderThan42pre5(version: String) =
    olderThan42pre2(version) ||
    remove3d(version) == "NetLogo 4.2pre2" ||
    remove3d(version) == "NetLogo 4.2pre3" ||
    remove3d(version) == "NetLogo 4.2pre4"

  def olderThan52(version: String) =
    olderThan42pre5(version) ||
    remove3d(version) == "NetLogo 5.0" ||
    remove3d(version) == "NetLogo 5.0.1" ||
    remove3d(version) == "NetLogo 5.0.2" ||
    remove3d(version) == "NetLogo 5.0.3" ||
    remove3d(version) == "NetLogo 5.0.4" ||
    remove3d(version) == "NetLogo 5.0.5" ||
    remove3d(version) == "NetLogo 5.1.0"

  def olderThan60(version: String) =
    olderThan42pre5(version) ||
    remove3d(version).startsWith("NetLogo 5")

  def remove3d(version: String) =
    version.replace("NetLogo 3D", "NetLogo")

  def olderThan3DPreview3(version: String) =
    is3D(version) &&
    (version.startsWith("NetLogo 3-D Preview 1") ||
     version.startsWith("NetLogo 3-D Preview 2"))

  def olderThan3DPreview4(version: String) =
    is3D(version) &&
    (version.startsWith("NetLogo 3-D Preview 1") ||
     version.startsWith("NetLogo 3-D Preview 2") ||
     version.startsWith("NetLogo 3D Preview 3"))

  def olderThan3DPreview5(version: String) =
    is3D(version) &&
    (version.startsWith("NetLogo 3-D Preview 1") ||
     version.startsWith("NetLogo 3-D Preview 2") ||
     version.startsWith("NetLogo 3D Preview 3") ||
     version.startsWith("NetLogo 3D Preview 4"))

}
