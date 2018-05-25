package com.github.kory33.util.sponge.ezdata.kotlin.translator

import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.asNullable
import com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal.toSnakeCase
import com.google.common.reflect.TypeToken
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.persistence.DataTranslator
import org.spongepowered.api.data.persistence.InvalidDataException

/**
 * Yields [DataTranslator] which can translate an instance of enum type [E].
 */
inline fun <reified E: Enum<E>> createEnumTranslator(modId: String): DataTranslator<E> = object: DataTranslator<E> {

    override fun getName() = "${E::class.java.name} Translator"

    override fun getId() = "$modId:${E::class.java.name.toSnakeCase()}_translator"

    override fun getToken() = TypeToken.of(E::class.java)

    override fun translate(view: DataView): E = view
            .getString(STRING_REPRESENTATION_QUERY).asNullable()?.let {
                try { enumValueOf<E>(it) } catch (_: Exception) { null }
            } ?: throw InvalidDataException("String representation of enum value is missing")

    override fun translate(obj: E): DataContainer = DataContainer.createNew()
            .set(STRING_REPRESENTATION_QUERY, obj.name)

    private val STRING_REPRESENTATION_QUERY = DataQuery.of("StringRepresentation")

}
