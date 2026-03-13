package com.ita24.yumly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class IngredientSearchActivity : AppCompatActivity() {

    private lateinit var rvResults: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: IngredientAdapter
    private lateinit var cbLimitMode: CheckBox
    private var allIngredients = listOf<String>()
    private val selectedItems = mutableSetOf<String>()
    private var isWhitelist = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_search)

        isWhitelist = intent.getBooleanExtra("isWhitelist", false)
        val initialSelected = intent.getStringArrayListExtra("selectedItems")?.toSet() ?: emptySet()
        selectedItems.addAll(initialSelected)

        val toolbar = findViewById<Toolbar>(R.id.searchToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvResults = findViewById(R.id.rvIngredientResults)
        searchView = findViewById(R.id.ingredientSearchView)
        cbLimitMode = findViewById(R.id.cbLimitMode)
        val progressBar = findViewById<View>(R.id.searchProgressBar)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmSelection)
        val btnSelectAll = findViewById<Button>(R.id.btnSelectAll)

        if (isWhitelist) {
            cbLimitMode.visibility = View.VISIBLE
            cbLimitMode.isChecked = intent.getBooleanExtra("isLimitMode", false)
        }

        adapter = IngredientAdapter(selectedItems) { item, isChecked ->
            if (isChecked) selectedItems.add(item) else selectedItems.remove(item)
        }
        rvResults.adapter = adapter

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            allIngredients = Imageloader.loadallIngredients()
            progressBar.visibility = View.GONE
            adapter.updateList(allIngredients)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })

        btnSelectAll.setOnClickListener {
            if (selectedItems.containsAll(allIngredients) && allIngredients.isNotEmpty()) {
                selectedItems.clear()
            } else {
                selectedItems.addAll(allIngredients)
            }
            adapter.notifyDataSetChanged()
        }

        btnConfirm.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("selectedItems", ArrayList(selectedItems))
            if (isWhitelist) {
                resultIntent.putExtra("isLimitMode", cbLimitMode.isChecked)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun filter(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allIngredients
        } else {
            allIngredients.filter { it.contains(query, ignoreCase = true) }
        }
        adapter.updateList(filtered)
    }

    private class IngredientAdapter(
        private val selectedSet: Set<String>,
        private val onToggle: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

        private var list = listOf<String>()

        fun updateList(newList: List<String>) {
            list = newList
            notifyDataSetChanged()
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.ingredientName)
            val checkBox: CheckBox = view.findViewById(R.id.ingredientCheckBox)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ingredient, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textView.text = item
            val isChecked = selectedSet.contains(item)
            holder.checkBox.isChecked = isChecked
            
            holder.itemView.setOnClickListener {
                val newChecked = !selectedSet.contains(item)
                onToggle(item, newChecked)
                notifyItemChanged(position)
            }
        }

        override fun getItemCount() = list.size
    }
}
