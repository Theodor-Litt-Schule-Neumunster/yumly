package com.ita24.yumly

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var allIngredients = listOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredient_search)

        val toolbar = findViewById<Toolbar>(R.id.searchToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        rvResults = findViewById(R.id.rvIngredientResults)
        searchView = findViewById(R.id.ingredientSearchView)
        val progressBar = findViewById<View>(R.id.searchProgressBar)

        adapter = IngredientAdapter { selectedIngredient ->
            val resultIntent = Intent()
            resultIntent.putExtra("selectedIngredient", selectedIngredient)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
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
    }

    private fun filter(query: String?) {
        if (query.isNullOrBlank()) {
            adapter.updateList(allIngredients)
        } else {
            val filtered = allIngredients.filter { it.contains(query, ignoreCase = true) }
            adapter.updateList(filtered)
        }
    }

    private class IngredientAdapter(private val onClick: (String) -> Unit) :
        RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {

        private var list = listOf<String>()

        fun updateList(newList: List<String>) {
            list = newList
            notifyDataSetChanged()
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.textView.text = item
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = list.size
    }
}
