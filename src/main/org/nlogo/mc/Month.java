// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public strictfp class Month {

  private int monthNum;
  private String monthString;

  private static List<Month> months;

  private Month(int monthNum, String monthString) {
    this.monthNum = monthNum;
    this.monthString = monthString;
  }

  public int getMonthNum() {
    return monthNum;
  }

  public String toString() {
    return monthString;
  }

  static {
    months = new ArrayList<Month>(12);
    String[] monthNames = (new DateFormatSymbols()).getMonths();
    for(int i = 0; i < monthNames.length; i++) {
      if(monthNames[i].length() > 0) {
        months.add(i, new Month(i + 1, monthNames[i]));
      }
    }
  }

  public static List<Month> getMonths() {
    return months;
  }

}

