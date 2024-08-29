package com.jer.kpuapp.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jer.kpuapp.R
import com.jer.kpuapp.databinding.ActivityFormEntryBinding
import com.jer.mynoteapps.db.DatabaseContract
import com.jer.mynoteapps.db.NoteHelper
import com.jer.mynoteapps.entity.Note
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FormEntryActivity : AppCompatActivity(), View.OnClickListener {


    private var isEdit = false
    private var note: Note? = null
    private var position: Int = 0
    private var kelamin: String = ""
    private var alamat: String = ""
    private val calendar = Calendar.getInstance()
    private lateinit var noteHelper: NoteHelper

    private lateinit var binding: ActivityFormEntryBinding
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
        const val REQUEST_IMAGE_CAPTURE = 1
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Izin kamera diberikan
                Toast.makeText(this, "Izin kamera diberikan", Toast.LENGTH_SHORT).show()

            } else {
                // Izin kamera ditolak
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFormEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)
        binding.btnSubmit.setOnClickListener(this)


        noteHelper = NoteHelper.getInstance(applicationContext)
        noteHelper.open()
        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }
        val actionBarTitle: String
        val btnTitle: String

        binding.btnCekLokasi.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        binding.ivCalendar.setOnClickListener {
            showCalendar()
        }

        binding.radioLk.setOnClickListener {
            sharedPreferences.edit().putString("L", "Laki-laki").apply()
        }

        binding.radioPr.setOnClickListener {
            sharedPreferences.edit().putString("P", "Perempuan").apply()
        }


        val l = sharedPreferences.getString("L", "Laki-laki")
        val p = sharedPreferences.getString("P", "Perempuan")


        binding.btnSetLokasi.setOnClickListener {
            val address = sharedPreferences.getString("alamat", "jonggol").toString()
            binding.edtAlamat.setText(address)

            alamat = address
        }


        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"
            note?.let {
                binding.edtNik.setText(it.nik)
                binding.edtNama.setText(it.nama)
                binding.edtNoHp.setText(it.hp)
//                binding.radioLk.setText(it.kelamin)
                binding.edtTgl.setText(it.tanggal)
                binding.edtAlamat.setText(it.alamat)
            }
        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"
        }
        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.btnSubmit.text = btnTitle

        checkCameraPermissionAndOpenCamera()

        binding.btnCamera.setOnClickListener {
            openCamera()
        }

        binding.btnUpload.setOnClickListener {
            openGallery()
        }




    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_submit) {
            val nik = binding.tvNik.text.toString().trim()
            val nama = binding.edtNama.text.toString().trim()
            val hp = binding.edtNoHp.text.toString().trim()
            if (binding.radioLk.isChecked) {
                kelamin = binding.radioLk.text.toString().trim()
            } else if (binding.radioPr.isChecked) {
                kelamin = binding.radioPr.text.toString().trim()
            }
            val tanggal = binding.edtTgl.text.toString().trim()


//            if (nik.isEmpty()) {
//                binding.edtNik.error = "NIK tidak boleh kosong"
//                return
//            }
            note?.nik = nik
            note?.nama = nama
            note?.hp = hp
            note?.kelamin = kelamin
            note?.tanggal = tanggal
            note?.alamat = alamat
            val intent = Intent()
            intent.putExtra(EXTRA_NOTE, note)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.NoteColumns.NIK, nik)
            values.put(DatabaseContract.NoteColumns.NAMA, nama)
            values.put(DatabaseContract.NoteColumns.HP, hp)
            values.put(DatabaseContract.NoteColumns.KELAMIN, kelamin)
            values.put(DatabaseContract.NoteColumns.TANGGAL, tanggal)
            values.put(DatabaseContract.NoteColumns.ALAMAT, alamat)
            binding.imageRumah.drawable?.toBitmap()?.let {
                val imageByteArray = convertBitmapToByteArray(it)
                values.put(DatabaseContract.NoteColumns.GAMBAR, imageByteArray)
            }

            if (isEdit) {
                val result = noteHelper.update(note?.id.toString(), values).toLong()
                if (result > 0) {
                    setResult(RESULT_UPDATE, intent)
                    finish()
                } else {
                    Toast.makeText(this@FormEntryActivity, "Gagal update data", Toast.LENGTH_SHORT).show()
                }
            } else {

                val result = noteHelper.insert(values)
                if (result > 0) {
                    note?.id = result.toInt()
                    setResult(RESULT_ADD, intent)
                    finish()
                } else {
                    Toast.makeText(this@FormEntryActivity, "Gagal menambah data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    private fun convertUriToByteArray(uri: Uri): ByteArray? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap?.let { convertBitmapToByteArray(it) }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getImageFromDatabase(byteArray: ByteArray?) {
        byteArray?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.imageRumah.setImageBitmap(bitmap)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imageRumah.setImageBitmap(imageBitmap)
            val imageByteArray = convertBitmapToByteArray(imageBitmap)
            getImageFromDatabase(imageByteArray)
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Izin sudah diberikan, langsung buka kamera
//                openCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Tampilkan alasan mengapa izin dibutuhkan dan kemudian minta izin
                Toast.makeText(
                    this,
                    "Izin kamera diperlukan untuk mengambil gambar",
                    Toast.LENGTH_SHORT
                ).show()
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                // Minta izin langsung
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }


    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncherGallery.launch(galleryIntent)
    }

    private var resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                val imageUri = data!!.data!!
                binding.imageRumah.setImageURI(imageUri)
                convertUriToByteArray(imageUri)
//                val imagePath = convertMediaUriToPath(imageUri)
//                val imgFile = File(imagePath)
//                scanImageQRCode(imgFile)
            } else {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
            }
        }

    private fun showCalendar() {
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.CustomDatePickerDialogTheme,
            { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.edtTgl.setText(formattedDate)

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.setOnShowListener {
            val positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            val negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)

            val positiveColor = ContextCompat.getColor(this, R.color.blue)
            val negativeColor = ContextCompat.getColor(this, R.color.blue)

            positiveButton.setTextColor(positiveColor)
            negativeButton.setTextColor(negativeColor)
        }

        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

//    override fun onBackPressed() {
//        super.onBackPressed()
//        showAlertDialog(ALERT_DIALOG_CLOSE)
//    }

//    override fun onBackPressed() {
//        // Avoid showing dialog if the activity is finishing
//        if (!isFinishing) {
//            showAlertDialog()
//        } else {
//            super.onBackPressed()
//        }
//    }


    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String
        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Note"
        }
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = noteHelper.deleteById(note?.id.toString()).toLong()
                    if (result > 0) {
                        val intent = Intent()
                        intent.putExtra(EXTRA_POSITION, position)
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(this@FormEntryActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }



}