import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField

interface Command {
     fun run()
     fun undo()
}

class AddCommand(
    private val text: String?,
    private val panelView: PanelView,
    private val indexToReplace: Int,
    private val parent: CompositeJSON,
    private val key: String?,
    private val newIsArray: Boolean?
) : Command {
    private var jsonElement: JsonElement? = null

    override fun run() {
        if (text == null && parent is ObjectJSON && key != null && newIsArray != null) {
            jsonElement = if (!newIsArray) {
                //                println("Parent is object and new is object")
                ObjectJSON(parent = parent, name = key)
            } else {
                //                println("Parent is object and new is array")
                ArrayJSON(parent = parent, name = key)
            }
        } else if (text == null && parent is ArrayJSON && newIsArray != null) {
            jsonElement = if (!newIsArray) {
                //                println("Parent is array and new is object")
                ObjectJSON(parent = parent)
            } else {
                //                println("Parent is array and new is array")
                ArrayJSON(parent = parent)
            }
        } else if (text == null || text == "null") {
//            println("Text is null")
            jsonElement = checkType("").toJSON(parent = parent, name = key)
        } else {
            jsonElement = checkType(text).toJSON(parent = parent, name = key)
        }
        panelView.replaceComponent(indexToReplace, jsonElement!!, key)
    }

    override fun undo() {
        if (parent is ObjectJSON && key != null) parent.removeByKey(key)
        else if (parent is ArrayJSON) parent.removeByIndex(indexToReplace)
//        println("Remove index $indexToReplace Size before ${panelView.components.size}")
        panelView.remove(indexToReplace)
        if (panelView.components.isEmpty())
            panelView.revalidateAndRepaint()

//        println("Remove index $indexToReplace Size after ${panelView.components.size}")
    }
}

class DeleteCommand(
    private val indexToRemove: Int,
    private val panelView: PanelView,
    private val parent: CompositeJSON,
    private val key: String?
) : Command {
    private var jsonElement: JsonElement? = null
    private var componentRemoved: Component? = null

    override fun run() {
        jsonElement = when (parent) {
            is ObjectJSON -> parent.removeByKey(key!!)
            is ArrayJSON -> parent.removeByIndex(indexToRemove)
        }
        componentRemoved = panelView.removeProperty(indexToRemove)
    }


    override fun undo() {
        when (parent) {
            is ObjectJSON -> {
                jsonElement?.let { parent.addElement(key!!, it) }
                componentRemoved?.let { panelView.add(it) }
            }

            is ArrayJSON -> {
                jsonElement?.let { parent.addElement(it, indexToRemove) }
                componentRemoved?.let { panelView.add(it, indexToRemove) }
            }
        }
        if (panelView.components.size == 1)
            panelView.revalidateAndRepaint()
    }
}

class UpdateCommand(
    private val newValue: Any,
    private val json: JsonElement,
    private val indexToReplace: Int,
    private val panelView: PanelView,
    private val key: String?
) : Command {

    private var newJson: JsonElement? = null
    private val oldValue =
        if (json is ObjectJSON) {
            getValue(json.getProperties()[key])
        } else {
            getValue(json)
        }
    private val oldJson =
        if (json is ObjectJSON) {
            json.getProperties()[key]
        } else {
            json
        }

    override fun run() {
        if (json.parent is ArrayJSON) {
            newJson = (json.parent as ArrayJSON).updateChildren(indexToReplace, newValue)
        } else if (json is ObjectJSON) {
            newJson = newValue.toJSON(parent = json, name = key)
        }
        if ((newJson is JSONBoolean && oldJson !is JSONBoolean) || newJson is JSONNull) {
            panelView.replaceComponent(indexToReplace, newJson!!, key)
        }
//        println("Run Update Old json: $oldJson")
//        println("Run Update New Json $newJson")
    }

    override fun undo() {
//        println("Undo Update oldJson: $newJson")
//        println("Undo Update newJson: $oldJson")
        if (json.parent is ArrayJSON) {
            newJson = newJson?.let { (json.parent as ArrayJSON).updateChildren(indexToReplace, oldValue) }
            checkComponent(panelView.components[indexToReplace], indexToReplace)
        }
        else if (json is ObjectJSON) {
            newJson = oldValue.toJSON(parent = json, name = key)
            getComponentOfObjectProperty()?.let { checkComponent(it, panelView.components.indexOf(it.parent)) }
        }
    }

    private fun getComponentOfObjectProperty() : Component? {
        key?.let {
            panelView.components.forEach {
                if (it is PanelView.ObjectProperty)
                    when (val label = it.components[0]) {
                        is JLabel -> {
//                            println("Label text: ${label.text} key: $key")
                            if (label.text == key) return it.components[1]
                        }
                    }
            }
        }
        return null
    }

    private fun checkComponent(comp: Component, index : Int) {
        if (comp is JTextField && oldJson !is JSONNull)
            comp.text = oldValue.toString()
        else if (comp is JCheckBox && oldJson is JSONBoolean)
            comp.isSelected = oldValue as Boolean
        else
            panelView.replaceComponent(index, oldJson!!, key)
    }
    private fun getValue(json: JsonElement?): Any {
//        println("Get value $json")
        return when (json) {
            is JSONBoolean -> json.getValue
            is JSONNumber -> json.getValue
            is JSONString -> json.getValue
            else -> ""
        }
    }
}




