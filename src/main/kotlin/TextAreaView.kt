import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JTextArea
import javax.swing.JTextField

class TextAreaView() : JComponent() {

    fun getComponent(value: String): JComponent {
        //println(value)
        val type = checkType(value)
        //println(type)
        return if (type is Boolean) {
            val box = JCheckBox().apply { if (type) isSelected = true}

            box.addItemListener(object  :ItemListener{
                override fun itemStateChanged(e: ItemEvent?) {
                    if (e?.stateChange == ItemEvent.SELECTED) {
                        box.isSelected=false
                        // Do something when the checkbox is checked
                    } else if (e?.stateChange == ItemEvent.DESELECTED) {
                        box.isSelected=true

                    }
                }
            })
            return box
        } else {
            JTextField(value)
        }
    }

    private fun checkType(text: String): Any? {
        return when {
            text.toIntOrNull() != null -> Int
            text.toDoubleOrNull() != null -> Double
            text.toFloatOrNull() != null -> Float
            text.toLongOrNull() != null -> Long
            text.equals("true", ignoreCase = true) -> true
            text.equals("false", ignoreCase = true) -> false
            text.isEmpty() -> null
            else -> String
        }
    }
}

