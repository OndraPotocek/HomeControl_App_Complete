package com.example.homecontrol

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.IOException
import java.lang.Exception
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

class AddNewTag : AppCompatActivity() {
    private var mNfcAdapter: NfcAdapter? = null



    private val realm = Realm.getDefaultInstance()
    private var etName: EditText? = null
    private var btnBack: Button? = null
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_tag)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)



        val isNfcSupported: Boolean = this.mNfcAdapter != null
        if (!isNfcSupported) {
            Toast.makeText(this, "NFC není podporováno", Toast.LENGTH_LONG).show()
            finish()
        }

        if (!mNfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC není zapnuté", Toast.LENGTH_LONG).show()
        }
        initViews()
        this.btnBack?.setOnClickListener {
            finish()
        }
    }

    private  fun initViews(){
        this.etName = findViewById(R.id.tag_name)
        this.btnBack = findViewById(R.id.btn_back)
    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.let {
            enableNFCInForeground(it, this,javaClass)
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.let {
            disableNFCInForeground(it,this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val hashName = md5(this.etName?.text.toString()+username)
        val messageWrittenSuccessfully = createNFCMessage(hashName, intent)

        if (messageWrittenSuccessfully){
            try {

                realm.beginTransaction()
                val tag: MyTag
                val newId = UUID.randomUUID().toString()
                tag = realm.createObject(MyTag::class.java, newId)
                tag.name = etName?.text.toString()
                tag.hashName = hashName
                realm.commitTransaction()
                finish()
            }catch(e: Exception){
                Toast.makeText(this, "Nastala chyba při ukládání do databáze!", Toast.LENGTH_LONG).show()}
            Toast.makeText(this,"Tag úspěšně vytvořen!", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "Něco se pokazilo, zkuste znovu!", Toast.LENGTH_LONG).show()

        }
        finish()

    }






    fun createNFCMessage(payload: String, intent: Intent?) : Boolean {
        val nfcRecord = NdefRecord.createMime(MIME_TEXT_PLAIN,payload.toByteArray())
        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
        intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            return  writeMessageToTag(nfcMessage, tag)
        }
        return false
    }

    private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

        try {
            val nDefTag = Ndef.get(tag)

            nDefTag?.let {
                it.connect()
                if (it.maxSize < nfcMessage.toByteArray().size) {
                    //Message to large to write to NFC tag
                    return false
                }
                if (it.isWritable) {
                    it.writeNdefMessage(nfcMessage)
                    it.close()
                    //Message is written to tag
                    return true
                } else {
                    //NFC tag is read-only
                    return false
                }
            }

            val nDefFormatableTag = NdefFormatable.get(tag)

            nDefFormatableTag?.let {
                try {
                    it.connect()
                    it.format(nfcMessage)
                    it.close()
                    //The data is written to the tag
                    return true
                } catch (e: IOException) {
                    //Failed to format tag
                    return false
                }
            }
            //NDEF is not supported
            return false

        } catch (e: Exception) {
            //Write operation has failed
        }
        return false
    }

    fun <T>enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType : Class<T>) {
        val pendingIntent = PendingIntent.getActivity(activity, 0,
            Intent(activity,classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val filters = arrayOf(nfcIntentFilter)

        val TechLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, TechLists)
    }

    fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }













    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

}