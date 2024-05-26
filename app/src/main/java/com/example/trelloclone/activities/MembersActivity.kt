package com.example.trelloclone.activities

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.ComponentDialog
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trelloclone.R
import com.example.trelloclone.adapters.MemberListItemsAdapter

import com.example.trelloclone.databinding.ActivityMembersBinding
import com.example.trelloclone.databinding.DialogSearchMemberBinding
import com.example.trelloclone.databinding.ItemMemberBinding
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.Executors

class MembersActivity : BaseActivity() {
    private lateinit var binding: ActivityMembersBinding
    private lateinit var bindingDialog: DialogSearchMemberBinding
    private lateinit var boardDetails: Board
    private lateinit var assignedMembersList: ArrayList<User>
    var anyChangesMade: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar(this, binding.toolbarMembersActivity)
        binding.toolbarMembersActivity.title = "Members"
        if(intent.hasExtra("boardDetail")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                boardDetails = intent.getParcelableExtra("boardDetail", Board::class.java)!!
                showProgressDialog("Please wait")
                FirestoreClass().getAssignedMembersListDetails(this, boardDetails.assistedTo)
            }
        }



    }
    fun setupMembersList(list: ArrayList<User>){
        assignedMembersList  = list

        hideProgressDialog()
        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)
        binding.rvMembersList.adapter = MemberListItemsAdapter(this, list)
    }

    fun memberDetails(user: User){


        boardDetails.assistedTo.add(user.id)
        FirestoreClass().assignedMemberToBoard(this, boardDetails, user)

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.add_member->{


                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        bindingDialog = DialogSearchMemberBinding.inflate(layoutInflater)
        bindingDialog.tvAdd.setOnClickListener {

            val email = bindingDialog.etEmailSearchMember.text.toString()
            if(email.isNotEmpty()){



                dialog.dismiss()
                showProgressDialog("Please wait")
                FirestoreClass().getMemberDetails(this, email)
            }else{
                Toast.makeText(this, "Please enter an email of a member you want to add.", Toast.LENGTH_LONG).show()
            }

        }
        bindingDialog.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    fun memberAssignSuccess(user:User ){
        hideProgressDialog()
        assignedMembersList.add(user)
        setupMembersList(assignedMembersList)
        anyChangesMade = true
    }



}