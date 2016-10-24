package com.tumpaca.tumpaca.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * SharedPreferencesにデータを保存するためのユーティリティ
 * Created by yabu on 2016/10/24.
 */
class TPSettings(val ctx: Context) {

    companion object {
        private const val TAG = "TPSettings"
        private const val SHOW_MY_POSTS = "SHOW_MY_POSTS"
        private const val EXCLUDE_PHOTO = "EXCLUDE_PHOTO"
    }

    /**
     * 自分のポストを表示するかどうかの設定
     */
    private var mIsShowMyPosts: Boolean? = null

    fun isShowMyPosts(): Boolean {
        if (mIsShowMyPosts == null) {
            mIsShowMyPosts = getBoolean(SHOW_MY_POSTS, true)
        }
        return mIsShowMyPosts!!
    }

    fun setShowMyPosts(isShowMyPosts: Boolean): Unit {
        mIsShowMyPosts = isShowMyPosts
        save(SHOW_MY_POSTS, isShowMyPosts)
    }

    /**
     * 写真、動画、音声ポストを除外するかどうかの設定
     */
    private var mExcludePhoto: Boolean? = null

    fun isExcludePhoto(): Boolean {
        if (mExcludePhoto == null) {
            mExcludePhoto = getBoolean(EXCLUDE_PHOTO, false)
        }
        return mExcludePhoto!!
    }

    fun setExcludePhoto(excludePhoto: Boolean): Unit {
        mExcludePhoto = excludePhoto
        save(EXCLUDE_PHOTO, excludePhoto)
    }

    private fun save(key: String, value: Any): Unit {
        val data = ctx.getSharedPreferences("DataSave", Context.MODE_PRIVATE)
        val editor = data.edit()

        when (value) {
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is String -> editor.putString(key, value)
            else -> Log.v(TAG, "value type must be Int, Boolean or String.")
        }
        editor.apply()
    }

    private fun getInt(key: String, defaultValue: Int): Int {
        val data = getSharedPreferences()
        return data.getInt(key, defaultValue)
    }

    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val data = getSharedPreferences()
        return data.getBoolean(key, defaultValue)
    }

    private fun getString(key: String, defaultValue: String): String {
        val data = getSharedPreferences()
        return data.getString(key, defaultValue)
    }

    private fun getSharedPreferences(): SharedPreferences =
            ctx.getSharedPreferences("DataSave", Context.MODE_PRIVATE)

}