sealed interface JsonElement {
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

sealed interface CompositeJSON : JsonElement {
    override val parent: CompositeJSON?

    override fun accept(v: Visitor)

    abstract override fun toString(): String

}

class ObjectJSON(override val parent: CompositeJSON? = null) : CompositeJSON(parent) {
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    private val properties: LinkedHashMap<String, JsonElement> = LinkedHashMap()


    fun getProperties() = properties.toMap()


    private fun auxToString(): String {
        return properties.map { "\"${it.key}\":${it.value}" }.joinToString(prefix = "{", postfix = "}")
    }

    override fun toString(): String = auxToString()

    internal fun addElement(name: String, element: JsonElement) {
        properties[name] = element
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            properties.forEach { it.value.accept(v) }
        v.endVisit(this)
    }

}

class ArrayJSON(override val parent: CompositeJSON? = null) : CompositeJSON {
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    private val elements = mutableListOf<JsonElement>()
    fun addElement(element: JsonElement) {
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


class JSONString(private val value: String, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(value: String, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = "\"$value\""

    override fun accept(v: Visitor) = v.visit(this)

    fun getValue() = value
}

class JSONNumber(private val value: Number, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(value: Number, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    fun getValue() = value
}

class JSONBoolean(private val value: Boolean, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(value: Boolean, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    fun getValue() = value
}

class JSONNull(private val value: Any? = null, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(parent: ObjectJSON, name: String) : this(parent = parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    fun getValue() = value
}

fun valueToString(value: Any?): String = "$value"


