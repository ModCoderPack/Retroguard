/**
 * 
 */
package COM_.rl;

import COM_.rl.obf.classfile.ClassFileException;
import COM_.rl.obf.classfile.NameMapper;
import de.oceanlabs.mcp.retroguard.shadow.asm.signature.*;

public class MapSignatureAdapter extends SignatureVisitor
{
    private SignatureVisitor sv;
    private String currentClassName;
    private NameMapper nm;

    /**
     * Constructor
     */
    public MapSignatureAdapter(SignatureVisitor sv, NameMapper nm)
    {
        super(0);
        this.sv = sv;
        this.nm = nm;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitFormalTypeParameter(java.lang.String)
     */
    @Override
    public void visitFormalTypeParameter(String name) throws SignatureException
    {
        this.sv.visitFormalTypeParameter(name);
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitClassBound()
     */
    @Override
    public SignatureVisitor visitClassBound() throws SignatureException
    {
        this.sv.visitClassBound();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitInterfaceBound()
     */
    @Override
    public SignatureVisitor visitInterfaceBound() throws SignatureException
    {
        this.sv.visitInterfaceBound();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitSuperclass()
     */
    @Override
    public SignatureVisitor visitSuperclass() throws SignatureException
    {
        this.sv.visitSuperclass();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitInterface()
     */
    @Override
    public SignatureVisitor visitInterface() throws SignatureException
    {
        this.sv.visitInterface();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitParameterType()
     */
    @Override
    public SignatureVisitor visitParameterType() throws SignatureException
    {
        this.sv.visitParameterType();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitReturnType()
     */
    @Override
    public SignatureVisitor visitReturnType() throws SignatureException
    {
        this.sv.visitReturnType();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitExceptionType()
     */
    @Override
    public SignatureVisitor visitExceptionType() throws SignatureException
    {
        this.sv.visitExceptionType();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitBaseType(char)
     */
    @Override
    public void visitBaseType(char descriptor) throws SignatureException
    {
        this.sv.visitBaseType(descriptor);
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitTypeVariable(java.lang.String)
     */
    @Override
    public void visitTypeVariable(String name) throws SignatureException
    {
        this.sv.visitTypeVariable(name);
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitArrayType()
     */
    @Override
    public SignatureVisitor visitArrayType() throws SignatureException
    {
        this.sv.visitArrayType();
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitClassType(java.lang.String)
     */
    @Override
    public void visitClassType(String name) throws SignatureException
    {
        this.currentClassName = name;
        String newName = null;
        try
        {
            newName = this.nm.mapClass(name);
        }
        catch (ClassFileException e)
        {
            throw new SignatureException(e);
        }
        this.sv.visitClassType(newName);
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitInnerClassType(java.lang.String)
     */
    @Override
    public void visitInnerClassType(String name) throws SignatureException
    {
        this.currentClassName += "." + name;
        String newName = null;
        try
        {
            newName = this.nm.mapClass(this.currentClassName);
        }
        catch (ClassFileException e)
        {
            throw new SignatureException(e);
        }
        this.sv.visitInnerClassType(newName);
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitTypeArgument()
     */
    @Override
    public void visitTypeArgument() throws SignatureException
    {
        this.sv.visitTypeArgument();
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitTypeArgument(char)
     */
    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) throws SignatureException
    {
        this.sv.visitTypeArgument(wildcard);
        return this;
    }

    /**
     * @see de.oceanlabs.mcp.retroguard.shadow.asm.signature.SignatureVisitor#visitEnd()
     */
    @Override
    public void visitEnd() throws SignatureException
    {
        this.sv.visitEnd();
    }
}
