package templates

import org.gradle.api.Plugin
import org.gradle.api.Project
import templates.tasks.config.CreateConfigProjectTask
import templates.tasks.config.ExportConfigTemplatesTask
import templates.tasks.eureka.CreateEurekaProjectTask
import templates.tasks.eureka.ExportEurekaTemplatesTask

/**
 * spring-cloud config server generation
 *
 */

class ConfigTemplatesPlugin implements Plugin<Project> {

	void apply(Project project) {
		project.task 'createConfigProject', type: CreateConfigProjectTask

        project.task 'exportConfigTemplates', type: ExportConfigTemplatesTask
	}
}