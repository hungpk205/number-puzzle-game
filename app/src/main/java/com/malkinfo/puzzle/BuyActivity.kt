package com.malkinfo.puzzle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.malkinfo.numberpulzzgame.R
import org.json.JSONObject


class BuyActivity : AppCompatActivity() {
    private var billingClient: BillingClient? = null
    private lateinit var buttonHome: Button
    var listViewProduct: ListView? = null
    var arrayAdapter: ArrayAdapter<String?>? = null
    var products: ArrayList<Product> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)
        listViewProduct = findViewById(R.id.list_view_product)

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            verifyPurchase(purchase)
                        }
                    }
                }
            }
            .build()

        handleHome();
        connectToGooglePayBilling();

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, purchaseItemDisplay)
        listViewProduct!!.adapter = arrayAdapter
        notifyList()
        listViewProduct!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (billingClient!!.isReady) {
                initiatePurchase(purchaseItemIDs[position])
            }
        }
    }

    private fun handleHome() {
        buttonHome = findViewById(R.id.btnHome);
        buttonHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
    override fun onResume() {
        super.onResume()
        billingClient!!.queryPurchasesAsync(
            BillingClient.SkuType.INAPP
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
//                        verifyPurchase(purchase)
                        updateSlotPlay(5)
                    }
                }
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun updateSlotPlay(slotPlay: Int) {
        val settings = applicationContext.getSharedPreferences("SLOT_PLAY", 0)
        val editor = settings.edit()
        val currentSlot = settings.getString("SLOT_PLAY", "1")?.toInt()
        val newSlot = currentSlot?.plus(slotPlay)
        editor.putString("SLOT_PLAY", newSlot.toString())
    }

    private fun verifyPurchase(purchase: Purchase) {
        val requestUrl = "https://us-central1-puzzle-7e64f.cloudfunctions.net/verifyPurchases?" +
                "purchaseToken=" + purchase.purchaseToken + "&" +
                "purchaseTime=" + purchase.purchaseTime + "&" +
                "orderId=" + purchase.orderId
        val activity: Activity = this
        val stringRequest = StringRequest(
            Request.Method.POST,
            requestUrl,
            { response ->
                try {
                    val purchaseInfoFromServer = JSONObject(response)
                    if (purchaseInfoFromServer.getBoolean("isValid")) {
                        val consumeParams =
                            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                                .build()
                        billingClient!!.consumeAsync(
                            consumeParams
                        ) { billingResult, _ ->
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                Toast.makeText(activity, "Verified Purchase", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                } catch (err: Exception) {
                }
            }
        ) { }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun connectToGooglePayBilling() {
        billingClient!!.startConnection(
            object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    connectToGooglePayBilling()
                }
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    }
                }
            }
        )
    }

    companion object {
        const val PREF_FILE = "MyPref"

        //note add unique product ids
        //use same id for preference key
        private val purchaseItemIDs: ArrayList<String> = object : ArrayList<String>() {
            init {
                add("one_1")
                add("five_5")
                add("ten_10")
                add("twenty_20")
                add("fifty_50")
                add("hundred_100")
                add("twohundred_200")
                add("fivehundred_500")
            }
        }
        private val purchaseItemDisplay: ArrayList<String?> = ArrayList<String?>()
    }
    private fun notifyList() {
        purchaseItemDisplay.clear()
        for (p in purchaseItemIDs) {
            val xPrice = p.substring(p.indexOf("_") + 1)
            println(xPrice)
            purchaseItemDisplay.add("$xPrice for $xPrice times play")
        }
        arrayAdapter!!.notifyDataSetChanged()
    }

    private val preferenceObject: SharedPreferences
        get() = applicationContext.getSharedPreferences(PREF_FILE, 0)
    private val preferenceEditObject: SharedPreferences.Editor
        get() {
            val pref = applicationContext.getSharedPreferences(PREF_FILE, 0)
            return pref.edit()
        }
    private fun getPurchaseCountValueFromPref(PURCHASE_KEY: String): Int {
        return preferenceObject.getInt(PURCHASE_KEY, 1)
    }

    private fun savePurchaseCountValueToPref(PURCHASE_KEY: String, value: Int) {
        preferenceEditObject.putInt(PURCHASE_KEY, value).commit()
    }

    private fun initiatePurchase(PRODUCT_ID: String) {
        val skuList: MutableList<String> = ArrayList()
        skuList.add(PRODUCT_ID)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient!!.querySkuDetailsAsync(params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[0])
                        .build()
                    billingClient!!.launchBillingFlow(this, flowParams)
                } else {
                    //try to add item/product id "c1" "c2" "c3" inside managed product in google play console
                    Toast.makeText(applicationContext, "Purchase Item $PRODUCT_ID not Found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext,
                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

}