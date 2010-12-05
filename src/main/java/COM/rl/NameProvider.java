package COM.rl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import COM.rl.obf.Cl;
import COM.rl.obf.Fd;
import COM.rl.obf.Md;
import COM.rl.obf.Pk;
import COM.rl.obf.TreeItem;

public class NameProvider
{
	public static final int CLASSIC_MODE = 0;
	public static final int CHANGE_NOTHING_MODE = 1;
    public static final int DEOBFUSCATION_MODE = 2;
    public static final int REOBFUSCATION_MODE = 3;

    public static final String DEFAULT_CFG_FILE_NAME = "retroguard.cfg";
	
	public static int uniqueStart = 100000;
	
	public static int currentMode = CLASSIC_MODE;
	
    private static File packagesFile = null;
    private static File classesFile = null;
    private static File methodsFile = null;
    private static File fieldsFile = null;
    private static File npLog = null;
    
    private static List<String> protectedPackages = new ArrayList<String>();
    private static HashMap<String, String> classNameLookup = new HashMap<String, String>();

    private static List<PackageEntry> packageDefs = new ArrayList<PackageEntry>();
    private static List<ClassEntry> classDefs = new ArrayList<ClassEntry>();
    private static List<MethodEntry> methodDefs = new ArrayList<MethodEntry>();
    private static List<FieldEntry> fieldDefs = new ArrayList<FieldEntry>();

    private static HashMap<String, PackageEntry> packagesObf2Deobf = new HashMap<String, PackageEntry>();
    private static HashMap<String, PackageEntry> packagesDeobf2Obf = new HashMap<String, PackageEntry>();
    private static HashMap<String, ClassEntry> classesObf2Deobf = new HashMap<String, ClassEntry>();
    private static HashMap<String, ClassEntry> classesDeobf2Obf = new HashMap<String, ClassEntry>();
    private static HashMap<String, MethodEntry> methodsObf2Deobf = new HashMap<String, MethodEntry>();
    private static HashMap<String, MethodEntry> methodsDeobf2Obf = new HashMap<String, MethodEntry>();
    private static HashMap<String, FieldEntry> fieldsObf2Deobf = new HashMap<String, FieldEntry>();
    private static HashMap<String, FieldEntry> fieldsDeobf2Obf = new HashMap<String, FieldEntry>();
    
	////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	public static String[] parseCommandLine( String[] args )
	{
		System.out.println( "Parsing " + args.length + " parameters" );
		
		if( (args.length > 0) && args[0].equalsIgnoreCase("-searge"))
			return parseNameSheetModeArgs( args );
		
		if( args.length < 5 )
			return args;
		
		int idx;
		try
		{
			idx = Integer.parseInt(args[4]);
		}
		catch( NumberFormatException up )
		{
			System.out.println("Invalid start index: " + args[4]);
			throw up;
		}
		
		System.out.println( "New start index is " + idx );
		
		uniqueStart = idx;
		
		String[] newArgs = new String[4];
		
		for( int i = 0; i < 4; ++i )
			newArgs[i] = args[i];
		
		return newArgs;
	}

	////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	private static String[] parseNameSheetModeArgs(String[] args)
	{
	    if(args.length < 2)
	        return null;
	    
	    String configFileName = args[1];
	    File configFile = new File(configFileName);
	    if(!configFile.exists() || !configFile.isFile())
	    {
	        System.err.println("ERROR: could not find config file " + configFileName);
	        return null;
	    }
	    
        FileReader fileReader = null;
        BufferedReader reader = null;
	    String[] newArgs = new String[4];
	    try
        {
	        fileReader = new FileReader( configFile );
	        reader = new BufferedReader( fileReader );
            String line = "";
            while(line != null)
            {
                line = reader.readLine();
                if(line == null || line.trim().startsWith( "#" ))
                    continue;
                
                String[] defines = line.split( "=" );
                if(defines.length > 1)
                {
                    defines[1] = line.substring( defines[0].length() + 1 ).trim();
                    defines[0] = defines[0].trim();

                    if(defines[0].equalsIgnoreCase("packages"))
                    {
                        packagesFile = new File(defines[1]);
                        if(!packagesFile.exists() || !packagesFile.isFile())
                            packagesFile = null;
                    }
                    else if(defines[0].equalsIgnoreCase("classes"))
                    {
                        classesFile = new File(defines[1]);
                        if(!classesFile.exists() || !classesFile.isFile())
                            classesFile = null;
                    }
                    else if(defines[0].equalsIgnoreCase("methods"))
                    {
                        methodsFile = new File(defines[1]);
                        if(!methodsFile.exists() || !methodsFile.isFile())
                            methodsFile = null;
                    }
                    else if(defines[0].equalsIgnoreCase("fields"))
                    {
                        fieldsFile = new File(defines[1]);
                        if(!fieldsFile.exists() || !fieldsFile.isFile())
                            fieldsFile = null;
                    }
                    else if(defines[0].equalsIgnoreCase("input"))
                    {
                        newArgs[0] = defines[1];
                    }
                    else if(defines[0].equalsIgnoreCase("output"))
                    {
                        newArgs[1] = defines[1];
                    }
                    else if(defines[0].equalsIgnoreCase("script"))
                    {
                        newArgs[2] = defines[1];
                    }
                    else if(defines[0].equalsIgnoreCase("log"))
                    {
                        newArgs[3] = defines[1];
                    }
                    else if(defines[0].equalsIgnoreCase("nplog"))
                    {
                        npLog = new File(defines[1]);
                        if(npLog.exists() && !npLog.isFile())
                            npLog = null;
                        
                        if(npLog != null)
                        {
                            FileWriter writer = null;
                            try
                            {
                                writer = new FileWriter( npLog );
                            }
                            catch(IOException e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                if(writer != null)
                                    writer.close();
                            }
                            
                        }
                    }
                    else if(defines[0].equalsIgnoreCase("startindex"))
                    {
                        try
                        {
                            int start = Integer.parseInt(defines[1]);
                            uniqueStart = start;
                        }
                        catch(NumberFormatException e)
                        {
                        }
                    }
                    else if(defines[0].equalsIgnoreCase("protectedpackage"))
                    {
                        protectedPackages.add( defines[1] );
                    }
                }
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if(reader != null)
                    reader.close();
                if(fileReader != null)
                    fileReader.close();
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
	    
        if(newArgs[0] == null || newArgs[1] == null || newArgs[2] == null || newArgs[3] == null)
            return null;

        if(args[0].equalsIgnoreCase( "-searge" ))
            currentMode = DEOBFUSCATION_MODE;
        else if(args[0].equalsIgnoreCase( "-notch" ))
            currentMode = REOBFUSCATION_MODE;
        else
            return null;

        readSRGFiles();
        
	    return newArgs;
	}

    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	private static void readSRGFiles()
	{
	    readPackagesSRG();
	    readClassesSRG();
	    readMethodsSRG();
	    readFieldsSRG();
	    
	    updateAllXrefs();
	}

    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static void updateAllXrefs()
    {
        for(PackageEntry entry : packageDefs)
        {
            packagesObf2Deobf.put( entry.obfName, entry );
            packagesDeobf2Obf.put( entry.deobfName, entry );
        }

        for(ClassEntry entry : classDefs)
        {
            classesObf2Deobf.put( entry.obfName, entry );
            classesDeobf2Obf.put( entry.deobfName, entry );
        }

        for(MethodEntry entry : methodDefs)
        {
            methodsObf2Deobf.put( entry.obfName + entry.obfDesc, entry );
            methodsDeobf2Obf.put( entry.deobfName + entry.deobfDesc, entry );
        }

        for(FieldEntry entry : fieldDefs)
        {
            fieldsObf2Deobf.put( entry.obfName, entry );
            fieldsDeobf2Obf.put( entry.deobfName, entry );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static void readPackagesSRG()
    {
        if(packagesFile == null)
            return;
        
        String[] lines = readAllLines( packagesFile );
        
        for(String line : lines)
        {
            String[] lineParts = line.split( " " );
            if(lineParts.length != 3 || !lineParts[0].startsWith( "PK:" ))
                continue;
            
            PackageEntry entry = new PackageEntry();
            entry.obfName = lineParts[1];
            if(lineParts[2].equals( "." ))
                entry.deobfName = "";
            else
                entry.deobfName = lineParts[2];
            packageDefs.add( entry );
        }
    }
	
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static void readClassesSRG()
    {
        if(classesFile == null)
            return;
        
        String[] lines = readAllLines( classesFile );
        
        for(String line : lines)
        {
            String[] lineParts = line.split( " " );
            if(lineParts.length != 3 || !lineParts[0].startsWith( "CL:" ))
                continue;
            
            ClassEntry entry = new ClassEntry();
            entry.obfName = lineParts[1];
            entry.deobfName = lineParts[2];
            classDefs.add( entry );
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static void readMethodsSRG()
    {
        if(methodsFile == null)
            return;
        
        String[] lines = readAllLines( methodsFile );
        
        for(String line : lines)
        {
            String[] lineParts = line.split( " " );
            if(lineParts.length != 5 || !lineParts[0].startsWith( "MD:" ))
                continue;
            
            MethodEntry entry = new MethodEntry();
            entry.obfName = lineParts[1];
            entry.obfDesc = lineParts[2];
            entry.deobfName = lineParts[3];
            entry.deobfDesc = lineParts[4];
            methodDefs.add( entry );
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static void readFieldsSRG()
    {
        if(fieldsFile == null)
            return;
        
        String[] lines = readAllLines( fieldsFile );
        
        for(String line : lines)
        {
            String[] lineParts = line.split( " " );
            if(lineParts.length != 3 || !lineParts[0].startsWith( "FD:" ))
                continue;
            
            FieldEntry entry = new FieldEntry();
            entry.obfName = lineParts[1];
            entry.deobfName = lineParts[2];
            fieldDefs.add( entry );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static String[] readAllLines( File file )
    {
        List<String> lines = new ArrayList<String>();
        
        FileReader fileReader = null;
        BufferedReader reader = null;
        try
        {
            fileReader = new FileReader( file );
            reader = new BufferedReader( fileReader );
            
            String line = reader.readLine();
            while(line != null)
            {
                lines.add( line );
                line = reader.readLine();
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if(reader != null)
                    reader.close();
                if(fileReader != null)
                    fileReader.close();
            }
            catch( Exception e )
            {
                e.printStackTrace();
                return null;
            }
        }
        
        String[] result = new String[lines.size()];
        lines.toArray( result );
        
        return result;
    }
    
	////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	private static void log( String text )
	{
	    System.out.println(text);
	    
	    if( npLog == null )
	        return;
	    
	    FileWriter fileWriter = null;
	    BufferedWriter writer = null;
	    try
        {
	        fileWriter = new FileWriter( npLog, true );
	        writer = new BufferedWriter( fileWriter );
	        
	        writer.write( text );
	        writer.newLine();
	        
	        writer.flush();
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return;
        }
        finally
        {
            try
            {
                if(writer != null)
                    writer.close();
                if(fileWriter != null)
                    fileWriter.close();
            }
            catch( IOException e )
            {
                e.printStackTrace();
            }
        }
	}
	
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	public static String getNewTreeItemName( TreeItem ti )
	{
		//log("TI: " + ti.getFullInName());
		if( currentMode == CLASSIC_MODE )
			return null;
		
		if( ti instanceof Pk)
		{
			return getNewPackageName((Pk)ti);
		}
		else if( ti instanceof Cl)
		{
			return getNewClassName((Cl)ti);
		}
		else if( ti instanceof Md)
		{
			return getNewMethodName((Md)ti);
		}
		else if( ti instanceof Fd)
		{
			return getNewFieldName((Fd)ti);
		}
		else
		{
			log("Warning: trying to rename unknown type " + ti.getFullInName());
			return null;
		}
	}
	
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	public static String getNewPackageName( Pk pk )
	{
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
		{
	        log("PK: " + pk.getFullInName());
			return null;
		}
		
		String packageName = pk.getInName();
		
		if(currentMode == DEOBFUSCATION_MODE)
		{
		    if(packagesObf2Deobf.containsKey( pk.getFullInName() ))
		    {
		        String deobfName = packagesObf2Deobf.get( pk.getFullInName() ).deobfName;
		        if(deobfName.contains( "/" ))
		            packageName = deobfName.substring( deobfName.lastIndexOf( '/' ) + 1 );
		        else
		            packageName = deobfName;
		    }
		    else
		    {
		    }
		}

        pk.setOutName( packageName );
		
        log("PK: " + pk.getFullInName() + " " + pk.getFullOutName());
        
		return packageName;
	}
	
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	public static String getNewClassName( Cl cl )
	{
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
		{
	        log("CL: " + cl.getFullInName());
			return null;
		}
		
		String className = cl.getInName();

        if(currentMode == DEOBFUSCATION_MODE)
        {
            if(classesObf2Deobf.containsKey( cl.getFullInName() ))
            {
                String deobfName = classesObf2Deobf.get( cl.getFullInName() ).deobfName;
                if(deobfName.contains( "/" ))
                    className = deobfName.substring( deobfName.lastIndexOf( '/' ) + 1 );
                else
                    className = deobfName;
            }
            else
            {
                if(!isInProtectedPackage(cl.getFullInName()))
                {
                    className = "c_" + (uniqueStart++) + "_" + className;

                    classNameLookup.put( cl.getInName(), className );
                }
            }
        }

        cl.setOutName( className );
        
        log("CL: " + cl.getFullInName() + " " + cl.getFullOutName());
		
		return className;
	}
	
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	public static String getNewMethodName( Md md )
	{
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
		{
	        log("MD: " + md.getFullInName());
			return null;
		}
		
        String methodName = md.getInName();
        
        String desc = md.getDescriptor();
        String newDesc = desc;
        
        if(currentMode == DEOBFUSCATION_MODE)
        {
            if(methodsObf2Deobf.containsKey( md.getFullInName() + desc ))
            {
                String deobfName = methodsObf2Deobf.get( md.getFullInName() + desc ).deobfName;
                if(deobfName.contains( "/" ))
                    methodName = deobfName.substring( deobfName.lastIndexOf( '/' ) + 1 );
                else
                    methodName = deobfName;
            }
            else
            {
                int i = 0;
                while(i < desc.length())
                {
                    if(desc.charAt( i ) == 'L')
                    {
                        int j = i;
                        while(j < desc.length())
                        {
                            if(desc.charAt( j ) == ';')
                            {
                                String cls = desc.substring( i + 1, j );
                                
                                if(classNameLookup.containsKey( cls ))
                                {
                                    String newCls = classNameLookup.get( cls );
                                    newDesc = newDesc.replace( cls, newCls );
                                }
                                
                                i = j;
                                break;
                            }
                            ++j;                    
                        }
                    }
                    ++i;
                }

                if(!isInProtectedPackage(md.getFullInName()))
                    methodName = "m_" + (uniqueStart++); // + "_" + methodName;
            }
        }
        
        md.setOutName( methodName );

        log("MD: " + md.getFullInName() + " " + desc + " " + md.getFullOutName() + " " + newDesc);
        
        return methodName;
	}
	
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
	public static String getNewFieldName( Fd fd )
	{
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
		{
	        log("FD: " + fd.getFullInName());
			return null;
		}
		
        String fieldName = fd.getInName();

        if(currentMode == DEOBFUSCATION_MODE)
        {
            if(fieldsObf2Deobf.containsKey( fd.getFullInName() ))
            {
                String deobfName = fieldsObf2Deobf.get( fd.getFullInName() ).deobfName;
                if(deobfName.contains( "/" ))
                    fieldName = deobfName.substring( deobfName.lastIndexOf( '/' ) + 1 );
                else
                    fieldName = deobfName;
            }
            else
            {
                if(!isInProtectedPackage(fd.getFullInName()))
                    fieldName = "f_" + (uniqueStart++); // + "_" + fieldName;
            }
        }
        
        fd.setOutName( fieldName );

        log("FD: " + fd.getFullInName() + " " + fd.getFullOutName());
        
        return fieldName;
	}

    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    private static boolean isInProtectedPackage( String fullInName )
    {
        for( String pkg : protectedPackages)
        {
            if(fullInName.startsWith( pkg ))
                return true;
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
