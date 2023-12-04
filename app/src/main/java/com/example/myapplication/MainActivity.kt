package com.example.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var userField: EditText
    private lateinit var mainButton: Button
    private lateinit var resultInfo: TextView

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userField = findViewById(R.id.user_field)
        mainButton = findViewById(R.id.main_button)
        resultInfo = findViewById(R.id.result_info)
        val buffer = StringBuffer()
        var result = ""

        mainButton.setOnClickListener {
            if (userField.text.toString().trim() == "") {
                Toast.makeText(this, R.string.no_input, Toast.LENGTH_LONG).show()
            } else {
                val city = userField.text.toString()
                val key = "9eaa0f8a337234fc6e0bc3ca2f5a78dd"
                val urlAddress =
                    "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$key&units=metric&lang=ru"

                //instead of an excess class we use Executor:

                executor.execute {

                    //onPreExecute:
                    resultInfo.text = "Loading..."


                    lateinit var connection: HttpURLConnection
                    lateinit var reader: BufferedReader

                    try {
                        val url = URL(urlAddress)
                        connection = url.openConnection() as HttpURLConnection
                        connection.connect()

                        val inputStream: InputStream = connection.inputStream
                        reader = BufferedReader(InputStreamReader(inputStream))

//                        var line: String

//                        while (true) {
//                            line = reader.readLine()
//                            buffer.append(line).append("\n")
//                        }
                        val line: String = reader.readLine()
                        reader.lineSequence().forEach { _ ->
                            buffer.append(line).append("\n")
                        }
                        result = buffer.toString()

                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        connection.disconnect()
                        reader.close()
                    }
                } //its like doInBackground()


                handler.postDelayed({
                    try {
                        val jsonObject = JSONObject(result)

                        resultInfo.text = """
                                        |Temperature: ${jsonObject.getJSONObject("main").getDouble("temp")}
                                        |Humidity: ${jsonObject.getJSONObject("main").getInt("humidity")}
                                        |Wind speed: ${jsonObject.getJSONObject("main").getJSONObject("wind").getDouble("speed")}
                                    """.trimIndent()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, 1000L) //its like onPostExecute()
            }
        }
    }
}