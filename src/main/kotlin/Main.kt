interface JSONELEMENT {
    val parent: CompositeJSON?

    fun accept(v: Visitor)

}

interface Visitor {
    fun visit(c: ObjectJSON): Boolean = true
    fun endVisit(c: ObjectJSON) {}
    fun visit(c: ArrayJSON): Boolean = true
    fun endVisit(c: ArrayJSON) {}

    fun visit(l: LeafJSON)
}

interface CompositeJSON : JSONELEMENT {
    val name: String?
    override val parent: CompositeJSON?

    override fun accept(v: Visitor)

    fun addToParent() {
        if (parent != null && parent is ObjectJSON)
            name?.let { (parent as ObjectJSON).addElement(it, this) }
        else if (parent != null && parent is ArrayJSON)
            (parent as ArrayJSON).addElement(this)
    }

}

class ObjectJSON(override val parent: CompositeJSON? = null, override val name: String? = null) : CompositeJSON {
    var properties: HashMap<String, JSONELEMENT> = HashMap()

    init {
        if (parent is ObjectJSON && name == null)
            throw IllegalArgumentException("Name can't be null when parent is ObjectJSON")
        addToParent()
    }

    private fun auxToString(): String {
        return properties.map { "\"${it.key}\":${it.value}" }.joinToString(prefix = "{", postfix = "}")
    }

    override fun toString(): String {
        return if (parent is ObjectJSON) "\"${name}\":${auxToString()}"
        else auxToString()
    }

    fun addElement(name: String, element: JSONELEMENT) {
        properties[name] = element
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            properties.forEach { it.value.accept(v) }
        v.endVisit(this)
    }

}

class ArrayJSON(override val parent: CompositeJSON? = null, override val name: String? = null) : CompositeJSON {
    val elements = mutableListOf<JSONELEMENT>()

    fun addElement(element: JSONELEMENT) {
        elements.add(element)
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            elements.forEach { it.accept(v) }
        v.endVisit(this)
    }

    init {
        if (parent == null) throw IllegalArgumentException("Parent cannot be null")
        addToParent()
    }

    override fun toString(): String {
        return "[${elements.joinToString { it.toString() }}]"
    }
}


// value pode ser open??
abstract class LeafJSON(name: String, open val value: Any? = null, final override val parent: CompositeJSON?) :
    JSONELEMENT {

    init {
        if (parent is ObjectJSON)
            parent.addElement(name, this)
        else if (parent is ArrayJSON)
            parent.addElement(this)
    }

    override fun toString(): String {
        return if (value is String) "\"$value\""
        else "$value"
    }

    override fun accept(v: Visitor) = v.visit(this)
}

class JSONString(parentObject: CompositeJSON?, override val value: String, name: String = "") :
    LeafJSON(name, value, parentObject)

class JSONNumber(parentObject: CompositeJSON?, override val value: Number, name: String = "") :
    LeafJSON(name, value, parentObject)

class JSONBoolean(parentObject: CompositeJSON?, override val value: Boolean, name: String = "") :
    LeafJSON(name, value, parentObject)

class JSONNull(parentObject: CompositeJSON?, name: String = "") : LeafJSON(name, parent = parentObject)

