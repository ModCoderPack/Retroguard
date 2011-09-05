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
     * @param message
     */
    public RGSException(String message)
    {
        super(message);
    }

    public RGSException(Throwable cause)
    {
        super(cause);
    }

    public RGSException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
