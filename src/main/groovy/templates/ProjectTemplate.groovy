/*
 * Copyright (c) 2011,2012 Eric Berry <elberry@tellurianring.com>
 * Copyright (c) 2013 Christopher J. Stehno <chris@stehno.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package templates

import groovy.text.GStringTemplateEngine
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.StandardCopyOption
/**
 * This class is used to construct a ProjectTemplate. A project template consists of files and directories. This builder
 * can be used to set up the necessary files and directories needed for new projects.
 *
 * Eg.
 * <pre>
 * ProjectTemplate.fromUserDir {*    directory('src') { // creates new directory named 'src'
 *       dir('main') { // creates a new directory named 'main'
 *          d('java') { // creates a new directory named 'java'
 *             file('Class1.java') // creates a new file named 'Class1.java'
 *             f('Class2.java') // creates a new file named 'Class2.java'
 *}*}*}*}* </pre>
 *
 * Can also be used without method calls for directory and file.
 * Eg.
 * <pre>
 * ProjectTemplate.fromUserDir {*    'src/main' { // creates the directories 'src', and 'main'.
 *       'java' {*          'Class1.java' 'public class Class1 {}' // creates the file 'Class1.java' with some initial content.
 *}*       'resources' {}*}*}* </pre>
 *
 */
class ProjectTemplate {

    private File parent

    /**
     * Private so that it can't be accessed. Use one of the static 'fromUserDir' methods to start building a template.
     */
    private ProjectTemplate() {}


    /**
     * Creates a directory, and it's parents if they don't already exist.
     * @param nameparent
     * @param closure
     * @see #directory(String, Closure)
     */
    void directory(String name, Closure closure = {}) {
        File oldParent = parent
        if (parent) {
            parent = new File(parent, name)
        } else {
            parent = new File(name)
        }
        parent.mkdirs()
        closure.delegate = this
        closure()
        parent = oldParent
    }

    /**
     * Same as file method
     * @param args
     * @param name
     * @see #file(Map, String)
     */
    void f(Map args = [:], String name) {
        file(args, name)
    }

    /**
     * Same as file method
     * @param args
     * @param name
     * @see #file(String, String)
     */
    void f(String name, String content) {
        file(name, content)
    }

    File gtFile(String name, String addToPath) {
        File file
        if (parent && addToPath) {
            file = new File(parent.path + addToPath, name)
        } else if (parent && !addToPath) {
            file = new File(parent, name)
        } else {
            file = new File(name)
        }
        file.exists() ?: file.parentFile.mkdirs() && file.createNewFile()
        return file
    }
    /**
     * Creates a new file with the given name. If a 'content' argument is provided it will be appended, or replace the
     * content of the current file (if it exists) based on the value of the 'append' argument.
     * @param args Arguments to be used when creating the new file: [content: String, append: boolean]
     * @param name Name of the new file to be created.
     */
    void file(Map args = [:], String name) {
        File file
        def content
        println name
        if (args.content) {
            content = args.content.stripIndent()
        } else if (args.template) {
            if(args.path){
                  file=gtFile(name,args.path)
            }
            content = renderTemplate(args, args.template)
        } else if (args.copy) {
            Files.copy(getTemplatePath(args.path + "/" + name), new File(parent.absolutePath+"\\"+name).toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        if (!file) {
            file = gtFile(name, null)
        }
        if (content) {
            if (args.append) {
                file.append(content)
            } else {
                file.text = content
            }
        }
    }



    /**
     * Render the template at the given path with the provided parameters. An attempt will be made to load the template path
     * as an absolute path, then as a relative path, then lastly from the classpath.
     *
     * @param params
     * @param template the template path
     * @return the rendered template
     */
    String renderTemplate(Map params = [:], String template) {
        def tLoc = new File(template)
        if (!tLoc.exists()) { // check given path
            // Couldn't find file at given path, trying relative path.
            def rTemplate = template
            if (rTemplate.startsWith('/')) {
                rTemplate = rTemplate - '/'
            }
            tLoc = new File(System.getProperty('user.dir'), rTemplate)
            if (!tLoc.exists()) { // check relative path from current working dir.
                tLoc = getClass().getResource(template) // last ditch, use classpath.
            }
        }
        def tReader = tLoc?.newReader()
        if (tReader) {
            return new GStringTemplateEngine().createTemplate(tReader)?.make(params)?.toString()
        }
        throw new RuntimeException("Could not locate template: ${template}")
    }

    InputStream getTemplatePath(String template) {
        File tLoc = new File(template)
        if (!tLoc.exists()) {
            def rTemplate = template
            if (rTemplate.startsWith('/')) {
                rTemplate = rTemplate - '/'
            }

            tLoc = new File(System.getProperty('user.dir'), rTemplate)
            if (!tLoc.exists()) {
                return getClass().getResourceAsStream(template)
            }
            return new FileInputStream(tLoc)
        }
        return new RuntimeException("Could not locate template: ${template}")
    }


    /**
     * Calls file([content: content], name)
     * @param name
     * @param content
     * @see #file(Map, String)
     */
    void file(String name, String content) {
        file([content: content], name)
    }

    /**
     * Starts the ProjectTemplate in the "user.dir" directory, unless the init.dir system property is specified.
     * @param closure
     */
    static void fromUserDir(Closure closure = {}) {
        new ProjectTemplate().directory(System.getProperty('init.dir', System.getProperty('user.dir')), closure)
    }

    /**
     * Starts the ProjectTemplate in the given path.
     * @param path String path to the root of the new project.
     * @param closure
     */
    static void fromRoot(String path, Closure closure = {}) {
        new ProjectTemplate().directory(path, closure)
    }

    /**
     * Starts the ProjectTemplate in the given file path.
     * @param pathFile File path to the root of the new project.
     * @param closure
     */
    static void fromRoot(File pathFile, Closure closure = {}) {
        new ProjectTemplate().directory(pathFile.path, closure)
    }

    /**
     * Handles creation of files or directories without the need to specify directly.
     * @param name
     * @param args
     * @return
     */
    def methodMissing(String name, def args) {
        if (args) {
            def arg = args[0]
            if (arg instanceof Closure) {
                directory(name, arg)
            } else if (arg instanceof Map) {
                file(arg, name)// name 是要调用的方法
            } else if (arg instanceof String || arg instanceof GString) {
                file([content: arg], name)
            } else {
                println "Couldn't figure out what to do. name: ${name}, arg: ${arg}, type: ${arg.getClass()}"
            }
        }
    }

      static getClassParts( final String fullClassName ){
        def classParts = fullClassName.split(/\./) as List
        [
                className: classParts.removeLast(),
                classPackagePath: classParts.join(File.separator),
                classPackage: classParts.join('.')
        ]
    }
      static getPackageParts( final String fullPackageName ){
        def classParts = fullPackageName.split(/\./) as List
        [
                classPackagePath: classParts.join(File.separator),
                classPackage: classParts.join('.')
        ]
    }


    /**
     * Finds the path to the main java source directory.
     *
     * @param project The project.
     * @return The path to the main java source directory.
     */
     static File findSrcDir(Project project, final String newProjectPath) {
        def mainDir = new File(newProjectPath).listFiles({ it, name -> name.contains("src") } as FilenameFilter).last()
        return mainDir
    }



}