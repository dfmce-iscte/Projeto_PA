import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

interface PanelViewObserver {
    fun panelAdded(text : String, parent: CompositeJSON, name : String? = null) : JsonElement

    fun panelRemoved(parent: CompositeJSON, children : JsonElement) {}
}

class PanelView(private val objectParent : CompositeJSON, first : Boolean) : JPanel(){

    private val observers : MutableList<PanelViewObserver> = mutableListOf()
    private var jsonElement : JsonElement? = null

    val getJsonElement : JsonElement?
        get() = jsonElement
    fun addObserver(observer: PanelViewObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: PanelViewObserver) {
        observers.remove(observer)
    }



    init {
        layout = GridLayout(0,1)

        add(Property())

        objectParent.addObserver(object : JsonElementObserver {
            override fun elementAdded(children: JsonElement) {
//                add()
            }

            override fun elementRemoved(children: JsonElement) {

            }
        })

        addMouseListener(MouseClick({ addNewProperty() }, { addNewProperty()}))
    }

    inner class Keyboard(private val textField : JTextField, val action : (JTextField) -> Unit) : KeyAdapter(){
        override fun keyPressed(e: KeyEvent?) {
            if(e?.keyCode== KeyEvent.VK_ENTER){
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

    inner class MouseClick(val addAction : () -> Unit, val deleteAction: () -> Unit) : MouseAdapter() {
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


    inner class Property : JComponent() {
        private var textField : JTextField? = null
        init {
            layout = GridLayout(0,1)
            border=BorderFactory.createRaisedBevelBorder()
            val text = JTextField()
            text.addKeyListener(Keyboard(text) {
                removeInitialTextFieldAndAddLabelAndTextField(text)
            })
            add(text)

        }

        private fun removeInitialTextFieldAndAddLabelAndTextField(text : JTextField) {
            if (text.text.contains(":") && objectParent !is ObjectJSON) { /*dar erro*/}
            else if (text.text.contains(":")) {
                removeAll()
                layout = GridLayout(0, 2)
                textField = JTextField()
                add(JLabel(text.text))
                add(textField)
                revalidate()
                repaint()
                textField?.addKeyListener(Keyboard(textField!!) {
                    observers.forEach {
                        it.panelAdded(textField!!.text, objectParent, text.text.split(":")[0])
                    }
                    // transformar em checkbox, ou em lista, ....
                })
            }
        }

        private fun deleteProperty() {
            remove(this)
//            observers.forEach { it.panelRemoved() }
        }

     }
}
