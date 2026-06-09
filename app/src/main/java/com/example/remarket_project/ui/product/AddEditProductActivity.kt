package com.example.remarket_project.ui.product

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.remarket_project.databinding.ActivityAddEditProductBinding
import com.example.remarket_project.viewmodel.AddEditProductViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding

    private val viewModel: AddEditProductViewModel by viewModels()

    private val existingImageUrls = mutableListOf<String>() // 기존 사진 URL
    private val selectedImageUris = mutableListOf<Uri>()    // 새로 선택한 사진
    private lateinit var originalImageUrls: List<String>    // 변경 감지용 원본 스냅샷

    private val categories = listOf("의류", "전자기기", "도서", "운동용품", "기타")

    private val editProductId: Int by lazy { intent.getIntExtra("productId", -1) }
    private val isEditMode get() = editProductId != -1

    // 갤러리에서 최대 5장 선택
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remaining = 5 - (existingImageUrls.size + selectedImageUris.size)
            if (remaining > 0) {
                selectedImageUris.addAll(uris.take(remaining))
                updateImagePreview()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) "상품 수정" else "상품 등록"

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(categoryAdapter)

        if (isEditMode) {
            binding.etTitle.setText(intent.getStringExtra("title"))
            binding.etPrice.setText(intent.getIntExtra("price", 0).toString())
            binding.actvCategory.setText(intent.getStringExtra("category"), false)
            binding.etDescription.setText(intent.getStringExtra("description"))
            intent.getStringArrayListExtra("existingImageUrls")?.let {
                existingImageUrls.addAll(it)
            }
        }
        originalImageUrls = existingImageUrls.toList() // 나중에 변경 여부 비교용

        updateImagePreview()
        binding.ivAddImage.setOnClickListener { openImagePicker() }

        binding.btnSubmit.text = if (isEditMode) "수정하기" else "등록하기"
        binding.btnSubmit.setOnClickListener { submit() }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    private fun openImagePicker() {
        val remaining = 5 - (existingImageUrls.size + selectedImageUris.size)
        if (remaining <= 0) {
            Toast.makeText(this, "최대 5장까지 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        imagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // 기존 사진 + 새 사진 합쳐서 미리보기 갱신
    private fun updateImagePreview() {
        val addButton = binding.ivAddImage
        binding.layoutImages.removeAllViews()
        binding.layoutImages.addView(addButton)

        existingImageUrls.forEachIndexed { index, url ->
            addThumbnail(
                load = { iv -> Glide.with(this).load(url).centerCrop().into(iv) },
                onDelete = { existingImageUrls.removeAt(index); updateImagePreview() }
            )
        }

        selectedImageUris.forEachIndexed { index, uri ->
            addThumbnail(
                load = { iv -> Glide.with(this).load(uri).centerCrop().into(iv) },
                onDelete = { selectedImageUris.removeAt(index); updateImagePreview() }
            )
        }

        val total = existingImageUrls.size + selectedImageUris.size
        addButton.visibility = if (total >= 5) View.GONE else View.VISIBLE
        binding.toolbar.subtitle = if (total > 0) "${total}/5장" else ""
    }

    private fun addThumbnail(load: (ImageView) -> Unit, onDelete: () -> Unit) {
        val wrapper = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(90.dpToPx(), 90.dpToPx())
                .also { it.marginEnd = 8.dpToPx() }
        }
        val imageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        load(imageView)

        val deleteBtn = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(24.dpToPx(), 24.dpToPx()).also {
                it.gravity = android.view.Gravity.TOP or android.view.Gravity.END
            }
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(0xAAFFFFFF.toInt())
            setOnClickListener { onDelete() }
        }

        wrapper.addView(imageView)
        wrapper.addView(deleteBtn)
        binding.layoutImages.addView(wrapper, binding.layoutImages.childCount - 1)
    }

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).toInt()

    private fun submit() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        val priceStr = binding.etPrice.text?.toString()?.trim() ?: ""
        val category = binding.actvCategory.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim()

        if (title.isEmpty()) { binding.etTitle.error = "제목을 입력해주세요"; return }
        if (priceStr.isEmpty()) { binding.etPrice.error = "가격을 입력해주세요"; return }
        if (category.isEmpty() || category !in categories) {
            Toast.makeText(this, "카테고리를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        val price = priceStr.toIntOrNull() ?: run {
            binding.etPrice.error = "올바른 가격을 입력해주세요"
            return
        }

        binding.btnSubmit.isEnabled = false

        // 사진이 변경된 경우에만 파일 전송, 변경 없으면 빈 리스트 (서버가 기존 사진 유지)
        val imageChanged = selectedImageUris.isNotEmpty() || existingImageUrls != originalImageUrls

        lifecycleScope.launch {
            val imageFiles: List<File> = if (imageChanged) {
                withContext(Dispatchers.IO) {
                    // 남아있는 기존 사진은 URL에서 다운로드해 파일로 변환
                    val fromExisting = existingImageUrls.mapNotNull { urlToFile(it) }
                    val fromNew = selectedImageUris.mapNotNull { uriToFile(it) }
                    fromExisting + fromNew
                }
            } else emptyList()

            if (isEditMode) {
                viewModel.editProduct(editProductId, title, description, price, category, imageFiles) {
                    Toast.makeText(this@AddEditProductActivity, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                viewModel.createProduct(title, description, price, category, imageFiles) {
                    Toast.makeText(this@AddEditProductActivity, "등록되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            if (!loading) binding.btnSubmit.isEnabled = true
        }
    }

    // URI를 서버 전송용 파일로 변환
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val ext = contentResolver.getType(uri)?.substringAfter("/") ?: "jpg"
            val tempFile = File.createTempFile("upload_", ".$ext", cacheDir)
            FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
            tempFile
        } catch (e: Exception) { null }
    }

    // 기존 사진 URL에서 다운로드해서 파일로 변환
    private fun urlToFile(url: String): File? {
        return try {
            val ext = url.substringAfterLast(".").take(4).ifEmpty { "jpg" }
            val tempFile = File.createTempFile("existing_", ".$ext", cacheDir)
            FileOutputStream(tempFile).use { out ->
                URL(url).openStream().use { input -> input.copyTo(out) }
            }
            tempFile
        } catch (e: Exception) { null }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
