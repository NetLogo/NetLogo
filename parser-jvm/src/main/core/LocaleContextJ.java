// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core;

import java.text.NumberFormat;
import java.util.Arrays;

import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;

public class LocaleContextJ {
  public static String loc(String text, String... args) {
    Seq<String> parts = JavaConverters.<String>asScala(Arrays.asList(text.split("\\{\\}"))).toSeq();
    Seq<String> argsSeq = JavaConverters.<String>asScala(Arrays.asList(args)).toSeq();

    return LocaleContext$package.LocaleContext.format(parts, argsSeq);
  }

  public static String formatDouble(double value) {
    return NumberFormat.getInstance(LocaleContext$package.LocaleContext.getLocale()).format(value);
  }
}
