package com.example.desafio2
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class addItemActivity : AppCompatActivity() {

    private var imgUri: Uri? = null //Uniform Resource Identifier(URI)  es una cadena de caracteres única que identifica un recurso
    private lateinit var imageView: ImageView

    private var isEdit=false

    private var Id=""
    private var nombre=""
    private var pais=""
    private var precio:Double?=0.0
    private var descripcion=""
    private var imageUrl=""

    // Crea un objeto de ActivityResultLauncher para manejar la seleccion de imagenes
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imgUri = uri
            imageView.load(uri) // Uso de Coil para mostrar la imagen seleccionada
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.img_selected)
        val btnGetImg: Button = findViewById(R.id.btn_getIMG)
        val ed_name= findViewById<EditText>(R.id.ed_name)
        val spinner: Spinner = findViewById(R.id.countries_spinner)
        val ed_precio = findViewById<EditText>(R.id.ed_precio)
        val ed_descripcion = findViewById<EditText>(R.id.ed_descripcion)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        //Asigna el string array al spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.Countries_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        //si es isedit es true asigna los vloares editar a las variables correspondientes
        isEdit = intent.getBooleanExtra("isEdit", false)
        if(isEdit){
            Id = intent.getStringExtra("idE").toString()
            nombre = intent.getStringExtra("nombreE").toString()
            pais = intent.getStringExtra("paisE").toString()
            precio = intent.getDoubleExtra("precioE", 0.0)
            descripcion = intent.getStringExtra("descripcionE").toString()
            imageUrl = intent.getStringExtra("imageUrlE").toString()
            imgUri= Uri.parse(imageUrl)

            ed_name.setText(nombre)
            val paises = spinner.adapter as ArrayAdapter<String>
            val position = paises.getPosition(pais)
            spinner.setSelection(position)
            ed_precio.setText(precio.toString())
            ed_descripcion.setText(descripcion)
            imageView.load(imageUrl)
        }

        btnGetImg.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btn_registeritem).setOnClickListener {
            nombre = ed_name.text.toString()
            pais = spinner.selectedItem.toString()
            precio = ed_precio.text.toString().toDoubleOrNull()
            descripcion = ed_descripcion.text.toString()

            if (nombre.isEmpty() || precio==null || imgUri == null) {
                Toast.makeText(this, "Por favor complete todos los campos y seleccione una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            subirImagenYRegistrar(nombre, pais, precio, descripcion,progressBar)
        }
    }

    //Utiliza el SDK de cloudinary para subir al imagen selccionada
    // si es subida tiene exito retorna el URL de donde subido la imagen para
    // luego invocar la funcion de registro en firebase

    private fun subirImagenYRegistrar(nombre: String, pais: String, precio: Double?, descripcion: String,progressBar: ProgressBar) {
        if(imgUri!= Uri.parse(imageUrl)){
        val requestId = MediaManager.get().upload(imgUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    progressBar.visibility = ProgressBar.VISIBLE
                    Toast.makeText(applicationContext, "Iniciando subida...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Opcional: mostrar progreso
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    progressBar.visibility = ProgressBar.GONE
                    val imageURL = resultData?.get("secure_url") as? String
                    if (imageURL != null) {
                        registrarDestino(nombre, pais, precio, descripcion, imageURL)
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(applicationContext, "Error al subir imagen: ${error?.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                }
            })
            .dispatch()
            }
        else{
            registrarDestino(nombre, pais, precio, descripcion, imageUrl)
        }
    }

    private fun registrarDestino(nombre: String, pais: String, precio: Double?, descripcion: String, imgUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("destinos")

        var id: String? = null

        if (isEdit){
            id=Id
        }else{
            id = myRef.push().key
        }

        val nuevoDestino = Destino(
            id = id,
            nombre = nombre,
            pais = pais,
            descripcion = descripcion,
            precio = precio,
            imageUrl = imgUrl
        )

        if (id != null) {
            myRef.child(id).setValue(nuevoDestino)
                .addOnSuccessListener {
                    Toast.makeText(this, "Destino guardado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar en Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
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