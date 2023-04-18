import kotlin.test.Test
import kotlin.test.assertEquals

data class Point(val x: Int, val y: Int) {
    fun moveDown() = Point(x, y + 1)
    fun moveRight() = Point(x + 1, y)
    fun sum(x: Int, y: Int) = Point(this.x + x, this.y + y)
}

data class Person(val name: String, val age: Int)

data class Mix(val name:String="")  {
    val list = arrayListOf(1,2,3,4)
    val listChar = arrayListOf('c','f','g')
    val nullProperty = null
    val person = Person("ZE",7)
    val color = Color.BLUE
    val number = 0
    val decimal = 15.56
    val char='c'
    val string="STRING"
    val bool=true
    val hasMap= hashMapOf("foo" to 1, "bar" to 2)
    var set= setOf(1, 2, 3, 2, 1)

}

data class MixExclude(val name:String="")  {
    val list = arrayListOf(1,2,3,4)
    val listChar = arrayListOf('c','f','g')
    val nullProperty = null
    val person = Person("ZE",7)
    val color = Color.BLUE
    val number = 0
    val decimal = 15.56
    val char='c'
    @ExcludeFromJson
    val string="STRING"
    val bool=true
    val hasMap= hashMapOf("foo" to 1, "bar" to 2)
    var set= setOf(1, 2, 3, 2, 1)

}

data class MixToJsonString(val name:String="")  {
    val list = arrayListOf(1,2,3,4)
    val listChar = arrayListOf('c','f','g')
    val nullProperty = null
    val person = Person("ZE",7)
    val color = Color.BLUE
    val number = 0

    val decimal = 15.56
    val char='c'
    val string="STRING"
    val bool=true
    val hasMap= hashMapOf("foo" to 1, "bar" to 2)
    var set= setOf(1, 2, 3, 2, 1)

}

enum class Color(val rgb: Int, val hex: String) {
    RED(0xFF0000, "#FF0000"),
    GREEN(0x00FF00, "#00FF00"),
    BLUE(0x0000FF, "#0000FF")
}

class TestReflexao {


    @Test
    fun testReflexaoDataClass() {
        val obj = Point(3, 2)
        val p=Person("ZE",7)
        //obj.toJSON()
        val json = obj.toJSON()
        val json1=p.toJSON()
        println(json1)
       // println(json.properties)
        println(json.toString())

    }

    @Test
    fun testReflexaoMix() {
        val obj = Mix("Mix")
        println(obj.toJSON())
    }

    @Test
    fun testReflexaoExcludeFromJson() {
        val obj = MixExclude("Mix")
        val json=obj.toJSON()
        assertEquals(json.properties["string"],null)
    }
}