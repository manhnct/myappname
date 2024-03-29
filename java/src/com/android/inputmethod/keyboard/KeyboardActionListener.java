/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.inputmethod.keyboard;

public interface KeyboardActionListener {

    /**
     * Called when the user presses a key. This is sent before the {@link #onCodeInput} is called.
     * For keys that repeat, this is only called once.
     *
     * @param primaryCode the unicode of the key being pressed. If the touch is not on a valid key,
     *            the value will be zero.
     */
    public void onPressKey(int primaryCode);

    /**
     * Called when the user releases a key. This is sent after the {@link #onCodeInput} is called.
     * For keys that repeat, this is only called once.
     *
     * @param primaryCode the code of the key that was released
     * @param withSliding true if releasing has occurred because the user slid finger from the key
     *             to other key without releasing the finger.
     */
    public void onReleaseKey(int primaryCode, boolean withSliding);

    /**
     * Send a key code to the listener.
     *
     * @param primaryCode this is the code of the key that was pressed
     * @param x x-coordinate pixel of touched event. If {@link #onCodeInput} is not called by
     *            {@link PointerTracker} or so, the value should be {@link #NOT_A_TOUCH_COORDINATE}.
     *            If it's called on insertion from the suggestion strip, it should be
     *            {@link #SUGGESTION_STRIP_COORDINATE}.
     * @param y y-coordinate pixel of touched event. If {@link #onCodeInput} is not called by
     *            {@link PointerTracker} or so, the value should be {@link #NOT_A_TOUCH_COORDINATE}.
     *            If it's called on insertion from the suggestion strip, it should be
     *            {@link #SUGGESTION_STRIP_COORDINATE}.
     */
    public void onCodeInput(int primaryCode, int x, int y);

    public static final int NOT_A_TOUCH_COORDINATE = -1;
    public static final int SUGGESTION_STRIP_COORDINATE = -2;
    public static final int SPELL_CHECKER_COORDINATE = -3;

    /**
     * Sends a sequence of characters to the listener.
     *
     * @param text the sequence of characters to be displayed.
     */
    public void onTextInput(CharSequence text);

    /**
     * Called when user released a finger outside any key.
     */
    public void onCancelInput();

    /**
     * Send a non-"code input" custom request to the listener.
     * @return true if the request has been consumed, false otherwise.
     */
    public boolean onCustomRequest(int requestCode);

    public static class Adapter implements KeyboardActionListener {
        @Override
        public void onPressKey(int primaryCode) {}
        @Override
        public void onReleaseKey(int primaryCode, boolean withSliding) {}
        @Override
        public void onCodeInput(int primaryCode, int x, int y) {}
        @Override
        public void onTextInput(CharSequence text) {}
        @Override
        public void onCancelInput() {}
        @Override
        public boolean onCustomRequest(int requestCode) {
            return false;
        }
    }
}
