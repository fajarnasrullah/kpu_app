package com.jer.mynoteapps.entity

import android.os.Parcelable
import com.jer.mynoteapps.db.DatabaseContract
import kotlinx.parcelize.Parcelize
import java.sql.Blob


@Parcelize
data class Note(
    var id: Int = 0,
    var nik: String? = null,
    var nama: String? = null,
    var hp: String? = null,
    var kelamin: String? = null,
    var tanggal: String? = null,
    var alamat: String? = null,
    var gambar: ByteArray? = null
) : Parcelable
