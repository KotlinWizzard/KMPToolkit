package com.kmptoolkit.core.presentation.components.focus

import androidx.compose.ui.focus.FocusRequester

class FocusLayoutState {
    val focusRequester = FocusRequester()

    fun clearFocus() {
        focusRequester.freeFocus()
    }

    fun requestFocus() {
        focusRequester.requestFocus()
    }

    fun hideKeyboard() {
        focusRequester.requestFocus()
        focusRequester.freeFocus()
    }
}