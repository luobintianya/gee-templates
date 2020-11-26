package templates

import org.gradle.api.Plugin
import org.gradle.api.Project
import templates.tasks.eureka.CreateEurekaProjectTask
import templates.tasks.eureka.ExportEurekaTemplatesTask
import templates.tasks.zuul.CreateZuulProjectTask
import templates.tasks.zuul.ExportZuulTemplatesTask

/**
 * spring-cloud zuul server generation
 *
 */

class ZuulTemplatesPlugin implements Plugin<Project> {


	void apply(Project project) {
		project.task 'createZuulProjectTask', type: CreateZuulProjectTask

        project.task 'exportZuulTemplatesTask', type: ExportZuulTemplatesTask
	}
}