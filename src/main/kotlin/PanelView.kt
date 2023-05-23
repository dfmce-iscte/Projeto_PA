import java.awt.*
import java.awt.event.*
import javax.swing.*

interface PanelViewObserver {
    fun elementAdded(text: String?, panelView: PanelView, indexToReplace : Int, parent: CompositeJSON, name: String? = null)

    fun elementUpdated(text: String, json: JsonElement, key: String? = null, panelView: PanelView? = null, indexToReplace: Int? = null)

    fun elementRemoved(parent: CompositeJSON, children: JsonElement) {}
}

interface UpdatedAction {
    fun invoke(text: String) {}

}
class PanelView(private val compJson: CompositeJSON) : JPanel() {

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
        if ((compJson is ObjectJSON && compJson.getProperties().isEmpty()) ||
            (compJson is ArrayJSON && compJson.getElements.isEmpty())) {
            add(NewProperty(false))
        } else if (compJson is ArrayJSON) {
            compJsonIsArray(compJson)
        } else if (compJson is ObjectJSON) {
            compJson.getProperties().forEach{
                add(ObjectProperty(it.key, it.value))
            }
        }
    }

    fun replaceComponent(indexToReplace: Int, json: JsonElement, key: String? = null) {
        remove(indexToReplace)
        if (key != null)
            add(ObjectProperty(key,json), indexToReplace)
        else if (json is ArrayJSON)
            compJsonIsArray(json)
        else
            add(getComponentArray(json.toString().replace("\"", ""), indexToReplace), indexToReplace)
    }

    private fun compJsonIsArray(json : ArrayJSON) {
        json.getElements.forEach { it ->
            if (it is CompositeJSON) {
                val panel = PanelView(it)
                observers.forEach { panel.addObserver(it) }
                add(PanelView(it))
            }
            else {
                val area = getComponentArray(value = it.toString().replace("\"", ""), json.getElements.indexOf(it))
                add(area)
            }
        }
    }

    private fun informObserversUpdated(text: String, jsonIndex: Int? = null, json : JsonElement? = null, name: String? = null, indexToReplace: Int? = null) {
        if (jsonIndex != null && compJson is ArrayJSON) {
//            println("informObserversUpdated: Array")
//            println("Children: ${compJson.getChildren(jsonIndex)}")
            observers.forEach { it.elementUpdated(text, compJson.getChildren(jsonIndex), panelView = this, indexToReplace = indexToReplace) }
        } else if(json != null) {
            observers.forEach { it.elementUpdated(text, json, name, panelView = this, indexToReplace = indexToReplace) }
        }
    }

    private fun informObserversAdded(text: String?, panelView: PanelView, indexToReplace : Int, parent: CompositeJSON, key: String? = null) {
        observers.forEach { it.elementAdded(text, panelView, indexToReplace, parent, key) }
    }

    inner class Keyboard(private val jTextField: JTextField, private val action : UpdatedAction) : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (e?.keyCode == KeyEvent.VK_ENTER) {
                action.invoke(jTextField.text)
            }
        }
    }

    private fun removeProperty() {
        remove(this)
        revalidate()
        repaint()
    }

    private fun addNewProperty(isObject: Boolean) {
        add(NewProperty(isObject))
        revalidate()
        repaint()
    }

    private fun addNewPropertyArray() {
        val field = JTextField()
        field.addKeyListener(Keyboard(field, object : UpdatedAction {
            override fun invoke(text: String) {
                informObserversAdded(field.text, this@PanelView, components.indexOf(field), compJson)
            }
        }))
        add(field)
        revalidate()
        repaint()
    }

    inner class MouseClick(val addAction: () -> Unit, val deleteAction: () -> Unit, val addObjectAction : () -> Unit) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            println("Mouse Clicked")
            if (e?.button == MouseEvent.BUTTON3) {
                println("Right button")

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
                        deleteAction()
                    }
                    add(addButton)
                    add(addObjectButton)
                    add(deleteButton)
                }
                frame.isVisible = true
            }

        }
    }

    inner class ObjectProperty(key : String, value : JsonElement) : JComponent() {
        init {
            layout = GridLayout(0, 2)
            border = BorderFactory.createRaisedBevelBorder()
            add(JLabel(key))
            if (value is CompositeJSON)  {
                val panel = PanelView(value)
                observers.forEach { panel.addObserver(it) }
                add(panel)
            }
            else  {
                val area = getComponent(value = value.toString().replace("\"", ""), key = key)
                add(area)
            }
        }
    }

    inner class NewProperty(private val isObject : Boolean) : JComponent() {
        init {
            layout = GridLayout(0, 1)
            border = BorderFactory.createRaisedBevelBorder()
            val textField = JTextField()
            textField.addKeyListener(Keyboard(textField, object : UpdatedAction {
                override fun invoke(text: String) {
                    removeInitialTextFieldAndAddLabelAndTextField(textField)
                }
            }))
            add(textField)
        }

        private fun removeInitialTextFieldAndAddLabelAndTextField(labelText: JTextField) {
            if (isObject) {
                removeAll()
                informObserversAdded(null, this@PanelView,this@PanelView.components.indexOf(this@NewProperty), compJson, labelText.text)
            }
            else if (!(labelText.text.contains(":") && compJson !is ObjectJSON)) {
                removeAll()
                layout = GridLayout(0, 2)
                val textField = JTextField()
                val label = JLabel(labelText.text).apply {
                    addMouseListener(MouseClick({ addNewProperty(false) }, addObjectAction = {addNewProperty(true)}, deleteAction = { removeProperty() }))
                }
                add(label)
                add(textField)
                revalidate()
                repaint()
                textField.addKeyListener(Keyboard(textField, object : UpdatedAction {
                    override fun invoke(text: String) {
                        informObserversAdded(textField.text, this@PanelView, this@PanelView.components.indexOf(this@NewProperty), compJson, labelText.text)
                    }
                }))
            }
        }
    }

    private fun getComponent(key: String, value: String): JComponent {
        val type = checkType(value)
        return if (type is Boolean) setUpCheckBox(type,compJson, key)
        else {
//            println("getComponent: $type")
            setUpTextField(type.toString(), compJson, key = key)
        }
    }

    private fun getComponentArray(value: String, index : Int): JComponent {
//        println("getComponentArray: $value")
        val type = checkType(value)
        return if (type is Boolean) setUpCheckBox(type,compJson)
        else {
            setUpTextField(type.toString(), compJson, elemIndex = index)
        }
    }

    private fun JComponent.addMouseListenerToArray() = addMouseListener(MouseClick({addNewPropertyArray()}, {addNewPropertyArray()}, {addNewPropertyArray()}))
    private fun setUpCheckBox(value: Boolean, json: JsonElement, key: String? = null): JCheckBox {
        val box = JCheckBox().apply { isSelected = value }
        box.addItemListener { e ->
            if (e?.stateChange == ItemEvent.SELECTED || e?.stateChange == ItemEvent.DESELECTED) {
                informObserversUpdated(box.isSelected.toString(), jsonIndex = components.indexOf(box), json = json, name = key)
            }
        }
        box.addMouseListenerToArray()
        return box
    }

    private fun setUpTextField(value: String, jsonEle : JsonElement, elemIndex : Int? = null, key : String? = null) : JTextField {
        val textfield = JTextField(value).apply {
            addMouseListenerToArray()
            addKeyListener(Keyboard(this, object : UpdatedAction {
                override fun invoke(text: String) {
//                    println("TextField updated invoke")
                    val type = checkType(text)
                    if (type is Boolean) {
                        val x = setUpCheckBox(type, compJson)
                        add(x)
                        if (jsonEle is ArrayJSON) {
                            informObserversUpdated(x.isSelected.toString(), elemIndex, indexToReplace = this@PanelView.components.indexOf(this@apply))
                        }
                        else {
                            informObserversUpdated(x.isSelected.toString(), json = compJson, name = key, indexToReplace = this@PanelView.components.indexOf(this@apply.parent))
                        }
                    } else if (jsonEle is ArrayJSON)
                        informObserversUpdated(this@apply.text, elemIndex)
                    else if (jsonEle is ObjectJSON)
                        informObserversUpdated(this@apply.text, json = jsonEle, name = key)
                }
            }))
        }
        return textfield
    }
}



fun checkType(text: String): Any? {
    return when {
        text.toIntOrNull() != null -> text.toInt()
        text.toDoubleOrNull() != null -> text.toDouble()
        text.toFloatOrNull() != null -> text.toFloat()
        text.toLongOrNull() != null -> text.toLong()
        text.equals("true", ignoreCase = true) -> true
        text.equals("false", ignoreCase = true) -> false
        text.isEmpty() -> null
        else -> text
    }
}
