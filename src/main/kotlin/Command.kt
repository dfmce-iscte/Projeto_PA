interface Command {
    fun run()
    fun undo()
}

class AddCommand(private val parent: CompositeJSON, private val element: Any, private val key : String? = null) : Command {
    private var jsonElement : JsonElement? = null

    override fun run() {
        jsonElement = element.toJSON(parent = parent, name = key)
    }

    override fun undo() {
        if (parent is ObjectJSON && key != null) parent.removeByKey(key)
        else if (parent is ArrayJSON) parent.removeChildren(jsonElement!!)
    }
}

class DeleteCommand(private val element : JsonElement, private val key : String? = null) : Command {

    override fun run() {
        when(element.parent) {
            is ObjectJSON -> (element.parent as ObjectJSON).removeByKey(key!!)
            else -> (element.parent as ArrayJSON).removeChildren(element)
        }
    }

    override fun undo() {
        when(element.parent) {
            is ObjectJSON -> (element.parent as ObjectJSON).addElement(key!!,element)
            else -> (element.parent as ArrayJSON).addElement(element)
        }
    }
}