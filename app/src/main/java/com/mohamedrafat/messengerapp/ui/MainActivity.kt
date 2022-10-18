package com.mohamedrafat.messengerapp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.fragments.ChatFragment
import com.mohamedrafat.messengerapp.fragments.PeopleFragment
import com.mohamedrafat.messengerapp.fragments.DiscoverFragment
import com.mohamedrafat.messengerapp.model.User
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object{
        var senderName:String = ""
    }
    private val auth: FirebaseAuth by lazy {  FirebaseAuth.getInstance()  }
    private val db:FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private val currentUserDocRef:DocumentReference get() =
        db.document("Users/${auth.currentUser!!.uid.toString()}")

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val chatFragment = ChatFragment()
    private val peopleFragment = PeopleFragment()
    private val discoverFragment = DiscoverFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // هذا السطر لكي يجعل ال icon الي في status bar باللون الاسود
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }

        setSupportActionBar(main_toolbar)
        supportActionBar!!.title = ""


        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject<User>()
            setNameAndImage(user!!)
        }


        setFragment(chatFragment)
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_chat_item -> {
                    setFragment(chatFragment)

                    currentUserDocRef.get().addOnSuccessListener {
                        val user = it.toObject<User>()
                        setNameAndImage(user!!)
                    }

                     true
                }
                R.id.navigation_people_item -> {
                    setFragment(peopleFragment)
                    title_toolbar.text = "People"
                     true
                }
                R.id.navigation_discover_item -> {
                    setFragment(discoverFragment)
                    title_toolbar.text = "Discover"
                     true
                }
                else -> false
            }
        }

        profile_image_toolbar.setOnClickListener {
            startActivity(Intent(this , ProfileActivity::class.java))
        }

    }


    override fun onResume() {
        super.onResume()
        // this make the user is online
        if(auth.currentUser != null)
            db.collection("Users").document(auth.currentUser!!.uid).update("online", true)
    }


    private fun setNameAndImage(user: User){
        senderName = user.userName  // this take sender name i am need in other activity

        title_toolbar.text = senderName
        if(user.profileImage.isNotEmpty()){
            Glide.with(this@MainActivity)
                .load(storageInstance.getReference(user.profileImage))
                .placeholder(R.drawable.user)
                .into(profile_image_toolbar)
        }
    }


    fun setFragment(fragment: Fragment){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.coordinatorLayout, fragment)
        ft.commit()
    }


    //  هذه الداله علشان لما اعمل تسجيل دخول الي MainActivity
    // واعمل باك يخرجني بره التطبيق مش كل شويه يفضل يشغلها تاني
    override fun onBackPressed() {
        if(auth.currentUser != null)
            db.collection("Users").document(auth.currentUser!!.uid).update("online", false)

        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_HOME)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(mainIntent)
    }
}
