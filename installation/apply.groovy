
buildscript {

	repositories {
		url:mavenLocal().getUrl()
	}

	dependencies {
		classpath 'gee-templates:gee-templates:1.6'
	}

}
if (!project.plugins.findPlugin(templates.TemplatesPlugin)) {
	project.apply(plugin: templates.TemplatesPlugin)
}