package templates

import org.gradle.api.Plugin
import org.gradle.api.Project
import templates.tasks.eureka.CreateEurekaProjectTask
import templates.tasks.eureka.ExportEurekaTemplatesTask

/**
 * spring-cloud eureka server generation
 *
 */

class EurekaTemplatesPlugin implements Plugin<Project> {
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

	void apply(Project project) {
		project.task 'createEurekaProject', type: CreateEurekaProjectTask

        project.task 'exportEurekaTemplates', type: ExportEurekaTemplatesTask
	}
}