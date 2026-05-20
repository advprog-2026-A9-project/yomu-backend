pluginManagement {
	repositories {
		maven {
			url = uri("https://repo1.maven.org/maven2/")
		}
		gradlePluginPortal()
		maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
	}
}

rootProject.name = "yomu-backend"
