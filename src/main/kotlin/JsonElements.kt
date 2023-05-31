sealed interface JsonElement {

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
interface CompositeJsonObserver {

    fun elementAdded() {}

    fun elementRemoved() {}
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
    val observers : MutableList<CompositeJsonObserver>

    fun addObserver(observer: CompositeJsonObserver)

    fun removeObserver(observer: CompositeJsonObserver)

    fun updateJSON () {
        observers.forEach { it.updateJSON() }
    }

    fun informElementAdded() {
        observers.forEach { it.elementAdded() }
    }

    fun informElementRemoved() {
        observers.forEach { it.elementRemoved() }
    }



    override fun accept(v: Visitor)

    override fun toString(): String

    fun removeChildren(children : JsonElement)


}

class ObjectJSON(override val parent: CompositeJSON? = null) : CompositeJSON {
    override val observers = mutableListOf<CompositeJsonObserver>()
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }
    private val properties: LinkedHashMap<String, JsonElement> = LinkedHashMap()

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }


    override fun addObserver(observer: CompositeJsonObserver) {
        observers.add(observer)
        properties.forEach {
            val value = it.value
            if (value is CompositeJSON)
                value.addObserver(observer)
        }
    }

    override fun removeObserver(observer: CompositeJsonObserver) {
        observers.remove(observer)
        properties.forEach {
            val value = it.value
            if (value is CompositeJSON)
                value.removeObserver(observer)
        }
    }

    fun getProperties() = properties.toMap()

    private fun auxToString(): String {
        return properties.map { "\n"+"\t".repeat(it.value.depth)+"\"${it.key}\":${it.value}" }.joinToString(prefix = "{", postfix = "\n"+"\t".repeat(this.depth)+"}")
    }

    override fun toString(): String = auxToString()

    fun addElement(name: String, element: JsonElement) {
//        observers.forEach { element.addObserver(it) }
//        println("Name: $name Element: $element")
        properties[name] = element
        if (element is CompositeJSON)
            observers.forEach { element.addObserver(it) }
        informElementAdded()
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
        informElementRemoved()
    }

    fun removeByKey(key : String) : JsonElement? {
        val removed = properties.remove(key)
        informElementRemoved()
        return removed
    }

}

class ArrayJSON(override val parent: CompositeJSON? = null) : CompositeJSON {
    override val observers = mutableListOf<CompositeJsonObserver>()
    private val elements = mutableListOf<JsonElement>()
    constructor(parent: ObjectJSON, name: String) : this(parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    fun removeByIndex(index: Int) : JsonElement {
        val removed = elements.removeAt(index)
        informElementRemoved()
        return removed
    }



    override fun addObserver(observer: CompositeJsonObserver) {
        observers.add(observer)
        elements.forEach {
            if (it is CompositeJSON)
                it.addObserver(observer)
        }
    }

    override fun removeObserver(observer: CompositeJsonObserver) {
        observers.remove(observer)
        elements.forEach {
            if (it is CompositeJSON)
                it.removeObserver(observer)
        }
    }


    val getElements : MutableList<JsonElement>
        get() = elements

    fun addElement(element: JsonElement, index: Int? = null) {
//        println("Adding element to array")
//        observers.forEach { element.addObserver(it) }
        if (index != null) elements.add(index, element)
        else elements.add(element)
       // println("Before add observer element: $element")
        if (element is CompositeJSON)
            observers.forEach {
             //   println("Adding observer to element: $element")
                element.addObserver(it)
            }
      //  println("After add observer element: $element")
        informElementAdded()
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
        informElementRemoved()
    }

    fun updateChildren(index: Int, value: Any?) : JsonElement{
        val newValue = if (value is String && value.isNotEmpty()) JSONString(value, this)
        else if (value is Number) JSONNumber(value, this)
        else if (value is Boolean) JSONBoolean(value, this)
        else JSONNull(parent = this)

        elements[index] = newValue
        elements.removeAt(elements.size - 1)

        updateJSON()

        return newValue
    }

    fun getChildren(index:Int) = elements[index]

}


class JSONString(private var value: String, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(value: String, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = "\"$value\""

    override fun accept(v: Visitor) = v.visit(this)

    val getValue : String
        get() = value
}

class JSONNumber(private var value: Number, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(value: Number, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }
    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    val getValue : Number
        get() = value
}

class JSONBoolean(private var value: Boolean, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(value: Boolean, parent: ObjectJSON, name: String) : this(value = value, parent = parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    val getValue : Boolean
        get() = value
}

class JSONNull(val value: Any? = null, override val parent: CompositeJSON? = null) : JsonElement {
    constructor(parent: ObjectJSON, name: String) : this(parent = parent) {
        parent.addElement(name, this)
    }

    init {
        if (parent is ArrayJSON) parent.addElement(this)
    }

    override fun toString(): String = valueToString(value)

    override fun accept(v: Visitor) = v.visit(this)

    val getValue: Any? = value
}

private fun valueToString(value: Any?): String = "$value"


