package com.github.btnewton.intellijdevplugin.services

import com.intellij.openapi.project.Project
import com.github.btnewton.intellijdevplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
