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
     * @param message
     */
    public ClassFileException(String message)
    {
        super(message);
    }

    public ClassFileException(Throwable cause)
    {
        super(cause);
    }

    public ClassFileException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
