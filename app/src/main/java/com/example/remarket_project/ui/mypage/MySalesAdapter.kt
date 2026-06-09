package com.example.remarket_project.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.remarket_project.R
import com.example.remarket_project.databinding.ItemMyProductBinding
import com.example.remarket_project.network.dto.ProductItem

// 내 판매 목록 어댑터 (수정/삭제/상태변경 버튼 포함)
class MySalesAdapter(
    private val onItemClick: (ProductItem) -> Unit,
    private val onEditClick: (ProductItem) -> Unit,
    private val onDeleteClick: (ProductItem) -> Unit,
    private val onStatusClick: (ProductItem) -> Unit,
    private val onSoldOutClick: (ProductItem) -> Unit
) : ListAdapter<ProductItem, MySalesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMyProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductItem) {
            binding.tvTitle.text = product.title
            binding.tvPrice.text = String.format("%,d원", product.price)

            // 상태 배지 설정
            val (statusText, bgRes) = when (product.status) {
                "ON_SALE"  -> "판매중" to R.drawable.bg_status_on_sale
                "RESERVED" -> "예약중" to R.drawable.bg_status_reserved
                else       -> "판매완료" to R.drawable.bg_status_sold_out
            }
            binding.tvStatus.text = statusText
            binding.tvStatus.setBackgroundResource(bgRes)

            bindImages(product)

            binding.root.setOnClickListener { onItemClick(product) }
            binding.btnEdit.setOnClickListener { onEditClick(product) }
            binding.btnDelete.setOnClickListener { onDeleteClick(product) }
            binding.layoutStatus.setOnClickListener { onStatusClick(product) }

            // 판매중일 때만 "판매완료로 변경" 버튼 표시
            if (product.status == "ON_SALE") {
                binding.btnMarkSoldOut.visibility = android.view.View.VISIBLE
                binding.btnMarkSoldOut.setOnClickListener { onSoldOutClick(product) }
            } else {
                binding.btnMarkSoldOut.visibility = android.view.View.GONE
            }
        }

        private fun bindImages(product: ProductItem) {
            val images = product.images
            val ctx = binding.root.context
            val categoryColor = when (product.category) {
                "의류"   -> ctx.getColor(R.color.categoryClothing)
                "전자기기" -> ctx.getColor(R.color.categoryElectronics)
                "도서"   -> ctx.getColor(R.color.categoryBooks)
                "운동용품" -> ctx.getColor(R.color.categorySports)
                else     -> ctx.getColor(R.color.mainColor)
            }

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
                    binding.ivImg4.setImageDrawable(null)
                    binding.ivImg4.setBackgroundColor(categoryColor)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ProductItem>() {
        override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem) =
            oldItem == newItem
    }
}
