/*
 * Copyright 2009-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codebrowsing.tests

import junit.extensions.TestSetup
import junit.framework.Test

import org.codehaus.groovy.eclipse.test.TestProject
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.groovy.tests.builder.SimpleProgressMonitor
import org.eclipse.jdt.core.tests.util.Util
import org.eclipse.jdt.internal.core.CompilationUnit
import org.eclipse.jdt.internal.core.JavaProject

class BrowsingTestSetup extends TestSetup {

    private static Hashtable<String, String> savedPreferences
    private static TestProject testProject

    BrowsingTestSetup(Test test) {
        super(test)
    }

    protected void setUp() {
        savedPreferences = JavaCore.getOptions()
        testProject = new TestProject()
        testProject.autoBuilding = false
    }

    protected void tearDown() {
        JavaCore.setOptions(savedPreferences)
        testProject.dispose()
        testProject = null
    }

    static void setJavaPreference(String name, String value) {
        def opts = JavaCore.getOptions()
        opts.put(name, value)
        JavaCore.setOptions(opts)
    }

    static void addJUnit4() {
        IClasspathEntry entry = JavaCore.newContainerEntry(new Path('org.eclipse.jdt.junit.JUNIT_CONTAINER/4'))
        testProject.addEntry(testProject.project, entry)
    }

    static CompilationUnit addJavaSource(CharSequence contents, String name = 'Pojo', String pack = '') {
        def type = testProject.createJavaTypeAndPackage(pack, name + '.java', contents.toString())
        return type.compilationUnit as CompilationUnit
    }

    static GroovyCompilationUnit addGroovySource(CharSequence contents, String name = 'Pogo', String pack = '') {
        def file = testProject.createGroovyTypeAndPackage(pack, name + '.groovy', contents.toString())
        return JavaCore.createCompilationUnitFrom(file) as GroovyCompilationUnit
    }

    static void removeSources() {
        testProject.deleteWorkingCopies()
        IResource sourceFolder = testProject.sourceFolder.resource
        (sourceFolder as IContainer).members().each { IResource item -> Util.delete(item) }

        SimpleProgressMonitor spm = new SimpleProgressMonitor("$testProject.project.name clean");
        testProject.project.build(IncrementalProjectBuilder.CLEAN_BUILD, spm)
        spm.waitForCompletion()

        // TODO: Something in the project is not reset in the case of code select on package fragment...

        ((JavaProject) testProject.javaProject).resetCaches()

        /*spm = new SimpleProgressMonitor("$testProject.project.name refresh");
        testProject.javaProject.children.each {
            if (it instanceof IOpenable && !it.isConsistent()) {
                println "refreshing $it"
                it.makeConsistent(spm)
            }
        }
        spm.waitForCompletion()*/
    }
}
