class JsonValues(vararg values: JsonElement):Iterable<JsonElement>{

    private val data = values.toMutableSet()
    private val observers: MutableList<JsonElementSetObserver> = mutableListOf()

    fun addObserver(observer: JsonElementSetObserver) = observers.add(observer)

    fun removeObserver(observer: JsonElementSetObserver) = observers.remove(observer)
    override fun iterator(): Iterator<JsonElement> {
       return data.iterator()
    }
    override fun toString(): String {
        return data.joinToString(separator = "    ") { it.toString() }
    }

}

interface JsonElementSetObserver {
    fun JsonElementAdded(pair: JsonElement) {}

    fun JsonElementRemoved(pair: JsonElement) {}

    fun JsonElementReplaced(pairOld: JsonElement, pairNew: JsonElement) {}


}