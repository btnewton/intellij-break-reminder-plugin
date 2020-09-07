package com.github.btnewton.breakreminder.states

import com.github.btnewton.breakreminder.services.AppSettingsState
import com.github.btnewton.breakreminder.services.WorkSessionService
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import java.time.Duration
import javax.swing.SwingUtilities

sealed class State(val name: String) {
    abstract fun handleWorkDone(): State

    open fun tick(): State = this
}

object WaitingState : State("Waiting") {
    override fun handleWorkDone(): State = WorkingState()
}

class WorkingState : State("Working") {
    private val timer = LazyTimer()
    private val inactivityTimer = LazyTimer()

    override fun tick(): State {
        if (!timer.isRunning()) {
            val settings = service<AppSettingsState>()
            timer.start(settings.workSessionTime())
            SwingUtilities.invokeLater {
                WorkSessionService.notify("Work session started.")
            }
        }

        if (!inactivityTimer.isRunning())
            resetInactivityTimer()

        return if (inactivityTimer.isRunning() && inactivityTimer.isDone())
            WaitingState
        else
            this
    }

    private fun resetInactivityTimer() {
        val settings = service<AppSettingsState>()
        inactivityTimer.start(settings.inactivityTimeout())
    }

    fun extend(dur: Duration) = timer.extendTime(dur)

    fun getMinutesWorked() = timer.minutes()

    override fun handleWorkDone(): State {
        resetInactivityTimer()
        return if (timer.isDone()) WorkDoneDecision(this) else this
    }
}

class WorkDoneDecision(private val currentWork: WorkingState) : State("Work Done Decision") {
    @Volatile
    private var exitCode: Int? = null

    private var showDialogFlag = true


    override fun handleWorkDone(): State = this

    override fun tick(): State {
        if (showDialogFlag) {
            showDialogFlag = false
            SwingUtilities.invokeLater {
                val dialog = TakeABreakDialog(currentWork)
                dialog.showAndGet()
                exitCode = dialog.exitCode
            }
        }

        return when (exitCode) {
            null -> this
            DialogWrapper.OK_EXIT_CODE ->BreakState()
            else -> {
                val settings = service<AppSettingsState>()
                SwingUtilities.invokeLater {
                    WorkSessionService.notify("You have ${settings.extensionTimeMinutes} minutes to wrap up")
                }
                currentWork.extend(settings.getExtensionTime())
                currentWork
            }
        }
    }
}

class BreakState() : State("Break") {
    private val gracePeriodTimer = LazyTimer()
    private val breakTimer = LazyTimer()

    override fun handleWorkDone(): State {
        if (gracePeriodTimer.isRunning() && gracePeriodTimer.isDone()) {
            SwingUtilities.invokeLater {
                WorkSessionService.notify("Oi! Take your break! 🚨")
            }
        }
        return this
    }

    override fun tick(): State {
        if (!gracePeriodTimer.isRunning()) {
            val settings = service<AppSettingsState>()
            gracePeriodTimer.start(settings.gracePeriod())
        }
        if (!breakTimer.isRunning()) {
            val settings = service<AppSettingsState>()
            breakTimer.start(settings.breakDuration())
        }
        return if (breakTimer.isDone()) WaitingState else this
    }
}