import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.full.*

@Target(AnnotationTarget.PROPERTY)
annotation class Name(val id: String)

@Target(AnnotationTarget.PROPERTY)
annotation class ToJsonString

@Target(AnnotationTarget.PROPERTY)
annotation class ExcludeFromJson

fun isEnum(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Enum::class)
fun isNumber(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Number::class)

//fun isCollection(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Collection::class)
fun isIterable(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Iterable::class)
fun isMap(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Map::class)

fun convertMapToObjectJSON(map: Map<*, *>, mapParent: CompositeJSON, mapName: String): ObjectJSON {
    val objectJSON = ObjectJSON(mapParent, mapName)
    map.forEach {
        val name = it.key as String
        when (it.value) {
            is String -> JSONString(value = it.value as String, parent = objectJSON, name = name)
            is Char -> JSONString(value = (it.value as Char).toString(), parent = objectJSON, name = name)
            is Boolean -> JSONBoolean(value = it.value as Boolean, parent = objectJSON, name = name)
            is Number -> JSONNumber(value = it.value as Number, parent = objectJSON, name = name)
            is Enum<*> -> getEnumProperties(objectJSON, it.value as Enum<*>, name)
            null -> JSONNull(parent = objectJSON, name = name)
            is Iterable<*> -> convertIterableToArrayJSON(it.value as Collection<*>,objectJSON, name)
            is Map<*, *> -> convertMapToObjectJSON(it.value as Map<*, *>, objectJSON, name)
            else -> (it.value as KClass<*>).toJSON(objectJSON, name)
        }
    }
    return objectJSON
}

fun getEnumProperties(obj: ObjectJSON, enum: Enum<*>, name: String) {
    val objectJSON = ObjectJSON(obj, name)
    val enumClass = enum.javaClass.declaredFields
        .filter { it.type.isPrimitive || it.type == String::class.java }
        .map { it.isAccessible = true; it.name to it.get(enum) }
    for ((name, value) in enumClass) {
        when (value) {
            is String -> JSONString(value = value, parent = objectJSON, name = name)
            is Char -> JSONString(value = value.toString(), parent = objectJSON, name = name)
            is Boolean -> JSONBoolean(value = value, parent = objectJSON, name = name)
            is Number -> JSONNumber(value = value, parent = objectJSON, name = name)

        }
    }
}

fun convertIterableToArrayJSON(collection: Iterable<*>, arrayParent: CompositeJSON, arrayName: String): ArrayJSON {
    val arrayJSON = ArrayJSON(arrayParent, arrayName)
    collection.forEach {
        when (it) {
            is String -> JSONString(value = it, parent = arrayJSON)
            is Char -> JSONString(value = it.toString(), parent = arrayJSON)
            is Boolean -> JSONBoolean(value = it, parent = arrayJSON)
            is Number -> JSONNumber(value = it, parent = arrayJSON)
            is Enum<*> -> JSONString(value = it.toString(), parent = arrayJSON)
            null -> JSONNull(parent = arrayJSON)
            is Collection<*> -> convertIterableToArrayJSON(it, arrayJSON, arrayName)
            is Map<*, *> -> convertMapToObjectJSON(it,arrayJSON,arrayName)
            else -> (it as KClass<*>).toJSON(arrayJSON)
        }
    }
    return arrayJSON
}

fun Any.toJSON(parent: CompositeJSON? = null, name: String? = null): ObjectJSON {
    val clazz = this::class
    if (!clazz.isData) throw IllegalArgumentException("This function can only be used with object class.")

    val obj: ObjectJSON =
        if (parent == null) ObjectJSON()
        else ObjectJSON(parent, name)

    clazz.declaredMemberProperties.forEach {
        if (!it.hasAnnotation<ExcludeFromJson>()) {
            val callThis = it.call(this)
            var itName = it.name
            if (it.hasAnnotation<Name>()) {
                val ann = it.findAnnotation<Name>()!!
                itName = ann.id
            }
            if (it.hasAnnotation<ToJsonString>()) {
                println(itName)
                JSONString(value = callThis.toString(), parent = obj, name = itName)
            } else {
                val classifier = it.returnType.classifier!!
//        println("Name: ${it.name} Class: ${it.returnType.classifier} Boolean: ${Boolean::class}")
                when {
                    classifier == String::class -> JSONString(value = callThis as String, parent = obj, name = itName)
                    classifier == Char::class -> JSONString(value = (callThis as Char).toString(), parent = obj, name = itName)
                    classifier == Boolean::class -> JSONBoolean(value = callThis as Boolean, parent = obj, name = itName)
                    isNumber(classifier) -> JSONNumber(value = callThis as Number, parent = obj, name = itName)
                    isEnum(classifier) -> getEnumProperties(obj, callThis as Enum<*>,itName)
                    callThis == null -> JSONNull(parent = obj, name = itName)
                    isIterable(classifier) -> convertIterableToArrayJSON(callThis as Collection<*>, obj, itName)
                    isMap(classifier) -> convertMapToObjectJSON(callThis as Map<*, *>, obj, itName)
                    (classifier as KClass<*>).isData -> callThis.toJSON(obj, itName)
                }
            }
        }

    }
    return obj
}

