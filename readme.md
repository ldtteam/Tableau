<!--suppress HtmlDeprecatedAttribute -->
<h1 align="center" style="text-align: center;">
<img src="./logo-wide.png">
</h1>

<div align="center">

[![Build Status](https://img.shields.io/github/actions/workflow/status/ldtteam/Tableau/publish.yaml?branch=main&logo=github)][Build Workflow]
[![GitHub Releases](https://img.shields.io/github/v/tag/ldtteam/Tableau?sort=semver&display_name=tag&logo=github)][GitHub Releases]
[![GitHub Issues](https://img.shields.io/github/issues-raw/ldtteam/Tableau/bug?label=open%20bugs)][GitHub Issues]
[![Maven](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fldtteam.jfrog.io%2Fartifactory%2Ftableau-publishing%2Fcom%2Fldtteam%2Ftableau%2FTableau%2Fmaven-metadata.xml)][Maven]
</br>
[![Discord](https://img.shields.io/discord/449079260070674443?logo=discord&label=Discord&color=%235865F2)][Discord]

</div>

___

Tableau is a collection of Gradle plugins that provide a common set of functionality for Minecraft Modding projects.

Its primary goal is to simplify the setup and configuration of Minecraft Modding projects by providing a set of plugins that can be used to configure and manage the project's build process.
It does this in a declarative fashion. You tell Tableau what you want, and it takes care of the rest.

## Features
- Handling of core mod properties
- Interpolation of properties in resources
- Custom pre- and post-processing of changelogs (can be used in combination with GitHub workflows)
- Crowdin integration
- Automatic CurseForge file uploads
- Extraction of information from Git
- Support for JarJar and ShadowJar
- Support for configuring the Java SDK
- Support for injecting Jetbrains annotations
- Support for `.gradle` file handling in the `gradle` directory of a project
- Support for configuring Maven Publishing
  - Publishing to the LDTTeam Maven
  - Publishing to GitHub Releases and Packages
  - Publishing to a Local Directory
  - Extraction of information for the POM from the Git repository
  - Extraction of information for the POM from other Tableau modules
- Support for NeoGradle project management
  - Other Modding Frameworks to follow
  - Other Modding Platforms to follow
- Support for Parchment
- Support for direct source set management (no need for `sourceSets` block, or `dependencies` block)

## Website
TODO: Insert website with all documentation here.

## Getting Started
To get started there are two ways to apply the Tableau plugin to your project.
You can apply the bootstrap module, or apply the core Tableau module directly.

### Applying the Bootstrap Module
The bootstrap module is supposed to be released on Plugins portal, once it is released, you can apply it to your `settings` like this:
```groovy settings.gradle
plugins {
    id 'com.ldtteam.tableau' version '1.0.0'
}
```
or when using Kotlin DSL:
```kotlin settings.gradle.kts
plugins {
    id("com.ldtteam.tableau") version "1.0.0"
}
```

However, right now it is not released yet and you need to pull the bootstrap module from the Tableau Maven repository.
To do this, you need to add the following to your `settings.gradle` file:
```groovy settings.gradle
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url "https://ldtteam.jfrog.io/artifactory/tableau/"
        }
    }
}

plugins {
    id 'com.ldtteam.tableau' version '1.0.0'
}
```
or when using Kotlin DSL:
```kotlin settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://ldtteam.jfrog.io/artifactory/tableau/")
        }
    }
}

plugins {
    id("com.ldtteam.tableau") version "1.0.0"
}
```

The version above is just an example, you should replace it with the latest version available on the [Maven Repository][Maven].
Check the `bootstrap` module directory on our maven for the available versions.

### Configuring the basics
To get started you will need to tell Tableau about the mod in your project.
To do this, you need to add the following to your `build.gradle` file:
```groovy build.gradle
tableau {
    mod {
        modId = "examplemod"
        group = "com.example"
        minecraftVersion = "1.21.3"
        publisher = "LDTTeam"
        url = "https://github.com/someorg/examplemod"
    }
}
```
or when using Kotlin DSL:
```kotlin build.gradle.kts
tableau {
    mod {
        modId.set("examplemod")
        group.set("com.example")
        minecraftVersion.set("1.21.3")
        publisher.set("LDTTeam")
        url.set("https://github.com/someorg/examplemod")
    }
}
```

This will get your project started with the latest NeoForge version for that Minecraft version.
We will add support for other modding frameworks in the future.

If you want to configure the individual modules, please visit the [Tableau Website](#website) for more information.

## Contributing
If you want to contribute to Tableau, please read our [Contributing Guidelines](CONTRIBUTING.md) first.

## License
Tableau is licensed under the GPL-3.0 License. See the [LICENSE](LICENSE) file for more information.

[Build Workflow]: https://github.com/ldtteam/Tableau/actions/workflows/publish.yaml
[GitHub Releases]: https://github.com/ldtteam/Tableau/tags
[GitHub Issues]: https://github.com/ldtteam/Tableau/issues?q=is%3Aopen+is%3Aissue+label%3Abug
[Maven]: https://ldtteam.jfrog.io/ui/native/tableau-publishing/com/ldtteam/tableau/
[Discord]: https://discord.gg/Pd5vYh5K

