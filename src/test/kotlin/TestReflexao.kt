import kotlin.test.*

data class Point(val x: Int, val y: Int) {
    fun moveDown() = Point(x, y + 1)
    fun moveRight() = Point(x + 1, y)
    fun sum(x: Int, y: Int) = Point(this.x + x, this.y + y)
}

data class Person(val name: String, val age: Int)

data class Mix(val name: String = "") {
    val list = arrayListOf(1, 2, 3, 4)
    val listChar = arrayListOf('c', 'f', 'g')
    val nullProperty = null
    val person = Person("ZE", 7)
    val color = Color.BLUE
    val number = 0
    val decimal = 15.56
    val char = 'c'
    val string = "STRING"
    val bool = true
    val hasMap = hashMapOf("foo" to 1, "bar" to 2)
    var set = setOf(1, 2, 3, 2, 1)

}

data class MixExclude(val name: String = "") {
    val list = arrayListOf(1, 2, 3, 4)
    val listChar = arrayListOf('c', 'f', 'g')
    val nullProperty = null
    val person = Person("ZE", 7)
    val color = Color.BLUE
    val number = 0
    val decimal = 15.56
    val char = 'c'

    @ExcludeFromJson
    val string = "STRING"
    val bool = true
    val hasMap = hashMapOf("foo" to 1, "bar" to 2)
    var set = setOf(1, 2, 3, 2, 1)

}

data class MixToJsonString(val name: String = "") {
    @ToJsonString
    val list = arrayListOf(1, 2, 3, 4)
    val listChar = arrayListOf('c', 'f', 'g')
    val nullProperty = null
    val person = Person("ZE", 7)
    val color = Color.BLUE
    val number = 0

    @ToJsonString
    val decimal = 15.56
    val char = 'c'
    val string = "STRING"
    val bool = true
    val hasMap = hashMapOf("foo" to 1, "bar" to 2)
    var set = setOf(1, 2, 3, 2, 1)

}

data class MixChangeName(val name: String = "") {
    @Name("novonome")
    val list = arrayListOf(1, 2, 3, 4)
    val listChar = arrayListOf('c', 'f', 'g')
    val nullProperty = null
    val person = Person("ZE", 7)
    val color = Color.BLUE
    val number = 0

    @Name("fixe")
    val decimal = 15.56
    val char = 'c'
    val string = "STRING"
    val bool = true
    val hasMap = hashMapOf("foo" to 1, "bar" to 2)
    var set = setOf(1, 2, 3, 2, 1)

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
        val p = Person("ZE", 7)
        //obj.toJSON()
        val json = obj.toJSON()
        val json1 = p.toJSON()
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
        val json = obj.toJSON()
        println(json)
        assertEquals(json.properties["string"], null)
    }

    @Test
    fun testReflexaoToJsonString() {
//        val x=ObjectJSON()
//        val z=JSONNumber(x,14,"nome")
//        println(x.properties["nome"].)
        val obj = MixToJsonString("Mix")
        val json = obj.toJSON()
        assertEquals(json.properties["decimal"]!!::class.simpleName,"JSONString")
        println(json)
    }
    //VER QUANDO A TAG ESTÁ PARA LISTAS OU HASHMAPS

    @Test
    fun testReflexaoChangeName() {
        val obj = MixChangeName("Mix")
        val json = obj.toJSON()
        assertEquals(json.properties["decimal"], null)
        assertTrue("fixe" in json.properties)
        assertFalse("decimal" in json.properties)
        println(json)


    }
}