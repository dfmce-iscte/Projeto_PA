import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea

class LabelView(private val model: JsonValues) : JTextArea() {
    init {
        text = "$model"
        model.addObserver(object : JsonElementSetObserver {
            override fun JsonElementAdded(value: JsonElement) {
                text = "$model"
            }

            override fun JsonElementReplaced(valueOld: JsonElement, valueNew: JsonElement) {
                text = "$model"
            }

            override fun JsonElementRemoved(value: JsonElement) {
                text = "$model"
            }
        }
        )

    }
}