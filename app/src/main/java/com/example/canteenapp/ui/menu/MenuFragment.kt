package com.example.canteenapp.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentMenuBinding
import com.example.canteenapp.utils.adapters.CategoryAdapter
import com.example.canteenapp.utils.models.CategoryData
import com.example.canteenapp.utils.adapters.DishAdapter
import com.example.canteenapp.utils.models.DishData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment(), DishAdapter.DishAdapterClicksInterface {

    private var _binding: FragmentMenuBinding? = null
    private lateinit var databaseMenuReference: DatabaseReference

    private lateinit var dishAdapter: DishAdapter
    private lateinit var dishList: MutableList<DishData>

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryList: MutableList<CategoryData>

    private var currentCart: HashMap<String, DishData?>? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val menuNavigation = binding.menuNavigation

        currentCart = arguments?.getSerializable("currentCart") as HashMap<String, DishData?>?

        if (currentCart == null) {
            currentCart = HashMap()
            currentCart!!["mainDish"] = null
            currentCart!!["secondDish"] = null
            currentCart!!["drink"] = null
        }

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
        databaseMenuReference.child("categories/mainDishes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    categoryList.clear()
                    for (categorySnapshot in snapshot.children) {
                        val category = categorySnapshot.key?.let {
                            val title: String = categorySnapshot.child("title").value as String
                            val dishesCount: Long =
                                categorySnapshot.child("dishesCount").value as Long

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

        databaseMenuReference.child("dishes/mainDishes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dishList.clear()
                    for (dishSnapshot in snapshot.children) {
                        val dish = dishSnapshot.key?.let {
                            val weight: String = dishSnapshot.child("weight").value.toString()
                            val title: String = dishSnapshot.child("title").value as String
                            val category: String = dishSnapshot.child("category").value as String
                            val price: String = dishSnapshot.child("price").value.toString()
                            val picture: String = dishSnapshot.child("picture").value as String

                            var isActive = false

                            if (currentCart?.get("mainDish") != null && currentCart!!["mainDish"]?.id == it) {
                                isActive = true
                            }

                            DishData(it, weight, title, category, price, picture, isActive)
                        }

                        if (dish != null) {
                            dishList.add(dish)
                        }
                    }

                    dishAdapter.notifyDataSetChanged()

                    binding.menuContainer.animate().alpha(1.0f).duration = 300
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
        dishAdapter.setListener(this)

        binding.recyclerDishes.adapter = dishAdapter

        binding.recyclerCategories.setHasFixedSize(true)
        binding.recyclerCategories.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        categoryList = mutableListOf()
        categoryAdapter = CategoryAdapter(categoryList)
        binding.recyclerCategories.adapter = categoryAdapter

        binding.menuNavigation.backwardsArrow.setImageDrawable(resources.getDrawable(R.drawable.ic_round_arrow_back_ios_24_disabled))

        binding.menuNavigation.forwardArrow.setOnClickListener {
            val bundle = Bundle()

            bundle.putSerializable("currentCart", currentCart)

            findNavController().navigate(
                R.id.action_navigation_menu_to_menuSecondaryFragment,
                bundle
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCardClicked(dishData: DishData) {
        val currDish = dishList.find { item -> item.id === dishData.id }!!
        currDish.isActive = !currDish.isActive

        val prevPickedDishId = currentCart?.get("mainDish")?.id

        if (currDish.id != prevPickedDishId && prevPickedDishId != null) {
            dishList[prevPickedDishId.toInt()].isActive = false
        }

        dishAdapter.notifyDataSetChanged()
        currentCart?.set(
            "mainDish",
            if (prevPickedDishId != dishData.id) dishData else null
        )
    }
}