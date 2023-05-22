import javafx.scene.Parent
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.*
import javax.swing.*


fun main() {
    Editor().open()
}
data class Point(val x: Int, val y: Int)
data class Mix(val name: String = "") {
    val list = arrayListOf(1, 2, 3, 4)
    //    val listChar = arrayListOf('c', 'f', 'g')
    val number = 0
    val decimal = 15.56
    //    val char = 'c'
//    val string = "STRING"
//    val nullProperty = null
    val point = Point(1, 2)
    val bool = true
//    val hasMap = hashMapOf("foo" to 1, "bar" to 2)
//    var set = setOf(1, 2, 3, 2, 1)

}

/*
* Se passarmos um object class do Mix, e dps dá-se run, tem de ter os paneis logo criados
* Os panels reagem ao modelo.J
* */

/*
    Fazer botão delete
    Criar botão para criar objeto
    Adiconar novos elementos a arrays
    Corrigir checkbox

 */

class Editor {
    //    val model = ObjectJSON()
    val model = (Mix("ze").toJSON() as CompositeJSON)
    val frame = JFrame("JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 2)
        size = Dimension(600, 600)
        setLocationRelativeTo(null)

        val scrollPane= JScrollPane().apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        }

        val panel = PanelView(compJson = model)
        panel.addObserver(object : PanelViewObserver {
            override fun elementAdded(text: String, parent: CompositeJSON, name : String?): JsonElement {
                //convert text to right object type e dps toJSON
                return checkType(text)?.toJSON(parent = parent, name = name)!!
            }
            override fun elementRemoved(parent: CompositeJSON, children : JsonElement) {
                parent.removeChildren(children)
                //falta ver onde se remove o panel. Aqui ou na classe PanelView
                model.updateJSON()
            }
            override fun elementUpdated(text: String, json: JsonElement, name: String?) {
                val newValue = checkType(text)
                if (json.parent is ArrayJSON) {
                    println("New array value: $newValue")
                    (json.parent as ArrayJSON).updateChildren(json, newValue)
                } else if (json is ObjectJSON){
                    println("New object value: $newValue")
                    println(name)
                    newValue?.toJSON(parent = json, name = name)
                }
                model.updateJSON()
            }
        })
        scrollPane.setViewportView(panel)
        add(scrollPane)

        val right = JPanel()
        right.layout = GridLayout()
        val textArea = JTextArea().apply {
            tabSize=2
            text = "$model"
            isEditable = false
        }
        right.add(textArea)
        add(right)

        model.addObserver(object : JsonElementObserver {
            override fun elementAdded(children: JsonElement) {
                textArea.text = "$model"
            }

            override fun elementRemoved(children: JsonElement) {
                textArea.text = "$model"
            }

            override fun updateJSON() {
                textArea.text = "$model"
            }
        })

    }

    fun open() {
        frame.isVisible = true
    }

//    fun checkType(text: String): Any? {
//        return when {
//            text.toIntOrNull() != null -> text.toInt()
//            text.toDoubleOrNull() != null -> text.toDouble()
//            text.toFloatOrNull() != null -> text.toFloat()
//            text.toLongOrNull() != null -> text.toLong()
//            text.equals("true", ignoreCase = true) -> true
//            text.equals("false", ignoreCase = true) -> false
//            text.isEmpty() -> null
//            else -> text
//        }
//    }

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






