import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*

interface PanelViewObserver {
    fun panelAdded(text: String, parent: CompositeJSON, name: String? = null): JsonElement

    fun panelUpdated(text: String, json: JsonElement, name: String? = null)

    fun panelRemoved(parent: CompositeJSON, children: JsonElement) {}
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
                val area = getComponent(value = it.toString())
                if (area is JTextField) {
                    area.addKeyListener(Keyboard(area, json.getElements.indexOf(it)) { text, index, json, name -> informObserversUpdated(text, index, json, name) })
                    area.addMouseListener(MouseClickArray({addNewPropertyArray()},{addNewPropertyArray()}))
                }
                add(area)

            }
        }
    }

    private fun informObserversUpdated(text: String, jsonIndex: Int = -1, json : JsonElement? = null, name: String? = null) {
        if (jsonIndex != -1 && compJson is ArrayJSON) {
            observers.forEach { it.panelUpdated(text, compJson.getChildren(jsonIndex)) }
        } else if(json != null ) {
            observers.forEach { it.panelUpdated(text, json, name) }
        }
    }

    private fun informObserversAdded(text: String, parent: CompositeJSON, name: String? = null) {
        observers.forEach { it.panelAdded(text, parent, name) }
    }

    inner class Keyboard(private val jTextField: JTextField, private val jsonIndex : Int = -1, private val json : JsonElement? = null,
                         private val name : String? = null, val action: (text : String, index : Int, json : JsonElement?, name : String?) -> Unit) : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
//            println("Key pressed")
            if (e?.keyCode == KeyEvent.VK_ENTER) {
                action(jTextField.text, jsonIndex, json, name)
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

    inner class MouseClickArray(val addAction: () -> Unit, val deleteAction: () -> Unit) : MouseAdapter() {
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
                    add(addButton)
                    add(deleteButton)
                }
                frame.isVisible = true
            }

        }
    }

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
                if (area is JTextField) {
                    area.addKeyListener(Keyboard(area, json = value, name = key)  { text, index, json, name ->  informObserversUpdated(text, index, json, name) })
                }
                add(area)
            }
        }
    }

    inner class PropertyArray : JComponent() {

        init {
            layout = GridLayout(0, 1)
            val text = JTextField()
            text.addMouseListener(MouseClickArray({ addNewPropertyArray() }, { addNewPropertyArray() }))
            text.addKeyListener(Keyboard(text) { text, _, name -> informObserversAdded(text, compJson, name) })
            add(text)
        }
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
                        val newtextField = getComponent(value = valueTextField)
                        remove(textField)
                        textField = newtextField
                        add(newtextField)
                        observers.forEach {
                            it.panelAdded(valueTextField, compJson, text.text.split(":")[0])
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

    fun getComponent(key: String? = null, value: String): JComponent {
        val type = checkType(value)
        return if (type is Boolean) {
            val box = JCheckBox().apply { if (type) isSelected = value.toBoolean() }

            box.addItemListener { e ->
                if (e?.stateChange == ItemEvent.SELECTED) {
                    informObserversUpdated(box.isSelected.toString(), json = compJson, name = key)
                } else if (e?.stateChange == ItemEvent.DESELECTED) {
                    informObserversUpdated(box.isSelected.toString(), json = compJson, name = key)
                }
            }
            return box
        } else {
            JTextField(value)
        }
    }

}


fun checkType(text: String): Any? {
    return when {
        text.toIntOrNull() != null -> Int
        text.toDoubleOrNull() != null -> Double
        text.toFloatOrNull() != null -> Float
        text.toLongOrNull() != null -> Long
        text.equals("true", ignoreCase = true) -> true
        text.equals("false", ignoreCase = true) -> false
        text.isEmpty() -> null
        else -> String
    }
}
