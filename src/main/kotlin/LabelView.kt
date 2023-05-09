import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea

class LabelView(private val model: JsonValues) : JTextArea() {
    init {
        text = "$model"

    }
}