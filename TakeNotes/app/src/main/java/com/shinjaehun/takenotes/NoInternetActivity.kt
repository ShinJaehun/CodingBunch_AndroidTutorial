package com.shinjaehun.takenotes

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.shinjaehun.takenotes.databinding.ActivityNoInternetBinding

private const val TAG = "NoInternetActivity"
class NoInternetActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var binding: ActivityNoInternetBinding

    //private lateinit var cm: ConnectivityManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoInternetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이렇게 하지 말고 callback을 등록/해제하는 함수를 별도로 둡시다.
        // cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // val builder = NetworkRequest.Builder()
        // cm.registerNetworkCallback(builder.build(), networkCallback)

        // 옛날 방식
        // checkNetwork()

        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        binding.btnData.setOnClickListener {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect = VibrationEffect.createOneShot(30, 100)
            vibrator.vibrate(vibrationEffect)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val intent: Intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
                startActivity(intent)
            } else {
                val intent: Intent = Intent()
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.setAction(Settings.ACTION_DATA_ROAMING_SETTINGS)
                startActivity(intent)
            }
        }

        binding.sWifi.setChecked(wifiManager.isWifiEnabled)
        binding.sWifi.setOnCheckedChangeListener{ _, isChecked ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent: Intent = Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
//                startActivityForResult(intent, 545)
                startActivity(intent)
            } else {
                wifiManager.setWifiEnabled(isChecked)
                val vibrator_like_actual_switch:Long = 100
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val vibrationEffect = VibrationEffect.createOneShot(30, 100)
                    vibrator.vibrate(vibrationEffect)
                }, vibrator_like_actual_switch)

                // 이 새낀 무슨 vibrator 성애자냐
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val vibrationEffect = VibrationEffect.createOneShot(30, 100)
                vibrator.vibrate(vibrationEffect)

            }
        }

    }

    // 한번 체크 후
    // wifi 버튼 눌러서 활성화시키면 변화 없음 => 여기서 바로 넘어가게 만들고 싶은데.... 이건 나중에 구현하도록 합시다.
    // 근데 다시 wifi 버튼 눌러서 Done 누르면
    // 접속 성공

    // 그러니까 wifi 버튼을 누름과 동시에 체크가 끝남
    // dis
    // 버튼 -> dis -> 활성화시킴(con)
    // 하지만 이미 dis 상태인거지...
    // 여기서 버튼을 한번 더 누르면 -> con 상태임!

    // 그러니까 이런 과거 방식을 사용하지 말고

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 545) {
//            checkNetwork()
//        }
//    }
//
//    private fun checkNetwork() {
//        if (haveNetwork()) {
//            Log.i(TAG, "체크끝")
//            val intent: Intent = Intent(this@NoInternetActivity, MainPageActivity::class.java)
//            startActivity(intent)
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//            finish()
//        }
//    }
//
//    private fun haveNetwork(): Boolean {
//        // 뭐 되게 복잡했는데 걍 검색해서 최근꺼 반영했음!
//        Log.i(TAG, "체크중")
//        val connectivityManager: ConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        if (connectivityManager != null) {
//            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//            if (capabilities != null) {
//                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                    Log.i(TAG, "network connected by cellular")
//                    return true
//                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                    Log.i(TAG, "network connected by WIFI")
//                    return true
//                }
//            }
//        }
//        Log.i(TAG, "network DISCONNECTED")
//        return false
//    }

    private val networkCallBack = object: ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            // 네트워크가 연결될 때 호출됩니다.
            Toast.makeText(this@NoInternetActivity, "연결성공$network",Toast.LENGTH_SHORT).show()

            val intent: Intent = Intent(this@NoInternetActivity, MainPageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        override fun onLost(network: Network) {
            // 네트워크가 끊길 때 호출됩니다.
            Toast.makeText(this@NoInternetActivity,"연결실패",Toast.LENGTH_SHORT).show()

            // 여기서는 뭘 해야지?
        }
    }

    // 콜백을 등록하는 함수
    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallBack)
    }

    // 콜백을 해제하는 함수
    private fun terminateNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.unregisterNetworkCallback(networkCallBack)
    }

    override fun onResume() {
        super.onResume()
        registerNetworkCallback()
    }

    override fun onStop() {
        super.onStop()
        terminateNetworkCallback()
    }

}