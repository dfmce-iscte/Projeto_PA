class JsonValues(private val values: JsonElement) {

    private val observers: MutableList<JsonElementSetObserver> = mutableListOf()

    fun addObserver(observer: JsonElementSetObserver) = observers.add(observer)

    fun removeObserver(observer: JsonElementSetObserver) = observers.remove(observer)
//    override fun iterator(): Iterator<JsonElement> {
//       return data.iterator()
//    }
    override fun toString(): String = "$values"

}

interface JsonElementSetObserver {
    fun JsonElementAdded(pair: JsonElement) {}

    fun JsonElementRemoved(pair: JsonElement) {}

    fun JsonElementReplaced(pairOld: JsonElement, pairNew: JsonElement) {}


}