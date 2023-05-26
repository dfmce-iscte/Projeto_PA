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
        text: String,
        json: JsonElement,
        key: String? = null,
        panelView: PanelView? = null,
        indexToReplace: Int? = null
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
        if (indexToReplace < components.size)
            remove(indexToReplace)
        if (key != null) {
            add(ObjectProperty(key, json), indexToReplace)
        } else if (json is CompositeJSON) {
            val panel = PanelView(json).apply {
                border = BorderFactory.createRaisedBevelBorder()
            }
            observers.forEach { panel.addObserver(it) }
            add(panel, indexToReplace)

        } else
            add(getComponentArray(json.toString().replace("\"", ""), indexToReplace), indexToReplace)
    }

    private fun compJsonIsArray(json: ArrayJSON) {
        json.getElements.forEach { it ->
            if (it is CompositeJSON) {
                val panel = PanelView(it)
                observers.forEach { panel.addObserver(it) }
                add(PanelView(it))
            } else {
                val area = getComponentArray(value = it.toString().replace("\"", ""), json.getElements.indexOf(it))
                add(area)
            }
        }
//        if (json.getElements.isEmpty())
//            addNewPropertyArray(IS_PROPERTY)
    }

    private fun informObserversUpdated(
        text: String,
        jsonIndex: Int? = null,
        json: JsonElement? = null,
        name: String? = null,
        indexToReplace: Int? = null
    ) {
        if (jsonIndex != null && compJson is ArrayJSON) {
//            println("informObserversUpdated: Array")
//            println("Children: ${compJson.getChildren(jsonIndex)}")
            observers.forEach {
                it.elementUpdated(
                    text,
                    compJson.getChildren(jsonIndex),
                    panelView = this,
                    indexToReplace = indexToReplace
                )
            }
        } else if (json != null) {
            observers.forEach { it.elementUpdated(text, json, name, panelView = this, indexToReplace = indexToReplace) }
        }
    }

    private fun informObserversAdded(
        text: String?, panelView: PanelView, indexToReplace: Int, parent: CompositeJSON, key: String? = null,
        newIsArray: Boolean? = null
    ) {
        println("informObserversAdded: $text")
        observers.forEach { it.elementAdded(text, panelView, indexToReplace, parent, key, newIsArray) }
    }

    private fun informObserversRemoved(
        indexToRemove: Int,
        key: String? = null,
        componentIsPanelView: PanelView? = null
    ) {
        observers.forEach {
            if (componentIsPanelView != null && componentIsPanelView.parent is PanelView && compJson.parent != null) {
                println("informObserversRemoved: $indexToRemove PanelView")
                it.elementRemoved(indexToRemove, componentIsPanelView.parent as PanelView, compJson.parent!!, key)
            } else
                it.elementRemoved(indexToRemove, this, compJson, key)
        }
    }

    private fun informObserversNullValue(indexToReplace: Int, key: String? = null) {
        println("informObserversNullValue: $indexToReplace")
        observers.forEach { it.updateNullValue(this, indexToReplace, key) }
    }

    fun removeProperty(indexToRemove: Int) {
        if (compJson.parent == null && components.isEmpty()) return
        if (components.size == 1) {
            (parent as? ObjectProperty)?.removeObjectProperty()
            (parent as? PanelView)?.informObserversRemoved(parent.components.indexOf(this))
        } else {
            remove(indexToRemove)
            revalidate()
            repaint()
        }
    }

    private fun addNewProperty(new: String) {
        add(NewProperty(new))
        revalidate()
        repaint()
    }

    private fun addNewPropertyArray(new: String) {
        println("addNewPropertyArray: $new")
        when (new) {
            IS_PROPERTY -> {
                val field = JTextField()
                field.addKeyListener(Keyboard(field, object : UpdatedAction {
                    override fun invoke(text: String) {
                        if (text.isNotEmpty())
                            informObserversAdded(text, this@PanelView, components.indexOf(field), compJson)
                    }
                }))
                add(field)
                revalidate()
                repaint()
            }

            IS_ARRAY -> {
                informObserversAdded(null, this, components.size, compJson, newIsArray = true)
            }

            else -> {
                informObserversAdded(null, this, components.size, compJson, newIsArray = false)
            }
        }
    }

    private fun getComponent(key: String, value: String): JComponent {
        val type = checkType(value)
        println(type)
        val result = if (type is Boolean) setUpCheckBox(type, compJson, key)
        else if (type == "") setUpCheckNull(compJson, key)
        else {
//            println("getComponent: $type")
            setUpTextField(type.toString(), compJson, key = key)
        }
        result.addMouseListenerToComponent(false)
        return result
    }

    private fun getComponentArray(value: String, index: Int): JComponent {
        println("getComponentArray: $value")
        val type = checkType(value)
        val result = if (type is Boolean) setUpCheckBox(type, compJson)
        else if (type == "") setUpCheckNull(compJson)
        else {
            println("SetUpTextField")
            setUpTextField(type.toString(), compJson, elemIndex = index)
        }
        result.addMouseListenerToComponent(true)
        return result
    }

    private fun JComponent.addMouseListenerToComponent(toArray: Boolean) {
        if (toArray) {
            addMouseListener(
                MouseClick(
                    addAction = { addNewPropertyArray(IS_PROPERTY) },
                    addObjectAction = { addNewPropertyArray(IS_OBJECT) },
                    addArrayAction = { addNewPropertyArray(IS_ARRAY) },
                    deleteAction = { index: Int, key: String?, panel: PanelView? ->
                        informObserversRemoved(
                            index,
                            key,
                            panel
                        )
                    },
                    addNullAction = { indexToReplace: Int, key: String? ->
                        informObserversNullValue(
                            indexToReplace,
                            key
                        )
                    })
            )
        } else {
            addMouseListener(
                MouseClick(
                    addAction = { addNewProperty(IS_PROPERTY) },
                    addObjectAction = { addNewProperty(IS_OBJECT) },
                    addArrayAction = { addNewProperty(IS_ARRAY) },
                    deleteAction = { index: Int, key: String?, panel: PanelView? ->
                        informObserversRemoved(
                            index,
                            key,
                            panel
                        )
                    },
                    addNullAction = { indexToReplace: Int, key: String? ->
                        informObserversNullValue(
                            indexToReplace,
                            key
                        )
                    })
            )
        }
    }

    private fun setUpCheckBox(value: Boolean, json: JsonElement, key: String? = null): JCheckBox {
        val box = JCheckBox().apply { isSelected = value }
        box.addItemListener { e ->
            if (e?.stateChange == ItemEvent.SELECTED || e?.stateChange == ItemEvent.DESELECTED) {
                informObserversUpdated(
                    box.isSelected.toString(),
                    jsonIndex = components.indexOf(box),
                    json = json,
                    name = key
                )
            }
        }
//        box.addMouseListenerToComponent(true)
        return box
    }

    private fun setUpCheckNull(json: JsonElement, key: String? = null): JLabel {
        println("aqui")
        //        nullElement.addMouseListenerToComponent(true)
        return JLabel("N/A")
    }

    private fun setUpTextField(
        value: String,
        parentJsonEle: JsonElement,
        elemIndex: Int? = null,
        key: String? = null
    ): JTextField {
        val textfield = JTextField(value).apply {
            if (text.equals("N/A")) {
                text = ""
            }
            addKeyListener(Keyboard(this, object : UpdatedAction {
                override fun invoke(text: String) {
//                    println("TextField updated invoke")
                    val type = checkType(text)
                    if (parentJsonEle is ArrayJSON && elemIndex != null) {
                        when (type) {
                            is String ->
                                informObserversUpdated(
                                    type,
                                    elemIndex,
                                    indexToReplace = this@PanelView.components.indexOf(this@apply)
                                )

                            is Boolean ->
                                informObserversUpdated(
                                    type.toString(),
                                    elemIndex,
                                    indexToReplace = this@PanelView.components.indexOf(this@apply)
                                )

                            else -> informObserversUpdated(this@apply.text, elemIndex)
                        }

                    } else if (parentJsonEle is ObjectJSON && key != null) {
                        when (type) {
                            is String ->
                                informObserversUpdated(
                                    type,
                                    json = compJson,
                                    name = key,
                                    indexToReplace = this@PanelView.components.indexOf(this@apply.parent)
                                )

                            is Boolean ->
                                informObserversUpdated(
                                    type.toString(),
                                    json = compJson,
                                    name = key,
                                    indexToReplace = this@PanelView.components.indexOf(this@apply.parent)
                                )

                            else -> informObserversUpdated(this@apply.text, json = parentJsonEle, name = key)
                        }
                    }
                }
            }))
        }
        return textfield
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
            if (e?.button == MouseEvent.BUTTON1 && e.component is JLabel &&
                !(e.component.parent is ObjectProperty && e.component.parent.components.indexOf(e.component) == 0 )) actionNALabel(e)
            else if (e?.button == MouseEvent.BUTTON3) actionRightClick(e)
        }

        private fun actionNALabel(e: MouseEvent) {
            println("actionNALabel")
            if (e.component.parent is ObjectProperty) {
                val parent = (e.component.parent as ObjectProperty)
                val key = (parent.components[0] as JLabel).text
                parent.getParentPanel().components.forEach {
                    if (it is ObjectProperty && it.components[0] is JLabel && (it.components[0] as JLabel).text == key) {
                        addNullAction(parent.getParentPanel().components.indexOf(it), key)
                    }
                }
            } else if (e.component.parent is PanelView && compJson is ArrayJSON) {
                println("Entrou    n")
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
                    if (it is JLabel)
                        deleteAction(
                            this@PanelView.components.indexOf(e.component.parent),
                            it.text,
                            null
                        )
                }
            } else {
                if (e.component is PanelView) {
                    println("Delete PanelView")
                    deleteAction(
                        e.component.parent.components.indexOf(e.component),
                        null,
                        e.component as PanelView
                    )
                } else
                    deleteAction(this@PanelView.components.indexOf(e.component), null, null)
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
                val area = getComponent(value = value.toString().replace("\"", ""), key = key)
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
                    if (text.isNotEmpty())
                        removeInitialTextFieldAndAddLabelAndTextField(textField)
                }
            }))
            add(textField)
        }

        private fun removeInitialTextFieldAndAddLabelAndTextField(labelText: JTextField) {
            if (labelText.text.isEmpty())
                return
            when (new) {
                IS_OBJECT -> {
                    removeAll()
                    informObserversAdded(
                        null,
                        this@PanelView,
                        this@PanelView.components.indexOf(this@NewProperty),
                        compJson,
                        labelText.text,
                        false
                    )
                }

                IS_ARRAY -> {
                    removeAll()
                    informObserversAdded(
                        null,
                        this@PanelView,
                        this@PanelView.components.indexOf(this@NewProperty),
                        compJson,
                        labelText.text,
                        true
                    )
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
                            if (text.isNotEmpty())
                                informObserversAdded(
                                    text,
                                    this@PanelView,
                                    this@PanelView.components.indexOf(this@NewProperty),
                                    compJson,
                                    labelText.text
                                )
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
