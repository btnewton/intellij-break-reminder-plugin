package com.github.btnewton.intellijdevplugin.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
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
        val workTimer = LazyTimer(Duration.ofMinutes(45))
        val extensionTime = Duration.ofMinutes(5)
        // let user save work and stuff
        val gracePeriodTimer = LazyTimer(Duration.ofSeconds(30))
        val breakTimer = LazyTimer(Duration.ofMinutes(5))
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
        workTimer.start()
    }

    fun tellUserWorkSessionDone() {
        val d = TakeABreakDialog()
        if (d.showAndGet()) {
            if (d.exitCode == 5) {
                notify("You have ${extensionTime.toMinutes()} minutes to wrap up")
                workTimer.extendTime(extensionTime)
            } else {
                workTimer.cancel()
                gracePeriodTimer.start()
                breakTimer.start()
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
            val keepWorking = DialogWrapperExitAction("5 more minutes", 5)
            val stretch = DialogWrapperExitAction("Stretch time!", 0)
            val coffee = DialogWrapperExitAction("Coffee Break!", 0)
            val stroll = DialogWrapperExitAction("Goin' for a stroll!", 0)
            stroll.putValue(DEFAULT_ACTION, true)
            return arrayOf(
                    keepWorking,
                    stretch,
                    coffee,
                    stroll,
            )
        }

        init {
            init()
            title = "Take a Break!"
        }
    }
}

class LazyTimer(val defaultTime: Duration) {
    private var doneTimeMs: Long? = null
    private var startTime: LocalDateTime? = null

    fun start(minutes: Duration = defaultTime) {
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

