package com.samrez.sp_test_case.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.samrez.sp_test_case.databinding.ActivityPictureAddAndResultBinding

class PictureAddAndResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPictureAddAndResultBinding
    private lateinit var scannedBitmap: Bitmap
    private var bitmapState: Bitmap? = null
    private lateinit var textResult: String
    private lateinit var fused: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPictureAddAndResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fused = LocationServices.getFusedLocationProviderClient(this@PictureAddAndResultActivity)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        initListener()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun initListener() {
        binding.tvResultCapture.clearFocus()
        binding.btnTakePicture.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Choose action:")
            val pictureDialogItem = arrayOf(
                "Take from Gallery",
                "Take from Camera"
            )
            pictureDialog.setItems(pictureDialogItem) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            pictureDialog.show()
        }

        binding.btnEditResult.setOnClickListener {
            val intent = Intent(this, EditTextResultActivity::class.java)
            intent.putExtra("textResult", textResult)
            launcherIntentEditResult.launch(intent)
        }
    }

    private val launcherIntentEditResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            textResult = it.data?.extras?.get("textResult").toString()
            binding.tvResultCapture.text = textResult
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        launcherIntentCamera.launch(intent)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val bitmap = it.data?.extras?.get("data") as Bitmap
            scannedBitmap = bitmap
            bitmapState = bitmap
            Glide.with(this).load(bitmap).into(binding.ivCapture)
            detectText(scannedBitmap)
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data?.data != null) {
                scannedBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, result.data?.data)
                bitmapState =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, result.data?.data)
            }
            Glide.with(this).load(result.data?.data).into(binding.ivCapture)

            detectText(scannedBitmap)
        }
    }

    private fun detectText(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = imageFromBitmap(bitmap)

        recognizer.process(image)
            .addOnSuccessListener {
                binding.tvResultCapture.text = it.text
                textResult = it.text
                Toast.makeText(this, "Success detect text", Toast.LENGTH_SHORT).show()

                if (it.text == "") {
                    binding.btnEditResult.visibility = View.GONE
                    binding.tvResultCapture.text = "-"
                } else {
                    binding.btnEditResult.visibility = View.VISIBLE
                }
                binding.tvLocation.visibility = View.VISIBLE
                getLocation()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed detect text", Toast.LENGTH_SHORT).show()
                binding.btnEditResult.visibility = View.GONE
            }
    }

    private fun getLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val location = fused.lastLocation
        location.addOnSuccessListener {
            if (it != null) {
                val latitude: Double = it.latitude
                val longitude: Double = it.longitude

                val latitudePlaza: Double = -7.1555178
                val longitudePlaza: Double = 107.2298156

                val results = FloatArray(1)
                Location.distanceBetween(latitude,longitude,latitudePlaza,longitudePlaza,results)
                val distance = results[0]
                val kilometer: Int = (distance/1000).toInt()

                val duration: Double = (kilometer/70).toDouble()

                binding.tvLocation.text = "Jarak dari tempat anda ke Plaza Indonesia Jakarta : ${kilometer} Km, Waktu yang dibutuhkan ${duration} jam"
            }
        }
    }

    private fun imageFromBitmap(bitmap: Bitmap): InputImage {
        return InputImage.fromBitmap(bitmap, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "No have permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}