interface JSONELEMENT {
    val parent: CompositeJSON?

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

abstract class CompositeJSON(override val parent: CompositeJSON? = null) : JSONELEMENT {

    abstract override fun accept(v: Visitor)

    abstract override fun toString(): String

}

class ObjectJSON(override val parent: CompositeJSON? = null) : CompositeJSON(parent) {
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }

    init {
        if(parent is ArrayJSON) parent.addElement(this)
    }

    val properties: LinkedHashMap<String, JSONELEMENT> = LinkedHashMap()

    private fun auxToString(): String {
        return properties.map { "\"${it.key}\":${it.value}" }.joinToString(prefix = "{", postfix = "}")
    }

    override fun toString(): String = auxToString()

    fun addElement(name: String, element: JSONELEMENT) {
        properties[name] = element
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            properties.forEach { it.value.accept(v) }
        v.endVisit(this)
    }

}

class ArrayJSON(override val parent: CompositeJSON? = null) : CompositeJSON(parent) {
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }
    init {
        if(parent is ArrayJSON) parent.addElement(this)
    }

    val elements = mutableListOf<JSONELEMENT>()
    fun addElement(element: JSONELEMENT) {
        elements.add(element)
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            elements.forEach { it.accept(v) }
        v.endVisit(this)
    }

    override fun toString(): String {
        return "[${elements.joinToString { it.toString() }}]"
    }
}



class JSONString(val value: String, override val parent: CompositeJSON? = null) : JSONELEMENT {
    constructor(value: String, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = "\"$value\""

    override fun accept(v: Visitor) = v.visit(this)
}

class JSONNumber(val value: Number, override val parent: CompositeJSON? = null) : JSONELEMENT {
    constructor(value: Number, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)
}

class JSONBoolean(val value: Boolean, override val parent: CompositeJSON? = null) : JSONELEMENT {
    constructor(value: Boolean, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)
}

class JSONNull(val value : Any? = null, override val parent: CompositeJSON? = null) : JSONELEMENT {
    constructor(parent: ObjectJSON, name: String) : this(parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }
    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)
}

fun valueToString(value: Any?): String = "$value"
