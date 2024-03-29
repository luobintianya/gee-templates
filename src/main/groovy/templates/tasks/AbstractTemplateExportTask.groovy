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

package templates.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import templates.TemplatesPlugin

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Abstract base task for template exporters.
 */
abstract class AbstractTemplateExportTask extends DefaultTask {

    private final templatePaths = []

    /**
     * Creates a new template export task with the given properties and templates.
     *
     * @param name the task name
     * @param description the task description
     * @param paths the template paths
     */
    protected AbstractTemplateExportTask(final String description, final paths = []) {

        this.group = TemplatesPlugin.group
        this.description = description
        this.templatePaths = paths
    }

    /**
     * Exports the configured templates.
     */
    @TaskAction
    void export() {
        exportTemplates templatePaths
    }

    private void exportTemplates(def templates = []) {
        templates.ProjectTemplate.fromUserDir {
            templates.each {
                template ->
                    List<String> files = new ArrayList<>()
                    getFileFromFolder(template, files)
                    files.each { file ->
                        def tStream = getClass().getResourceAsStream(file)
                        "$file" tStream.text
                    }


            }
        }
    }


    void getFileFromFolder(String template, List<String> files) {

        String rootPath = getClass().getResource(template).toString()
        String rmPath = rootPath.replace(template, "").replace("file:/","").replaceAll("/","\\\\")

        if (rootPath.indexOf("file:") >= 0) {
            File file = new File(rootPath.substring("file:/".length(), rootPath.length()))
            if (file.isDirectory()) {
                if (file.listFiles().length > 0) {
                    file.listFiles().each {
                        getFileFromFolder(it.absolutePath.replace(rmPath,"").replaceAll("\\\\","/"), files)
                    }
                }
                return
            } else {
                files.add(file.absolutePath.replace(rmPath,"").replaceAll("\\\\","/"))
            }
            return
        }
        String jarPath = rootPath.substring(0, rootPath.indexOf("!/") + 2)
        URL jarURL = new URL(jarPath)
        JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection()
        JarFile jarFile = jarCon.getJarFile()
        Enumeration<JarEntry> jarEntrys = jarFile.entries()
        while (jarEntrys.hasMoreElements()) {
            JarEntry entry = jarEntrys.nextElement()
            String name = entry.getName()
            if (name.indexOf(template.replaceFirst("/", "")) >= 0) {
                if (!entry.isDirectory()) {
                    files.add("/" + name)
                }
            }
        }
    }

}




