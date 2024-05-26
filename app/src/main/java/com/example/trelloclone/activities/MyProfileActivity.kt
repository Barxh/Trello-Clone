package com.example.trelloclone.activities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityMyProfileBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.User
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyProfileActivity : BaseActivity() {

    companion object{
        private const val MANAGE_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_STORAGE = 2
    }
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var userDetails: User
    private var profileImageURL: String = ""
    private var selectedImageFileUri: Uri? = null
    private lateinit var binding: ActivityMyProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textViewName.setText("Milan")
        setupToolbar()
        FirestoreClass().loadUserData(this)
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
                result->
            if(result.resultCode == Activity.RESULT_OK &&result.data!=null && result.data!!.data!=null){


                val image = result.data?.data
                selectedImageFileUri = image
                Glide.with(this)
                    .load(selectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.imageMyProfile)

                //binding.imageMyProfile.setImageURI(image)
            }
        }
        binding.imageMyProfile.setOnClickListener {

            Log.e("error", "(:")
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED){

                Log.e("error", "-_-")
                showImageChooser()
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),

                    MANAGE_STORAGE_PERMISSION_CODE


                )
                Log.e("error", "-_-")
            }
        }
        binding.buttonUpdate.setOnClickListener {
            if(selectedImageFileUri!=null){
                uploadUserImage()
            }else{
                showProgressDialog("Please wait")
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== MANAGE_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                Toast.makeText(this,"Oops, you denied the permission for storage. You can also allow it from settings"
                ,Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun showImageChooser(){
        var galleryInput = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        launcher.launch(galleryInput)

    }
    private fun setupToolbar(){
        setSupportActionBar(binding.toolbarMyProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMyProfile.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
    fun setUserDataInUI(user:User){

        Glide.with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.imageMyProfile)
        binding.textViewName.setText(user.name)
        if(user.mobile!=0L){
            binding.textViewMobile.setText(user.mobile.toString())

        }
        binding.textViewEmail.setText(user.email)
        Log.e("error", "${user.name}")
        userDetails = user
    }
    private fun uploadUserImage(){
        showProgressDialog("Please wait")

        if(selectedImageFileUri!=null){
            val sRef : StorageReference = FirebaseStorage.getInstance().reference.child("USER_IMAGE"+System.currentTimeMillis()
                    + "."+ getFileExtension(selectedImageFileUri))
            sRef.putFile(selectedImageFileUri!!).addOnSuccessListener {
                taskSnapShot->
                Log.i("Firebase Image URI",taskSnapShot.metadata!!.reference!!.toString())
                taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->

                    Log.i("Downloadable Image URI", uri.toString())

                    profileImageURL = uri.toString()

                    updateUserProfileData()

                }
            }.addOnFailureListener {
                exception->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }

        }
    }
    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }
    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        var changeMade = false
        if(profileImageURL.isNotEmpty() && profileImageURL != userDetails.image){
            userHashMap["image"] = profileImageURL

            changeMade = true
        }
        if(binding.textViewName.text.toString()!=userDetails.name){
            userHashMap["name"]= binding.textViewName.text.toString()

            changeMade = true
        }
        if(binding.textViewMobile.text.toString()!=userDetails.mobile.toString() &&
            binding.textViewMobile.text.toString().isNotEmpty()){
            userHashMap["mobile"]= binding.textViewMobile.text.toString().toLong()
changeMade = true
        }
        if(changeMade) FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}