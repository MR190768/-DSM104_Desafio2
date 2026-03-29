package com.example.desafio2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth= FirebaseAuth.getInstance()


        findViewById<Button>(R.id.btn_signIn).setOnClickListener {
            val intent = Intent(this, Activitity_Registro::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email=findViewById<EditText>(R.id.ed_email).text.toString()
            val password=findViewById<EditText>(R.id.ed_password).text.toString()
            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Por favor ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(email,password)
        }
    }

    private fun loginUser(email: String, password: String){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    val intent = Intent(this, Activity_menu::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener{e->
                Toast.makeText(
                    applicationContext,
                    e.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}