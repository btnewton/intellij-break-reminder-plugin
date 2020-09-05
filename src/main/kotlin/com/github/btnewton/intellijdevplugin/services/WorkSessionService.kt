package com.github.btnewton.intellijdevplugin.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import java.time.Duration
import java.time.LocalDateTime
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

@Service
class WorkSessionService {
    private val NOTIFICATION_GROUP = NotificationGroup("Break Time Group", NotificationDisplayType.BALLOON, true)

    companion object {
        val workTimer = LazyTimer()
        val gracePeriodTimer = LazyTimer()
        val breakTimer = LazyTimer()
    }

    fun workDone() {
        println("Work detected")
        if (gracePeriodTimer.isRunning() && !gracePeriodTimer.isDone()){
            println("Grace period so not bothering user")
        }
        else if (breakTimer.isRunning() && !breakTimer.isDone()) {
            println("Work done on break!?")
            notify("Oi! Take your break! 🚨")
        } else if (!workTimer.isRunning()) {
            println("Starting work session...")
            resetWorkTimer()
        } else if (workTimer.isDone()) {
            println("Work session done!")
            tellUserWorkSessionDone()
        }
    }

    fun endBreakSession() {
        breakTimer.cancel()
        gracePeriodTimer.cancel()
    }

    fun resetWorkTimer() {
        endBreakSession()
        notify("Work session started.")
        val settings = service<AppSettingsState>()
        workTimer.start(Duration.ofMinutes(settings.workSessionTimeMinutes.toLong()))
    }

    fun tellUserWorkSessionDone() {
        val d = TakeABreakDialog()
        if (d.showAndGet()) {
            if (d.exitCode == 5) {
                val settings = service<AppSettingsState>()
                notify("You have ${settings.extensionTimeMinutes} minutes to wrap up")
                workTimer.extendTime(Duration.ofMinutes(settings.extensionTimeMinutes.toLong()))
            } else {
                workTimer.cancel()
                val settings = service<AppSettingsState>()
                gracePeriodTimer.start(Duration.ofSeconds(settings.gracePeriodSeconds.toLong()))
                breakTimer.start(Duration.ofMinutes(settings.breakDurationMinutes.toLong()))
            }
        }
    }

    private fun notify(content: String): Notification {
        val notification: Notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION)
        notification.notify(null)
        return notification
    }

    class TakeABreakDialog : DialogWrapper(true) {
        override fun createCenterPanel(): JComponent? {
            val dialogPanel = JPanel(BorderLayout())
            val label = JLabel("You've been working for ${workTimer.minutes()} minutes. Take a break!")
            label.setPreferredSize(Dimension(100, 100))
            dialogPanel.add(label, BorderLayout.CENTER)
            return dialogPanel
        }

        override fun createActions(): Array<Action> {
            val settings = service<AppSettingsState>()
            val keepWorking = DialogWrapperExitAction("${settings.extensionTimeMinutes} more minutes", 5)
            val stretch = DialogWrapperExitAction("Stretch time!", 0)
            val coffee = DialogWrapperExitAction("Coffee Break!", 0)
            val walk = DialogWrapperExitAction("Goin' for a walk!", 0)
            walk.putValue(DEFAULT_ACTION, true)
            return arrayOf(
                    keepWorking,
                    stretch,
                    coffee,
                    walk,
            )
        }

        init {
            init()
            title = "Take a Break!"
        }
    }
}

class LazyTimer {
    private var doneTimeMs: Long? = null
    private var startTime: LocalDateTime? = null

    fun start(minutes: Duration) {
        startTime = LocalDateTime.now()
        extendTime(minutes)
    }

    fun extendTime(minutes: Duration) {
        doneTimeMs = System.currentTimeMillis() + minutes.toMillis()
    }

    fun minutes() = Duration.between(startTime, LocalDateTime.now())

    fun isDone() = doneTimeMs == null || doneTimeMs!! < System.currentTimeMillis()

    fun isRunning() = doneTimeMs != null

    fun cancel() {
        doneTimeMs = null
        startTime = null
    }
}

