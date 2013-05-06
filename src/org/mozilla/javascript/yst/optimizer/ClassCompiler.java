/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Igor Bukanov
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.javascript.yst.optimizer;

import org.mozilla.javascript.yst.*;

/**
 * Generates class files from script sources.
 *
 * since 1.5 Release 5
 * @author Igor Bukanov
 */

public class ClassCompiler
{
    /**
     * Construct ClassCompiler that uses the specified compiler environment
     * when generating classes.
     */
    public ClassCompiler(CompilerEnvirons compilerEnv)
    {
        if (compilerEnv == null) throw new IllegalArgumentException();
        this.compilerEnv = compilerEnv;
        this.mainMethodClassName = Codegen.DEFAULT_MAIN_METHOD_CLASS;
    }

    /**
     * Set the class name to use for main method implementation.
     * The class must have a method matching
     * <tt>public static void main(Script sc, String[] args)</tt>, it will be
     * called when <tt>main(String[] args)</tt> is called in the generated
     * class. The class name should be fully qulified name and include the
     * package name like in <tt>org.foo.Bar<tt>.
     */
    public void setMainMethodClass(String className)
    {
        // XXX Should this check for a valid class name?
        mainMethodClassName = className;
    }

    /**
     * Get the name of the class for main method implementation.
     * @see #setMainMethodClass(String)
     */
    public String getMainMethodClass()
    {
        return mainMethodClassName;
    }

    /**
     * Get the compiler environment the compiler uses.
     */
    public CompilerEnvirons getCompilerEnv()
    {
        return compilerEnv;
    }

    /**
     * Get the class that the generated target will extend.
     */
    public Class getTargetExtends()
    {
        return targetExtends;
    }

    /**
     * Set the class that the generated target will extend.
     *
     * @param extendsClass the class it extends
     */
    public void setTargetExtends(Class extendsClass)
    {
        targetExtends = extendsClass;
    }

    /**
     * Get the interfaces that the generated target will implement.
     */
    public Class[] getTargetImplements()
    {
        return targetImplements == null ? null : (Class[])targetImplements.clone();
    }

    /**
     * Set the interfaces that the generated target will implement.
     *
     * @param implementsClasses an array of Class objects, one for each
     *                          interface the target will extend
     */
    public void setTargetImplements(Class[] implementsClasses)
    {
        targetImplements = implementsClasses == null ? null : (Class[])implementsClasses.clone();
    }

    /**
     * Build class name for a auxiliary class generated by compiler.
     * If the compiler needs to generate extra classes beyond the main class,
     * it will call this function to build the auxiliary class name.
     * The default implementation simply appends auxMarker to mainClassName
     * but this can be overridden.
     */
    protected String makeAuxiliaryClassName(String mainClassName,
                                            String auxMarker)
    {
        return mainClassName+auxMarker;
    }

    /**
     * Compile JavaScript source into one or more Java class files.
     * The first compiled class will have name mainClassName.
     * If the results of {@link #getTargetExtends()} or
     * {@link #getTargetImplements()} are not null, then the first compiled
     * class will extend the specified super class and implement
     * specified interfaces.
     *
     * @return array where elements with even indexes specifies class name
     *         and the following odd index gives class file body as byte[]
     *         array. The initial element of the array always holds
     *         mainClassName and array[1] holds its byte code.
     */
    public Object[] compileToClassFiles(String source,
                                        String sourceLocation,
                                        int lineno,
                                        String mainClassName)
    {
        Parser p = new Parser(compilerEnv, compilerEnv.getErrorReporter());
        ScriptOrFnNode tree = p.parse(source, sourceLocation, lineno);
        String encodedSource = p.getEncodedSource();

        Class superClass = getTargetExtends();
        Class[] interfaces = getTargetImplements();
        String scriptClassName;
        boolean isPrimary = (interfaces == null && superClass == null);
        if (isPrimary) {
            scriptClassName = mainClassName;
        } else {
            scriptClassName = makeAuxiliaryClassName(mainClassName, "1");
        }

        Codegen codegen = new Codegen();
        codegen.setMainMethodClass(mainMethodClassName);
        byte[] scriptClassBytes
            = codegen.compileToClassFile(compilerEnv, scriptClassName,
                                         tree, encodedSource,
                                         false);

        if (isPrimary) {
            return new Object[] { scriptClassName, scriptClassBytes };
        }
        int functionCount = tree.getFunctionCount();
        ObjToIntMap functionNames = new ObjToIntMap(functionCount);
        for (int i = 0; i != functionCount; ++i) {
            FunctionNode ofn = tree.getFunctionNode(i);
            String name = ofn.getFunctionName();
            if (name != null && name.length() != 0) {
                functionNames.put(name, ofn.getParamCount());
            }
        }
        if (superClass == null) {
            superClass = ScriptRuntime.ObjectClass;
        }
        byte[] mainClassBytes
            = JavaAdapter.createAdapterCode(
                functionNames, mainClassName,
                superClass, interfaces, scriptClassName);

        return new Object[] { mainClassName, mainClassBytes,
                              scriptClassName, scriptClassBytes };
    }

    private String mainMethodClassName;
    private CompilerEnvirons compilerEnv;
    private Class targetExtends;
    private Class[] targetImplements;

}

