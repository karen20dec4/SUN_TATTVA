package com.android.sun.util

import android.util.Log
import com.android.sun.BuildConfig

/**
 * Wrapper pentru debug logging care verifică BuildConfig.DEBUG.
 * În build-urile release, mesajele de debug nu vor fi procesate.
 *
 * Înlocuiește apelurile directe android.util.Log.d() din tot proiectul.
 */
object AppLog {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }
}
