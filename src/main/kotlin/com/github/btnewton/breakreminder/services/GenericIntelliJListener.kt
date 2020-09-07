package com.github.btnewton.breakreminder.services

import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.problems.ProblemListener
import com.intellij.task.ProjectTaskListener
import org.jetbrains.annotations.NotNull

// naive way of detecting any interactions with IntelliJ
class GenericIntelliJListener :
    BulkFileListener,
    RunManagerListener,
    ProblemListener,
    ProjectTaskListener,
    VcsConfigurationChangeListener {
    override fun after(@NotNull events: List<VFileEvent?>) = workDone("VFS Event")

    override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings) = workDone("Run Config Event")

    override fun problemsAppeared(file: VirtualFile) = workDone("Problem Appeared")

    override fun problemsChanged(file: VirtualFile) = workDone("Problem Changed")

    override fun problemsDisappeared(file: VirtualFile) = workDone("Problem Disappeared")

    override fun beforeRunTasksChanged() = workDone("beforeRunTasksChanged")

    override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings, existingId: String?) = workDone("runConfigurationChanged")

    override fun beginUpdate() = workDone("beginUpdate")

    override fun runConfigurationSelected(settings: RunnerAndConfigurationSettings?) = workDone("runConfigurationSelected")

    private fun workDone(eventSourceName: String) {
        println("Work Done: $eventSourceName")
        val applicationService = service<WorkSessionService>()
        applicationService.workDone()
    }
}
