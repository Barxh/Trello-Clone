package com.example.trelloclone.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import com.example.trelloclone.R
import com.example.trelloclone.firebase.FirestoreClass

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setFlags()
        val typeface: Typeface = Typeface.createFromAsset(assets, "carbon bl.otf")
        findViewById<TextView>(R.id.text_view_app_name).typeface= typeface
        Handler(Looper.getMainLooper()).postDelayed(
            {
                val currentUserID =FirestoreClass().getCurrentUserId()
                if(currentUserID.isEmpty()) {
                    startActivity(Intent(this, IntroActivity::class.java))
                }
                else{
                    startActivity(Intent(this, MainActivity::class.java))

                }
                finish()

            }, 3000)
    }
}