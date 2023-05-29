import java.awt.Component
import javax.swing.JCheckBox
import javax.swing.JTextField

abstract class Command(private val model: ObjectJSON) {
    abstract fun run()
    abstract fun undo()
}

class AddCommand(
    private val model: ObjectJSON,
    private val text: String?,
    private val panelView: PanelView,
    private val indexToReplace: Int,
    private val parent: CompositeJSON,
    private val key: String?,
    private val newIsArray: Boolean?
) : Command(model) {
    private var jsonElement: JsonElement? = null

    override fun run() {
        if (text == null && parent is ObjectJSON && key != null && newIsArray != null) {
            if (!newIsArray) {
//                println("Parent is object and new is object")
                jsonElement = ObjectJSON(parent = parent, name = key)
                panelView.replaceComponent(indexToReplace, jsonElement as ObjectJSON, key)
            } else {
//                println("Parent is object and new is array")
                jsonElement = ArrayJSON(parent = parent, name = key)
                panelView.replaceComponent(indexToReplace, jsonElement as ArrayJSON, key)
            }
        } else if (text == null && parent is ArrayJSON && newIsArray != null) {
            if (!newIsArray) {
//                println("Parent is array and new is object")
                jsonElement = ObjectJSON(parent = parent)
                panelView.replaceComponent(indexToReplace, jsonElement as ObjectJSON)
            } else {
//                println("Parent is array and new is array")
                jsonElement = ArrayJSON(parent = parent)
                panelView.replaceComponent(indexToReplace, jsonElement as ArrayJSON)
            }
        } else if (text == null || text == "null") {
//            println("Text is null")
            jsonElement = checkType("").toJSON(parent = parent, name = key)
            panelView.replaceComponent(indexToReplace, jsonElement!!, key)
        } else {
            jsonElement = checkType(text).toJSON(parent = parent, name = key)
            panelView.replaceComponent(indexToReplace, jsonElement!!, key)
        }
        model.updateJSON()
    }

    override fun undo() {
        if (parent is ObjectJSON && key != null) parent.removeByKey(key)
        else if (parent is ArrayJSON) parent.removeByIndex(indexToReplace)
        panelView.remove(indexToReplace)
        println("Undo AddCommand")
        model.updateJSON()
    }
}

class DeleteCommand(
    private val model: ObjectJSON,
    private val indexToRemove: Int,
    private val panelView: PanelView,
    private val parent: CompositeJSON,
    private val key: String?
) : Command(model) {
    private var jsonElement: JsonElement? = null
    private var componentRemoved: Component? = null

    override fun run() {
        jsonElement = when (parent) {
            is ObjectJSON -> parent.removeByKey(key!!)
            is ArrayJSON -> parent.removeByIndex(indexToRemove)
        }
        componentRemoved = panelView.removeProperty(indexToRemove)
        model.updateJSON()
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
        model.updateJSON()
    }
}

class UpdateCommand(
    private val model: ObjectJSON,
    private val newValue: Any,
    private val json: JsonElement,
    private val indexToReplace: Int,
    private val panelView: PanelView,
    private val key: String?
) : Command(model) {

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
        println("Run Update Old json: $oldJson")
        println("Run Update New Json $newJson")
        model.updateJSON()
    }

    override fun undo() {
        println("Undo Update oldJson: $newJson")
        println("Undo Update newJson: $oldJson")
        if (json.parent is ArrayJSON)
            newJson = newJson?.let { (json.parent as ArrayJSON).updateChildren(indexToReplace, oldValue) }
        else if (json is ObjectJSON)
            newJson = oldValue.toJSON(parent = json, name = key)

        when (val comp = panelView.components[indexToReplace]) {
            is PanelView.ObjectProperty -> checkComponent(comp.components[1])
            else -> checkComponent(comp)
        }
        model.updateJSON()
    }

    private fun checkComponent(comp: Component) {
        if (comp is JTextField && oldJson !is JSONNull)
            comp.text = oldValue.toString()
        else if (comp is JCheckBox && oldJson is JSONBoolean)
            comp.isSelected = oldValue as Boolean
        else
            panelView.replaceComponent(indexToReplace, oldJson!!, key)
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




