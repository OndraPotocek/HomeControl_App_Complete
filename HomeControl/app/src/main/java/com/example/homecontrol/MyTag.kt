package com.example.homecontrol

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class MyTag : RealmObject() {
    @PrimaryKey
    @Required
    var id: String? = null
    var name: String? = null
    var hashName: String? = null
}