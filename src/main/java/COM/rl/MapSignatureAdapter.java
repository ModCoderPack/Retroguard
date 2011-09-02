/**
 * 
 */
package COM.rl;

import org.objectweb.asm.signature.SignatureVisitor;

public class MapSignatureAdapter implements SignatureVisitor
{
    private SignatureVisitor sv;

    /**
     * Constructor
     */
    public MapSignatureAdapter(SignatureVisitor sv)
    {
        this.sv = sv;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitFormalTypeParameter(java.lang.String)
     */
    @Override
    public void visitFormalTypeParameter(String name)
    {
        this.sv.visitFormalTypeParameter(name);
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitClassBound()
     */
    @Override
    public SignatureVisitor visitClassBound()
    {
        this.sv.visitClassBound();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitInterfaceBound()
     */
    @Override
    public SignatureVisitor visitInterfaceBound()
    {
        this.sv.visitInterfaceBound();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitSuperclass()
     */
    @Override
    public SignatureVisitor visitSuperclass()
    {
        this.sv.visitSuperclass();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitInterface()
     */
    @Override
    public SignatureVisitor visitInterface()
    {
        this.sv.visitInterface();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitParameterType()
     */
    @Override
    public SignatureVisitor visitParameterType()
    {
        this.sv.visitParameterType();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitReturnType()
     */
    @Override
    public SignatureVisitor visitReturnType()
    {
        this.sv.visitReturnType();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitExceptionType()
     */
    @Override
    public SignatureVisitor visitExceptionType()
    {
        this.sv.visitExceptionType();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitBaseType(char)
     */
    @Override
    public void visitBaseType(char descriptor)
    {
        this.sv.visitBaseType(descriptor);
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitTypeVariable(java.lang.String)
     */
    @Override
    public void visitTypeVariable(String name)
    {
        this.sv.visitTypeVariable(name);
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitArrayType()
     */
    @Override
    public SignatureVisitor visitArrayType()
    {
        this.sv.visitArrayType();
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitClassType(java.lang.String)
     */
    @Override
    public void visitClassType(String name)
    {
        System.err.println("visitClassType: " + name);
        this.sv.visitClassType(name);
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitInnerClassType(java.lang.String)
     */
    @Override
    public void visitInnerClassType(String name)
    {
        System.err.println("visitInnerClassType: " + name);
        this.sv.visitInnerClassType(name);
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitTypeArgument()
     */
    @Override
    public void visitTypeArgument()
    {
        this.sv.visitTypeArgument();
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitTypeArgument(char)
     */
    @Override
    public SignatureVisitor visitTypeArgument(char wildcard)
    {
        this.sv.visitTypeArgument(wildcard);
        return this;
    }

    /**
     * @see org.objectweb.asm.signature.SignatureVisitor#visitEnd()
     */
    @Override
    public void visitEnd()
    {
        this.sv.visitEnd();
    }

}
