import kotlin.reflect.KClass

class SearchForArray(private val property: String) : Visitor {
    var list = mutableListOf<String>()

    override fun visit(l: LeafJSON) {

    }

    override fun visit(c: ObjectJSON): Boolean {
        c.properties[property]?.let { list.add(it.toString()) }
        //  var x = c.properties.filter { it.key == property }
        //  if (x.isNotEmpty()) {
        //      list.add(x[property].toString())
        // }
        return true
    }
}


class SearchForObject(private val property1: String, private val property2: String) : Visitor {
    var list = mutableListOf<ObjectJSON>()

    override fun visit(l: LeafJSON) {

    }

    override fun visit(c: ObjectJSON): Boolean {
        if (c.properties.contains(property1) && c.properties.contains(property2)) {
            list.add(c)
        }
        return true
    }
}


class CheckStructure(val property: String, val type: KClass<*>) : Visitor {
    var valid = true

    override fun visit(l: LeafJSON) {

    }

    override fun visit(c: ObjectJSON): Boolean {
        if (!valid)
            return false
        if (c.properties[property] != null) {
            val leaf = (c.properties[property]!! as LeafJSON)
            if (leaf.value!!::class != type) {
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

    override fun visit(l: LeafJSON) {

    }

    override fun visit(c: ObjectJSON): Boolean {
        if (!valid)
            return false
        if (allProperties.contains(c.parent)) {
            for (p in type.properties)
                if (c.properties[p.key] == null || c.properties[p.key]!!::class != p.value::class) {
                    valid = false
                    return false
                }
        } else if (c.properties[array] != null) {
            if (c.properties[array]!!::class != ArrayJSON::class) {
                valid = false
                return false
            }
            val array = (c.properties[array]!! as ArrayJSON)
            allProperties.add(array)
        }
        return true
    }
}