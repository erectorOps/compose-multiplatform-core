/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.text.input

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.PlatformTextInputMethodRequest
import androidx.compose.ui.platform.UIKitNativeTextInputContextMenuCustomAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.CoroutineScope
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
class TextInputConnectionTest {
    @Test
    fun setMarkedTextAppliesSelectionRelativeToMarkedText() {
        val request =
            TestInputMethodRequest(
                TextFieldValue(
                    text = "abへんかんがおかしいなcd",
                    selection = TextRange(12),
                    composition = TextRange(2, 12),
                )
            )
        val connection = TestTextInputConnection()
        connection.start(request)

        connection.setMarkedText(markedText = "変換がおかしいな", selectedRange = TextRange(2))

        assertEquals("ab変換がおかしいなcd", request.currentValue.text)
        assertEquals(TextRange(2, 10), request.currentValue.composition)
        assertEquals(TextRange(4), request.currentValue.selection)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class TestTextInputConnection :
    TextInputConnection(
        updateView = {},
        view = UIView(frame = CGRectZero.readValue()),
        coroutineScope = CoroutineScope(EmptyCoroutineContext),
        focusedViewsList = null,
        focusManager = { null },
    ) {
    override val textInputView = UIView(frame = CGRectZero.readValue())

    override fun attachInputToView() = Unit

    override fun detachView() = Unit

    override fun stateWillChange(textChanged: Boolean, selectionChanged: Boolean) = Unit

    override fun stateDidChange(textChanged: Boolean, selectionChanged: Boolean) = Unit

    override fun onViewGeometryUpdated() = Unit

    override fun setAvailableEditMenuActions(
        copy: (() -> Unit)?,
        paste: (() -> Unit)?,
        cut: (() -> Unit)?,
        selectAll: (() -> Unit)?,
        customActions: List<UIKitNativeTextInputContextMenuCustomAction>?,
    ) = Unit
}

private class TestInputMethodRequest(initialValue: TextFieldValue) :
    PlatformTextInputMethodRequest {
    private val editProcessor =
        EditProcessor().apply {
            reset(initialValue.copy(selection = TextRange.Zero, composition = null), null)
            reset(initialValue, null)
        }

    var currentValue = editProcessor.toTextFieldValue()
        private set

    private val editorState =
        object : TextEditorState {
            override val selection: TextRange
                get() = currentValue.selection

            override val composition: TextRange?
                get() = currentValue.composition

            override val length: Int
                get() = currentValue.text.length

            override fun get(index: Int): Char = currentValue.text[index]

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
                currentValue.text.subSequence(startIndex, endIndex)

            override val text: String
                get() = currentValue.text
        }

    override val value: () -> TextFieldValue = { currentValue }
    override val state: TextEditorState = editorState
    override val imeOptions: ImeOptions = ImeOptions.Default
    override val onEditCommand: (List<EditCommand>) -> Unit = { commands ->
        currentValue = editProcessor.apply(commands)
    }
    override val onImeAction: ((ImeAction) -> Unit)? = null
    override val textLayoutResult: () -> TextLayoutResult? = { null }
    override val focusedRectInRoot: () -> Rect? = { null }
    override val textFieldRectInRoot: () -> Rect? = { null }
    override val textClippingRectInRoot: () -> Rect? = { null }
    override val unclippedTextOffsetInRoot: () -> Offset? = { null }
    override val editText: (block: TextEditingScope.() -> Unit) -> Unit = { block ->
        val commands = mutableListOf<EditCommand>()
        RecordingTextEditingScope(commands).block()
        onEditCommand(commands)
    }
}

private class RecordingTextEditingScope(private val commands: MutableList<EditCommand>) :
    TextEditingScope {
    override fun deleteSurroundingTextInCodePoints(
        lengthBeforeCursor: Int,
        lengthAfterCursor: Int,
    ) {
        commands += DeleteSurroundingTextInCodePointsCommand(lengthBeforeCursor, lengthAfterCursor)
    }

    override fun setSelection(start: Int, end: Int) {
        commands += SetSelectionCommand(start, end)
    }

    override fun commitText(text: CharSequence, newCursorPosition: Int) {
        commands += CommitTextCommand(text.toString(), newCursorPosition)
    }

    override fun setComposingRegion(start: Int, end: Int) {
        commands += SetComposingRegionCommand(start, end)
    }

    override fun setComposingText(text: CharSequence, newCursorPosition: Int) {
        commands += SetComposingTextCommand(text.toString(), newCursorPosition)
    }

    override fun finishComposingText() {
        commands += FinishComposingTextCommand()
    }
}
