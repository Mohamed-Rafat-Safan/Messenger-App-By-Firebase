package com.mohamedrafat.messengerapp.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.model.User
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy {  FirebaseAuth.getInstance()  }

    companion object{
        const val  RC_SELECT_IMAGE = 1
    }
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val currentUserStorageRef: StorageReference get() =
        storageInstance.reference.child(FirebaseAuth.getInstance().currentUser!!.uid.toString())

    private val db:FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private val currentUserDocRef:DocumentReference get() =
        db.document("Users/${FirebaseAuth.getInstance().currentUser!!.uid.toString()}")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // هذا السطر لكي يجعل ال icon الي في status bar باللون الاسود
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }

        setSupportActionBar(profile_toolbar)
        supportActionBar!!.title = "Me"
        // السطرين دول علشان اظهر السهم الي موجود (سهم الرجوع)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        btn_signOut.setOnClickListener {
            db.collection("Users").document(auth.currentUser!!.uid).update("online", false)

            auth.signOut()
            val intent = Intent(this@ProfileActivity , SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        setDataInProfile()

    }

    // this put data (image and userName ) in activity
    fun setDataInProfile() {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject<User>()
            tv_userName.text = user!!.userName

            if(user.profileImage.isNotEmpty()){
                Glide.with(this@ProfileActivity)
                    .load(storageInstance.getReference(user.profileImage))
                    .placeholder(R.drawable.user)
                    .into(profile_image)
            }
        }

    }


    // هذه الدله علشان لما اضغط علي السهم يعمل finish for activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home ->{
//                val intent = Intent(this , MainActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }



    fun onClickImageProfile(view: View) {
        val myIntentImage = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES , arrayOf("image/jpeg" , "image/png"))
        }
        startActivityForResult(myIntentImage , RC_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RC_SELECT_IMAGE && resultCode== RESULT_OK){
            profile_progressBar.visibility = View.VISIBLE

            profile_image.setImageURI(data!!.data)

            val selectedImagePath = data.data
            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver , selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG , 20 , outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            // this method to upload image to storage in firebase
            // and after uploaded take location of image and add to Object User in fireStore
            uploadProfileImage(selectedImageBytes){ path ->
                currentUserDocRef.update("profileImage" , path).addOnSuccessListener {
                    Toast.makeText(this , "User Is Updated" , Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Toast.makeText(this , "User Failure Updated : ${it.message}" , Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun uploadProfileImage(selectedImageBytes: ByteArray , onSuccess:(imagePath:String)->Unit) {
        val ref = currentUserStorageRef.child("ProfileImages/${UUID.nameUUIDFromBytes(selectedImageBytes)}")
        ref.putBytes(selectedImageBytes).addOnSuccessListener {
            profile_progressBar.visibility = View.INVISIBLE
            onSuccess(ref.path) // هنا ارسلت ال location بتاع الصوره الي في ال  firebase Storage
            Toast.makeText(this , "Successfully Upload Image" , Toast.LENGTH_LONG).show()
        }.addOnFailureListener{
            Toast.makeText(this , "Failure Upload Image : ${it.message}" , Toast.LENGTH_LONG).show()
        }
    }


}


