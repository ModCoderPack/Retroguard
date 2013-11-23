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
 * A type signature parser to make a signature visitor visit an existing
 * signature.
 * 
 * @author Thomas Hallgren
 * @author Eric Bruneton
 */
public class SignatureReader
{

    /**
     * The signature to be read.
     */
    private final String signature;

    /**
     * Constructs a {@link SignatureReader} for the given signature.
     * 
     * @param signature
     *            A <i>ClassSignature</i>, <i>MethodTypeSignature</i>, or <i>FieldTypeSignature</i>.
     */
    public SignatureReader(final String signature)
    {
        this.signature = signature;
    }

    private void accept(final SignatureVisitor v, boolean isClass) throws SignatureException
    {
        String signature = this.signature;
        int len = signature.length();
        int pos;
        char c;

        if (signature.charAt(0) == '<')
        {
            pos = 2;
            do
            {
                int end = signature.indexOf(':', pos);
                v.visitFormalTypeParameter(signature.substring(pos - 1, end));
                pos = end + 1;

                c = signature.charAt(pos);
                if ((c == 'L') || (c == '[') || (c == 'T'))
                {
                    pos = SignatureReader.parseType(signature, pos, v.visitClassBound());
                }

                while ((c = signature.charAt(pos++)) == ':')
                {
                    pos = SignatureReader.parseType(signature, pos, v.visitInterfaceBound());
                }
            } while (c != '>');
        }
        else
        {
            pos = 0;
        }

        if (isClass)
        {
            pos = SignatureReader.parseType(signature, pos, v.visitSuperclass());
            while (pos < len)
            {
                pos = SignatureReader.parseType(signature, pos, v.visitInterface());
            }
        }
        else if (signature.charAt(pos) == '(')
        {
            pos++;
            while (signature.charAt(pos) != ')')
            {
                pos = SignatureReader.parseType(signature, pos, v.visitParameterType());
            }
            pos = SignatureReader.parseType(signature, pos + 1, v.visitReturnType());
            while (pos < len)
            {
                pos = SignatureReader.parseType(signature, pos + 1, v.visitExceptionType());
            }
        }
        else
        {
            throw new SignatureException("Invalid signature '" + signature + "'");
        }
    }

    /**
     * Makes the given visitor visit the signature of this {@link SignatureReader}. This signature is the one specified in the
     * constructor (see {@link #SignatureReader(String) SignatureReader}).
     * This method is intended to be called on a {@link SignatureReader} that was created using a <i>ClassSignature</i> (such as
     * the {@code signature} parameter of the {@code ClassVisitor.visit} method).
     * 
     * @param v
     *            the visitor that must visit this signature.
     */
    public void acceptClassType(final SignatureVisitor v) throws SignatureException
    {
        this.accept(v, true);
    }

    /**
     * Makes the given visitor visit the signature of this {@link SignatureReader}. This signature is the one specified in the
     * constructor (see {@link #SignatureReader(String) SignatureReader}).
     * This method is intended to be called on a {@link SignatureReader} that was created using a <i>MethodTypeSignature</i>
     * (such as the {@code signature} parameter of the {@code ClassVisitor.visitMethod} method).
     * 
     * @param v
     *            the visitor that must visit this signature.
     */
    public void acceptMethodType(final SignatureVisitor v) throws SignatureException
    {
        this.accept(v, false);
    }

    /**
     * Makes the given visitor visit the signature of this {@link SignatureReader}.
     * This signature is the one specified in the constructor (see {@link #SignatureReader(String) SignatureReader}).
     * This method is intended to be called on a {@link SignatureReader} that was created using a <i>FieldTypeSignature</i>, such
     * as the {@code signature} parameter of the {@code ClassVisitor.visitField} or {@code MethodVisitor.visitLocalVariable}
     * methods.
     * 
     * @param v
     *            the visitor that must visit this signature.
     */
    public void acceptFieldType(final SignatureVisitor v) throws SignatureException
    {
        SignatureReader.parseType(this.signature, 0, v);
    }

    /**
     * Parses a field type signature and makes the given visitor visit it.
     * 
     * @param signature
     *            a string containing the signature that must be parsed.
     * @param pos
     *            index of the first character of the signature to parsed.
     * @param v
     *            the visitor that must visit this signature.
     * @return the index of the first character after the parsed signature.
     */
    private static int parseType(final String signature, int pos, final SignatureVisitor v) throws SignatureException
    {
        char c;
        int start, end;
        boolean visited, inner;
        String name;

        switch (c = signature.charAt(pos++))
        {
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
            case 'F':
            case 'J':
            case 'D':
            case 'V':
                v.visitBaseType(c);
                return pos;

            case '[':
                return SignatureReader.parseType(signature, pos, v.visitArrayType());

            case 'T':
                end = signature.indexOf(';', pos);
                v.visitTypeVariable(signature.substring(pos, end));
                return end + 1;

            case 'L':
                start = pos;
                visited = false;
                inner = false;
                while (true)
                {
                    switch (c = signature.charAt(pos++))
                    {
                        case '.':
                        case ';':
                            if (!visited)
                            {
                                name = signature.substring(start, pos - 1);
                                if (inner)
                                {
                                    v.visitInnerClassType(name);
                                }
                                else
                                {
                                    v.visitClassType(name);
                                }
                            }
                            if (c == ';')
                            {
                                v.visitEnd();
                                return pos;
                            }
                            start = pos;
                            visited = false;
                            inner = true;
                            break;

                        case '<':
                            name = signature.substring(start, pos - 1);
                            if (inner)
                            {
                                v.visitInnerClassType(name);
                            }
                            else
                            {
                                v.visitClassType(name);
                            }
                            visited = true;
                            top:
                            while (true)
                            {
                                switch (c = signature.charAt(pos))
                                {
                                    case '>':
                                        break top;

                                    case '*':
                                        ++pos;
                                        v.visitTypeArgument();
                                        break;

                                    case '+':
                                    case '-':
                                        pos = SignatureReader.parseType(signature, pos + 1, v.visitTypeArgument(c));
                                        break;

                                    default:
                                        pos = SignatureReader.parseType(signature, pos, v.visitTypeArgument('='));
                                        break;
                                }
                            }
                            break;
                    }
                }

            default:
                throw new SignatureException("Invalid signature '" + signature + "'");
        }
    }
}
