package com.malkinfo.puzzle.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.malkinfo.numberpulzzgame.R
import com.malkinfo.puzzle.Product

class ProductAdapter() : RecyclerView.Adapter<ProductAdapter.ItemHolder>() {

    var listProduct: ArrayList<Product> = ArrayList();

//    private var billingClient: BillingClient? = null;


//    fun ProductAdapter(context: Context, listTrip: ArrayList<Product>) {
//        this.context = context
//        this.listProduct = listTrip
//    }
    fun setListTrip(products: ArrayList<Product>) {
        listProduct.clear()
        listProduct.addAll(products)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_product, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val product: Product = listProduct[position];
        holder.txtPrice.text = product.price.toString();
        holder.txtSlotPlay.text = product.value.toString();
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtSlotPlay: TextView
        var txtPrice: TextView
        var btnBuy: Button

        init {
            txtSlotPlay = itemView.findViewById(R.id.txt_slot_play)
            txtPrice = itemView.findViewById(R.id.txt_price_product)
            btnBuy = itemView.findViewById(R.id.btn_buy_product)
//            btnBuy.setOnClickListener {
//                val intent = Intent(context, DetailTripActivity::class.java)
//                intent.putExtra("trip_id", listTrip.get(position).getTripId().toString())
//                context.startActivity(intent)

//            }
//            btnBuy.setOnClickListener {
//
////                billingClient.launchBillingFlow(
////                            activity,
////                            BillingFlowParams
////                                .newBuilder()
////                                .setSkuDetails(itemInfo)
////                                .build()
////                        )
////                    }
        }
    }

    override fun getItemCount(): Int {
        return listProduct.size;
    }

}