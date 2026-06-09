package com.example.remarket_project.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.example.remarket_project.R
import com.example.remarket_project.databinding.FragmentSearchBinding
import com.example.remarket_project.ui.home.ProductAdapter
import com.example.remarket_project.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()

    private val regions = listOf(
        "전체", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    )

    private val adapter = ProductAdapter(
        onItemClick = { product ->
            findNavController().navigate(
                R.id.action_searchFragment_to_productDetailFragment,
                bundleOf("productId" to product.id)
            )
        },
        onWishClick = { product ->
            viewModel.toggleWish(product.id, product.isWished)
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.isNestedScrollingEnabled = false

        val regionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, regions)
        binding.actvRegion.setAdapter(regionAdapter)
        binding.actvRegion.setText("전체", false)

        binding.rangeSlider.addOnChangeListener { slider, _, _ ->
            val min = slider.values[0].toInt()
            val max = slider.values[1].toInt()
            binding.tvPriceMin.text = String.format("%,d원", min)
            binding.tvPriceMax.text = String.format("%,d원", max)
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        binding.btnSearch.setOnClickListener {
            hideKeyboard()
            performSearch()
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
        val minPrice = binding.rangeSlider.values[0].toInt()
        val maxPrice = binding.rangeSlider.values[1].toInt()
        val region = binding.actvRegion.text?.toString()?.trim()

        val checkedChip = binding.chipGroupSearch.checkedChipId
        val category = if (checkedChip != View.NO_ID) {
            binding.chipGroupSearch.findViewById<Chip>(checkedChip)?.text?.toString()
        } else null

        viewModel.search(
            keyword = keyword,
            category = if (category == "전체") null else category,
            minPrice = if (minPrice > 0) minPrice else null,
            maxPrice = if (maxPrice < 2_000_000) maxPrice else null,
            region = if (region == "전체" || region.isNullOrEmpty()) null else region
        )
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
