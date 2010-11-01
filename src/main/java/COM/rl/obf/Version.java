/* ===========================================================================
 * $RCSfile: Version.java,v $
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


/**
 * Central point for version and build control.
 *
 * @author Mark Welsh
 */
public class Version 
{
    // Constants -------------------------------------------------------------
    private static final String REL_VERSION = "2.3.1";
    private static final String RETROGUARD_CLASS_ID = "RGLite";
    private static final String RETROGUARD_REL_JAR_COMMENT =
    "Obfuscation by RetroGuard Lite - Academic / Not-For-Profit License - www.retrologic.com";
    private static final String RETROGUARD_FULL_VERSION_COMMENT =
    "RetroGuard v" + REL_VERSION + " by Retrologic Systems - www.retrologic.com";

    public static final boolean isLite = true;


    // Class Methods ---------------------------------------------------------
    /** Return the current major.minor.patch version string. */
    public static String getVersion() 
    {
        return REL_VERSION;
    }

    /** Return a major.minor.patch versioned comment string. */
    public static String getVersionComment() 
    {
        return RETROGUARD_FULL_VERSION_COMMENT;
    }

    /** Return the current class ID string. */
    public static String getClassIdString() 
    {
        return RETROGUARD_CLASS_ID;
    }

    /** Return the default Jar comment string. */
    public static String getJarComment() 
    {
        return RETROGUARD_REL_JAR_COMMENT;
    }
}
