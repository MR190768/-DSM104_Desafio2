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

        findViewById<FloatingActionButton>(R.id.btn_addItem).setOnClickListener {
            val intent = Intent(this, addItemActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDestinos)
        val database = FirebaseDatabase.getInstance().getReference("destinos")

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = DestinoAdapter(emptyList())
        recyclerView.adapter = adapter

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

class DestinoAdapter(private var listaDestinos: List<Destino>) :
    RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    class DestinoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDestino: ImageView = view.findViewById(R.id.img_place)
        val tvNombre: TextView = view.findViewById(R.id.tv_name)
        val tvPais: TextView = view.findViewById(R.id.tv_pais)
        val tvPrecio: TextView = view.findViewById(R.id.tv_precio)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_Delete)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_Edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false)
        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = listaDestinos[position]

        holder.tvNombre.text = destino.nombre
        holder.tvPais.text = destino.pais
        holder.tvPrecio.text = "$${destino.precio}"

        // Uso de Coil para cargar la imagen desde la URL de Cloudinary
        holder.imgDestino.load(destino.imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.stat_notify_error)
        }

        holder.btnDelete.setOnClickListener {
            val database = FirebaseDatabase.getInstance().getReference("destinos")
            destino.id?.let { id ->
                database.child(id).removeValue()
            }
        }
    }

    override fun getItemCount(): Int = listaDestinos.size

    fun updateData(newList: List<Destino>) {
        listaDestinos = newList
        notifyDataSetChanged()
    }
}
