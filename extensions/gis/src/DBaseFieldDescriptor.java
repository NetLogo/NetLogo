//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import java.util.ArrayList;
import java.util.List;
import org.myworldgis.util.StringUtils;


/**
 * Class representing a field in a dBase file.
 */
public final strictfp class DBaseFieldDescriptor implements DBaseConstants {
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static String makeLegalFieldName (String name) {
        StringBuffer result = new StringBuffer();
        int firstAlpha = -1;
        for (int i = 0; i < name.length(); i += 1) {
            if (Character.isLetter(name.charAt(i))) {
                firstAlpha = i;
                break;
            }
        }
        if (firstAlpha < 0) {
            result.append('f');
            firstAlpha = 0;
        }
        for (int i = firstAlpha; (i < name.length()) && (result.length() < DBF_FIELD_DESCRIPTOR_NAME_LENGTH); i += 1) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c) || (c == '_')) {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * Process a bunch of field names at once so that we can remove common 
     * substrings at the front, ensuring the field names will be more readable
     * in ArcView/ArcGIS.
     */
    public static String[] makeLegalFieldNames (String[] strings) {
        int maxLen = DBF_FIELD_DESCRIPTOR_NAME_LENGTH - 1;
        String[] work = new String[strings.length];
        String[] result = new String[strings.length];
        for (int i = 0; i < strings.length; i += 1) {
            work[i] = result[i] = StringUtils.stripNonAlphanumeric(strings[i]).toUpperCase();
        }
        for (int i = 0; i < result.length; i += 1) {
            while (result[i].length() > maxLen) {
                int matchSize = 0;
                int[] matches = null;
                for (int j = 2; j <= result[i].length(); j += 1) {
                    String matchStr = result[i].substring(0, j);
                    List<Integer> matchList = new ArrayList<Integer>(result.length - i);
                    for (int k = (i+1); k < result.length; k += 1) {
                        if ((result[k].length() > maxLen) && StringUtils.startsWithIgnoreCase(result[k], matchStr)) {
                            matchList.add(k);
                        }
                    }
                    if (matchList.size() > 0) {
                        matchSize = matchStr.length();
                        matches = new int[matchList.size()];
                        for (int k = 0; k < matchList.size(); k += 1) {
                            matches[k] = matchList.get(k);
                        }
                    } else {
                        break;
                    }
                }
                if (matches != null) {
                    result[i] = result[i].substring(matchSize);
                    for (int j = 0; j < matches.length; j += 1) {
                        result[matches[j]] = result[matches[j]].substring(matchSize);
                    }
                } else {
                    break;
                }
            }
        }
        for (int i = 0; i < result.length; i += 1) {
            if (result[i].length() > maxLen) {
                result[i] = result[i].substring(0, maxLen);
            } else if ((work[i].length() > maxLen) && 
                       (result[i].length() < maxLen)) {
                result[i] = work[i].substring(0, maxLen - result[i].length()) + result[i];
            }
        }
        return(result);
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private final char _type;
    
    /** */
    private final String _name;
    
    /** */
    private final int _length;
    
    /** */
    private final int _decimalCount;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public DBaseFieldDescriptor (char type, String name, int length, int decimalCount) {
        _type = type;
        _name = makeLegalFieldName(name);
        _length = length;
        _decimalCount = decimalCount;
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public char getType () {
        return(_type);
    }
    
    /** */
    public String getName () {
        return(_name);
    }
    
    /** */
    public int getLength () {
        return(_length);
    }
    
    /** */
    public int getDecimalCount () {
        return(_decimalCount);
    }
    
    /** */
    public boolean equals (Object obj) {
        if (obj instanceof DBaseFieldDescriptor) {
            DBaseFieldDescriptor dfd = (DBaseFieldDescriptor)obj;
            return (dfd._type == _type) &&
                   (dfd._name.equals(_name)) &&
                   (dfd._length == _length) &&
                   (dfd._decimalCount == _decimalCount);
        } else {
            return false;
        }
    }
}
