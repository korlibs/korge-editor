package editor

import com.soywiz.korge.scene.*
import com.soywiz.korio.file.*

open class BaseEditorScene : Scene() {
	private val originalFile by lazy { injector.getSync<EditorFile>().file }
	private var _file: VfsFile? = null
	var file: VfsFile
		get() = _file ?: originalFile
		set(value) { _file = value }
}
