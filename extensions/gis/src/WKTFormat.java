//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.wkt;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/** 
 * 
 */
public final strictfp class WKTFormat extends Format {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    static {
        NUMBER_FORMAT.setGroupingUsed(false);
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(12);
    }
    
    /** */
    static final long serialVersionUID = 1L;
    
    /** */
    private static final WKTFormat _instance = new WKTFormat();
    
    /** */
    private static final String OPEN_BRACKETS = "[(";
    
    /** */
    private static final String QUOTE = "\"";
    
    /** */
    private static final String CLOSE_BRACKETS = "])";
    
    /** */
    private static final String SEPARATORS;
    static {
        final NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            final char decimalSeparator = ((DecimalFormat)nf).getDecimalFormatSymbols().getDecimalSeparator();
            if (decimalSeparator == ',') {
                SEPARATORS = ";,";
            } else {
                SEPARATORS = ",;";
            }   
        } else {
            SEPARATORS = ",;";
        }
    }
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static WKTFormat getInstance () {
        return _instance;
    }

    /** */
    private static int parseOptionalSeparator (String text, ParsePosition position, String separators) {
        final int length = text.length();
        int index = position.getIndex();
        while ((index < length) && Character.isWhitespace(text.charAt(index))) {
            index += 1;
        }
        int separatorIndex = separators.indexOf(text.charAt(index));
        if (separatorIndex >= 0) {
            index += 1;
        }
        position.setIndex(index);
        return separatorIndex;
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public String formatWKT (WKTElement wkt) {
        StringBuffer str = new StringBuffer();
        str.append(wkt.getKeyword());
        Iterator<Object> iterator = wkt.iterator();
        if (iterator.hasNext()) {
            str.append(OPEN_BRACKETS.charAt(0));
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof WKTElement) {
                    str.append(formatWKT((WKTElement)obj));
                } else if (obj instanceof String) {
                    str.append(QUOTE);
                    str.append((String)obj);
                    str.append(QUOTE);
                } else if (obj instanceof Number) {
                    NUMBER_FORMAT.format(obj, str, new FieldPosition(0));
                } else {
                    throw new IllegalArgumentException("unexpected type " +obj.getClass().getName());
                }   
                if (iterator.hasNext()) {
                    str.append(SEPARATORS.charAt(0));
                }
            }
            str.append(CLOSE_BRACKETS.charAt(0));
        }
        return str.toString();
    }
    
    /** */
    public WKTElement parseWKT (String text, ParsePosition pos) throws ParseException {
        int keywordStart = pos.getIndex();
        final int length = text.length();
        while (keywordStart < length && Character.isWhitespace(text.charAt(keywordStart))) {
            keywordStart += 1;
        }
        int keywordEnd = keywordStart;
        while (keywordEnd < length && Character.isUnicodeIdentifierPart(text.charAt(keywordEnd))) {
            keywordEnd += 1;
        }
        if (keywordEnd <= keywordStart) {
            throw new ParseException("empty keyword", keywordStart);
        }
        final String keyword = text.substring(keywordStart, keywordEnd);
        pos.setIndex(keywordEnd);
        // Parse the opening bracket. The type of the opening bracket (i.e.,
        // '[' or '(') must match the type of the closing bracket. If there
        // is no opening bracket, the element must be a bare keyword.
        int openBracketType = parseOptionalSeparator(text, pos, OPEN_BRACKETS);
        if (openBracketType < 0) {
            return new WKTElement(keyword);
        }
        List<Object> contents = new LinkedList<Object>();
        // Parse all elements inside the bracket
        do {
            if (pos.getIndex() >= length) {
                throw new ParseException("unexpected end of input", length);
            }
            // Try to parse the next element as a quoted string. We will take
            // it as a string if the first non-blank character is a quote.
            if (parseOptionalSeparator(text, pos, QUOTE) >= 0) {
                int stringBegin = pos.getIndex();
                int stringEnd = text.indexOf(QUOTE, stringBegin);
                if (stringEnd <= stringBegin) {
                    throw new ParseException("expected closing quote", pos.getIndex());
                }
                contents.add(text.substring(stringBegin, stringEnd));
                pos.setIndex(stringEnd + 1);
                continue;
            }
            // Try to parse the next element as a number. We will take it as a number if
            // the first non-blank character is not the begining of an unicode identifier.
            int numberBegin = pos.getIndex();
            if (!Character.isUnicodeIdentifierStart(text.charAt(numberBegin))) {
                final Number number = NUMBER_FORMAT.parse(text, pos);
                if (number == null) {
                    throw new ParseException("unparseable number", pos.getErrorIndex());
                }
                contents.add(number);
                continue;
            }
            // Otherwise, recursively parse the child element.
            contents.add(parseWKT(text, pos));
            
        } while (parseOptionalSeparator(text, pos, SEPARATORS) >= 0);
        int closeBracketType = parseOptionalSeparator(text, pos, CLOSE_BRACKETS);
        if (closeBracketType == openBracketType) {
            return new WKTElement(keyword, contents);
        } else {
            throw new ParseException("invalid close bracket at position "+pos.getIndex()+" of '"+text+"'", pos.getIndex());
        } 
    }
    
    //--------------------------------------------------------------------------
    // Format implementation
    //--------------------------------------------------------------------------
    
    /** */
    public StringBuffer format (Object obj, StringBuffer buf, FieldPosition pos) {
        buf.append(formatWKT((WKTElement)obj));
        return buf;
    }
    
    /** */
    public Object parseObject (String str, ParsePosition pos) {
        try {
            return parseWKT(str, pos);
        } catch (ParseException e) {
            pos.setIndex(0);
            pos.setErrorIndex(e.getErrorOffset());
            return null;
        }
    }
}
