package com.example.desafio2

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Activity_menu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val intent = Intent(this, addItemActivity::class.java)
        findViewById<FloatingActionButton>(R.id.btn_addItem).setOnClickListener {
            intent.putExtra("isEdit",false)
            startActivity(intent)
        }


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDestinos)
        //Es la conexion a la base de datos de firebase
        val database = FirebaseDatabase.getInstance().getReference("destinos")

        //Configura le recyclerView para usar un linarlayout
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = DestinoAdapter(emptyList(),intent)
        recyclerView.adapter = adapter

        //permite actulizar la lista ante cualquier cambio en la base da datos
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaDestinos = mutableListOf<Destino>()
                for (data in snapshot.children) {
                    val destino = data.getValue(Destino::class.java)
                    if (destino != null) listaDestinos.add(destino)
                }
                adapter.updateData(listaDestinos)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}

class DestinoAdapter(private var listaDestinos: List<Destino>,intent : Intent) :
    RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

        //Define la vista de cada elemento de la lista
        private val intentE = intent

    class DestinoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDestino: ImageView = view.findViewById(R.id.img_place)
        val tvNombre: TextView = view.findViewById(R.id.tv_name)
        val tvPais: TextView = view.findViewById(R.id.tv_pais)
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio)

        val tvDescripcion: TextView = view.findViewById(R.id.tv_description)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_Delete)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_Edit)
    }

    //Enalaza con el layout modelo para le reycler
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false)
        return DestinoViewHolder(view)
    }

    private fun confirmarEliminacion(context: Activity_menu,id:String?) {
        MaterialAlertDialogBuilder(context)
            .setTitle("¿Eliminar elemento?")
            .setMessage("Esta acción no se puede deshacer. ¿Estás seguro de que quieres borrarlo?")
            .setCancelable(false)

            // Botón de Confirmar
            .setPositiveButton("Eliminar") { dialog, which ->
                val database = FirebaseDatabase.getInstance().getReference("destinos")
                id?.let { id ->
                    database.child(id).removeValue()
                }
            }

            // Botón de Cancelar
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }

            .show()
    }

    //Vincula los datos con la vista, la variable postion indica la posicion del elemento
    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = listaDestinos[position]

        holder.tvNombre.text = destino.nombre
        holder.tvPais.text = destino.pais
        holder.tvPrecio.text = "$${destino.precio}"
        holder.tvDescripcion.text = destino.descripcion

        // Uso de Coil para cargar la imagen desde la URL de Cloudinary
        holder.imgDestino.load(destino.imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.stat_notify_error)
        }

        //boton eliminar
        holder.btnDelete.setOnClickListener {
            confirmarEliminacion(holder.itemView.context as Activity_menu,destino.id)
        }

        holder.btnEdit.setOnClickListener {
            intentE.putExtra("isEdit",true)
            intentE.putExtra("idE", destino.id)
            intentE.putExtra("nombreE", destino.nombre)
            intentE.putExtra("paisE", destino.pais)
            intentE.putExtra("precioE", destino.precio)
            intentE.putExtra("descripcionE", destino.descripcion)
            intentE.putExtra("imageUrlE", destino.imageUrl)
            //Se usa itemView para referenciar y dar contexto del layout del item de la lista
            holder.itemView.context.startActivity(intentE)
        }
    }

    override fun getItemCount(): Int = listaDestinos.size

    fun updateData(newList: List<Destino>) {
        listaDestinos = newList
        notifyDataSetChanged()
    }

}
