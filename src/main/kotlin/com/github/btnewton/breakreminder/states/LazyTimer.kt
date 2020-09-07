package com.github.btnewton.breakreminder.states

import java.time.Duration
import java.time.LocalDateTime

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
