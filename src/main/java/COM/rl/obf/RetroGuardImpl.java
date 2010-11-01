/* ===========================================================================
 * $RCSfile: RetroGuardImpl.java,v $
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
import COM.rl.obf.classfile.ClassFile;
import COM.rl.obf.gui.*;

/**
 * Main implementation class for obfuscator package.
 *
 * @author      Mark Welsh
 */
public class RetroGuardImpl
{
    // Constants -------------------------------------------------------------
    public static final String DEFAULT_IN_FILE_NAME = "in.jar";
    public static final String DEFAULT_OUT_FILE_NAME = "out.jar";
    public static final String DEFAULT_RGS_FILE_NAME = "script.rgs";
    public static final String DEFAULT_LOG_FILE_NAME = "retroguard.log";
    private static final String LOG_TITLE_PRE_VERSION = "# RetroGuard v";
    private static final String LOG_TITLE_POST_VERSION = ", by Retrologic Systems - www.retrologic.com";
    private static final String LOG_CREATED = "# Logfile created on ";
    private static final String LOG_INPUT_FILE =  "# Jar file to be obfuscated:           ";
    private static final String LOG_OUTPUT_FILE = "# Target Jar file for obfuscated code: ";
    private static final String LOG_SCRIPT_FILE = "# RetroGuard Script file used:         ";
    private static final String LOG_NO_SCRIPT = "(none, defaults used)";
    private static final String LOG_ERROR = "# Unrecoverable error during obfuscation:";
    private static final String LOG_ZIP_ERROR = "# Review input jar for duplicate classes (same classfile with two different filenames).";
    private static final String SEE_LOG_FILE = "Unrecoverable error during obfuscation, see log file for details.";


    // Fields ----------------------------------------------------------------
    private File inFile;
    private File outFile;
    private File rgsFile;
    private File logFile;


    // Class Methods ---------------------------------------------------------
    /**
     * Main entry point for the obfuscator.
     *
     * @param inFilename a readable input JAR file name
     * @param outFilename a writable JAR file name for obfuscated output
     * @param rgsFilename valid RetroGuard Script data file name, or null (which implies default settings)
     * @param logFilename name for the log data file
     */
    public static void obfuscate(String inFilename, 
                                 String outFilename, 
                                 String rgsFilename, 
                                 String logFilename) throws Exception
    {
        File inFile = new File(inFilename == null ? 
                               DEFAULT_IN_FILE_NAME : inFilename);
        File outFile = new File(outFilename == null ? 
                                DEFAULT_OUT_FILE_NAME : outFilename);
        File rgsFile = new File(rgsFilename == null ? 
                                DEFAULT_RGS_FILE_NAME : rgsFilename);
        File logFile = new File(logFilename == null ? 
                                DEFAULT_LOG_FILE_NAME : logFilename);

        // Input JAR file must exist and be readable
        if (!inFile.exists())
        {
            throw new IllegalArgumentException("JAR specified for obfuscation does not exist.");
        }
        if (!inFile.canRead())
        {
            throw new IllegalArgumentException("JAR specified for obfuscation exists but cannot be read.");
        }
        
        // Output JAR file must be writable if it exists
        if (outFile.exists() && !outFile.canWrite())
        {
            throw new IllegalArgumentException("Output JAR file cannot be written to.");
        }
        
        // Script file must be readable if it exists, but need not exist 
        // (revert to default obfuscation settings in that case)
        if (rgsFile.exists())
        {
            if (!rgsFile.canRead())
            {
                throw new IllegalArgumentException("Script file exists but cannot be read.");
            }
        }

        // Logfile must be writable if it exists
        if (logFile.exists() && !logFile.canWrite())
        {
            throw new IllegalArgumentException("Logfile cannot be written to.");
        }
        
        // Call the main entry point on the obfuscator.
        RetroGuardImpl.obfuscate(inFile, outFile, rgsFile, logFile);
    }

    /**
     * Main entry point for the obfuscator.
     *
     * @param inFile a File pointing to a readable JAR
     * @param outFile a writable JAR for obfuscated output
     * @param rgsFile valid RetroGuard Script data, or null (which implies default settings)
     * @param logFile file for the log data.
     */
    public static void obfuscate(File inFile, File outFile, File rgsFile, File logFile) throws Exception
    {
        new RetroGuardImpl(inFile, outFile, rgsFile, logFile).run();
    }


    // Instance Methods ------------------------------------------------------
    // Private constructor takes in-jar, out-jar and script specifiers.
    private RetroGuardImpl(File inFile, File outFile, File rgsFile, File logFile)
    {
        this.inFile = inFile;
        this.outFile = outFile;
        this.rgsFile = rgsFile;
        this.logFile = logFile;
    }

    // Run the obfuscator.
    private void run() throws Exception
    {
        // Create the session log file
        PrintWriter log = new PrintWriter(
            new BufferedOutputStream(
                new FileOutputStream(logFile)));
        try
        {
            // Write out the log header
            writeLogHeader(log);

            // Create the name mapping database for the input JAR, constrained 
            // by the options in the rgs script
	    boolean enableTrim = RgsEnum.hasOptionTrim(rgsFile);
            GuardDB db = new GuardDB(inFile, enableTrim);
            try
            {
		InputStream rgsInputStream = 
		    (rgsFile.exists() ? new FileInputStream(rgsFile) : null);
                db.retain(new RgsEnum(rgsInputStream), log);
                db.logWarnings(log);
                if (rgsInputStream != null)
                {
                    rgsInputStream.close();
                }
                // If requested in script, trim unused methods, fields, classes
		if (enableTrim) 
		{
		    db.trim(log);
		}
                db.remapTo(outFile, log);
            }
            finally
            {
                db.close();
            }
        }
        catch (Exception e)
        {
            // Log exceptions before exiting
            log.println();
            log.println(LOG_ERROR);
            if (e instanceof java.util.zip.ZipException) {
                log.println(LOG_ZIP_ERROR);
            }
            log.println("# " + e.toString());
            e.printStackTrace(log);
            log.println();
            System.err.println(SEE_LOG_FILE);
            throw e;
        }
        finally
        {
            log.flush();
            log.close();
        }
    }

    // Write a header out to the log file
    private void writeLogHeader(PrintWriter log)
    {
        log.println("# If this log is to be used for incremental obfuscation / patch generation, ");
        log.println("# add any '.class', '.method', '.field' and '.attribute' restrictions here:");
        log.println();
        log.println();
        log.println("#-DO-NOT-EDIT-BELOW-THIS-LINE------------------DO-NOT-EDIT-BELOW-THIS-LINE--");
        log.println("#");
        log.println(LOG_TITLE_PRE_VERSION + Version.getVersion() + LOG_TITLE_POST_VERSION);
        log.println("#");
        log.println(LOG_CREATED + new Date().toString());
        log.println("#");
        log.println(LOG_INPUT_FILE + inFile.getName());
        log.println(LOG_OUTPUT_FILE + outFile.getName());
        log.println(LOG_SCRIPT_FILE + (rgsFile.exists() ? rgsFile.getName() : LOG_NO_SCRIPT));
        log.println("#");
    }
}
