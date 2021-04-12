package com.example.homecontrol

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmObject
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


class MainActivity : AppCompatActivity() {

    private var helper: ItemTouchHelper? = null
    private var actualUsername: TextView? = null
    private var nfcAdapter: NfcAdapter? = null
    private var incomingHash: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initViews()

        val addNewTag : FloatingActionButton = findViewById(R.id.addTag)
        addNewTag.setOnClickListener {
            val intent = Intent(this, AddNewTag::class.java)
            //intent.putExtra("Username", username)
            startActivity(intent)
        }

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)?.let { it }
        val isNfcSupported: Boolean = this.nfcAdapter != null

        if (!isNfcSupported) {
            Toast.makeText(this, "NFC není podporováno", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC není zapnuté", Toast.LENGTH_SHORT).show()
        }
        initViews()

    }

    private fun initViews(){
        actualUsername = findViewById(R.id.tv_activity_main_username)

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveMessageFromDevice(intent)
        val realm = Realm.getDefaultInstance()

        val tags = mutableListOf<MyTag>()
        tags += realm.where(MyTag::class.java).findAll()

        for (i in tags){
            if (i.hashName.equals(incomingHash)){
                send(i.name.toString())
            }
        }

    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch(this, this.nfcAdapter)
    }

    override fun onResume() {
        super.onResume()

        enableForegroundDispatch(this, this.nfcAdapter)
        receiveMessageFromDevice(intent)

        val realm = Realm.getDefaultInstance()



        val tags = mutableListOf<MyTag>()

        tags += realm.where(MyTag::class.java).findAll()

        tags.sortBy { it.name }

        val tagAdapter = MyTagAdapter(tags)

        val rvTags: RecyclerView = findViewById(R.id.tagList)
        rvTags.adapter = tagAdapter
        rvTags.layoutManager = LinearLayoutManager(applicationContext)

        helper?.attachToRecyclerView(null) // Odpojí starý helper
        helper = ItemTouchHelper(SwipeToDeleteCallback(tagAdapter))
        helper?.attachToRecyclerView(rvTags) // Připojí nový
    }

    private fun receiveMessageFromDevice(intent: Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            with(parcelables) {
                val inNdefMessage = this?.get(0) as NdefMessage
                val inNdefRecords = inNdefMessage.records
                val ndefRecord_0 = inNdefRecords[0]

                val inMessage = String(ndefRecord_0.payload)
                incomingHash = inMessage
            }
        }
    }

    private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {

        // here we are setting up receiving activity for a foreground dispatch
        // thus if activity is already started it will take precedence over any other activity or app
        // with the same intent filters

        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)

        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf<Array<String>>()

        filters[0] = IntentFilter()
        with(filters[0]) {
            this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
            this?.addCategory(Intent.CATEGORY_DEFAULT)
            try {
                this?.addDataType(MIME_TEXT_PLAIN)
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Check your MIME type")
            }
        }

        adapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
    }

    private fun disableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        adapter?.disableForegroundDispatch(activity)
    }

    private fun send(tagName: String){
        val topic: String    = "users/OndrejPotocek/"+tagName
        val content: String  = "2"
        val qos: Int         = 2;
        val broker: String   = "tcp://webelias.site:1883"
        val clientId: String = username
        val persistence = MemoryPersistence()


        try {
            val sampleClient = MqttClient(broker, clientId, persistence)
            val connOpts = MqttConnectOptions()
            connOpts.userName = username
            connOpts.password = password.toCharArray()
            connOpts.setCleanSession(true)

            sampleClient.connect(connOpts)

            val message = MqttMessage(content.toByteArray())
            message.setQos(qos)
            sampleClient.publish(topic, message)

            sampleClient.disconnect()


        }
        catch(e: MqttException) {
            Toast.makeText(this, "Chyba při odesílání na server!", Toast.LENGTH_SHORT).show()
        }
    }


}
