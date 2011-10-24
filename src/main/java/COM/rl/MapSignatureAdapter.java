/**
 * 
 */
package COM.rl;

import org.objectweb.asm.signature.*;

import COM.rl.obf.classfile.ClassFileException;
import COM.rl.obf.classfile.NameMapper;

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
}
