package com.example.canteenapp.ui.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SpinnerAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentAddDishDialogBinding
import com.example.canteenapp.utils.models.DishData

class AddDishDialogFragment : DialogFragment() {

    private var _binding: FragmentAddDishDialogBinding? = null

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

    fun setListener(listener: OnDialogNextBtnClickListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "DialogFragment"
        @JvmStatic
        fun newInstance(dish: String) =
            AddDishDialogFragment().apply {
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
        _binding = FragmentAddDishDialogBinding.inflate(inflater, container, false)
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

        binding.todoClose.setOnClickListener {
            dismiss()
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
        binding.addImageOverlay.visibility = View.INVISIBLE
    }

    private fun getImageFromDevice() {
        resultLauncher.launch("image/*")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnDialogNextBtnClickListener{
        fun saveDish(dish: DishData?, dishEdit: FragmentAddDishDialogBinding, imageUri: Uri?, dishClass: String)
        fun updateDish(dishData: DishData)
    }
}