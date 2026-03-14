package com.exory550.exoryfilemanager.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.exory550.exoryfilemanager.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = when {
            !preferenceManager.isIntroCompleted -> IntroActivity.getIntent(this)
            preferenceManager.isAppLockEnabled -> AuthenticationActivity.getIntent(this)
            else -> MainActivity.getIntent(this)
        }
        startActivity(intent)
        finish()
    }
}
