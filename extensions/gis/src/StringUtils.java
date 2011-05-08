//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//


package org.myworldgis.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;


/**
 *
 */
public final class StringUtils {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final class WildcardMatcher {
        
        private final String[] _subpatterns;
        
        public WildcardMatcher (String pattern) {
            _subpatterns = pattern.split("\\*");
        }
        
        public boolean matches (String str) {
            int lastIndex = 0;
            for (int i = 0; i < _subpatterns.length; i += 1) {
                int index = str.indexOf(_subpatterns[i], lastIndex);
                if (index < lastIndex) {
                    return false;
                } else {
                    lastIndex = index + _subpatterns[i].length();
                }
            }
            return true;
        }
    }
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** See http://www.ietf.org/rfc/rfc2396.txt */
    private static final String RESERVED_URI_CHARS = ";/?:@&=+$,%#";
    
    /** See http://www.ietf.org/rfc/rfc2396.txt */
    private static final String UNRESERVED_URI_CHARS = "-_.!~*'()";
        
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static boolean startsWithIgnoreCase (String str1, String str2) {
        if (str1.length() >= str2.length()) {
            return(str1.regionMatches(true, 0, str2, 0, str2.length()));
        } else {
            return(false);
        }
    }
    
    /** */
    public static boolean endsWithIgnoreCase (String str1, String str2) {
        if (str1.length() >= str2.length()) {
            return(str1.regionMatches(true, str1.length() - str2.length(), str2, 0, str2.length()));
        } else {
            return(false);
        }
    }
    
    /** */
    public static String stripNonAlphanumeric (String string) {
        for (int i = 0; i < string.length(); i += 1) {
            if (!Character.isLetterOrDigit(string.charAt(i))) {
                StringBuffer newString = new StringBuffer(string.length());
                for (int j = 0; j < string.length(); j += 1) {
                    char c = string.charAt(j);
                    if (Character.isLetterOrDigit(c)) {
                        newString.append(c);
                    }
                }
                return(newString.toString());
            }
        }
        return(string);
    }
    
    /** */
    public static boolean hasFileExtension (String fileName, String extension) {
        return (fileName.length() >= (extension.length() + 1)) &&
               (fileName.charAt(fileName.length() - extension.length() - 1) == '.') &&
               fileName.regionMatches(true, 
                                      (fileName.length() - extension.length()), 
                                      extension, 
                                      0, 
                                      extension.length());
    }
    
    /** */
    public static String getFileExtension (String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if ((dotIndex > 0) && (fileName.length() > (dotIndex + 1))) {
            return(fileName.substring(dotIndex + 1));
        }
        return null;
    }
    
    /** */
    public static String stripFileExtension (String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if ((dotIndex > 0) && (fileName.length() > (dotIndex + 1))) {
            return(fileName.substring(0, dotIndex));
        }
        return fileName;
    }
    
    /** */
    public static String changeFileExtension (String fileName, String extension) {
        if (hasFileExtension(fileName, extension)) {
            return fileName;
        } else {
            return stripFileExtension(fileName) + "." + extension;
        }
    }
    
    /** */
    public static void writeDelimited (String string, char delimiter, Writer out) throws IOException {
        if (delimiter == '\"') {
            throw new IllegalArgumentException("cannot use double-quote char as delimiter");
        }
        if (string.indexOf(delimiter) >= 0) {
            out.write('\"');
            if (string.indexOf('\"') >= 0) {
                for (int i = 0; i < string.length(); i += 1) {
                    char c = string.charAt(i);
                    if (c == '\"') {
                        out.write("\"\"");
                    } else {
                        out.write(c);
                    }
                }
            } else {
                out.write(string);
            }
            out.write('\"');
        } else {
            out.write(string);
        }
    }
    
    /** */
    public static String readDelimited (Reader in, char delimiter) throws IOException {
        if (delimiter == '\"') {
            throw new IllegalArgumentException("cannot use double-quote char as delimiter");
        }
        PushbackReader pIn = new PushbackReader(in);
        boolean escaped = false;
        StringBuilder builder = new StringBuilder();
        while (true) {
            int i = pIn.read();
            if (i < 0) {
                if (builder.length() > 0) {
                    return builder.toString();
                } else {
                    return null;
                }
            }
            char c = (char)i;
            if ((c == delimiter) && (!escaped)) {
                return builder.toString();
            } else if (c == '\"') {
                char next = (char)pIn.read();
                if (next == '\"') {
                    builder.append(c);
                } else {
                    escaped = !escaped;
                    pIn.unread(next);
                }
            } else {
                builder.append(c);
            }
        }
    }

    /** */
    public static String encodeURL (String in) {
        StringBuilder resultBuffer = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i += 1) {
            char c = in.charAt(i);
            if (Character.isLetterOrDigit(c) || (RESERVED_URI_CHARS.indexOf(c) >= 0) || (UNRESERVED_URI_CHARS.indexOf(c) >= 0)) {
                resultBuffer.append(c);
            } else {
                if ((c == '%') && (i < (in.length() - 2)) && isHexDigit(in.charAt(i+1)) && isHexDigit(in.charAt(i+2))) {
                    // already encoded
                    resultBuffer.append(c);
                } else {
                    resultBuffer.append("%" + Integer.toHexString(c));
                }
            }
        }
        return(resultBuffer.toString());
    }
    
    /** */
    public static boolean isHexDigit (char c) {
        return ((c >= 0x30) && (c <= 0x39)) || // 0-9
               ((c >= 0xC0) && (c <= 0xC5)) || // A-F
               ((c >= 0xDF) && (c <= 0xE4));   // a-f
    }
}
