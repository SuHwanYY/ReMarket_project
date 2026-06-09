package com.example.remarket_project.ui.home

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.example.remarket_project.R
import com.example.remarket_project.databinding.FragmentHomeBinding
import com.example.remarket_project.ui.product.AddEditProductActivity
import com.example.remarket_project.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val adapter = ProductAdapter(
        onItemClick = { product ->
            findNavController().navigate(
                R.id.action_homeFragment_to_productDetailFragment,
                bundleOf("productId" to product.id)
            )
        },
        onWishClick = { product ->
            viewModel.toggleWish(product.id, product.isWished)
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.chipGroup.setOnCheckedStateChangeListener { _, _ -> }

        binding.btnSearch.setOnClickListener {
            hideKeyboard()
            performSearch()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                performSearch()
                true
            } else false
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditProductActivity::class.java))
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun performSearch() {
        val keyword = binding.etSearch.text?.toString()?.trim()?.ifEmpty { null }

        val checkedId = binding.chipGroup.checkedChipId
        val category = if (checkedId != View.NO_ID) {
            binding.chipGroup.findViewById<Chip>(checkedId)?.text?.toString()
        } else "전체"

        viewModel.loadProducts(
            keyword = keyword,
            category = if (category == "전체") null else category
        )
    }

    // 다른 화면에서 돌아올 때 목록 새로고침
    override fun onResume() {
        super.onResume()
        val checkedId = binding.chipGroup.checkedChipId
        val category = if (checkedId != View.NO_ID) {
            binding.chipGroup.findViewById<Chip>(checkedId)?.text?.toString() ?: "전체"
        } else "전체"
        viewModel.filterByCategory(category)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
