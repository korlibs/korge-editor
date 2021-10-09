package editor

import com.soywiz.korag.*
import com.soywiz.korge.annotations.*
import com.soywiz.korge.particle.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import util.*
import util.UIEditableNumberPropsList
import util.uiWindow

@OptIn(KorgeExperimental::class)
class ParticleEditorScene() : BaseEditorScene() {
	var emitter: ParticleEmitter = ParticleEmitter()

	suspend fun load() {
		emitter = try {
			file.readParticleEmitter()
		} catch (e: Throwable) {
			//e.printStackTrace()
			ParticleEmitter()
		}
	}

	suspend fun save() {
		file.writeParticleEmitter(emitter)
	}

	override suspend fun Container.sceneMain() {
		//val bitmap = resourcesVfs["korge.png"].readBitmap()
		//val emitterView = emitterView(100, 100, Colors.RED)
		load()
		emitter.blendFuncSource = AG.BlendFactor.SOURCE_ALPHA
		emitter.blendFuncDestination = AG.BlendFactor.DESTINATION_ALPHA
		val emitterView = particleEmitter(emitter)
		emitter.texture = Bitmaps.white
		emitterView.position(100, 100)
		//UIWindow
		uiWindow("Properties", 300.0, 500.0) {
			emitterView.dockedTo(Anchor(0.5, 0.7)) {
				it.x = (views.actualVirtualWidth - 300.0) * 0.5
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
			uiVerticalStack(300.0) {
				uiText("Particle") { textColor = Colors.RED }
				uiPropertyNumberRow("MaxParticles", *UIEditableIntPropsList(emitter::maxParticles))
				uiPropertyComboBox("EmitterType", emitter::emitterType)
				uiPropertyComboBox("BlendFuncSource", emitter::blendFuncSource)
				uiPropertyComboBox("BlendFuncDestination", emitter::blendFuncDestination)
				uiPropertyNumberRow("Angle", *UIEditableAnglePropsList(emitter::angle, emitter::angleVariance))
				uiPropertyNumberRow("Speed", *UIEditableNumberPropsList(emitter::speed, emitter::speedVariance, min = 0.0, max = +1000.0))
				uiPropertyNumberRow("Lifespan", *UIEditableNumberPropsList(emitter::lifeSpan, emitter::lifespanVariance, min = -10.0, max = +10.0))
				uiPropertyNumberRow("Duration", *UIEditableNumberPropsList(emitter::duration, min = -10.0, max = +10.0))
				uiText("Acceleration") { textColor = Colors.RED }
				uiPropertyNumberRow("RadialAcceleration", *UIEditableNumberPropsList(emitter::radialAcceleration, emitter::radialAccelVariance, min = -1000.0, max = +1000.0))
				uiPropertyNumberRow("TangentialAcceleration", *UIEditableNumberPropsList(emitter::tangentialAcceleration, emitter::tangentialAccelVariance, min = -1000.0, max = +1000.0))
				uiText("Color") { textColor = Colors.RED }
				uiPropertyNumberRow("Start Color", *UIEditableColorPropsList(emitter::startColor))
				uiPropertyNumberRow("Start Color Variance", *UIEditableColorPropsList(emitter::startColorVariance))
				uiPropertyNumberRow("End Color", *UIEditableColorPropsList(emitter::endColor))
				uiPropertyNumberRow("End Color Variance", *UIEditableColorPropsList(emitter::endColorVariance))
				uiText("Radial") { textColor = Colors.RED }
				uiPropertyNumberRow("Gravity", *UIEditablePointPropsList(emitter::gravity, min = -1000.0, max = +1000.0))
				uiPropertyNumberRow("Min Radius", *UIEditableNumberPropsList(emitter::minRadius, emitter::minRadiusVariance, min = -1000.0, max = +1000.0))
				uiPropertyNumberRow("Max Radius", *UIEditableNumberPropsList(emitter::maxRadius, emitter::maxRadiusVariance, min = -1000.0, max = +1000.0))
				uiText("Rotate") { textColor = Colors.RED }
				uiPropertyNumberRow("RotatePerSecond", *UIEditableAnglePropsList(emitter::rotatePerSecond, emitter::rotatePerSecondVariance))
				uiPropertyNumberRow("RotationStart", *UIEditableAnglePropsList(emitter::rotationStart, emitter::rotationStartVariance))
				uiPropertyNumberRow("RotationEnd", *UIEditableAnglePropsList(emitter::rotationEnd, emitter::rotationEndVariance))
				uiText("Size") { textColor = Colors.RED }
				uiPropertyNumberRow("StartSize", *UIEditableNumberPropsList(emitter::startSize, emitter::startSizeVariance, min = -1000.0, max = +1000.0))
				uiPropertyNumberRow("EndSize", *UIEditableNumberPropsList(emitter::endSize, emitter::endSizeVariance, min = -1000.0, max = +1000.0))

				/*
				uiEditableValue(this@ParticleEmitterView::localCoords)
				uiEditableValue(Pair(this@ParticleEmitterView::emitterX, this@ParticleEmitterView::emitterY), min = -1000.0, max = +1000.0, clamp = false, name = "emitterPos")
				uiEditableValue("Source Position", particle.sourcePosition)
				uiEditableValue("Source Position Variance", particle.sourcePositionVariance)
				 */


				uiText("Properties") { textColor = Colors.RED }
				uiPropertyNumberRow("Alpha", *UIEditableNumberPropsList(emitterView::alpha))
				uiPropertyNumberRow("Position", *UIEditableNumberPropsList(emitterView::x, emitterView::y, min = -1024.0, max = +1024.0, clamped = false))
				uiPropertyNumberRow("Size", *UIEditableNumberPropsList(emitterView::width, emitterView::height, min = -1024.0, max = +1024.0, clamped = false))
				uiPropertyNumberRow("Scale", *UIEditableNumberPropsList(emitterView::scaleX, emitterView::scaleY, min = -1.0, max = +1.0, clamped = false))
				uiPropertyNumberRow("Rotation", *UIEditableNumberPropsList(emitterView::rotationDeg, min = -360.0, max = +360.0, clamped = true))
				val skewProp = uiPropertyNumberRow("Skew", *UIEditableNumberPropsList(emitterView::skewXDeg, emitterView::skewYDeg, min = -360.0, max = +360.0, clamped = true))
				uiPropertyCheckBox("Visible", emitterView::visible)
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
