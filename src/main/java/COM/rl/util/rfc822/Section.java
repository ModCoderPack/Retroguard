/* ===========================================================================
 * $RCSfile: Section.java,v $
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

import java.io.*;
import java.util.*;

/**
 * An RFC822 section is a list of 'header's (tag/value pairs).
 *
 * @author Mark Welsh
 */
public class Section
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private Vector headers;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Blank section. */
    public Section()
    {
        headers = new Vector();
    }

    /** Append a header to this section. */
    public void add(Header header)
    {
        headers.addElement(header);
    }

    /** Append a header to this section. */
    public void add(String tag, String value)
    {
        add(new Header(tag, value));
    }

    /** Return an Enumeration of headers. */
    public Enumeration elements()
    {
        return headers.elements();
    }

    /** Does the section contain a header matching the specified one? */
    public boolean hasHeader(Header queryHeader)
    {
        if (queryHeader != null)
        {
            for (Enumeration enm = headers.elements(); enm.hasMoreElements(); )
            {
                Header header = (Header)enm.nextElement();
                if (queryHeader.equals(header)) 
                {
                    return true;
                }
            }
        }
        return false;
    }

    /** Find a header matching the specified tag, or null if none. */
    public Header findTag(String tag)
    {
        // Check params
        if (tag == null) return null;
        
        // For now, do linear search of headers
        for (Enumeration enm = headers.elements(); enm.hasMoreElements(); )
        {
            Header header = (Header)enm.nextElement();
            if (tag.equals(header.getTag())) 
            {
                // Found
                return header;
            }
        }

        // Not found
        return null;
    }

    /** Print String rep of this object to a java.io.Writer. */
    public void writeString(Writer writer) throws IOException
    {
        for (Enumeration enm = headers.elements(); enm.hasMoreElements(); )
        {
            ((Header)enm.nextElement()).writeString(writer);
        }
        writer.write("\015\012");
    }

    /** Return String rep of this object. */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (Enumeration enm = headers.elements(); enm.hasMoreElements(); )
        {
            sb.append(((Header)enm.nextElement()).toString());
            sb.append("\015\012");
        }
        sb.append("\015\012");
        return sb.toString();
    }
}
