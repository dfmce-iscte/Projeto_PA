import java.awt.Component
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ScrollPane (private val model: JsonValues) : JScrollPane() {
    init {
    testPanel().apply {
        horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
    }

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