package uz.muhammadziyo.bluetoothhc_6.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.muhammadziyo.bluetoothhc_6.databinding.ItemRvBinding
import uz.muhammadziyo.bluetoothhc_6.models.Bluetooth

class RvAdapter(var list: ArrayList<Bluetooth>, val rvClick: rvClick) :
    RecyclerView.Adapter<RvAdapter.Vh>() {

    inner class Vh(var itemRv: ItemRvBinding) : RecyclerView.ViewHolder(itemRv.root) {

        fun onBind(bluetooth: Bluetooth) {
            itemRv.name.text = bluetooth.name
            itemRv.root.setOnClickListener {
                rvClick.OnClick(bluetooth)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemRvBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

}

interface rvClick{
    fun OnClick(bluetooth: Bluetooth)
}