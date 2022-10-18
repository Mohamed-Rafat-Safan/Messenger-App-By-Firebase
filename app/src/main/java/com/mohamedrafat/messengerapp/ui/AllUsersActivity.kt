package com.mohamedrafat.messengerapp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.model.ContentItemUser
import com.mohamedrafat.messengerapp.model.User
import com.mohamedrafat.messengerapp.recyclerView.CustomAdapterForUsers
import kotlinx.android.synthetic.main.activity_all_users.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.util.*
import kotlin.collections.ArrayList

class AllUsersActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private var allUser = ArrayList<ContentItemUser>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_users)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // هذا السطر لكي يجعل ال icon الي في status bar باللون الاسود
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = Color.WHITE
        }

        setSupportActionBar(allUser_toolbar)
        supportActionBar!!.title = "All Friends"
        // السطرين دول علشان اظهر السهم الي موجود (سهم الرجوع)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        if(auth.currentUser != null)
            getAllUsers()

    }


    private fun getAllUsers() {
        db.collection("Users")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                allUser.clear()  // this clear for all user to their enter from new

                value!!.documents.forEach { document ->
                    val user = document.toObject(User::class.java)
                    if(user!!.uid != auth.currentUser!!.uid)
                        allUser.add(ContentItemUser("" , Date(), user))
                }

                val adapter = CustomAdapterForUsers(allUser, this , 2)
                adapter.notifyDataSetChanged()
                recyclerView_AllUsers.layoutManager = LinearLayoutManager(this)
                recyclerView_AllUsers.adapter = adapter
                recyclerView_AllUsers.setHasFixedSize(true)
            }
    }


    // هذه الدله علشان لما اضغط علي السهم يعمل finish for activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }
}
