package org.objectweb.asm.signature;

public class SignatureException extends Exception
{
    private static final long serialVersionUID = 1L;

    public SignatureException()
    {
    }

    public SignatureException(String message)
    {
        super(message);
    }

    public SignatureException(Throwable cause)
    {
        super(cause);
    }

    public SignatureException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
