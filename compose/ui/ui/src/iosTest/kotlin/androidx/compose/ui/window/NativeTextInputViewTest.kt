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

package androidx.compose.ui.window

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeTextInputViewTest {
    @Test
    fun markedTextSelectionNotifiesInputDelegateWhileTrackingTouches() {
        assertTrue(
            shouldNotifyInputDelegateOfSelectionChange(
                hasMarkedText = true,
                isAtLeastIOS17 = true,
                isTrackingTouches = true,
            )
        )
    }

    @Test
    fun ordinarySelectionKeepsPlatformNotificationPolicy() {
        assertFalse(
            shouldNotifyInputDelegateOfSelectionChange(
                hasMarkedText = false,
                isAtLeastIOS17 = true,
                isTrackingTouches = false,
            )
        )
        assertTrue(
            shouldNotifyInputDelegateOfSelectionChange(
                hasMarkedText = false,
                isAtLeastIOS17 = false,
                isTrackingTouches = false,
            )
        )
    }
}
