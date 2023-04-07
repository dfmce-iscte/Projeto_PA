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