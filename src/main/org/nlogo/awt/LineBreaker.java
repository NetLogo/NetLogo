package org.nlogo.awt;

import java.util.List;
import java.util.ArrayList;

public final strictfp class LineBreaker {

  // this class is not instantiable
  private LineBreaker() { throw new IllegalStateException(); }

  /// line wrapping

  public static List<String> breakLines(String text,
                                        java.awt.FontMetrics metrics,
                                        int width) {
    List<String> result = new ArrayList<String>();
    while (text.length() > 0) {
      int index = 0;
      while (index < text.length()
          && (metrics.stringWidth(text.substring(0, index + 1)) < width
          || text.charAt(index) == ' ')) {
        if (text.charAt(index) == '\n') {
          text = text.substring(0, index) + ' ' + text.substring(index + 1);
          index++;
          break;
        }
        index++;
      }

      // if index is still 0, then this line will never wrap
      // so just give up and return the whole thing as one line
      if (index == 0) {
        result.add(text);
        return result;
      }

      // invariant: index is now the index of the first non-space
      // character which won't fit in the current line

      if (index < text.length()) {
        int spaceIndex = text.substring(0, index).lastIndexOf(' ');
        if (spaceIndex >= 0) {
          index = spaceIndex + 1;
        }
      }

      // invariant: index is now the index of the first character
      // which will *not* be included in the current line

      String thisLine = text.substring(0, index);
      if (index < text.length()) {
        text = text.substring(index, text.length());
      } else {
        text = "";
      }
      result.add(thisLine);
    }
    if (result.isEmpty()) {
      result.add("");
    }
    return result;
  }

}
