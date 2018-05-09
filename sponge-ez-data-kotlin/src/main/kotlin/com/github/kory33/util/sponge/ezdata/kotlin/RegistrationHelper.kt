package com.github.kory33.util.sponge.ezdata.kotlin

import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.toKebabCase
import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.toManipulatorId
import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.toSnakeCase
import com.google.common.reflect.TypeToken
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.DataRegistration
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.manipulator.DataManipulator
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator
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

/**
 * Partially build data registration from given information
 *
 * @param clazz class of the mutable manipulator
 * @param immutableClazz class of the immutable manipulator
 * @param builder builder for manipulators
 */
fun <T: DataManipulator<T, I>, I: ImmutableDataManipulator<I, T>>
        buildPartialRegistration(clazz: Class<T>,
                                 immutableClazz: Class<I>,
                                 builder: DataManipulatorBuilder<T, I>): DataRegistration.Builder<T, I> =
        DataRegistration.builder()
                .dataClass(clazz)
                .immutableClass(immutableClazz)
                .builder(builder)
                .manipulatorId(clazz.simpleName.toKebabCase().toManipulatorId())
                .dataName(clazz.simpleName)
