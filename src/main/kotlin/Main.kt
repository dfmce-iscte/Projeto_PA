interface JSONELEMENT {
    var parent: CompositeJSON?

    fun accept(v: Visitor)

}

interface Visitor {
    fun visit(c: ObjectJSON): Boolean = true
    fun endVisit(c: ObjectJSON) {}
    fun visit(c: ArrayJSON): Boolean = true
    fun endVisit(c: ArrayJSON) {}

    fun visit(l: JSONNumber) {}
    fun visit(l: JSONString) {}
    fun visit(l: JSONBoolean) {}
    fun visit(l: JSONNull) {}
}

interface CompositeJSON : JSONELEMENT {
    val name: String?
    override var parent: CompositeJSON?

    override fun accept(v: Visitor)

    fun addToParent() {
        if (parent != null && parent is ObjectJSON)
            name?.let { (parent as ObjectJSON).addElement(it, this) }
        else if (parent != null && parent is ArrayJSON)
            (parent as ArrayJSON).addElement(this)
    }

}

class ObjectJSON(override var parent: CompositeJSON? = null, override val name: String? = null) : CompositeJSON {
    var properties: LinkedHashMap<String, JSONELEMENT> = LinkedHashMap()

    init {
        if (parent is ObjectJSON && name == null)
            throw IllegalArgumentException("Name can't be null when parent is ObjectJSON")
        addToParent()
    }

    private fun auxToString(): String {
        return properties.map { "\"${it.key}\":${it.value}" }.joinToString(prefix = "{", postfix = "}")
    }

    override fun toString(): String {
//        return if (parent is ObjectJSON) "\"${name}\":${auxToString()}"
//        else auxToString()
        return auxToString()
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

class ArrayJSON(override var parent: CompositeJSON? = null, override val name: String? = null) : CompositeJSON {
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



class JSONString(val value: String) : JSONELEMENT {
    override var parent: CompositeJSON? = null

    constructor(value: String, parent: ObjectJSON, name: String) : this(value = value) {
        parent.addElement(name, this)
        this.parent = parent
    }

    constructor(value: String, parent: ArrayJSON) : this(value = value) {
        parent.addElement(this)
        this.parent = parent
    }

    override fun toString(): String = "\"$value\""

    override fun accept(v: Visitor) = v.visit(this)
}

class JSONNumber(val value: Number) : JSONELEMENT {
    override var parent: CompositeJSON? = null

    constructor(value: Number, parent: ObjectJSON, name: String) : this(value = value) {
        parent.addElement(name, this)
        this.parent = parent
    }

    constructor(value: Number, parent: ArrayJSON) : this(value = value) {
        parent.addElement(this)
        this.parent = parent
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)
}

class JSONBoolean(val value: Boolean) : JSONELEMENT {
    override var parent: CompositeJSON? = null

    constructor(value: Boolean, parent: ObjectJSON, name: String) : this(value = value) {
        parent.addElement(name, this)
        this.parent = parent
    }

    constructor(value: Boolean, parent: ArrayJSON) : this(value = value) {
        parent.addElement(this)
        this.parent = parent
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)
}

class JSONNull(val value : Any? = null) : JSONELEMENT {
    override var parent: CompositeJSON? = null

    constructor(parent: ObjectJSON, name: String) : this() {
        parent.addElement(name, this)
        this.parent = parent
    }

    constructor(parent: ArrayJSON) : this() {
        parent.addElement(this)
        this.parent = parent
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)
}

fun valueToString(value: Any?): String = "$value"
