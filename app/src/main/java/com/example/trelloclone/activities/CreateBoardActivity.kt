package com.example.trelloclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.databinding.ActivityCreateBoardBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateBoardActivity : BaseActivity() {

    companion object{
        private const val MANAGE_STORAGE_PERMISSION_CODE = 1
    }

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var binding : ActivityCreateBoardBinding
    private lateinit var userName: String
    private var boardImageURL: String = ""
    private var selectedImageFileUri : Uri? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.e("user",intent.hasExtra("name").toString() )
        if(intent.hasExtra("name")){
            userName = intent.getStringExtra("name").toString()
            Log.e("user",intent.getStringExtra("name").toString() )
        }
        setupToolbar()
        setFlags()
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
                    .into(binding.circleImageOfBoard)

                //binding.imageMyProfile.setImageURI(image)
            }
        }
        binding.circleImageOfBoard.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED){

                showImageChooser()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    MANAGE_STORAGE_PERMISSION_CODE)
            }
        }
        binding.buttonCreate.setOnClickListener {

            if(selectedImageFileUri!=null){
                uploadBoardImage()
            }else{
                showProgressDialog("Please wait")
                createBoard()
            }


        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    private fun setupToolbar(){
        setSupportActionBar(binding.toolbarCreateBoard)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCreateBoard.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    private fun showImageChooser(){
        var galleryInput = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        launcher.launch(galleryInput)

    }
    private fun createBoard(){
        val assignedUsersArrayList: ArrayList<String> = java.util.ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())
        var board = Board(
            binding.textViewBoardName.text.toString(),
            boardImageURL,
            userName,
            assignedUsersArrayList
        )
        FirestoreClass().registerBoard(this, board)
    }
    private fun uploadBoardImage(){
        showProgressDialog("Please wait")

        val sRef : StorageReference = FirebaseStorage.getInstance().reference.child("BOARD_IMAGE"+System.currentTimeMillis()
                + "."+ getFileExtension(selectedImageFileUri))
        sRef.putFile(selectedImageFileUri!!).addOnSuccessListener {
                taskSnapShot->
            Log.i("Firebase Board Image URI",taskSnapShot.metadata!!.reference!!.toString())
            taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->

                Log.i("Downloadable Image URI", uri.toString())

                boardImageURL = uri.toString()

                createBoard()

            }
        }.addOnFailureListener {
                exception->
            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }
    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

}