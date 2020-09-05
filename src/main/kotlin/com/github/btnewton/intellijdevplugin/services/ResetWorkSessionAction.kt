package com.github.btnewton.intellijdevplugin.services

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service


/**
 * @author brandt - 9/4/20
 */
class ResetWorkSessionAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val applicationService = service<WorkSessionService>()
        applicationService.resetWorkTimer()
    }
}
