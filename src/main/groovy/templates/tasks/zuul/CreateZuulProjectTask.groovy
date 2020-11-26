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

package templates.tasks.zuul


import org.gradle.api.tasks.TaskAction
import templates.ProjectTemplate
import templates.TemplatesPlugin
import templates.tasks.java.AbstractJavaProjectTask

/**
 * Task that creates a new spring-cloud config  project in a specified directory.
 */
class CreateZuulProjectTask extends AbstractJavaProjectTask {

    CreateZuulProjectTask() {
        super(
                'Creates a new Gradle spring-cloud config project in a new directory named after your project.'
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
                'build.gradle' template: '/templates/config/build.gradle.tmpl', projectGroup: projectGroup, version: projectVersion
                'gradle.properties' content: "version=${projectVersion}", append: true
            }

            def mainSrcDir = null
            def testSrcDir = null
            def mainResourceDir = null
            try {
                // get main java dir, and check to see if Java plugin is installed.

                String createProjectPath = project.projectDir.path + "\\" + projectPath
                def srcDir = ProjectTemplate.findSrcDir(project, createProjectPath)
                mainSrcDir = srcDir.listFiles({ it, name -> name.contains("main") } as FilenameFilter)?.last().listFiles( {it,name -> name.contains("java")} as FilenameFilter).last().absolutePath
                testSrcDir = srcDir.listFiles({ it, name -> name.contains("test") } as FilenameFilter)?.last().listFiles({ it, name -> name.contains("java") } as FilenameFilter).last().absolutePath
                mainSrcDir=mainSrcDir.minus(project.projectDir.path)
                testSrcDir= testSrcDir.minus(project.projectDir.path)
                mainResourceDir = srcDir.listFiles({ it, name -> name.contains("main") } as FilenameFilter)?.last().listFiles({ it, name -> name.contains("resources") } as FilenameFilter)?.last().absolutePath;
                mainResourceDir= mainResourceDir.minus(project.projectDir.path)
            } catch (Exception e) {
                e.printStackTrace()
                throw new IllegalStateException('It seems that the Java plugin is not installed, I cannot determine the main java source directory.', e)
            }

            String packageName = (project.properties[NEW_PACKAGE_NAME] ?: TemplatesPlugin.prompt('Package name (com.example)')) + ".config"
            packageName= packageName.replaceFirst("\r","").replaceFirst("\t","")
            if (packageName) {
                def classParts = ProjectTemplate.getPackageParts(packageName)
                ProjectTemplate.fromUserDir {
                    "${mainSrcDir}" {
                        "${classParts.classPackagePath}" {
                            "GatewayApplication.java" template: '/templates/zuul/gateway/GatewayApplication-class.tmpl', classPackage: classParts.classPackage, className: "GatewayApplication"
                            "TokenFilter.java" template: '/templates/zuul/gateway/filter/TokenFilter-class.tmpl', classPackage: classParts.classPackage, className: "TokenFilter",path:"/filter"
                            "RateLimitFilter.java" template: '/templates/zuul/gateway/filter/RateLimitFilter-class.tmpl', classPackage: classParts.classPackage, className: "RateLimitFilter",path:"/filter"

                        }
                    }
                    "${testSrcDir}" {
                        "${classParts.classPackagePath}" {
                            "CloudtestApplicationTests.java" template: '/templates/common/CloudtestApplicationTests-class.tmpl', classPackage: classParts.classPackage, className: "CloudtestApplicationTests"

                        }
                    }
                    "${mainResourceDir}" {
                        "application.yml" copy: true, path: "/templates/zuul"
                        "banner.txt" copy: true, path: "/templates/zuul"
                        "logback.xml" copy: true, path: "/templates/zuul"
                        "favicon.ico" copy: true, path: "/templates/zuul"
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


}
