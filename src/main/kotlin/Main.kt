sealed interface JsonElement {
    val observers : MutableList<JsonElementObserver>

    fun addObserver(observer: JsonElementObserver) = observers.add(observer)

    fun removeObserver(observer: JsonElementObserver) = observers.remove(observer)

    fun updateJSON () {
        observers.forEach { it.updateJSON() }
    }

    fun informElementAdded(children: JsonElement) {
        observers.forEach { it.elementAdded(children) }
    }

    fun informElementRemoved(children: JsonElement) {
        observers.forEach { it.elementRemoved(children) }
    }

    val parent: CompositeJSON?

    fun accept(v: Visitor)

    val depth : Int
        get() = calculateDepth()


    private fun calculateDepth(): Int {
        tailrec fun aux(dir : JsonElement, currentDepth : Int) : Int {
            return if (dir.parent != null) {
                aux(dir.parent!!, currentDepth+1)
            } else {
                currentDepth
            }
        }
        return aux(this, 0)
    }
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

    override fun toString(): String

    fun removeChildren(children : JsonElement)

}

class ObjectJSON(override val parent: CompositeJSON? = null) : CompositeJSON {
    override val observers = mutableListOf<JsonElementObserver>()
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    private val properties: LinkedHashMap<String, JsonElement> = LinkedHashMap()


    fun getProperties() = properties.toMap()


    private fun auxToString(): String {
        return properties.map { "\n"+"\t".repeat(it.value.depth)+"\"${it.key}\":${it.value}" }.joinToString(prefix = "{", postfix = "\n"+"\t".repeat(this.depth)+"}")
    }

    override fun toString(): String = auxToString()

    internal fun addElement(name: String, element: JsonElement) {
        properties[name] = element
        informElementAdded(element)
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            properties.forEach { it.value.accept(v) }
        v.endVisit(this)
    }

    override fun removeChildren(children: JsonElement) {
        properties.forEach{
            if (it.value === children) properties.remove(it.key) //comprar pelas instancias e nao pelo value
        }
        informElementRemoved(children)
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
        informElementAdded(element)
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            elements.forEach { it.accept(v) }
        v.endVisit(this)
    }

    override fun toString(): String {
        return "[${elements.joinToString { "\n"+"\t".repeat(it.depth)+it.toString() }}\n"+"\t".repeat(this.depth)+"]"
    }

    override fun removeChildren(children: JsonElement) {
        elements.remove(children)
        informElementRemoved(children)
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
    //alterar só para get()
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

private fun valueToString(value: Any?): String = "$value"


