import java.awt.*
import java.awt.event.*
import javax.swing.*

interface PanelViewObserver {
    fun elementAdded(text: String?, panelView: PanelView, indexToReplace : Int, parent: CompositeJSON, name: String? = null, newIsArray : Boolean? = null)

    fun elementUpdated(text: String, json: JsonElement, key: String? = null, panelView: PanelView? = null, indexToReplace: Int? = null)

    fun elementRemoved(indexToRemove: Int, panelView: PanelView, parent: CompositeJSON, key : String? = null) {}
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
        if ((compJson is ObjectJSON && compJson.getProperties().isEmpty()))
            add(NewProperty(IS_PROPERTY))
        else if (compJson is ArrayJSON && compJson.getElements.isEmpty()) {
            addNewPropertyArray()
        } else if (compJson is ArrayJSON) {
            println("compJson is array")
            compJsonIsArray(compJson)
        } else if (compJson is ObjectJSON) {
            compJson.getProperties().forEach{
                add(ObjectProperty(it.key, it.value))
            }
        }
    }

    fun replaceComponent(indexToReplace: Int, json: JsonElement, key: String? = null) {
        println("${key == null}")
        println(json)
        remove(indexToReplace)
        if (key != null) {
            println("key is not null")
            println("json is Array ${json is ArrayJSON}")
            add(ObjectProperty(key,json), indexToReplace)
        }
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

    private fun informObserversAdded(text: String?, panelView: PanelView, indexToReplace : Int, parent: CompositeJSON, key: String? = null,
                                     newIsArray : Boolean? = null) {
        observers.forEach { it.elementAdded(text, panelView, indexToReplace, parent, key, newIsArray) }
    }

    private fun informObserversRemoved(indexToRemove : Int, key : String? = null) {
        println("informObserversRemoved: $indexToRemove")
        observers.forEach { it.elementRemoved(indexToRemove, this, compJson, key) }
    }

    fun removeProperty(indexToRemove : Int) {
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

    private fun getComponent(key: String, value: String): JComponent {
        val type = checkType(value)
        val result = if (type is Boolean) setUpCheckBox(type,compJson, key)
        else {
//            println("getComponent: $type")
            setUpTextField(type.toString(), compJson, key = key)
        }
        result.addMouseListenerToComponent(false)
        return result
    }

    private fun getComponentArray(value: String, index : Int): JComponent {
        println("getComponentArray: $value")
        val type = checkType(value)
        val result = if (type is Boolean) setUpCheckBox(type,compJson)
        else {
            setUpTextField(type.toString(), compJson, elemIndex = index)
        }
        result.addMouseListenerToComponent(true)
        return result
    }

    private fun JComponent.addMouseListenerToComponent(toArray : Boolean) {
        if (toArray) {addMouseListener(MouseClick(addAction = { addNewPropertyArray() }, addObjectAction = { addNewPropertyArray() },
            addArrayAction = {addNewProperty(IS_ARRAY)}, deleteAction = { index: Int, key: String? -> informObserversRemoved(index, key) }))
        } else {
            addMouseListener(
                MouseClick(
                    addAction = { addNewProperty(IS_PROPERTY) },
                    addObjectAction = { addNewProperty(IS_OBJECT) },
                    addArrayAction = { addNewProperty(IS_ARRAY) },
                    deleteAction = { index: Int, key: String? -> informObserversRemoved(index, key) })
            )
        }
    }
    private fun setUpCheckBox(value: Boolean, json: JsonElement, key: String? = null): JCheckBox {
        val box = JCheckBox().apply { isSelected = value }
        box.addItemListener { e ->
            if (e?.stateChange == ItemEvent.SELECTED || e?.stateChange == ItemEvent.DESELECTED) {
                informObserversUpdated(box.isSelected.toString(), jsonIndex = components.indexOf(box), json = json, name = key)
            }
        }
        box.addMouseListenerToComponent(true)
        return box
    }

    private fun setUpTextField(value: String, jsonEle : JsonElement, elemIndex : Int? = null, key : String? = null) : JTextField {
        val textfield = JTextField(value).apply {
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

    inner class Keyboard(private val jTextField: JTextField, private val action : UpdatedAction) : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (e?.keyCode == KeyEvent.VK_ENTER) {
                action.invoke(jTextField.text)
            }
        }
    }

    inner class MouseClick(val addAction: () -> Unit, val deleteAction: (Int, String?) -> Unit, val addObjectAction : () -> Unit, val addArrayAction : () -> Unit) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
//            println("\nMouse Clicked")
//            println("is PanelView ${e?.component is PanelView}")
//            println("is Label ${e?.component is JLabel}")
            if (e?.button == MouseEvent.BUTTON3) {
//                println("Right button")

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
                        if (e.component.parent is ObjectProperty) {
                            e.component.parent.components.forEach {
                                if (it is JLabel)
                                    deleteAction(this@PanelView.components.indexOf(e.component.parent), it.text)
                            }
                        }
                        else {
                            deleteAction(this@PanelView.components.indexOf(e.component), null)
                        }
                    }

                    val addArrayButton=JButton("Add Array")
                    addArrayButton.addActionListener{
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

        }
    }

    inner class ObjectProperty(val key : String, value : JsonElement) : JComponent() {
        init {
            layout = GridLayout(0, 2)
            border = BorderFactory.createRaisedBevelBorder()
            val label = JLabel(key).apply {
                addMouseListenerToComponent(false)
            }
            add(label)
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

        fun removeObjectProperty() {
            informObserversRemoved(this@PanelView.components.indexOf(this), key)
        }
    }

    inner class NewProperty(private val new : String) : JComponent() {
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
            if (new == IS_OBJECT && labelText.text.contains(":")) {
                removeAll()
                informObserversAdded(null, this@PanelView,this@PanelView.components.indexOf(this@NewProperty), compJson, labelText.text, false)
            } else if (new == IS_ARRAY && labelText.text.contains(":")) {
                removeAll()
                informObserversAdded(null, this@PanelView,this@PanelView.components.indexOf(this@NewProperty), compJson, labelText.text, true)
            } else if (!(labelText.text.contains(":") && compJson !is ObjectJSON)) {
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
                        informObserversAdded(textField.text, this@PanelView, this@PanelView.components.indexOf(this@NewProperty), compJson, labelText.text)
                    }
                }))
            }
        }
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
