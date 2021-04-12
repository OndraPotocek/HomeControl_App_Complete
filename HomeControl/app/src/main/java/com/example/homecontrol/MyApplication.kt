package com.example.homecontrol
import android.app.Application
import android.content.Intent
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import java.lang.Exception


class MyApplication: Application() {

    override fun onCreate(){
        super.onCreate()
        Realm.init(this)

        /*if (!userIsRegistered()){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)*/

    }
    /*private fun userIsRegistered(): Boolean{
        try {
            val username = activeUsername
            if (username != ""){return true}
        }
        catch (e: Exception){
            Toast.makeText(this, "Nastala chyba při ověření uživatele!", Toast.LENGTH_SHORT).show()}

        return false
    }*/
}