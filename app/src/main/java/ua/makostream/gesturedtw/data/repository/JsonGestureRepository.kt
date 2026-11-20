package ua.makostream.gesturedtw.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import timber.log.Timber
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.model.GestureTemplate

/** Keeps all templates in one JSON [file]. A missing or corrupted file is treated as empty. */
class JsonGestureRepository(
    private val file: File,
    private val gson: Gson = Gson()
) : GestureRepository {

    private data class StoredSample(val x: Float, val y: Float, val z: Float, val t: Long)
    private data class StoredTemplate(val gestureId: String, val samples: List<StoredSample>)

    @Synchronized
    override fun loadAll(): List<GestureTemplate> {
        if (!file.exists()) {
            Timber.d("Templates file does not exist yet: %s", file.name)
            return emptyList()
        }
        return try {
            val type = object : TypeToken<List<StoredTemplate>>() {}.type
            val stored: List<StoredTemplate>? =
                file.reader(Charsets.UTF_8).use { gson.fromJson(it, type) }
            val templates = (stored ?: emptyList()).map { it.toDomain() }
            Timber.d("Loaded %d templates from %s", templates.size, file.name)
            templates
        } catch (e: Exception) {
            // Malformed JSON, missing fields or invalid data: do not crash the app.
            Timber.e(e, "Failed to read templates from %s", file.name)
            emptyList()
        }
    }

    @Synchronized
    override fun add(template: GestureTemplate) {
        saveAll(loadAll() + template)
        Timber.i("Template added: '%s' (%d samples)", template.gestureId, template.samples.size)
    }

    @Synchronized
    override fun removeAll(gestureId: String) {
        val remaining = loadAll().filterNot { it.gestureId == gestureId }
        saveAll(remaining)
        Timber.i("Templates removed for gesture '%s'", gestureId)
    }

    private fun saveAll(templates: List<GestureTemplate>) {
        file.parentFile?.mkdirs()
        file.writer(Charsets.UTF_8).use { writer ->
            gson.toJson(templates.map { it.toStored() }, writer)
        }
    }

    private fun GestureTemplate.toStored() = StoredTemplate(
        gestureId = gestureId,
        samples = samples.map { StoredSample(it.x, it.y, it.z, it.timestampNanos) }
    )

    private fun StoredTemplate.toDomain() = GestureTemplate(
        gestureId = gestureId,
        samples = samples.map { AccelerometerSample(it.x, it.y, it.z, it.t) }
    )
}