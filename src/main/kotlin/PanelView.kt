import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

interface PanelViewObserver {
    fun panelAdded(text: String, parent: CompositeJSON, name: String? = null): JsonElement

    fun panelRemoved(parent: CompositeJSON, children: JsonElement) {}
}

class PanelView(private val compJson: CompositeJSON) : JPanel() {

    private val observers: MutableList<PanelViewObserver> = mutableListOf()

    fun addObserver(observer: PanelViewObserver) {
        observers.add(observer)
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
            compJson.getElements.forEach { it ->
                if (it is CompositeJSON) add(PanelView(it))
                else {
                    val area = TextAreaView().getComponent(it.toString())
                    if (area is JTextField) {
                        area.addKeyListener(Keyboard(area) { informObserversAdded(area.text, compJson) })
                        println("Array Textfield")
                    }
                    add(TextAreaView().getComponent(it.toString()))
                }
            }
        } else if (compJson is ObjectJSON) {
            println("Is objectJson")
            compJson.getProperties().forEach{
                add(ObjectProperty(it.key, it.value))
            }
        }


        addMouseListener(MouseClick({ addNewProperty() }, { addNewProperty() }))
    }

    private fun informObserversAdded(text: String, parent: CompositeJSON, name: String? = null) {
        println("New value: $text")
        observers.forEach { it.panelAdded(text, parent, name) }
    }

    inner class Keyboard(private val textField: JComponent, val action: (JComponent) -> Unit) : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            println("Key pressed")
            if (e?.keyCode == KeyEvent.VK_ENTER) {
                action(textField)
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
            if (value is CompositeJSON) add(PanelView(value))
            else  {
                val area = TextAreaView().getComponent(value.toString())
                if (area is JTextField) {
                    area.addKeyListener(Keyboard(area)  { informObserversAdded(area.text, compJson, key) })
                }
                add(area)
            }
        }
    }


    inner class Property : JComponent() {
        private var textField: JComponent? = null

        init {
            layout = GridLayout(0, 1)
            border = BorderFactory.createRaisedBevelBorder()
            val text = JTextField()
            text.addKeyListener(Keyboard(text) {
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
                textField?.addKeyListener(Keyboard(textField!!) {
                    if (textField is JTextField) {
                        val valueTextField = (textField as JTextField).text
                        val newtextField = TextAreaView().getComponent(valueTextField)
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
}
