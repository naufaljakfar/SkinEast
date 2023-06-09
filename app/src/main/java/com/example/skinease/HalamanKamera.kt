package com.example.skinease

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.skinease.databinding.ActivityHalamanKameraBinding
import com.example.skinease.halaman_penyakit.jerawat
import com.example.skinease.halaman_penyakit.kemerahan
import com.example.skinease.halaman_penyakit.kurap
import com.example.skinease.halaman_penyakit.kusta
import com.example.skinease.halaman_penyakit.kutil
import com.example.skinease.halaman_penyakit.panu
import com.example.skinease.halaman_penyakit.shingles
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

lateinit var handler: Handler
lateinit var handlerThread: HandlerThread
lateinit var cameraManager: CameraManager
lateinit var imageView: ImageView
lateinit var imageView2: ImageView
lateinit var button: Button
lateinit var buttonProses: Button
val  REQUEST_IMAGE_CAPTURE = 100



class HalamanKamera : AppCompatActivity() {

    private lateinit var binding: ActivityHalamanKameraBinding
    private lateinit var currentPhotoPath: String

    private var getFile: File? = null

    companion object {

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    //    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ){
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (!allPermissionsGranted()) {
//                Toast.makeText(
//                    this,
//                    "Tidak mendapatkan permission.",
//                    Toast.LENGTH_SHORT
//                ).show()
//                finish()
//            }
//        }
//    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHalamanKameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        get_permission()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.btnProses.setOnClickListener { uploadImage() }
        binding.btnCamera.setOnClickListener { startTakePhoto() }

        imageView = findViewById(R.id.Image_Save)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("VideoThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)
        button = findViewById(R.id.btn_camera)
        buttonProses = findViewById(R.id.btn_proses)
        imageView2 = findViewById(R.id.Image_Save)
        // buttonProses.visibility = View.GONE


// CARA KALAU PAKE BITMAP
//        button.setOnClickListener{
//            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            intent.resolveActivity(packageManager)
//
//            try {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            }catch (e: ActivityNotFoundException){
//                Toast.makeText(this,"Error:" + e.localizedMessage, Toast.LENGTH_SHORT).show()
//            }
//        }

//        buttonProses.setOnClickListener{
//           // Toast.makeText(this, "Menampilkan hasil gambar...", Toast.LENGTH_SHORT).show()
//            val pindahIntent = Intent(this, HalamanDetail::class.java)
//            startActivity(pindahIntent)
//        }
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@HalamanKamera,
                "com.dicoding.picodiploma.mycamera",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            myFile.let { file ->
//              Silakan gunakan kode ini jika mengalami perubahan rotasi
//              rotateFile(file)
                getFile = file
                binding.ImageSave.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Mendapatkan dimensi tampilan ImageView
            val imageViewWidth = imageView.width
            val imageViewHeight = imageView.height

            // Mendapatkan dimensi gambar yang diambil
            val imageWidth = imageBitmap.width
            val imageHeight = imageBitmap.height

            // Menghitung faktor skala untuk menyesuaikan ukuran gambar dengan tampilan ImageView
            val scaleWidth = imageViewWidth.toFloat() / imageWidth
            val scaleHeight = imageViewHeight.toFloat() / imageHeight
            val scaleFactor = scaleWidth.coerceAtMost(scaleHeight)

            // Mengatur skala gambar
            val scaledBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                (imageWidth * scaleFactor).toInt(),
                (imageHeight * scaleFactor).toInt(),
                true
            )

            val HD_WIDTH = 300
            val HD_HEIGHT = 500
            val hdBitmap = Bitmap.createScaledBitmap(
                scaledBitmap,
                HD_WIDTH,
                HD_HEIGHT,
                true
            )
            imageView.setImageBitmap(hdBitmap)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun get_permission() {
        val permissionLst = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionLst.add(android.Manifest.permission.CAMERA)
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionLst.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionLst.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionLst.size > 0) {
            requestPermissions(permissionLst.toTypedArray(), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                get_permission()
            }
        }
    }

//    override fun onClick(v: View?) {
//
//        if (v!=null){
//            when(v.id){
//                R.id.btn_proses -> {
//                    val pindahIntent = Intent(this, HalamanDetail::class.java)
//                    startActivity(pindahIntent)
//                }
//            }
//        }
//    }

    private fun uploadImage() {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val description =
                "Ini adalah deksripsi gambar".toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestImageFile
            )

            val apiService = ApiConfig().getApiService()
            val uploadImageRequest = apiService.uploadImage(imageMultipart)

            uploadImageRequest.enqueue(object : Callback<FileUploadResponse> {
                override fun onResponse(
                    call: Call<FileUploadResponse>,
                    response: Response<FileUploadResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            if (responseBody.message == "0"){
                                startActivity(Intent(this@HalamanKamera, shingles::class.java))
                            }else if (responseBody.message == "1"){
                                startActivity(Intent(this@HalamanKamera, jerawat::class.java))
                            }else if (responseBody.message == "2"){
                                startActivity(Intent(this@HalamanKamera, kurap::class.java))
                            }else if (responseBody.message == "3"){
                                startActivity(Intent(this@HalamanKamera, kusta::class.java))
                            }else if (responseBody.message == "4"){
                                startActivity(Intent(this@HalamanKamera,kutil::class.java))
                            }else if (responseBody.message == "5"){
                                startActivity(Intent(this@HalamanKamera, panu::class.java))
                            }else if (responseBody.message == "6"){
                                startActivity(Intent(this@HalamanKamera, kemerahan::class.java))
                            }else {
                                Toast.makeText(
                                    this@HalamanKamera,
                                    "Gambar Tidak terdeteksi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@HalamanKamera, response.message(), Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                    Toast.makeText(this@HalamanKamera, t.message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(
                this@HalamanKamera,
                "Silakan masukkan berkas gambar terlebih dahulu.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}