package com.example.homecontrol

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import io.realm.Realm
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*


const val MIME_TEXT_PLAIN = "text/plain"
var username = ""
var password = ""
class LoginActivity : AppCompatActivity() {

    private lateinit var mqttClient: MqttAndroidClient
    // TAG
    companion object {
        const val TAG = "AndroidMqttClient"
    }

    var sucesfullyConnected: Boolean = true

    private var etUsername: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var tvWrongUsernameOrPasswordMessage: TextView? = null
    private var pbLoading: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //val realm = Realm.getDefaultInstance()


        initViews()
        this.btnLogin?.setOnClickListener {
            this.pbLoading?.visibility = View.VISIBLE
            /*try {
                realm.beginTransaction()
                val newId = UUID.randomUUID().toString()
                val user: LoggedUser = realm.createObject(LoggedUser::class.java, newId)
                user.username = etUsername?.text.toString()
                user.password = etPassword?.text.toString()
                realm.commitTransaction()

            }
            catch (e: Exception) {
                Toast.makeText(this, "Nastala chyba při ukládání uživatelského jména do databáze", Toast.LENGTH_SHORT).show()}*/
            connect()


        }

    }



    private fun initViews(){
        this.etUsername = findViewById(R.id.et_username)
        this.etPassword = findViewById(R.id.et_password)
        this.btnLogin = findViewById(R.id.btn_login)
        this.tvWrongUsernameOrPasswordMessage = findViewById(R.id.tv_wrong_username_or_password_messge)
        this.pbLoading = findViewById(R.id.loading)
    }



    fun connect(){



        val broker: String   = "tcp://webelias.site:1883"
        val clientId: String = this.etUsername?.text.toString()
        val persistence = MemoryPersistence()


        try {
            val sampleClient = MqttClient(broker, clientId, persistence)
            val connOpts = MqttConnectOptions()
            connOpts.userName = this.etUsername?.text.toString()
            connOpts.password = this.etPassword?.text.toString().toCharArray()
            connOpts.setCleanSession(true)
            sampleClient.connect(connOpts)
            sampleClient.disconnect()

        }
        catch(e: MqttException) {
            this.tvWrongUsernameOrPasswordMessage?.visibility = View.VISIBLE
            this.pbLoading?.visibility = View.INVISIBLE
            this.sucesfullyConnected = false

        }
        if (sucesfullyConnected){
            this.pbLoading?.visibility = View.INVISIBLE
            startMainActivity()
        }



    }

    private fun startMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        username = etUsername?.text.toString()
        password = etPassword?.text.toString()
        startActivity(intent)

    }






}