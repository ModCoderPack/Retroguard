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
 * @author Mark Welsh
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
    private static final String WARNING_INCOMPATIBLE_VERSION_1 =
        "# WARNING - class file format has incompatible major-version number: v";
    private static final String WARNING_INCOMPATIBLE_VERSION_2 =
        "# WARNING - this version of RetroGuard supports up to class format:  v";


    // Fields ----------------------------------------------------------------
    /**
     * JAR file for obfuscation
     */
    private ZipFile inJar;

    /**
     * MANIFEST.MF RFC822-style data from old Jar
     */
    private SectionList oldManifest;

    /**
     * MANIFEST.MF RFC822-style data for new Jar
     */
    private SectionList newManifest;

    /**
     * Tree of packages, classes. methods, fields
     */
    private ClassTree classTree;

    /**
     * Has the mapping been generated already?
     */
    private boolean hasMap = false;

    /**
     * Remap strings in reflection?
     */
    private boolean enableMapClassString = false;

    /**
     * Repackage classes for size?
     */
    private boolean enableRepackage = false;

    /**
     * Produce SHA-1 manifest digests?
     */
    private boolean enableDigestSHA = false;

    /**
     * Produce MD5 manifest digests?
     */
    private boolean enableDigestMD5 = false;


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /**
     * A classfile database for obfuscation.
     * 
     * @param inFile
     * @throws IOException
     */
    public GuardDB(File inFile) throws IOException
    {
        this.inJar = new ZipFile(inFile);
        this.parseManifest();
    }

    /**
     * Close input JAR file and log-file at GC-time.
     */
    @Override
    protected void finalize()
    {
        this.close();
    }

    /**
     * Create a classfile database.
     * 
     * @param log
     * @throws IOException
     * @throws ClassFileException
     */
    public void buildClassTree(PrintWriter log) throws IOException, ClassFileException
    {
        // Go through the input Jar, adding each class file to the database
        int incompatibleVersion = 0;
        this.classTree = new ClassTree();
        Enumeration entries = this.inJar.entries();
        while (entries.hasMoreElements())
        {
            // Get the next entry from the input Jar
            ZipEntry inEntry = (ZipEntry)entries.nextElement();
            String name = inEntry.getName();
            if ((name.length() > GuardDB.CLASS_EXT.length())
                && name.substring(name.length() - GuardDB.CLASS_EXT.length(), name.length()).equals(GuardDB.CLASS_EXT))
            {
                // Create a full internal representation of the class file
                DataInputStream inStream = new DataInputStream(new BufferedInputStream(this.inJar.getInputStream(inEntry)));
                ClassFile cf = null;
                try
                {
                    cf = ClassFile.create(inStream);
                }
                catch (ClassFileException e)
                {
                    // TODO printStackTrace
                    e.printStackTrace();
                    log.println(GuardDB.ERROR_CORRUPT_CLASS + name + " (" + e.getMessage() + ")");
                }
                finally
                {
                    inStream.close();
                }

                if (cf != null)
                {
                    if (cf.hasIncompatibleVersion())
                    {
                        incompatibleVersion = cf.getMajorVersion();
                    }
                    this.classTree.addClassFile(cf);
                }
            }
        }
        // Warn if classes are incompatible version of class file format
        if (incompatibleVersion != 0)
        {
            log.println(GuardDB.WARNING_INCOMPATIBLE_VERSION_1 + incompatibleVersion);
            log.println(GuardDB.WARNING_INCOMPATIBLE_VERSION_2 + ClassConstants.MAJOR_VERSION);
        }
    }

    /**
     * Go through database marking certain entities for retention, while maintaining polymorphic integrity.
     * 
     * @param rgsIter
     * @param log
     * @throws IOException
     * @throws ClassFileException
     * @throws RGSException
     */
    public void retain(RgsEnum rgsIter, PrintWriter log) throws IOException, ClassFileException, RGSException
    {

        // Build database if not already done, or if a mapping has already been generated
        if ((this.classTree == null) || this.hasMap)
        {
            this.hasMap = false;
            this.buildClassTree(log);
        }

        try
        {

// Always retain native methods and their classes, using script entry:
// .method;native ** * and_class
            this.classTree.retainMethod("**", "*", true, null, false, ClassConstants.ACC_NATIVE, ClassConstants.ACC_NATIVE);

// Always retain the auto-generated values() and valueOf(...) methods in Enums, using script entries:
// .method;public;static;final **/values * extends java/lang/Enum
// .method;public;static **/valueOf * extends java/lang/Enum
            this.classTree.retainMethod("**/values", "*", false, "java/lang/Enum", false,
                ClassConstants.ACC_PUBLIC | ClassConstants.ACC_STATIC | ClassConstants.ACC_FINAL,
                ClassConstants.ACC_PUBLIC | ClassConstants.ACC_STATIC | ClassConstants.ACC_FINAL);
            this.classTree.retainMethod("**/valueOf", "*", false, "java/lang/Enum", false,
                ClassConstants.ACC_PUBLIC | ClassConstants.ACC_STATIC,
                ClassConstants.ACC_PUBLIC | ClassConstants.ACC_STATIC);
        }
        catch (ClassFileException e)
        {
            // TODO printStackTrace
            e.printStackTrace();
            // shouldn't get here
        }

        // Iterate the entries in the RGS script
        while (rgsIter.hasNext())
        {
            RgsEntry entry = rgsIter.next();
            try
            {
                switch (entry.type)
                {
                    case OPTION:
                        if (ClassConstants.OPTION_DigestSHA.equals(entry.name))
                        {
                            this.enableDigestSHA = true;
                        }
                        else if (ClassConstants.OPTION_DigestMD5.equals(entry.name))
                        {
                            this.enableDigestMD5 = true;
                        }
                        else if (ClassConstants.OPTION_MapClassString.equals(entry.name))
                        {
                            this.enableMapClassString = true;
                        }
                        else if (ClassConstants.OPTION_Repackage.equals(entry.name))
                        {
                            this.enableRepackage = true;
                        }
                        else if (ClassConstants.OPTION_Generic.equals(entry.name))
                        {
                            this.classTree.retainAttribute(ClassConstants.ATTR_Signature);
                        }
                        else if (ClassConstants.OPTION_LineNumberDebug.equals(entry.name))
                        {
                            this.classTree.retainAttribute(ClassConstants.ATTR_LineNumberTable);
                            this.classTree.retainAttribute(ClassConstants.ATTR_SourceFile);
                        }
                        else if (ClassConstants.OPTION_RuntimeAnnotations.equals(entry.name))
                        {
                            this.classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleAnnotations);
                            this.classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleParameterAnnotations);
                            this.classTree.retainAttribute(ClassConstants.ATTR_AnnotationDefault);
                        }
                        else if (ClassConstants.OPTION_Annotations.equals(entry.name))
                        {
                            this.classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleAnnotations);
                            this.classTree.retainAttribute(ClassConstants.ATTR_RuntimeInvisibleAnnotations);
                            this.classTree.retainAttribute(ClassConstants.ATTR_RuntimeVisibleParameterAnnotations);
                            this.classTree.retainAttribute(ClassConstants.ATTR_RuntimeInvisibleParameterAnnotations);
                            this.classTree.retainAttribute(ClassConstants.ATTR_AnnotationDefault);
                        }
                        else if (ClassConstants.OPTION_Enumeration.equals(entry.name))
                        {
// .option Enumeration - translates into
// .class ** public extends java/lang/Enum
                            this.classTree.retainClass("**", true, false, false, false, false, "java/lang/Enum", false, 0, 0);
                        }
                        else if (ClassConstants.OPTION_Application.equals(entry.name))
                        {
// .option Application - translates into
// .method **/main ([Ljava/lang/String;)V and_class
                            this.classTree.retainMethod("**/main", "([Ljava/lang/String;)V", true, null, false, 0, 0);
                        }
                        else if (ClassConstants.OPTION_Applet.equals(entry.name))
                        {
// .option Applet - translates into
// .class ** extends java/applet/Applet
                            this.classTree.retainClass("**", false, false, false, false, false, "java/applet/Applet", false, 0, 0);
                        }
                        else if (ClassConstants.OPTION_RMI.equals(entry.name))
                        {
// .option RMI - translates into
// .option Serializable (see below for details)
// .class ** protected extends java/rmi/Remote
// .class **_Stub
// .class **_Skel
                            this.classTree.retainClass("**", false, true, false, false, false, "java/rmi/Remote", false, 0, 0);
                            this.classTree.retainClass("**_Stub", false, false, false, false, false, null, false, 0, 0);
                            this.classTree.retainClass("**_Skel", false, false, false, false, false, null, false, 0, 0);
                        }
                        if (ClassConstants.OPTION_Serializable.equals(entry.name) || ClassConstants.OPTION_RMI.equals(entry.name))
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
                            this.classTree.retainMethod("**/writeObject", "(Ljava/io/ObjectOutputStream;)V", false,
                                "java/io/Serializable", false, ClassConstants.ACC_PRIVATE, ClassConstants.ACC_PRIVATE);
                            this.classTree.retainMethod("**/readObject", "(Ljava/io/ObjectInputStream;)V", false,
                                "java/io/Serializable", false, ClassConstants.ACC_PRIVATE, ClassConstants.ACC_PRIVATE);
                            this.classTree.retainMethod("**/writeReplace", "()Ljava/lang/Object;", false, "java/io/Serializable",
                                false, 0, 0);
                            this.classTree.retainMethod("**/readResolve", "()Ljava/lang/Object;", false, "java/io/Serializable",
                                false, 0, 0);
                            this.classTree.retainField("**/serialVersionUID", "J", false, "java/io/Serializable", false,
                                ClassConstants.ACC_STATIC | ClassConstants.ACC_FINAL,
                                ClassConstants.ACC_STATIC | ClassConstants.ACC_FINAL);
                            this.classTree.retainField("**/serialPersistentFields", "[Ljava/io/ObjectStreamField;", false,
                                "java/io/Serializable", false,
                                ClassConstants.ACC_PRIVATE | ClassConstants.ACC_STATIC | ClassConstants.ACC_FINAL,
                                ClassConstants.ACC_PRIVATE | ClassConstants.ACC_STATIC | ClassConstants.ACC_FINAL);
                            this.classTree.retainClass("**", false, false, false, false, false, "java/io/Serializable",
                                false, 0, 0);
                            this.classTree.retainField("**", "*", false, "java/io/Serializable", false,
                                ClassConstants.ACC_TRANSIENT | ClassConstants.ACC_STATIC, 0);
                        }
                        break;

                    case ATTR:
                        this.classTree.retainAttribute(entry.name);
                        break;

                    case NOWARN:
                        this.classTree.noWarnClass(entry.name);
                        break;

                    case CLASS:
                    case NOT_CLASS:
                        this.classTree.retainClass(entry.name, entry.retainToPublic, entry.retainToProtected,
                            entry.retainPubProtOnly, entry.retainFieldsOnly, entry.retainMethodsOnly, entry.extendsName,
                            entry.type == RgsEntryType.NOT_CLASS, entry.accessMask, entry.accessSetting);
                        break;

                    case METHOD:
                    case NOT_METHOD:
                        this.classTree.retainMethod(entry.name, entry.descriptor, entry.retainAndClass, entry.extendsName,
                            entry.type == RgsEntryType.NOT_METHOD, entry.accessMask, entry.accessSetting);
                        break;

                    case FIELD:
                    case NOT_FIELD:
                        this.classTree.retainField(entry.name, entry.descriptor, entry.retainAndClass, entry.extendsName,
                            entry.type == RgsEntryType.NOT_FIELD, entry.accessMask, entry.accessSetting);
                        break;

                    case PACKAGE_MAP:
                        this.classTree.retainPackageMap(entry.name, entry.obfName);
                        break;

                    case REPACKAGE_MAP:
                        this.classTree.retainRepackageMap(entry.name, entry.obfName);
                        break;

                    case CLASS_MAP:
                        this.classTree.retainClassMap(entry.name, entry.obfName);
                        break;

                    case METHOD_MAP:
                        this.classTree.retainMethodMap(entry.name, entry.descriptor, entry.obfName);
                        break;

                    case FIELD_MAP:
                        this.classTree.retainFieldMap(entry.name, entry.obfName);
                        break;

                    default:
                        throw new RGSException("Illegal type received from the .rgs script");
                }
            }
            catch (ClassFileException e)
            {
                // TODO printStackTrace
                e.printStackTrace();
                log.println(GuardDB.WARNING_SCRIPT_ENTRY_ABSENT + entry.name);
            }
            catch (RGSException e)
            {
                // TODO printStackTrace
                e.printStackTrace();
                log.println(GuardDB.WARNING_SCRIPT_ENTRY_ABSENT + entry.name);
            }
        }
    }

    /**
     * Write any non-suppressed warnings to the log.
     * 
     * @param log
     */
    public void logWarnings(PrintWriter log)
    {
        if (this.classTree != null)
        {
            this.classTree.logWarnings(log);
        }
    }

    /**
     * Generate a mapping table for obfuscation.
     * 
     * @param log
     * @throws ClassFileException
     * @throws IOException
     */
    public void createMap(PrintWriter log) throws IOException, ClassFileException
    {
        // Build database if not already done
        if (this.classTree == null)
        {
            this.buildClassTree(log);
        }

        // TODO: Searge: check if those two walks are obsolete
        this.classTree.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl)
            {
                cl.resetResolve();
            }
        });
        this.classTree.walkTree(new TreeAction()
        {
            @Override
            public void classAction(Cl cl) throws ClassFileException
            {
                cl.setupNameListDowns();
            }
        });

        // Traverse the class tree, generating obfuscated names within package and class namespaces
        this.classTree.generateNames(this.enableRepackage);

        // Resolve the polymorphic dependencies of each class, generating non-private method and field names for each namespace
        this.classTree.resolveClasses();

        // Signal that the namespace maps have been created
        this.hasMap = true;

        // Write the memory usage at this point to the log file
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        log.println("#");
        log.println(GuardDB.LOG_MEMORY_USED + Long.toString(rt.totalMemory() - rt.freeMemory()) + GuardDB.LOG_MEMORY_BYTES);
        log.println(GuardDB.LOG_MEMORY_TOTAL + Long.toString(rt.totalMemory()) + GuardDB.LOG_MEMORY_BYTES);
        log.println("#");
    }

    /**
     * Remap each class based on the remap database, and remove attributes.
     * 
     * @param out
     * @param log
     * @throws IOException
     * @throws ClassFileException
     */
    public void remapTo(File out, PrintWriter log) throws IOException, ClassFileException
    {
        // Generate map table if not already done
        if (!this.hasMap)
        {
            this.createMap(log);
        }

        // Write the name frequency and name mapping table to the log file
        this.classTree.dump(log);

        // Go through the input Jar, removing attributes and remapping the Constant Pool for each class file. Other files are
        // copied through unchanged, except for manifest and any signature files - these are deleted and the manifest is
        // regenerated.
        Enumeration entries = this.inJar.entries();
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
                    inStream = new DataInputStream(new BufferedInputStream(this.inJar.getInputStream(inEntry)));
                    String inName = inEntry.getName();
                    if ((inName.length() > GuardDB.CLASS_EXT.length()) && inName.substring(
                        inName.length() - GuardDB.CLASS_EXT.length(), inName.length()).equals(GuardDB.CLASS_EXT))
                    {
                        // Write obfuscated class to the output Jar
                        ClassFile cf = ClassFile.create(inStream);
                        // To reduce output jar size in Pro, no class ID string
                        if (Version.isLite)
                        {
                            cf.setIdString(Version.getClassIdString());
                        }
                        Cl cl = this.classTree.getCl(cf.getName());
                        // Trim entire class if requested
                        if (cl != null)
                        {
                            cf.trimAttrs(this.classTree);
                            cf.updateRefCount();
                            cf.remap(this.classTree, log, this.enableMapClassString);
                            ZipEntry outEntry = new ZipEntry(cf.getName() + GuardDB.CLASS_EXT);
                            outJar.putNextEntry(outEntry);

                            // Create an OutputStream piped through a number of digest generators for the manifest
                            MessageDigest shaDigest = null;
                            MessageDigest md5Digest = null;
                            OutputStream outputStream = outJar;
                            if (this.enableDigestSHA)
                            {
                                try
                                {
                                    shaDigest = MessageDigest.getInstance("SHA-1");
                                    outputStream = new DigestOutputStream(outputStream, shaDigest);
                                }
                                catch (NoSuchAlgorithmException e)
                                {
                                    // TODO printStackTrace
                                    e.printStackTrace();
                                    this.enableDigestSHA = false;
                                }
                            }
                            if (this.enableDigestMD5)
                            {
                                try
                                {
                                    md5Digest = MessageDigest.getInstance("MD5");
                                    outputStream = new DigestOutputStream(outputStream, md5Digest);
                                }
                                catch (NoSuchAlgorithmException e)
                                {
                                    // TODO printStackTrace
                                    e.printStackTrace();
                                    this.enableDigestMD5 = false;
                                }
                            }
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                            // Dump the classfile, while creating the digests
                            cf.write(dataOutputStream);
                            dataOutputStream.flush();
                            outJar.closeEntry();

                            // Now update the manifest entry for the class with new name and new digests
                            MessageDigest[] digests =
                            {
                                shaDigest, md5Digest
                            };
                            this.updateManifest(inName, cf.getName() + GuardDB.CLASS_EXT, digests);
                        }
                    }
                    else if (GuardDB.STREAM_NAME_MANIFEST.equals(inName.toUpperCase())
                        || ((inName.length() > (GuardDB.SIGNATURE_PREFIX.length() + 1 + GuardDB.SIGNATURE_EXT.length()))
                            && (inName.indexOf(GuardDB.SIGNATURE_PREFIX) != -1) && inName.substring(
                            inName.length() - GuardDB.SIGNATURE_EXT.length(), inName.length()).equals(GuardDB.SIGNATURE_EXT)))
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
                            String outName = this.classTree.getOutName(inName);
                            ZipEntry outEntry = new ZipEntry(outName);
                            outJar.putNextEntry(outEntry);

                            // Create an OutputStream piped through a number of digest generators for the manifest
                            MessageDigest shaDigest = null;
                            MessageDigest md5Digest = null;
                            OutputStream outputStream = outJar;
                            if (this.enableDigestSHA)
                            {
                                try
                                {
                                    shaDigest = MessageDigest.getInstance("SHA-1");
                                    outputStream = new DigestOutputStream(outputStream, shaDigest);
                                }
                                catch (NoSuchAlgorithmException e)
                                {
                                    // TODO printStackTrace
                                    e.printStackTrace();
                                    this.enableDigestSHA = false;
                                }
                            }
                            if (this.enableDigestMD5)
                            {
                                try
                                {
                                    md5Digest = MessageDigest.getInstance("MD5");
                                    outputStream = new DigestOutputStream(outputStream, md5Digest);
                                }
                                catch (NoSuchAlgorithmException e)
                                {
                                    // TODO printStackTrace
                                    e.printStackTrace();
                                    this.enableDigestMD5 = false;
                                }
                            }
                            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                            // Dump the data, while creating the digests
                            dataOutputStream.write(bytes, 0, bytes.length);
                            dataOutputStream.flush();
                            outJar.closeEntry();

                            // Now update the manifest entry for the entry with new name and new digests
                            MessageDigest[] digests =
                            {
                                shaDigest, md5Digest
                            };
                            this.updateManifest(inName, outName, digests);
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
            ZipEntry outEntry = new ZipEntry(GuardDB.STREAM_NAME_MANIFEST);
            outJar.putNextEntry(outEntry);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outJar)));
            this.newManifest.writeString(writer);
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

    /**
     * Close input JAR file.
     */
    public void close()
    {
        if (this.inJar != null)
        {
            try
            {
                this.inJar.close();
            }
            catch (IOException e)
            {
                // TODO printStackTrace
                e.printStackTrace();
            }
            this.inJar = null;
        }
    }

    /**
     * Parse the RFC822-style MANIFEST.MF file
     * 
     * @throws IOException
     */
    private void parseManifest() throws IOException
    {
        // The manifest file is the first in the jar and is called (case insensitively) 'MANIFEST.MF'
        this.oldManifest = new SectionList();
        Enumeration entries = this.inJar.entries();
        while (entries.hasMoreElements())
        {
            // Get the first entry only from the input Jar
            ZipEntry inEntry = (ZipEntry)entries.nextElement();
            String name = inEntry.getName();
            if (GuardDB.STREAM_NAME_MANIFEST.equals(name.toUpperCase()))
            {
                this.oldManifest.parse(this.inJar.getInputStream(inEntry));
                break;
            }
        }

        // Create a fresh manifest, with a version header
        this.newManifest = new SectionList();
        Section version = this.oldManifest.find(GuardDB.MANIFEST_VERSION_TAG, GuardDB.MANIFEST_VERSION_VALUE);
        if (version == null)
        {
            version = new Section();
            version.add(GuardDB.MANIFEST_VERSION_TAG, GuardDB.MANIFEST_VERSION_VALUE);
        }
        this.newManifest.add(version);

        // copy through all the none-filename sections, apart from the version
        for (Iterator iter = this.oldManifest.iterator(); iter.hasNext();)
        {
            Section section = (Section)iter.next();
            if ((section != null) && (section != version))
            {
                Header name = section.findTag(GuardDB.MANIFEST_NAME_TAG);
                if (name == null)
                {
                    this.newManifest.add(section);
                }
                else
                {
                    String value = name.getValue();
                    if ((value.length() > 0) && (value.charAt(value.length() - 1) == '/'))
                    {
                        this.newManifest.add(section);
                    }
                }
            }
        }
    }

    /**
     * Update an entry in the manifest file
     * 
     * @param inName
     * @param outName
     * @param digests
     */
    private void updateManifest(String inName, String outName, MessageDigest[] digests)
    {
        // Check for section in old manifest
        Section oldSection = this.oldManifest.find(GuardDB.MANIFEST_NAME_TAG, inName);
        if (oldSection != null)
        {
            // Create fresh section for entry, and enter "Name" header
            Section newSection = new Section();
            newSection.add(GuardDB.MANIFEST_NAME_TAG, outName);

            // Copy over non-"Name", non-digest entries
            for (Iterator iter = oldSection.iterator(); iter.hasNext();)
            {
                Header header = (Header)iter.next();
                if (!header.getTag().equals(GuardDB.MANIFEST_NAME_TAG) && (header.getTag().indexOf("Digest") == -1))
                {
                    newSection.add(header);
                }
            }

            // Create fresh digest entries in the new section
            if ((digests != null) && (digests.length > 0))
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
                    newSection.add(GuardDB.MANIFEST_DIGESTALG_TAG, sb.toString());
                }

                // *-Digest headers
                for (int i = 0; i < digests.length; i++)
                {
                    if (digests[i] != null)
                    {
                        newSection.add(digests[i].getAlgorithm() + "-Digest", Tools.toBase64(digests[i].digest()));
                    }
                }
            }

            // Append the new section to the new manifest
            this.newManifest.add(newSection);
        }
    }
}
