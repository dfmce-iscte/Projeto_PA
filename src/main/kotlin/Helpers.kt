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
Criar array
 */

class Editor {
//        val model = ObjectJSON()
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
            override fun elementAdded(text: String?, panelView: PanelView, indexToReplace : Int, parent: CompositeJSON, name : String?, newIsArray : Boolean?) {
                if (text == null && parent is ObjectJSON && name != null && newIsArray != null) {
                    if (!newIsArray) {
                        println("Parent is object and new is object")
                        panelView.replaceComponent(indexToReplace, ObjectJSON(parent = parent, name = name), name)
                    }
                    else {
                        println("Parent is object and new is array")
                        panelView.replaceComponent(indexToReplace, ArrayJSON(parent = parent, name = name), name)
                    }
                } else if (text == null && parent is ArrayJSON && newIsArray != null) {
                    if (!newIsArray) {
                        println("Parent is array and new is object")
                        panelView.replaceComponent(indexToReplace, ObjectJSON(parent = parent))
                    }
                    else {
                        println("Parent is array and new is array")
                        panelView.replaceComponent(indexToReplace, ArrayJSON(parent = parent))
                    }
                }
                else if (text != null) {
                    println("Text is not null")
                    panelView.replaceComponent(indexToReplace, checkType(text)?.toJSON(parent = parent, name = name)!!, name)
                }
                model.updateJSON()
            }
            override fun elementRemoved(indexToRemove:Int, panelView: PanelView, parent: CompositeJSON, key : String?) {
                if (parent is ObjectJSON && key != null)
                    parent.removeByKey(key)
                else if (parent is ArrayJSON)
                    parent.removeByIndex(indexToRemove)


                println("indexToRemove: $indexToRemove")
                panelView.removeProperty(indexToRemove)
                model.updateJSON()
            }
            override fun elementUpdated(text: String, json: JsonElement, key: String?, panelView: PanelView?, indexToReplace: Int?) {
                val newValue = checkType(text)
                var newJson : JsonElement? = null
                if (json.parent is ArrayJSON) {
                    newJson = (json.parent as ArrayJSON).updateChildren(json, newValue)
                } else if (json is ObjectJSON){
                    newJson = newValue?.toJSON(parent = json, name = key)
                }
                if (panelView != null && indexToReplace != null) {
                    panelView.replaceComponent(indexToReplace, newJson!!, key)
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

}






