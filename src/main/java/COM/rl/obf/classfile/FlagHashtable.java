/* ===========================================================================
 * $RCSfile: FlagHashtable.java,v $
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

package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

/**
 * Subclass of Hashtable used for storing flags while walking Code.
 *
 * @author      Mark Welsh
 */
public class FlagHashtable extends Hashtable
{
    public void updateFlag(CpInfo cpInfo, int index, boolean forNameFlag) 
    {
        StringCpInfoFlags flags = (StringCpInfoFlags)get(cpInfo);
        if (flags == null) 
        {
            flags = new StringCpInfoFlags();
            flags.stringIndex = index;
            put(cpInfo, flags);
        }
        if (forNameFlag) 
        {
            flags.forNameFlag = true;
        }
        else 
        {
            flags.otherFlag = true;
        }
    }
}

class StringCpInfoFlags
{
    protected int stringIndex;
    protected boolean forNameFlag;
    protected boolean otherFlag;
    protected StringCpInfoFlags() {}
}    

