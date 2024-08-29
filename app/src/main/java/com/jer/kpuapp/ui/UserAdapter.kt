package com.jer.kpuapp.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jer.kpuapp.R
import com.jer.kpuapp.databinding.ItemViewBinding
import com.jer.mynoteapps.entity.Note

class UserAdapter(private val onItemClickCallback: OnItemClickCallback): RecyclerView.Adapter<UserAdapter.UserViewHolder>()  {

    var listNotes = ArrayList<Note>()
        set(listNotes) {
            if (listNotes.size > 0) {
                this.listNotes.clear()
            }

            this.listNotes.addAll(listNotes)
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.UserViewHolder, position: Int) {
        holder.bind(listNotes[position])
    }

    override fun getItemCount(): Int = this.listNotes.size

    fun addItem(note: Note) {
        this.listNotes.add(note)
        notifyItemInserted(this.listNotes.size - 1)
    }
    fun updateItem(position: Int, note: Note) {
        this.listNotes[position] = note
        notifyItemChanged(position, note)
    }
    fun removeItem(position: Int) {
        this.listNotes.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, this.listNotes.size)
    }


    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemViewBinding.bind(itemView)
        fun bind(note: Note) {
            binding.tvHasilNik.text = note.nik
            binding.tvHasilNama.text = note.nama
            binding.tvHasilHp.text = note.hp
            binding.tvHasilKelamin.text = note.kelamin
            binding.tvHasilTanggal.text = note.tanggal
            binding.tvHasilAlamat.text = note.alamat
            binding.cvItemNote.setOnClickListener {
                onItemClickCallback.onItemClicked(note, adapterPosition)
            }

            val byteArray: ByteArray? = note.gambar
            byteArray?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                binding.imageRumah.setImageBitmap(bitmap)
            }

        }
    }


    interface OnItemClickCallback {
        fun onItemClicked(selectedNote: Note?, position: Int?)
    }
}