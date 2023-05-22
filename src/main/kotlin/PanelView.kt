import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*

interface PanelViewObserver {
    fun elementAdded(text: String, parent: CompositeJSON, name: String? = null): JsonElement

    fun elementUpdated(text: String, json: JsonElement, name: String? = null)

    fun elementRemoved(parent: CompositeJSON, children: JsonElement) {}
}

interface UpdatedAction {
    fun invoke(text: String, index: Int? = null, json: JsonElement? = null, name: String? = null)

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
            add(Property())
        } else if (compJson is ArrayJSON) {
            compJsonIsArray(compJson)
        } else if (compJson is ObjectJSON) {
            compJson.getProperties().forEach{
                add(ObjectProperty(it.key, it.value))
            }
        }
        addMouseListener(MouseClick({ addNewProperty() }, { removeProperty() }))
    }

    private fun compJsonIsArray(json : ArrayJSON) {
        json.getElements.forEach { it ->
            if (it is CompositeJSON) {
                val panel = PanelView(it)
                observers.forEach { panel.addObserver(it) }
                add(PanelView(it))
            }
            else {
                val area = getComponentArray(value = it.toString(), json.getElements.indexOf(it))
                add(area)
            }
        }
    }

    private fun informObserversUpdated(text: String, jsonIndex: Int? = null, json : JsonElement? = null, name: String? = null) {
        if (jsonIndex != null && compJson is ArrayJSON) {
            println("informObserversUpdated: Array")
            observers.forEach { it.elementUpdated(text, compJson.getChildren(jsonIndex)) }
        } else if(json != null) {
            observers.forEach { it.elementUpdated(text, json, name) }
        }
    }

    private fun informObserversAdded(text: String, parent: CompositeJSON, name: String? = null) {
        observers.forEach { it.elementAdded(text, parent, name) }
    }

    inner class Keyboard(private val jTextField: JTextField, private val action : UpdatedAction) : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (e?.keyCode == KeyEvent.VK_ENTER) {
                action.invoke(jTextField.text)
            }
        }
    }

    inner class OtherKeyboard(private val jTextField: JTextField, val action : (JTextField) -> Unit): KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            println("Key pressed")
            if (e?.keyCode == KeyEvent.VK_ENTER) {
                action(jTextField)
            }
        }
    }

    private fun removeProperty() {
        remove(this)
        revalidate()
        repaint()
    }

    private fun addNewProperty() {
        add(Property())
        revalidate()
        repaint()
    }

    private fun addNewPropertyArray() {
        add(PropertyArray())
        revalidate()
        repaint()
    }

//    inner class MouseClickArray(val addAction: () -> Unit, val deleteAction: () -> Unit) : MouseAdapter() {
//        override fun mouseClicked(e: MouseEvent?) {
//            if (e?.button == MouseEvent.BUTTON3) {
//                println("Right button")
//                val frame = JFrame("Add new element").apply {
//                    defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
//                    layout = GridBagLayout()
//                    size = Dimension(400, 100)
//                    setLocationRelativeTo(null)
//
//                    val addButton = JButton("add")
//                    addButton.addActionListener {
//                        dispose()
//                        addAction()
//                    }
//                    val deleteButton = JButton("delete")
//                    deleteButton.addActionListener {
//                        dispose()
//                        deleteAction()
//                    }
//                    add(addButton)
//                    add(deleteButton)
//                }
//                frame.isVisible = true
//            }
//
//        }
//    }

    inner class MouseClick(val addAction: () -> Unit, val deleteAction: () -> Unit) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            if (e?.button == MouseEvent.BUTTON3) {
                println("Right button")

                val frame = JFrame("Add new element").apply {
                    defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
                    layout = GridBagLayout()
                    size = Dimension(400, 100)
                    setLocationRelativeTo(null)

                    val addButton = JButton("add")
                    addButton.addActionListener {
                        dispose()
                        addAction()
                    }
                    val deleteButton = JButton("delete")
                    deleteButton.addActionListener {
                        dispose()
                        deleteAction()
                    }
                    //criar button para criar novo objeto
                    add(addButton)
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
                val area = getComponent(value = value.toString(), key = key)
                add(area)
            }
        }
    }

    inner class PropertyArray : JComponent() {

//        init {
//            layout = GridLayout(0, 1)
//            val text = JTextField()
//            text.addMouseListener(MouseClickArray({ addNewPropertyArray() }, { addNewPropertyArray() }))
//            text.addKeyListener(Keyboard(text) { text, _, name -> informObserversAdded(text, compJson, name) })
//            add(text)
//        }
    }


    inner class Property : JComponent() {
        private var textField: JComponent? = null

        init {
            layout = GridLayout(0, 1)
            border = BorderFactory.createRaisedBevelBorder()
            val text = JTextField()
            text.addKeyListener(OtherKeyboard(text) {
                removeInitialTextFieldAndAddLabelAndTextField(text)
            })
            add(text)

        }

        //melhorar este função
        private fun removeInitialTextFieldAndAddLabelAndTextField(text: JTextField) {
            if (text.text.contains(":") && compJson !is ObjectJSON) {
                /*dar erro*/
            } else if (text.text.contains(":")) {
                removeAll()
                layout = GridLayout(0, 2)
                textField = JTextField()
                add(JLabel(text.text))
                add(textField)
                revalidate()
                repaint()
                textField?.addKeyListener(OtherKeyboard((textField as JTextField)) {
                    if (textField is JTextField) {
                        val valueTextField = (textField as JTextField).text
//                        val newtextField = getComponent(value = valueTextField)
//                        remove(textField)
//                        textField = newtextField
//                        add(newtextField)
                        observers.forEach {
                            it.elementAdded(valueTextField, compJson, text.text.split(":")[0])
                        }
                        // transformar em checkbox, ou em lista, ....
                    }
                })
            }
        }

        private fun deleteProperty() {
            remove(this)
//            observers.forEach { it.panelRemoved() }
        }

    }

    private fun getComponent(key: String, value: String): JComponent {
        val type = checkType(value)
        return if (type is Boolean) setUpCheckBox(type,compJson, key)
        else {
            setUpTextField(type.toString(), compJson, key = key)
        }
    }

    private fun getComponentArray(value: String, index : Int): JComponent {
        val type = checkType(value)
        return if (type is Boolean) setUpCheckBox(type,compJson)
        else {
            setUpTextField(type.toString(), compJson, elemIndex = index)
        }
    }

    private fun JComponent.addMouseListenerToArray() = addMouseListener(MouseClick({addNewPropertyArray()}, {addNewPropertyArray()}))
    private fun setUpCheckBox(value: Boolean, json : JsonElement, key: String? = null) : JCheckBox {
        val box = JCheckBox().apply { isSelected = value }
        box.addItemListener { e ->
            if (e?.stateChange == ItemEvent.SELECTED || e?.stateChange == ItemEvent.DESELECTED) {
                informObserversUpdated(box.isSelected.toString(), json = json, name = key)
            }
        }
        box.addMouseListenerToArray()
        return box
    }

    private fun setUpTextField(value: String, jsonEle : JsonElement, elemIndex : Int? = null, key : String? = null) : JTextField {
        val textfield = JTextField(value).apply {
            addMouseListenerToArray()
            if (jsonEle is ArrayJSON)
                addKeyListener(Keyboard(this, object: UpdatedAction {
                    override fun invoke(text: String, index: Int?, json: JsonElement?, name: String?) {
                        println("TextField updated invoke")
                        informObserversUpdated(this@apply.text, elemIndex)
                    }
                }))
            else
                addKeyListener(Keyboard(this, object: UpdatedAction {
                    override fun invoke(text: String, index: Int?, json: JsonElement?, name: String?) {
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
