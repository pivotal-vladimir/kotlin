package org.jetbrains.kotlin.gradle.plugin.android

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.factories.SerializerFactory
import com.esotericsoftware.kryo.serializers.FieldSerializer

class NameFilteredFieldSerializer<T>(
        kryo: Kryo?,
        type: Class<*>?
) : FieldSerializer<T>(kryo, type) {
    object Factory : SerializerFactory {
        override fun makeSerializer(kryo: Kryo, type: Class<*>?) = NameFilteredFieldSerializer<Any>(kryo, type)
    }

    private companion object {
        private val BLACKLISTED_NAMES = listOf("__dyn_obj__", "__meta_class__")
    }

    override fun initializeCachedFields() {
        super.initializeCachedFields()

        for (cachedField in fields) {
            if (cachedField.field.name in BLACKLISTED_NAMES) {
                removeField(cachedField.field.name)
            }
        }

        this.removedFields.clear()
    }
}