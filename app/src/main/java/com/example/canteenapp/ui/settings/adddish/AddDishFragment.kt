package com.example.canteenapp.ui.settings.adddish

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import com.example.canteenapp.databinding.FragmentAddDishBinding
import com.example.canteenapp.utils.models.DishData

class AddDishFragment : Fragment() {

    private var _binding: FragmentAddDishBinding? = null

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var imageURI: Uri? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var listener : OnDialogNextBtnClickListener? = null
    private var dishData: String? = null

    private val dishClasses: Map<String, String> = mapOf("Основное блюдо" to "mainDishes", "Второе блюдо" to "secondaryDishes",
        "Напитки" to "drinks"
    )

    companion object {
        const val TAG = "DialogFragment"
        @JvmStatic
        fun newInstance(dish: String) =
            AddDishFragment().apply {
                arguments = Bundle().apply {
                    putString("dishId", dish)
                    putString("dish", dish)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDishBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()

        binding.imageButton.setOnClickListener {
            getImageFromDevice()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            dishData = arguments?.getString("dish")
            binding.dishTitle.setText(dishData)
            println(dishData)
        }

        binding.addDishBtn.setOnClickListener {
            val dishTitle = binding.dishTitle.text.toString()
            val dishCategory = binding.dishCategory.text.toString()
            val dishPrice = binding.dishPrice.text.toString()
            val dishWeight = binding.dishWeight.text.toString()
            val dishPicture = ""
            val dishClass = dishClasses[binding.dishClassPicker.selectedItem]!!
            val dish = DishData("-1", dishWeight, dishTitle, dishCategory, dishPrice, dishPicture)
            listener?.saveDish(dish, binding, imageURI, dishClass)
        }
    }

    private fun init() {
        sharedPreferences = this.requireActivity().getSharedPreferences(sharedPrefsId,
            Context.MODE_PRIVATE
        )
        initSpinner()
    }

    private fun initSpinner() {
        val names = dishClasses.keys.toList()

        val spinnerAdapter = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, names) }
        spinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dishClassPicker.adapter = spinnerAdapter
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        imageURI = it
        binding.imageButton.setImageURI(it)
        binding.addImageHint.text = "Изменить фото"
        val resetIconId = resources.getIdentifier("com.example.canteenapp:drawable/" + "ic_round_restart_alt_24", null, null);
        binding.addImageIcon.setImageResource(resetIconId)
//        binding.addImageOverlay.visibility = View.INVISIBLE
    }

    private fun getImageFromDevice() {
        resultLauncher.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnDialogNextBtnClickListener{
        fun saveDish(dish: DishData?, dishEdit: FragmentAddDishBinding, imageUri: Uri?, dishClass: String)
        fun updateDish(dishData: DishData)
    }
}