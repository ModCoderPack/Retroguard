/* ===========================================================================
 * $RCSfile: GuiDB.java,v $
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

package COM.rl.obf.gui;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.zip.*;
import COM.rl.obf.*;
import COM.rl.util.*;
import COM.rl.obf.classfile.*;

/**
 * JAR analysis database for rgs script GUI.
 *
 * @author      Mark Welsh
 */
public class GuiDB implements GuiConstants
{
    // Constants -------------------------------------------------------------
    private static final String MANIFEST_ENTRY = "META-INF/MANIFEST.MF";
    private static final String TITLE_RGS_IN_PARSE_WARNINGS = "Input script warnings issued";
    private static final String TITLE_CLASSES_CORRUPT = "Corrupt classes in JAR";
    private static final String TITLE_DANGEROUS_METHODS = "Unsafe method calls in JAR";
    private static final String WARNING_RGS_IN_PARSE_FIRST = "Warnings during input script parsing:";
    private static final String RGS_IN_CANT_OPEN = "The input script file could not be opened";
    private static final String LOG_DANGER_HEADER1 = "# WARNING - Methods are called which may unavoidably break in obfuscated";
    private static final String LOG_DANGER_HEADER2 = "# version at runtime. Please review your source code to ensure that these";
    private static final String LOG_DANGER_HEADER3 = "# methods are not intended to act on classes within the obfuscated Jar file.";


    // Fields ----------------------------------------------------------------
    private File currentInJarFile; // Current JAR file
    private File currentRgsInFile; // Current input RGS file
    private ZipFile inJar;      // JAR file for obfuscation
    private boolean isInitialSet; // Are the initial settings already made?
    private boolean isAnalysed; // Is JAR analysed?
    private ClassTree classTree;
    private Vector appList; // ClPreserve/MdPreserve's for applications
    private Vector appletList; // ClPreserve's for applets
    private Vector beanList; // ClPreserve's for beans
    public boolean preserveSourceFile;           // Keep all 'SourceFile' attributes?
    public boolean preserveLocalVariableTable;   // Keep all 'LVT' attributes?
    public boolean preserveLineNumberTable;      // Keep all 'LNT' attributes?
    public Hashtable preserveClass;   // List of classes to be preserved
    public Hashtable preserveMethod;  // List of methods to be preserved
    public Hashtable preserveField;   // List of fields to be preserved


    // Class Methods ---------------------------------------------------------


    // Instance Methods ------------------------------------------------------
    /** Ctor. */
    public GuiDB() {}

    /** Return the ClassTree after analysis. */
    public Pk getClassTreeRoot() {return (classTree != null) ? classTree.getRoot() : null;}

    /** Get Enumeration of ClPreserve/MdPreserve's for applications in JAR. */
    public Enumeration getAppElements() {return appList == null ? new Vector().elements() : appList.elements();}

    /** Get Enumeration of ClPreserve's for applets in JAR. */
    public Enumeration getAppletElements() {return appletList == null ? new Vector().elements() : appletList.elements();}

    /** Get Enumeration of ClPreserve's for beans in JAR. */
    public Enumeration getBeanElements() {return beanList == null ? new Vector().elements() : beanList.elements();}

    /** Get particular ClPreserve/MdPreserve for applications in JAR. */
    public Cons getAppElement(int i) {return (Cons)appList.elementAt(i);}

    /** Get particular ClPreserve for applets in JAR. */
    public ClPreserve getAppletElement(int i) {return (ClPreserve)appletList.elementAt(i);}

    /** Get particular ClPreserve for beans in JAR. */
    public ClPreserve getBeanElement(int i) {return (ClPreserve)beanList.elementAt(i);}

    /** Set the RGS input file which is to provide initial settings. */
    public void setRgsIn(File rgsInFile) throws Exception
    {
        // Only set for new file
        if ((rgsInFile == null && currentRgsInFile != null) ||
            (rgsInFile != null && !rgsInFile.equals(currentRgsInFile)))
        {
            currentRgsInFile = rgsInFile;
            isInitialSet = false;
        }
    }

    /** Set the JAR ZipFile to be analysed. */
    public void setJar(File inJarFile) throws Exception
    {
        // Only set for new JARs
        if (inJarFile != null && !inJarFile.equals(currentInJarFile))
        {
            currentInJarFile = inJarFile;
            inJar = new ZipFile(currentInJarFile);
            isAnalysed = false;
            isInitialSet = false;
        }
    }

    /** Set if the JAR ZipFile has been analysed. */
    public void setAnalysed() {isAnalysed = true;}

    /** Is there a valid JAR file for analysis? */
    public boolean hasJar() {return inJar != null;}

    /** Is there a valid JAR file that is unanalysed? */
    public boolean hasNewJar() {return hasJar() && (!isAnalysed || !isInitialSet);}

    /** Analyse the JAR. */
    public void analyseJar(Frame parentFrame)
    {
        if (hasJar() && !isAnalysed)
        {
            // Go through the input Jar, adding each class file to the database
            classTree = new ClassTree();
            Vector warningsCorrupt = new Vector();
            Vector warningsDanger = new Vector();
            Enumeration entries = inJar.entries();
            while (entries.hasMoreElements())
            {
                // Get the next entry from the input Jar
                ZipEntry inEntry = (ZipEntry)entries.nextElement();
                String name = inEntry.getName();
                if (name.length() > CLASS_EXT.length() &&
                    name.substring(name.length() - CLASS_EXT.length(), name.length()).equals(CLASS_EXT))
                {
                    DataInputStream inStream = null;
                    ClassFile cf = null;
                    try
                    {
                        // Create a full internal representation of the class file
                        inStream = new DataInputStream(
                            new BufferedInputStream(
                                inJar.getInputStream(inEntry)));
                        cf = ClassFile.create(inStream);

                        // Add the classfile to our representation of the JAR
                        classTree.addClassFile(cf);
                    }
                    catch (Exception e)
                    {
                        warningsCorrupt.addElement("Corrupt classfile: " + name);
                    }
                    finally
                    {
                        try
                        {
                            if (inStream != null)
                            {
                                inStream.close();
                            }
                        }
                        catch (IOException ee)
                        {
                            // Take no action if close fails
                        }
                    }

                    // Check the classfile for references to 'dangerous' methods
                    String[] dangers = null;
                    try
                    {
                        dangers = cf.getDangerousMethods();
                    }
                    catch (Exception e)
                    {
                        dangers = null;
                    }
                    if (dangers != null && dangers.length > 0)
                    {
                        for (int i = 0; i < dangers.length; i++)
                        {
                            warningsDanger.addElement(dangers[i]);
                        }
                    }
                }
            }

            // Put up warning dialog if corrupt classes or dangerous methods encountered
            if (warningsCorrupt.size() > 0)
            {
                String[] warningArray = new String[warningsCorrupt.size()];
                for (int i = 0; i < warningsCorrupt.size(); i++)
                {
                    warningArray[i] = (String)warningsCorrupt.elementAt(i);
                }
                new WarningDialog(parentFrame, TITLE_CLASSES_CORRUPT, warningArray, true).setVisible(true);
            }
            if (warningsDanger.size() > 0)
            {
                String[] warningArray = new String[warningsDanger.size() + 3];
                warningArray[0] = LOG_DANGER_HEADER1;
                warningArray[1] = LOG_DANGER_HEADER2;
                warningArray[2] = LOG_DANGER_HEADER3;
                for (int i = 0; i < warningsDanger.size(); i++)
                {
                    warningArray[i + 3] = (String)warningsDanger.elementAt(i);
                }
                new WarningDialog(parentFrame, TITLE_DANGEROUS_METHODS, warningArray, true).setVisible(true);
            }

            // Signal the Jar has been analysed
            isAnalysed = true;
        }

        // Initialize the class/method/field preserve lists
        if (hasJar() && !isInitialSet)
        {
            resetDB();
            try
            {
                // Scan for applications
                classTree.walkTree(new TreeAction() {
                    public void methodAction(Md md)
                    {
                        if (md.getInName().equals(METHOD_NAME_MAIN) &&
                            md.getDescriptor().equals(METHOD_DESCRIPTOR_MAIN))
                        {
                            ClPreserve clp = new ClPreserve((Cl)md.getParent());
                            MdPreserve mdp = new MdPreserve();
                            if (currentRgsInFile == null)
                            {
                                clp.setPreserve(true);
                                mdp.setPreserve(true);
                            }
                            appList.addElement(new Cons(clp, mdp));
                            preserveClass.put(clp.getPkCl(), clp);
                            preserveMethod.put(md, mdp);
                        }
                    }
                });
                // Sort app list
                Object[] sortApps = new Object[appList.size()];
                for (int i = 0; i < sortApps.length; i++) 
                {
                    sortApps[i] = appList.elementAt(i);
                }
                Sort.quicksort(sortApps, 
                               new Compare() {
                                   public boolean isLess(Object o1, Object o2) 
                                   {
                                       return ((ClPreserve)((Cons)o1).car).getPkCl().getInName().compareTo(((ClPreserve)((Cons)o2).car).getPkCl().getInName()) < 0;
                                   }
                               });
                appList = new Vector();
                for (int i = 0; i < sortApps.length; i++) 
                {
                    appList.addElement(sortApps[i]);
                }

                // Scan for applets
                classTree.walkTree(new TreeAction() {
                    public void classAction(Cl cl)
                    {
                        if (cl.hasAsSuper(CLASS_NAME_APPLET))
                        {
                            ClPreserve clp = new ClPreserve(cl);
                            if (currentRgsInFile == null)
                            {
                                clp.setPreserve(true);
                            }
                            appletList.addElement(clp);
                            preserveClass.put(clp.getPkCl(), clp);
                        }
                    }
                });
                // Sort applet list
                Object[] sortApplets = new Object[appletList.size()];
                for (int i = 0; i < sortApplets.length; i++) 
                {
                    sortApplets[i] = appletList.elementAt(i);
                }
                Sort.quicksort(sortApplets, 
                               new Compare() {
                                   public boolean isLess(Object o1, Object o2) 
                                   {
                                       return ((ClPreserve)o1).getPkCl().getInName().compareTo(((ClPreserve)o1).getPkCl().getInName()) < 0;
                                   }
                               });
                appletList = new Vector();
                for (int i = 0; i < sortApplets.length; i++) 
                {
                    appletList.addElement(sortApplets[i]);
                }

                // Scan for Beans via the META-INF/manifest.mf file
                ZipEntry inEntry = inJar.getEntry(MANIFEST_ENTRY);
                if (inEntry != null)
                {
                    BufferedReader reader = null;
                    try
                    {
                        reader = new BufferedReader(
                            new InputStreamReader(
                                inJar.getInputStream(inEntry)));
                        String line = null;
                        String name = null;
                        boolean isBean = false;
                        while ((line = reader.readLine()) != null)
                        {
                            int pos = line.indexOf(':');
                            if (pos > 0)
                            {
                                String tag = line.substring(0, pos).trim();
                                String value = line.substring(pos + 1).trim();
                                if (tag.equals("Name"))
                                {
                                    int dotPos = value.indexOf(".");
                                    if (dotPos > 0)
                                    {
                                        name = value.substring(0, dotPos);
                                    }
                                    else
                                    {
                                        name = null;
                                    }
                                }
                                else if (tag.equals("Java-Bean") && value.toLowerCase().equals("true"))
                                {
                                    isBean = true;
                                }
                            }
                            else
                            {
                                if (name != null && isBean)
                                {
                                    Cl cl = null;
                                    try
                                    {
                                        cl = classTree.getCl(name);
                                    }
                                    catch (Exception e)
                                    {
                                        // Internal error, so discard any result
                                        cl = null;
                                    }
                                    if (cl != null)
                                    {
                                        ClPreserve clp = new ClPreserve(cl);
                                        if (currentRgsInFile == null)
                                        {
                                            clp.setPreserve(true);
                                            clp.setKeepPublic(true);
                                            clp.setKeepProtected(true);
                                        }
                                        beanList.addElement(clp);
                                        preserveClass.put(clp.getPkCl(), clp);
                                    }
                                }
                                name = null;
                                isBean = false;
                            }
                        }
                    }
                    finally
                    {
                        try
                        {
                            if (reader != null)
                            {
                                reader.close();
                            }
                        }
                        catch (IOException ee)
                        {
                            // Take no action if close fails
                        }
                    }
                }
                // Sort bean list
                Object[] sortBeans = new Object[beanList.size()];
                for (int i = 0; i < sortBeans.length; i++) 
                {
                    sortBeans[i] = beanList.elementAt(i);
                }
                Sort.quicksort(sortBeans, 
                               new Compare() {
                                   public boolean isLess(Object o1, Object o2) 
                                   {
                                       return ((ClPreserve)o1).getPkCl().getInName().compareTo(((ClPreserve)o2).getPkCl().getInName()) < 0;
                                   }
                               });
                beanList = new Vector();
                for (int i = 0; i < sortBeans.length; i++) 
                {
                    beanList.addElement(sortBeans[i]);
                }
            }
            catch (Exception eee)
            {
                // On app/applet/bean scan failure, reset the database
                resetDB();
            }

            // If an input RGS file is to be used, scan it for initial settings
            if (currentRgsInFile != null)
            {
                InputStream rgsInputStream = null;
                Vector warnings = new Vector();
                try
                {
                    rgsInputStream = new FileInputStream(currentRgsInFile);
                    for (RgsEnum rgsEnum = new RgsEnum(rgsInputStream); rgsEnum.hasMoreEntries(); )
                    {
                        try
                        {
                            RgsEntry entry = (RgsEntry)rgsEnum.nextEntry();
                            switch (entry.type)
                            {
                            case RgsEntry.TYPE_ATTR:
                                // Preserve attributes
                                if (entry.name != null)
                                {
                                    if (entry.name.equals(ClassConstants.ATTR_SourceFile))
                                    {
                                        preserveSourceFile = true;
                                    }
                                    else if (entry.name.equals(ClassConstants.ATTR_LocalVariableTable))
                                    {
                                        preserveLocalVariableTable = true;
                                    }
                                    else if (entry.name.equals(ClassConstants.ATTR_LineNumberTable))
                                    {
                                        preserveLineNumberTable = true;
                                    }
                                    else if (!Tools.isInArray(entry.name, ClassConstants.KNOWN_ATTRS))
                                    {
                                        throw new Exception("Unknown attribute: " + entry.name);
                                    }
                                }
                                break;

                            case RgsEntry.TYPE_CLASS:
                                // Preserve classes
                                try
                                {
                                    ClPreserve clp = null;
                                    if (entry.name != null && entry.name.indexOf('*') != -1)
                                    {
                                        // Wildcard identifiers are treated with package in GUI
                                        Pk pk = classTree.getPk(entry.name.substring(0, entry.name.length() - 2));
                                        clp = (ClPreserve)preserveClass.get(pk);
                                        if (clp == null)
                                        {
                                            clp = new ClPreserve(pk);
                                            preserveClass.put(pk, clp);
                                        }
                                    }
                                    else
                                    {
                                        Cl cl = classTree.getCl(entry.name);
                                        clp = (ClPreserve)preserveClass.get(cl);
                                        if (clp == null)
                                        {
                                            clp = new ClPreserve(cl);
                                            preserveClass.put(clp.getPkCl(), clp);
                                        }
                                    }
                                    if (clp != null)
                                    {
                                        clp.setPreserve(true);
                                        clp.setKeepPublic(entry.retainToPublic || entry.retainToProtected || entry.retainPubProtOnly);
                                        clp.setKeepProtected(entry.retainToProtected || entry.retainPubProtOnly);
                                        if (entry.retainFieldsOnly)
                                        {
                                            clp.keepFieldsOnly();
                                        }
                                        else if (entry.retainMethodsOnly)
                                        {
                                            clp.keepMethodsOnly();
                                        }
                                        else
                                        {
                                            clp.keepMethodsAndFields();
                                        }
                                    }
                                    else
                                    {
                                        throw new Exception();
                                    }
                                }
                                catch (Exception e)
                                {
                                    throw new Exception("Unknown class: " + entry.toString());
                                }
                                break;

                            case RgsEntry.TYPE_METHOD:
                                // Preserve methods
                                try
                                {
                                    Md md = classTree.getMd(entry.name, entry.descriptor);
                                    if (md != null)
                                    {
                                        MdPreserve mdp = (MdPreserve)preserveMethod.get(md);
                                        if (mdp == null)
                                        {
                                            mdp = new MdPreserve();
                                            preserveMethod.put(md, mdp);
                                        }
                                        mdp.setPreserve(true);
                                    }
                                    else
                                    {
                                        throw new Exception();
                                    }
                                }
                                catch (Exception e)
                                {
                                    throw new Exception("Unknown method: " + entry.toString());
                                }
                                break;

                            case RgsEntry.TYPE_FIELD:
                                // Preserve fields
                                try
                                {
                                    Fd fd = classTree.getFd(entry.name);
                                    if (fd != null)
                                    {
                                        FdPreserve fdp = (FdPreserve)preserveField.get(fd);
                                        if (fdp == null)
                                        {
                                            fdp = new FdPreserve();
                                            preserveField.put(fd, fdp);
                                        }
                                        fdp.setPreserve(true);
                                    }
                                    else
                                    {
                                        throw new Exception();
                                    }
                                }
                                catch (Exception e)
                                {
                                    throw new Exception("Unknown field: " + entry.toString());
                                }
                                break;

                            default:
                                // Ignore illegal types
                                break;
                            }
                        }
                        catch (Exception e)
                        {
                            warnings.addElement(e.getMessage());
                        }
                    }
                }
                catch (Exception e)
                {
                    // Failed to open the InputStream
                    warnings.addElement(RGS_IN_CANT_OPEN);
                }
                finally
                {
                    try
                    {
                        if (rgsInputStream != null)
                        {
                            rgsInputStream.close();
                        }
                    }
                    catch (IOException ee)
                    {
                        // Take no action if close fails
                    }
                }

                // Issue warnings if necessary
                if (warnings.size() > 0)
                {
                    String[] warningArray = new String[warnings.size() + 1];
                    warningArray[0] = WARNING_RGS_IN_PARSE_FIRST;
                    for (int i = 0; i < warnings.size(); i++)
                    {
                        warningArray[i + 1] = "    " + (String)warnings.elementAt(i);
                    }
                    new WarningDialog(parentFrame, TITLE_RGS_IN_PARSE_WARNINGS, warningArray, true).setVisible(true);
                }
            }
            else
            {
                // Reset attributes to 'don't preserve' default
                preserveSourceFile = false;
                preserveLocalVariableTable = false;
                preserveLineNumberTable = false;
            }
            isInitialSet = true;
        }
    }
    private void resetDB()
    {
        preserveClass = new Hashtable();
        preserveMethod = new Hashtable();
        preserveField = new Hashtable();
        appList = new Vector();
        appletList = new Vector();
        beanList = new Vector();
    }

    /** Does the analysed JAR have apps? */
    public boolean hasApps()
    {
        return (appList != null) && (appList.size() != 0);
    }

    /** Does the analysed JAR have applets? */
    public boolean hasApplets()
    {
        return (appletList != null) && (appletList.size() != 0);
    }

    /** Does the analysed JAR have beans? */
    public boolean hasBeans()
    {
        return (beanList != null) && (beanList.size() != 0);
    }
}
