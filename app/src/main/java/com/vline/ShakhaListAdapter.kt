package com.vline


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.vline.databinding.MovieListBinding

class ShakhaListAdapter() :
    RecyclerView.Adapter<ShakhaListAdapter.ViewHolder>() {

    lateinit var context: Context
    private var mOptionList: ArrayList<String> =
        java.util.ArrayList<String>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShakhaListAdapter.ViewHolder {

        val viewProductCategoryBinding: MovieListBinding =
            MovieListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(viewProductCategoryBinding)


    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            Glide.with(context)
                .load(mOptionList.get(position).toString())
                .into(holder.binding.image)
        } catch (e: Exception) {

        }
    }

    fun setData(mOptionList: ArrayList<String>) {
        this.mOptionList = mOptionList
        notifyDataSetChanged()
    }

    fun updateList(list: ArrayList<String>) {
        mOptionList = list
        notifyDataSetChanged()
    }


    private fun getItem(index: Int): String {
        return mOptionList[index].toString()
    }

    override fun getItemCount(): Int {
        return mOptionList.size
    }


    inner class ViewHolder(val binding: MovieListBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        override fun onClick(v: View?) {
            ////
        }
    }

    fun bind(photo: String) {

    }
}