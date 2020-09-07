package com.github.btnewton.breakreminder.services

import com.intellij.openapi.components.*
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.Nullable
import java.time.Duration
import javax.swing.JComponent
import javax.swing.JPanel

@State(name = "com.github.btnewton.intellijdevplugin.services.AppSettingsState", storages = [Storage("SdkSettingsPlugin.xml")])
class AppSettingsState : PersistentStateComponent<AppSettingsState> {
    var breakDurationMinutes = 15
    var workSessionTimeMinutes = 45
    var extensionTimeMinutes = 5
    var gracePeriodSeconds = 30
    var inactivityTimeoutMinutes = 60

    @Nullable
    override fun getState(): AppSettingsState {
        return this
    }

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun breakDuration() = Duration.ofMinutes(breakDurationMinutes.toLong())
    fun workSessionTime() = Duration.ofMinutes(workSessionTimeMinutes.toLong())
    fun getExtensionTime(): Duration = Duration.ofMinutes(extensionTimeMinutes.toLong())
    fun gracePeriod() = Duration.ofSeconds(gracePeriodSeconds.toLong())
    fun inactivityTimeout() = Duration.ofMinutes(inactivityTimeoutMinutes.toLong())
}

/**
 * Supports creating and managing a JPanel for the Settings Dialog.
 */
class AppSettingsComponent {
    val panel: JPanel
    private val _breakDurationMinutesText = JBTextField()
    private val _workSessionTimeMinutesText = JBTextField()
    private val _gracePeriodSecondsText = JBTextField()
    private val _extensionTimeMinutesText = JBTextField()

    val preferredFocusedComponent: JComponent
        get() = _breakDurationMinutesText

    var breakDurationMinutesText: String
        get() = _breakDurationMinutesText.text
        set(newText) {
            _breakDurationMinutesText.text = newText
        }
    var workSessionTimeMinutesText: String
        get() = _workSessionTimeMinutesText.text
        set(newText) {
            _workSessionTimeMinutesText.text = newText
        }
    var gracePeriodSecondsText: String
        get() = _gracePeriodSecondsText.text
        set(newText) {
            _gracePeriodSecondsText.text = newText
        }
    var extensionTimeMinutesText: String
        get() = _extensionTimeMinutesText.text
        set(newText) {
            _extensionTimeMinutesText.text = newText
        }

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Break Duration Minutes: "), _breakDurationMinutesText, 1, false)
            .addLabeledComponent(JBLabel("Work Duration Minutes: "), _workSessionTimeMinutesText, 1, false)
            .addLabeledComponent(JBLabel("Grace Period Seconds: "), _gracePeriodSecondsText, 1, false)
            .addLabeledComponent(JBLabel("Extension Minutes: "), _extensionTimeMinutesText, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}

/**
 * Provides controller functionality for application settings.
 */
class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String? {
        return "Work Session"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings = service<AppSettingsState>()
        return (mySettingsComponent!!.breakDurationMinutesText != settings.breakDurationMinutes.toString()) or
            (mySettingsComponent!!.workSessionTimeMinutesText != settings.workSessionTimeMinutes.toString()) or
            (mySettingsComponent!!.extensionTimeMinutesText != settings.extensionTimeMinutes.toString()) or
            (mySettingsComponent!!.gracePeriodSecondsText != settings.gracePeriodSeconds.toString())
    }

    override fun apply() {
        val settings = service<AppSettingsState>()
        settings.breakDurationMinutes = mySettingsComponent!!.breakDurationMinutesText.toIntOrNull() ?: 15
        settings.workSessionTimeMinutes = mySettingsComponent!!.workSessionTimeMinutesText.toIntOrNull() ?: 45
        settings.gracePeriodSeconds = mySettingsComponent!!.gracePeriodSecondsText.toIntOrNull() ?: 30
        settings.extensionTimeMinutes = mySettingsComponent!!.extensionTimeMinutesText.toIntOrNull() ?: 5
    }

    override fun reset() {
        val settings = service<AppSettingsState>()
        mySettingsComponent!!.breakDurationMinutesText = settings.breakDurationMinutes.toString()
        mySettingsComponent!!.workSessionTimeMinutesText = settings.workSessionTimeMinutes.toString()
        mySettingsComponent!!.gracePeriodSecondsText = settings.gracePeriodSeconds.toString()
        mySettingsComponent!!.extensionTimeMinutesText = settings.extensionTimeMinutes.toString()
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
