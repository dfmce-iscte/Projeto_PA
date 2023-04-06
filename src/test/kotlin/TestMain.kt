import kotlin.test.Test
import kotlin.test.assertEquals

class TestMain {

    val rootObject = ObjectJSON()
    val n = JSONNumber(rootObject,1,"number")
    val array = ArrayJSON("array",rootObject)
    val b1 = JSONBoolean(array,true)
    val b2 = JSONBoolean(array,false)
    val arr2 = ArrayJSON("arr2",rootObject)
    val arrayObject = ObjectJSON("arrayObject",arr2)
    val n1 = JSONNumber(arrayObject,101101,"numero")
    val no1 = JSONString(arrayObject,"Dave Farley","nome")
    val i1 = JSONBoolean(arrayObject,true,"internacional")
    val arrayObject2 = ObjectJSON("arrayObject2",arr2)
    val n2 = JSONNumber(arrayObject2,101102.1,"numero")
    val no2 = JSONString(arrayObject2,"Martin Fowler","nome")
    val i2 = JSONBoolean(arrayObject2,true,"internacional")
    val nul = JSONNull(rootObject,"null")

    @Test
    fun `test object creation` () {
        println(rootObject)
    }


    @Test
    fun testCheckStructure() {
        val cs = CheckStructure("nome",String::class)
        rootObject.accept(cs)
        assertEquals(expected = true, cs.valid)

        val cs_numero = CheckStructure("numero",Int::class)
        rootObject.accept(cs_numero)
        assertEquals(expected = false, cs_numero.valid)
    }

    @Test
    fun testCheckArrayStructure() {
        val cas = CheckArrayStructure("arr2",arrayObject)
        rootObject.accept(cas)
        assertEquals(expected = true, cas.valid)
    }
}