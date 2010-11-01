/* ===========================================================================
 * $RCSfile: RetroGuardTask.java,v $
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
 * RetroGuard task for Apache Ant build tool.
 *
 * @author      Mark Welsh
 */
public class RetroGuardTask extends Task
{
    // Constants -------------------------------------------------------------


    // Fields ----------------------------------------------------------------
    private String inFile;
    private String outFile;
    private String rgsFile;
    private String logFile;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Set the input jar name. */
    public void setInfile(String inFile) 
    {
        this.inFile = inFile;
    }

    /** Set the output jar name. */
    public void setOutfile(String outFile) 
    {
        this.outFile = outFile;
    }

    /** Set the script file name. */
    public void setRgsfile(String rgsFile) 
    {
        this.rgsFile = rgsFile;
    }

    /** Set the log file name. */
    public void setLogfile(String logFile) 
    {
        this.logFile = logFile;
    }

    /** Execute the task. */
    public void execute() throws BuildException 
    {
        try 
        {
            RetroGuardImpl.obfuscate(inFile, outFile, rgsFile, logFile);
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
}
