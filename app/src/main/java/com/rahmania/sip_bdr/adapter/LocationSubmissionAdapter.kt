package com.rahmania.sip_bdr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rahmania.sip_bdr.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class LocationSubmissionAdapter : RecyclerView.Adapter<LocationSubmissionAdapter.ListViewHolder>() {
    private var locationData = JSONArray()
    private var listener: OnItemClickListener? = null

    fun LocationSubmissionAdapter(listener: OnItemClickListener?) {
        this.listener = listener
    }

    fun setData(items: JSONArray) {
        locationData = items
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById<View>(R.id.tv_item_student) as TextView
        val tvNim: TextView = itemView.findViewById<View>(R.id.tv_item_nim) as TextView
        fun bind(item: JSONObject, listener: OnItemClickListener?) {
            itemView.setOnClickListener {
                try {
                    listener?.onItemClick(item)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListViewHolder {
        val mView: View = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.locations_list,
            viewGroup, false
        )
        return ListViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return locationData.length()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        try {
            holder.tvName.text = locationData.getJSONObject(position).getString("name")
            holder.tvNim.text = locationData.getJSONObject(position).getString("nim")
            holder.bind(locationData.getJSONObject(position), listener)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface OnItemClickListener {
        @Throws(JSONException::class)
        fun onItemClick(item: JSONObject)
    }
}