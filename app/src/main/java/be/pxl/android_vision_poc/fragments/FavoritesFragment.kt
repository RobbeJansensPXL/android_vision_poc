package be.pxl.android_vision_poc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import be.pxl.android_vision_poc.MainActivity
import be.pxl.android_vision_poc.R
import be.pxl.android_vision_poc.room.FavoriteBeerModel


class FavoritesFragment : Fragment() {
    private lateinit var viewBinding: View
    private val newBeerActivityRequestCode = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = inflater.inflate(R.layout.fragment_favorites, container, false)

        val recyclerView = viewBinding.findViewById<RecyclerView>(R.id.rv_favorites)
        val adapter = BeerListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        (activity as MainActivity).beerViewModel.allBeers.observe(viewLifecycleOwner) { words ->
            words.let { adapter.submitList(it) }
        }
        // Inflate the layout for this fragment
        return viewBinding
    }
}

class BeerListAdapter : ListAdapter<FavoriteBeerModel, BeerListAdapter.BeerViewHolder>(BeerComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeerViewHolder {
        return BeerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BeerViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.beer)
    }

    class BeerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val beerItemView: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(text: String?) {
            beerItemView.text = text
        }

        companion object {
            fun create(parent: ViewGroup): BeerViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return BeerViewHolder(view)
            }
        }
    }

    class BeerComparator : DiffUtil.ItemCallback<FavoriteBeerModel>() {
        override fun areItemsTheSame(oldItem: FavoriteBeerModel, newItem: FavoriteBeerModel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: FavoriteBeerModel, newItem: FavoriteBeerModel): Boolean {
            return oldItem.beer == newItem.beer
        }
    }
}