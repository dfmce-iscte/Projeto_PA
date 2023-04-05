import kotlin.test.Test

class TestMain {

    @Test
    fun `test object creation` () {
        val rootObject = ObjectJSON()

        //faz-se JSONNumber(rootObject,1) ou rootObject.addElement(JSONNumber(1))???
        JSONNumber(rootObject,1,"number")

        val array = ArrayJSON("array",rootObject)
        JSONBoolean(array,true)
        JSONBoolean(array,false)

        val arr2 = ArrayJSON("arr2",rootObject)
        val arrayObject = ObjectJSON("arrayObject",arr2)
        JSONNumber(arrayObject,101101,"numero")
        JSONString(arrayObject,"Dave Farley","nome")
        JSONBoolean(arrayObject,true,"internacional")

        val arrayObject2 = ObjectJSON("arrayObject2",arr2)
        JSONNumber(arrayObject2,101102,"numero")
        JSONString(arrayObject2,"Martin Fowler","nome")
        JSONBoolean(arrayObject2,true,"internacional")

        JSONNull(rootObject,"null")
        println(rootObject)
    }
}