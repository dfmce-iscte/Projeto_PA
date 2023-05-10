import javax.swing.JTextArea

class TextAreaView(private val model: JsonValues) : JTextArea() {
    init {
        tabSize=2
        text = "$model"
        isEditable = false
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