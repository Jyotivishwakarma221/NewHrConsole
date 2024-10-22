package com.investmango.hrconsole.AwsUpload

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContentProviderCompat.requireContext
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadFileAws() {
    lateinit var apiInterface: ApiInterface

    fun getRealPathFromUri(uri: Uri, context: Context): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            context!!.contentResolver.query(uri, projection, null, null, null) ?: return null


        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    suspend fun uploadFile(file: File, folderName: String, context: Context): String? =
        withContext(Dispatchers.IO) {
            val apiClient = ApiClient(context)
            val apiInterface = apiClient.apiInterface
            var uriStr = ""

            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            try {
                val response = apiInterface.saveImage(body, folderName).execute()
                if (response.isSuccessful) {
                    uriStr = JSONObject(response.body().toString()).optString("message")
                    uriStr
                } else null
            } catch (e: Exception) {
                ""
            }
        }

    suspend fun deleteFile(file: String, context: Context): String? = withContext(Dispatchers.IO) {
        val apiClient = ApiClient(context)
        val apiInterface = apiClient.apiInterface
        var responseStr = ""
        try {
            val response = apiInterface.deleteDocument(file).execute()
            if (response.isSuccessful) {
                responseStr = JSONObject(response.body().toString()).optString("message")
                responseStr
            } else null
        } catch (e: Exception) {
            ""
        }
    }

    fun openGallery(launcher: ActivityResultLauncher<Intent>) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        launcher.launch(galleryIntent)
    }

}