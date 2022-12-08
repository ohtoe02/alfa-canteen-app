package com.example.canteenapp.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentOrderBinding
import com.example.canteenapp.databinding.InorderAddDishCardBinding
import com.example.canteenapp.databinding.InorderDishCardBinding
import com.example.canteenapp.utils.models.DishData
import com.squareup.picasso.Picasso

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private var currentCart: HashMap<String, DishData?>? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var dishClasses: Map<String, Triple<InorderDishCardBinding, InorderAddDishCardBinding, Int>>
    private lateinit var dishNamings: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)

        dishClasses = mapOf(
            "mainDish" to
                    Triple(binding.mainDishCheckout, binding.mainDishCheckoutAdd, R.id.action_navigation_order_to_navigation_menu),
            "secondDish" to
                    Triple(binding.secondaryDishCheckout, binding.secondaryDishCheckoutAdd, R.id.action_navigation_order_to_menuSecondaryFragment),
            "drink" to
                    Triple(binding.drinkCheckout, binding.drinkCheckoutAdd, R.id.action_navigation_order_to_menuDrinksFragment)
        )

        dishNamings = mapOf("mainDish" to "основное блюдо", "secondDish" to "второе блюдо", "drink" to "напиток", )

        init()

        currentCart?.forEach { (key, value) -> dishClasses[key]?.let { setDish(it, value, key, dishNamings[key]) } }

        return binding.root
    }

    private fun init() {
        binding.orderHint.actionTitle.text = "Моя"
        binding.orderHint.actionCategoryTitle.text = "Заявка"

        currentCart = arguments?.getSerializable("currentCart") as HashMap<String, DishData?>?

        if (currentCart == null) {
            currentCart = HashMap()
            currentCart!!["mainDish"] = null
            currentCart!!["secondDish"] = null
            currentCart!!["drink"] = null
        }

        binding.backwardsArrow.setOnClickListener {
            val bundle = Bundle()

            bundle.putSerializable("currentCart", currentCart)
            findNavController().navigate(R.id.action_navigation_order_to_menuDrinksFragment, bundle)
        }

        binding.checkoutButton.root.setOnClickListener {
            val sum: Int? = currentCart?.values?.fold(0) { a, b -> a + (b?.price?.toInt() ?: 0) }
            Toast.makeText(context, "Общая цена $sum₽", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setDish(dishViewTriple: Triple<InorderDishCardBinding, InorderAddDishCardBinding, Int>, dishData: DishData?, dishKey: String, dishNaming: String?) {
        if (dishData == null) {
            dishViewTriple.first.root.visibility = View.INVISIBLE
            dishViewTriple.second.root.visibility = View.VISIBLE
            dishViewTriple.second.addDishDescription.text = String.format("Добавить %s", dishNaming)
            dishViewTriple.second.root.setOnClickListener {
                val bundle = Bundle()

                bundle.putSerializable("currentCart", currentCart)
                findNavController().navigate(dishViewTriple.third, bundle)
            }
            return
        }

        dishViewTriple.first.removeButton.setOnClickListener {
            currentCart?.set(dishKey, null)
            setDish(dishViewTriple, null, dishKey, dishNaming)
        }

        dishViewTriple.first.title.text = dishData.title
        dishViewTriple.first.category.text = dishData.category
        dishViewTriple.first.price.text = String.format("%s₽", dishData.price)
        Picasso.get().load(dishData.picture).into(dishViewTriple.first.dishPicture)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
