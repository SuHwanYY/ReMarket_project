package com.example.remarket_project.ui.product

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.remarket_project.R
import com.example.remarket_project.auth.SessionManager
import com.example.remarket_project.databinding.FragmentProductDetailBinding
import com.example.remarket_project.network.dto.ProductItem
import com.example.remarket_project.viewmodel.ProductDetailViewModel

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()

    // 멤버로 관리해야 unregister 할 수 있음
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val total = binding.viewPagerImages.adapter?.itemCount ?: 1
            binding.tvImageCounter.text = "${position + 1}/$total"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getInt("productId") ?: return

        // 이전 상품 데이터 지우고 새로 로드
        viewModel.clearProduct()
        viewModel.loadProduct(productId)

        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let { bind(it) }
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                viewModel.clearNavigateBack()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.actionSuccess.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearActionSuccess()
            }
        }
    }

    private fun bind(product: ProductItem) {
        binding.tvTitle.text = product.title
        binding.tvPrice.text = String.format("%,d원", product.price)
        binding.tvCategory.text = product.category
        binding.tvDescription.text = product.description ?: "설명 없음"
        binding.tvSellerNickname.text = product.seller.nickname
        binding.tvSellerRegion.text = product.seller.region
        binding.tvWishCount.text = product.wishCount.toString()

        val (statusText, bgRes) = when (product.status) {
            "ON_SALE" -> "판매중" to R.drawable.bg_status_on_sale
            "RESERVED" -> "예약중" to R.drawable.bg_status_reserved
            else -> "판매완료" to R.drawable.bg_status_sold_out
        }
        binding.tvStatus.text = statusText
        binding.tvStatus.setBackgroundResource(bgRes)

        binding.viewPagerImages.adapter = ImagePagerAdapter(product.images, requireContext())
        binding.viewPagerImages.setCurrentItem(0, false)
        if (product.images.size > 1) {
            binding.tvImageCounter.visibility = View.VISIBLE
            binding.tvImageCounter.text = "1/${product.images.size}"
            binding.viewPagerImages.unregisterOnPageChangeCallback(pageChangeCallback)
            binding.viewPagerImages.registerOnPageChangeCallback(pageChangeCallback)
        } else {
            binding.tvImageCounter.visibility = View.GONE
            binding.viewPagerImages.unregisterOnPageChangeCallback(pageChangeCallback)
        }

        binding.ivWishDetail.setImageResource(
            if (product.isWished) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
        binding.ivWishDetail.setOnClickListener {
            viewModel.toggleWish(product.id, product.isWished)
        }

        // 판매자 본인만 수정/삭제/상태변경 버튼 보여줌
        val isSeller = product.seller.id == SessionManager.getUserId(requireContext())

        if (isSeller) {
            binding.layoutStatusButtons.visibility = View.VISIBLE
            binding.layoutSellerActions.visibility = View.VISIBLE

            binding.btnOnSale.setOnClickListener { viewModel.updateStatus(product.id, "ON_SALE") }
            binding.btnReserved.setOnClickListener { viewModel.updateStatus(product.id, "RESERVED") }
            binding.btnSoldOut.setOnClickListener { viewModel.updateStatus(product.id, "SOLD_OUT") }

            binding.btnEdit.setOnClickListener {
                startActivity(Intent(requireContext(), AddEditProductActivity::class.java).apply {
                    putExtra("productId", product.id)
                    putExtra("title", product.title)
                    putExtra("description", product.description)
                    putExtra("price", product.price)
                    putExtra("category", product.category)
                    putStringArrayListExtra("existingImageUrls", ArrayList(product.images))
                })
            }

            binding.btnDelete.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("상품 삭제")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        viewModel.deleteProduct(product.id)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        binding.viewPagerImages.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroyView()
        _binding = null
    }
}
