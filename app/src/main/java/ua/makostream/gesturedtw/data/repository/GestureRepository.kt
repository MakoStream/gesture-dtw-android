package ua.makostream.gesturedtw.data.repository

import ua.makostream.gesturedtw.domain.model.GestureTemplate

/** Storage of recorded gesture templates. */
interface GestureRepository {

    fun loadAll(): List<GestureTemplate>

    /** Adds a template. Several templates may share one gestureId (more recordings = better recognition). */
    fun add(template: GestureTemplate)

    /** Removes every template of the given gesture. */
    fun removeAll(gestureId: String)
}