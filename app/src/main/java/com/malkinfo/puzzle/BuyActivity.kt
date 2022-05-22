package com.malkinfo.puzzle

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.malkinfo.numberpulzzgame.R
import com.malkinfo.puzzle.adapter.ProductAdapter
import org.json.JSONObject

class BuyActivity : AppCompatActivity() {
    private var billingClient: BillingClient? = null;
    private var productAdapter: ProductAdapter? = null;
    private lateinit var buttonHome: Button;
    var recyclerViewProduct: RecyclerView? = null;
    var products: ArrayList<Product> = ArrayList();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)
        setup()

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
                        verifyPurchase(purchase)
                    }
                }
            }
        }
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
                        ) { billingResult, s ->
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
                        getProductDetails()
                    }
                }
            }
        )
    }

    private fun getProductDetails() {
        val productIds: ArrayList<String> = ArrayList();
        val one = "one_1";
        val five = "five_5";
        productIds.add(one);
        productIds.add(five);
        val getProductDetailsQuery = SkuDetailsParams
            .newBuilder()
            .setSkusList(productIds)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        val activity: Activity = this
        billingClient?.querySkuDetailsAsync(
            getProductDetailsQuery,
            SkuDetailsResponseListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                    && list != null
                ) {
                    list.forEach {
                        val productOnGooglePlay = Product(it.title,  it.price, 5)
                        products.add(productOnGooglePlay);
                    }
                    System.out.println("Products Size: aaaaaaa " + products.size)
                    productAdapter?.setListTrip(products)
                    productAdapter?.notifyDataSetChanged()
//                    val itemNameTxtView = findViewById<TextView>(R.id.txtOneTime)
//                    val itemPriceButton = findViewById<View>(R.id.btnOneTime) as Button
//                    val itemInfo = list[0]
//                    itemNameTxtView.text = itemInfo.title
//                    println("price: " + itemInfo.price)
//                    itemPriceButton.text = itemInfo.price
//                    println("price: " + itemPriceButton.text)
//                    itemPriceButton.setOnClickListener {
//                        billingClient.launchBillingFlow(
//                            activity,
//                            BillingFlowParams
//                                .newBuilder()
//                                .setSkuDetails(itemInfo)
//                                .build()
//                        )
//                    }
                }
            }
        )
    }

    private fun setup() {
        productAdapter = ProductAdapter()
        recyclerViewProduct = findViewById(R.id.recycler_view_product);
        recyclerViewProduct?.setHasFixedSize(true)
        recyclerViewProduct?.layoutManager = GridLayoutManager(applicationContext, 1)
        recyclerViewProduct?.adapter = productAdapter;
    }

}