package com.shinjaehun.takenotes

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.net.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.itextpdf.text.*
import com.shinjaehun.takenotes.databinding.ActivityMainPageBinding
import java.io.*

import com.itextpdf.text.pdf.PdfWriter

class MainPageActivity : AppCompatActivity() {

    companion object {
        private const val FCR = 1
        const val REQUEST_SELECT_FILE = 100
        private const val RC_APP_UPDATE = 11
    }

    private lateinit var binding: ActivityMainPageBinding
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private lateinit var mAppUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNetwork()

        // runtime external storage permission for saving download files
        checkPerms()

        // checking first time : this is for cougar onwards
        if (isFirstTime) {
            AlertDialog.Builder(this)
                .setTitle("Export Folder as PDF")
                .setMessage(R.string.instructions)
                .setCancelable(false)
                // null listener allow the button to dismiss the dialog and take no further action
                .setNeutralButton("Fantastic", null)
                .create().show()

            AlertDialog.Builder(this)
                .setTitle("Tutorial")
                .setMessage("Would you like to go through a quick tutorial to master take notes?")
                .setCancelable(false)
                .setNeutralButton("Sure") { _, _ ->
                    val url = "https://the-rebooted-coder.github.io/Take-Notes-Web/tutorial"
                    val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
                    builder.setToolbarColor(Color.parseColor("#006400"))
                    val customTabsIntent: CustomTabsIntent = builder.build()
                    customTabsIntent.launchUrl(this@MainPageActivity, Uri.parse(url))
                }
                .setNegativeButton("Nope") { _, _ ->
                    Toast.makeText(this, "You can view the tutorial from settings", Toast.LENGTH_SHORT).show()
                }
                .create().show()
        }
    }

    private fun checkNetwork() {
        if (haveNetwork()) {
            couchSit()
        } else if (!haveNetwork()) {
            val intent = Intent(this@MainPageActivity, NoInternetActivity::class.java)
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

    // onStart
    override fun onStart() {
        super.onStart()
        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        mAppUpdateManager.registerListener(installStateUpdatedListener)
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo, AppUpdateType.IMMEDIATE, this@MainPageActivity, RC_APP_UPDATE
                    )
                } catch (e : IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            } else {

            }
        }
    }

    private var installStateUpdatedListener: InstallStateUpdatedListener = object : InstallStateUpdatedListener {
        override fun onStateUpdate(state: InstallState) {
            if (state.installStatus() == InstallStatus.INSTALLED) {
                if (mAppUpdateManager != null) {
                    mAppUpdateManager.unregisterListener(this)
                }
            } else {
                // app is fully updated nothing to do continuing normal workflow
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mUMA == null) {
            return
        }
        mUMA?.onReceiveValue(arrayOf())
        mUMA = null
    }

//    @SuppressLint("SetJavaScriptEnabled") // 자동 변환기로는 얘가 나오는디...
//    private fun couchSit() {

//    @RequiresApi(Build.VERSION_CODES.O)
    private fun couchSit() {
        binding.wvPlugin.settings.javaScriptEnabled
        binding.wvPlugin.settings.cacheMode
        binding.wvPlugin.settings.domStorageEnabled
        binding.wvPlugin.settings.databaseEnabled
        binding.wvPlugin.webViewClient = WebViewClient()
        registerForContextMenu(binding.wvPlugin)
        binding.wvPlugin.settings.useWideViewPort
        binding.wvPlugin.setInitialScale(1)
        binding.wvPlugin.loadUrl("https://the-rebooted-coder.github.io/Take-Notes/")
        binding.wvPlugin.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
//                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                if (mUMA != null) {
                    mUMA?.onReceiveValue(null)
                    mUMA = null
                }

                mUMA = filePathCallback

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                try {
                    startActivityForResult(chooserIntent, REQUEST_SELECT_FILE)
                    Toast.makeText(this@MainPageActivity, "Pick a suitable file", Toast.LENGTH_SHORT).show()
                } catch (e: ActivityNotFoundException) {
                    mUMA = null
                    Toast.makeText(this@MainPageActivity, "Cannot open file picker", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }
        }
        binding.wvPlugin.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String
            ): Boolean {
                if (url == getString(R.string.take_notes_image_to_be_displayed)) {
//                if (url.matches(getString(R.string.take_notes_image_to_be_displayed))) { // 이건 Type mismatch: inferred type is String but Regex was expected
                    val intent: Intent = Intent(this@MainPageActivity, Settings::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()

                } else if (url == getString(R.string.print)) {
//                } else if (url.matches(R.string.print)) {
                    try {
                        val folderPath = File(Environment.getExternalStorageDirectory().toString() + "/Documents/TakeNotes" )
                        val imageList = folderPath.listFiles()
                        val imagesArrayList = ArrayList<File>()
                        for (absolutePath in imageList) {
                            imagesArrayList.add(absolutePath)
                        }
                        CreatePdfTask(this@MainPageActivity, imagesArrayList).execute()
                    } catch (e: Exception) {
                        AlertDialog.Builder(this@MainPageActivity)
                            .setTitle("No Image Found")
                            .setMessage(R.string.no_img)
                            .setCancelable(false)
                            .setPositiveButton("I know") { _, _ ->
                                val i = Intent(this@MainPageActivity, MainPageActivity::class.java)
                                startActivity(i)
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                finish()
                            }
                            .create().show()
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }
        }

        binding.wvPlugin.setDownloadListener { url, _, _, _, _ ->
//        binding.wvPlugin.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    val m = StrictMath::class.java.getMethod("disableDeathOnFileUriExposure")
                    m.invoke(null)
                    if (url.startsWith("date:")) {
                        val path: String? = createAndSaveFileFromBase64Url(url)
                        return@setDownloadListener
                    }
                    val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
                    val dm: DownloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(request)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun displayExceptionMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkPerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissions, 1)
                val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)
                if (account != null) {
                    val personName: String? = account.displayName
                    Toast.makeText(this, "Howdy $personName you are in!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Welcome to Take Notes!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.not_yet_in, Toast.LENGTH_SHORT).show()
                    val i = Intent(this@MainPageActivity, SignUpActivity::class.java)
                    startActivity(i)
                }
            }
        }
    }

    private fun createAndSaveFileFromBase64Url(url: String): String? {
        val path: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/TakeNotes")
        val filetype : String = url.substring(url.indexOf("/") + 1, url.indexOf(";"))
        val account : GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)
        val personName = account?.displayName
        val fileName = personName + "s notes " + System.currentTimeMillis() + "." + filetype
        Toast.makeText(this, R.string.success_toast, Toast.LENGTH_SHORT).show()
        val file = File(path, fileName)
        try {
            if (!path.exists())
                path.mkdir()
            if (!file.exists())
                file.createNewFile()

            val base64EncodedString: String = url.substring(url.indexOf(",") + 1)
            val decodeBytes = Base64.decode(base64EncodedString, Base64.DEFAULT)
            val os : OutputStream = FileOutputStream(file)
            os.write(decodeBytes)
            os.close()

            // tell the media scanner about the file so that it is immediately available to the user
            MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null) { _, uri ->
                Log.i("onScanCompleted", "Scanned $path:")
                Log.i("onScanCompleted", "-> uri=$uri")
            }
//            MediaScannerConnection.scanFile(this, arrayOf<String>(file.toString()), null, object : OnScanCompletedListener {
//                override fun onScanCompleted(path: String?, uri: Uri?) {
//                    Log.i("onScanCompleted", "Scanned $path:")
//                    Log.i("onScanCompleted", "-> uri=$uri")
//                }
//            })

            //Set notification after download complete and add "click to view" action to that
            //we don't need notification

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationId = 1
                val CHANNEL_ID = "SavedReminderService"
                val notificationManager: NotificationManager? = getSystemService(
                    NOTIFICATION_SERVICE
                ) as NotificationManager?
                val mimetype = url.substring(url.indexOf(":") + 1, url.indexOf("/"))
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(Uri.fromFile(file), "$mimetype/*")
                val pIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                val notificationChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Notes Saved Notification",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val notification: Notification = Notification.Builder(this, CHANNEL_ID)
                    .setContentText("Tap to Check Now!")
                    .setContentTitle("Your generated note got saved")
                    .setContentIntent(pIntent)
                    .setColor(resources.getColor(R.color.notification))
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo_take_notes)
                    .setAutoCancel(true)
                    .build()
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel)
                    notificationManager.notify(notificationId, notification)
                }
            } else {
                val mimetype = url.substring(url.indexOf(":") + 1, url.indexOf("/"))
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(Uri.fromFile(file), "$mimetype/*")
                val pIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
                val notification: Notification = NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logo_take_notes)
                    .setColor(resources.getColor(R.color.notification))
                    .setContentText("Tap to Check Now!")
                    .setContentTitle("Your generated note got saved")
                    .setContentIntent(pIntent)
                    .build()
                notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
                val notificationId = 85851
                val notificationManager: NotificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(notificationId, notification)
            }

        } catch (e: IOException) {
            Log.w("ExternalStorage", "Error writing $file", e)
            Toast.makeText(applicationContext, R.string.error_downloading, Toast.LENGTH_LONG)
                .show()
        }
        return file.toString()
    }

    override fun onStop() {
        super.onStop()
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (mUMA == null) return
                mUMA?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                mUMA = null
            }
        } else if (requestCode == FCR) {
            if (null == mUM) return
            val result: Uri? =
                if (intent == null || resultCode != Activity.RESULT_OK) {
                    null
                } else {
                    intent.data
                }
            mUM?.onReceiveValue(result)
            mUM = null
        } else Toast.makeText(this, R.string.failed_to_load_fnt, Toast.LENGTH_LONG).show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (binding.wvPlugin.canGoBack()) {
                    binding.wvPlugin.goBack()
                } else {
                    finish()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    inner class CreatePdfTask(var context: Context, var files: ArrayList<File>) :
        AsyncTask<String?, Int?, File?>() {
        var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog?.setTitle("Hmmmm...")
            progressDialog?.setMessage(getString(R.string.advice))
            progressDialog?.isIndeterminate = false
            progressDialog?.setCancelable(false)
            progressDialog?.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "I Know!"
            ) { _, _ ->
                (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
                    .clearApplicationUserData()
            }
//            progressDialog?.setButton(
//                DialogInterface.BUTTON_NEGATIVE,
//                "I Know!",
//                object : DialogInterface.OnClickListener {
//                    override fun onClick(dialog: DialogInterface, which: Int) {
//                        (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
//                            .clearApplicationUserData()
//                    }
//                })
            progressDialog?.show()
        }

        override fun doInBackground(vararg p0: String?): File? {
            val account: GoogleSignInAccount? =
                GoogleSignIn.getLastSignedInAccount(applicationContext)
            val username: String? = account?.displayName
            val outputMediaFile = File(
                Environment.getExternalStorageDirectory(),
                Environment.DIRECTORY_DOCUMENTS + "/" + username + System.currentTimeMillis() + ".pdf"
            )
            val document = Document(PageSize.A4, 38.0f, 38.0f, 50.0f, 38.0f)
            try {
                PdfWriter.getInstance(document, FileOutputStream(outputMediaFile))
            } catch (e: DocumentException) {
                e.printStackTrace()
                progressDialog?.dismiss()
            } catch (e: ExceptionConverter) {
                e.printStackTrace()
                progressDialog?.dismiss()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                progressDialog?.dismiss()
                return null
            }
            document.open()
            try {
                document.add(Chunk(""))
            } catch (e: DocumentException) {
                e.printStackTrace()
            }
            var i = 0
            while (true) {
                if (i < files.size) {
                    try {
                        val image: Image = Image.getInstance(files[i].absolutePath)
                        val scaler: Float =
                            (document.pageSize.width - document.leftMargin()
                                    - document.rightMargin() - 0) / image.width * 100
                        // 0 means you have no indentation. If you have any, change it.
                        image.scalePercent(scaler)
                        image.alignment = Image.ALIGN_CENTER or Image.ALIGN_TOP
                        image.setAbsolutePosition(
                            (document.pageSize.width - image.scaledWidth) / 2.0f,
                            (document.pageSize.height - image.scaledHeight) / 2.0f
                        )
                        document.add(image)
                        document.newPage()
                        publishProgress(i)
                        i++
                    } catch (e: BadElementException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: DocumentException) {
                        e.printStackTrace()
                    }
                } else {
                    document.close()
                    return outputMediaFile
                }
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            progressDialog?.dismiss()
        }

        override fun onPostExecute(result: File?) {
            super.onPostExecute(result)
            progressDialog?.dismiss()
        }

    }

    private val isFirstTime: Boolean
        get() {
            val preferences: SharedPreferences = getPreferences(Context.MODE_PRIVATE)
            val ranBefore: Boolean = preferences.getBoolean("RanBefore", false)
            if (!ranBefore) {
                //first time
                val editor: SharedPreferences.Editor = preferences.edit()
                editor.putBoolean("RanBefore", true)
//                editor.commit()
                editor.apply()
            }
            return !ranBefore
        }

//    private class CreatePdfTask {
//        fun execute() {
//
//        }
//
//        constructor()
//
//        constructor(mainPageActivity: MainPageActivity, imagesArrayList: ArrayList<File>)
//    }
}
