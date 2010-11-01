/* ===========================================================================
 * $RCSfile: Header.java,v $
 * ===========================================================================
 *
 * RetroGuard -- an obfuscation package for Java classfiles.
 *
 * Copyright (c) 1998-2006 Mark Welsh (markw@retrologic.com)
 *
 * This program can be redistributed and/or modified under the terms of the 
 * Version 2 of the GNU General Public License as published by the Free 
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details.
 *
 */

package COM.rl.util.rfc822;

import java.lang.Math;
import java.io.*;
import java.util.*;

/**
 * An RFC822 'header' is a 'tag' / 'value' pair.
 *
 * @author Mark Welsh
 */
public class Header
{
    // Constants -------------------------------------------------------------
    /* Maximum length of header line in a section, before break to next line */
    private static final int MAX_HEADER_LINE_LENGTH = 70;


    // Fields ----------------------------------------------------------------
    private String tag;
    private String value;

    // Class Methods ---------------------------------------------------------
    /** Parse a header from the specified String. */
    public static Header parse(String line)
    {
        Header header = null;
        if (line != null) 
        {
            int pos = line.indexOf(':');
            if (pos != -1)
            {
                header = new Header(line.substring(0, pos).trim(), 
                                    line.substring(pos + 1).trim());
            }
        }
        return header;
    }

    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public Header(String tag, String value)
    {
        this.tag = (tag == null ? "" : tag);
        this.value = (value == null ? "" : value);
    }

    /** Return the tag. */
    public String getTag() { return tag; }

    /** Return the value. */
    public String getValue() { return value; }

    /** Test equality of headers. */
    public boolean equals(Object o)
    {
        if (o instanceof Header)
        {
            Header header = (Header)o;
            if (header.getTag().equals(getTag()) 
                && header.getValue().equals(getValue()))
            {
                return true;
            }
        }
        return false;
    }

    /** Print String rep of this object to a java.io.Writer. */
    public void writeString(Writer writer) throws IOException
    {
        String prefix = getTag() + ": ";
        String value = getValue();
        for (int index = 0; index < value.length(); 
             prefix = " ") // continuation lines are prefixed with single space
        {
            int start = index;
            // Compute length of value that can be appended to this line
            index += Math.min(value.length() - index,
                              MAX_HEADER_LINE_LENGTH - prefix.length());
            // Write tag or continuation space, (part of) value, EOL
            writer.write(prefix + value.substring(start, index) + "\015\012");
        }
    }

    /** Return String rep of this object. */
    public String toString()
    {
        return getTag() + ": " + getValue();
    }
}
