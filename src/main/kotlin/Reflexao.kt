import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.full.*

@Target(AnnotationTarget.PROPERTY)
annotation class Name(val id:String)

@Target(AnnotationTarget.PROPERTY)
annotation class ToJsonString
@Target(AnnotationTarget.PROPERTY)
annotation class ExcludeFromJson

fun isEnum(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Enum::class)
fun isNumber(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Number::class)
fun isCollection(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Collection::class)
fun isMap(obj: KClassifier) = obj is KClass<*> && obj.isSubclassOf(Map::class)

fun convertMapToObjectJSON (map: Map<*,*>, mapName: String, mapParent : CompositeJSON):ObjectJSON {
    val objectJSON = ObjectJSON(mapParent,mapName)
    map.forEach {
        val name = it.key as String
        when(it.value) {
            is String -> JSONString(objectJSON,it.value as String,name)
            is Char -> JSONString(objectJSON,(it.value as Char).toString(),name)
            is Boolean -> JSONBoolean(objectJSON,it.value as Boolean,name)
            is Number -> JSONNumber(objectJSON,it.value as Number,name)
            is Enum<*> -> JSONString(objectJSON,it.toString(),name)
            null -> JSONNull(objectJSON)
            is Collection<*> -> convertCollectionToArrayJSON(it.value as Collection<*>,name,objectJSON)
            is Map<*,*> -> convertMapToObjectJSON(it.value as Map<*,*>,name,objectJSON)
            else -> (it.value as KClass<*>).toJSON(objectJSON,name)
        }
    }
    return objectJSON
}

fun convertCollectionToArrayJSON(collection: Collection<*>, arrayName : String, arrayParent : CompositeJSON): ArrayJSON {
    val arrayJSON = ArrayJSON(arrayParent,arrayName)
    collection.forEach {
        when(it) {
            is String -> JSONString(arrayJSON,it)
            is Char -> JSONString(arrayJSON,it.toString())
            is Boolean -> JSONBoolean(arrayJSON,it)
            is Number -> JSONNumber(arrayJSON,it)
            is Enum<*> -> JSONString(arrayJSON,it.toString())
            null -> JSONNull(arrayJSON)
            is Collection<*> -> convertCollectionToArrayJSON(it, arrayName,arrayJSON)
            is Map<*,*> -> convertMapToObjectJSON(it,arrayName,arrayJSON)
            else -> (it as KClass<*>).toJSON(arrayJSON)
        }
    }
    return arrayJSON
}

fun Any.toJSON(parent: CompositeJSON?=null, name: String?=null) : ObjectJSON {
    val obj : ObjectJSON =
        if (parent == null) ObjectJSON()
        else ObjectJSON(parent,name)

    val clazz = this::class
    clazz.declaredMemberProperties.forEach{
        val callThis = it.call(this)
        val classifier = it.returnType.classifier!!
//        println("Name: ${it.name} Class: ${it.returnType.classifier} Boolean: ${Boolean::class}")
        when {
            classifier == String::class -> JSONString(obj, callThis as String, it.name)
            classifier == Char::class -> JSONString(obj, (callThis as Char).toString(), it.name)
            classifier == Boolean::class -> JSONBoolean(obj, callThis as Boolean, it.name)
            isNumber(classifier) -> JSONNumber(obj, callThis as Number, it.name)
            isEnum(classifier) -> JSONString(obj, callThis.toString(), it.name)
            callThis == null -> JSONNull(obj,it.name)
            isCollection(classifier) -> convertCollectionToArrayJSON(callThis as Collection<*>, it.name, obj)
            isMap(classifier) -> convertMapToObjectJSON(callThis as Map<*,*>, it.name, obj)
            (classifier as KClass<*>).isData -> callThis.toJSON(obj, it.name)
        }
    }
    return obj
}

