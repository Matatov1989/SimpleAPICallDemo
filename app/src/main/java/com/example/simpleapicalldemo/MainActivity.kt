package com.example.simpleapicalldemo

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CallAPILoginAsyncTask("denis", "123456").execute()
    }

    private inner class CallAPILoginAsyncTask(val username: String, val password: String) :
        AsyncTask<Any, Void, String>() {
        private lateinit var customProgressDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result: String

            var connection: HttpURLConnection? = null

            try {
                val url = URL("http://www.mocky.io/v2/5e3826143100006a00d37ffa")
                connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true

                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.useCaches = false

                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                jsonRequest.put("username", username)
                jsonRequest.put("password", password)

                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

                val httpResult: Int = connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?

                    try {
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error: ${e.message}"
            } finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            Log.i("JSON_RESPONSE", result.toString())

            parseJsonByGson(result!!)

//            parseJsonOld(result!!)
        }

        private fun showProgressDialog() {
            customProgressDialog = Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.show()
        }

        private fun cancelProgressDialog() {
            customProgressDialog.dismiss()
        }
    }

    private fun parseJsonByGson(result: String) {
        val responseData = Gson().fromJson(result, ResponseData::class.java)
        Log.i("MESSAGE", "${responseData.message}")
        Log.i("USER_ID", "${responseData.user_id}")
        Log.i("NAME", "${responseData.name}")
        Log.i("MAIL", "${responseData.email}")
        Log.i("MOBILE", "${responseData.mobile}")

        Log.i("IS_COMPLETED", "${responseData.profile_details.is_profile_completed}")
        Log.i("rating", "${responseData.profile_details.rating}")

        for (item in responseData.data_list.indices) {
            Log.i("ARRAY_ITEM", "${responseData.data_list[item]}")
            Log.i("ARRAY_ITEM_ID", "${responseData.data_list[item].id}")
            Log.i("ARRAY_ITEM_VALUE", "${responseData.data_list[item].value}")
        }
    }

    private fun parseJsonOld(result: String) {
        val jsonObject = JSONObject(result)
        val message = jsonObject.optString("message")
        Log.i("MESSAGE", message)
        val userId = jsonObject.optString("user_id")
        Log.i("USER_ID", userId)
        val name = jsonObject.optString("name")
        Log.i("NAME", name)

        val profileDetailsObject = jsonObject.getJSONObject("profile_details")
        val isProfileComplete = profileDetailsObject.getBoolean("is_profile_completed")
        Log.i("IS_COMPLETED", "$isProfileComplete")

        val dataListArray = jsonObject.optJSONArray("data_list")
        Log.i("ARRAY_SIZE", "${dataListArray.length()}")

        for (item in 0 until dataListArray.length()) {
            Log.i("ARRAY_ITEM", "${dataListArray[item]}")

            val dataItemObject: JSONObject = dataListArray[item] as JSONObject

            val id = dataItemObject.optInt("id")
            Log.i("ARRAY_ITEM_ID", "$id")

            val value = dataItemObject.optString("value")
            Log.i("ARRAY_ITEM_VALUE", "$value")
        }
    }
}
