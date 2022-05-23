package com.malkinfo.puzzle

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var btnOne: Button;
    private lateinit var btnFive: Button;
    var recyclerViewProduct: RecyclerView? = null;
    var products: ArrayList<Product> = ArrayList();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

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
//        handleButtonBuy();
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
                        handleButtonBuy()
                        handleButtonBuy2()
                    }
                }
            }
        )
    }

    private fun handleButtonBuy() {
        btnOne = findViewById(R.id.one_1);
        val productId = "one_1";
        val getProductDetailsQuery = SkuDetailsParams
            .newBuilder()
            .setSkusList(listOf(productId))
            .setType(BillingClient.SkuType.INAPP)
            .build()
        val activity: Activity = this
        billingClient?.querySkuDetailsAsync(
            getProductDetailsQuery
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                && list != null
            ) {
                val itemInfo = list[0]
                btnOne.text = itemInfo.price
                btnOne.setOnClickListener {
                    Toast.makeText(this, "btn One clicked", Toast.LENGTH_LONG).show();
                    billingClient!!.launchBillingFlow(
                        activity,
                        BillingFlowParams
                            .newBuilder()
                            .setSkuDetails(itemInfo)
                            .build()
                    )
                }
            }
        }
    }
    private fun handleButtonBuy2() {
        btnFive = findViewById(R.id.five_5);
        val productId = "five_5";
        val getProductDetailsQuery = SkuDetailsParams
            .newBuilder()
            .setSkusList(listOf(productId))
            .setType(BillingClient.SkuType.INAPP)
            .build()
        val activity: Activity = this
        billingClient?.querySkuDetailsAsync(
            getProductDetailsQuery
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
                && list != null
            ) {
                val itemInfo = list[0]
                btnFive.text = itemInfo.price
                btnFive.setOnClickListener {
                    Toast.makeText(this, "btn five clicked", Toast.LENGTH_LONG).show();
                    billingClient!!.launchBillingFlow(
                        activity,
                        BillingFlowParams
                            .newBuilder()
                            .setSkuDetails(itemInfo)
                            .build()
                    )
                }
            }
        }
    }

}