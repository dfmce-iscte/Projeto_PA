import kotlin.test.Test
import kotlin.test.assertEquals

class TestMain {

    val rootObject = ObjectJSON()
    val n = JSONNumber(1, rootObject, "number")
    val t1=JSONBoolean(true, rootObject, "boolean")
    val testNum = ObjectJSON(rootObject, "teste")
    val numbers = JSONNumber( 5,testNum, "number")
    val array = ArrayJSON(rootObject, "array")
    val b1 = JSONBoolean(true, array)
    val b2 = JSONBoolean(false, array)
    val arr2 = ArrayJSON(rootObject, "arr2")
    val arrayObject = ObjectJSON(arr2)
    val n1 = JSONNumber( 101101,arrayObject, "numero")
    val no1 = JSONString( "Dave Farley",arrayObject, "nome")
    val i1 = JSONBoolean( true, arrayObject,"internacional")
    val arrayObject2 = ObjectJSON(arr2)
    val n2 = JSONNumber(101102,arrayObject2, "numero")
    val no2 = JSONString( "Martin Fowler", arrayObject2,"nome")
    val i2 = JSONBoolean( true, arrayObject2, "internacional")
    val nul = JSONNull(rootObject, "null")

    @Test
    fun `test object creation`() {
        println(rootObject)
    }


    @Test
    fun testCheckStructure() {
        val cs = CheckStructure("nome", String::class)
        rootObject.accept(cs)
        assertEquals(expected = true, cs.valid)

        val cs_numero = CheckStructure("numero", Int::class)
        rootObject.accept(cs_numero)
        assertEquals(expected = true, cs_numero.valid)
    }

    @Test
    fun testCheckArrayStructure() {
        val cas = CheckArrayStructure("arr2", arrayObject)
        rootObject.accept(cas)
        assertEquals(expected = true, cas.valid)
    }

    @Test
    fun testSearchForArray() {
        val searchfor = SearchForArray("number")
        rootObject.accept(searchfor)
        assertEquals(2, searchfor.list.size)
    }

    @Test
    fun testSearchForObject() {
        val searchfor = SearchForObject("numero", "nome")
        rootObject.accept(searchfor)
        assertEquals(2, searchfor.list.size)
    }
}