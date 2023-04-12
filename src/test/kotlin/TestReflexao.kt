import kotlin.test.Test

 data class Point(val x: Int, val y: Int) : DataClass {
    fun moveDown() = Point(x, y + 1)
    fun moveRight() = Point(x + 1, y)
    fun sum(x: Int, y: Int) = Point(this.x + x, this.y + y)
}

data class Person(val name: String, val age: Int) : DataClass

data class Mix(val name:String="") : DataClass {
    val list = arrayListOf(1,2,3,4)
    val nullProperty = null
    val person = Person("ZE",7)
    val color = Color.BLUE
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
        //obj.toJSON()
        val json = obj.toJSON()
        println(json)

    }
}