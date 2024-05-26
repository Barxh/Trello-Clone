package com.example.trelloclone.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.trelloclone.R
import com.example.trelloclone.adapters.BoardItemsAdapter
import com.example.trelloclone.databinding.ActivityCreateBoardBinding
import com.example.trelloclone.databinding.ActivityMainBinding
import com.example.trelloclone.databinding.AppBarMainBinding
import com.example.trelloclone.databinding.MainContentBinding
import com.example.trelloclone.databinding.NavHeaderMainBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity() , NavigationView.OnNavigationItemSelectedListener{
    private lateinit var binding : ActivityMainBinding
    private lateinit var bindingBarMainBinding: AppBarMainBinding
    private lateinit var userName: String
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var launcherBoard: ActivityResultLauncher<Intent>
    private lateinit var bindingMainContentBinding: MainContentBinding
    private lateinit var bindingHeaderMainBinding: NavHeaderMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    companion object {
        const val MY_PROFILE_REQUEST_CODE = 11
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindingBarMainBinding = AppBarMainBinding.bind(binding.appBarMain.root)
        bindingHeaderMainBinding = NavHeaderMainBinding.bind(binding.navigationView.getHeaderView(0))
        bindingMainContentBinding = MainContentBinding.bind(bindingBarMainBinding.mainContext.root)
        setFlags()
        setupActionBar()
        launcher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()){
                result->
            if(result.resultCode == Activity.RESULT_OK ){

            FirestoreClass().loadUserData(this)



            //binding.imageMyProfile.setImageURI(image)
        }
        }
        launcherBoard = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK ){


                FirestoreClass().getBoardsList(this)

            }
        }
        binding.navigationView.setNavigationItemSelectedListener(this)
        sharedPreferences = this.getSharedPreferences("TrelloclonePrefs", Context.MODE_PRIVATE)

        val tokenUpdated = sharedPreferences.getBoolean("fcmTokenUpdated", false)
        if(tokenUpdated){
            showProgressDialog("Please wait")
            FirestoreClass().loadUserData(this, true)

        }else{
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token->
                    updateFCMToken(token)

                }
        }
        FirestoreClass().loadUserData(this, true)
        bindingBarMainBinding.floatingButtonMain.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            Log.e("user", userName)
            intent.putExtra("name",userName)
           launcherBoard.launch(intent)
        }

    }
    fun populateBoardsListToUI(boardList: ArrayList<Board>){
        hideProgressDialog()


        if(boardList.size>0){
            bindingMainContentBinding.recyclerViewBoards.visibility = View.VISIBLE
            bindingMainContentBinding.textViewNoBoards.visibility = View.GONE


            bindingMainContentBinding.recyclerViewBoards.layoutManager = LinearLayoutManager(this)
            bindingMainContentBinding.recyclerViewBoards.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            bindingMainContentBinding.recyclerViewBoards.adapter = adapter
            adapter.setOnClickListener(object: BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra("documentId",model.documentId)
                    startActivity(intent)
                }

            })
        }else{
            bindingMainContentBinding.recyclerViewBoards.visibility = View.GONE
            bindingMainContentBinding.textViewNoBoards.visibility = View.VISIBLE
        }

    }
    private fun setupActionBar(){
        setSupportActionBar(bindingBarMainBinding.toolbarMainActivity)
        bindingBarMainBinding.toolbarMainActivity.setNavigationIcon(R.drawable.baseline_menu_24)
        bindingBarMainBinding.toolbarMainActivity.setNavigationOnClickListener {

            toggleDrawer()
        }

    }
    private fun toggleDrawer(){
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile->{


                launcher.launch(Intent(this,MyProfileActivity::class.java))
            }
            R.id.nav_sign_out->{
                FirebaseAuth.getInstance().signOut()
                sharedPreferences.edit().clear().apply()
                val intent = Intent(this, IntroActivity::class.java)

                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK or  Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }


        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(loggedInUser: User, readBoardList: Boolean) {

        hideProgressDialog()
        userName = loggedInUser.name
        Glide
            .with(this)
            .load(loggedInUser.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(bindingHeaderMainBinding.userImage)
        bindingHeaderMainBinding.textViewUsername.text = loggedInUser.name

        if(readBoardList){
            showProgressDialog("Please wait")
            FirestoreClass().getBoardsList(this)
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("fcmTokenUpdated", true)
        editor.apply()
        showProgressDialog("Please wait")
        FirestoreClass().loadUserData(this, true)

    }
    private fun updateFCMToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap["fcmToken"] = token
        showProgressDialog("Please wait")

        FirestoreClass().updateUserProfileData(this,userHashMap)
    }
}