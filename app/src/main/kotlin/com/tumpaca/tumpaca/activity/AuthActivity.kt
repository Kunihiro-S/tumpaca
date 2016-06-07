package com.tumpaca.tumpaca.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.widget.Button
import com.tumblr.loglr.LoginResult
import com.tumblr.loglr.Loglr
import com.tumpaca.tumpaca.R
import java.util.*

/**
 * Created by amake on 6/1/16.
 */
class AuthActivity: AppCompatActivity() {

    final var credentialsFile = "credentials.properties"
    final var urlCallback = "tumpaca://tumblr/auth/ok"
    final var consumerKeyProp = "tumblr.consumer.key"
    final var consumerSecretProp = "tumblr.consumer.secret"
    final var authTokenProp = "auth.token"
    final var authTokenSecretProp = "auth.token.secret"

    var consumerKey: String? = null
    var consumerSecret: String? = null
    var authToken: String? = null
    var authTokenSecret: String? = null

    var tag = "AuthActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadCredentials()

        setContentView(R.layout.activity_auth)

        var authButton = findViewById(R.id.button_authorize) as Button
        authButton.setOnClickListener { doAuth() }
    }

    fun loadCredentials() {
        var authProps = Properties()
        resources.openRawResource(R.raw.auth).use { authProps.load(it) }
        consumerKey = String(Base64.decode(authProps.get(consumerKeyProp) as String, Base64.DEFAULT))
        consumerSecret = String(Base64.decode(authProps.get(consumerSecretProp) as String, Base64.DEFAULT))

        assert(consumerKey != null)
        assert(consumerSecret != null)

        val prefs = getPreferences(MODE_PRIVATE)
        prefs.getString(authTokenProp, null)?.let { authToken = String(Base64.decode(it, Base64.DEFAULT)) }
        prefs.getString(authTokenSecretProp, null)?.let { authTokenSecret = String(Base64.decode(it, Base64.DEFAULT)) }
    }

    fun doAuth() {
        Loglr.getInstance()
                .setConsumerKey(consumerKey)
                .setConsumerSecretKey(consumerSecret)
                .setLoginListener { onLogin(it) }
                .setExceptionHandler { onException(it) }
                .setUrlCallBack(urlCallback)
                .initiateInActivity(this)
    }

    fun onLogin(r: LoginResult) {
        authToken = r.oAuthToken
        authTokenSecret = r.oAuthTokenSecret
        val editor = getPreferences(MODE_PRIVATE).edit()
        editor.putString(authTokenProp, Base64.encodeToString(r.oAuthToken.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        editor.putString(authTokenSecretProp, Base64.encodeToString(r.oAuthTokenSecret.toByteArray(Charsets.UTF_8), Base64.DEFAULT))
        val committed = editor.commit()
        assert(committed)
        goToDashboard()
    }

    fun goToDashboard() {
        assert(authToken != null, { "authToken がまだ null のまま Dashboard を開こうとしている" })
        assert(authTokenSecret != null, { "authTokenSecret がまだ null のまま Dashboard を開こうとしている" })
        var intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("consumerKey", consumerKey)
        intent.putExtra("consumerSecret", consumerSecret)
        intent.putExtra("authToken", authToken)
        intent.putExtra("authTokenSecret", authTokenSecret)
        startActivity(intent)
    }

    fun onException(r: RuntimeException) {
        Log.e(tag, "Exception occurred on login", r)
    }
}