import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import javax.swing.*


fun main() {
    Editor().open()
}

data class Point(val x: Int, val y: Int)
data class Mix(val name: String = "") {
    val list = arrayListOf(1, 2, 3, 4)
    val listChar = arrayListOf('c', 'f', 'g')
    val number = 0
    val decimal = 15.56
    val char = 'c'
    val string = "STRING"
    val nullProperty = null
    val point = Point(1, 2)
    val bool = true
    val hasMap = hashMapOf("foo" to 1, "bar" to 2)
    var set = setOf(1, 2, 3, 2, 1)

}

/*
Criar array
 */

class Editor {
    //        val model = ObjectJSON()
    val model = (Mix("ze").toJSON() as ObjectJSON)

    val commands = mutableListOf<Command>()

    val frame = JFrame("JSON Object Editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        layout = GridLayout(0, 1)
        size = Dimension(600, 600)
        setLocationRelativeTo(null)

        val total = JPanel().apply {
            layout = GridLayout(0, 2)

            val scrollPane = JScrollPane().apply {
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

                val panel = PanelView(compJson = model)
                panel.addObserver(object : PanelViewObserver {
                    override fun elementAdded(
                        text: String?,
                        panelView: PanelView,
                        indexToReplace: Int,
                        parent: CompositeJSON,
                        name: String?,
                        newIsArray: Boolean?
                    ) {
//                println("Element added text : $text")
                        val cmd = AddCommand(text, panelView, indexToReplace, parent, name, newIsArray)
                        commands.add(cmd)
                        cmd.run()
                    }

                    override fun elementRemoved(
                        indexToRemove: Int,
                        panelView: PanelView,
                        parent: CompositeJSON,
                        key: String?
                    ) {
                        var cmd: DeleteCommand? = null
                        if (indexToRemove == -1) {
                            println("indexToRemove == -1")
                            when (val parentOfpanel = panelView.parent) {
                                is PanelView.ObjectProperty -> {
                                    println("ObjectProperty Index ${parentOfpanel.parent.components.indexOf(parentOfpanel)}")
                                    cmd = DeleteCommand(parentOfpanel.parent.components.indexOf(parentOfpanel), (parentOfpanel.parent as PanelView), parent.parent!!, key)
                                }
                                is PanelView -> {
                                    println("PanelView Index ${parentOfpanel.components.indexOf(parentOfpanel)}")
                                    cmd = DeleteCommand(parentOfpanel.components.indexOf(parentOfpanel), parentOfpanel, parent.parent!!, key)
                                }
                            }
                        } else cmd = DeleteCommand(indexToRemove, panelView, parent, key)
                        commands.add(cmd!!)
                        cmd.run()
                    }

                    override fun elementUpdated(
                        newValue: Any,
                        json: JsonElement,
                        indexToReplace: Int,
                        panelView: PanelView,
                        key: String?
                    ) {
                        val cmd = UpdateCommand(newValue, json, indexToReplace, panelView, key)
                        commands.add(cmd)
                        cmd.run()
                    }

                    override fun updateNullValue(panelView: PanelView, indexToReplace: Int, key: String?) {
//                        println("updateNullValue")
                        panelView.replaceComponent(indexToReplace, JSONString("N/A"), key)
                        model.updateJSON()
                    }
                })
                setViewportView(panel)
            }
            add(scrollPane)

            val right = JPanel().apply {
                layout = BorderLayout()
                val textArea = JTextArea().apply {
                    size = Dimension(300, 550)
                    tabSize = 2
                    text = "$model"
                    isEditable = false
                    font = Font("SansSerif", Font.BOLD, 20)
                }

                model.addObserver(object : CompositeJsonObserver {
                    override fun elementAdded() {
                        textArea.text = "$model"
                    }

                    override fun elementRemoved() {
                        textArea.text = "$model"
                    }

                    override fun updateJSON() {
                        textArea.text = "$model"
                    }
                })

                val undoButton = JButton("Undo").apply {
                    size = Dimension(300, 50)
                    addActionListener {
                        commands.removeLastOrNull()?.undo()
                    }
                }
                add(undoButton, BorderLayout.NORTH)
                add(textArea, BorderLayout.CENTER)
            }
            add(right)


        }
        add(total)


    }

    fun open() {
        frame.isVisible = true
    }


}










