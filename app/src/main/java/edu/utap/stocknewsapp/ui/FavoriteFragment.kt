package edu.utap.stocknewsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.*
import edu.utap.stocknewsapp.MainActivity
import edu.utap.stocknewsapp.R
import edu.utap.stocknewsapp.api.NewsData
import edu.utap.stocknewsapp.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentFavoriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun initSearchEntity() {
        viewModel.observeFoundEntity().observe(viewLifecycleOwner) {
            val foundEntity = viewModel.observeFoundEntity().value
            if (foundEntity != null) {
                if (foundEntity.isNotEmpty()) {
                    if (!viewModel.isFavoriteByName(foundEntity[0].symbol!!)) {
                        viewModel.addFavorite(foundEntity[0],context)
                    } else {
                        Toast.makeText(
                            context, "Symbol is already added",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else {
                    Toast.makeText(context,"No symbol found with given keyword",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPos(holder: RecyclerView.ViewHolder) : Int {
        val pos = holder.bindingAdapterPosition
        // notifyDataSetChanged was called, so position is not known
        if( pos == RecyclerView.NO_POSITION) {
            return holder.absoluteAdapterPosition
        }
        return pos
    }

    // Touch helpers provide functionality like detecting swipes or moving
    // entries in a recycler view.  Here we do swipe left to delete
    private fun initTouchHelper(): ItemTouchHelper {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START)
            {
                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    val position = getPos(viewHolder)
                    Log.d(javaClass.simpleName, "Swipe delete $position")
                    viewModel.removeFavorite(position,context)
                }
            }
        return ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavoriteBinding.bind(view)
        Log.d(javaClass.simpleName, "Favorite Frag Created")
        val rv = binding.favRV
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        rv.layoutManager = layoutManager
        val adapter = FavoriteRowAdapter(viewModel)
        val itemDecor = DividerItemDecoration(rv.context, LinearLayoutManager.VERTICAL)
        rv.addItemDecoration(itemDecor)
        rv.adapter = adapter
        rv.itemAnimator = null
        initTouchHelper().attachToRecyclerView(rv)
        initSearchEntity()

        viewModel.observeFavStocksList().observe(viewLifecycleOwner) {
            val newFavList = it
            adapter.submitList(newFavList)
            adapter.notifyDataSetChanged()
        }

        // Add action when Enter is pressed
        binding.searchET.setOnEditorActionListener { /*v*/_, actionId, event ->
            // If user has pressed enter, or if they hit the soft keyboard "send" button
            // (which sends DONE because of the XML)
            if ((event != null
                        &&(event.action == KeyEvent.ACTION_DOWN)
                        &&(event.keyCode == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)) {
                (requireActivity() as MainActivity).hideKeyboard()
                binding.addBut.callOnClick()
            }
            false
        }

        // Handle clicking Add button
        binding.addBut.setOnClickListener {
            (requireActivity() as MainActivity).hideKeyboard()
            val inputSearchET = binding.searchET.text.toString()
            if (inputSearchET == "") {
                Toast.makeText(context,"Please input a valid symbol",Toast.LENGTH_SHORT).show()
            } else {
                if (viewModel.isFavoriteByName(inputSearchET)) {
                    Toast.makeText(context,"Symbol is already added",
                        Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.fetchSearch(inputSearchET)
                }
            }
        }
    }

    override fun onDestroyView() {
        viewModel.reinitializeSearchVars()
        super.onDestroyView()
        _binding = null
    }
}