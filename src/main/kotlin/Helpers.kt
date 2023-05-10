import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*


fun main() {
    Editor().open()
}
data class Mix(val name: String = "") {
    val list = arrayListOf(1, 2, 3, 4)
    val listChar = arrayListOf('c', 'f', 'g')
    val nullProperty = null
    val number = 0
    val decimal = 15.56
    val char = 'c'
    val string = "STRING"
    val bool = true
    val hasMap = hashMapOf("foo" to 1, "bar" to 2)
    var set = setOf(1, 2, 3, 2, 1)

}

class Editor {
    val model=JsonValues(Mix("ze").toJSON())
    val frame = JFrame("Josue - JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)
        setLocationRelativeTo(null)

        val scrollPane=ScrollPane(model)
//        val left = PanelView()
//        left.layout = GridLayout()
//        scrollPane.add(left)
////        val scrollPane1 = JScrollPane(testPanel()).apply {
////            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
////            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
////        }
////        left.add(scrollPane1)

        add(scrollPane)

        val right = JPanel()
        right.layout = GridLayout()
        right.add(TextAreaView(model))
        add(right)
    }

    fun open() {
        frame.isVisible = true
    }

    fun testPanel(): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(testWidget("A", "um"))
            add(testWidget("B", "dois"))
            add(testWidget("C", "tres"))

            // menu
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        val menu = JPopupMenu("Message")
                        val add = JButton("add")
                        add.addActionListener {
                            val text = JOptionPane.showInputDialog("text")
                            add(testWidget(text, "?"))
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        val del = JButton("delete all")
                        del.addActionListener {
                            components.forEach {
                                remove(it)
                            }
                            menu.isVisible = false
                            revalidate()
                            frame.repaint()
                        }
                        menu.add(add);
                        menu.add(del)
                        menu.show(this@apply, 100, 100);
                    }
                }
            })
        }


    fun testWidget(key: String, value: String): JPanel =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            add(JLabel(key))
            val text = JTextField(value)
            text.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent) {
                    println("perdeu foco: ${text.text}")
                }
            })
            add(text)
        }
}






