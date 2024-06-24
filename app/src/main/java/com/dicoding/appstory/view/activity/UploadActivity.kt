package com.dicoding.appstory.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dicoding.appstory.R
import com.dicoding.appstory.data.result.ResultState
import com.dicoding.appstory.databinding.ActivityUploadBinding
import com.dicoding.appstory.utils.getImageUri
import com.dicoding.appstory.utils.reduceFileImage
import com.dicoding.appstory.utils.uriToFile
import com.dicoding.appstory.view.viewmodel.MainViewModel
import com.dicoding.appstory.view.viewmodel.ViewModelFactory

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var selectedImageUri: Uri? = null
    private val viewModel: MainViewModel by viewModels { ViewModelFactory.getInstance(this) }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            showToast(getString(R.string.permission_granted))
        } else {
            showToast(getString(R.string.permission_denied))
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            displaySelectedImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            displaySelectedImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.add_story)
            setDisplayHomeAsUpEnabled(true)
        }

        if (!hasCameraPermission()) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q &&
            (!hasWriteExternalStoragePermission() || !hasReadExternalStoragePermission())) {
            requestStoragePermissions()
        }

        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnUpload.setOnClickListener { uploadStory() }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasWriteExternalStoragePermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasReadExternalStoragePermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_CODE_STORAGE_PERMISSION
        )
    }

    private fun openGallery() {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openCamera() {
        selectedImageUri = getImageUri(this)
        cameraLauncher.launch(selectedImageUri!!)
    }

    private fun displaySelectedImage() {
        selectedImageUri?.let { uri ->
            Log.d("Image URI", "displaySelectedImage: $uri")
            binding.ivPriview.setImageURI(uri)
        }
    }

    private fun uploadStory() {
        selectedImageUri?.let { uri ->
            uriToFile(uri, this)?.let { imageFile ->
                val compressedFile = imageFile.reduceFileImage()
                Log.d("Image File", "uploadStory: ${compressedFile.path}")
                val description = binding.descEditText.text.toString()
                viewModel.retrieveUserSession().observe(this) { session ->
                    val token = session.token
                    viewModel.submitImage(token, compressedFile, description).observe(this) { result ->
                        when (result) {
                            is ResultState.Loading -> setLoadingState(true)
                            is ResultState.Success -> handleUploadSuccess(result.data.message)
                            is ResultState.Error -> handleUploadError(result.exception.message ?: "An error occurred")
                        }
                    }
                }
            } ?: showToast(getString(R.string.empty_image_warning))
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleUploadSuccess(message: String) {
        showToast(message)
        setLoadingState(false)
        navigateToMain()
    }

    private fun handleUploadError(error: String) {
        showToast(error)
        setLoadingState(false)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }
}
