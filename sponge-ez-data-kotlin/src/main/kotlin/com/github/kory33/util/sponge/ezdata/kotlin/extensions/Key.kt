package com.github.kory33.util.sponge.ezdata.kotlin.extensions

import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.toSnakeCase
import com.google.common.reflect.TypeToken
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.value.BaseValue

/**
 * Construct key for Sponge Data API
 *
 * @param token Type token of the key
 * @param keyName Upper-Camel-Cased name of the key
 */
fun <T, B: BaseValue<T>> constructKey(token: TypeToken<B>, keyName: String): Key<B> =
        Key.builder()
                .type(token)
                .id(keyName.toSnakeCase())
                .name(keyName)
                .query(DataQuery.of(keyName))
                .build()
