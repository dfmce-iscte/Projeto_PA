import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

interface PanelViewObserver {
    fun panelAdded() {}

    fun panelRemoved(panel : PanelView) {}
}

class PanelView(private val parent : PanelView? = null, first : Boolean) : JPanel(){

    private val observers : MutableList<PanelViewObserver> = mutableListOf()

    init {
        layout = GridLayout(0,1)

        if (first) add(Property())

        addMouseListener(MouseClick())
        parent?.addChildren(object : PanelViewObserver {
            override fun panelRemoved(panel : PanelView) {
                remove(panel)
            }
        })
    }

    private fun createJsonElement(text: JTextField, name: String) {
//        if (text.text.toIntOrNull() != null) {
//
//        }

    }

    private fun addChildren(observer: PanelViewObserver) {
        observers.add(observer)
    }


    inner class MouseClick() : MouseAdapter() {
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
                        addNewPanel()
                    }
                    val deleteButton = JButton("delete")
                    deleteButton.addActionListener {
//                        observers.forEach {
//                            it.panelRemoved(this@PanelView)
//                        }
                    }
                    add(addButton)
                    add(deleteButton)
                }
                frame.isVisible = true
            }

        }
    }


    inner class Property : JComponent() {
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
            if (text.text.contains(":")) {
                val panel = PanelView(first = false).apply { layout = GridLayout(0,2) }
                val label = JLabel(text.text)
                remove(text)
                panel.add(label)
                val textField = JTextField()
                panel.add(textField)
                add(panel)
                revalidate()
                repaint()
                textField.addKeyListener(Keyboard(textField) {
                    createJsonElement(textField,text.text)
                })
            }
        }
    }
}