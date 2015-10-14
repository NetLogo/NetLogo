// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import java.util.ArrayList;
import java.util.List;

public final strictfp class PrinterManager {
  /**
   * the filename for this print job. Placed at the bottom of each text page
   * (so this will only be used if printText() is used).
   */
  private final String fileName;

  /**
   * the cached text pages for this print job. We use this to caceh the result
   * of the first call to buildTextPages().
   * Only used for printing text pages, naturally.
   */
  private List<List<Object>> textPages;

  /**
   * makes a new PrinterManager. Used to create a new PrinterManager
   * specifically for each print job. Each instance contains job-specific
   * data.
   */
  private PrinterManager(String fileName) {
    this.fileName = fileName;
    textPages = null;
  }

  /**
   * initiate a print job. This method is used to create a new print job.
   * It is the only entry point into the printing system.
   */
  public static void print(final org.nlogo.swing.Printable p, String fileName)
      throws java.awt.print.PrinterAbortException, java.awt.print.PrinterException {
    java.awt.print.PrinterJob printerJob =
        java.awt.print.PrinterJob.getPrinterJob();
    final PrinterManager printer = new PrinterManager(fileName);
    printerJob.setPrintable(new java.awt.print.Printable() {
      public int print(java.awt.Graphics g,
                       java.awt.print.PageFormat pageFormat, int pageIndex)
          throws java.awt.print.PrinterException {
        // for now set the font in the graphics object manually
        // there's apparently some Apple bug where it doesn't get
        // set ev 5/14/08
        if (System.getProperty("os.name").startsWith("Mac")
            && System.getProperty("os.version").startsWith("10.5")) {
          g.setFont(printer.printFont);
        }
        return p.print(g, pageFormat, pageIndex, printer);
      }
    }, printerJob.pageDialog(printerJob.defaultPage()));
    if (printerJob.printDialog()) {
      printerJob.print();
    }
  }

  /**
   * builds the page footer. Only used for building text pages.
   */
  protected String getFooter(int pageNum, int pageWidth, java.awt.FontMetrics fm) {
    String pageNumString = " - page " + pageNum;
    String shortFileName = org.nlogo.awt.Fonts.shortenStringToFit(fileName, pageWidth - fm.stringWidth(pageNumString), fm);
    return shortFileName + pageNumString;
  }


  private final java.awt.Font printFont =
      new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 9);

  /**
   * builds the actual text pages.
   */
  private List<List<Object>>
  buildTextPages(java.awt.Graphics g,
                 java.awt.print.PageFormat pageFormat, String text)
      throws java.io.IOException {
    List<List<Object>> pages =
        new ArrayList<List<Object>>();
    List<Object> lines = new ArrayList<Object>();

    java.awt.geom.Point2D.Float pen = new java.awt.geom.Point2D.Float();
    float wrappingWidth = (float) pageFormat.getImageableWidth();

    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
    java.awt.font.TextLayout defaultLayout =
        new java.awt.font.TextLayout(" ", printFont, g2d.getFontRenderContext());

    float layoutDescent = defaultLayout.getDescent();
    float layoutAscent = defaultLayout.getAscent();

    double pageHeight = pageFormat.getImageableHeight() - 2 *
        (defaultLayout.getLeading() + layoutDescent + layoutAscent);

    java.io.LineNumberReader lineReader =
        new java.io.LineNumberReader(new java.io.StringReader(text));
    String nextLine = lineReader.readLine();
    while (nextLine != null) {
      if (nextLine.length() == 0) {
        pen.y += layoutAscent + layoutDescent;
        if (pen.y < pageHeight) {
          lines.add(nextLine);
          pen.y += defaultLayout.getLeading();
        } else {
          pages.add(lines);
          lines = new ArrayList<Object>();
          lines.add(nextLine);
          pen = new java.awt.geom.Point2D.Float();
          pen.y += defaultLayout.getLeading() + layoutDescent + layoutAscent;
        }
        nextLine = lineReader.readLine();
        continue;
      }
      java.text.AttributedString formattedText = new java.text.AttributedString(nextLine);
      formattedText.addAttribute(java.awt.font.TextAttribute.FONT, printFont);

      // Use a LineBreakMeasurer instance to break our text into
      // lines that fit the imageable area of the page.
      java.text.AttributedCharacterIterator charIterator = formattedText.getIterator();
      java.awt.font.LineBreakMeasurer measurer =
          new java.awt.font.LineBreakMeasurer(charIterator, g2d.getFontRenderContext());
      while (measurer.getPosition() < charIterator.getEndIndex()) {
        java.awt.font.TextLayout layout = measurer.nextLayout(wrappingWidth);
        pen.y += layoutAscent + layoutDescent;
        if (pen.y < pageHeight) {
          lines.add(layout);
          pen.y += layout.getLeading();
        } else {
          pages.add(lines);
          lines = new ArrayList<Object>();
          lines.add(layout);
          pen = new java.awt.geom.Point2D.Float();
          pen.y += layout.getLeading() + layoutDescent + layoutAscent;
        }
      }
      nextLine = lineReader.readLine();
    }
    pages.add(lines);
    return pages;
  }

  /**
   * draws text to the printer in a consistent manner. There is a somewhat
   * strange contract here: in each PrinterManager, the text pages are built
   * once and then cached for that print job. This means that the text
   * argument to this method is ignored after the first time it is called,
   * and that you won't be able to change the content of the pages once
   * the pages have been built.
   */
  public int printText(java.awt.Graphics g, java.awt.print.PageFormat
      pageFormat, int pageIndex, String text)
      throws java.io.IOException {
    if (textPages == null) {
      textPages = buildTextPages(g, pageFormat, text);
    }
    if (pageIndex >= textPages.size()) {
      return java.awt.print.Printable.NO_SUCH_PAGE;
    }

    float wrappingWidth = (float) pageFormat.getImageableWidth();
    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
    java.awt.font.TextLayout defaultLayout =
        new java.awt.font.TextLayout(" ", printFont, g2d.getFontRenderContext());

    float layoutDescent = defaultLayout.getDescent();
    float layoutAscent = defaultLayout.getAscent();

    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    List<Object> lines = textPages.get(pageIndex);
    java.awt.geom.Point2D.Float pen = new java.awt.geom.Point2D.Float();
    for (int i = 0; i < lines.size(); i++) {
      if (!(lines.get(i) instanceof java.awt.font.TextLayout)) {
        pen.y += defaultLayout.getLeading() + layoutDescent + layoutAscent;
        continue;
      }
      java.awt.font.TextLayout layout = (java.awt.font.TextLayout) lines.get(i);
      pen.y += layoutAscent;
      layout.draw(g2d, pen.x, pen.y);
      pen.y += layoutDescent + layout.getLeading();
    }

    // add a footer at the bottom of the page
    String footer = getFooter(pageIndex + 1, (int) StrictMath.floor(pageFormat.getImageableWidth()),
        g.getFontMetrics(printFont));
    java.awt.font.TextLayout footerLayout =
        new java.awt.font.TextLayout(footer, printFont, g2d.getFontRenderContext());
    pen.x = (wrappingWidth - footerLayout.getAdvance()) / 2;
    pen.y = (float) pageFormat.getImageableHeight() - layoutDescent;
    footerLayout.draw(g2d, pen.x, pen.y);

    return java.awt.print.Printable.PAGE_EXISTS;
  }

}
