/* ===========================================================================
 * $RCSfile: Gui.java,v $
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
import java.text.*;
import java.util.zip.*;
import java.awt.*;
import java.lang.reflect.*;
import java.awt.event.*;
import COM.rl.util.*;
import COM.rl.obf.*;

/**
 * A graphical user interface for interactive generation of an rgs script.
 *
 * @author      Mark Welsh
 */
public class Gui extends Frame implements Runnable, GuiConstants
{
    // Constants -------------------------------------------------------------
    private static final String FRAME_TITLE = "RGS Script Generator";
    private static final String LABEL_SELECT_JAR = "1. Select the JAR file which will be obfuscated with this script:";
    private static final String LABEL_SELECT_RGS_IN = "2. Optionally, select a RetroGuard script to supply initial settings?";
    private static final String LABEL_SELECT_APPS = "3. Select application classes and main methods to preserve:";
    private static final String LABEL_SELECT_APPLETS = "4. Select applet classes to preserve:";
    private static final String LABEL_SELECT_BEANS = "5. Select beans to preserve:";
    private static final String LABEL_SELECT_CLASS = "6. Select additional class/interface names to preserve:";
    private static final String LABEL_SELECT_CLASS_MF = "7. Select specific additional method and field names to preserve (choose a class/interface first):";
    private static final String LABEL_SELECT_METHOD = "Select a method:";
    private static final String LABEL_SELECT_FIELD = "Select a field:";
    private static final String LABEL_SELECT_ATTR = "8. Select class attributes to preserve, for all classes:";
    private static final String LABEL_SELECT_RGS_OUT = "9. Select a name for the RetroGuard script file to generate, then hit 'Finish':";
    private static final String CHECK_LABEL_PRESERVE = "Preserve?";
    private static final String CHECK_LABEL_SHOW_ALL = "Show all?";
    private static final String CHECK_LABEL_PRESERVE_CLASS = "Preserve the class or interface name?";
    private static final String CHECK_LABEL_PRESERVE_PUBLIC_MF = "Preserve public names in the class or interface?";
    private static final String CHECK_LABEL_PRESERVE_PROTECTED_MF = "In addition, preserve protected names in the class or interface?";;
    private static final String CHECK_LABEL_PRESERVE_METHODS_FIELDS = "Preserve both method and field names?";
    private static final String CHECK_LABEL_PRESERVE_METHODS_ONLY = "Preserve method names only?";
    private static final String CHECK_LABEL_PRESERVE_FIELDS_ONLY = "Preserve field names only?";
    private static final String CHECK_LABEL_PRESERVE_SOURCEFILE = "Preserve 'SourceFile' attributes?";
    private static final String CHECK_LABEL_PRESERVE_LINENUMBERS = "Preserve 'LineNumberTable' attributes?";
    private static final String CHECK_LABEL_PRESERVE_LOCALVARIABLES = "Preserve 'LocalVariableTable' attributes?";
    private static final String FIELD_DEFAULT_IN_JAR = "in.jar";
    private static final String FIELD_DEFAULT_RGS_IN = "script.rgs";
    private static final String FIELD_DEFAULT_RGS_OUT = "script.rgs";
    private static final String BUTTON_BROWSE = "Browse...";
    private static final String FD_SELECT = "Select a file";
    private static final String TITLE_NO_SUCH_JAR = "No JAR file selected";
    private static final String[] WARNING_NO_SUCH_JAR = {"The file selected did not",
                                                         "exist or was not a JAR file.",
                                                         "Please select a JAR file ",
                                                         "for analysis."};
    private static final String TITLE_CANT_OPEN_RGS = "Can't access the script file";
    private static final String[] WARNING_CANT_OPEN_RGS_IN = {"The script file selected could",
                                                              "not be opened. Default settings",
                                                              "will be used."};
    private static final String[] WARNING_CANT_OPEN_RGS_OUT = {"The file selected could not",
                                                               "be written.",
                                                               "Please select another file",
                                                               "to write the script."};
    private static final String STATUS_SCAN_JAR = "Analysing the selected JAR file...";
    private static final String STATUS_COMPILING_RGS = "Compiling the RetroGuard script file...";
    private static final String STATUS_WRITING_RGS = "Writing the file...";
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;
    private static final String METHOD_NAME_SER_READ = "readObject";
    private static final String METHOD_NAME_SER_WRITE = "writeObject";
    private static final String METHOD_DESCRIPTOR_SER_READ = "(Ljava/io/ObjectInputStream;)V";
    private static final String METHOD_DESCRIPTOR_SER_WRITE = "(Ljava/io/ObjectOutputStream;)V";


    // Fields ----------------------------------------------------------------
    private WizardPanel wizardPanel;// Wizard abstraction
    private GuiDB guiDB;            // database
    private String lastDir;
    // Active UI elements
    private TextField inJarField;
    private Checkbox checkboxRgsIn;
    private Button buttonRgsIn;
    private TextField rgsInField;
    private Checkbox checkboxApp;
    private java.awt.List listApp;
    private Checkbox checkboxApplet;
    private java.awt.List listApplet;
    private Checkbox checkboxBean;
    private java.awt.List listBean;
    private ClPreserve currentClPreserve;
    private MdPreserve currentMdPreserve;
    private FdPreserve currentFdPreserve;
    private java.awt.List listClass;
    private TreeControl tree;
    private Checkbox checkboxClass;
    private Checkbox checkboxPublicMF;
    private Checkbox checkboxProtectedMF;
    private Checkbox checkboxMFBoth;
    private Checkbox checkboxMOnly;
    private Checkbox checkboxFOnly;
    private java.awt.List listClassMF;
    private Vector vecMethods = new Vector();
    private java.awt.List listMethods;
    private Vector vecFields = new Vector();
    private java.awt.List listFields;
    private Checkbox checkboxMethod;
    private Checkbox checkboxAllMethods;
    private Checkbox checkboxField;
    private Checkbox checkboxAllFields;
    private Checkbox checkboxAttrSF;
    private Checkbox checkboxAttrLNT;
    private Checkbox checkboxAttrLVT;
    private TextField rgsOutField;


    // Class Methods ---------------------------------------------------------
    /**
     * Create a graphical user interface for interactive generation of an rgs script.
     */
    public static void create()
    {
        // Create the application's UI.
        new Gui().createUI();
    }


    // Instance Methods ------------------------------------------------------
    // Private constructor.
    private Gui()
    {
        // Set the frame title
        super(FRAME_TITLE);
    }

    /** Can we flip past first page? */
    public boolean canFlipFirst() {return (guiDB != null) && guiDB.hasJar();}

    // Create the application's UI.
    private void createUI()
    {
        // Set fixed frame size
        setSize(FRAME_WIDTH, FRAME_HEIGHT);

        // Close JVM on window close
        addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e) {System.exit(0);}});

        // Set up a Wizard with our Panels
        wizardPanel = new WizardPanel(generatePanels(),
                                      new ActionListener() {public void actionPerformed(ActionEvent e) {onFirstNext();}},
                                      new ActionListener() {public void actionPerformed(ActionEvent e) {onFinish();}});
        add(wizardPanel);

        // Set enabled status of all components, basis on state of wizard
        updateEnabled();

        // Show the frame
        setVisible(true);
    }

    /** User hit "next" on first page. */
    public void onFirstNext()
    {
        try
        {
            // Signal that current JAR is not analysed, and create main database if not already done
            if (guiDB == null)
            {
                guiDB = new GuiDB();
            }

            // Check that the selected file exists, can be read, and is a JAR
            File file = new File(inJarField.getText());
            if (file.exists() && file.canRead())
            {
                // Try to open the jar
                try
                {
                    guiDB.setJar(file);
                }
                catch (Exception e)
                {
                    guiDB.setJar(null);
                }
            }

            // Determine if an input RGS is to be used
            if (checkboxRgsIn.getState())
            {
                File fileRgsIn = new File(rgsInField.getText());
                if (fileRgsIn.exists() && fileRgsIn.canRead())
                {
                    guiDB.setRgsIn(fileRgsIn);
                }
                else
                {
                    Dialog dialog = new WarningDialog(this, TITLE_CANT_OPEN_RGS, WARNING_CANT_OPEN_RGS_IN);
                    dialog.setVisible(true);
                    guiDB.setRgsIn(null);
                }
            }
            else
            {
                guiDB.setRgsIn(null);
            }

            // Throw up a warning dialog if no JAR
            if (guiDB.hasJar())
            {
                // Flip the page
                wizardPanel.flipFirst();
                if (guiDB.hasNewJar())
                {
                    // Update enabling of many UI elements on wizard.
                    updateEnabled();

                    // We have a JAR and need to scan it
                    wizardPanel.setStatus(STATUS_SCAN_JAR);

                    // Analyse the JAR in a separate thread
                    new Thread(this).start();
                }
            }
            else
            {
                Dialog dialog = new WarningDialog(this, TITLE_NO_SUCH_JAR, WARNING_NO_SUCH_JAR);
                dialog.setVisible(true);
            }
        }
        catch (Exception e)
        {
            System.out.println("Error initializing GUI.");
        }
    }

    /** Implementation of Runnable - used for analysing JAR file. */
    public void run()
    {
        guiDB.analyseJar(this);

        // Clear the status bar
        wizardPanel.setStatus("");

        // Update the content of all list UI elements on wizard.
        updateAllLists();

        // Controls enabling of many UI elements on wizard.
        updateEnabled();
    }

    /** User hit "Finish". */
    public void onFinish()
    {
        try
        {
            // Some status bar pauses to let the user know we haven't just crashed out
            wizardPanel.setStatus(STATUS_COMPILING_RGS);
            Thread.sleep(1000);
            wizardPanel.setStatus(STATUS_WRITING_RGS);
            Thread.sleep(700);
            wizardPanel.setStatus("");

            // Open the script file
            String filename = rgsOutField.getText();
            if (filename.indexOf(File.separatorChar) == -1 && lastDir != null)
            {
                filename = lastDir + filename;
            }
            File file = new File(filename);
            PrintWriter pw = null;
            try
            {
                if (!file.exists() || file.canWrite())
                {
                    pw = new PrintWriter(
                        new BufferedOutputStream(
                            new FileOutputStream(file)));

                    // Write the script file header
                    pw.println("#");
                    pw.println("# Automatically generated script for RetroGuard bytecode obfuscator.");
                    pw.println("# To be used with Java JAR-file: " + inJarField.getText());
                    pw.println("# " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date()));
                    pw.println("#");

                    // Output the preservation orders from the main listboxes
                    // Classes
                    for (Enumeration clEnum = guiDB.preserveClass.keys(); clEnum.hasMoreElements(); )
                    {
                        PkCl pkcl = (PkCl)clEnum.nextElement();
                        String name = pkcl.getFullInName();
                        ClPreserve clPreserve = (ClPreserve)guiDB.preserveClass.get(pkcl);
                        if (pkcl instanceof Pk)
                        {
                            // Wildcard entry
                            name += (name.length() > 0 ? "/" : "") + "*";
                        }
                        if (clPreserve.isPreserve())
                        {
                            pw.print(SCRIPT_CLASS + name);
                            if (clPreserve.isKeepPublic())
                            {
                                if (!clPreserve.isKeepProtected())
                                {
                                    pw.print(SCRIPT_PUBLIC);
                                }
                                else
                                {
                                    pw.print(SCRIPT_PROTECTED);
                                }
                                if (clPreserve.isKeepMethodsOnly())
                                {
                                    pw.print(SCRIPT_METHODS_ONLY);
                                }
                                else if (clPreserve.isKeepFieldsOnly())
                                {
                                    pw.print(SCRIPT_FIELDS_ONLY);
                                }
                            }
                            pw.println();
                        }
                    }

                    // Methods
                    for (Enumeration mdEnum = guiDB.preserveMethod.keys(); mdEnum.hasMoreElements(); )
                    {
                        Md md = (Md)mdEnum.nextElement();
                        boolean isKeeper = ((MdPreserve)guiDB.preserveMethod.get(md)).isPreserve();
                        if (isKeeper)
                        {
                            pw.println(SCRIPT_METHOD + md.getFullInName() + " " + md.getDescriptor());
                        }
                    }

                    // Fields
                    for (Enumeration fdEnum = guiDB.preserveField.keys(); fdEnum.hasMoreElements(); )
                    {
                        Fd fd = (Fd)fdEnum.nextElement();
                        boolean isKeeper = ((FdPreserve)guiDB.preserveField.get(fd)).isPreserve();
                        if (isKeeper)
                        {
                            pw.println(SCRIPT_FIELD + fd.getFullInName() + " " + fd.getDescriptor());
                        }
                    }

                    // Output the attribute preservation orders
                    if (guiDB.preserveSourceFile)
                    {
                        pw.println(SCRIPT_ATTR + SCRIPT_SF);
                    }
                    if (guiDB.preserveLineNumberTable)
                    {
                        pw.println(SCRIPT_ATTR + SCRIPT_LNT);
                    }
                    if (guiDB.preserveLocalVariableTable)
                    {
                        pw.println(SCRIPT_ATTR + SCRIPT_LVT);
                    }
                }
                else
                {
                    Dialog dialog = new WarningDialog(this, TITLE_CANT_OPEN_RGS, WARNING_CANT_OPEN_RGS_OUT);
                    dialog.setVisible(true);
                    return;
                }
            }
            finally
            {
                // Close the script file in all circumstances
                if (pw != null)
                {
                    pw.close();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error writing the script file.");
        }
        finally
        {
            wizardPanel.setStatus("");
        }
        System.exit(0);
    }

    // Generate the wizard panels
    private Panel[] generatePanels()
    {
        // 1. Splashscreen and JAR selection
        Panel p1 = new RaisedPanel() {public Insets getInsets() {return new Insets(2, 2, 2, 2);}};
        p1.setLayout(new BorderLayout());
        p1.setBackground(Color.lightGray);
        // Splash logo
        Panel p1upper = new LogoRaisedPanel();
        p1.add("Center", p1upper);
        // JAR selection text-field and browse button
        Panel p1lower = new Panel();
        p1lower.setLayout(new GridLayout(2, 1));
        Panel p1a = new Panel();
        GridBagLayout gridbag1a = new GridBagLayout();
        GridBagConstraints c1a = new GridBagConstraints();
        p1a.setLayout(gridbag1a);
        c1a.anchor = GridBagConstraints.WEST;
        c1a.fill = GridBagConstraints.BOTH;
        c1a.weightx = 1.0;
        c1a.insets = new Insets(4, 4, 4, 4);
        c1a.gridwidth = GridBagConstraints.REMAINDER; //end of row
        Label label1a = new Label(LABEL_SELECT_JAR);
        gridbag1a.setConstraints(label1a, c1a);
        p1a.add(label1a);
        c1a.weightx = 1.0;
        c1a.gridwidth = 1;
        inJarField = new TextField(FIELD_DEFAULT_IN_JAR);
        gridbag1a.setConstraints(inJarField, c1a);
        p1a.add(inJarField);
        c1a.weightx = 0.0;
        c1a.gridwidth = GridBagConstraints.REMAINDER; //end of row
        Button button1a = new Button(BUTTON_BROWSE);
        button1a.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {browse(inJarField);}});
        gridbag1a.setConstraints(button1a, c1a);
        p1a.add(button1a);
        p1lower.add(p1a);
        // script file selection text-field and browse button
        Panel p1b = new Panel();
        GridBagLayout gridbag1b = new GridBagLayout();
        GridBagConstraints c1b = new GridBagConstraints();
        p1b.setLayout(gridbag1b);
        c1b.fill = GridBagConstraints.BOTH;
        c1b.anchor = GridBagConstraints.WEST;
        c1b.weightx = 1.0;
        c1b.insets = new Insets(4, 4, 4, 4);
        c1b.gridwidth = GridBagConstraints.REMAINDER; //end of row
        Panel p1bUpper = new Panel();
        p1bUpper.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        Label label1b = new Label(LABEL_SELECT_RGS_IN);
        p1bUpper.add(label1b);
        checkboxRgsIn = new Checkbox();
        checkboxRgsIn.addItemListener(new ItemListener() {public void itemStateChanged(ItemEvent e) {
            rgsInField.setEnabled(checkboxRgsIn.getState());
            buttonRgsIn.setEnabled(checkboxRgsIn.getState());}});
        p1bUpper.add(checkboxRgsIn);
        gridbag1b.setConstraints(p1bUpper, c1b);
        p1b.add(p1bUpper);
        c1b.weightx = 1.0;
        c1b.gridwidth = 1;
        rgsInField = new TextField(FIELD_DEFAULT_RGS_IN);
        rgsInField.setEnabled(checkboxRgsIn.getState());
        gridbag1b.setConstraints(rgsInField, c1b);
        p1b.add(rgsInField);
        c1b.weightx = 0.0;
        c1b.gridwidth = GridBagConstraints.REMAINDER; //end of row
        buttonRgsIn = new Button(BUTTON_BROWSE);
        buttonRgsIn.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {browse(rgsInField);}});
        buttonRgsIn.setEnabled(checkboxRgsIn.getState());
        gridbag1b.setConstraints(buttonRgsIn, c1b);
        p1b.add(buttonRgsIn);
        p1lower.add(p1b);
        p1.add("South", p1lower);

        // 2. Select applications, applets, beans to preserve
        Panel p2 = new RaisedPanel() {public Insets getInsets() {return new Insets(2, 2, 2, 2);}};
        p2.setBackground(Color.lightGray);
        p2.setLayout(new GridLayout(0, 1, 2, 2));
        // Applications
        Panel p2a = new RaisedPanel();
        p2a.setBackground(Color.lightGray);
        GridBagLayout gridbag2a = new GridBagLayout();
        GridBagConstraints c2a = new GridBagConstraints();
        p2a.setLayout(gridbag2a);
        c2a.fill = GridBagConstraints.BOTH;
        c2a.anchor = GridBagConstraints.WEST;
        c2a.insets = new Insets(4, 4, 4, 4);
        // label
        c2a.weightx = 1.0;
        c2a.weighty = 0.0;
        c2a.gridwidth = 1;
        Label labelAppSelect = new Label(LABEL_SELECT_APPS);
        gridbag2a.setConstraints(labelAppSelect, c2a);
        p2a.add(labelAppSelect);
        // checkbox
        c2a.weightx = 0.0;
        c2a.gridwidth = GridBagConstraints.REMAINDER; //end of row
        checkboxApp = new Checkbox(CHECK_LABEL_PRESERVE);
        gridbag2a.setConstraints(checkboxApp, c2a);
        p2a.add(checkboxApp);
        // listbox
        c2a.weightx = 1.0;
        c2a.weighty = 1.0;
        listApp = new java.awt.List();
        gridbag2a.setConstraints(listApp, c2a);
        p2a.add(listApp);

        // Applets
        Panel p2b = new RaisedPanel();
        p2b.setBackground(Color.lightGray);
        GridBagLayout gridbag2b = new GridBagLayout();
        GridBagConstraints c2b = new GridBagConstraints();
        p2b.setLayout(gridbag2b);
        c2b.fill = GridBagConstraints.BOTH;
        c2b.anchor = GridBagConstraints.WEST;
        c2b.insets = new Insets(4, 4, 4, 4);
        // label
        c2b.weightx = 1.0;
        c2b.weighty = 0.0;
        c2b.gridwidth = 1;
        Label labelAppletSelect = new Label(LABEL_SELECT_APPLETS);
        gridbag2b.setConstraints(labelAppletSelect, c2b);
        p2b.add(labelAppletSelect);
        // checkbox
        c2b.weightx = 0.0;
        c2b.gridwidth = GridBagConstraints.REMAINDER; //end of row
        checkboxApplet = new Checkbox(CHECK_LABEL_PRESERVE);
        gridbag2b.setConstraints(checkboxApplet, c2b);
        p2b.add(checkboxApplet);
        // listbox
        c2b.weightx = 1.0;
        c2b.weighty = 1.0;
        listApplet = new java.awt.List();
        gridbag2b.setConstraints(listApplet, c2b);
        p2b.add(listApplet);

        // Beans
        Panel p2c = new RaisedPanel();
        p2c.setBackground(Color.lightGray);
        GridBagLayout gridbag2c = new GridBagLayout();
        GridBagConstraints c2c = new GridBagConstraints();
        p2c.setLayout(gridbag2c);
        c2c.fill = GridBagConstraints.BOTH;
        c2c.anchor = GridBagConstraints.WEST;
        c2c.insets = new Insets(4, 4, 4, 4);
        // label
        c2c.weightx = 1.0;
        c2c.weighty = 0.0;
        c2c.gridwidth = 1;
        Label labelBeanSelect = new Label(LABEL_SELECT_BEANS);
        gridbag2c.setConstraints(labelBeanSelect, c2c);
        p2c.add(labelBeanSelect);
        // checkbox
        c2c.weightx = 0.0;
        c2c.gridwidth = GridBagConstraints.REMAINDER; //end of row
        checkboxBean = new Checkbox(CHECK_LABEL_PRESERVE);
        gridbag2c.setConstraints(checkboxBean, c2c);
        p2c.add(checkboxBean);
        // listbox
        c2c.weightx = 1.0;
        c2c.weighty = 1.0;
        listBean = new java.awt.List();
        gridbag2c.setConstraints(listBean, c2c);
        p2c.add(listBean);

        // add the three panels to the card
        p2.add(p2a);
        p2.add(p2b);
        p2.add(p2c);

        // 3. Select specific classes to preserve
        Panel p3 = new RaisedPanel() {public Insets getInsets() {return new Insets(2, 2, 2, 2);}};
        p3.setBackground(Color.lightGray);
        GridBagLayout gridbag3 = new GridBagLayout();
        GridBagConstraints c3 = new GridBagConstraints();
        p3.setLayout(gridbag3);
        c3.fill = GridBagConstraints.BOTH;
        c3.anchor = GridBagConstraints.WEST;
        c3.insets = new Insets(2, 4, 2, 4);
        // label
        c3.weightx = 1.0;
        c3.weighty = 0.0;
        c3.gridwidth = GridBagConstraints.REMAINDER; // one per row
        Label labelClassSelect = new Label(LABEL_SELECT_CLASS);
        gridbag3.setConstraints(labelClassSelect, c3);
        p3.add(labelClassSelect);
        // listbox
        c3.weighty = 1.0;
        listClass = new java.awt.List();
        gridbag3.setConstraints(listClass, c3);
        p3.add(listClass);
        // checkboxes
        c3.weightx = 0.0;
        c3.weighty = 0.0;
        c3.fill = GridBagConstraints.NONE;
        c3.insets = new Insets(0, 12, 1, 12);
        checkboxClass = new Checkbox(CHECK_LABEL_PRESERVE_CLASS);
        gridbag3.setConstraints(checkboxClass, c3);
        p3.add(checkboxClass);
        checkboxPublicMF = new Checkbox(CHECK_LABEL_PRESERVE_PUBLIC_MF);
        gridbag3.setConstraints(checkboxPublicMF, c3);
        p3.add(checkboxPublicMF);
        checkboxProtectedMF = new Checkbox(CHECK_LABEL_PRESERVE_PROTECTED_MF);
        gridbag3.setConstraints(checkboxProtectedMF, c3);
        p3.add(checkboxProtectedMF);
        CheckboxGroup cbg = new CheckboxGroup();
        checkboxMFBoth = new Checkbox(CHECK_LABEL_PRESERVE_METHODS_FIELDS, cbg, false);
        gridbag3.setConstraints(checkboxMFBoth, c3);
        p3.add(checkboxMFBoth);
        checkboxMOnly = new Checkbox(CHECK_LABEL_PRESERVE_METHODS_ONLY, cbg, false);
        gridbag3.setConstraints(checkboxMOnly, c3);
        p3.add(checkboxMOnly);
        checkboxFOnly = new Checkbox(CHECK_LABEL_PRESERVE_FIELDS_ONLY, cbg, false);
        gridbag3.setConstraints(checkboxFOnly, c3);
        p3.add(checkboxFOnly);

        // 4. Select specific methods and fields to preserve
        Panel p4 = new RaisedPanel() {public Insets getInsets() {return new Insets(2, 2, 2, 2);}};
        p4.setBackground(Color.lightGray);
        GridBagLayout gridbag4 = new GridBagLayout();
        GridBagConstraints c4 = new GridBagConstraints();
        p4.setLayout(gridbag4);
        c4.fill = GridBagConstraints.BOTH;
        c4.anchor = GridBagConstraints.WEST;
        c4.insets = new Insets(2, 4, 2, 4);
        // label
        c4.weightx = 1.0;
        c4.weighty = 0.0;
        c4.gridwidth = GridBagConstraints.REMAINDER; // one per row
        Label labelClassMFSelect = new Label(LABEL_SELECT_CLASS_MF);
        gridbag4.setConstraints(labelClassMFSelect, c4);
        p4.add(labelClassMFSelect);
        // class listbox
        c4.weighty = 1.0;
        listClassMF = new java.awt.List();
        gridbag4.setConstraints(listClassMF, c4);
        p4.add(listClassMF);
        // method listbox
        c4.weighty = 0.0;
        c4.gridwidth = 1;
        Label labelMethodSelect = new Label(LABEL_SELECT_METHOD);
        gridbag4.setConstraints(labelMethodSelect, c4);
        p4.add(labelMethodSelect);
        c4.weightx = 0.0;
        checkboxAllMethods = new Checkbox(CHECK_LABEL_SHOW_ALL);
        checkboxAllMethods.setState(true);
        gridbag4.setConstraints(checkboxAllMethods, c4);
        p4.add(checkboxAllMethods);
        c4.gridwidth = GridBagConstraints.REMAINDER; // end of row
        checkboxMethod = new Checkbox(CHECK_LABEL_PRESERVE);
        gridbag4.setConstraints(checkboxMethod, c4);
        p4.add(checkboxMethod);
        c4.weightx = 1.0;
        c4.weighty = 1.0;
        listMethods = new java.awt.List();
        gridbag4.setConstraints(listMethods, c4);
        p4.add(listMethods);
        // field listbox
        c4.weighty = 0.0;
        c4.gridwidth = 1;
        Label labelFieldSelect = new Label(LABEL_SELECT_FIELD);
        gridbag4.setConstraints(labelFieldSelect, c4);
        p4.add(labelFieldSelect);
        c4.weightx = 0.0;
        checkboxAllFields = new Checkbox(CHECK_LABEL_SHOW_ALL);
        checkboxAllFields.setState(true);
        gridbag4.setConstraints(checkboxAllFields, c4);
        p4.add(checkboxAllFields);
        c4.gridwidth = GridBagConstraints.REMAINDER; // end of row
        checkboxField = new Checkbox(CHECK_LABEL_PRESERVE);
        gridbag4.setConstraints(checkboxField, c4);
        p4.add(checkboxField);
        c4.weightx = 1.0;
        c4.weighty = 1.0;
        listFields = new java.awt.List();
        gridbag4.setConstraints(listFields, c4);
        p4.add(listFields);


        // 5. Select attributes to preserve, choose an rgs script file for output, and hit finish
        Panel p5 = new RaisedPanel() {public Insets getInsets() {return new Insets(2, 2, 2, 2);}};
        p5.setBackground(Color.lightGray);
        GridBagLayout gridbag5 = new GridBagLayout();
        GridBagConstraints c5 = new GridBagConstraints();
        p5.setLayout(gridbag5);
        c5.fill = GridBagConstraints.BOTH;
        c5.anchor = GridBagConstraints.WEST;
        c5.insets = new Insets(2, 4, 2, 4);
        // label
        c5.weightx = 1.0;
        c5.weighty = 0.0;
        c5.gridwidth = GridBagConstraints.REMAINDER; // one per row
        Label labelAttrSelect = new Label(LABEL_SELECT_ATTR);
        gridbag5.setConstraints(labelAttrSelect, c5);
        p5.add(labelAttrSelect);
        // checkboxes
        c5.fill = GridBagConstraints.NONE;
        c5.weightx = 0.0;
        c5.insets = new Insets(0, 12, 1, 12);
        checkboxAttrSF = new Checkbox(CHECK_LABEL_PRESERVE_SOURCEFILE);
        gridbag5.setConstraints(checkboxAttrSF, c5);
        p5.add(checkboxAttrSF);
        checkboxAttrLNT = new Checkbox(CHECK_LABEL_PRESERVE_LINENUMBERS);
        gridbag5.setConstraints(checkboxAttrLNT, c5);
        p5.add(checkboxAttrLNT);
        checkboxAttrLVT = new Checkbox(CHECK_LABEL_PRESERVE_LOCALVARIABLES);
        gridbag5.setConstraints(checkboxAttrLVT, c5);
        p5.add(checkboxAttrLVT);
        // gap
        c5.fill = GridBagConstraints.BOTH;
        c5.weighty = 1.0;
        c5.insets = new Insets(2, 4, 2, 4);
        Panel space5a = new Panel();
        gridbag5.setConstraints(space5a, c5);
        p5.add(space5a);
        // script file selection text-field and browse button
        c5.weighty = 0.0;
        Panel p5lower = new Panel();
        GridBagLayout gridbag5a = new GridBagLayout();
        GridBagConstraints c5a = new GridBagConstraints();
        p5lower.setLayout(gridbag5a);
        c5a.fill = GridBagConstraints.BOTH;
        c5a.anchor = GridBagConstraints.WEST;
        c5a.weightx = 1.0;
        c5a.insets = new Insets(4, 4, 4, 4);
        c5a.gridwidth = GridBagConstraints.REMAINDER; //end of row
        Label labelRgs = new Label(LABEL_SELECT_RGS_OUT);
        gridbag5a.setConstraints(labelRgs, c5a);
        p5lower.add(labelRgs);
        c5a.gridwidth = 1;
        rgsOutField = new TextField(FIELD_DEFAULT_RGS_OUT);
        gridbag5a.setConstraints(rgsOutField, c5a);
        p5lower.add(rgsOutField);
        c5a.weightx = 0.0;
        c5a.gridwidth = GridBagConstraints.REMAINDER; //end of row
        Button button5a = new Button(BUTTON_BROWSE);
        button5a.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {browse(rgsOutField);}});
        gridbag5a.setConstraints(button5a, c5a);
        p5lower.add(button5a);
        gridbag5.setConstraints(p5lower, c5);
        p5.add(p5lower);

        // Set up the synchronous tree control for two of the Lists
        java.awt.List[] lists = {listClass, listClassMF};
        tree = new TreeControl(lists);

        // Link all dependent UI elements with event handlers
        // 3. Select apps
        checkboxApp.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxAppAction();}});
        listApp.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {listAppAction();}});
        // 4. Select applets
        checkboxApplet.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxAppletAction();}});
        listApplet.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {listAppletAction();}});
        // 5. Select beans
        checkboxBean.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxBeanAction();}});
        listBean.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {listBeanAction();}});
        // 6. Select classes
        listClass.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {listClassAction();}});
        checkboxClass.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxClassAction();}});
        checkboxPublicMF.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxPublicMFAction();}});
        checkboxProtectedMF.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxProtectedMFAction();}});
        checkboxMFBoth.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxMFBothAction();}});
        checkboxMOnly.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxMOnlyAction();}});
        checkboxFOnly.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxFOnlyAction();}});
        // 7. Select methods and fields
        listClassMF.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {listClassAction();}});
        checkboxAllMethods.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxAllMethodsAction();}});
        checkboxMethod.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxMethodAction();}});
        listMethods.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {listMethodsAction();}});
        checkboxAllFields.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxAllFieldsAction();}});
        checkboxField.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxFieldAction();}});
        listFields.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {listFieldsAction();}});
        // 8. Select attributes
        checkboxAttrSF.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxAttrSFAction();}});
        checkboxAttrLNT.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxAttrLNTAction();}});
        checkboxAttrLVT.addItemListener(new ItemListener()
        {public void itemStateChanged(ItemEvent e) {checkboxAttrLVTAction();}});

        // Return an array of the generated panels
        Panel[] panels = {p1, p2, p3, p4, p5};
        return panels;
    }

    // Browse for a file and set it in the specified TextField.
    void browse(TextField tf)
    {
        // Create the dialog, pointed at the last directory used in any file dialog
        FileDialog fd = new FileDialog(this, FD_SELECT, FileDialog.LOAD);
        if (lastDir != null)
        {
            fd.setDirectory(lastDir);
        }
        fd.setVisible(true);

        // Set the text box and save the current directory for next time
        String dir = fd.getDirectory();
        String file = fd.getFile();
        if (dir != null && file != null)
        {
            tf.setText(dir + file);
            lastDir = dir;
        }
    }


    // Direct responses to user actions
    //
    // App class list checkbox hit
    void checkboxAppAction()
    {
        // Preserve the class and main method
        Cons cons = (Cons)guiDB.getAppElement(listApp.getSelectedIndex());
        ClPreserve clp = (ClPreserve)cons.car;
        MdPreserve mdp = (MdPreserve)cons.cdr;
        clp.setPreserve(checkboxApp.getState());
        mdp.setPreserve(checkboxApp.getState());

        // Update dependent UI
        updateCheckboxApplet();
        updateCheckboxBean();
        updateCheckboxClass();
        updateCheckboxMethod();
    }

    // Item in app class list selected
    void listAppAction()
    {
        // Update dependent UI
        updateCheckboxApp();
    }

    // Applet class list checkbox hit
    void checkboxAppletAction()
    {
        // Preserve the class
        guiDB.getAppletElement(listApplet.getSelectedIndex()).setPreserve(checkboxApplet.getState());

        // Update dependent UI
        updateCheckboxApp();
        updateCheckboxBean();
        updateCheckboxClass();
    }

    // Item in applet class list selected
    void listAppletAction()
    {
        // Update dependent UI
        updateCheckboxApplet();
    }

    // Bean class list checkbox hit
    void checkboxBeanAction()
    {
        // Preserve the class
        guiDB.getBeanElement(listBean.getSelectedIndex()).setPreserve(checkboxBean.getState());

        // Update dependent UI
        updateCheckboxApp();
        updateCheckboxApplet();
        updateCheckboxClass();
    }

    // Item in bean class list selected
    void listBeanAction()
    {
        // Update dependent UI
        updateCheckboxBean();
    }

    // Item in class list selected
    void listClassAction()
    {
        // Update the current class preservation data structure
        try
        {
            if (listClass.getSelectedItem() != null)
            {
                int index = listClass.getSelectedIndex();
                PkClEntry entry = (PkClEntry)tree.getEntry(index);
                PkCl pkcl = entry.getPkCl();
                currentClPreserve = (ClPreserve)guiDB.preserveClass.get(pkcl);
                if (currentClPreserve == null)
                {
                    // Add a fresh class preservation structure to the DB
                    currentClPreserve = new ClPreserve(pkcl);
                    guiDB.preserveClass.put(pkcl, currentClPreserve);
                }
            }
            else
            {
                currentClPreserve = null;
            }
        }
        catch (Exception e)
        {
            currentClPreserve = null;
        }

        // Update dependent UI
        updateCheckboxClass();
        updateListMethods();
        updateListFields();
    }

    // Class checked
    void checkboxClassAction()
    {
        // Update the data structures
        currentClPreserve.setPreserve(checkboxClass.getState());

        // Update dependent UI
        updateCheckboxApp();
        updateCheckboxApplet();
        updateCheckboxBean();
        updateListMethods();
        updateListFields();
        updateCheckboxPublicMF();
    }

    // Preserve public methods and fields checked
    void checkboxPublicMFAction()
    {
        // Update the data structures
        currentClPreserve.setKeepPublic(checkboxPublicMF.getState());

        // Update dependent UI
        updateListMethods();
        updateListFields();
        updateCheckboxProtectedMF();
        updateCheckboxMFGroup();
    }

    // Preserve public, protected, package methods and fields checked
    void checkboxProtectedMFAction()
    {
        // Update the data structures
        currentClPreserve.setKeepProtected(checkboxProtectedMF.getState());

        // Update dependent UI
        updateListMethods();
        updateListFields();
        updateCheckboxMFGroup();
    }

    // Preserve both methods and fields checked
    void checkboxMFBothAction()
    {
        // Update the data structures
        if (checkboxMFBoth.getState())
        {
            currentClPreserve.keepMethodsAndFields();
        }

        // Update dependent UI
        updateListMethods();
        updateListFields();
    }

    // Preserve methods only checked
    void checkboxMOnlyAction()
    {
        // Update the data structures
        if (checkboxMOnly.getState())
        {
            currentClPreserve.keepMethodsOnly();
        }

        // Update dependent UI
        updateListMethods();
        updateListFields();
    }

    // Preserve fields only checked
    void checkboxFOnlyAction()
    {
        // Update the data structures
        if (checkboxFOnly.getState())
        {
            currentClPreserve.keepFieldsOnly();
        }

        // Update dependent UI
        updateListMethods();
        updateListFields();
    }

    // "Show all" methods checked
    void checkboxAllMethodsAction()
    {
        // Update dependent UI
        updateListMethods();
    }

    // Method checked in list
    void checkboxMethodAction()
    {
        // Update the data structures
        currentMdPreserve.setPreserve(checkboxMethod.getState());

        // Update dependent UI
        updateCheckboxApp();
    }

    // Method selected in list
    void listMethodsAction()
    {
        // Update currently selected method
        int index = listMethods.getSelectedIndex();
        if (index != -1)
        {
            Md md = (Md)vecMethods.elementAt(index);
            currentMdPreserve = (MdPreserve)guiDB.preserveMethod.get(md);
            if (currentMdPreserve == null)
            {
                currentMdPreserve = new MdPreserve();
                guiDB.preserveMethod.put(md, currentMdPreserve);
            }
        }
        else
        {
            currentMdPreserve = null;
        }

        // Update UI dependencies
        updateCheckboxMethod();
    }

    // "Show all" fields checked
    void checkboxAllFieldsAction()
    {
        // Update dependent UI
        updateListFields();
    }

    // Field checked in list
    void checkboxFieldAction()
    {
        // Update the data structures
        currentFdPreserve.setPreserve(checkboxField.getState());
    }

    // Field selected in list
    void listFieldsAction()
    {
        // Update currently selected field
        int index = listFields.getSelectedIndex();
        if (index != -1)
        {
            Fd fd = (Fd)vecFields.elementAt(index);
            currentFdPreserve = (FdPreserve)guiDB.preserveField.get(fd);
            if (currentFdPreserve == null)
            {
                currentFdPreserve = new FdPreserve();
                guiDB.preserveField.put(fd, currentFdPreserve);
            }
        }
        else
        {
            currentFdPreserve = null;
        }

        // Update UI dependencies
        updateCheckboxField();
    }

    // Attribute 'SourceFile' checkbox hit
    void checkboxAttrSFAction()
    {
        guiDB.preserveSourceFile = checkboxAttrSF.getState();
    }

    // Attribute 'LineNumberTable' checkbox hit
    void checkboxAttrLNTAction()
    {
        guiDB.preserveLineNumberTable = checkboxAttrLNT.getState();
    }

    // Attribute 'LocalVariableTable' checkbox hit
    void checkboxAttrLVTAction()
    {
        guiDB.preserveLocalVariableTable = checkboxAttrLVT.getState();
    }


    // Update UI based on current state
    //
    // Indirect change to checkbox for app preservation
    private void updateCheckboxApp()
    {
        checkboxApp.setEnabled(listApp.getSelectedItem() != null);
        if (listApp.getSelectedIndex() != -1)
        {
            Cons cons = (Cons)guiDB.getAppElement(listApp.getSelectedIndex());
            ClPreserve clp = (ClPreserve)cons.car;
            MdPreserve mdp = (MdPreserve)cons.cdr;
            checkboxApp.setState(clp.isPreserve() && mdp.isPreserve());
        }
        else
        {
            checkboxApp.setState(false);
        }
        checkboxApp.validate();
    }

    // Indirect change to checkbox for applet preservation
    private void updateCheckboxApplet()
    {
        checkboxApplet.setEnabled(listApplet.getSelectedItem() != null);
        if (listApplet.getSelectedIndex() != -1)
        {
            ClPreserve clp = guiDB.getAppletElement(listApplet.getSelectedIndex());
            checkboxApplet.setState(clp.isPreserve());
        }
        else
        {
            checkboxApplet.setState(false);
        }
        checkboxApplet.validate();
    }

    // Indirect change to checkbox for bean preservation
    private void updateCheckboxBean()
    {
        checkboxBean.setEnabled(listBean.getSelectedItem() != null);
        if (listBean.getSelectedIndex() != -1)
        {
            ClPreserve clp = guiDB.getBeanElement(listBean.getSelectedIndex());
            checkboxBean.setState(clp.isPreserve());
        }
        else
        {
            checkboxBean.setState(false);
        }
        checkboxBean.validate();
    }

    // Indirect change to checkbox for class preservation
    private void updateCheckboxClass()
    {
        // Respond to change
        PkCl pkcl = null;
        if (listClass.getSelectedItem() != null)
        {
            int index = listClass.getSelectedIndex();
            PkClEntry entry = null;
            try
            {
                entry = (PkClEntry)tree.getEntry(index);
                pkcl = entry.getPkCl();
            }
            catch (Exception e)
            {
                pkcl = null;
            }
        }
        checkboxClass.setEnabled(pkcl != null);
        checkboxClass.setState(currentClPreserve == null ? false : currentClPreserve.isPreserve());
        checkboxClass.validate();

        // Update dependencies
        updateCheckboxPublicMF();
    }

    // Indirect change to checkbox for public method/field preservation
    private void updateCheckboxPublicMF()
    {
        // Respond to change
        if (checkboxClass.isEnabled() && checkboxClass.getState())
        {
            checkboxPublicMF.setEnabled(true);
            checkboxPublicMF.setState(currentClPreserve == null ? false : currentClPreserve.isKeepPublic());
        }
        else
        {
            checkboxPublicMF.setEnabled(false);
            checkboxPublicMF.setState(false);
        }
        checkboxPublicMF.validate();

        // Update dependencies
        updateCheckboxProtectedMF();
        updateCheckboxMFGroup();
    }

    // Indirect change to checkbox for public method/field preservation
    private void updateCheckboxProtectedMF()
    {
        // Respond to change
        if (checkboxPublicMF.isEnabled() && checkboxPublicMF.getState())
        {
            checkboxProtectedMF.setEnabled(true);
            checkboxProtectedMF.setState(currentClPreserve == null ? false : currentClPreserve.isKeepProtected());
        }
        else
        {
            checkboxProtectedMF.setEnabled(false);
            checkboxProtectedMF.setState(false);
        }
        checkboxProtectedMF.validate();
    }

    // Indirect change to checkbox for public method/field preservation
    private void updateCheckboxMFGroup()
    {
        // Respond to change
        if (checkboxPublicMF.isEnabled() && checkboxPublicMF.getState())
        {
            checkboxMFBoth.setEnabled(true);
            checkboxMFBoth.setState(currentClPreserve == null ? false : currentClPreserve.isKeepMethodsAndFields());
            checkboxMOnly.setEnabled(true);
            checkboxMOnly.setState(currentClPreserve == null ? false : currentClPreserve.isKeepMethodsOnly());
            checkboxFOnly.setEnabled(true);
            checkboxFOnly.setState(currentClPreserve == null ? false : currentClPreserve.isKeepFieldsOnly());
        }
        else
        {
            checkboxMFBoth.setEnabled(false);
            checkboxMFBoth.setState(true);
            checkboxMOnly.setEnabled(false);
            checkboxMOnly.setState(false);
            checkboxFOnly.setEnabled(false);
            checkboxFOnly.setState(false);
        }
        checkboxMFBoth.validate();
        checkboxMOnly.validate();
        checkboxFOnly.validate();
    }

    // Indirect change to checkbox for method preservation
    private void updateCheckboxMethod()
    {
        // Respond to change
        checkboxMethod.setEnabled(listMethods.getSelectedItem() != null);
        checkboxMethod.setState(currentMdPreserve == null ? false : currentMdPreserve.isPreserve());
        checkboxMethod.validate();
    }

    // Update the current list of methods which can be preserved
    private void updateListMethods()
    {
        // Clear the method list
        vecMethods.removeAllElements();
        listMethods.removeAll();
        currentMdPreserve = null;

        // Get list of valid methods
        Vector vec = new Vector();

        // Refill only if we have a class currently selected
        if (currentClPreserve != null)
        {
            // Can be a Pk to represent wildcarding
            PkCl pkcl = currentClPreserve.getPkCl();
            if (pkcl instanceof Cl)
            {
                Cl cl = (Cl)pkcl;
                try
                {
                    for (Enumeration mdEnum = cl.getMethodEnum(); mdEnum.hasMoreElements(); )
                    {
                        Md md = (Md)mdEnum.nextElement();

                        // Does it qualify for a listing?
                        // (Must be non-synthetic.
                        //  If private, must be one of the special Serialization methods.)
                        if (!md.isSynthetic())
                        {
                            int modifiers = md.getModifiers();
                            if (checkboxAllMethods.getState() || !(Modifier.isPrivate(modifiers) || ((currentClPreserve.isKeepProtected() || (currentClPreserve.isKeepPublic() && Modifier.isPublic(modifiers))) && !currentClPreserve.isKeepFieldsOnly())))
                            {
                                vec.addElement(md);
                            }
                            // Special check for the Serialization 'private' methods -- they are an anomaly and
                            // are to be displayed also
                            else if (Modifier.isPrivate(modifiers) &&
                                     (METHOD_NAME_SER_READ.equals(md.getInName()) && METHOD_DESCRIPTOR_SER_READ.equals(md.getDescriptor())) ||
                                     (METHOD_NAME_SER_WRITE.equals(md.getInName()) && METHOD_DESCRIPTOR_SER_WRITE.equals(md.getDescriptor())))
                            {
                                vec.addElement(md);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    // Do nothing
                }
            }
        }

        if (vec.size() > 0)
        {
            // Sort the methods by name
            Md[] methods = new Md[vec.size()];        
            for (int i = 0; i < methods.length; i++)
            {
                methods[i] = (Md)vec.elementAt(i);
            }
            Sort.quicksort(methods, new Compare() {public boolean isLess(Object o1, Object o2)
                {return ((Md)o1).getInName().compareTo(((Md)o2).getInName()) < 0;}});
                
            // Copy the sorted methods into the list
            for (int i = 0; i < methods.length; i++)
            {
                vecMethods.addElement(methods[i]);
                listMethods.add(methods[i].toString());
            }
            
            // Select the first method as though the user had done so
            listMethods.select(0);
            listMethodsAction();
        }

        // Update dependencies
        updateCheckboxMethod();
    }

    // Indirect change to checkbox for field preservation
    private void updateCheckboxField()
    {
        // Respond to change
        checkboxField.setEnabled(listFields.getSelectedItem() != null);
        checkboxField.setState(currentFdPreserve == null ? false : currentFdPreserve.isPreserve());
        checkboxField.validate();
    }

    // Update the current list of fields which can be preserved
    private void updateListFields()
    {
        // Clear the field list
        vecFields.removeAllElements();
        listFields.removeAll();
        currentFdPreserve = null;

        // Get list of valid fields
        Vector vec = new Vector();

        // Refill only if we have a class currently selected
        if (currentClPreserve != null)
        {
            // Can be a Pk to represent wildcarding
            PkCl pkcl = currentClPreserve.getPkCl();
            if (pkcl instanceof Cl)
            {
                Cl cl = (Cl)pkcl;
                try
                {
                    for (Enumeration fdEnum = cl.getFieldEnum(); fdEnum.hasMoreElements(); )
                    {
                        Fd fd = (Fd)fdEnum.nextElement();

                        // Does it qualify for a listing?
                        // (Must be non-synthetic.)
                        if (!fd.isSynthetic())
                        {
                            int modifiers = fd.getModifiers();
                            if (checkboxAllFields.getState() 
                                || !(Modifier.isPrivate(modifiers) 
                                     || ((currentClPreserve.isKeepProtected() 
                                          || (currentClPreserve.isKeepPublic() 
                                              && Modifier.isPublic(modifiers)))
                                         && !currentClPreserve.isKeepMethodsOnly())))
                            {
                                vec.addElement(fd);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    // Do nothing
                }
            }
        }

        if (vec.size() > 0)
        {
            // Sort the fields by name
            Fd[] fields = new Fd[vec.size()];        
            for (int i = 0; i < fields.length; i++)
            {
                fields[i] = (Fd)vec.elementAt(i);
            }
            Sort.quicksort(fields, new Compare() {public boolean isLess(Object o1, Object o2)
                {return ((Fd)o1).getInName().compareTo(((Fd)o2).getInName()) < 0;}});
                
            // Copy the sorted fields into the list
            for (int i = 0; i < fields.length; i++)
            {
                vecFields.addElement(fields[i]);
                listFields.add(fields[i].toString());
            }
            
            // Select the first field as though the user had done so
            listFields.select(0);
            listFieldsAction();
        }

        // Update dependencies
        updateCheckboxField();
    }


    // One-shot per jar update methods
    //
    // Update the content of all list UI elements on wizard.
    private void updateAllLists()
    {
        updateAppList();
        updateAppletList();
        updateBeanList();
        updateClassLists();

        // Refresh the frame
        validate();
    }

    // Update the content of the application list.
    private void updateAppList()
    {
        listApp.removeAll();
        for (Enumeration enm = guiDB.getAppElements(); enm.hasMoreElements(); )
        {
            Cons cons = (Cons)enm.nextElement();
            listApp.add(((ClPreserve)cons.car).getPkCl().getFullInName().replace('/', '.'));
        }
    }

    // Update the content of the applet list.
    private void updateAppletList()
    {
        listApplet.removeAll();
        for (Enumeration enm = guiDB.getAppletElements(); enm.hasMoreElements(); )
        {
            ClPreserve clp = (ClPreserve)enm.nextElement();
            listApplet.add(clp.getPkCl().getFullInName().replace('/', '.'));
        }
    }

    // Update the content of the bean list.
    private void updateBeanList()
    {
        listBean.removeAll();
        for (Enumeration enm = guiDB.getBeanElements(); enm.hasMoreElements(); )
        {
            ClPreserve clp = (ClPreserve)enm.nextElement();
            listBean.add(clp.getPkCl().getFullInName().replace('/', '.'));
        }
    }

    // Update the contents of the class tree lists.
    private void updateClassLists()
    {
        tree.removeAll();
        tree.add(new PkClEntry(0, guiDB.getClassTreeRoot()));
        currentClPreserve = null;
    }

    // Set enabled status of all components, basis on state of wizard
    private void updateEnabled()
    {
        // Enable top level UI
        boolean isEnabled = (guiDB != null) && guiDB.hasJar() && !guiDB.hasNewJar();
        listApp.setEnabled(isEnabled);
        listApplet.setEnabled(isEnabled);
        listBean.setEnabled(isEnabled);
        checkboxAttrSF.setEnabled(isEnabled);
        checkboxAttrLNT.setEnabled(isEnabled);
        checkboxAttrLVT.setEnabled(isEnabled);
        checkboxAttrSF.setState((guiDB != null) && guiDB.preserveSourceFile);
        checkboxAttrLNT.setState((guiDB != null) && guiDB.preserveLineNumberTable);
        checkboxAttrLVT.setState((guiDB != null) && guiDB.preserveLocalVariableTable);
        wizardPanel.setFinishEnabled(isEnabled);

        // Update dependent UI
        updateCheckboxApp();
        updateCheckboxApplet();
        updateCheckboxBean();
        updateCheckboxClass();
        updateCheckboxPublicMF();
        updateListMethods();
        updateCheckboxMethod();
        updateListFields();
        updateCheckboxField();

        // Refresh the frame
        validate();
    }


    // 3D raised panel displaying logo
    private static final String LOGO_LINE1 = "RetroGuard Script";
    private static final String LOGO_LINE2 = "Generator, v" + Version.getVersion();
    private static final String LOGO_IMAGE = "logo.gif";
    class LogoRaisedPanel extends RaisedPanel
    {
        Image image = null;
        public void paint(Graphics g)
        {
            super.paint(g);
            // paint the logo
            Dimension d = getSize();
            Font font = new Font("TimesRoman", Font.BOLD, 24);
            FontMetrics fm = getFontMetrics(font);
            g.setFont(font);
            if (image == null)
            {
                image = getToolkit().getImage(getClass().getResource(LOGO_IMAGE));
            }
            int imageWidth = 0;
            int imageHeight = 0;
            int logoLine1Width = fm.stringWidth(LOGO_LINE1);
            int logoLine2Width = fm.stringWidth(LOGO_LINE2);
            int maxLogoWidth = Math.max(logoLine1Width, logoLine2Width);
            if (image != null)
            {
                imageWidth = image.getWidth(this) * 6 / 5;
                imageHeight = image.getHeight(this);
                g.drawImage(image, 
                            (d.width - imageWidth - maxLogoWidth) / 2, 
                            (d.height - imageHeight) / 2,
                            this);
            }
            int x1 = (d.width + imageWidth - logoLine1Width) / 2;
            int y1 = d.height / 2 - 20;
            int x2 = (d.width + imageWidth - logoLine2Width) / 2;
            int y2 = d.height / 2 + 20;
            Color textColor = Color.darkGray;
            g.setColor(textColor);
            g.drawString(LOGO_LINE1, x1, y1);
            g.drawString(LOGO_LINE2, x2, y2);
        }
    }

    // 3D raised panel
    class RaisedPanel extends Panel
    {
        public void paint(Graphics g)
        {
            Dimension d = getSize();
            g.setColor(Color.lightGray);
            g.fill3DRect(0, 0, d.width, d.height, true);
        }
    }
}
