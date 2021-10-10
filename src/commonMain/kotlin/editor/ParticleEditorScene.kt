package editor

import com.soywiz.klock.*
import com.soywiz.korag.*
import com.soywiz.korev.*
import com.soywiz.korge.annotations.*
import com.soywiz.korge.component.docking.*
import com.soywiz.korge.input.*
import com.soywiz.korge.particle.*
import com.soywiz.korge.time.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*
import kotlinx.coroutines.flow.*

@OptIn(KorgeExperimental::class)
class ParticleEditorScene() : BaseEditorScene() {
	var emitter: ParticleEmitter = ParticleEmitter()
	lateinit var emitterView: ParticleEmitterView

	suspend fun load() {
		val realFile = if (file.extensionLC == "zip") {
			val zip = file.openAsZip()
			zip.list().first { it.extensionLC == "pex" }
		} else {
			file
		}

		emitter = try {
			realFile.readParticleEmitter()
		} catch (e: Throwable) {
			//e.printStackTrace()
			ParticleEmitter()
		}
	}

	suspend fun save() {
		file.writeParticleEmitter(emitter)
	}

	private fun repositonEmitterView() {
		//println("repositonEmitterView")
		if (followCursor) {
			emitterView.position(stage.mouseXY)
		} else {
			emitterView.y = (views.actualVirtualHeight) * 0.7
			emitterView.x = (views.actualVirtualWidth - 300.0) * 0.5
		}
	}

	var followCursor: Boolean = false
		set(value) {
			field = value
			repositonEmitterView()
		}

	override suspend fun Container.sceneMain() {
		uiButton(text = "Load...") {
			onClick {
				launchImmediately {
					val file = views.openFileDialog(FileFilter("PEX Files (*.pex)" to listOf("*.pex", "*.zip")), write = false, multi = false)
					this@ParticleEditorScene.file = file.first()
					load()
				}
			}
		}
		//val bitmap = resourcesVfs["korge.png"].readBitmap()
		//val emitterView = emitterView(100, 100, Colors.RED)
		load()

		emitter.texture = resourcesVfs["texture.png"].readBitmapSlice()
		emitterView = particleEmitter(emitter)
		emitterView.position(100, 100)
		//UIWindow
		uiWindow("Properties", 300.0, 500.0) {
			emitterView.dockedTo(Anchor(0.5, 0.7)) {
				repositonEmitterView()
			}
			it.dragProcessor = { }
			it.x = 300.0
			it.dockedTo(Anchor.TOP_RIGHT) {
				it.x -= 300.0
				it.scaledHeight = views.actualVirtualHeight.toDouble()
			}
			it.isCloseable = false
			it.container.mobileBehaviour = false
			it.container.overflowRate = 0.0
			//keys { down(Key.RETURN) { emitterView.visible = !emitterView.visible } }
			mouse {
				onMoveAnywhere {
					if (followCursor) repositonEmitterView()
				}
			}
			uiVerticalStack(300.0) {
				val props = UIObservablePropertyList()
				fun <T : UIObservableProperty<*>> Array<T>.register(): Array<T> = this.register(props)

				uiText("Particle") { textColor = Colors.RED }
				uiPropertyNumberRow("MaxParticles", *UIEditableIntPropsList(emitter::maxParticles).register())
				uiPropertyComboBox("EmitterType", emitter::emitterType)
				uiPropertyComboBox("BlendFuncSource", emitter::blendFuncSource)
				uiPropertyComboBox("BlendFuncDestination", emitter::blendFuncDestination)
				uiPropertyNumberRow("Angle", *UIEditableAnglePropsList(emitter::angle, emitter::angleVariance).register())
				uiPropertyNumberRow("Speed", *UIEditableNumberPropsList(emitter::speed, emitter::speedVariance, min = 0.0, max = +1000.0).register())
				uiPropertyNumberRow("Lifespan", *UIEditableNumberPropsList(emitter::lifeSpan, emitter::lifespanVariance, min = -10.0, max = +10.0).register())
				uiPropertyNumberRow("Duration", *UIEditableNumberPropsList(emitter::duration, min = -10.0, max = +10.0).register())
				uiText("Acceleration") { textColor = Colors.RED }
				uiPropertyNumberRow("RadialAcceleration", *UIEditableNumberPropsList(emitter::radialAcceleration, emitter::radialAccelVariance, min = -1000.0, max = +1000.0).register())
				uiPropertyNumberRow("TangentialAcceleration", *UIEditableNumberPropsList(emitter::tangentialAcceleration, emitter::tangentialAccelVariance, min = -1000.0, max = +1000.0).register())
				uiText("Color") { textColor = Colors.RED }
				uiPropertyNumberRow("Start Color", *UIEditableColorPropsList(emitter::startColor).register())
				uiPropertyNumberRow("Start Color Variance", *UIEditableColorPropsList(emitter::startColorVariance).register())
				uiPropertyNumberRow("End Color", *UIEditableColorPropsList(emitter::endColor).register())
				uiPropertyNumberRow("End Color Variance", *UIEditableColorPropsList(emitter::endColorVariance).register())
				uiText("Radial") { textColor = Colors.RED }
				uiPropertyNumberRow("Gravity", *UIEditablePointPropsList(emitter::gravity, min = -1000.0, max = +1000.0).register())
				uiPropertyNumberRow("Min Radius", *UIEditableNumberPropsList(emitter::minRadius, emitter::minRadiusVariance, min = -1000.0, max = +1000.0).register())
				uiPropertyNumberRow("Max Radius", *UIEditableNumberPropsList(emitter::maxRadius, emitter::maxRadiusVariance, min = -1000.0, max = +1000.0).register())
				uiText("Rotate") { textColor = Colors.RED }
				uiPropertyNumberRow("RotatePerSecond", *UIEditableAnglePropsList(emitter::rotatePerSecond, emitter::rotatePerSecondVariance).register())
				uiPropertyNumberRow("RotationStart", *UIEditableAnglePropsList(emitter::rotationStart, emitter::rotationStartVariance).register())
				uiPropertyNumberRow("RotationEnd", *UIEditableAnglePropsList(emitter::rotationEnd, emitter::rotationEndVariance).register())
				uiText("Size") { textColor = Colors.RED }
				uiPropertyNumberRow("StartSize", *UIEditableNumberPropsList(emitter::startSize, emitter::startSizeVariance, min = -1000.0, max = +1000.0).register())
				uiPropertyNumberRow("EndSize", *UIEditableNumberPropsList(emitter::endSize, emitter::endSizeVariance, min = -1000.0, max = +1000.0).register())

				/*
				uiEditableValue(this@ParticleEmitterView::localCoords)
				uiEditableValue(Pair(this@ParticleEmitterView::emitterX, this@ParticleEmitterView::emitterY), min = -1000.0, max = +1000.0, clamp = false, name = "emitterPos")
				uiEditableValue("Source Position", particle.sourcePosition)
				uiEditableValue("Source Position Variance", particle.sourcePositionVariance)
				 */

				uiText("Properties") { textColor = Colors.RED }
				uiPropertyNumberRow("Alpha", *UIEditableNumberPropsList(emitterView::alpha))
				uiPropertyNumberRow("Position", *UIEditableNumberPropsList(emitterView::x, emitterView::y, min = -1024.0, max = +1024.0, clamped = false).register())
				uiPropertyNumberRow("Size", *UIEditableNumberPropsList(emitterView::width, emitterView::height, min = -1024.0, max = +1024.0, clamped = false))
				uiPropertyNumberRow("Scale", *UIEditableNumberPropsList(emitterView::scaleX, emitterView::scaleY, min = -1.0, max = +1.0, clamped = false).register())
				uiPropertyNumberRow("Rotation", *UIEditableNumberPropsList(emitterView::rotationDeg, min = -360.0, max = +360.0, clamped = true).register())
				val skewProp = uiPropertyNumberRow("Skew", *UIEditableNumberPropsList(emitterView::skewXDeg, emitterView::skewYDeg, min = -360.0, max = +360.0, clamped = true).register())
				uiPropertyCheckBox("Visible", *UIEditableBooleanPropsList(emitterView::visible).register())
				uiText("Extra") { textColor = Colors.RED }
				//uiPropertyCheckBox("Follow Cursor", *UIEditableBooleanPropsList(::followCursor).register())
				uiPropertyCheckBox("Follow Cursor", *UIEditableBooleanPropsList(::followCursor).register())

				timers.interval(250.milliseconds) {
					props.sync()
				}
			}
		}
	}
}

private var View.rotationDeg: Double
	get() = rotation.degrees
	set(value) { rotation = value.degrees }

private var View.skewXDeg: Double
	get() = skewX.degrees
	set(value) { skewX = value.degrees }

private var View.skewYDeg: Double
	get() = skewY.degrees
	set(value) { skewY = value.degrees }
