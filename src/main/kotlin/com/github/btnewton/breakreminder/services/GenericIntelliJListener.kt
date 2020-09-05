package com.github.btnewton.breakreminder.services

import com.intellij.codeInsight.completion.CompletionPhaseListener
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.changes.committed.VcsConfigurationChangeListener
import com.intellij.openapi.vcs.update.UpdatedFilesListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.problems.ProblemListener
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskManager
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManagerListener
import org.jetbrains.annotations.NotNull


// nieve way of detecting any interation with IntelliJ
class GenericIntelliJListener : BulkFileListener, ToolWindowManagerListener,
CommandListener,
        RunManagerListener,
        CompletionPhaseListener,
        UpdatedFilesListener,
        ProblemListener,
        XDebuggerManagerListener,
        ProjectTaskListener,
        VcsConfigurationChangeListener

{
    override fun after(@NotNull events: List<VFileEvent?>) = workDone("VFS Event")

    override fun toolWindowRegistered(id: String) = workDone("Tool Window Event")

    override fun toolWindowUnregistered(id: String, toolWindow: ToolWindow) = workDone("Tool Window Event")

    override fun completionPhaseChanged(isCompletionRunning: Boolean) = workDone("Completion Event")

    override fun commandStarted(event: CommandEvent) = workDone("Command Event")

    override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings) = workDone("Run Config Event")

    override fun consume(t: MutableSet<String>?) = workDone("Updated files")

    override fun problemsAppeared(file: VirtualFile) = workDone("Problem Appeared")
    override fun problemsChanged(file: VirtualFile) = workDone("Problem Changed")
    override fun problemsDisappeared(file: VirtualFile) = workDone("Problem Disappeared")

    override fun beforeRunTasksChanged() = workDone("beforeRunTasksChanged")

    override fun processStarted(debugProcess: XDebugProcess) = workDone("processStarted")

    override fun runConfigurationChanged(settings: RunnerAndConfigurationSettings, existingId: String?) = workDone("runConfigurationChanged")

    override fun runConfigurationRemoved(settings: RunnerAndConfigurationSettings) = workDone("runConfigurationRemoved")

    override fun beginUpdate() = workDone("beginUpdate")

    override fun runConfigurationSelected(settings: RunnerAndConfigurationSettings?) = workDone("runConfigurationSelected")

    override fun runConfigurationSelected() = workDone("runConfigurationSelected")

    override fun currentSessionChanged(previousSession: XDebugSession?, currentSession: XDebugSession?) = workDone("currentSessionChanged")

    override fun processStopped(debugProcess: XDebugProcess) = workDone("processStopped")

    override fun finished(result: ProjectTaskManager.Result) = workDone("ProjectTaskManagerfinished")

    private fun workDone(eventSourceName: String) {
        println("Work Done: $eventSourceName")
        val applicationService = service<WorkSessionService>()
        applicationService.workDone()
    }
}