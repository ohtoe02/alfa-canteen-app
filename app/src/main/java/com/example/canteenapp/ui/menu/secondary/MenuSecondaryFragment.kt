package com.example.canteenapp.ui.menu.secondary

import android.content.Context
import android.content.SharedPreferences
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
import com.example.canteenapp.databinding.FragmentMenuSecondaryBinding
import com.example.canteenapp.utils.adapters.CategoryAdapter
import com.example.canteenapp.utils.models.CategoryData
import com.example.canteenapp.utils.adapters.DishAdapter
import com.example.canteenapp.utils.models.DishData
import com.example.canteenapp.utils.models.KidData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MenuSecondaryFragment : Fragment(), DishAdapter.DishAdapterClicksInterface {

    private var _binding: FragmentMenuSecondaryBinding? = null
    private lateinit var databaseMenuReference: DatabaseReference

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSchool: String? = null

    private lateinit var dishAdapter: DishAdapter
    private lateinit var dishList: MutableList<DishData>

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryList: MutableList<CategoryData>

    private var currentCart: HashMap<String, DishData?>? = null
    private var currentKid: KidData? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuSecondaryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val menuNavigation = binding.menuNavigation

        menuNavigation.categoryDescription.text = "Заказать питание"
        menuNavigation.actionDescription.text = "Второе блюдо"

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentCart = arguments?.getSerializable("currentCart") as HashMap<String, DishData?>?
        currentKid = arguments?.getSerializable("currentKid") as KidData?

        init()

        getDataFromFirebase()
        binding.menuSecondaryContainer.animate().alpha(1.0f).duration = 300
        binding.loaderContainer.animate().alpha(0.0f)
    }

    private fun getDataFromFirebase() {
        databaseMenuReference.child("categories/secondaryDishes")
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

        databaseMenuReference.child("dishes/secondaryDishes")
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

                            if (currentCart?.get("secondDish") != null && currentCart!!["secondDish"]?.id == it) {
                                isActive = true
                            }

                            DishData(it, weight, title, category, price, picture, isActive)
                        }

                        if (dish != null) {
                            dishList.add(dish)
                        }
                    }

                    dishAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })

    }

    private fun init() {
        sharedPreferences = this.requireActivity().getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        currentSchool = sharedPreferences.getString("school", "")

        databaseMenuReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/menu")

        binding.recyclerSecondaryDishes.setHasFixedSize(true)
        binding.recyclerSecondaryDishes.layoutManager = GridLayoutManager(context, 2)

        dishList = mutableListOf()
        dishAdapter = DishAdapter(dishList)
        binding.recyclerSecondaryDishes.adapter = dishAdapter
        dishAdapter.setListener(this)


        binding.recyclerSecondaryCategories.setHasFixedSize(true)
        binding.recyclerSecondaryCategories.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        categoryList = mutableListOf()
        categoryAdapter = CategoryAdapter(categoryList)
        binding.recyclerSecondaryCategories.adapter = categoryAdapter

        val bundle = Bundle()

        bundle.putSerializable("currentCart", currentCart)
        bundle.putSerializable("currentKid", currentKid)


        binding.menuNavigation.forwardArrow.setOnClickListener {
//            clearBackStack(findNavController())
            findNavController().navigate(
                R.id.action_menuSecondaryFragment_to_menuDrinksFragment,
                bundle
            )
        }

        binding.menuNavigation.backwardsArrow.setOnClickListener {
//            clearBackStack(findNavController())
            findNavController().navigate(
                R.id.action_menuSecondaryFragment_to_navigation_menu,
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
        val currDishIndex = dishList.indexOf(currDish)
        currDish.isActive = !currDish.isActive

        val prevPickedDish = currentCart?.get("secondDish")
        val prevPickedDishIndex = dishList.indexOf(prevPickedDish)

        if (prevPickedDish != null && currDishIndex != prevPickedDishIndex) {
            dishList[prevPickedDishIndex].isActive = false
            dishAdapter.notifyItemChanged(prevPickedDishIndex)
        }

        dishAdapter.notifyItemChanged(currDishIndex)

        currentCart?.set(
            "secondDish",
            if (prevPickedDishIndex != currDishIndex) dishData else null
        )
    }
}