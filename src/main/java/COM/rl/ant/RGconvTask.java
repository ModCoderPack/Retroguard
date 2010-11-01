/* ===========================================================================
 * $RCSfile: RGconvTask.java,v $
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

package COM.rl.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import COM.rl.obf.*;


/**
 * RGconv task for Apache Ant build tool.
 *
 * @author      Mark Welsh
 */
public class RGconvTask extends Task
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private String rgsFile;
    private String inListFile;
    private String outListFile;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Set the script file name. */
    public void setRgsfile(String rgsFile) 
    {
        this.rgsFile = rgsFile;
    }

    /** Set the input file of unobfuscated identifiers. */
    public void setInlistfile(String inListFile) 
    {
        this.inListFile = inListFile;
    }

    /** Set the output file for obfuscated identifiers. */
    public void setOutlistfile(String outListFile) 
    {
        this.outListFile = outListFile;
    }

    /** Execute the task. */
    public void execute() throws BuildException 
    {
        if (rgsFile == null || inListFile == null || outListFile == null)
        {
            throw new BuildException("RGconvTask requires rgsfile, inlistfile, and outlistfile to be set.");
        }
        try 
        {
            RGconvImpl.convert(rgsFile, inListFile, outListFile);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
}
