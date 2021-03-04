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


class StudentAdapter : RecyclerView.Adapter<StudentAdapter.ListViewHolder>() {
    private var studentData = JSONArray()
    private var listener: OnItemClickListener? = null

    fun StudentAdapter(listener: OnItemClickListener?) {
        this.listener = listener
    }

    fun setData(items: JSONArray) {
        studentData = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListViewHolder {
        val mView: View = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.students_list,
            viewGroup, false
        )
        return ListViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return studentData.length()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        try {
            holder.tvName.text = studentData.getJSONObject(position).getString("name")
            holder.tvNim.text = studentData.getJSONObject(position).getString("nim")
            holder.tvStatus.text = ("Status: " + studentData.getJSONObject(position).getString("presence_status"))
            holder.bind(studentData.getJSONObject(position), listener)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById<View>(R.id.tv_name) as TextView
        val tvNim: TextView = itemView.findViewById<View>(R.id.tv_nim) as TextView
        val tvStatus: TextView = itemView.findViewById<View>(R.id.tv_presence_status) as TextView

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

    interface OnItemClickListener {
        @Throws(JSONException::class)
        fun onItemClick(item: JSONObject)
    }
}