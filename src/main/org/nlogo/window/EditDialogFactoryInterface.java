// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.Editable;

// This is used so that other packages don't depend on the properties package.  It gets instantiated
// by dependency injection. - ST 2/17/10

// The name "canceled" describes the value returned: did the user cancel the dialog or not?  Calling
// code may need to know this, for example if we are creating an object and not just editing an
// existing one, canceling the dialog should cause the new object to be discarded.  If the return
// value is false, then the user's edits have been applied to the object edited (the "target").
// - ST 2/24/10

// There are two different methods because the JDialog created needs a parent, and JDialog has
// two different constructors for the two different possible parent types. - ST 2/24/10

public interface EditDialogFactoryInterface {
  boolean canceled(java.awt.Frame frame, Editable target);

  boolean canceled(java.awt.Dialog dialog, Editable target);
}
