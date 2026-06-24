package com.example.mobile_herbalify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.herbalify.R
import com.example.herbalify.model.Article

class ArticleAdapter(
    private val articleList: List<Article>,
    private val onClick: (Article) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivArticleImage: ImageView = itemView.findViewById(R.id.ivArticleImage)
        val tvArticleTitle: TextView = itemView.findViewById(R.id.tvArticleTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articleList[position]

        holder.tvArticleTitle.text = article.title

        if (article.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(article.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivArticleImage)
        } else {
            holder.ivArticleImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onClick(article)
        }
    }

    override fun getItemCount(): Int = articleList.size
}