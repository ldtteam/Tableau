# Tableau Bootstrap Module

This is the bootstrap module of Tableau, published to the Gradle Plugin Portal. It provides essential functionality to register the Tableau module Maven repository, apply the Tableau default module, configure defaults for all modules, and prepare all projects to use Tableau.

## Applying the Plugin

To apply the Tableau Bootstrap plugin to your Gradle project, you need to apply it to a `settings` object, not a `project`. This is not a normal practice, so please be aware of this requirement.


### Using the `plugins {}` block

You can apply the plugin directly from the Gradle Plugin Portal using the `plugins {}` block in your `settings.gradle` or `settings.gradle.kts` file.

#### Using Groovy DSL

```groovy
plugins {
    id 'com.ldtteam.tableau.bootstrap' version '1.0.0'
}
```

#### Using Kotlin DSL

```kotlin
plugins {
    id("com.ldtteam.tableau.bootstrap") version "1.0.0"
}
```

### From the Tableau Maven Repository

Alternatively, you can apply the plugin from the Tableau Maven repository.
Register the Tableau Maven repository in the `pluginManagement` block and apply the plugin using the `apply plugin` statement.

#### Using Groovy DSL

Add the following to your `settings.gradle` file:

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url "https://ldtteam.jfrog.io/artifactory/tableau/"
        }
    }
    plugins {
        id 'com.ldtteam.tableau.bootstrap' version '1.0.0'
    }
}

apply plugin: 'com.ldtteam.tableau.bootstrap'
```

#### Using Kotlin DSL

Add the following to your `settings.gradle.kts` file:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://ldtteam.jfrog.io/artifactory/tableau/")
        }
    }
    plugins {
        id("com.ldtteam.tableau.bootstrap") version "1.0.0"
    }
}

apply(plugin = "com.ldtteam.tableau.bootstrap")
```


## Functionality

The Tableau Bootstrap plugin provides the following functionality:

- Registers the Tableau module Maven repository.
- Applies the Tableau default module.
- Configures the defaults for all modules and prepares all projects to use Tableau.

## Publishing

This plugin is published to the Gradle Plugin Portal. You can find it [here](https://plugins.gradle.org/plugin/com.ldtteam.tableau.bootstrap).

## License

This project is licensed under the GNU General Public License v3.0.