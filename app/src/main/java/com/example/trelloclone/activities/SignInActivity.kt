package com.example.trelloclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivitySignInBinding
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        setFlags()
        binding.btnSignIn.setOnClickListener {
            signInUser()
        }
    }
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignUp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun signInUser(){
        val email: String = binding.etEmail.text.toString().trim{it <=' '}
        val password: String = binding.etPassword.text.toString().trim{it <=' '}

        if(validateForm(email, password)){

            showProgressDialog("Please wait")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {

                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
    private fun validateForm( email: String, password: String): Boolean{
        return when{

            TextUtils.isEmpty(email)-> {
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)-> {
                showErrorSnackBar("Please enter a password")
                false
            }else->{
                true
            }

        }
    }

    fun signInSuccess(user: User) {

        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()

    }
}