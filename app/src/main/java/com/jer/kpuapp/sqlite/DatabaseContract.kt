package com.jer.mynoteapps.db

import android.provider.BaseColumns

internal class DatabaseContract {

    internal class NoteColumns : BaseColumns {
        companion object {
            const val TABLE_NAME = "note"
            const val _ID = "_id"
            const val NIK = "nik"
            const val NAMA = "nama"
            const val HP = "hp"
            const val KELAMIN = "kelamin"
            const val TANGGAL = "tanggal"
            const val ALAMAT = "alamat"
            const val GAMBAR = "gambar"
        }
    }

}