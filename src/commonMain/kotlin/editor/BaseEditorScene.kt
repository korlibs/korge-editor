package editor

import com.soywiz.korge.scene.*

open class BaseEditorScene : Scene() {
	val file by lazy { injector.getSync<EditorFile>().file }
}
