/* ===========================================================================
 * $RCSfile: PatchMaker.java,v $
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

package COM.rl.obf.patch;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.security.*;
import COM.rl.util.*;
import COM.rl.util.rfc822.*;

/**
 * Cycle through a Jar file, copying out required entries to a new patch Jar.
 *
 * @author Mark Welsh
 */
public class PatchMaker 
{
    // Constants -------------------------------------------------------------
    private static final String STREAM_NAME_MANIFEST = "META-INF/MANIFEST.MF";
    private static final String MANIFEST_VERSION_TAG = "Manifest-Version";
    private static final String MANIFEST_VERSION_VALUE = "1.0";
    private static final String MANIFEST_NAME_TAG = "Name";
    private static final String MANIFEST_DIGESTALG_TAG = "Digest-Algorithms";
    private static final String CLASS_EXT = ".class";
    private static final String SIGNATURE_PREFIX = "META-INF/";
    private static final String SIGNATURE_EXT = ".SF";
    private static final String ERROR_CORRUPT_CLASS = "ERROR - corrupt class file: ";


    // Fields ----------------------------------------------------------------
    Vector toKeep = null;              // Jar entries to be copied
    private ZipFile inJar;             // Old JAR file
    private SectionList oldManifest;   // MANIFEST.MF RFC822 data from old Jar
    private SectionList newManifest;   // MANIFEST.MF RFC822 data for new Jar


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public PatchMaker(Vector toKeep) 
    {
        this.toKeep = toKeep;
    }

    /** Make a patch Jar from an obfuscated Jar. */
    public void makePatch(File inFile, File outFile) 
    {
        try 
        {
            inJar = new ZipFile(inFile);
            copyJar(outFile);
        } 
        catch (Exception e) 
        {
            inJar = null;
        }
    }

    /** Close input JAR file at GC-time. */
    protected void finalize() throws Exception 
    {
        close();
    }

    /** Close input JAR file. */
    public void close() throws Exception 
    {
        if (inJar != null) 
        {
            inJar.close();
            inJar = null;
        }
    }

    // Requested files are copied through unchanged, except for manifest and 
    // any signature files - these are deleted and the manifest is regenerated.
    private void copyJar(File outFile) throws Exception 
    {
        parseManifest();
        Enumeration entries = inJar.entries();
        ZipOutputStream outJar = null;
        try 
        {
            outJar = new ZipOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(outFile)));
            while (entries.hasMoreElements()) 
            {
                // Get the next entry from the input Jar
                ZipEntry inEntry = (ZipEntry)entries.nextElement();

                // Ignore directories
                if (inEntry.isDirectory()) 
                {
                    continue;
                }
                
                // Open the entry and prepare to process it
                DataInputStream inStream = null;
                try 
                {
                    inStream = new DataInputStream(
                        new BufferedInputStream(
                            inJar.getInputStream(inEntry)));
                    String inName = inEntry.getName();
                    if (isToKeep(inName)) 
                    {
                        // Copy the entry through unchanged
                        long size = inEntry.getSize();
                        if (size != -1) 
                        {
                            byte[] bytes = new byte[(int)size];
                            inStream.readFully(bytes);
                            ZipEntry outEntry = new ZipEntry(inName);
                            outJar.putNextEntry(outEntry);
                            
                            // Pipe OutputStream via digest generators
                            MessageDigest shaDigest = 
                                MessageDigest.getInstance("SHA");
                            MessageDigest md5Digest = 
                                MessageDigest.getInstance("MD5");
                            DataOutputStream dataOutputStream = 
                                new DataOutputStream(
                                    new DigestOutputStream(
                                        new DigestOutputStream(outJar, 
                                                               shaDigest), 
                                        md5Digest));

                            // Dump the data, while creating the digests
                            dataOutputStream.write(bytes, 0, bytes.length);
                            dataOutputStream.flush();
                            outJar.closeEntry();
                    
                            // Update the manifest entry with the new digests
                            MessageDigest[] digests = {shaDigest, md5Digest};
                            updateManifest(inName, digests);
                        }
                    }
                } 
                finally 
                {
                    if (inStream != null) 
                    {
                        inStream.close();
                    }
                }
            }
            
            // Finally, write the new manifest file
            ZipEntry outEntry = new ZipEntry(STREAM_NAME_MANIFEST);
            outJar.putNextEntry(outEntry);
            PrintWriter writer = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(outJar)));
            writer.write(newManifest.toString());
            writer.flush();
            outJar.closeEntry();
        } 
        finally 
        {
            if (outJar != null) 
            {
                outJar.close();
            }
        }
    }

    // Is the entry to be copied?
    private boolean isToKeep(String name) 
    {
        // Name is a keeper if: it is directly listed; or, if it is an 
        // inner class (at any depth) of a listed class.
        
        // Transform inner class name to outermost class name
        if (name.length() > CLASS_EXT.length() &&
            name.substring(name.length() - CLASS_EXT.length(), 
                           name.length()).equals(CLASS_EXT) && 
            name.indexOf('$') != -1) 
        {
            name = name.substring(0, name.indexOf('$')) + CLASS_EXT;
        }
        
        // Check for listing 
        for (Enumeration enm = toKeep.elements(); enm.hasMoreElements(); ) 
        {
            if (name.equals((String)enm.nextElement())) 
            {
                return true;
            }
        }
        return false;
    }

    // Parse the RFC822-style MANIFEST.MF file
    private void parseManifest() throws Exception 
    {
        // The manifest file is called (case insensitively) 'MANIFEST.MF'
        oldManifest = new SectionList();
        Enumeration entries = inJar.entries();
        while (entries.hasMoreElements()) 
        {
            ZipEntry inEntry = (ZipEntry)entries.nextElement();
            String name = inEntry.getName();
            if (STREAM_NAME_MANIFEST.equals(name.toUpperCase())) 
            {
                oldManifest.parse(inJar.getInputStream(inEntry));
                break;
            }
        }

        // Create a fresh manifest, with a version header
        newManifest = new SectionList();
        Section version = new Section();
        version.add(MANIFEST_VERSION_TAG, MANIFEST_VERSION_VALUE);
        newManifest.add(version);
    }

    // Update an entry in the manifest file
    private void updateManifest(String inName, MessageDigest[] digests) 
    {
        // Create fresh section for entry, and enter "Name" header
        Section newSection = new Section();
        newSection.add(MANIFEST_NAME_TAG, inName);

        // Check for section in old manifest, and copy over non-"Name", 
        // non-digest entries
        Section oldSection = oldManifest.find(MANIFEST_NAME_TAG, inName);
        if (oldSection != null) 
        {
            for (Enumeration enm = oldSection.elements(); 
                 enm.hasMoreElements(); ) 
            {
                Header header = (Header)enm.nextElement();
                if (!header.getTag().equals(MANIFEST_NAME_TAG) &&
                    header.getTag().indexOf("Digest") == -1) 
                {
                    newSection.add(header);
                }
            }
        }

        // Create fresh digest entries in the new section
        if (digests != null && digests.length > 0) 
        {
            // Digest-Algorithms header
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digests.length; i++) 
            {
                sb.append(digests[i].getAlgorithm());
                sb.append(" ");
            }
            newSection.add(MANIFEST_DIGESTALG_TAG, sb.toString());

            // *-Digest headers
            for (int i = 0; i < digests.length; i++) 
            {
                newSection.add(digests[i].getAlgorithm() + "-Digest", 
                               Tools.toBase64(digests[i].digest()));
            }            
        }

        // Append the new section to the new manifest
        newManifest.add(newSection);
    }
}

