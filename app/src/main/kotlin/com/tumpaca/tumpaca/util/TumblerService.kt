package com.tumpaca.tumpaca.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.tumblr.jumblr.JumblrClient
import com.tumblr.loglr.LoginResult
import com.tumblr.loglr.Loglr
import com.tumpaca.tumpaca.R
import java.util.*

/**
 * 認証などの Tumbler に関するサービスを提供します。
 */
class TumblerService(context: Context) {
    companion object {
        const val TAG = "TumblerService"
        const val URL_CALLBACK = "tumpaca://tumblr/auth/ok"
        const val AUTH_SHARED_PREFERENCE_NAME = "TumpacaPreference"
        const val CONSUMER_KEY_PROP = "tumblr.consumer.key"
        const val CONSUMER_SECRET_PROP = "tumblr.consumer.secret"
        const val AUTH_TOKEN_PROP = "auth.token"
        const val AUTH_TOKEN_SECRET_PROP = "auth.token.secret"
    }

    val context: Context = context
    val consumerInfo: ConsumerInfo
    var authInfo: AuthInfo? = null
    var client: JumblrClient? = null

    init {
        consumerInfo = loadConsumer()
        authInfo = loadAuthToken()
    }

    fun isLogin(): Boolean {
        // ログインしているかどうかを authInfo でチェック。
        // しかし、本当に有効なトークンかどうかは未検証なので注意する。
        return authInfo != null
    }

    fun getJumblrClient(): JumblrClient? {
        return client ?: createClient()
    }

    fun auth(activity: Activity, success: () -> Unit) {
        Loglr.getInstance()
                .setConsumerKey(consumerInfo.key)
                .setConsumerSecretKey(consumerInfo.secret)
                .setLoginListener { onLogin(it); success() }
                .setExceptionHandler { onException(it) }
                .setUrlCallBack(URL_CALLBACK)
                .initiateInActivity(activity)
    }

    fun logout() {
        client = null
        authInfo = null
    }

    private fun createClient(): JumblrClient? {
        // それっぽい書き方がありあそう
        if (authInfo != null) {
            client = JumblrClient(consumerInfo.key, consumerInfo.secret, authInfo!!.token, authInfo!!.secret)
        }
        return client
    }

    private fun loadConsumer(): ConsumerInfo {
        val authProps = Properties()
        context.resources.openRawResource(R.raw.auth).use { authProps.load(it) }
        val key = String(Base64.decode(authProps[CONSUMER_KEY_PROP] as String, Base64.DEFAULT))
        val secret = String(Base64.decode(authProps[CONSUMER_SECRET_PROP] as String, Base64.DEFAULT))
        return ConsumerInfo(key, secret)
    }

    private fun loadAuthToken(): AuthInfo? {
        val prefs = getAuthSharedPreference()
        val token = prefs.getString(AUTH_TOKEN_PROP, null)?.let { String(Base64.decode(it, Base64.DEFAULT)) }
        val secret = prefs.getString(AUTH_TOKEN_SECRET_PROP, null)?.let { String(Base64.decode(it, Base64.DEFAULT)) }
        Log.d(TAG, "Loaded AuthToken: token=$token, secret=$secret")
        return if (token != null && secret != null) AuthInfo(token, secret) else null
    }

    private fun saveAuthToken(authInfo: AuthInfo) {
        Log.d(TAG, "Saved AuthToken: token=" + authInfo.token + ", secret=" + authInfo.secret)
        context.editSharedPreferences(AUTH_SHARED_PREFERENCE_NAME) {
            it.putString(AUTH_TOKEN_PROP, Base64.encodeToString(authInfo.token.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
            it.putString(AUTH_TOKEN_SECRET_PROP, Base64.encodeToString(authInfo.secret.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        }
    }

    private fun getAuthSharedPreference(): SharedPreferences {
        return context.getSharedPreferences(AUTH_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private fun onLogin(result: LoginResult) {
        // TODO null チェックとかしたほうがいい
        authInfo = AuthInfo(result.oAuthToken, result.oAuthTokenSecret)
        saveAuthToken(authInfo!!)
    }

    private fun onException(e: RuntimeException) {
        Log.e(TAG, "Exception occurred on login", e)
    }


}

class ConsumerInfo(val key: String, val secret: String)

class AuthInfo(val token: String, val secret: String)
