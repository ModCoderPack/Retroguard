/* ===========================================================================
 * $RCSfile: GuardDB.java,v $
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
import java.util.zip.*;
import java.security.*;
import COM.rl.obf.classfile.*;
import COM.rl.util.*;
import COM.rl.util.rfc822.*;

/**
 * Classfile database for obfuscation.
 *
 * @author      Mark Welsh
 */
public class GuardDB implements ClassConstants
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
    private static final String LOG_MEMORY_USED = "# Memory in use after class data structure built: ";
    private static final String LOG_MEMORY_TOTAL = "# Total memory available                        : ";
    private static final String LOG_MEMORY_BYTES = " bytes";
    private static final String WARNING_SCRIPT_ENTRY_ABSENT = "# WARNING - identifier from script file not found in JAR: ";
    private static final String ERROR_CORRUPT_CLASS = "# ERROR - corrupt class file: ";
    private static final String WARNING_INCOMPATIBLE_VERSION_1 = "# WARNING - class file format has incompatible major-version number: v";
    private static final String WARNING_INCOMPATIBLE_VERSION_2 = "# WARNING - this version of RetroGuard supports up to class format:  v";


    // Fields ----------------------------------------------------------------
    private ZipFile inJar;          // JAR file for obfuscation
    private SectionList oldManifest;   // MANIFEST.MF RFC822-style data from old Jar
    private SectionList newManifest;   // MANIFEST.MF RFC822-style data for new Jar
    private ClassTree classTree;    // Tree of packages, classes. methods, fields
    private boolean hasMap = false; // Has the mapping been generated already?
    private boolean enableMapClassString = false; // Remap strings in reflection?
    private boolean enableTrim = false; // Trim unused method, field, classes?
    private boolean enableRepackage = false; // Repackage classes for size?
    private boolean enableDummySourceFile = false; // Remap SourceFile attribute to text "SourceFile"?
    private boolean enableDigestSHA = false; // Produce SHA-1 manifest digests?
    private boolean enableDigestMD5 = false; // Produce MD5 manifest digests?


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** A classfile database for obfuscation. */
    public GuardDB(File inFile, boolean enableTrim) throws Exception
    {
        inJar = new ZipFile(inFile);
	this.enableTrim = enableTrim;
        parseManifest();
    }

    /** Close input JAR file and log-file at GC-time. */
    protected void finalize() throws Exception
    {
        close();
    }

    /** Create a classfile database. */
    public void buildClassTree(PrintWriter log) throws Exception
    {
        // Go through the input Jar, adding each class file to the database
        int incompatibleVersion = 0;
        classTree = new ClassTree();
        Enumeration entries = inJar.entries();
        while (entries.hasMoreElements())
        {
            // Get the next entry from the input Jar
            ZipEntry inEntry = (ZipEntry)entries.nextElement();
            String name = inEntry.getName();
            if (name.length() > CLASS_EXT.length() &&
                name.substring(name.length() - CLASS_EXT.length(), name.length()).equals(CLASS_EXT))
            {
                // Create a full internal representation of the class file
                DataInputStream inStream = new DataInputStream(
                    new BufferedInputStream(
                        inJar.getInputStream(inEntry)));
                ClassFile cf = null;
                try
                {
                    cf = ClassFile.create(inStream);
                }
                catch (Exception e)
                {
                    log.println(ERROR_CORRUPT_CLASS + name + " (" + e.getMessage() + ")");
                }
                finally
                {
                    inStream.close();
                }
                if (cf.hasIncompatibleVersion())
                {
                    incompatibleVersion = cf.getMajorVersion();
                }
                classTree.addClassFile(cf, enableTrim);
            }
        }
        // Warn if classes are incompatible version of class file format
        if (incompatibleVersion != 0)
        {
            log.println(WARNING_INCOMPATIBLE_VERSION_1 + incompatibleVersion);
            log.println(WARNING_INCOMPATIBLE_VERSION_2 + MAJOR_VERSION);
        }
    }

    /**
     * Go through database marking certain entities for retention, while
     * maintaining polymorphic integrity.
     */
    public void retain(RgsEnum rgsEnum, PrintWriter log) throws Exception
    {

        // Build database if not already done, or if a mapping has already been generated
        if (classTree == null || hasMap)
        {
            hasMap = false;
            buildClassTree(log);
        }

        // Always retain native methods and their classes, using script entry:
        // .method;native ** * and_class
        classTree.retainMethod("**", "*", true, null, false, false, 
                               ClassConstants.ACC_NATIVE, 
                               ClassConstants.ACC_NATIVE);

        // Always retain the auto-generated values() and valueOf(...) 
        // methods in Enums, using script entries:
        // .method;public;static;final **/values * extends java/lang/Enum
        // .method;public;static **/valueOf * extends java/lang/Enum
        classTree.retainMethod
            ("**/values", "*", false, "java/lang/Enum", false, false, 
             ClassConstants.ACC_PUBLIC | 
             ClassConstants.ACC_STATIC | 
             ClassConstants.ACC_FINAL, 
             ClassConstants.ACC_PUBLIC | 
             ClassConstants.ACC_STATIC | 
             ClassConstants.ACC_FINAL);
        classTree.retainMethod
            ("**/valueOf", "*", false, "java/lang/Enum", false, false, 
             ClassConstants.ACC_PUBLIC | 
             ClassConstants.ACC_STATIC, 
             ClassConstants.ACC_PUBLIC | 
             ClassConstants.ACC_STATIC);

        // Enumerate the entries in the RGS script
        while (rgsEnum.hasMoreEntries())
        {
            RgsEntry entry = rgsEnum.nextEntry();
            try
            {
                switch (entry.type)
                {
                case RgsEntry.TYPE_OPTION:
                    if (ClassConstants.OPTION_DigestSHA.equals(entry.name))
                    {
                        enableDigestSHA = true;
                    }
                    else if (ClassConstants.OPTION_DigestMD5.equals(entry.name))
                    {
                        enableDigestMD5 = true;
                    }
                    else if (ClassConstants.OPTION_MapClassString.equals(entry.name))
                    {
                        enableMapClassString = true;
                    }
                    else if (ClassConstants.OPTION_Trim.equals(entry.name))
                    {
			// NOTE - already set in special RGS pass
                        //enableTrim = true;
                    }
                    else if (ClassConstants.OPTION_Repackage.equals(entry.name))
                    {
                        enableRepackage = true;
                    }
                    else if (ClassConstants.OPTION_Generic.equals(entry.name))
                    {
                        classTree.retainAttribute(ClassConstants.ATTR_Signature);
                    }
                    else if (ClassConstants.OPTION_LineNumberDebug.equals(entry.name))
                    {
                        classTree.retainAttribute(ClassConstants.ATTR_LineNumberTable);
                        classTree.retainAttribute(ClassConstants.ATTR_SourceFile);
                        enableDummySourceFile = true;
                    }
                    else if (ClassConstants.OPTION_RuntimeAnnotations.equals(entry.name))
                    {
                        classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleAnnotations);
                        classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleParameterAnnotations);
                        classTree.retainAttribute(ClassConstants.ATTR_AnnotationDefault);
                    }
                    else if (ClassConstants.OPTION_Annotations.equals(entry.name))
                    {
                        classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleAnnotations);
                        classTree.retainAttribute(ClassConstants.ATTR_RuntimeInvisibleAnnotations);
                        classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleParameterAnnotations);
                        classTree.retainAttribute(ClassConstants.ATTR_RuntimeInvisibleParameterAnnotations);
                        classTree.retainAttribute(ClassConstants.ATTR_AnnotationDefault);
                    }
                    else if (ClassConstants.OPTION_Enumeration.equals(entry.name))
                    {
// .option Enumeration - translates into
// .class ** public extends java/lang/Enum
                        classTree.retainClass("**", true, false, 
                                              false, false, false,
                                              "java/lang/Enum", false, false, 
                                              0, 0);
                    }
                    else if (ClassConstants.OPTION_Application.equals(entry.name))
                    {
// .option Application - translates into
// .method **/main ([Ljava/lang/String;)V and_class
                        classTree.retainMethod("**/main", 
                                               "([Ljava/lang/String;)V",
                                               true, null, false, false, 
                                               0, 0);
                    }
                    else if (ClassConstants.OPTION_Applet.equals(entry.name))
                    {
// .option Applet - translates into
// .class ** extends java/applet/Applet
                        classTree.retainClass("**", false, false, 
                                              false, false, false,
                                              "java/applet/Applet", false, false, 
                                              0, 0);
                    }
                    else if (ClassConstants.OPTION_RMI.equals(entry.name))
                    {
// .option RMI - translates into
// .option Serializable (see below for details)
// .class ** protected extends java/rmi/Remote
// .class **_Stub
// .class **_Skel
                        classTree.retainClass
                            ("**", false, 
                             true, // protected
                             false, false, false,
                             "java/rmi/Remote", false, false, 
                             0, 0);
                        classTree.retainClass
                            ("**_Stub", false, false, 
                             false, false, false,
                             null, false, false, 
                             0, 0);
                        classTree.retainClass
                            ("**_Skel", false, false, 
                             false, false, false,
                             null, false, false, 
                             0, 0);
                    }
                    if (ClassConstants.OPTION_Serializable.equals(entry.name) ||
                        ClassConstants.OPTION_RMI.equals(entry.name))
                    {
// .option Serializable - translates into
// .method;private **/writeObject (Ljava/io/ObjectOutputStream;)V extends java/io/Serializable
// .method;private **/readObject (Ljava/io/ObjectInputStream;)V extends java/io/Serializable
// .method **/writeReplace ()Ljava/lang/Object; extends java/io/Serializable
// .method **/readResolve ()Ljava/lang/Object; extends java/io/Serializable
// .field;static;final **/serialVersionUID J extends java/io/Serializable
// .field;static;final **/serialPersistentFields [Ljava/io/ObjectStreamField; extends java/io/Serializable
// .class ** extends java/io/Serializable
// .field;!transient;!static ** * extends java/io/Serializable
                        classTree.retainMethod
                            ("**/writeObject", 
                             "(Ljava/io/ObjectOutputStream;)V",
                             false, "java/io/Serializable", false, false, 
                             ClassConstants.ACC_PRIVATE, 
                             ClassConstants.ACC_PRIVATE);
                        classTree.retainMethod
                            ("**/readObject", 
                             "(Ljava/io/ObjectInputStream;)V",
                             false, "java/io/Serializable", false, false, 
                             ClassConstants.ACC_PRIVATE, 
                             ClassConstants.ACC_PRIVATE);
                        classTree.retainMethod
                            ("**/writeReplace", 
                             "()Ljava/lang/Object;",
                             false, "java/io/Serializable", false, false, 
                             0, 0);
                        classTree.retainMethod
                            ("**/readResolve", 
                             "()Ljava/lang/Object;",
                             false, "java/io/Serializable", false, false, 
                             0, 0);
                        classTree.retainField
                            ("**/serialVersionUID", 
                             "J", 
                             false, "java/io/Serializable", false, false, 
                             ClassConstants.ACC_STATIC | 
                             ClassConstants.ACC_FINAL, 
                             ClassConstants.ACC_STATIC | 
                             ClassConstants.ACC_FINAL);
                        classTree.retainField
                            ("**/serialPersistentFields", 
                             "[Ljava/io/ObjectStreamField;",
                             false, "java/io/Serializable", false, false, 
                             ClassConstants.ACC_PRIVATE | 
                             ClassConstants.ACC_STATIC | 
                             ClassConstants.ACC_FINAL, 
                             ClassConstants.ACC_PRIVATE | 
                             ClassConstants.ACC_STATIC | 
                             ClassConstants.ACC_FINAL);
                        classTree.retainClass
                            ("**", false, false, 
                             false, false, false,
                             "java/io/Serializable", false, false, 
                             0, 0);
                        classTree.retainField
                            ("**", "*", 
                             false, "java/io/Serializable", false, false, 
                             ClassConstants.ACC_TRANSIENT | 
                             ClassConstants.ACC_STATIC,
                             0);
                    }
                    break;

                case RgsEntry.TYPE_ATTR:
                    classTree.retainAttribute(entry.name);
                    break;

                case RgsEntry.TYPE_NOWARN:
                    classTree.noWarnClass(entry.name);
                    break;

                case RgsEntry.TYPE_CLASS:
                case RgsEntry.TYPE_NOTRIM_CLASS:
                case RgsEntry.TYPE_NOT_CLASS:
                    classTree.retainClass(entry.name, 
                                          entry.retainToPublic, 
                                          entry.retainToProtected, 
                                          entry.retainPubProtOnly,
                                          entry.retainFieldsOnly, 
                                          entry.retainMethodsOnly,
                                          entry.extendsName,
                                          entry.type == entry.TYPE_NOT_CLASS,
                                          entry.type == entry.TYPE_NOTRIM_CLASS,
                                          entry.accessMask,
                                          entry.accessSetting);
                    break;

                case RgsEntry.TYPE_METHOD:
                case RgsEntry.TYPE_NOTRIM_METHOD:
                case RgsEntry.TYPE_NOT_METHOD:
                    classTree.retainMethod(entry.name, entry.descriptor,
                                           entry.retainAndClass,
                                           entry.extendsName,
                                           entry.type == entry.TYPE_NOT_METHOD,
                                           entry.type == entry.TYPE_NOTRIM_METHOD,
                                           entry.accessMask,
                                           entry.accessSetting);
                    break;

                case RgsEntry.TYPE_FIELD:
                case RgsEntry.TYPE_NOT_FIELD:
                    classTree.retainField(entry.name, entry.descriptor,
                                          entry.retainAndClass,
                                          entry.extendsName,
                                          entry.type == entry.TYPE_NOT_FIELD,
                                          entry.type == entry.TYPE_NOTRIM_FIELD,
                                          entry.accessMask,
                                          entry.accessSetting);
                    break;

                case RgsEntry.TYPE_PACKAGE_MAP:
                    classTree.retainPackageMap(entry.name, entry.obfName);
                    break;

                case RgsEntry.TYPE_REPACKAGE_MAP:
                    classTree.retainRepackageMap(entry.name, entry.obfName);
                    break;

                case RgsEntry.TYPE_CLASS_MAP:
                    classTree.retainClassMap(entry.name, entry.obfName);
                    break;

                case RgsEntry.TYPE_METHOD_MAP:
                    classTree.retainMethodMap(entry.name, entry.descriptor, 
                                              entry.obfName);
                    break;

                case RgsEntry.TYPE_FIELD_MAP:
                    classTree.retainFieldMap(entry.name, entry.obfName);
                    break;

                default:
                    throw new Exception("Illegal type received from the .rgs script");
                }
            }
            catch (Exception e)
            {
                log.println(WARNING_SCRIPT_ENTRY_ABSENT + entry.name);
            }
        }
    }

    /** Write any non-suppressed warnings to the log. */
    public void logWarnings(PrintWriter log) throws Exception
    {
        if (classTree != null) 
        {
            classTree.logWarnings(log);
        }
    }

    /** Generate a mapping table for obfuscation. */
    public void createMap(PrintWriter log) throws Exception
    {
        // Build database if not already done
        if (classTree == null)
        {
            buildClassTree(log);
        }

        // Traverse the class tree, generating obfuscated names within
        // package and class namespaces
        classTree.generateNames(enableRepackage);

        // Resolve the polymorphic dependencies of each class, generating
        // non-private method and field names for each namespace
        classTree.resolveClasses();

        // Signal that the namespace maps have been created
        hasMap = true;

        // Write the memory usage at this point to the log file
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        log.println("#");
        log.println(LOG_MEMORY_USED + Long.toString(rt.totalMemory() - rt.freeMemory()) + LOG_MEMORY_BYTES);
        log.println(LOG_MEMORY_TOTAL + Long.toString(rt.totalMemory()) + LOG_MEMORY_BYTES);
        log.println("#");
    }

    /**
     * If trim requested, mark all classes, methods, fields for trimming,
     * then traverse method calls from isFixed methods, untrimming methods,
     * fields, and classes touched.
     */
    public void trim(PrintWriter log) throws Exception
    {
        // Generate map table if not already done
        if (!hasMap)
        {
            createMap(log);
        }

	if (enableTrim) {
	    // Mark all for trimming
	    classTree.walkTree(new TreeAction() 
	    {
		public void classAction(Cl cl) { 
		    cl.setTrimCheck(false); 
		    cl.setTrimmed(true); } 
		public void methodAction(Md md) { 
		    md.setTrimCheck(false);
		    md.setTrimmed(true); }
		public void fieldAction(Fd fd)  { 
		    fd.setTrimCheck(false);
		    fd.setTrimmed(true); }
		public void packageAction(Pk pk) { }
	    });
	    // Add script-fixed and inheritance-fixed items to stack
	    final TIStack stack = new TIStack();
	    classTree.walkTree(new TreeAction() 
	    {
		public void classAction(Cl cl)  
                { 
		    if (cl.isFromScript()) stack.push(cl); 
                }
		public void methodAction(Md md)
                { 
		    if (md.isFromScript()) stack.push(md); 
                    // NOTE - Push all methods which are overrides indirectly
                    // onto the stack via their parent class (only doing the 
                    // actual push later, if their class is referenced). This
                    // is still over-conservative, since the override method
                    // could be safely trimmed if none of its super 
                    // implementations is called. 
                    if (md.isOverride()) 
                    {
                        md.getParent().addRef(md);
                    }
                }
		public void fieldAction(Fd fd) 
                { 
		    if (fd.isFromScript()) stack.push(fd); 
                    // NOTE - See above
                    if (fd.isOverride()) 
                    {
                        fd.getParent().addRef(fd);
                    }
                }
		public void packageAction(Pk pk) { }
	    });
            // Process each, which will involve the addition and processing
            // of dependent references
	    while (!stack.empty())
	    {
		TreeItem ti = (TreeItem)stack.pop();
		ti.pushRefs(stack);
	    }
	}
    }

    /** Remap each class based on the remap database, and remove attributes. */
    public void remapTo(File out, PrintWriter log) throws Exception
    {
        // Generate map table if not already done
        if (!hasMap)
        {
            createMap(log);
        }

        // Write the name frequency and name mapping table to the log file
        classTree.dump(log);

        // Go through the input Jar, removing attributes and remapping the Constant Pool
        // for each class file. Other files are copied through unchanged, except for manifest
        // and any signature files - these are deleted and the manifest is regenerated.
        Enumeration entries = inJar.entries();
        ZipOutputStream outJar = null;
        try
        {
            outJar = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
            // No comment in Pro, to reduce output jar size
            if (Version.isLite)
            {
                outJar.setComment(Version.getJarComment());
            }
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
                    if (inName.length() > CLASS_EXT.length() &&
                        inName.substring(inName.length() - CLASS_EXT.length(), inName.length()).equals(CLASS_EXT))
                    {
                        // Write obfuscated class to the output Jar
                        ClassFile cf = ClassFile.create(inStream);
                        // To reduce output jar size in Pro, no class ID string
                        if (Version.isLite)
                        {
                            cf.setIdString(Version.getClassIdString());
                        }
                        Cl cl = classTree.getCl(cf.getName());
                        // Trim entire class if requested
                        if (cl != null && !cl.isTrimmed())
                        {
                            cf.trimAttrs(classTree);
                            cl.trimClassFile(cf);
                            cf.updateRefCount();
                            cf.remap(classTree, log, enableMapClassString, 
                                     enableDummySourceFile);
                            ZipEntry outEntry = 
                                new ZipEntry(cf.getName() + CLASS_EXT);
                            outJar.putNextEntry(outEntry);
                        
                            // Create an OutputStream piped through a number of digest generators for the manifest
                            MessageDigest shaDigest = null;
                            MessageDigest md5Digest = null;
                            OutputStream outputStream = outJar;
                            if (enableDigestSHA)
                            {
                                shaDigest = MessageDigest.getInstance("SHA-1");
                                outputStream = 
                                    new DigestOutputStream(outputStream, 
                                                           shaDigest);
                            }
                            if (enableDigestMD5)
                            {
                                md5Digest = MessageDigest.getInstance("MD5");
                                outputStream = 
                                    new DigestOutputStream(outputStream, 
                                                           md5Digest);
                            }
                            DataOutputStream dataOutputStream = 
                                new DataOutputStream(outputStream);
                            
                            // Dump the classfile, while creating the digests
                            cf.write(dataOutputStream);
                            dataOutputStream.flush();
                            outJar.closeEntry();
                            
                            // Now update the manifest entry for the class with new name and new digests
                            MessageDigest[] digests = {shaDigest, md5Digest};
                            updateManifest(inName, cf.getName() + CLASS_EXT, 
                                           digests);
                        }
                    }
                    else if (STREAM_NAME_MANIFEST.equals(inName.toUpperCase()) ||
                             (inName.length() > (SIGNATURE_PREFIX.length() + 1 + SIGNATURE_EXT.length()) &&
                              inName.indexOf(SIGNATURE_PREFIX) != -1 && 
                              inName.substring(inName.length() - SIGNATURE_EXT.length(), inName.length()).equals(SIGNATURE_EXT)))
                    {
                        // Don't pass through the manifest or signature files
                        continue;
                    }
                    else
                    {
                        // Copy the non-class entry through unchanged
                        long size = inEntry.getSize();
                        if (size != -1)
                        {
                            byte[] bytes = new byte[(int)size];
                            inStream.readFully(bytes);
                            String outName = classTree.getOutName(inName);
                            ZipEntry outEntry = new ZipEntry(outName);
                            outJar.putNextEntry(outEntry);
                            
                            // Create an OutputStream piped through a number of digest generators for the manifest
                            MessageDigest shaDigest = null;
                            MessageDigest md5Digest = null;
                            OutputStream outputStream = outJar;
                            if (enableDigestSHA)
                            {
                                shaDigest = MessageDigest.getInstance("SHA-1");
                                outputStream = 
                                    new DigestOutputStream(outputStream, 
                                                           shaDigest);
                            }
                            if (enableDigestMD5)
                            {
                                md5Digest = MessageDigest.getInstance("MD5");
                                outputStream = 
                                    new DigestOutputStream(outputStream, 
                                                           md5Digest);
                            }
                            DataOutputStream dataOutputStream = 
                                new DataOutputStream(outputStream);

                            // Dump the data, while creating the digests
                            dataOutputStream.write(bytes, 0, bytes.length);
                            dataOutputStream.flush();
                            outJar.closeEntry();
                    
                            // Now update the manifest entry for the entry with new name and new digests
                            MessageDigest[] digests = {shaDigest, md5Digest};
                            updateManifest(inName, outName, digests);
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
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outJar)));
            newManifest.writeString(writer);
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

    /** Close input JAR file. */
    public void close() throws Exception {
        if (inJar != null) 
        {
            inJar.close();
            inJar = null;
        }
    }

    // Parse the RFC822-style MANIFEST.MF file
    private void parseManifest() throws Exception
    {
        // The manifest file is the first in the jar and is called
        // (case insensitively) 'MANIFEST.MF'
        oldManifest = new SectionList();
        Enumeration entries = inJar.entries();
        while (entries.hasMoreElements()) 
        {
            // Get the first entry only from the input Jar
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
        Section version = oldManifest.find(MANIFEST_VERSION_TAG, 
                                           MANIFEST_VERSION_VALUE);
        if (version == null) 
        {
            version = new Section();
            version.add(MANIFEST_VERSION_TAG, MANIFEST_VERSION_VALUE);
        }
        newManifest.add(version);

        // copy through all the none-filename sections, apart from the version
        for (Enumeration enm = oldManifest.elements(); 
             enm.hasMoreElements(); ) 
        {
            Section section = (Section)enm.nextElement();
            if (section != null && section != version) 
            {
                Header name = section.findTag(MANIFEST_NAME_TAG);
                if (name == null) 
                {
                    newManifest.add(section);
                } 
                else 
                {
                    String value = name.getValue();
                    if (value.length() > 0 && 
                        value.charAt(value.length() - 1) == '/') 
                    {
                        newManifest.add(section);
                    }
                }
            }
        }
    }

    // Update an entry in the manifest file
    private void updateManifest(String inName, String outName, 
                                MessageDigest[] digests)
    {
        // Check for section in old manifest
        Section oldSection = oldManifest.find(MANIFEST_NAME_TAG, inName);
        if (oldSection != null)
        {
            // Create fresh section for entry, and enter "Name" header
            Section newSection = new Section();
            newSection.add(MANIFEST_NAME_TAG, outName);

            // Copy over non-"Name", non-digest entries
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

            // Create fresh digest entries in the new section
            if (digests != null && digests.length > 0)
            {
                // Digest-Algorithms header
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < digests.length; i++)
                {
                    if (digests[i] != null)
                    {
                        sb.append(digests[i].getAlgorithm());
                        sb.append(" ");
                    }
                }
                if (sb.length() > 0) 
                {
                    newSection.add(MANIFEST_DIGESTALG_TAG, sb.toString());
                }
                
                // *-Digest headers
                for (int i = 0; i < digests.length; i++)
                {
                    if (digests[i] != null)
                    {
                        newSection.add(digests[i].getAlgorithm() + "-Digest", 
                                       Tools.toBase64(digests[i].digest()));
                    }
                }            
            }
            
            // Append the new section to the new manifest
            newManifest.add(newSection);
        }
    }
}

// Stack used for marking TreeItems that must not be trimmed
class TIStack extends Stack
{
    public Object push(Object o)
    {
        try
        {
            if (o != null)
            {
                TreeItem ti = (TreeItem)o;
                if (ti instanceof Cl)
                {
                    pushClTree((Cl)ti);
                }
                else if (ti instanceof MdFd)
                {
                    MdFd mdfd = (MdFd)ti;
                    // Preserve class if method or field is static
                    Cl cl = (Cl)mdfd.getParent();
                    if (mdfd.isStatic())
                    {
                        pushClTree(cl);
                    }
                    if (mdfd instanceof Fd)
                    {
                        pushFdGroup(cl, mdfd.getInName());
                    }
                    else // method
                    {
                        Md md = (Md)mdfd;
                        String mdName = md.getInName();
                        // Treat special methods (<init> <clinit>) differently
                        if (mdName.charAt(0) == '<')
                        {
                            pushItem(md);
                        }
                        else
                        {
                            pushMdGroup(cl, mdName, md.getDescriptor());
                        }
                    }
                }
            }
        } catch (Exception e) { /* ignore */ }
        return o;
    }
    // Push class and all supers onto trim-preserve stack
    private Object pushClTree(Cl cl) throws Exception
    {
        if (cl != null)
        {
            pushItem(cl);
            // Propagate up supers in jar
            pushClTree(cl.getSuperCl());
            for (Enumeration enm = cl.getSuperInterfaces();
                 enm.hasMoreElements();
                 pushClTree((Cl)enm.nextElement()));
        }
        return cl;
    }
    // Push item onto trim-preserve stack
    private void pushItem(TreeItem ti)
    {
        if (ti != null && !ti.isTrimCheck())
        {
            ti.setTrimCheck(true);
            ti.setTrimmed(false);
            super.push(ti);
        }
    }
    // Push method across inheritance group onto trim-preserve stack
    private void pushMdGroup(Cl cl, String mdName, String mdDesc) throws Exception
    {
        pushMdFdGroup(cl, mdName, mdDesc);
    }
    // Push field across inheritance group onto trim-preserve stack
    private void pushFdGroup(Cl cl, String fdName) throws Exception
    {
        pushMdFdGroup(cl, fdName, null);
    }
    private void pushMdFdGroup(Cl cl, String name, String desc) throws Exception
    {
        if (cl != null)
        {
            final String fname = name;
            final String fdesc = desc;
            cl.walkGroup(new TreeAction() {
                public void classAction(Cl cl)
                { 
                    try {
                        MdFd mdfd = (fdesc == null ? 
                                     (MdFd)cl.getField(fname) :
                                     (MdFd)cl.getMethod(fname, fdesc));
                        pushItem(mdfd);
                    } catch (Exception e) { /* ignore */ }
                }
            });
        }
    }
}


