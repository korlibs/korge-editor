import com.soywiz.korge.*
import com.soywiz.korge.annotations.*
import com.soywiz.korge.scene.*
import com.soywiz.korim.color.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import editor.*

@OptIn(KorgeExperimental::class)
suspend fun main() = Korge(width = 1280, height = 720, bgcolor = Colors["#2b2b2b"], clipBorders = false, scaleMode = ScaleMode.NO_SCALE, scaleAnchor = Anchor.TOP_LEFT) {

	injector.mapPrototype { ParticleEditorScene() }

	val sceneContainer = sceneContainer()

	sceneContainer.changeTo<ParticleEditorScene>(EditorFile(MemoryVfsMix("particle.pex" to "")))
}
