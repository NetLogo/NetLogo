package org.nlogo.awt;

public final strictfp class EventQueue {

  // this class is not instantiable
  private EventQueue() { throw new IllegalStateException(); }

  /// thread safety utils

  // At the moment this one is useless, but historically we sometimes
  // had extra stuff attached here, and we might want to add some
  // again in the future, so... - ST 8/3/03
  public static void invokeLater(final Runnable r) {
    java.awt.EventQueue.invokeLater(r);
  }

  public static void invokeAndWait(final Runnable r)
      throws InterruptedException {
    try {
      java.awt.EventQueue.invokeAndWait(r);
    } catch (java.lang.reflect.InvocationTargetException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static void mustBeEventDispatchThread() {
    if (!java.awt.EventQueue.isDispatchThread()) {
      throw new IllegalStateException("not event thread: " + Thread.currentThread());
    }
  }

  public static void cantBeEventDispatchThread() {
    if (java.awt.EventQueue.isDispatchThread()) {
      throw new IllegalStateException();
    }
  }

}
