package com.example.canteenapp.ui.menu

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.databinding.FragmentMenuBinding
import com.example.canteenapp.utils.CategoryAdapter
import com.example.canteenapp.utils.CategoryData
import com.example.canteenapp.utils.DishAdapter
import com.example.canteenapp.utils.DishData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private lateinit var databaseMenuReference: DatabaseReference

    private lateinit var dishAdapter: DishAdapter
    private lateinit var dishList: MutableList<DishData>

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryList: MutableList<CategoryData>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val menuViewModel =
            ViewModelProvider(this)[MenuViewModel::class.java]

        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.menuCategoriesText
        menuViewModel.text.observe(viewLifecycleOwner) {
            textView.text = "Меню"
        }

        val menuNavigation = binding.menuNavigation

        menuNavigation.categoryDescription.text = "Заказать питание"
        menuNavigation.actionDescription.text = "Основное блюдо"

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        getDataFromFirebase()
    }

    private fun getDataFromFirebase() {
        databaseMenuReference.child("categories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.key?.let {
                        val title: String = categorySnapshot.child("title").value as String
                        val dishesCount: Long = categorySnapshot.child("dishesCount").value as Long

                        CategoryData(it, title, dishesCount)
                    }

                    if (category != null) {
                        categoryList.add(category)
                    }
                }

                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        databaseMenuReference.child("dishes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishList.clear()
                for (dishSnapshot in snapshot.children) {
                    val dish = dishSnapshot.key?.let {
                        val weight: String = dishSnapshot.child("weight").value.toString()
                        val title: String = dishSnapshot.child("title").value as String
                        val category: String = dishSnapshot.child("category").value as String
                        val price: String = dishSnapshot.child("price").value.toString()
                        val picture: String = dishSnapshot.child("picture").value as String

                        DishData(it, weight, title, category, price, picture)
                    }

                    if (dish != null) {
                        dishList.add(dish)
                    }
                }

                dishAdapter.notifyDataSetChanged()


                binding.menuContainer.animate().alpha(1.0f).duration = 300
//                binding.menuContainer.visibility = View.VISIBLE
                binding.loaderContainer.animate().alpha(0.0f)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun init() {
        databaseMenuReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/school1/menu")

        binding.recyclerDishes.setHasFixedSize(true)
        binding.recyclerDishes.layoutManager = GridLayoutManager(context, 2)

        dishList = mutableListOf()
        dishAdapter = DishAdapter(dishList)
        binding.recyclerDishes.adapter = dishAdapter

        binding.recyclerCategories.setHasFixedSize(true)
        binding.recyclerCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)


        categoryList = mutableListOf()
        categoryAdapter = CategoryAdapter(categoryList)
        binding.recyclerCategories.adapter = categoryAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}