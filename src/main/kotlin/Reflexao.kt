import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.*

interface DataClass

fun DataClass.toJSON(parent: CompositeJSON?=null, name: String?=null) : ObjectJSON {
    fun aux(array: Array<*>, arrayName : String, arrayParent : CompositeJSON): ArrayJSON {
        val arrayJSON = ArrayJSON(arrayParent,arrayName)
        array.forEach {
            when(it) {
                is Number -> JSONNumber(arrayJSON,it)
                is Boolean -> JSONBoolean(arrayJSON,it)
                is String -> JSONString(arrayJSON,it)
                is Char -> JSONString(arrayJSON,it.toString())
                is Enum<*> -> JSONString(arrayJSON,it.toString())
                is DataClass -> it.toJSON(arrayJSON)
                is Array<*> -> aux(it, arrayName,arrayJSON)
                null -> JSONNull(parent)
            }
        }
        return arrayJSON
    }

    val obj : ObjectJSON =
        if (parent == null) ObjectJSON()
        else ObjectJSON(parent,name)

    val clazz = this::class
    println("Size: ${clazz.declaredMemberProperties.size}")
    clazz.declaredMemberProperties.forEach{
        println("${it.name} " + (it.returnType.classifier is DataClass))
        when(it.returnType.classifier) {
            Int::class -> JSONNumber(obj,it.call(this) as Number,it.name)
            Double::class -> JSONNumber(obj,it.call(this) as Number,it.name)
            Float::class -> JSONNumber(obj,it.call(this) as Number,it.name)
            Long::class -> JSONNumber(obj,it.call(this) as Number,it.name)
            Short::class -> JSONNumber(obj,it.call(this) as Number,it.name)
            Boolean::class -> JSONBoolean(obj,it.call(this) as Boolean,it.name)
            String::class -> JSONString(obj,it.call(this) as String,it.name)
            Char::class -> JSONString(obj,it.call(this) as String,it.name)
            Array::class -> aux((it.call(this) as Array<*>),it.name,obj)
            is KClass<*> -> {
                val klass = it.returnType.classifier as KClass<*>
                if (DataClass::class.java.isAssignableFrom(klass.java)) {
                    (it.call(this) as DataClass).toJSON(obj, it.name)
                }
            }
            //is DataClass -> (it.call(this) as DataClass).toJSON(obj,it.name)
            Enum::class -> JSONString(obj,it.call(this).toString(),it.name)
            null -> JSONNull(obj,it.name)
        }
    }
    return obj
}



//fun Collection<*>.toJSON() : JSONELEMENT {
//
//}
//
//fun Map<*,*>.toJSON() : JSONELEMENT {
//
//}
//
//fun Char.toJSON() : JSONELEMENT {
//
//}
//fun Byte.toJSON() : JSONELEMENT {
//
//}
//fun String.toJSON() : JSONELEMENT {
//
//}
//fun Boolean.toJSON() : JSONELEMENT {
//
//}
//fun Number.toJSON() : JSONELEMENT {
//
//}
//
//fun Enum<*>.toJSON() : JSONELEMENT {
//
//}
