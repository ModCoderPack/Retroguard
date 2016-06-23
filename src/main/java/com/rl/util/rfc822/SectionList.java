/* ===========================================================================
 * $RCSfile: SectionList.java,v $
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

package com.rl.util.rfc822;

import java.io.*;
import java.util.*;

/**
 * A list of RFC822 sections, usually representing a file.
 * 
 * @author Mark Welsh
 */
public class SectionList implements Iterable<Section>
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private List<Section> sections;

    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * Construct with no initial sections.
     */
    public SectionList()
    {
        this.sections = new ArrayList<Section>();
    }

    /**
     * Parse the stream, appending the sections found there to our list.
     * 
     * @param in
     */
    public void parse(InputStream in)
    {
        // Wrap the stream for text reading
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        // Read until end of file
        String line;
        Section section = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                // If at end of section, close section
                if ((section != null) && (line.indexOf(':') == -1))
                {
                    this.add(section);
                    section = null;
                }

                // If at start of section, skip non-header lines
                if ((section == null) && (line.indexOf(':') == -1))
                {
                    continue;
                }

                // Start or continue section
                if (section == null)
                {
                    section = new Section();
                }

                // Read header, potentially split over multiple lines
                boolean done = false;
                while (!done)
                {
                    // Mark for reset, read a line, and...
                    reader.mark(80);
                    String nextLine = reader.readLine();
                    if (nextLine == null) // stop on EOF, or...
                    {
                        done = true;
                    }
                    else if (nextLine.indexOf(' ') == 0) // append, or...
                    {
                        line = line.concat(nextLine.substring(1));
                    }
                    else
                    // new section, so reset in preparation for read.
                    {
                        reader.reset();
                        done = true;
                    }
                }
                // Parse the header and add to this section
                section.add(Header.parse(line));
            }
        }
        catch (IOException e)
        {
            // Unexpected EOF during readLine() can cause this.
            // Just terminate the read quietly.
            return;
        }
    }

    /**
     * Add a Section to the list.
     * 
     * @param section
     */
    public void add(Section section)
    {
        this.sections.add(section);
    }

    /**
     * Return an Iterator of sections.
     */
    @Override
    public Iterator<Section> iterator()
    {
        return this.sections.iterator();
    }

    /**
     * Find the first section in the list containing the matching header.
     * 
     * @param tag
     * @param value
     */
    public Section find(String tag, String value)
    {
        return this.find(new Header(tag, value));
    }

    /**
     * Find the first section in the list containing the matching header.
     * 
     * @param header
     */
    public Section find(Header header)
    {
        for (Section section : this.sections)
        {
            if (section.hasHeader(header))
            {
                return section;
            }
        }
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
        for (Section section : this.sections)
        {
            section.writeString(writer);
        }
    }

    /**
     * Return String rep of this object.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Section section : this.sections)
        {
            sb.append(section.toString());
        }
        return sb.toString();
    }
}
