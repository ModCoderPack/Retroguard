package COM.rl.obf.classfile;

import java.io.*;
import java.util.*;

public class ClassFileException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public ClassFileException()
    {
    }

    /**
     * Constructor
     * 
     * @param s
     */
    public ClassFileException(String s)
    {
        super(s);
    }
}
