package com.example.remarket_project.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.remarket_project.R
import com.example.remarket_project.databinding.ItemProductBinding
import com.example.remarket_project.network.dto.ProductItem

// 홈/검색 화면 상품 목록 어댑터
// 이미지는 최대 4장 2x2 그리드, 5장 이상이면 "+N" 표시
class ProductAdapter(
    private val onItemClick: (ProductItem) -> Unit = {},
    private val onWishClick: (ProductItem) -> Unit = {}
) : ListAdapter<ProductItem, ProductAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductItem) {
            binding.tvTitle.text  = product.title
            binding.tvSeller.text = product.seller.nickname
            binding.tvRegion.text = " · ${product.seller.region}"
            binding.tvPrice.text  = String.format("%,d원", product.price)
            binding.tvWishCount.text = "♥ ${product.wishCount}"

            // 찜 여부로 하트 아이콘 변경
            binding.ivWish.setImageResource(
                if (product.isWished) R.drawable.ic_favorite else R.drawable.ic_favorite_border
            )

            // 판매 상태에 따라 배지 색상 다르게
            val (statusText, bgRes) = when (product.status) {
                "ON_SALE"  -> "판매중" to R.drawable.bg_status_on_sale
                "RESERVED" -> "예약중" to R.drawable.bg_status_reserved
                else       -> "판매완료" to R.drawable.bg_status_sold_out
            }
            binding.tvStatus.text = statusText
            binding.tvStatus.setBackgroundResource(bgRes)

            bindImages(product)

            binding.root.setOnClickListener { onItemClick(product) }
            binding.ivWish.setOnClickListener { onWishClick(product) }
        }

        // 이미지 개수에 따라 그리드 구성
        private fun bindImages(product: ProductItem) {
            val images = product.images
            val ctx = binding.root.context
            val categoryColor = getCategoryColor(product.category, ctx)

            // RecyclerView 재사용 시 이전 상태 초기화
            binding.ivImg2.visibility    = android.view.View.GONE
            binding.dividerV1.visibility = android.view.View.GONE
            binding.dividerH1.visibility = android.view.View.GONE
            binding.layoutRow2.visibility = android.view.View.GONE
            binding.tvMoreCount.visibility = android.view.View.GONE

            if (images.isEmpty()) {
                binding.ivImg1.setImageDrawable(null)
                binding.ivImg1.setBackgroundColor(categoryColor)
                return
            }

            binding.ivImg1.setBackgroundColor(0)
            Glide.with(ctx).load(images[0]).centerCrop().placeholder(R.color.divider).into(binding.ivImg1)

            if (images.size >= 2) {
                binding.ivImg2.visibility    = android.view.View.VISIBLE
                binding.dividerV1.visibility = android.view.View.VISIBLE
                Glide.with(ctx).load(images[1]).centerCrop().placeholder(R.color.divider).into(binding.ivImg2)
            }

            if (images.size >= 3) {
                binding.dividerH1.visibility  = android.view.View.VISIBLE
                binding.layoutRow2.visibility = android.view.View.VISIBLE
                Glide.with(ctx).load(images[2]).centerCrop().placeholder(R.color.divider).into(binding.ivImg3)

                if (images.size >= 4) {
                    Glide.with(ctx).load(images[3]).centerCrop().placeholder(R.color.divider).into(binding.ivImg4)
                    if (images.size > 4) {
                        binding.tvMoreCount.visibility = android.view.View.VISIBLE
                        binding.tvMoreCount.text = "+${images.size - 4}"
                    }
                } else {
                    // 3장이면 4번 슬롯은 카테고리 색상 배경으로 채움
                    binding.ivImg4.setImageDrawable(null)
                    binding.ivImg4.setBackgroundColor(categoryColor)
                }
            }
        }

        // 이미지 없을 때 카테고리별 배경색 반환
        private fun getCategoryColor(category: String, context: android.content.Context): Int =
            when (category) {
                "의류"   -> context.getColor(R.color.categoryClothing)
                "전자기기" -> context.getColor(R.color.categoryElectronics)
                "도서"   -> context.getColor(R.color.categoryBooks)
                "운동용품" -> context.getColor(R.color.categorySports)
                else     -> context.getColor(R.color.mainColor)
            }
    }

    // ListAdapter가 변경된 아이템만 업데이트하는 데 사용
    class DiffCallback : DiffUtil.ItemCallback<ProductItem>() {
        override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem) =
            oldItem == newItem
    }
}
