import xyz.jpenilla.runpaper.task.RunServerTask

plugins {
    id("base")
    id("net.kyori.indra.git")
    id("xyz.jpenilla.run-paper")
}

runPaper {
    disablePluginJarDetection()
}

val runModules = (findProperty("runModules") as String?)
    ?.trim()?.split(",") ?: emptySet()

tasks {
    runServer {
        // Enable modules for this task using the runModules property.
        // Ex: './gradlew :runServer -PrunModules=chat,spawn'
        minecraftVersion(RUN_PAPER_MINECRAFT_VERSION)
    }
    register<RunServerTask>("runAll") {
        group = "essentials"
        description = "Run a test server with all EssentialsX modules."
        minecraftVersion(RUN_PAPER_MINECRAFT_VERSION)
    }
    register<RunServerTask>("runOld") {
        group = "essentials"
        description = "Run a test server on an older Paper version."
        minecraftVersion(RUN_PAPER_MINECRAFT_VERSION_OLD)
        runDirectory.set(project.layout.projectDirectory.dir("runOld"))
    }
    named<Delete>("clean") {
        delete(file("jars"))
    }
}

subprojects {
    afterEvaluate {
        val moduleExt = extensions.findByType<EssentialsModuleExtension>() ?: return@afterEvaluate
        rootProject.tasks.named<RunServerTask>("runAll").configure {
            pluginJars.from(moduleExt.archiveFile)
        }
        val genericTasks = listOf(
            rootProject.tasks.runServer,
            rootProject.tasks.named<RunServerTask>("runOld")
        )
        if (name == "EssentialsX") {
            genericTasks.forEach {
                it.configure {
                    pluginJars.from(moduleExt.archiveFile)
                }
            }
            return@afterEvaluate
        }
        for (module in runModules) {
            if (name.contains(module, ignoreCase = true)) {
                genericTasks.forEach {
                    it.configure {
                        pluginJars.from(moduleExt.archiveFile)
                    }
                }
            }
        }
    }
}
