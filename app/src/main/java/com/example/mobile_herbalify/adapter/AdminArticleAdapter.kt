package com.example.mobile_herbalify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile_herbalify.R
import com.example.mobile_herbalify.model.Article

class AdminArticleAdapter(
    private val articleList: List<Article>,
    private val onEditClick: (Article) -> Unit,
    private val onDeleteClick: (Article) -> Unit
) : RecyclerView.Adapter<AdminArticleAdapter.AdminArticleViewHolder>() {

    class AdminArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivAdminArticleImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvAdminArticleTitle)
        val tvSnippet: TextView = itemView.findViewById(R.id.tvAdminArticleSnippet)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnAdminEditArticle)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnAdminDeleteArticle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_article, parent, false)
        return AdminArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminArticleViewHolder, position: Int) {
        val article = articleList[position]

        holder.tvTitle.text = article.title
        holder.tvSnippet.text = article.content

        if (article.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(article.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Set Aksi Klik Tombol
        holder.btnEdit.setOnClickListener { onEditClick(article) }
        holder.btnDelete.setOnClickListener { onDeleteClick(article) }
    }

    override fun getItemCount(): Int = articleList.size
}