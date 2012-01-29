/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm.signature;

/**
 * A visitor to visit a generic signature. The methods of this interface must be called in one of the three following orders (the
 * last one is the only valid order for a {@link SignatureVisitor} that is returned by a method of this interface):
 * <ul>
 * <li><i>ClassSignature</i> = {@code ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )* ( visitSuperClass
 * visitInterface* )}</li>
 * <li><i>MethodSignature</i> = {@code ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )* ( visitParameterType*
 * visitReturnType visitExceptionType>* )}</li>
 * <li><i>TypeSignature</i> = {@code visitBaseType | visitTypeVariable | visitArrayType | (visitClassType visitTypeArgument* (
 * visitInnerClassType visitTypeArgument* )* visitEnd ) )}</li>
 * </ul>
 * 
 * @author Thomas Hallgren
 * @author Eric Bruneton
 */
public abstract class SignatureVisitor
{

    /**
     * Wildcard for an "extends" type argument.
     */
    public final static char EXTENDS = '+';

    /**
     * Wildcard for a "super" type argument.
     */
    public final static char SUPER = '-';

    /**
     * Wildcard for a normal type argument.
     */
    public final static char INSTANCEOF = '=';

    /**
     * The ASM API version implemented by this visitor.
     */
    protected final int api;

    /**
     * Constructs a new {@link SignatureVisitor}.
     * 
     * @param api
     *            the ASM API version implemented by this visitor.
     */
    public SignatureVisitor(final int api)
    {
        this.api = api;
    }

    /**
     * Visits a formal type parameter.
     * 
     * @param name
     *            the name of the formal parameter.
     * @throws SignatureException
     */
    public void visitFormalTypeParameter(String name) throws SignatureException
    {
        // nothing
    }

    /**
     * Visits the class bound of the last visited formal type parameter.
     * 
     * @return a non null visitor to visit the signature of the class bound.
     * @throws SignatureException
     */
    public SignatureVisitor visitClassBound() throws SignatureException
    {
        return this;
    }

    /**
     * Visits an interface bound of the last visited formal type parameter.
     * 
     * @return a non null visitor to visit the signature of the interface bound.
     * @throws SignatureException
     */
    public SignatureVisitor visitInterfaceBound() throws SignatureException
    {
        return this;
    }

    /**
     * Visits the type of the super class.
     * 
     * @return a non null visitor to visit the signature of the super class
     *         type.
     * @throws SignatureException
     */
    public SignatureVisitor visitSuperclass() throws SignatureException
    {
        return this;
    }

    /**
     * Visits the type of an interface implemented by the class.
     * 
     * @return a non null visitor to visit the signature of the interface type.
     * @throws SignatureException
     */
    public SignatureVisitor visitInterface() throws SignatureException
    {
        return this;
    }

    /**
     * Visits the type of a method parameter.
     * 
     * @return a non null visitor to visit the signature of the parameter type.
     * @throws SignatureException
     */
    public SignatureVisitor visitParameterType() throws SignatureException
    {
        return this;
    }

    /**
     * Visits the return type of the method.
     * 
     * @return a non null visitor to visit the signature of the return type.
     * @throws SignatureException
     */
    public SignatureVisitor visitReturnType() throws SignatureException
    {
        return this;
    }

    /**
     * Visits the type of a method exception.
     * 
     * @return a non null visitor to visit the signature of the exception type.
     * @throws SignatureException
     */
    public SignatureVisitor visitExceptionType() throws SignatureException
    {
        return this;
    }

    /**
     * Visits a signature corresponding to a primitive type.
     * 
     * @param descriptor
     *            the descriptor of the primitive type, or 'V' for {@code void}.
     * @throws SignatureException
     */
    public void visitBaseType(char descriptor) throws SignatureException
    {
        // nothing
    }

    /**
     * Visits a signature corresponding to a type variable.
     * 
     * @param name
     *            the name of the type variable.
     * @throws SignatureException
     */
    public void visitTypeVariable(String name) throws SignatureException
    {
        // nothing
    }

    /**
     * Visits a signature corresponding to an array type.
     * 
     * @return a non null visitor to visit the signature of the array element
     *         type.
     * @throws SignatureException
     */
    public SignatureVisitor visitArrayType() throws SignatureException
    {
        return this;
    }

    /**
     * Starts the visit of a signature corresponding to a class or interface
     * type.
     * 
     * @param name
     *            the internal name of the class or interface.
     * @throws SignatureException
     */
    public void visitClassType(String name) throws SignatureException
    {
        // nothing
    }

    /**
     * Visits an inner class.
     * 
     * @param name
     *            the local name of the inner class in its enclosing class.
     * @throws SignatureException
     */
    public void visitInnerClassType(String name) throws SignatureException
    {
        // nothing
    }

    /**
     * Visits an unbounded type argument of the last visited class or inner
     * class type.
     * 
     * @throws SignatureException
     */
    public void visitTypeArgument() throws SignatureException
    {
        // nothing
    }

    /**
     * Visits a type argument of the last visited class or inner class type.
     * 
     * @param wildcard
     *            '+', '-' or '='.
     * @return a non null visitor to visit the signature of the type argument.
     * @throws SignatureException
     */
    public SignatureVisitor visitTypeArgument(char wildcard) throws SignatureException
    {
        return this;
    }

    /**
     * Ends the visit of a signature corresponding to a class or interface type.
     * 
     * @throws SignatureException
     */
    public void visitEnd() throws SignatureException
    {
        // nothing
    }
}
