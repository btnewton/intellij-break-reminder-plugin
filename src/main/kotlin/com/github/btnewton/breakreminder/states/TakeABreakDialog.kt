package com.github.btnewton.breakreminder.states

import com.github.btnewton.breakreminder.services.AppSettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class TakeABreakDialog(private val workingState: WorkingState) : DialogWrapper(true) {

    companion object {
        val EXTENSION_TIME_EXIT_CODE = 5
    }

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel("You've been working for ${workingState.getMinutesWorked()} minutes. Take a break!")
        label.setPreferredSize(Dimension(100, 100))
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }

    override fun createActions(): Array<Action> {
        val settings = service<AppSettingsState>()
        val keepWorking = DialogWrapperExitAction("${settings.extensionTimeMinutes} more minutes", 5)
        val breakTime = DialogWrapperExitAction("Break time!", 0)
        breakTime.putValue(DEFAULT_ACTION, true)
        return arrayOf(
                keepWorking,
                breakTime
        )
    }

    init {
        init()
        title = "Take a Break!"
    }
}