import javax.swing.JTextArea

class TextAreaView(private val model: JsonValues) : JTextArea() {
    init {
        tabSize=2
        text = "$model"
        isEditable = false
        model.addObserver(object : JsonElementObserver {
//            override fun jsonElementAdded(value: JsonElement) {
//                text = "$model"
//            }
//
//            override fun jsonElementReplaced(valueOld: JsonElement, valueNew: JsonElement) {
//                text = "$model"
//            }
//
//            override fun jsonElementRemoved(value: JsonElement) {
//                text = "$model"
//            }
            override fun updateJSON() {
                text = "$model"
            }
        }
        )

    }
}