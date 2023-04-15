package com.shinjaehun.takenotes

import android.content.Context
import android.content.Intent
import android.net.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        checkNetwork()
    }

    private fun checkNetwork() {
        if (haveNetwork()) {

        } else if (!haveNetwork()) {
            val intent: Intent = Intent(this@MainPageActivity, NoInternetActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun haveNetwork(): Boolean {
        // 뭐 되게 복잡했는데 걍 검색해서 최근꺼 반영했음!
        val connectivityManager: ConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                }
            }
        }
        return false
    }

    // NoInternetActivity와 달리 여기에서는 NetworkCallBack을 사용하지 않았는데 그 이유는!
    // https://stackoverflow.com/questions/70324348/onlost-in-networkcallback-doesnt-work-when-i-launch-the-app

}