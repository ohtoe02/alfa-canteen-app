package com.example.canteenapp.ui.settings.adddish

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentAddDishBinding
import com.example.canteenapp.utils.FileUtil
import com.example.canteenapp.utils.createErrorSnack
import com.example.canteenapp.utils.createSuccessfulSnack
import com.example.canteenapp.utils.models.DishData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class AddDishFragment : Fragment() {

    private var _binding: FragmentAddDishBinding? = null

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var imageURI: Uri? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var dishTitle = ""
    private var dishCategory = ""
    private var dishPrice = ""
    private var dishWeight = ""
    private var dishPicture = ""
    private var dishClass = ""

    private var baseUri = ""

    private var actualImage: File? = null
    private var compressedImage: File? = null

    private var currentSchool: String? = null
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseDatabase: DatabaseReference

    private var dishData: String? = null

    private val dishClasses: Map<String, String> = mapOf("Основное блюдо" to "mainDishes", "Второе блюдо" to "secondaryDishes",
        "Напитки" to "drinks"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddDishBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            dishData = arguments?.getString("dish")
            binding.dishTitle.setText(dishData)
        }

        binding.addDishBtn.setOnClickListener {
            dishTitle = binding.dishTitle.text.toString().trim()
            dishCategory = binding.dishCategory.text.toString().trim()
            dishPrice = binding.dishPrice.text.toString().trim()
            dishWeight = binding.dishWeight.text.toString().trim()
            dishClass = dishClasses[binding.dishClassPicker.selectedItem]!!
            if (dishTitle == "" || dishCategory == "" || dishPrice == "" || dishWeight == "") {
                showError("Какое-то из полей не заполнено")
                return@setOnClickListener
            }
            val dish = DishData("-1", dishWeight, dishTitle, dishCategory, dishPrice, dishPicture)

            proceedDish(dish, dishClass)
        }

    }

    private fun init() {
        sharedPreferences = this.requireActivity().getSharedPreferences(sharedPrefsId,
            Context.MODE_PRIVATE
        )
        binding.orderHint.actionTitle.text = "Добавить"
        binding.orderHint.actionCategoryTitle.text = "Блюдо"
        binding.backwardsArrow.setOnClickListener {
            findNavController().popBackStack()
        }
        currentSchool = sharedPreferences.getString("school", "")
        storageRef = FirebaseStorage.getInstance("gs://alfa-canteen.appspot.com").reference.child("Images/$currentSchool")
        firebaseDatabase = Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("schools/$currentSchool/menu/dishes")
        setupSpinner()

        binding.imageButton.setOnClickListener {
            getImageFromDevice()
        }
    }

    private fun setupSpinner() {
        val names = dishClasses.keys.toList()

        val spinnerAdapter = context?.let { ArrayAdapter(it, R.layout.custom_spinner_item, names) }
        spinnerAdapter?.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.dishClassPicker.adapter = spinnerAdapter
    }

    private fun proceedDish(dish: DishData, dishClass: String) {
        binding.addDishBtn.text = "Загрузка..."
        binding.addDishBtn.isEnabled = false
        binding.addDishBtn.setTextColor(resources.getColor(R.color.dark_main))
        val newUri = compressedImage?.toUri()
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        newUri?.let {
            storageRef.putFile(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        baseUri = uri.toString()
                        dish.picture = baseUri
                        saveDish(dish, binding, dishClass)
                    }
                } else {
                    task.exception?.message?.let { it1 -> showError(it1) }
                    binding.addDishBtn.text = "Добавить"
                    binding.addDishBtn.isEnabled = true
                    binding.addDishBtn.setTextColor(resources.getColor(R.color.beige_main))
                }
            }
        }
    }

    private fun saveDish(dish: DishData, dishEdit: FragmentAddDishBinding, dishClass: String) {
        val dishKey = firebaseDatabase.child(dishClass).push()
        dish.id = dishKey.key.toString()
        dishKey.setValue(dish).addOnCompleteListener {
            if (it.isSuccessful) {
                view?.let { it1 ->
                    createSuccessfulSnack(it1, "Блюдо добавлено!")
                }
                dishEdit.dishTitle.text = null
                dishEdit.dishCategory.text = null
                dishEdit.dishPrice.text = null
                dishEdit.dishWeight.text = null
                binding.imageButton.setImageResource(android.R.color.transparent)
                binding.addDishBtn.text = "Добавить"
                binding.addDishBtn.isEnabled = true
                binding.addDishBtn.setTextColor(resources.getColor(R.color.beige_main))
            } else {
                showError(it.exception.toString())
            }
        }
    }

    private fun getImageFromDevice() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data == null) {
                showError("Failed to open picture!")
                return
            }
            try {
                actualImage = FileUtil.from(context, data.data)
                binding.addImageHint.text = "Изменить фото"
                val icon = resources.getDrawable(R.drawable.ic_round_restart_alt_24)
                binding.addImageIcon.setImageDrawable(icon)
                compressImage()
            } catch (e: IOException) {
                showError("Failed to read picture data!")
                e.printStackTrace()
            }
        }
    }

    private fun showError(errorMessage: String) {
        view?.let { createErrorSnack(it, errorMessage) }
    }

    private fun compressImage() {
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = context?.let { Compressor.compress(it, imageFile) }
                binding.imageButton.setImageURI(compressedImage?.toUri())
            }
        } ?: showError("Please choose an image!")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}