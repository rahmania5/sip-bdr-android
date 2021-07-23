package com.rahmania.sip_bdr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById<View>(R.id.tv_name) as TextView
        val tvNim: TextView = itemView.findViewById<View>(R.id.tv_nim) as TextView
        val tvInfo: TextView = itemView.findViewById<View>(R.id.tv_info) as TextView
        val ivStatus: ImageView = itemView.findViewById<View>(R.id.iv_status) as ImageView
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
            val info = studentData.getJSONObject(position).getInt("needs_review")
            when (info) {
                0 -> {
                    holder.tvInfo.visibility = View.GONE
                }
                1 -> {
                    holder.tvInfo.setText(R.string.status_left_too_early)
                }
                2 -> {
                    holder.tvInfo.setText(R.string.status_late)
                }
            }
            val status = studentData.getJSONObject(position).getString("presence_status")
            when (status) {
                "Hadir" -> {
                    holder.ivStatus.setImageResource(R.drawable.ic_checked)
                }
                "Absen" -> {
                    holder.ivStatus.setImageResource(R.drawable.ic_delete)
                }
                else -> {
                    holder.ivStatus.setImageResource(R.drawable.ic_info)
                }
            }
            holder.bind(studentData.getJSONObject(position), listener)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface OnItemClickListener {
        @Throws(JSONException::class)
        fun onItemClick(item: JSONObject)
    }
}