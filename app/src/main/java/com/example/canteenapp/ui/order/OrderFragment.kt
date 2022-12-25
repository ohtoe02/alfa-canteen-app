package com.example.canteenapp.ui.order

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentOrderBinding
import com.example.canteenapp.databinding.InorderAddDishCardBinding
import com.example.canteenapp.databinding.InorderDishCardBinding
import com.example.canteenapp.utils.betterNavigate
import com.example.canteenapp.utils.createErrorSnack
import com.example.canteenapp.utils.createSuccessfulSnack
import com.example.canteenapp.utils.models.DishData
import com.example.canteenapp.utils.models.KidData
import com.example.canteenapp.utils.models.MyRequestData
import com.example.canteenapp.utils.models.OrderData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class OrderFragment : Fragment() {

    private var _binding: FragmentOrderBinding? = null
    private var currentCart: HashMap<String, DishData?>? = null
    private var totalPrice: Int = 0
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSchool: String? = null
    private var userLogin: String? = null
    private var currentKid: KidData? = null
    private val binding get() = _binding!!
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var dishClasses: Map<String, Triple<InorderDishCardBinding, InorderAddDishCardBinding, Int>>
    private lateinit var dishNamings: Map<String, String>
    private lateinit var kidsMap: Map<String, String>
    private lateinit var reversedKidsMap: Map<String, String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        findNavController().backQueue.forEach { item -> println("${item.savedStateHandle} ///") }

        dishClasses = mapOf(
            "mainDish" to
                    Triple(
                        binding.mainDishCheckout,
                        binding.mainDishCheckoutAdd,
                        R.id.action_navigation_order_to_navigation_menu
                    ),
            "secondDish" to
                    Triple(
                        binding.secondaryDishCheckout,
                        binding.secondaryDishCheckoutAdd,
                        R.id.action_navigation_order_to_menuSecondaryFragment
                    ),
            "drink" to
                    Triple(
                        binding.drinkCheckout,
                        binding.drinkCheckoutAdd,
                        R.id.action_navigation_order_to_menuDrinksFragment
                    )
        )

        dishNamings = mapOf(
            "mainDish" to "основное блюдо",
            "secondDish" to "второе блюдо",
            "drink" to "напиток",
        )

        init()

        currentCart?.forEach { (key, value) ->
            dishClasses[key]?.let {
                setDish(
                    it,
                    value,
                    key,
                    dishNamings[key]
                )
            }
        }

        return binding.root
    }

    private fun init() {
        binding.orderHint.actionTitle.text = "Моя"
        binding.orderHint.actionCategoryTitle.text = "Заявка"

        currentKid = arguments?.getSerializable("currentKid") as KidData?

        sharedPreferences = this.requireActivity().getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        currentSchool = sharedPreferences.getString("school", "")
        userLogin = sharedPreferences.getString("login", "")

        firebaseDatabase =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/")

        val tempCart = arguments?.getSerializable("currentCart") as HashMap<String, DishData?>?

        if (tempCart != null)
            currentCart = tempCart

        if (currentCart == null) {
            currentCart = HashMap()
            currentCart!!["mainDish"] = null
            currentCart!!["secondDish"] = null
            currentCart!!["drink"] = null
        }

        binding.backwardsArrow.setOnClickListener {
            val bundle = Bundle()

            bundle.putSerializable("currentCart", currentCart)
            bundle.putSerializable("currentKid", currentKid)
            betterNavigate(
                findNavController(),
                R.id.action_navigation_order_to_navigation_menu,
                bundle
            )
        }

        updateTotalPrice()
        binding.checkoutButton.root.setOnClickListener {
            if (totalPrice == 0) {
                view?.let { it1 -> createErrorSnack(it1, "Выберите хотя бы одно блюдо") }
                return@setOnClickListener
            }

            val kidId = reversedKidsMap[binding.childPicker.selectedItem.toString()]!!
            val author = userLogin!!
            val price = totalPrice.toString()

            val totalCart = hashMapOf(
                "mainDish" to currentCart!!["mainDish"]?.id,
                "secondDish" to currentCart!!["secondDish"]?.id,
                "drink" to currentCart!!["drink"]?.id
            )


            val info = hashMapOf("class" to "", "teacher" to "", "time" to "time", "kidId" to kidId, "parent" to author, "price" to price)

            val order = OrderData(totalCart, info)
            setOrderRequest(order)
        }

        getKids()
    }

    private fun setOrderRequest(order: OrderData) {
        val reqId = firebaseDatabase.child("classes/classID/orderRequests").push()

        val myCart = hashMapOf(
            "mainDish" to Pair(currentCart!!["mainDish"]?.id, currentCart!!["mainDish"]?.title),
            "secondDish" to Pair(currentCart!!["secondDish"]?.id, currentCart!!["secondDish"]?.title),
            "drink" to Pair(currentCart!!["drink"]?.id, currentCart!!["drink"]?.title)
        )

        val myRequest = mapOf(reqId.key!! to MyRequestData(reqId.key!!, myCart, order.info))

        reqId.setValue(order).addOnCompleteListener {
            if (it.isSuccessful) {
                view?.let { it1 -> createSuccessfulSnack(it1, "Заявка оставлена!") }
                firebaseDatabase.child("users/$userLogin/requests").updateChildren(myRequest)
            }
        }
    }

    private fun getKids() {

        firebaseDatabase.child("users/${userLogin}/family/kids")
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    kidsMap = snapshot.value as Map<String, String>
                    reversedKidsMap = kidsMap.entries.associateBy({ it.value }) { it.key }
                    setSpinner()
                }
            }
    }

    private fun setSpinner() {
        val names = kidsMap.values.toList()

        val spinnerAdapter = context?.let { ArrayAdapter(it, R.layout.custom_spinner_item, names) }
        spinnerAdapter?.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.childPicker.adapter = spinnerAdapter
        binding.childPicker.setSelection(names.indexOf(currentKid?.name))

        binding.childPicker.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val kidId = kidsMap.keys.elementAt(position)
                val kidName = kidsMap.values.elementAt(position)
                currentKid = userLogin?.let { KidData(kidId, kidName, it) }
                println(currentKid)
            }
        }
    }

    private fun setDish(
        dishViewTriple: Triple<InorderDishCardBinding, InorderAddDishCardBinding, Int>,
        dishData: DishData?,
        dishKey: String,
        dishNaming: String?
    ) {
        if (dishData == null) {
            dishViewTriple.first.root.visibility = View.INVISIBLE
            dishViewTriple.second.root.visibility = View.VISIBLE
            dishViewTriple.second.addDishDescription.text = String.format("Добавить %s", dishNaming)
            dishViewTriple.second.root.setOnClickListener {
                val bundle = Bundle()

                bundle.putSerializable("currentCart", currentCart)
                bundle.putSerializable("currentKid", currentKid)
                betterNavigate(findNavController(), dishViewTriple.third, bundle)
            }
            return
        }

        dishViewTriple.first.removeButton.setOnClickListener {
            currentCart?.set(dishKey, null)
            setDish(dishViewTriple, null, dishKey, dishNaming)
            updateTotalPrice()
        }

        dishViewTriple.first.title.text = dishData.title
        dishViewTriple.first.category.text = dishData.category
        dishViewTriple.first.price.text = String.format("%s₽", dishData.price)
        Picasso.get().load(dishData.picture).into(dishViewTriple.first.dishPicture)
    }

    private fun updateTotalPrice() {
        totalPrice = currentCart?.values?.fold(0) { a, b -> a + (b?.price?.toInt() ?: 0) }!!
        binding.checkoutButton.textView.text = String.format("%s₽", totalPrice)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
