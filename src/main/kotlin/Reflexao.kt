import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.full.*

@Target(AnnotationTarget.PROPERTY)
annotation class Name(val id: String)

@Target(AnnotationTarget.PROPERTY)
annotation class ToJsonString

@Target(AnnotationTarget.PROPERTY)
annotation class ExcludeFromJson

private fun isEnum(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Enum::class)
private fun isNumber(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Number::class)
private fun isIterable(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Iterable::class)
private fun isMap(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Map::class)

private fun convertMapToObjectJSON(
    map: Map<*, *>,
    mapParent: CompositeJSON? = null,
    mapName: String? = null
): ObjectJSON {
    val objectJSON = createObjectJSON(mapParent, mapName)
    map.forEach {
        val name = it.key as String
        when (it.value) {
            is String -> JSONString(value = it.value as String, parent = objectJSON, name = name)
            is Char -> JSONString(value = (it.value as Char).toString(), parent = objectJSON, name = name)
            is Boolean -> JSONBoolean(value = it.value as Boolean, parent = objectJSON, name = name)
            is Number -> JSONNumber(value = it.value as Number, parent = objectJSON, name = name)
            is Enum<*> -> JSONString(value = it.toString(), parent = objectJSON, name = name)
            null -> JSONNull(parent = objectJSON, name = name)
            is Iterable<*> -> convertIterableToArrayJSON(it.value as Collection<*>, objectJSON, name)
            is Map<*, *> -> convertMapToObjectJSON(it.value as Map<*, *>, objectJSON, name)
            else -> toJSONObject(value = it.value!!, parent = objectJSON, name = name)
        }
    }
    return objectJSON
}

private fun convertIterableToArrayJSON(
    collection: Iterable<*>,
    arrayParent: CompositeJSON? = null,
    arrayName: String? = null
): ArrayJSON {
    val arrayJSON =
        when (arrayParent) {
            is ObjectJSON -> ArrayJSON(arrayParent, arrayName!!)
            is ArrayJSON -> ArrayJSON(arrayParent)
            else -> ArrayJSON()
        }
    collection.forEach {
        when (it) {
            is String -> JSONString(value = it, parent = arrayJSON)
            is Char -> JSONString(value = it.toString(), parent = arrayJSON)
            is Boolean -> JSONBoolean(value = it, parent = arrayJSON)
            is Number -> JSONNumber(value = it, parent = arrayJSON)
            is Enum<*> -> JSONString(value = it.toString(), parent = arrayJSON)
            null -> JSONNull(parent = arrayJSON)
            is Collection<*> -> convertIterableToArrayJSON(it, arrayJSON)
            is Map<*, *> -> convertMapToObjectJSON(it, arrayJSON)
            else -> toJSONObject(value = it, parent = arrayJSON)
        }
    }
    return arrayJSON
}


private fun toJSONObject(value: Any, parent: CompositeJSON? = null, name: String? = null): ObjectJSON {
    val clazz = value::class
    if (parent is ObjectJSON && name == null)
        throw IllegalArgumentException("The name of the object must be informed when the parent is a ObjectJSON.")

    val obj = createObjectJSON(parent, name)

    clazz.declaredMemberProperties.forEach {
        if (!it.hasAnnotation<ExcludeFromJson>()) {
            val callThis = it.call(value)
            val itName = checkIfHasAnnotationName(it)

            if (it.hasAnnotation<ToJsonString>()) {
                JSONString(value = callThis.toString(), parent = obj, name = itName)
            } else {
                val classifier = it.returnType.classifier!!
                when {
                    classifier == String::class -> JSONString(value = callThis as String, parent = obj, name = itName)
                    classifier == Char::class -> JSONString(value = (callThis as Char).toString(), parent = obj, name = itName)
                    classifier == Boolean::class -> JSONBoolean(value = callThis as Boolean,parent = obj,name = itName)
                    isNumber(classifier) -> JSONNumber(value = callThis as Number, parent = obj, name = itName)
                    isEnum(classifier) -> JSONString(value = callThis.toString(), parent = obj, name = itName)
                    callThis == null -> JSONNull(parent = obj, name = itName)
                    isIterable(classifier) -> convertIterableToArrayJSON(callThis as Collection<*>, obj, itName)
                    isMap(classifier) -> convertMapToObjectJSON(callThis as Map<*, *>, obj, itName)
                    (classifier as KClass<*>).isData -> toJSONObject(callThis, obj, itName)
                }
            }
        }

    }
    return obj
}

private fun checkIfHasAnnotationName(it: KProperty<*>): String =
    if (it.hasAnnotation<Name>()) it.findAnnotation<Name>()!!.id
    else it.name

private fun createObjectJSON(parent: CompositeJSON? = null, name: String? = null): ObjectJSON =
    when (parent) {
        is ObjectJSON -> ObjectJSON(parent, name!!)
        is ArrayJSON -> ObjectJSON(parent)
        else -> ObjectJSON()
    }

fun Any.toJSON(parent : CompositeJSON? = null, name : String? = null): JsonElement {
    if (parent is ObjectJSON && name == null)
        throw IllegalArgumentException("The name of the object must be informed when the parent is a ObjectJSON.")
    return if (parent is ObjectJSON && name != null) {
        when (this) {
            is String -> JSONString(value = this, parent = parent, name = name)
            is Char -> JSONString(value = this.toString(), parent = parent, name = name)
            is Boolean -> JSONBoolean(value = this, parent = parent, name = name)
            is Number -> JSONNumber(value = this, parent = parent, name = name)
            is Enum<*> -> JSONString(value = this.toString(), parent = parent, name = name)
            is Collection<*> -> convertIterableToArrayJSON(this, arrayParent = parent, arrayName = name)
            is Map<*, *> -> convertMapToObjectJSON(this, mapParent = parent, mapName = name)
            else -> toJSONObject(this, name = name)
        }
    } else {
        when (this) {
            is String -> JSONString(value = this, parent = parent)
            is Char -> JSONString(value = this.toString(), parent = parent)
            is Boolean -> JSONBoolean(value = this, parent = parent)
            is Number -> JSONNumber(value = this, parent = parent)
            is Enum<*> -> JSONString(value = this.toString(), parent = parent)
            is Collection<*> -> convertIterableToArrayJSON(this)
            is Map<*, *> -> convertMapToObjectJSON(this)
            else -> toJSONObject(this)
        }
    }

}



