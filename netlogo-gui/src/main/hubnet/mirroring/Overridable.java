// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract strictfp class Overridable {
  private final RollbackStack rollbackStack = new RollbackStack();

  abstract String getMethodName(int varName);

  public void set(int var, Object value) {
    try {
      String methodName = getMethodName(var);
      Method getter = getClass().getMethod(methodName);
      Method setter = getSetter(methodName, value.getClass());
      // note that the setter for the old value might be different
      // than the setter for the new value ev 4/29/08
      Object oldValue = getter.invoke(this);
      rollbackStack.push(getSetter(methodName, oldValue.getClass()), oldValue);
      setter.invoke(this, value);
    } catch (NoSuchMethodException ex) {
      throw new IllegalStateException(ex);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  private Method getSetter(String methodName, Class<? extends Object> class1)
      throws NoSuchMethodException {
    try {
      return getClass().getMethod(methodName, class1);
    } catch (NoSuchMethodException ex) {
      // if we don't find a setter specific for this type look for an Object
      // type setter ev 5/16/08
      return getClass().getMethod(methodName, Object.class);
    }
  }

  public static int getOverrideIndex(String[] variables, String varName) {
    for (int i = 0; i < variables.length; i++) {
      if (varName.equalsIgnoreCase(variables[i])) {
        return i;
      }
    }
    return -1;
  }

  public void rollback() {
    rollbackStack.rollback(this);
  }

  private strictfp class RollbackStack {
    private final java.util.LinkedList<Method> methodStack = new java.util.LinkedList<Method>();
    private final java.util.LinkedList<Object> valueStack = new java.util.LinkedList<Object>();

    void push(Method setter, Object value) {
      methodStack.addLast(setter);
      valueStack.addLast(value);
    }

    void rollback(Object owner) {
      try {
        while (!methodStack.isEmpty()) {
          methodStack.removeLast().invoke(owner, valueStack.removeLast());
        }
      } catch (IllegalAccessException e) {
        throw new IllegalStateException(e);
      } catch (InvocationTargetException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
