package com.example.trelloclone.activities

import android.app.Activity
import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.trelloclone.R
import com.example.trelloclone.databinding.DialogProgressBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {




    private var doubleBackToExitPressedOnce = false

    private lateinit var  progressDialog: Dialog

    private lateinit var binding: DialogProgressBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }
    fun showProgressDialog(text: String){
        progressDialog = Dialog(this)

        progressDialog.setContentView(R.layout.dialog_progress)
        binding = DialogProgressBinding.inflate(layoutInflater)
        binding.tvProgressText.text = text
        progressDialog.show()
    }
    fun hideProgressDialog(){

        progressDialog.dismiss()
    }

    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){
        if(doubleBackToExitPressedOnce){
            onBackPressedDispatcher.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            "Please click back again to exit",
            Toast.LENGTH_SHORT
        ).show()
        Handler(Looper.getMainLooper()).postDelayed(
            {
            doubleBackToExitPressedOnce = false

        }, 3000)
    }
    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_error_color))
        snackBar.show()

    }

    fun setupActionBar(activity: BaseActivity, toolbar: androidx.appcompat.widget.Toolbar){
        activity.setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.back_arrow)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            if(activity is MembersActivity && activity.anyChangesMade){
                activity.setResult(Activity.RESULT_OK)
            }
            onBackPressedDispatcher.onBackPressed()
        }

    }

    fun setFlags(){
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }
}