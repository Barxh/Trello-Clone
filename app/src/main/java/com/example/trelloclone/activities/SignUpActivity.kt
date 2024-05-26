package com.example.trelloclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivitySignUpBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {


    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFlags()
        setupActionBar()

        binding.btnSignUp.setOnClickListener { registerUser() }

    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbarSignUp)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarSignUp.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun registerUser(){
        val name: String = binding.etName.text.toString().trim {it <=' ' }
        val email: String = binding.etEmail.text.toString().trim {it <=' ' }
        val password: String = binding.etPassword.text.toString().trim {it <=' ' }

        if(validateForm(name, email, password)){

            showProgressDialog("Please wait")
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                task->
                hideProgressDialog()
                if(task.isSuccessful){
                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid, name, registeredEmail)
                    FirestoreClass().registerUser(this, user)

                }else{
                    Toast.makeText(this, task.exception!!.message,Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name)-> {
                showErrorSnackBar("Please enter a name")
                false
            }
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

    fun userRegisteredSuccess() {
        Toast.makeText(this, "You have been successfully registered", Toast.LENGTH_LONG).show()
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
//        FirebaseAuth.getInstance().signOut()
        finish()
    }
}