package COM.rl;

import COM.rl.obf.Cl;
import COM.rl.obf.Fd;
import COM.rl.obf.Md;
import COM.rl.obf.Pk;
import COM.rl.obf.TreeItem;

public class NameProvider
{
	public static final int CLASSIC_MODE = 0;
	public static final int CHANGE_NOTHING_MODE = 1;
	public static final int NAMESHEET_MODE = 2;

	public static int classicModeUniqueStart = 100000;
	
	public static int currentMode = CLASSIC_MODE;
	
	public static String[] parseCommandLine( String[] args )
	{
		System.out.println( "Parsing " + args.length + " parameters" );
		
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
		
		classicModeUniqueStart = idx;
		
		String[] newArgs = new String[4];
		
		for( int i = 0; i < 4; ++i )
			newArgs[i] = args[i];
		
		return newArgs;
	}
	
	public static String GetNewTreeItemName( TreeItem ti )
	{
		System.out.println("TI: " + ti.getFullInName());
		if( currentMode == CLASSIC_MODE )
			return null;
		
		if( ti instanceof Pk)
		{
			return GetNewPackageName((Pk)ti);
		}
		else if( ti instanceof Cl)
		{
			return GetNewClassName((Cl)ti);
		}
		else if( ti instanceof Md)
		{
			return GetNewMethodName((Md)ti);
		}
		else if( ti instanceof Fd)
		{
			return GetNewFieldName((Fd)ti);
		}
		else
		{
			System.out.println("Warning: trying to rename unknown type " + ti.getFullInName());
			return null;
		}
	}
	
	public static String GetNewPackageName( Pk pk )
	{
		System.out.println("PK: " + pk.getFullInName());
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
			return null;
		
		return null;
	}
	
	public static String GetNewClassName( Cl cl )
	{
		System.out.println("CL: " + cl.getFullInName());
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
			return null;
		
		return null;
	}
	
	public static String GetNewMethodName( Md md )
	{
		System.out.println("MD: " + md.getFullInName());
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
			return null;
		
		return null;
	}
	
	public static String GetNewFieldName( Fd fd )
	{
		System.out.println("FD: " + fd.getFullInName());
		if( currentMode == CLASSIC_MODE || currentMode == CHANGE_NOTHING_MODE )
			return null;
		
		return null;
	}
	
}
