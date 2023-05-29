import kotlin.reflect.KClass

class SearchForArray(private val property: String) : Visitor {
    var list = mutableListOf<String>()

    override fun visit(c: ObjectJSON): Boolean {
        c.getProperties()[property]?.let { list.add(it.toString()) }
        //  var x = c.properties.filter { it.key == property }
        //  if (x.isNotEmpty()) {
        //      list.add(x[property].toString())
        // }
        return true
    }

}


class SearchForObject(private val property1: String, private val property2: String) : Visitor {
    var list = mutableListOf<ObjectJSON>()


    override fun visit(c: ObjectJSON): Boolean {
        if (c.getProperties().contains(property1) && c.getProperties().contains(property2)) {
            list.add(c)
        }
        return true
    }
}


class CheckStructure(val property: String, val type: KClass<*>) : Visitor {
    var valid = true

    override fun visit(c: ObjectJSON): Boolean {

        if (!valid)
            return false
        if (c.getProperties()[property] != null) {
            val element = (c.getProperties()[property]!!)
            var value: Any? = null

            when (element) {
                is JSONString -> value = element.getValue
                is JSONNumber -> value = element.getValue
                is JSONBoolean -> value = element.getValue
                else -> JSONNull()
            }

            if (value!!::class != type) {
                valid = false
                return false
            }
        }
        return true
    }
}


class CheckArrayStructure(val array: String, val type: ObjectJSON) : Visitor {
    var valid = true
    val allProperties = mutableListOf<ArrayJSON>()

    override fun visit(c: ObjectJSON): Boolean {
        if (!valid)
            return false
        if (allProperties.contains(c.parent)) {
            for (p in type.getProperties())
                if (c.getProperties()[p.key] == null || c.getProperties()[p.key]!!::class != p.value::class) {
                    valid = false
                    return false
                }
        } else if (c.getProperties()[array] != null) {
            if (c.getProperties()[array]!!::class != ArrayJSON::class) {
                valid = false
                return false
            }
            val array = (c.getProperties()[array]!! as ArrayJSON)
            allProperties.add(array)
        }
        return true
    }
}