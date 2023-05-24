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
interface JsonElementObserver {

    fun elementAdded(children: JsonElement) {}

    fun elementRemoved(children: JsonElement) {}
    fun updateJSON() {}
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

    fun getKey(value: JsonElement):String {
        properties.forEach{
            if (it.value === value) return it.key
        }
        return ""
    }

    private fun auxToString(): String {
        return properties.map { "\n"+"\t".repeat(it.value.depth)+"\"${it.key}\":${it.value}" }.joinToString(prefix = "{", postfix = "\n"+"\t".repeat(this.depth)+"}")
    }

    override fun toString(): String = auxToString()

    internal fun addElement(name: String, element: JsonElement) {
//        observers.forEach { element.addObserver(it) }
//        println("Name: $name Element: $element")
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

    fun removeByKey(key : String) = properties.remove(key)

}

class ArrayJSON(override val parent: CompositeJSON? = null) : CompositeJSON {
    override val observers = mutableListOf<JsonElementObserver>()
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    fun removeByIndex(index: Int) = elements.removeAt(index)


    private val elements = mutableListOf<JsonElement>()

    val getElements : MutableList<JsonElement>
        get() = elements

    fun addElement(element: JsonElement) {
//        println("Adding element to array")
//        observers.forEach { element.addObserver(it) }
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

    fun updateChildren(children: JsonElement, value: Any?) : JsonElement{
        val newValue = when(value){
            is String -> JSONString(value, this)
            is Number -> JSONNumber(value, this)
            is Boolean -> JSONBoolean(value, this)
            else -> JSONNull(this)
        }
        elements.forEach{
            if (it === children)  {
                val index = elements.indexOf(it)
                println("Old value: ${elements[index]}")
                elements[index] = newValue
                println("New value: ${elements[index]}")
            }
        }
        elements.removeAt(elements.size - 1)
        updateJSON()
        return newValue
    }

    fun getChildren(index:Int) = elements[index]

}


class JSONString(private var value: String, override val parent: CompositeJSON? = null) : JsonElement {
    override val observers = mutableListOf<JsonElementObserver>()
    constructor(value: String, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = "\"$value\""

    override fun accept(v: Visitor) = v.visit(this)

    fun updateValue(newValue: String){
        value=newValue
        updateJSON()
    }

    fun getValue() = value
    //alterar só para get()
}

class JSONNumber(private var value: Number, override val parent: CompositeJSON? = null) : JsonElement {
    override val observers = mutableListOf<JsonElementObserver>()
    constructor(value: Number, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    fun updateValue(newValue: Number) {
        value=newValue
        updateJSON()
    }

    fun getValue() = value
}

class JSONBoolean(private var value: Boolean, override val parent: CompositeJSON? = null) : JsonElement {
    override val observers = mutableListOf<JsonElementObserver>()
    constructor(value: Boolean, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    fun updateValue(newValue: Boolean){
        value=newValue
        updateJSON()
    }

    fun getValue() = value
}

class JSONNull(private val value: Any? = null, override val parent: CompositeJSON? = null) : JsonElement {
    override val observers = mutableListOf<JsonElementObserver>()
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


