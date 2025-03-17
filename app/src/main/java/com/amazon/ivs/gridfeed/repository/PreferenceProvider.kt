package com.amazon.ivs.gridfeed.repository

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private const val PREFERENCES_NAME = "GridFeedPreferences"

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

inline fun <reified T> T.toJson() = json.encodeToString(this)

inline fun <reified T> String.asObject(): T = json.decodeFromString(this)

@Singleton
class PreferenceProvider @Inject constructor(context: Context) {
    var settings by stringPreference()

    private val sharedPreferences by lazy { context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE) }

    private fun stringPreference() = object : ReadWriteProperty<Any?, String?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>) = sharedPreferences.getString(property.name, null)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            sharedPreferences.edit { putString(property.name, value) }
        }
    }
}
