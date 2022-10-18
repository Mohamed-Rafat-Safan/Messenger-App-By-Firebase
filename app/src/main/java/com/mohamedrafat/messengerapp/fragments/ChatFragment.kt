package com.mohamedrafat.messengerapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.model.ContentItemUser
import com.mohamedrafat.messengerapp.model.DataEveryItem
import com.mohamedrafat.messengerapp.model.User
import com.mohamedrafat.messengerapp.recyclerView.CustomAdapterForUsers
import com.mohamedrafat.messengerapp.ui.AllUsersActivity
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis

class ChatFragment : Fragment() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var allUsers = ArrayList<ContentItemUser>()
    private var allUsersChatted = ArrayList<DataEveryItem>()

    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        view.tv_search.setOnClickListener {
            startActivity(Intent(requireContext(), AllUsersActivity::class.java))
        }


        addUsersToRecyclerView(view)


        return view
    }



    private fun addUsersToRecyclerView(view: View) {
        // هذه المتغير لكي يعرض الاصدقاء بي ترتيب اخر رساله اتبعتت
        val query = db.collection("Users")
            .document(auth.currentUser!!.uid)
            .collection("sharedChat")
            .orderBy("timeLastMessage", Query.Direction.DESCENDING)

        query.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            allUsersChatted.clear()
            value!!.documents.forEach { document ->
                val dataEveryItem = DataEveryItem(document.id ,
                    document.getString("lastMessage")!! ,
                    document.getDate("timeLastMessage")!! )

                allUsersChatted.add(dataEveryItem)
            }

            db.collection("Users")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    allUsers.clear()  // this clear for all user to their enter from new
                    for (i in allUsersChatted) {
                        value!!.documents.forEach {
                            val user = it.toObject(User::class.java)
                            if (it.id == i.userId) {
                                allUsers.add( ContentItemUser( i.lastMessage, i.timeLastMessage,  user!!) )
                            }
                        }
                    }

                    if(auth.currentUser != null){
                        val adapter = CustomAdapterForUsers(allUsers, requireActivity(), 1)
                        view.recyclerView_users.layoutManager = LinearLayoutManager(this.context)
                        view.recyclerView_users.adapter = adapter
                        view.recyclerView_users.setHasFixedSize(true)
                    }

                }

        }


        val time = measureTimeMillis { }
        // ديه بتستخدم لكي تحسب الوقت المستغرق لتنفيذ الكود الي بداخلها
    }


}