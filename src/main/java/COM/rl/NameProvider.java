package COM.rl;

import java.io.*;
import java.util.*;

import COM.rl.obf.*;
import COM.rl.obf.classfile.ClassFileException;

public class NameProvider
{
    public static final int CLASSIC_MODE = 0;
    public static final int CHANGE_NOTHING_MODE = 1;
    public static final int DEOBFUSCATION_MODE = 2;
    public static final int REOBFUSCATION_MODE = 3;

    public static final String DEFAULT_CFG_FILE_NAME = "retroguard.cfg";

    public static int uniqueStart = 100000;

    public static int currentMode = NameProvider.CLASSIC_MODE;

    private static File packagesFile = null;
    private static File classesFile = null;
    private static File methodsFile = null;
    private static File fieldsFile = null;
    private static File reobFile = null;
    private static File npLog = null;
    private static File roLog = null;

    private static List<String> protectedPackages = new ArrayList<String>();
    private static Map<String, String> classNameLookup = new HashMap<String, String>();
    private static Map<String, String> packageNameLookup = new HashMap<String, String>();

    private static List<PackageEntry> packageDefs = new ArrayList<PackageEntry>();
    private static List<ClassEntry> classDefs = new ArrayList<ClassEntry>();
    private static List<MethodEntry> methodDefs = new ArrayList<MethodEntry>();
    private static List<FieldEntry> fieldDefs = new ArrayList<FieldEntry>();

    private static Map<String, PackageEntry> packagesObf2Deobf = new HashMap<String, PackageEntry>();
    private static Map<String, PackageEntry> packagesDeobf2Obf = new HashMap<String, PackageEntry>();
    private static Map<String, ClassEntry> classesObf2Deobf = new HashMap<String, ClassEntry>();
    private static Map<String, ClassEntry> classesDeobf2Obf = new HashMap<String, ClassEntry>();
    private static Map<String, MethodEntry> methodsObf2Deobf = new HashMap<String, MethodEntry>();
    private static Map<String, MethodEntry> methodsDeobf2Obf = new HashMap<String, MethodEntry>();
    private static Map<String, FieldEntry> fieldsObf2Deobf = new HashMap<String, FieldEntry>();
    private static Map<String, FieldEntry> fieldsDeobf2Obf = new HashMap<String, FieldEntry>();

    public static String[] parseCommandLine(String[] args)
    {
        if ((args.length > 0) && (args[0].equalsIgnoreCase("-searge") || args[0].equalsIgnoreCase("-notch")))
        {
            return NameProvider.parseNameSheetModeArgs(args);
        }

        if (args.length < 5)
        {
            return args;
        }

        int idx;
        try
        {
            idx = Integer.parseInt(args[4]);
        }
        catch (NumberFormatException e)
        {
            System.err.println("ERROR: Invalid start index: " + args[4]);
            throw e;
        }

        NameProvider.uniqueStart = idx;

        String[] newArgs = new String[4];

        for (int i = 0; i < 4; ++i)
        {
            newArgs[i] = args[i];
        }

        return newArgs;
    }

    private static String[] parseNameSheetModeArgs(String[] args)
    {
        if (args.length < 2)
        {
            return null;
        }

        String configFileName = args[1];
        File configFile = new File(configFileName);
        if (!configFile.exists() || !configFile.isFile())
        {
            System.err.println("ERROR: could not find config file " + configFileName);
            return null;
        }

        String reobinput = null;
        String reoboutput = null;
        FileReader fileReader = null;
        BufferedReader reader = null;
        String[] newArgs = new String[4];
        try
        {
            fileReader = new FileReader(configFile);
            reader = new BufferedReader(fileReader);
            String line = "";
            while (line != null)
            {
                line = reader.readLine();
                if ((line == null) || line.trim().startsWith("#"))
                {
                    continue;
                }

                String[] defines = line.split("=");
                if (defines.length > 1)
                {
                    defines[1] = line.substring(defines[0].length() + 1).trim();
                    defines[0] = defines[0].trim();

                    if (defines[0].equalsIgnoreCase("packages"))
                    {
                        NameProvider.packagesFile = new File(defines[1]);
                        if (!NameProvider.packagesFile.exists() || !NameProvider.packagesFile.isFile())
                        {
                            NameProvider.packagesFile = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("classes"))
                    {
                        NameProvider.classesFile = new File(defines[1]);
                        if (!NameProvider.classesFile.exists() || !NameProvider.classesFile.isFile())
                        {
                            NameProvider.classesFile = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("methods"))
                    {
                        NameProvider.methodsFile = new File(defines[1]);
                        if (!NameProvider.methodsFile.exists() || !NameProvider.methodsFile.isFile())
                        {
                            NameProvider.methodsFile = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("fields"))
                    {
                        NameProvider.fieldsFile = new File(defines[1]);
                        if (!NameProvider.fieldsFile.exists() || !NameProvider.fieldsFile.isFile())
                        {
                            NameProvider.fieldsFile = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("reob"))
                    {
                        NameProvider.reobFile = new File(defines[1]);
                        if (!NameProvider.reobFile.exists() || !NameProvider.reobFile.isFile())
                        {
                            NameProvider.reobFile = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("input"))
                    {
                        newArgs[0] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("output"))
                    {
                        newArgs[1] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("reobinput"))
                    {
                        reobinput = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("reoboutput"))
                    {
                        reoboutput = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("script"))
                    {
                        newArgs[2] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("log"))
                    {
                        newArgs[3] = defines[1];
                    }
                    else if (defines[0].equalsIgnoreCase("nplog"))
                    {
                        NameProvider.npLog = new File(defines[1]);
                        if (NameProvider.npLog.exists() && !NameProvider.npLog.isFile())
                        {
                            NameProvider.npLog = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("rolog"))
                    {
                        NameProvider.roLog = new File(defines[1]);
                        if (NameProvider.roLog.exists() && !NameProvider.roLog.isFile())
                        {
                            NameProvider.roLog = null;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("startindex"))
                    {
                        try
                        {
                            int start = Integer.parseInt(defines[1]);
                            NameProvider.uniqueStart = start;
                        }
                        catch (NumberFormatException e)
                        {
                            System.err.println("Invalid start index: " + args[4]);
                            throw e;
                        }
                    }
                    else if (defines[0].equalsIgnoreCase("protectedpackage"))
                    {
                        NameProvider.protectedPackages.add(defines[1]);
                    }
                }
            }
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
                if (fileReader != null)
                {
                    fileReader.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }

        if (args[0].equalsIgnoreCase("-searge"))
        {
            NameProvider.currentMode = NameProvider.DEOBFUSCATION_MODE;
        }
        else if (args[0].equalsIgnoreCase("-notch"))
        {
            NameProvider.currentMode = NameProvider.REOBFUSCATION_MODE;
        }
        else
        {
            return null;
        }

        if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            newArgs[0] = reobinput;
            newArgs[1] = reoboutput;
        }

        if ((newArgs[0] == null) || (newArgs[1] == null) || (newArgs[2] == null) || (newArgs[3] == null))
        {
            return null;
        }

        try
        {
            NameProvider.initLogfiles();
            NameProvider.readSRGFiles();
        }
        catch (IOException e)
        {
            return null;
        }

        return newArgs;
    }

    private static void initLogfiles() throws IOException
    {
        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            if (NameProvider.npLog != null)
            {
                FileWriter writer = null;
                try
                {
                    writer = new FileWriter(NameProvider.npLog);
                }
                finally
                {
                    if (writer != null)
                    {
                        try
                        {
                            writer.close();
                        }
                        catch (IOException e)
                        {
                            // ignore
                        }
                    }
                }
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            if (NameProvider.roLog != null)
            {
                FileWriter writer = null;
                try
                {
                    writer = new FileWriter(NameProvider.roLog);
                }
                finally
                {
                    if (writer != null)
                    {
                        try
                        {
                            writer.close();
                        }
                        catch (IOException e)
                        {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    private static void readSRGFiles() throws IOException
    {
        if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            NameProvider.packagesFile = NameProvider.reobFile;
            NameProvider.classesFile = NameProvider.reobFile;
            NameProvider.methodsFile = NameProvider.reobFile;
            NameProvider.fieldsFile = NameProvider.reobFile;
        }

        NameProvider.readPackagesSRG();
        NameProvider.readClassesSRG();
        NameProvider.readMethodsSRG();
        NameProvider.readFieldsSRG();

        NameProvider.updateAllXrefs();
    }

    private static void updateAllXrefs()
    {
        for (PackageEntry entry : NameProvider.packageDefs)
        {
            NameProvider.packagesObf2Deobf.put(entry.obfName, entry);
            NameProvider.packagesDeobf2Obf.put(entry.deobfName, entry);
        }

        for (ClassEntry entry : NameProvider.classDefs)
        {
            NameProvider.classesObf2Deobf.put(entry.obfName, entry);
            NameProvider.classesDeobf2Obf.put(entry.deobfName, entry);
        }

        for (MethodEntry entry : NameProvider.methodDefs)
        {
            NameProvider.methodsObf2Deobf.put(entry.obfName + entry.obfDesc, entry);
            NameProvider.methodsDeobf2Obf.put(entry.deobfName + entry.deobfDesc, entry);
        }

        for (FieldEntry entry : NameProvider.fieldDefs)
        {
            NameProvider.fieldsObf2Deobf.put(entry.obfName, entry);
            NameProvider.fieldsDeobf2Obf.put(entry.deobfName, entry);
        }
    }

    private static void readPackagesSRG() throws IOException
    {
        if (NameProvider.packagesFile == null)
        {
            return;
        }

        List<String> lines = NameProvider.readAllLines(NameProvider.packagesFile);

        for (String line : lines)
        {
            String[] lineParts = line.split(" ");
            if ((lineParts.length != 3) || !lineParts[0].startsWith("PK:"))
            {
                continue;
            }

            PackageEntry entry = new PackageEntry();
            if (lineParts[1].equals("."))
            {
                entry.obfName = "";
            }
            else
            {
                entry.obfName = lineParts[1];
            }
            if (lineParts[2].equals("."))
            {
                entry.deobfName = "";
            }
            else
            {
                entry.deobfName = lineParts[2];
            }
            NameProvider.packageDefs.add(entry);
        }
    }

    private static void readClassesSRG() throws IOException
    {
        if (NameProvider.classesFile == null)
        {
            return;
        }

        List<String> lines = NameProvider.readAllLines(NameProvider.classesFile);

        for (String line : lines)
        {
            String[] lineParts = line.split(" ");
            if ((lineParts.length != 3) || !lineParts[0].startsWith("CL:"))
            {
                continue;
            }

            ClassEntry entry = new ClassEntry();
            entry.obfName = lineParts[1];
            entry.deobfName = lineParts[2];
            NameProvider.classDefs.add(entry);
        }
    }

    private static void readMethodsSRG() throws IOException
    {
        if (NameProvider.methodsFile == null)
        {
            return;
        }

        List<String> lines = NameProvider.readAllLines(NameProvider.methodsFile);

        for (String line : lines)
        {
            String[] lineParts = line.split(" ");
            if ((lineParts.length < 4) || !lineParts[0].startsWith("MD:"))
            {
                continue;
            }

            MethodEntry entry = new MethodEntry();
            entry.obfName = lineParts[1];
            entry.obfDesc = lineParts[2];
            entry.deobfName = lineParts[3];
            if (lineParts.length > 4)
            {
                entry.deobfDesc = lineParts[4];
            }
            NameProvider.methodDefs.add(entry);
        }
    }

    private static void readFieldsSRG() throws IOException
    {
        if (NameProvider.fieldsFile == null)
        {
            return;
        }

        List<String> lines = NameProvider.readAllLines(NameProvider.fieldsFile);

        for (String line : lines)
        {
            String[] lineParts = line.split(" ");
            if ((lineParts.length != 3) || !lineParts[0].startsWith("FD:"))
            {
                continue;
            }

            FieldEntry entry = new FieldEntry();
            entry.obfName = lineParts[1];
            entry.deobfName = lineParts[2];
            NameProvider.fieldDefs.add(entry);
        }
    }

    private static List<String> readAllLines(File file) throws IOException
    {
        List<String> lines = new ArrayList<String>();

        FileReader fileReader = null;
        BufferedReader reader = null;
        try
        {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);

            String line = reader.readLine();
            while (line != null)
            {
                lines.add(line);
                line = reader.readLine();
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
                if (fileReader != null)
                {
                    fileReader.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }

        return lines;
    }

    public static void log(String text)
    {
        System.out.println(text);

        File log = null;
        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            log = NameProvider.npLog;
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            log = NameProvider.roLog;
        }

        if (log == null)
        {
            return;
        }

        FileWriter fileWriter = null;
        BufferedWriter writer = null;
        try
        {
            fileWriter = new FileWriter(log, true);
            writer = new BufferedWriter(fileWriter);

            writer.write(text);
            writer.newLine();

            writer.flush();
        }
        catch (IOException e)
        {
            return;
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
                if (fileWriter != null)
                {
                    fileWriter.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }

    public static String getNewTreeItemName(TreeItem ti) throws ClassFileException
    {
//        NameProvider.log("TI: " + ti.getFullInName());
        if (ti instanceof Pk)
        {
            return NameProvider.getNewPackageName((Pk)ti);
        }
        else if (ti instanceof Cl)
        {
            return NameProvider.getNewClassName((Cl)ti);
        }
        else if (ti instanceof Md)
        {
            return NameProvider.getNewMethodName((Md)ti);
        }
        else if (ti instanceof Fd)
        {
            return NameProvider.getNewFieldName((Fd)ti);
        }
        else
        {
            NameProvider.log("# Warning: trying to rename unknown type " + ti.getFullInName());
            return null;
        }
    }

    public static String getNewPackageName(Pk pk)
    {
        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            NameProvider.log("PK: " + pk.getFullInName() + " " + pk.getFullInName());
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            String packageName = "p_" + (++NameProvider.uniqueStart) + "_" + pk.getInName();
            pk.setOutName(packageName);
            NameProvider.log("PK: " + pk.getFullInName() + " " + pk.getFullOutName());
            return packageName;
        }

        String packageName = pk.getFullInName();
        boolean known = NameProvider.packageNameLookup.containsKey(packageName);

        if (!NameProvider.isInProtectedPackage(packageName))
        {
            if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
            {
                if (NameProvider.packagesObf2Deobf.containsKey(pk.getFullInName()))
                {
                    String deobfName = NameProvider.packagesObf2Deobf.get(pk.getFullInName()).deobfName;
                    packageName = deobfName;
                    pk.setRepackageName(deobfName);
                    NameProvider.packageNameLookup.put(pk.getFullInName(), deobfName);
                }
                else
                {
                    // check if parent got remapped
                    TreeItem parent = pk.getParent();
                    if ((parent != null) && (parent instanceof Pk) && (parent.getParent() != null))
                    {
                        packageName = NameProvider.getNewPackageName(parent.getFullOutName()) + pk.getOutName();
                        pk.setRepackageName(packageName);
                    }
                    NameProvider.packageNameLookup.put(pk.getFullInName(), packageName);
                }
            }
            else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
            {
                if (NameProvider.packagesDeobf2Obf.containsKey(pk.getFullInName()))
                {
                    String obfName = NameProvider.packagesDeobf2Obf.get(pk.getFullInName()).obfName;
                    packageName = obfName;
                    pk.setRepackageName(obfName);
                    NameProvider.packageNameLookup.put(pk.getFullInName(), obfName);
                }
                else
                {
                    // check if parent got remapped
                    TreeItem parent = pk.getParent();
                    if ((parent != null) && (parent instanceof Pk) && (parent.getParent() != null))
                    {
                        packageName = NameProvider.getNewPackageName(parent.getFullOutName()) + pk.getOutName();
                        pk.setRepackageName(packageName);
                    }
                    NameProvider.packageNameLookup.put(pk.getFullInName(), packageName);
                }
            }
        }

        pk.setOutName(packageName);

        String inName = pk.getFullInName();
        String outName = pk.getFullOutName();

        if (inName.equals(""))
        {
            inName = ".";
        }
        if (outName.equals(""))
        {
            outName = ".";
        }

        if (!NameProvider.isInProtectedPackage(inName + "/") && !known)
        {
            NameProvider.log("PK: " + inName + " " + outName);
        }

        return packageName;
    }

    private static String getNewPackageName(String pkgName)
    {
        String newPkg = "";

        if (NameProvider.packageNameLookup.containsKey(pkgName))
        {
            newPkg = NameProvider.packageNameLookup.get(pkgName);
        }
        else
        {
            newPkg = pkgName;
        }

        return newPkg.equals("") ? newPkg : newPkg + "/";
    }

    public static String getNewClassName(Cl cl)
    {
        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            NameProvider.log("CL: " + cl.getFullInName() + " " + cl.getFullInName());
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            String className = "C_" + (++NameProvider.uniqueStart) + "_" + cl.getInName();
            cl.setOutName(className);
            NameProvider.log("CL: " + cl.getFullInName() + " " + cl.getFullOutName());
            return className;
        }

        String className = cl.getInName();

        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            if (NameProvider.classesObf2Deobf.containsKey(cl.getFullInName()))
            {
                String deobfName = NameProvider.classesObf2Deobf.get(cl.getFullInName()).deobfName;
                if (deobfName.contains("/"))
                {
                    className = deobfName.substring(deobfName.lastIndexOf('/') + 1);
                }
                else
                {
                    className = deobfName;
                }

                NameProvider.classNameLookup.put(cl.getInName(), className);
            }
            else
            {
                if (!NameProvider.isInProtectedPackage(cl.getFullInName()) && (NameProvider.uniqueStart > 0))
                {
                    className = "C_" + (NameProvider.uniqueStart++) + "_" + className;

                    NameProvider.classNameLookup.put(cl.getInName(), className);
                }
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            if (NameProvider.classesDeobf2Obf.containsKey(cl.getFullInName()))
            {
                String obfName = NameProvider.classesDeobf2Obf.get(cl.getFullInName()).obfName;
                if (obfName.contains("/"))
                {
                    className = obfName.substring(obfName.lastIndexOf('/') + 1);
                }
                else
                {
                    className = obfName;
                }

                NameProvider.classNameLookup.put(cl.getInName(), className);
            }
        }

        cl.setOutName(className);

        if (!NameProvider.isInProtectedPackage(cl.getFullInName()))
        {
            NameProvider.log("CL: " + cl.getFullInName() + " " + cl.getFullOutName());
        }

        return className;
    }

    public static String getNewMethodName(Md md) throws ClassFileException
    {
        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            NameProvider.log("MD: " + md.getFullInName() + " " + md.getDescriptor() + " "
                + md.getFullInName() + " " + md.getDescriptor());
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            String methodName = "func_" + (++NameProvider.uniqueStart) + "_" + md.getInName();
            md.setOutName(methodName);
            NameProvider.log("MD: " + md.getFullInName() + " " + md.getDescriptor() + " "
                + md.getFullOutName() + " " + md.getDescriptor());
            return methodName;
        }

        String methodName = md.getInName();

        String desc = md.getDescriptor();
        String newDesc = desc;

        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            if (NameProvider.methodsObf2Deobf.containsKey(md.getFullInName() + desc))
            {
                String deobfName = NameProvider.methodsObf2Deobf.get(md.getFullInName() + desc).deobfName;
                if (deobfName.contains("/"))
                {
                    methodName = deobfName.substring(deobfName.lastIndexOf('/') + 1);
                }
                else
                {
                    methodName = deobfName;
                }
            }
            else
            {
                if (!NameProvider.isInProtectedPackage(md.getFullInName()) && (NameProvider.uniqueStart > 0))
                {
                    methodName = "func_" + (NameProvider.uniqueStart++) + "_" + methodName;
                }
            }

            int i = 0;
            while (i < desc.length())
            {
                if (desc.charAt(i) == 'L')
                {
                    int j = i;
                    while (j < desc.length())
                    {
                        if (desc.charAt(j) == ';')
                        {
                            String cls = desc.substring(i + 1, j);

                            String pkgName;
                            String clsName;
                            if (cls.contains("/"))
                            {
                                pkgName = cls.substring(0, cls.lastIndexOf('/'));
                                clsName = cls.substring(cls.lastIndexOf('/') + 1);
                            }
                            else
                            {
                                pkgName = "";
                                clsName = cls;
                            }

                            String newCls = clsName;
                            if (NameProvider.classNameLookup.containsKey(clsName))
                            {
                                newCls = NameProvider.classNameLookup.get(clsName);
                            }

                            String newPkg = NameProvider.getNewPackageName(pkgName);
                            if (pkgName.equals(""))
                            {
                                newDesc = newDesc.replaceFirst("L" + clsName + ";", "L" + newPkg + newCls + ";");
                            }
                            else
                            {
                                newDesc = newDesc.replaceFirst("L" + pkgName + "/" + clsName + ";",
                                    "L" + newPkg + newCls + ";");
                            }

                            i = j;
                            break;
                        }
                        ++j;
                    }
                }
                ++i;
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            if (NameProvider.methodsDeobf2Obf.containsKey(md.getFullInName() + desc))
            {
                String obfName = NameProvider.methodsDeobf2Obf.get(md.getFullInName() + desc).obfName;
                if (obfName.contains("/"))
                {
                    methodName = obfName.substring(obfName.lastIndexOf('/') + 1);
                }
                else
                {
                    methodName = obfName;
                }
            }
            else
            {
                Cl cls = (Cl)md.getParent();


                Md tmpMd = new Md(cls, md.isSynthetic(), md.getInName(), md.getDescriptor(), md.getModifiers());

                Iterator<Cl> children = cls.getDownClasses();
//                NameProvider.log("Children: " + children.hasMoreElements());

                boolean goingDown = false;
                do
                {
                    tmpMd.setParent(cls);
//                    NameProvider.log("CHECKING: " + tmpMd.getFullInName() + desc);
                    if (NameProvider.methodsDeobf2Obf.containsKey(tmpMd.getFullInName() + desc))
                    {
                        String obfName = NameProvider.methodsDeobf2Obf.get(tmpMd.getFullInName() + desc).obfName;
                        if (obfName.contains("/"))
                        {
                            methodName = obfName.substring(obfName.lastIndexOf('/') + 1);
                        }
                        else
                        {
                            methodName = obfName;
                        }
                        break;
                    }

                    boolean found = false;
                    try
                    {
                        for (Cl iface : cls.getSuperInterfaces())
                        {
                            tmpMd.setParent(iface);
//                            NameProvider.log("CHECKING: " + tmpMd.getFullInName() + desc);
                            if (NameProvider.methodsDeobf2Obf.containsKey(tmpMd.getFullInName() + desc))
                            {
                                String obfName = NameProvider.methodsDeobf2Obf.get(tmpMd.getFullInName() + desc).obfName;
                                if (obfName.contains("/"))
                                {
                                    methodName = obfName.substring(obfName.lastIndexOf('/') + 1);
                                }
                                else
                                {
                                    methodName = obfName;
                                }
                                found = true;
                            }
                        }
                    }
                    catch (ClassFileException e)
                    {
                        // TODO printStackTrace
                        e.printStackTrace();
                    }

                    if (found)
                    {
                        break;
                    }

                    if (!goingDown)
                    {
                        try
                        {
                            cls = cls.getSuperCl();
                        }
                        catch (ClassFileException e)
                        {
                            // TODO printStackTrace
                            e.printStackTrace();
                            cls = null;
                        }
                        if (cls == null)
                        {
                            goingDown = true;
                        }
//                        else
//                        {
//                            NameProvider.log("Parent: " + cls.getFullInName());
//                        }
                    }

                    if (goingDown)
                    {
                        if (children.hasNext())
                        {
                            cls = children.next();
//                            NameProvider.log("Child: " + cls.getFullInName());
                        }
                        else
                        {
                            cls = null;
                        }
                    }
                } while (cls != null);
            }

            int i = 0;
            while (i < desc.length())
            {
                if (desc.charAt(i) == 'L')
                {
                    int j = i;
                    while (j < desc.length())
                    {
                        if (desc.charAt(j) == ';')
                        {
                            String cls = desc.substring(i + 1, j);

                            String pkgName;
                            String clsName;
                            if (cls.contains("/"))
                            {
                                pkgName = cls.substring(0, cls.lastIndexOf('/'));
                                clsName = cls.substring(cls.lastIndexOf('/') + 1);
                            }
                            else
                            {
                                pkgName = "";
                                clsName = cls;
                            }

                            String newCls = clsName;
                            if (NameProvider.classNameLookup.containsKey(clsName))
                            {
                                newCls = NameProvider.classNameLookup.get(clsName);
                            }

                            String newPkg = NameProvider.getNewPackageName(pkgName);
                            if (pkgName.equals(""))
                            {
                                newDesc = newDesc.replaceFirst("L" + clsName + ";", "L" + newPkg + newCls + ";");
                            }
                            else
                            {
                                newDesc = newDesc.replaceFirst("L" + pkgName + "/" + clsName + ";",
                                    "L" + newPkg + newCls + ";");
                            }

                            i = j;
                            break;
                        }
                        ++j;
                    }
                }
                ++i;
            }
        }

        md.setOutName(methodName);

        if (!NameProvider.isInProtectedPackage(md.getFullInName()))
        {
            NameProvider.log("MD: " + md.getFullInName() + " " + desc + " " + md.getFullOutName() + " " + newDesc);
        }

        return methodName;
    }

    public static String getNewFieldName(Fd fd)
    {
        if (NameProvider.currentMode == NameProvider.CHANGE_NOTHING_MODE)
        {
            NameProvider.log("FD: " + fd.getFullInName() + " " + fd.getFullInName());
            return null;
        }

        if (NameProvider.currentMode == NameProvider.CLASSIC_MODE)
        {
            String fieldName = "field_" + (++NameProvider.uniqueStart) + "_" + fd.getInName();
            fd.setOutName(fieldName);
            NameProvider.log("FD: " + fd.getFullInName() + " " + fd.getFullOutName());
            return fieldName;
        }

        String fieldName = fd.getInName();

        if (NameProvider.currentMode == NameProvider.DEOBFUSCATION_MODE)
        {
            if (NameProvider.fieldsObf2Deobf.containsKey(fd.getFullInName()))
            {
                String deobfName = NameProvider.fieldsObf2Deobf.get(fd.getFullInName()).deobfName;
                if (deobfName.contains("/"))
                {
                    fieldName = deobfName.substring(deobfName.lastIndexOf('/') + 1);
                }
                else
                {
                    fieldName = deobfName;
                }
            }
            else
            {
                if (!NameProvider.isInProtectedPackage(fd.getFullInName()) && (NameProvider.uniqueStart > 0))
                {
                    fieldName = "field_" + (NameProvider.uniqueStart++) + "_" + fieldName;
                }
            }
        }
        else if (NameProvider.currentMode == NameProvider.REOBFUSCATION_MODE)
        {
            if (NameProvider.fieldsDeobf2Obf.containsKey(fd.getFullInName()))
            {
                String obfName = NameProvider.fieldsDeobf2Obf.get(fd.getFullInName()).obfName;
                if (obfName.contains("/"))
                {
                    fieldName = obfName.substring(obfName.lastIndexOf('/') + 1);
                }
                else
                {
                    fieldName = obfName;
                }
            }
        }

        fd.setOutName(fieldName);

        if (!NameProvider.isInProtectedPackage(fd.getFullInName()))
        {
            NameProvider.log("FD: " + fd.getFullInName() + " " + fd.getFullOutName());
        }

        return fieldName;
    }

    private static boolean isInProtectedPackage(String fullInName)
    {
        for (String pkg : NameProvider.protectedPackages)
        {
            if (fullInName.startsWith(pkg))
            {
                return true;
            }
        }
        return false;
    }

}

class PackageEntry
{
    public String obfName;
    public String deobfName;
}

class ClassEntry
{
    public String obfName;
    public String deobfName;
}

class MethodEntry
{
    public String obfName;
    public String obfDesc;
    public String deobfName;
    public String deobfDesc;
}

class FieldEntry
{
    public String obfName;
    public String deobfName;
}
