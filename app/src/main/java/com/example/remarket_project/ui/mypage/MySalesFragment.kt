package com.example.remarket_project.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remarket_project.R
import com.example.remarket_project.databinding.FragmentMySalesBinding
import com.example.remarket_project.ui.product.AddEditProductActivity
import com.example.remarket_project.viewmodel.MySalesViewModel

class MySalesFragment : Fragment() {

    private var _binding: FragmentMySalesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MySalesViewModel by viewModels()

    private val adapter = MySalesAdapter(
        onItemClick = { product ->
            findNavController().navigate(
                R.id.action_mySalesFragment_to_productDetailFragment,
                bundleOf("productId" to product.id)
            )
        },
        onEditClick = { product ->
            startActivity(Intent(requireContext(), AddEditProductActivity::class.java).apply {
                putExtra("productId", product.id)
                putExtra("title", product.title)
                putExtra("description", product.description)
                putExtra("price", product.price)
                putExtra("category", product.category)
                putStringArrayListExtra("existingImageUrls", ArrayList(product.images))
            })
        },
        onDeleteClick = { product ->
            AlertDialog.Builder(requireContext())
                .setTitle("상품 삭제")
                .setMessage("'${product.title}'을(를) 삭제하시겠습니까?")
                .setPositiveButton("삭제") { _, _ ->
                    viewModel.deleteProduct(product.id) {
                        Toast.makeText(requireContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        },
        onStatusClick = { product ->
            val statuses = arrayOf("판매중", "예약중", "판매완료")
            val statusCodes = arrayOf("ON_SALE", "RESERVED", "SOLD_OUT")
            val currentIndex = statusCodes.indexOf(product.status).coerceAtLeast(0)

            AlertDialog.Builder(requireContext())
                .setTitle("판매 상태 변경")
                .setSingleChoiceItems(statuses, currentIndex) { dialog, which ->
                    if (statusCodes[which] != product.status) {
                        viewModel.updateStatus(product.id, statusCodes[which])
                        Toast.makeText(requireContext(), "${statuses[which]}으로 변경됐습니다.", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("취소", null)
                .show()
        },
        onSoldOutClick = { product ->
            AlertDialog.Builder(requireContext())
                .setTitle("판매 완료")
                .setMessage("'${product.title}'을(를) 판매 완료로 변경하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    viewModel.updateStatus(product.id, "SOLD_OUT")
                    Toast.makeText(requireContext(), "판매 완료로 변경됐습니다.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소", null)
                .show()
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMySalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.myProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    // 돌아올 때 목록 새로고침
    override fun onResume() {
        super.onResume()
        viewModel.loadMyProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
