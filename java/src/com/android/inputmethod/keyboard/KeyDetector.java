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


public class KeyDetector {
    public static final int NOT_A_CODE = -1;

    private final int mKeyHysteresisDistanceSquared;

    private Keyboard mKeyboard;
    private int mCorrectionX;
    private int mCorrectionY;
    private boolean mProximityCorrectOn;

    /**
     * This class handles key detection.
     *
     * @param keyHysteresisDistance if the pointer movement distance is smaller than this, the
     * movement will not been handled as meaningful movement. The unit is pixel.
     */
    public KeyDetector(float keyHysteresisDistance) {
        mKeyHysteresisDistanceSquared = (int)(keyHysteresisDistance * keyHysteresisDistance);
    }

    public void setKeyboard(Keyboard keyboard, float correctionX, float correctionY) {
        if (keyboard == null)
            throw new NullPointerException();
        mCorrectionX = (int)correctionX;
        mCorrectionY = (int)correctionY;
        mKeyboard = keyboard;
    }

    public int getKeyHysteresisDistanceSquared() {
        return mKeyHysteresisDistanceSquared;
    }

    public int getTouchX(int x) {
        return x + mCorrectionX;
    }

    public int getTouchY(int y) {
        return y + mCorrectionY;
    }

    public Keyboard getKeyboard() {
        if (mKeyboard == null)
            throw new IllegalStateException("keyboard isn't set");
        return mKeyboard;
    }

    public void setProximityCorrectionEnabled(boolean enabled) {
        mProximityCorrectOn = enabled;
    }

    public boolean isProximityCorrectionEnabled() {
        return mProximityCorrectOn;
    }

    public boolean alwaysAllowsSlidingInput() {
        return false;
    }

    /**
     * Detect the key whose hitbox the touch point is in.
     *
     * @param x The x-coordinate of a touch point
     * @param y The y-coordinate of a touch point
     * @return the key that the touch point hits.
     */
    public Key detectHitKey(int x, int y) {
        final int touchX = getTouchX(x);
        final int touchY = getTouchY(y);

        int minDistance = Integer.MAX_VALUE;
        Key primaryKey = null;
        for (final Key key: mKeyboard.getNearestKeys(touchX, touchY)) {
            final boolean isOnKey = key.isOnKey(touchX, touchY);
            final int distance = key.squaredDistanceToEdge(touchX, touchY);
            // To take care of hitbox overlaps, we compare mCode here too.
            if (primaryKey == null || distance < minDistance
                    || (distance == minDistance && isOnKey && key.mCode > primaryKey.mCode)) {
                minDistance = distance;
                primaryKey = key;
            }
        }
        return primaryKey;
    }

    public static String printableCode(Key key) {
        return key != null ? Keyboard.printableCode(key.mCode) : "none";
    }

    public static String printableCodes(int[] codes) {
        final StringBuilder sb = new StringBuilder();
        boolean addDelimiter = false;
        for (final int code : codes) {
            if (code == NOT_A_CODE) break;
            if (addDelimiter) sb.append(", ");
            sb.append(Keyboard.printableCode(code));
            addDelimiter = true;
        }
        return "[" + sb + "]";
    }
}
