package com.mohamedrafat.messengerapp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.model.User
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

class SignUpActivity : AppCompatActivity() , TextWatcher{
    private val auth:FirebaseAuth by lazy {  FirebaseAuth.getInstance()  }
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    private val currentUserRef: DocumentReference get() =
        db.document("Users/${auth.currentUser!!.uid.toString()}")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // هذا السطر لكي يجعل ال icon الي في status bar باللون الاسود
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.statusColor, this.theme)
        }else{
            window.statusBarColor = Color.WHITE
        }

        et_userName_signUp.addTextChangedListener(this)
        et_phoneOrEmail_signUp.addTextChangedListener(this)
        et_password_signUp.addTextChangedListener(this)


        btn_signUp.setOnClickListener {
            val userName = et_userName_signUp.text.toString().trim()
            val email = et_phoneOrEmail_signUp.text.toString().trim()
            val password = et_password_signUp.text.toString().trim()

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                et_phoneOrEmail_signUp.error = "Please Enter Valid E-mail"
                et_phoneOrEmail_signUp.requestFocus()
                return@setOnClickListener //setOnClickListener ديه معناها انه لو الشرط ده اتنفذ .. كده هو هيخرج من ال
            }

            if(password.length < 6){
                et_password_signUp.error = "Please Enter 6 Char Required "
                et_password_signUp.requestFocus()
                return@setOnClickListener
            }

            createNewAccount(userName, email, password)
        }


    }



    private fun createNewAccount(userName:String, email:String, password:String){
        progressBar_signUp.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }

                if (task.result != null) {
                    val token: String = task.result
                    showToast("Register Is Successfully")
                    val newUser = User(auth.currentUser!!.uid , userName , "" , token , true)
                    currentUserRef.set(newUser)
                }
            }

            progressBar_signUp.visibility = View.INVISIBLE
            val intent = Intent(this , MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }.addOnFailureListener {
            progressBar_signUp.visibility = View.INVISIBLE
            showToast("Register Is Failed : "+it.message)
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun afterTextChanged(p0: Editable?) {
        btn_signUp.isEnabled =
                et_userName_signUp.text!!.trim().isNotEmpty() &&
                et_phoneOrEmail_signUp.text!!.trim().isNotEmpty() &&
                et_password_signUp.text!!.trim().isNotEmpty()
    }


    fun showToast(text:String){
        Toast.makeText(this , text , Toast.LENGTH_LONG).show()
    }
}