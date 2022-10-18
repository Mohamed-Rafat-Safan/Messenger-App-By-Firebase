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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mohamedrafat.messengerapp.R
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() , TextWatcher {
    private val auth: FirebaseAuth by lazy {  FirebaseAuth.getInstance()  }
    private val db: FirebaseFirestore by lazy {
        Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // هذا السطر لكي يجعل ال icon الي في status bar باللون الاسود
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.statusColor, this.theme)
        }else{
            window.statusBarColor = Color.WHITE
        }


        et_phoneOrEmail_signIn.addTextChangedListener(this)
        et_password_signIn.addTextChangedListener(this)

        btn_login.setOnClickListener {
            val email = et_phoneOrEmail_signIn.text.toString().trim()
            val password = et_password_signIn.text.toString().trim()

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                et_phoneOrEmail_signIn.error = "Please Enter Valid E-mail"
                et_phoneOrEmail_signIn.requestFocus()
                return@setOnClickListener //setOnClickListener ديه معناها انه لو الشرط ده اتنفذ .. كده هو هيخرج من ال
            }
            if(password.length < 6){
                et_password_signIn.error = "Please Enter 6 Char Required "
                et_password_signIn.requestFocus()
                return@setOnClickListener
            }

            loginUser(email, password)

        }

        tv_signUp.setOnClickListener {
            startActivity(Intent(this , SignUpActivity::class.java))
        }

    }

    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null){
            startActivity(Intent(this , MainActivity::class.java))
        }
    }


    private fun loginUser(email:String, password:String){
        progressBar_signIn.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {

            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }

                if (task.result != null) {
                    val token: String = task.result
                    db.collection("Users").
                            document(auth.currentUser!!.uid).
                            update(mapOf("token" to token))
                }
            }

            progressBar_signIn.visibility = View.INVISIBLE
            showToast("Login Is Successfully")
            val intent = Intent(this , MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }.addOnFailureListener {
            progressBar_signIn.visibility = View.INVISIBLE
            showToast("Login Is Failed : "+it.message)
        }

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun afterTextChanged(p0: Editable?) {
        btn_login.isEnabled =
                    et_phoneOrEmail_signIn.text!!.trim().isNotEmpty() &&
                    et_password_signIn.text!!.trim().isNotEmpty()
    }

    fun showToast(text:String){
        Toast.makeText(this , text , Toast.LENGTH_LONG).show()
    }
}