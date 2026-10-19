package com.wolfram.jlink;

import java.io.Serializable;

// this is a dummy class that contains the necessary linking information from JLink.jar
// without requiring JLink.jar for compilation, which is useful because JLink.jar can't
// be checked into git due to licensing restrictions. (Isaac B 9/2/25)
public final class Expr implements Serializable {
  public static final int SYMBOL = 4;

  public Expr(Expr e, Expr[] es) {}
  public Expr(int i, String s) {}
  public Expr(String s) {}
  public Expr(double d) {}
  public Expr(int i) {}
}
