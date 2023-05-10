import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*

class ScrollPane(private val model: JsonValues) : JScrollPane() {

    init {
        addMouseListener(MouseClick())
        viewport.add(
            JPanel().apply {
                horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_ALWAYS
                verticalScrollBarPolicy = VERTICAL_SCROLLBAR_ALWAYS
            })
    }

    fun addNewPanel() {
        val newPanel = PanelView(first = true)
        setViewportView(newPanel)
        revalidate()
        repaint()
    }

    inner class MouseClick() : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            if (e?.button == MouseEvent.BUTTON3) {

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
                        add(addButton)
                    }
                    frame.isVisible = true
                }
            }
        }



//    fun testPanel(): JPanel =
//        JPanel().apply {
//            layout = BoxLayout(this, BoxLayout.Y_AXIS)
//            alignmentX = Component.LEFT_ALIGNMENT
//            alignmentY = Component.TOP_ALIGNMENT
//
//            add(testWidget("A", "um"))
//            add(testWidget("B", "dois"))
//            add(testWidget("C", "tres"))
//
//            // menu
//
//        }

//    fun testWidget(key: String, value: String): JPanel =
//        JPanel().apply {
//            layout = BoxLayout(this, BoxLayout.X_AXIS)
//            alignmentX = Component.LEFT_ALIGNMENT
//            alignmentY = Component.TOP_ALIGNMENT
//
//            add(JLabel(key))
//            val text = JTextField(value)
//            text.addFocusListener(object : FocusAdapter() {
//                override fun focusLost(e: FocusEvent) {
//                    println("perdeu foco: ${text.text}")
//                }
//            })
//            add(text)
//        }
    }