package com.mohamedrafat.messengerapp.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.constants.AppConstants
import com.mohamedrafat.messengerapp.model.ContentMessages
import com.mohamedrafat.messengerapp.model.User
import com.mohamedrafat.messengerapp.notification.Constants
import com.mohamedrafat.messengerapp.recyclerView.CustomAdapterForMessages
import kotlinx.android.synthetic.main.activity_chatting.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

const val TOPIC = "/topics/myTopic2"
class ChattingActivity : AppCompatActivity() {
    private val TAG = "chattingActivity"
    companion object{
        const val textMessage = "text"
        const val imageMessage = "image"
    }
    private val auth: FirebaseAuth by lazy {  FirebaseAuth.getInstance()  }

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val currentImageRef:StorageReference get() = storageInstance.reference


    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    private val chatChannelsCollectionRef = db.collection("chatChannels")


    //variables
    private lateinit var friendName:String
    private lateinit var friendId:String
    private lateinit var friendImage:String
    private val CAMERA_REQ_CODE = 1
    private val GALLERY_REQ_CODE = 2
    private lateinit var currentChannelId:String


    // this array that cary all message for me and your friend
    private var allMessages = ArrayList<ContentMessages>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // هذا السطر لكي يجعل ال icon الي في status bar باللون الاسود
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }


        setSupportActionBar(chat_main_toolbar)
        supportActionBar!!.title = ""

        friendName = intent.getStringExtra("friendName")!!
        friendImage = intent.getStringExtra("friendImage")!!
        friendId = intent.getStringExtra("uId")!!



        chat_fiend_name_toolbar.text = friendName
        if(friendImage!!.isNotEmpty()){
            Glide.with(this@ChattingActivity)
                .load(storageInstance.getReference(friendImage))
                .into(chat_fiend_image_toolbar)
        }else{
            chat_fiend_image_toolbar.setImageResource(R.drawable.man)
        }





        // this create New chat and send message
        createChatChannel(){channelId ->
            db.collection("Users").document(friendId).get().addOnSuccessListener {
                val user = it.toObject(User::class.java)
                val friendStatus = user!!.online
                if(friendStatus)
                    shape_online_Home.visibility = View.VISIBLE
                else
                    shape_online_Home.visibility = View.INVISIBLE

                if(intent.hasExtra("chatId")){
                    currentChannelId = intent.getStringExtra("chatId")!!
                    getMessages(intent.getStringExtra("chatId")!! , friendStatus) // this function to get all message from firebase
                } else{
                    currentChannelId = channelId
                    getMessages(channelId , friendStatus) // this function to get all message from firebase
                }

            }


            iv_send_message.setOnClickListener {
                val message = et_message.text.trim().toString()
                if(message.isNotEmpty()){
                    val contentMessages = ContentMessages(auth.currentUser!!.uid, friendId ,
                        MainActivity.senderName, friendName , textMessage ,"" ,
                        channelId , message , "", Calendar.getInstance().time )

                    sentMessage(channelId , contentMessages)

                    db.collection("Users")
                        .document(auth.currentUser!!.uid)
                        .collection("sharedChat")
                        .document(friendId).update(mapOf("lastMessage" to contentMessages.messageText ,
                                                      "timeLastMessage" to contentMessages.date))

                    db.collection("Users")
                        .document(friendId)
                        .collection("sharedChat")
                        .document(auth.currentUser!!.uid)
                        .update(mapOf("lastMessage" to contentMessages.messageText ,
                                   "timeLastMessage" to contentMessages.date) )


                    et_message.text.clear()
                    allMessages.clear()


                    // this send notification to friend
                    getToken(contentMessages)

                }else{
                    showToast("Pleas write Message")
                }
            }
        }

        // this to Take image by camera
        iv_takeImage.setOnClickListener {
            cameraCheckPermission()
        }

        // this to select image from gallery
        iv_chooseImage.setOnClickListener {
            galleryCheckPermission()
        }



        iv_back.setOnClickListener {
            finish()
        }

    }


    private fun galleryCheckPermission() {

        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                gallery()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(
                    this@ChattingActivity,
                    "You have denied the storage permission to select image",
                    Toast.LENGTH_SHORT
                ).show()
                showRotationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?) {
                showRotationalDialogForPermission()
            }
        }).onSameThread().check()
    }


    private fun gallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQ_CODE)
    }


    private fun cameraCheckPermission() {
        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE ,
                android.Manifest.permission.CAMERA ).withListener(
                 object : MultiplePermissionsListener{

                     override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                         report?.let {
                             if(report.areAllPermissionsGranted()){
                                 camera()
                             }
                         }
                     }

                     override fun onPermissionRationaleShouldBeShown(
                         p0: MutableList<PermissionRequest>?,
                         p1: PermissionToken?
                     ) {
                         showRotationalDialogForPermission()
                     }

                 } ).onSameThread().check()
    }

    private fun camera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent , CAMERA_REQ_CODE)
    }

    private fun showRotationalDialogForPermission(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions"
                    + "required for this feature. It can be enable under App settings!!!")

            .setPositiveButton("Go TO SETTINGS") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun sentMessage(channelId: String , contentMessages: ContentMessages) {
        chatChannelsCollectionRef.document(channelId).collection("message").add(contentMessages)
        showToast("message sent successfully")
    }


    private fun createChatChannel(onComplete:(channelId:String)->Unit) {
        db.collection("Users")
            .document(auth.currentUser!!.uid)
            .collection("sharedChat")
            .document(friendId)
            .get().addOnSuccessListener {document ->
                if(document.exists()){
                    onComplete(document["channelId"] as String)
                    return@addOnSuccessListener
                } // لو هو كان عامل document هيخرج ومش هيعمل واحد جديد

                val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
                val newChatChannel =db.collection("Users").document()

                // create chat channel in my Friend
                db.collection("Users")
                    .document(friendId)
                    .collection("sharedChat")
                    .document(currentUser)
                    .set(mapOf("channelId" to newChatChannel.id ,
                        "lastMessage" to "" ,
                        "timeLastMessage" to Date() ) )



                // create chat channel at the user
                db.collection("Users")
                    .document(currentUser)
                    .collection("sharedChat")
                    .document(friendId)
                    .set(mapOf("channelId" to newChatChannel.id ,
                               "lastMessage" to "" ,
                               "timeLastMessage" to Date() ) )


                onComplete(newChatChannel.id)

            }
    }


    private fun getMessages(channelId: String , friendStatus:Boolean){
        val query = chatChannelsCollectionRef.document(channelId).collection("message")
            .orderBy("date" , Query.Direction.DESCENDING)

        query.addSnapshotListener { value, error ->
            if(error != null){
                return@addSnapshotListener
            }

            allMessages.clear()
            value!!.documents.forEach { document ->
                val contentMessages = document.toObject(ContentMessages::class.java)
                contentMessages!!.friendPhoto = friendImage


                allMessages.add(contentMessages)
            }
            val adapter = CustomAdapterForMessages(allMessages , this , friendStatus)
            rv_messages.adapter = adapter
            rv_messages.setHasFixedSize(true)
        }
    }


    fun showToast(text:String){
        Toast.makeText(this , text , Toast.LENGTH_LONG).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null ) {
            when (requestCode) {
                CAMERA_REQ_CODE -> {
                    val bitmap = data.extras?.get("data") as Bitmap

                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG , 90 , outputStream)
                    val selectedImageBytes = outputStream.toByteArray()

                    uploadChatImage(selectedImageBytes){pathImage ->
                        val contentMessages = ContentMessages(auth.currentUser!!.uid, friendId ,
                            MainActivity.senderName, friendName , imageMessage ,"" ,
                            currentChannelId , "", pathImage, Calendar.getInstance().time )

                        db.collection("Users")
                            .document(auth.currentUser!!.uid)
                            .collection("sharedChat")
                            .document(friendId).update(mapOf("lastMessage" to "image" ,
                                "timeLastMessage" to contentMessages.date))

                        db.collection("Users")
                            .document(friendId)
                            .collection("sharedChat")
                            .document(auth.currentUser!!.uid)
                            .update(mapOf("lastMessage" to "image" ,
                                "timeLastMessage" to contentMessages.date) )


                        chatChannelsCollectionRef.document(currentChannelId)
                            .collection("message").add(contentMessages)
                    }
                }
                GALLERY_REQ_CODE -> {
                    val selectedImagePath = data.data
                    val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver , selectedImagePath)
                    val outputStream = ByteArrayOutputStream()
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG , 100 , outputStream)
                    val selectedImageBytes = outputStream.toByteArray()

                    uploadChatImage(selectedImageBytes){pathImage ->
                        val contentMessages = ContentMessages(auth.currentUser!!.uid, friendId ,
                            MainActivity.senderName, friendName  , imageMessage ,"" ,
                            currentChannelId , "", pathImage, Calendar.getInstance().time )

                        db.collection("Users")
                            .document(auth.currentUser!!.uid)
                            .collection("sharedChat")
                            .document(friendId).update(mapOf("lastMessage" to "image" ,
                                "timeLastMessage" to contentMessages.date))

                        db.collection("Users")
                            .document(friendId)
                            .collection("sharedChat")
                            .document(auth.currentUser!!.uid)
                            .update(mapOf("lastMessage" to "image" ,
                                "timeLastMessage" to contentMessages.date) )

                        chatChannelsCollectionRef.document(currentChannelId)
                            .collection("message").add(contentMessages)
                    }
                }
            }
            allMessages.clear()
        }

    }

    private fun uploadChatImage(selectedImageBytes: ByteArray , onSuccess:(imagePath:String)->Unit) {
        val ref = currentImageRef.child("${auth.currentUser!!.uid}/chatsImages/${UUID.nameUUIDFromBytes(selectedImageBytes)}")
        ref.putBytes(selectedImageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path) // this send path the image
            showToast("Successfully upload image")
        }.addOnFailureListener{
                showToast("Failure upload image")
            }
    }


    private fun getToken(contentMessages: ContentMessages){
        db.collection("Users")
            .document(friendId).get().addOnSuccessListener {
                val user = it.toObject(User::class.java)
//                Toast.makeText(activity, ""+user!!.token , Toast.LENGTH_SHORT).show()

                val to = JSONObject()
                val data = JSONObject()

                data.put("hisId", auth.currentUser!!.uid)
                data.put("hisImage", user!!.profileImage)
                data.put("title", contentMessages.senderName)
                if(contentMessages.messageText!=""){
                    data.put("message", contentMessages.messageText)
                }else{
                    data.put("message", "Sent You Image...")
                }
                data.put("chatId", contentMessages.messageId)


                to.put("to", user.token)
                to.put("data", data)

                sendNotification(to)
            }
    }


    private fun sendNotification(to: JSONObject) {
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            AppConstants.NOTIFICATION_URL,
            to,
            Response.Listener { response: JSONObject ->
                Log.d("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Log.d("TAG", "onError: $it")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val map: MutableMap<String, String> = HashMap()

                map["Authorization"] = "key=" + Constants.SERVER_KEY
                map["Content-type"] = "application/json"

                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        val requestQueue = Volley.newRequestQueue(this)
        request.retryPolicy = DefaultRetryPolicy(
            80000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(request)

    }


}