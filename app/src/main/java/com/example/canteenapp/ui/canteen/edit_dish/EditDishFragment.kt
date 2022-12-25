package com.example.canteenapp.ui.canteen.edit_dish

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentEditDishBinding
import com.example.canteenapp.utils.FileUtil
import com.example.canteenapp.utils.createErrorSnack
import com.example.canteenapp.utils.createSuccessfulSnack
import com.example.canteenapp.utils.models.DishData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import id.zelory.compressor.Compressor
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class EditDishFragment : Fragment() {
    private var _binding: FragmentEditDishBinding? = null

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences

    private val binding get() = _binding!!
    private lateinit var currentDish: DishData
    private var baseUri = ""

    private var actualImage: File? = null
    private var compressedImage: File? = null

    private var currentSchool: String? = null
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var currentDishClass: Pair<String, String>

    private val dishClasses: Map<String, String> = mapOf("Основное блюдо" to "mainDishes", "Второе блюдо" to "secondaryDishes",
        "Напитки" to "drinks"
    )

    companion object {
        fun newInstance() = EditDishFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDishBinding.inflate(inflater, container, false)

        currentDish = arguments?.getSerializable("dish") as DishData
        currentDishClass = arguments?.getSerializable("dishClass") as Pair<String, String>

        init()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        sharedPreferences = this.requireActivity().getSharedPreferences(sharedPrefsId,
            Context.MODE_PRIVATE
        )

        currentSchool = sharedPreferences.getString("school", "")
        storageRef = FirebaseStorage.getInstance("gs://alfa-canteen.appspot.com").reference.child("Images/$currentSchool")
        firebaseDatabase = Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("schools/$currentSchool/menu/dishes")

        setupNavigationInfo()
        fillInputs()
        setListeners()
        setupSpinner()
    }

    private fun setupNavigationInfo() {
        binding.orderHint.actionCategoryTitle.text = "Блюдо"
        binding.orderHint.actionTitle.text = "Редактировать"
    }

    private fun fillInputs() {
        binding.dishPrice.setText(currentDish.price)
        binding.dishWeight.setText(currentDish.weight)
        binding.dishTitle.setText(currentDish.title)
        binding.dishCategory.setText(currentDish.category)
        Picasso.get().load(currentDish.picture).into(binding.imageButton)
    }

    private fun setupSpinner() {
        val names = dishClasses.keys.toList()

        val spinnerAdapter = context?.let { ArrayAdapter(it, R.layout.custom_spinner_item, names) }
        spinnerAdapter?.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.dishClassPicker.adapter = spinnerAdapter!!
        binding.dishClassPicker.setSelection(spinnerAdapter.getPosition(currentDishClass.first))
    }

    private fun setListeners() {
        binding.applyButton.setOnClickListener {
            updateDish()
        }

        binding.removeButton.setOnClickListener {
            removeDish()
        }

        binding.backwardsArrow.setOnClickListener {
            findNavController().popBackStack()
//            findNavController().navigate(R.id.action_editDishFragment_to_allDishesListFragment)
        }

        binding.imageButton.setOnClickListener {
            getImageFromDevice()
        }
    }

    private fun updateDish() {
        val weight = binding.dishWeight.text.toString()
        val price = binding.dishPrice.text.toString()
        val title = binding.dishTitle.text.toString()
        val category = binding.dishCategory.text.toString()
        val dishClass = dishClasses[binding.dishClassPicker.selectedItem]!!


        if (title == "" || category == "" || price == "" || weight == "") {
            showError("Какое-то из полей не заполнено")
            return
        }

        val newDish = DishData(currentDish.id, weight, title, category, price, currentDish.picture)
        uploadImage(newDish, dishClass)
    }

    private fun getImageFromDevice() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
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

    private fun compressImage() {
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = context?.let { Compressor.compress(it, imageFile) }
                binding.imageButton.setImageURI(compressedImage?.toUri())
            }
        } ?: showError("Please choose an image!")
    }

    private fun uploadImage(dish: DishData, dishClass: String) {
        binding.applyButton.text = "Загрузка..."
        binding.applyButton.isEnabled = false
        binding.applyButton.setTextColor(resources.getColor(R.color.dark_main))
        if (compressedImage == null) {
            saveDish(dish, dishClass)
            return
        }
        val newUri = compressedImage?.toUri()
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        newUri?.let {
            storageRef.putFile(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        baseUri = uri.toString()
                        dish.picture = baseUri
                        saveDish(dish, dishClass)
                    }
                } else {
                    task.exception?.message?.let { it1 -> showError(it1) }
                    binding.applyButton.text = "Добавить"
                    binding.applyButton.isEnabled = true
                    binding.applyButton.setTextColor(resources.getColor(R.color.beige_main))
                }
            }
        }
    }

    private fun removeDish() {
        firebaseDatabase.child("${currentDishClass.second}/${currentDish.id}").removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()

            } else {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDish(dish: DishData, dishClass: String) {
        val dishKey = firebaseDatabase.child("$dishClass/${currentDish.id}")
        dishKey.setValue(dish).addOnCompleteListener {
            if (it.isSuccessful) {
                view?.let { it1 ->
                    createSuccessfulSnack(it1, "Блюдо отредактировано!")
                }

                binding.applyButton.text = "Применить"
                binding.applyButton.isEnabled = true
                binding.applyButton.setTextColor(resources.getColor(R.color.beige_main))
                if (currentDishClass.second != dishClass) {
                    removeDish()
                }
            } else {
                showError(it.exception.toString())
            }
        }
    }

    private fun showError(errorMessage: String) {
        view?.let { createErrorSnack(it, errorMessage) }
    }
}