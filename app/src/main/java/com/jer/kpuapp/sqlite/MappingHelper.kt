package com.jer.mynoteapps.helper

import android.database.Cursor
import com.jer.mynoteapps.db.DatabaseContract
import com.jer.mynoteapps.entity.Note

object MappingHelper {

    fun mapCursorToArrayList(notesCursor: Cursor?): ArrayList<Note> {
        val notesList = ArrayList<Note>()
        notesCursor?.apply {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID))
                val nik = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.NIK))
                val nama = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.NAMA))
                val hp = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.HP))
                val kelamin = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.KELAMIN))
                val tanggal = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.TANGGAL))
                val alamat = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.ALAMAT))
                val gambar = getBlob(getColumnIndexOrThrow(DatabaseContract.NoteColumns.GAMBAR))
                notesList.add(Note(id, nik, nama, hp, kelamin, tanggal, alamat, gambar))
            }
        }
        return notesList
    }

}


