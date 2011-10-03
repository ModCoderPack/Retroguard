/* ===========================================================================
 * $RCSfile: RgsEntry.java,v $
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

package COM.rl.obf;

import java.io.*;
import java.util.*;

/**
 * Representation of RGS script files entry.
 * 
 * @author Mark Welsh
 */
public class RgsEntry
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    public RgsEntryType type;
    public String name;
    public String descriptor;
    public String extendsName;
    public String obfName;
    public boolean retainToPublic;
    public boolean retainToProtected;
    public boolean retainPubProtOnly;
    public boolean retainFieldsOnly;
    public boolean retainMethodsOnly;
    public boolean retainAndClass;
    public int accessMask;
    public int accessSetting;


    // Instance Methods-------------------------------------------------------
    /**
     * Constructor
     * 
     * @param type
     * @param name
     */
    public RgsEntry(RgsEntryType type, String name)
    {
        this.type = type;
        this.name = name;
    }

    /**
     * Constructor
     * 
     * @param type
     * @param name
     * @param descriptor
     */
    public RgsEntry(RgsEntryType type, String name, String descriptor)
    {
        this.type = type;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
