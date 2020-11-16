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

package templates.tasks.eureka

import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import templates.EurekaTemplatesPlugin
import templates.JavaTemplatesPlugin
import templates.ProjectTemplate
import templates.TemplatesPlugin
import templates.tasks.java.AbstractJavaProjectTask

/**
 * Task that creates a new spring-cloud eureka  project in a specified directory.
 */
class CreateEurekaProjectTask extends AbstractJavaProjectTask {

    CreateEurekaProjectTask() {
        super(
                'Creates a new Gradle spring-cloud eureka project in a new directory named after your project.'
        )
    }

    @TaskAction
    void create() {
        String projectName = projectName()
        if (projectName) {
            String projectPath = projectPath(projectName)

            createBase projectPath
            String projectVersion = projectVersion()
            String projectGroup = projectGroup(projectName)

            ProjectTemplate.fromRoot(projectPath) {
                'build.gradle' template: '/templates/eureka/build.gradle.tmpl', projectGroup: projectGroup, version: projectVersion
                'gradle.properties' content: "version=${projectVersion}", append: true
            }

            def mainSrcDir = null
            def mainResourceDir = null
            try {
                // get main java dir, and check to see if Java plugin is installed.

                String createProjectPath = project.projectDir.path + "\\" + projectPath
                def mainDir = findMainDir(project, createProjectPath)
                mainSrcDir = mainDir.listFiles({ it, name -> name.contains("java") } as FilenameFilter)?.last().absolutePath;
                mainSrcDir=mainSrcDir.minus(project.projectDir.path);
                mainResourceDir = mainDir.listFiles({ it, name -> name.contains("resources") } as FilenameFilter)?.last().absolutePath;
                mainResourceDir= mainResourceDir.minus(project.projectDir.path);
            } catch (Exception e) {
                e.printStackTrace()
                throw new IllegalStateException('It seems that the Java plugin is not installed, I cannot determine the main java source directory.', e)
            }

            String packageName = (project.properties[NEW_PACKAGE_NAME] ?: TemplatesPlugin.prompt('Package name (com.example)')) + ".eureka"
            packageName= packageName.replaceFirst("\r","").replaceFirst("\t","")
            if (packageName) {
                def classParts = EurekaTemplatesPlugin.getPackageParts(packageName)
                ProjectTemplate.fromUserDir {
                    "${mainSrcDir}" {
                        "${classParts.classPackagePath}" {
                            "EurekaApplication.java" template: '/templates/eureka/EurekaApplication-class.tmpl', classPackage: classParts.classPackage, className: "EurekaApplication"

                        }
                    }
                    "${mainResourceDir}" {
                        "application.yaml" copy: true, path: "/templates/eureka"
                        "banner.txt" copy: true, path: "/templates/eureka"
                        "logback.xml" copy: true, path: "/templates/eureka"
                        "favicon.ico" copy: true, path: "/templates/eureka"
                    }
                }
            } else {
                // TODO: should be an error
                println 'No class name provided.'
            }

        } else {
            // FIXME: should be an error
            println 'No project name provided.'
        }
    }


    /**
     * Finds the path to the main java source directory.
     *
     * @param project The project.
     * @return The path to the main java source directory.
     */
    private static File findMainDir(Project project, final String newProjectPath) {

        def mainDir = new File(newProjectPath).listFiles({ it, name -> name.contains("src") } as FilenameFilter).last().listFiles({ it, name -> name.contains("main") } as FilenameFilter)
        mainDir = mainDir?.first()
        return mainDir
    }
}
