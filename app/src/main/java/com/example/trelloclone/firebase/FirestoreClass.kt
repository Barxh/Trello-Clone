package com.example.trelloclone.firebase

import android.app.Activity
import android.provider.ContactsContract.Contacts
import android.util.Log
import android.widget.Toast
import com.example.trelloclone.activities.BaseActivity
import com.example.trelloclone.activities.CardDetailActivity
import com.example.trelloclone.activities.CreateBoardActivity
import com.example.trelloclone.activities.MainActivity
import com.example.trelloclone.activities.MembersActivity
import com.example.trelloclone.activities.MyProfileActivity
import com.example.trelloclone.activities.SignInActivity
import com.example.trelloclone.activities.SignUpActivity
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.models.Board
import com.example.trelloclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject


class FirestoreClass {

    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo : User){
        fireStore.collection("Users")
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge() )
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }
    fun getCurrentUserId(): String{
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
    fun registerBoard(activity: CreateBoardActivity,board: Board ){
        fireStore.collection("Boards")
            .document().set(board, SetOptions.merge() )
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully!", Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {exception ->
                activity.hideProgressDialog()
                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
            }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false){
        fireStore.collection("Users")
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {

                val loggedInUser = it.toObject(User::class.java)!!
                when(activity){
                    is SignInActivity->{
                        if(loggedInUser !=null) {
                            activity.signInSuccess(loggedInUser)
                        }
                    }
                    is MainActivity ->{
                        activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                    }
                    is MyProfileActivity ->{

                        activity.setUserDataInUI(loggedInUser)
                    }
                }

            }.addOnFailureListener {

                when(activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()

                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity->{
                        activity.hideProgressDialog()
                    }
                }
            }
    }
    fun updateUserProfileData(activity: BaseActivity, userHashMap: HashMap<String,Any>){
        fireStore.collection("Users")
            .document(getCurrentUserId()).update(userHashMap).addOnSuccessListener {
                Toast.makeText(activity,"Profile updated successfully!",Toast.LENGTH_LONG).show()

                if(activity is MyProfileActivity)
                activity.profileUpdateSuccess()
                else if(activity is MainActivity){
                    activity.tokenUpdateSuccess()
                }
            }.addOnFailureListener {

                e->
                activity.hideProgressDialog()
                Toast.makeText(activity, e.message,Toast.LENGTH_LONG).show()
            }
    }
    fun getBoardsList(activity: MainActivity){
        fireStore.collection("Boards")
            .whereArrayContains("assistedTo", getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document->
                val boardsList: ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }
                Log.e("boards", boardsList.size.toString())
                activity.populateBoardsListToUI(boardsList)
            }.addOnFailureListener {exception->
                activity.hideProgressDialog()
                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
            }

    }

    fun getBoardDetails(taskListActivity: TaskListActivity, boardDocumentId: String) {
        fireStore.collection("Boards")
            .document(boardDocumentId)
            .get()
            .addOnSuccessListener {
                    document->

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                taskListActivity.boardDetails(board)

            }.addOnFailureListener {exception->
                taskListActivity.hideProgressDialog()
                Toast.makeText(taskListActivity, exception.message, Toast.LENGTH_LONG).show()
            }
    }
    fun addUpdateTaskList(activity: BaseActivity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap["taskList"] = board.taskList
        fireStore.collection("Boards")
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if(activity is TaskListActivity )
                activity.addUpdateTaskListSuccess()
                if(activity is CardDetailActivity)


                    activity.addUpdateTaskListSuccess()

            }.addOnFailureListener {
                exception->
                if(activity is TaskListActivity )
                    activity.hideProgressDialog()
                if(activity is CardDetailActivity)
                activity.hideProgressDialog()
                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
            }
    }
    fun getAssignedMembersListDetails(activity: BaseActivity, assignedTo:ArrayList<String>){
        fireStore.collection("Users")
            .whereIn("id", assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                val userList : ArrayList<User> = ArrayList()
                for(i in document.documents){
                    userList.add(i.toObject(User::class.java)!!)
                }
                if(activity is MembersActivity)
                activity.setupMembersList(userList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailsList(userList)
            }.addOnFailureListener {
                exception->

                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()

                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        fireStore.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener {
                document->
                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar( "No such member found")
                }

            }.addOnFailureListener {
                exception->
                activity.hideProgressDialog()
                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()

            }
    }
    fun assignedMemberToBoard(activity: MembersActivity, board: Board, userUser: User){

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap["assignedTo"] = board.assistedTo

        fireStore.collection("Boards")
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {

                activity.memberAssignSuccess(userUser)
            }.addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Toast.makeText(activity, exception.message, Toast.LENGTH_LONG).show()
            }
    }
}