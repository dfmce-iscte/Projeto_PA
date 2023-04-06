import kotlin.reflect.KClass

class CheckStructure(val property : String, val type : KClass<*>) : Visitor {
    var valid = true

    override fun visit(l: LeafJSON) {

    }

    override fun visit(c: ObjectJSON) : Boolean {
        if (!valid)
            return false
        if (c.properties[property] != null ) {
            val leaf = (c.properties[property]!! as LeafJSON)
            if (leaf.value!!::class != type) {
                valid = false
                return false
            }
        }
        return true
    }
}


class CheckArrayStructure(val property : String, val type : ObjectJSON) : Visitor {
    var valid = true
    val allProperties = mutableListOf<ArrayJSON>()

    override fun visit(l: LeafJSON) {

    }

    override fun visit(c: ObjectJSON) : Boolean {
        if (!valid)
            return false
        if (c.parent is ArrayJSON && allProperties.contains(c.parent)) {
            for (p in type.properties)
                if (c.properties[p.key] == null || c.properties[p.key]!!::class != p.value::class) {
                    valid = false
                    return false
                }
        }
        else if (c.properties[property] != null ) {
            val array = (c.properties[property]!! as ArrayJSON)
            allProperties.add(array)
        }
        return true
    }
}