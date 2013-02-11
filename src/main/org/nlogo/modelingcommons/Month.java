package org.nlogo.modelingcommons;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/15/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class Month {
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