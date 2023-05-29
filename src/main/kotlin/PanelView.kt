import java.awt.*
import java.awt.event.*
import javax.swing.*

interface PanelViewObserver {
    fun elementAdded(
        text: String?,
        panelView: PanelView,
        indexToReplace: Int,
        parent: CompositeJSON,
        name: String? = null,
        newIsArray: Boolean? = null
    )

    fun elementUpdated(
        newValue: Any,
        json: JsonElement,
        indexToReplace: Int,
        panelView: PanelView,
        key: String? = null
    )

    fun updateNullValue(panelView: PanelView, indexToReplace: Int, key: String? = null)

    fun elementRemoved(indexToRemove: Int, panelView: PanelView, parent: CompositeJSON, key: String? = null) {}
}

interface UpdatedAction {
    fun invoke(text: String) {}

}

class PanelView(private val compJson: CompositeJSON) : JPanel() {
    companion object {
        private const val IS_OBJECT = "New Object"
        private const val IS_ARRAY = "New Array"
        private const val IS_PROPERTY = "New property"
    }


    private val observers: MutableList<PanelViewObserver> = mutableListOf()

    fun addObserver(observer: PanelViewObserver) {
        observers.add(observer)
        components.filterIsInstance<ObjectProperty>().forEach {
            it.components.filterIsInstance<PanelView>().forEach { panel ->
                panel.addObserver(observer)
            }
        }
    }

    fun removeObserver(observer: PanelViewObserver) {
        observers.remove(observer)
    }

    init {
        layout = GridLayout(0, 1)
        if (compJson is ArrayJSON) {
            compJsonIsArray(compJson)
            addMouseListenerToComponent(true)
        } else if (compJson is ObjectJSON) {
            compJson.getProperties().forEach {
                add(ObjectProperty(it.key, it.value))
            }
            addMouseListenerToComponent(false)
        }
    }

    fun replaceComponent(indexToReplace: Int, json: JsonElement, key: String? = null) {
        if (indexToReplace < components.size) remove(indexToReplace)
        if (key != null) {
            add(ObjectProperty(key, json), indexToReplace)
        } else if (json is CompositeJSON) {
            val panel = PanelView(json).apply {
                border = BorderFactory.createRaisedBevelBorder()
            }
            observers.forEach { panel.addObserver(it) }
            add(panel, indexToReplace)

        } else add(getComponentArray(json), indexToReplace)
    }

    private fun compJsonIsArray(json: ArrayJSON) {
        json.getElements.forEach { it ->
            if (it is CompositeJSON) {
                val panel = PanelView(it)
                observers.forEach { panel.addObserver(it) }
                add(PanelView(it))
            } else {
                add(getComponentArray(value = it))
            }
        }
    }

    private fun checkIfIsNullORBoolean(value: Any) = (value is String && value.isEmpty()) || (value is Boolean)

    private fun informObserversUpdated(newValue: Any, indexToReplace: Int, name: String? = null) {
        if (compJson is ArrayJSON)
            observers.forEach {
                it.elementUpdated(newValue, compJson.getChildren(indexToReplace), indexToReplace, this)
            }
        else if (name != null)
            observers.forEach {
                it.elementUpdated(newValue, compJson, indexToReplace, this, name)
            }
    }

    private fun informObserversAdded(
        text: String? = null,
        indexToReplace: Int,
        parent: CompositeJSON,
        key: String? = null,
        newIsArray: Boolean? = null
    ) {
        println("informObserversAdded: $text")
        observers.forEach { it.elementAdded(text, this, indexToReplace, parent, key, newIsArray) }
    }

    private fun informObserversRemoved(indexToRemove: Int, key: String? = null, componentIsPanelView: PanelView? = null) {
        observers.forEach {
            if (componentIsPanelView != null && componentIsPanelView.parent is PanelView && compJson.parent != null) {
                it.elementRemoved(indexToRemove, componentIsPanelView.parent as PanelView, compJson.parent!!, key)
            } else it.elementRemoved(indexToRemove, this, compJson, key)
        }
    }

    private fun informObserversNullValue(indexToReplace: Int, key: String? = null) {
//        println("informObserversNullValue: $indexToReplace")
        observers.forEach { it.updateNullValue(this, indexToReplace, key) }
    }

    fun removeProperty(indexToRemove: Int) : Component? {
        if (compJson.parent == null && components.isEmpty()) return null
        /*if (components.size == 1) {
            (parent as? ObjectProperty)?.removeObjectProperty()
            (parent as? PanelView)?.informObserversRemoved(parent.components.indexOf(this))
        } else {*/
        val componentRemoved = getComponent(indexToRemove)
        remove(indexToRemove)
        revalidate()
        repaint()
        return componentRemoved
//        }
    }

    private fun addNewProperty(new: String) {
        add(NewProperty(new))
        revalidate()
        repaint()
    }

    private fun addNewPropertyArray(new: String) {
//        println("addNewPropertyArray: $new")
        when (new) {
            IS_PROPERTY -> {
                val field = JTextField()
                field.addKeyListener(Keyboard(field, object : UpdatedAction {
                    override fun invoke(text: String) {
                        informObserversAdded(text, components.indexOf(field), compJson)
                    }
                }))
                add(field)
                revalidate()
                repaint()
            }

            IS_ARRAY -> {
                informObserversAdded(null, components.size, compJson, newIsArray = true)
            }

            else -> {
                informObserversAdded(null, components.size, compJson, newIsArray = false)
            }
        }
    }

    private fun getComponent(key: String, value: JsonElement): JComponent {
        val result = when (value) {
            is JSONBoolean -> setUpCheckBox(value.getValue, key)
            is JSONNull -> setUpCheckNull()
            else -> {
                setUpTextField(value.toString().replace("\"",""), key = key)
            }
        }
        result.addMouseListenerToComponent(false)
        return result
    }

    private fun getComponentArray(value: JsonElement): JComponent {
        val result =
            when (value) {
                is JSONBoolean -> setUpCheckBox(value.getValue)
                is JSONNull -> setUpCheckNull()
                else -> setUpTextField(value.toString().replace("\"",""))
            }
        result.addMouseListenerToComponent(true)
        return result
    }

    private fun JComponent.addMouseListenerToComponent(toArray: Boolean) {
        if (toArray) {
            addMouseListener(
                MouseClick(addAction = { addNewPropertyArray(IS_PROPERTY) },
                    addObjectAction = { addNewPropertyArray(IS_OBJECT) },
                    addArrayAction = { addNewPropertyArray(IS_ARRAY) },
                    deleteAction = { index: Int, key: String?, panel: PanelView? ->
                        informObserversRemoved(
                            index, key, panel
                        )
                    },
                    addNullAction = { indexToReplace: Int, key: String? ->
                        informObserversNullValue(
                            indexToReplace, key
                        )
                    })
            )
        } else {
            addMouseListener(
                MouseClick(addAction = { addNewProperty(IS_PROPERTY) },
                    addObjectAction = { addNewProperty(IS_OBJECT) },
                    addArrayAction = { addNewProperty(IS_ARRAY) },
                    deleteAction = { index: Int, key: String?, panel: PanelView? ->
                        informObserversRemoved(
                            index, key, panel
                        )
                    },
                    addNullAction = { indexToReplace: Int, key: String? ->
                        informObserversNullValue(
                            indexToReplace, key
                        )
                    })
            )
        }
    }

    private fun setUpCheckBox(value: Boolean, key: String? = null): JCheckBox {
        return JCheckBox().apply {
            isSelected = value
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e?.button == MouseEvent.BUTTON1)
                        when (compJson) {
                            is ArrayJSON -> informObserversUpdated(isSelected, name = key, indexToReplace = this@PanelView.components.indexOf(this@apply))
                            else -> informObserversUpdated(isSelected, name = key, indexToReplace = this@PanelView.components.indexOf(this@apply.parent))
                        }
                }
            })
        }
    }

    private fun setUpCheckNull() = JLabel("N/A")

    private fun setUpTextField(value: String, key: String? = null): JTextField {
        val textField = JTextField(value).apply {
            if (text.equals("N/A")) text = ""

            addKeyListener(Keyboard(this, object : UpdatedAction {
                override fun invoke(text: String) {
                    val textInRightType = checkType(text)
                    if (compJson is ArrayJSON) {
                        informObserversUpdated(textInRightType, indexToReplace = this@PanelView.components.indexOf(this@apply))
                    } else if (compJson is ObjectJSON && key != null) {
                        informObserversUpdated(textInRightType, name = key, indexToReplace = this@PanelView.components.indexOf(this@apply.parent))
                    }
                }
            }))
        }
        return textField
    }

    inner class Keyboard(private val jTextField: JTextField, private val action: UpdatedAction) : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (e?.keyCode == KeyEvent.VK_ENTER) {
                action.invoke(jTextField.text)
            }
        }
    }

    inner class MouseClick(
        val addAction: () -> Unit,
        val deleteAction: (Int, String?, PanelView?) -> Unit,
        val addObjectAction: () -> Unit,
        val addArrayAction: () -> Unit,
        val addNullAction: (Int, String?) -> Unit
    ) : MouseAdapter() {

        override fun mouseClicked(e: MouseEvent?) {
            if (e?.button == MouseEvent.BUTTON1 && e.component is JLabel && !(e.component.parent is ObjectProperty &&
                        e.component.parent.components.indexOf(e.component) == 0)) actionNALabel(e)
            else if (e?.button == MouseEvent.BUTTON3) actionRightClick(e)
        }

        private fun actionNALabel(e: MouseEvent) {
            if (e.component.parent is ObjectProperty) {
                val parent = (e.component.parent as ObjectProperty)
                val key = (parent.components[0] as JLabel).text
                parent.getParentPanel().components.forEach {
                    if (it is ObjectProperty && it.components[0] is JLabel && (it.components[0] as JLabel).text == key) {
                        addNullAction(parent.getParentPanel().components.indexOf(it), key)
                    }
                }
            } else if (e.component.parent is PanelView && compJson is ArrayJSON) {
                addNullAction(e.component.parent.components.indexOf(e.component), null)
            }
        }

        private fun actionRightClick(e: MouseEvent) {

            val frame = JFrame("Add new element").apply {
                defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
                layout = GridBagLayout()
                size = Dimension(400, 100)
                setLocationRelativeTo(null)

                val addButton = JButton("Add")
                addButton.addActionListener {
                    dispose()
                    addAction()
                }

                val addObjectButton = JButton("Add Object")
                addObjectButton.addActionListener {
                    dispose()
                    addObjectAction()
                }

                val deleteButton = JButton("Delete")
                deleteButton.addActionListener {
                    dispose()
                    deleteButton(e)
                }

                val addArrayButton = JButton("Add Array")
                addArrayButton.addActionListener {
                    dispose()
                    addArrayAction()
                }
                add(addButton)
                add(addObjectButton)
                add(addArrayButton)
                add(deleteButton)
            }
            frame.isVisible = true
        }

        private fun deleteButton(e: MouseEvent) {
            if (e.component.parent is ObjectProperty) {
                e.component.parent.components.forEach {
                    if (it is JLabel) deleteAction(this@PanelView.components.indexOf(e.component.parent), it.text, null)
                }
            } else {
                if (e.component is PanelView) {
                    deleteAction(e.component.parent.components.indexOf(e.component), null, e.component as PanelView)
                } else deleteAction(this@PanelView.components.indexOf(e.component), null, null)
            }
        }
    }

    inner class ObjectProperty(private val key: String, value: JsonElement) : JComponent() {
        init {
            layout = GridLayout(0, 2)
            border = BorderFactory.createRaisedBevelBorder()
            val label = JLabel(key).apply {
                font = Font("SansSerif", Font.BOLD, 20)
                addMouseListenerToComponent(false)
            }
            add(label)
            if (value is CompositeJSON) {
                val panel = PanelView(value)
                observers.forEach { panel.addObserver(it) }
                add(panel)
            } else {
                val area = getComponent(value = value, key = key)
                add(area)
            }
        }

        fun removeObjectProperty() {
            informObserversRemoved(this@PanelView.components.indexOf(this), key)
        }

        fun getParentPanel(): PanelView {
            return this@PanelView
        }
    }

    inner class NewProperty(private val new: String) : JComponent() {
        init {
            layout = GridLayout(0, 1)
            border = BorderFactory.createRaisedBevelBorder()
            val textField = JTextField()
            textField.addKeyListener(Keyboard(textField, object : UpdatedAction {
                override fun invoke(text: String) {
                    if (text.isNotEmpty() && !text.contains(" ")) removeInitialTextFieldAndAddLabelAndTextField(textField)
                }
            }))
            add(textField)
        }

        private fun removeInitialTextFieldAndAddLabelAndTextField(labelText: JTextField) {
            val index = this@PanelView.components.indexOf(this@NewProperty)
            when (new) {
                IS_OBJECT -> {
                    removeAll()
                    informObserversAdded(null, index, compJson, labelText.text, false)
                }

                IS_ARRAY -> {
                    removeAll()
                    informObserversAdded(null, index, compJson, labelText.text, true)
                }

                else -> {
                    removeAll()
                    layout = GridLayout(0, 2)
                    val textField = JTextField()
                    val label = JLabel(labelText.text)
                    add(label)
                    add(textField)
                    revalidate()
                    repaint()
                    textField.addKeyListener(Keyboard(textField, object : UpdatedAction {
                        override fun invoke(text: String) {
                            informObserversAdded(text, index, compJson, labelText.text)
                        }
                    }))
                }
            }
        }
    }
}


fun checkType(text: String): Any {
    return when {
        text.toIntOrNull() != null -> text.toInt()
        text.toDoubleOrNull() != null -> text.toDouble()
        text.toFloatOrNull() != null -> text.toFloat()
        text.toLongOrNull() != null -> text.toLong()
        text.equals("true", ignoreCase = true) -> true
        text.equals("false", ignoreCase = true) -> false
        text.equals("null", ignoreCase = true) -> ""
        text.isEmpty() -> ""
        else -> text
    }
}
