package com.shinjaehun.takenotes

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*
import com.shinjaehun.takenotes.databinding.ActivitySignUpBinding

//private const val TAG = "SignUpActivity"

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private var RC_SIGN_IN: Int = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this) // 이게 왜 필요한거요?

        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btnSignIn.setOnClickListener {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect = VibrationEffect.createOneShot(30, 100)
            vibrator.vibrate(vibrationEffect)
            signIn()
        }
    }

    private fun signIn() {
        val intent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN) // 일단 해 놓고 수정합시다.
    }

    // 일단 해 놓고 수정합시다
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }
    private fun FirebaseGoogleAuth(acc: GoogleSignInAccount) {
        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(acc.idToken, null)
        mAuth.signInWithCredential(authCredential).addOnCompleteListener {
            if (it.isSuccessful) {
                val user: FirebaseUser? = mAuth.getCurrentUser()
            } else {
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val acc: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)
            if (acct != null) {
                val i: Intent = Intent(this@SignUpActivity, MainPageActivity::class.java)
                startActivity(i)
                finish()
            }
            FirebaseGoogleAuth(acc)
        } catch (e: ApiException) {
            Toast.makeText(this, "oops", Toast.LENGTH_SHORT).show()
        }
    }
}