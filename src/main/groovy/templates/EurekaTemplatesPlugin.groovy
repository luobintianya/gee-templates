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


	void apply(Project project) {
		project.task 'createEurekaProject', type: CreateEurekaProjectTask

        project.task 'exportEurekaTemplates', type: ExportEurekaTemplatesTask
	}
}