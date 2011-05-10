package org.nlogo.api;

public interface ReporterRunnable<T> {
  T run() throws LogoException;
}
