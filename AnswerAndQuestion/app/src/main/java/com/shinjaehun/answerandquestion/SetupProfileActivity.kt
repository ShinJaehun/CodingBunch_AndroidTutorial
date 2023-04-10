package com.shinjaehun.answerandquestion

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.shinjaehun.answerandquestion.databinding.ActivitySetupProfileBinding
import com.shinjaehun.answerandquestion.model.User
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "SetupProfileActivity"

class SetupProfileActivity : AppCompatActivity() {

    var binding: ActivitySetupProfileBinding? = null
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        dialog = ProgressDialog(this@SetupProfileActivity)
        // 헐 미친 개새끼 이거 tutorial에는 분명히 빠져 있는데 이렇게 엿 맥일려고...
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        var getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImage = result.data?.data
//                Log.i(TAG, "photoUri: $selectedImage")
                binding!!.ivProfile.setImageURI(selectedImage)
            } else {
                Toast.makeText(this, "Image picker action canceled", Toast.LENGTH_SHORT).show()
            }
        }

        // original code: startActivityForResult() deprecated
//        binding!!.ivProfile.setOnClickListener {
//            val intent = Intent()
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.type = "image/*"
//            startActivityForResult(intent, 45)
//        }

        binding!!.ivProfile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            getResult.launch(intent)
        }

        // original code: dialog deprecated, wrong imageUri
//        binding!!.btnProfile.setOnClickListener {
//            val name: String = binding!!.etName.text.toString()
//            if (name.isEmpty()) {
//                binding!!.etName.setError("Please type a name")
//            }
//            dialog!!.show()
//            if (selectedImage != null) {
//                val reference = storage!!.reference.child("Profile")
//                    .child(auth!!.uid!!)
//                reference.putFile(selectedImage!!).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        reference.downloadUrl.addOnCompleteListener {  uri ->
//                            val imageUri = uri.toString()
//                            val uid = auth!!.uid
//                            val phone = auth!!.currentUser!!.phoneNumber
//                            val name: String = binding!!.etName.text.toString()
//                            val user = User(uid, name, phone, imageUri)
//
//                            database!!.reference
//                                .child("users")
//                                .child(uid!!)
//                                .setValue(user)
//                                .addOnCompleteListener {
//                                    dialog!!.dismiss()
//                                    val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
//                                    startActivity(intent)
//                                    finish()
//                                }
//                        }
//                    } else {
//                        val uid = auth!!.uid
//                        val phone = auth!!.currentUser!!.phoneNumber
//                        val name: String = binding!!.etName.text.toString()
//                        val user = User(uid, name, phone, "No Image")
//                        database!!.reference
//                            .child("users")
//                            .child(uid!!)
//                            .setValue(user)
//                            .addOnCanceledListener {
//                                dialog!!.dismiss()
//                                val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
//                                startActivity(intent)
//                                finish()
//                            }
//                    }
//                }
//            }
//
//        }

        binding!!.btnProfile.setOnClickListener {
            val name: String = binding!!.etName.text.toString()
            if (name.isEmpty()) {
                binding!!.etName.setError("Please type a name")
            }

            binding!!.btnProfile.isEnabled = false

            if (selectedImage != null) {
                val reference = storage!!.reference.child("Profile/${System.currentTimeMillis()}-photo.jpg")
                reference.putFile(selectedImage!!).continueWithTask { task ->
                    Log.i(TAG, "uploaded bytes: ${task.result?.bytesTransferred}")
                    reference.downloadUrl
                }.continueWithTask { task ->
                    val uri = task.result.toString()
                    Log.i(TAG, "imageUri: $uri")
                    val uid = auth!!.uid
                    val phone = auth!!.currentUser!!.phoneNumber
                    val name: String = binding!!.etName.text.toString()
                    val user = User(uid, name, phone, uri)

                    database!!.reference
                        .child("users")
                        .child(uid!!)
                        .setValue(user)
                }.addOnCompleteListener {
                    binding!!.btnProfile.isEnabled = true

                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                val uid = auth!!.uid
                val phone = auth!!.currentUser!!.phoneNumber
                val name: String = binding!!.etName.text.toString()
                val user = User(uid, name, phone, "No Image")

                database!!.reference
                    .child("users")
                    .child(uid!!)
                    .setValue(user)
                    .addOnCompleteListener {
                        binding!!.btnProfile.isEnabled = false

                        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
        }
    }

    // original code: onActivityResult() deprecated
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (data != null) {
//            if (data.data != null) {
//                val uri = data.data // filePath
//                val storage = FirebaseStorage.getInstance()
//                val time = Date().time
//                val reference = storage.reference
//                    .child("Profile")
//                    .child(time.toString() + "")
//                reference.putFile(uri!!).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        reference.downloadUrl.addOnCompleteListener { uri ->
//                            val filePath = uri.toString()
//                            val obj = HashMap<String, Any>()
//                            obj["image"] = filePath
//                            database!!.reference
//                                .child("users")
//                                .child(FirebaseAuth.getInstance().uid!!)
//                                .updateChildren(obj).addOnSuccessListener {  }
//                        }
//                    }
//                }
//                binding!!.ivProfile.setImageURI(data.data)
//                selectedImage = data.data
//            }
//        }
//    }
}