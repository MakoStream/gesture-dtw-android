package ua.makostream.gesturedtw.data.repository

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ua.makostream.gesturedtw.domain.model.AccelerometerSample
import ua.makostream.gesturedtw.domain.model.GestureTemplate

class JsonGestureRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var file: File
    private lateinit var repo: JsonGestureRepository

    private fun template(id: String, vararg xs: Float) = GestureTemplate(
        id, xs.mapIndexed { i, x -> AccelerometerSample(x, x * 2, -x, i * 1000L) }
    )

    @Before
    fun setUp() {
        file = File(tmp.root, "data/templates.json")
        repo = JsonGestureRepository(file)
    }

    @Test
    fun missingFileGivesEmptyList() {
        assertTrue(repo.loadAll().isEmpty())
    }

    @Test
    fun addedTemplateIsLoadedBackUnchanged() {
        val t = template("circle", 0.5f, 1.5f, -2.25f)

        repo.add(t)

        assertEquals(listOf(t), repo.loadAll())
    }

    @Test
    fun templatesSurviveNewRepositoryInstance() {
        val t = template("shake", 1f, 2f, 3f)
        repo.add(t)

        val reopened = JsonGestureRepository(file)

        assertEquals(listOf(t), reopened.loadAll())
    }

    @Test
    fun severalTemplatesOfOneGestureAreKept() {
        repo.add(template("shake", 1f, 2f))
        repo.add(template("shake", 3f, 4f))

        assertEquals(2, repo.loadAll().size)
    }

    @Test
    fun removeAllDeletesOnlyThatGesture() {
        repo.add(template("shake", 1f, 2f))
        repo.add(template("circle", 3f, 4f))
        repo.add(template("shake", 5f, 6f))

        repo.removeAll("shake")

        assertEquals(listOf("circle"), repo.loadAll().map { it.gestureId })
    }

    @Test
    fun corruptedFileGivesEmptyList() {
        file.parentFile!!.mkdirs()
        file.writeText("this is not json")

        assertTrue(repo.loadAll().isEmpty())
    }

    @Test
    fun repositoryRecoversAfterCorruptedFile() {
        file.parentFile!!.mkdirs()
        file.writeText("this is not json")
        val t = template("circle", 1f, 2f)

        repo.add(t)

        assertEquals(listOf(t), repo.loadAll())
    }
}