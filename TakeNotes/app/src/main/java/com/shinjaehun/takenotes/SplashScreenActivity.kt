package com.shinjaehun.takenotes

import android.content.Context
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.shinjaehun.takenotes.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private var fadeIn: AlphaAnimation = AlphaAnimation(0.0f, 1.0f)
    private var binding: ActivitySplashScreenBinding? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)

        setContentView(binding!!.root)

        fireSplashScreen()

        binding!!.tvTitle.setText("Taken Notes")
        binding!!.tvTitle.startAnimation(fadeIn)
        fadeIn.setDuration(1500)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fireSplashScreen() {
        val splash_screen_time_out:Long = 3000
        val handler = Handler()
        handler.postDelayed({
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect = VibrationEffect.createOneShot(30, 100)
            vibrator.vibrate(vibrationEffect)
            check()
            finish()
        }, splash_screen_time_out)
    }

    private fun check() {
//        val intent = Intent(this@SplashScreenActivity, SignUpActivity::class.java)
//        startActivity(intent)

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)
        if (account != null) {
            val intent: Intent = Intent(this@SplashScreenActivity, MainPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            // newbie
            val intent: Intent = Intent(this@SplashScreenActivity, MainPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}