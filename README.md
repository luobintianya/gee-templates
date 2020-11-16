# GEE Templates Plugin

## Introduction

The Gee Templates plugin helps you get started using Gradle by providing convenient tasks for creating new projects that work with the Gradle build system.
Eg. To create a new spring cloud eureka  project you can run:

```gradle createEurekaProject```

Which will prompt you for the name of your new project and then create a new directory with it. It will also create a standard directory structure in your
project's directory that works with Gradle's default configurations.

 
## Installation
way 1:
The standard way to install this plugin is by adding the following to your build.gradle file:

```groovy
buildscript {
    repositories {
        maven {
			url 'http://dl.bintray.com/cjstehno/public'
		}
    }
    dependencies {
        classpath 'gradle-templates:gradle-templates:1.5'
    }
}

apply plugin:'templates'
```
way 2:
in user home ~/.gradle/init.gradle  file put content as below:
```
gradle.beforeProject { prj ->
   prj.apply from: 'E:\\gee-templates\\installation\\apply.groovy'
   //prj.apply from: 'http://www.tellurianring.com/projects/gradle-plugins/gradle-templates/apply.groovy'
}
```
## Usage

Run the `gradle tasks` command to see a list of "create", "export" tasks provided by the default plugin templates.
 

## Details

* Version: 1.6
* Project Site: [https://github.com/luobintianya/gee-templates](https://github.com/luobintianya/gee-templates) )

