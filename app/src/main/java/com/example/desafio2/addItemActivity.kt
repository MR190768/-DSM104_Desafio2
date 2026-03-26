package com.example.desafio2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast

class addItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val spinner: Spinner = findViewById(R.id.countries_spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.Countries_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        findViewById<Button>(R.id.btn_registeritem).setOnClickListener {
            val nombre = findViewById<EditText>(R.id.editTextText).text.toString()
            val pais = findViewById<Spinner>(R.id.countries_spinner).selectedItem.toString()
            val precio= findViewById<EditText>(R.id.editTextNumberDecimal).text.toString().toDouble()
            val descripcion = findViewById<EditText>(R.id.editTextTextMultiLine).text.toString()
            registrarDestino(nombre, pais, precio, descripcion)

        }
    }

    fun registrarDestino(nombre: String, pais: String, precio: Double, descripcion: String) {

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("destinos")

        val id = myRef.push().key

        val nuevoDestino = Destino(
            id = id,
            nombre = nombre,
            pais = pais,
            descripcion = descripcion,
            precio = precio,
            imageUrl = null
        )

        if (id != null) {
            myRef.child(id).setValue(nuevoDestino)
                .addOnSuccessListener {

                    Toast.makeText(this, "Destino guardado con éxito", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->

                    Toast.makeText(this, "error ${e.toString()}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

data class Destino(
    val id: String? = null,
    val nombre: String? = "",
    val pais: String? = "",
    val precio: Double? = 0.0,
    val descripcion: String? = "",
    val imageUrl: String? = ""
)