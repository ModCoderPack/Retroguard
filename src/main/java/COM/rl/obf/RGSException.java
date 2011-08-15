package COM.rl.obf;

import java.io.*;
import java.util.*;

public class RGSException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public RGSException()
    {
    }

    /**
     * Constructor
     * 
     * @param s
     */
    public RGSException(String s)
    {
        super(s);
    }
}
