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

package COM.rl.util.rfc822;

import java.io.*;
import java.util.*;

/**
 * A list of RFC822 sections, usually representing a file.
 *
 * @author Mark Welsh
 */
public class SectionList
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private Vector sections;

    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Construct with no initial sections. */
    public SectionList()
    {
        sections = new Vector();
    }

    /** Parse the stream, appending the sections found there to our list. */
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
                if (section != null && line.indexOf(':') == -1)
                {
                    add(section);
                    section = null;
                }
                
                // If at start of section, skip non-header lines
                if (section == null && line.indexOf(':') == -1)
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
                    else // new section, so reset in preparation for read.
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

    /** Add a Section to the list. */
    public void add(Section section)
    {
        sections.addElement(section);
    }

    /** Return an Enumeration of sections. */
    public Enumeration elements()
    {
        return sections.elements();
    }

    /** Find the first section in the list containing the matching header. */
    public Section find(String tag, String value)
    {
        return find(new Header(tag, value));
    }

    /** Find the first section in the list containing the matching header. */
    public Section find(Header header)
    {
        for (Enumeration enm = elements(); enm.hasMoreElements(); )
        {
            Section section = (Section)enm.nextElement();
            if (section.hasHeader(header))
            {
                return section;
            }
        }
        return null;
    }

    /** Print String rep of this object to a java.io.Writer. */
    public void writeString(Writer writer) throws IOException
    {
        for (Enumeration enm = elements(); enm.hasMoreElements(); )
        {
            ((Section)enm.nextElement()).writeString(writer);
        }
    }

    /** Return String rep of this object. */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (Enumeration enm = elements(); enm.hasMoreElements(); )
        {
            sb.append(((Section)enm.nextElement()).toString());
        }
        return sb.toString();
    }
}
