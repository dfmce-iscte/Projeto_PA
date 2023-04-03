interface JSONELEMENT {
    val name : String
    val parentObject : CompositeJSON?

    fun accept(v: Visitor)

}

interface Visitor {
    fun visit(c: CompositeJSON) : Boolean = true
    fun endVisit(c: CompositeJSON) {}

    fun visit(l: LeafJSON)
}

abstract class CompositeJSON(override val name: String = "", override val parentObject: CompositeJSON? = null) : JSONELEMENT {
    val atributtes = mutableListOf<JSONELEMENT>()

    open fun addElement(element: JSONELEMENT) {
        atributtes.add(element)
    }

    override fun accept(v: Visitor) {
        if (v.visit(this))
            atributtes.forEach{ it.accept(v) }
        v.endVisit(this)
    }
}

class ObjectJSON(override val name: String = "", override val parentObject: CompositeJSON? = null) : CompositeJSON(name, parentObject) {

    override fun toString(): String {
        return atributtes.map { it.toString() }.joinToString(separator = ", ", prefix = "{", postfix = "}")
    }

    init {
        parentObject?.addElement(this)
    }

}

class ArrayJSON(override val name: String, override val parentObject: CompositeJSON?) : CompositeJSON(name,parentObject) {

    override fun addElement(element: JSONELEMENT) {
        if (atributtes.size > 0 && element::class != atributtes[0]::class) throw IllegalArgumentException("All elements must be of the same type")
        super.addElement(element)
    }

    init {
        if (parentObject == null) throw IllegalArgumentException("Parent must be a ObjectJSON")
        parentObject.addElement(this)
    }
    override fun toString(): String {
        return "\"$name\": [${atributtes.joinToString { it.toString() }}]"
    }
}


// perguntar se por exemplo criar uma Date temos de converter para String ou devolve-se erro
abstract class LeafJSON(override val name: String, override val parentObject: CompositeJSON?) : JSONELEMENT {
    open val value: Any? = null

    init {
        parentObject?.addElement(this)
    }
    override fun toString(): String {
        return "\"$name\" : \"$value\""
    }

    override fun accept(v: Visitor) = v.visit(this)
}

class Number<T>(private val value: T) {
    init {
        if (value !is Int && value !is Float && value !is Double) throw IllegalArgumentException("Value must be a Number")
    }
    override fun toString(): String {
        return value.toString()
    }
}


class JSONString(name: String, parentObject: CompositeJSON?, override val value : String) : LeafJSON(name, parentObject)

class JSONNumber(name: String, parentObject: CompositeJSON?, override val value : Number<*>) : LeafJSON(name, parentObject)

class JSONBoolean(name: String, parentObject: CompositeJSON?, override val value : Boolean) : LeafJSON(name, parentObject)

class JSONNull(name: String, parentObject: CompositeJSON?) : LeafJSON(name, parentObject)

fun main(){
    val c=ObjectJSON()
    val number = JSONNumber("NUMERO",c,Number(1))
    val array = ArrayJSON("ARRAY",c)
    val arrayEle = JSONBoolean("b1",array,true)
    val arrayEle1 = JSONBoolean("b2",array,false)
    val c3Array = ArrayJSON("c3Array",c)
    val c3 = ObjectJSON("c3",c3Array)
    val array1 = JSONNumber("numero",c3,Number(101101))
    val array2 = JSONString("nome",c3,"Dave Farley")
    val array3 = JSONBoolean("internacional",c3,true)

    val c1 = ObjectJSON("c1",c)
    val jsonNull = JSONNull("null",c1)
    val jsonStr = JSONString("str",c1,"ola")
    println(c)
    println(c)
}
