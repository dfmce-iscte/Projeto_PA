class JsonValues(private val values: ObjectJSON){

    private val observers: MutableList<JsonElementObserver> = mutableListOf()

    fun addObserver(observer: JsonElementObserver) = observers.add(observer)

    fun removeObserver(observer: JsonElementObserver) = observers.remove(observer)
    override fun toString(): String = "$values"

    fun updateJSON () {
        observers.forEach { it.updateJSON() }
    }

}

interface JsonValuesObserver {
//    fun jsonElementAdded(pair: JsonElement) {}
//
//    fun jsonElementRemoved(pair: JsonElement) {}
//
//    fun jsonElementReplaced(pairOld: JsonElement, pairNew: JsonElement) {}
    fun updateJSON()


}