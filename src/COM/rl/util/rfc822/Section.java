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
public class Section implements Iterable<Header>
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private List<Header> headers = new ArrayList<Header>();


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Blank section.
     */
    public Section()
    {
    }

    /**
     * Append a header to this section.
     * 
     * @param header
     */
    public void add(Header header)
    {
        this.headers.add(header);
    }

    /**
     * Append a header to this section.
     * 
     * @param tag
     * @param value
     */
    public void add(String tag, String value)
    {
        this.add(new Header(tag, value));
    }

    /**
     * Return an Iterator of headers.
     */
    @Override
    public Iterator<Header> iterator()
    {
        return this.headers.iterator();
    }

    /**
     * Does the section contain a header matching the specified one?
     * 
     * @param queryHeader
     */
    public boolean hasHeader(Header queryHeader)
    {
        if (queryHeader != null)
        {
            for (Header header : this.headers)
            {
                if (queryHeader.equals(header))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find a header matching the specified tag, or null if none.
     * 
     * @param tag
     */
    public Header findTag(String tag)
    {
        // Check params
        if (tag == null)
        {
            return null;
        }

        // For now, do linear search of headers
        for (Header header : this.headers)
        {
            if (tag.equals(header.getTag()))
            {
                // Found
                return header;
            }
        }

        // Not found
        return null;
    }

    /**
     * Print String rep of this object to a java.io.Writer.
     * 
     * @param writer
     * @throws IOException
     */
    public void writeString(Writer writer) throws IOException
    {
        for (Header header : this.headers)
        {
            header.writeString(writer);
        }
        writer.write("\015\012");
    }

    /**
     * Return String rep of this object.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Header header : this.headers)
        {
            sb.append(header.toString());
            sb.append("\015\012");
        }
        sb.append("\015\012");
        return sb.toString();
    }
}
