package com.github.btnewton.breakreminder.services

import com.github.btnewton.breakreminder.states.State
import com.github.btnewton.breakreminder.states.WaitingState
import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service

@Service
class WorkSessionService : Disposable {
    private val loop: ServiceLoop = ServiceLoop()

    companion object {
        private val NOTIFICATION_GROUP = NotificationGroup("Break Time Group", NotificationDisplayType.BALLOON, true)

        fun notify(content: String): Notification {
            val notification: Notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION)
            notification.notify(null)
            return notification
        }
    }

    init {
        val loopThread = Thread(loop)
        loopThread.start()
    }

    fun workDone() {
        println("Work detected")
        loop.workDone()
    }

    fun resetWorkTimer() {
        loop.resetState()
    }

    override fun dispose() {
        loop.stop()
    }

    private class ServiceLoop : Runnable {

        private var state: State = WaitingState

        @Volatile
        private var workDetectedFlag = false

        @Volatile
        private var resetStateFlag = false

        @Volatile
        private var stopFlag = false

        override fun run() {
            while (!stopFlag) {
                val nextState: State = when {
                    resetStateFlag -> {
                        workDetectedFlag = false
                        resetStateFlag = false
                        WaitingState
                    }
                    workDetectedFlag -> {
                        workDetectedFlag = false
                        state.handleWorkDone()
                    }
                    else -> {
                        state.tick()
                    }
                }

                if (nextState != state) println("State changed from ${state.name} to ${nextState.name}")

                state = nextState

                Thread.sleep(1000)
            }
        }

        fun workDone() {
            workDetectedFlag = true
        }

        fun resetState() {
            resetStateFlag = true
        }

        fun stop() {
            stopFlag = true
        }
    }
}
