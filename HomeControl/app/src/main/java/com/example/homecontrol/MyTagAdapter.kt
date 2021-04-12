package com.example.homecontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmQuery

class MyTagAdapter(val tags: MutableList<MyTag>) : RecyclerView.Adapter<MyTagAdapter.MyViewHolder>() {
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_name)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_tag_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, index: Int) {
        val tag = tags[index]
        val context = viewHolder.view.context

        viewHolder.tvName.text = "${tag.name}"

    }

    fun removeItem(index: Int) {
        val tag = tags[index]
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()
        val realmQuery: RealmQuery<out Any>
        realmQuery = realm.where(MyTag::class.java)

        realmQuery
            .equalTo("id", tag.id)
            .findAll().deleteAllFromRealm()
        realm.commitTransaction()

        tags.removeAt(index)
        notifyItemRemoved(index)

    }

    override fun getItemCount(): Int = tags.size
}