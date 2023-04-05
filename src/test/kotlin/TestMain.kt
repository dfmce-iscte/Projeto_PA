import kotlin.test.Test

class TestMain {

    @Test
    fun `test object creation` () {
        val rootObject = ObjectJSON()

        JSONNumber("number",rootObject,Number(1))

        val array = ArrayJSON("array",rootObject)
        JSONBoolean("b1",array,true)
        JSONBoolean("b2",array,false)

        val arr2 = ArrayJSON("arr2",rootObject)
        val arrayObject = ObjectJSON("arrayObject",arr2)
        JSONNumber("numero",arrayObject,Number(101101))
        JSONString("nome",arrayObject,"Dave Farley")
        JSONBoolean("internacional",arrayObject,true)

        val arrayObject2 = ObjectJSON("arrayObject2",arr2)
        JSONNumber("numero",arrayObject2,Number(101102))
        JSONString("nome",arrayObject2,"Martin Fowler")
        JSONBoolean("internacional",arrayObject2,true)

        JSONNull("null",rootObject)
        println(rootObject)
    }
}