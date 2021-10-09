import com.soywiz.korge.annotations.*
import com.soywiz.korge.input.*
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korma.geom.*
import kotlin.reflect.*

@KorgeExperimental
inline fun <T> Container.uiPropertyComboBox(
	title: String, field: KMutableProperty0<T>, values: List<T>,
	width: Double = 128.0, height: Double = 20.0,
): UIPropertyRow = UIPropertyRow(title).also {
		it.container.apply {
			uiComboBox(items = values)
		}
	}.addTo(this)

@KorgeExperimental
inline fun <reified T : Enum<T>> Container.uiPropertyComboBox(
	title: String, field: KMutableProperty0<T>,
	width: Double = 128.0, height: Double = 20.0,
): UIPropertyRow = uiPropertyComboBox(title, field, enumValues<T>().toList(), width, height)

fun UIEditableNumberPropsList(vararg mut: KMutableProperty0<Double>, min: Double = 0.0, max: Double = 1.0, decimals: Int = 2, clamped: Boolean = true): Array<UIEditableNumberProps> {
	return mut.map { mut ->
		UIEditableNumberProps(mut.get(), min, max, decimals, clamped) { mut.set(it) }
	}.toTypedArray()
}

fun UIEditableIntPropsList(vararg mut: KMutableProperty0<Int>, min: Int = 0, max: Int = 1000): Array<UIEditableNumberProps> {
	return mut.map { mut ->
		UIEditableNumberProps(mut.get().toDouble(), min.toDouble(), max.toDouble(), 0, true) { mut.set(it.toInt()) }
	}.toTypedArray()
}

fun UIEditableAnglePropsList(vararg mut: KMutableProperty0<Angle>, min: Angle = -360.degrees, max: Angle = +360.degrees, clamped: Boolean = true): Array<UIEditableNumberProps> {
	return mut.map { mut ->
		UIEditableNumberProps(mut.get().degrees, min.degrees, max.degrees, 0, clamped) { mut.set(it.degrees) }
	}.toTypedArray()
}

fun UIEditableColorPropsList(prop: KProperty0<RGBAf>): Array<UIEditableNumberProps> {
	return UIEditableNumberPropsList(prop.get()::rd, prop.get()::gd, prop.get()::bd, prop.get()::ad, min = 0.0, max = 1.0)
}

fun UIEditablePointPropsList(prop: KProperty0<Point>, min: Double = -1000.0, max: Double = +1000.0): Array<UIEditableNumberProps> {
	return UIEditableNumberPropsList(prop.get()::x, prop.get()::y, min = min, max = max)
}

inline fun <T> Container.uiComboBox(
	width: Double = UI_DEFAULT_WIDTH,
	height: Double = UI_DEFAULT_HEIGHT,
	selectedIndex: Int = 0,
	items: List<T>,
	block: @ViewDslMarker UIComboBox<T>.() -> Unit = {}
) = UIComboBox(width, height, selectedIndex, items).addTo(this).apply(block)

open class UIComboBox<T>(
	width: Double = UI_DEFAULT_WIDTH,
	height: Double = UI_DEFAULT_HEIGHT,
	selectedIndex: Int = 0,
	items: List<T> = listOf(),
) : UIView(width, height) {

	var selectedIndex by uiObservable(selectedIndex) { updateState() }
	var selectedItem: T?
		get() = items.getOrNull(selectedIndex)
		set(value) {
			selectedIndex = items.indexOf(value)
		}
	var items: List<T> by uiObservable(items) { updateItems() }
	var itemHeight by uiObservable(32) { updateItemsSize() }
	var viewportHeight by uiObservable(196) { onSizeChanged() }

	private val itemsView = uiScrollable(width, height = 128.0)
	private val verticalList = itemsView.container.uiVerticalList(object : UIVerticalList.Provider {
		override val numItems: Int = items.size
		override val fixedHeight: Double = itemHeight.toDouble()
		override fun getItemHeight(index: Int): Double = fixedHeight
		override fun getItemView(index: Int): View = UIButton(text = items[index].toString()).also {
			it.onClick {
				this@UIComboBox.showItems = false
				this@UIComboBox.selectedIndex = index
			}
		}
	}, width = width)
	private val selectedButton = uiButton(width - height, height, "")
	private val expandButton = uiButton(height, height, icon = comboBoxExpandIcon).position(width - height, 0.0)
	private val invisibleRect = solidRect(width, height, Colors.TRANSPARENT_BLACK)
	private var showItems = false

	val onSelectionUpdate = Signal<UIComboBox<T>>()

	init {
		updateItems()
		invisibleRect.onOver {
			selectedButton.simulateOver()
			expandButton.simulateOver()
		}
		invisibleRect.onOut {
			selectedButton.simulateOut()
			expandButton.simulateOut()
		}
		invisibleRect.onDown {
			selectedButton.simulateDown()
			expandButton.simulateDown()
		}
		invisibleRect.onUp {
			selectedButton.simulateUp()
			expandButton.simulateUp()
		}
		invisibleRect.onClick {
			showItems = !showItems
			onSizeChanged()
		}
		onSizeChanged()
	}

	fun open() {
		addChild(itemsView)

		// Prevent overlap by other controls.
		parent?.sendChildToFront(this)
	}

	fun close() {
		removeChild(itemsView)
	}

	private fun updateItemsSize() {
		itemsView.container.forEachChildWithIndex { index, child ->
			child.scaledHeight = itemHeight.toDouble()
			child.position(0, index * itemHeight)
		}
	}

	private fun updateItems() {
		verticalList.updateList()
		//itemsView.container.removeChildren()
		//for ((index, item) in items.withIndex()) {
		//    itemsView.container.uiButton(
		//        width - 32.0,
		//        itemHeight.toDouble(),
		//        item.toString()
		//    ) {
		//        position(0, index * this@UIComboBox.itemHeight)
		//        onClick {
		//            this@UIComboBox.showItems = false
		//            this@UIComboBox.selectedIndex = index
		//        }
		//    }
		//}
		//itemsView.contentHeight = (items.size * itemHeight).toDouble()
		updateState()
	}

	override fun updateState() {
		onSizeChanged()
		for (i in items.indices) {
			val button = itemsView.container.getChildAtOrNull(i) as? UIButton ?: continue
			button.forcePressed = selectedIndex == i
		}
		onSelectionUpdate(this)
	}

	override fun onSizeChanged() {
		super.onSizeChanged()
		if (showItems) {
			open()
		} else {
			close()
		}
		//itemsView.size(width, viewportHeight.toDouble()).position(0.0, height)
		itemsView.size(width, 128.0.toDouble()).position(0.0, height)
		verticalList.size(width, verticalList.height)
		selectedButton.simulatePressing(showItems)
		expandButton.simulatePressing(showItems)
		expandButton.icon = if (showItems) comboBoxShrinkIcon else comboBoxExpandIcon
		invisibleRect.size(width, height)
		selectedButton.size(width - height, height)
		selectedButton.text = selectedItem?.toString() ?: ""
		expandButton.position(width - height, 0.0).size(height, height)
	}
}
