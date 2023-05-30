#Projeto PA MEI
93028 Diogo Cosme
92399 Pedro d'Oliveira

O principal objetivo deste biblioteca consiste na criação de objetos JSON a partir de data class.
Utilizando como exemplo a seguinte class:

    data class Mix(val name: String = "") {
        val list = arrayListOf(1, 2, 3, 4)
        val listChar = arrayListOf('c', 'f', 'g')
        val number = 0
    }
Ao executar o comando Mix("TESTE").toJSON() esta irá ser convertida para um objeto JSON com o seguinte formato:

    {
	    "list":[
		    1,
		    2,
		    3,
		    4
	    ],
	    "listChar":[
		    "c",
		    "f",
		    "g"
	    ],
	    "name":"TESTE",
	    "number":0
    }

Se o utilizador desejar, pode utilizar as seguintes notações: @ExcludeFromJson,@ToJsonString,@Name(id)
@ExcludeFromJson ao ser colocada, não permite adicionar a respetiva propriedade ao objeto JSON:

    data class Mix(val name: String = "") {
        val list = arrayListOf(1, 2, 3, 4)
        @ExcludeFromJson
        val listChar = arrayListOf('c', 'f', 'g')
        val number = 0
    }
O resultado final será:

    {
	    "list":[
		    1,
		    2,
		    3,
		    4
	    ],
	    "name":"TESTE",
	    "number":0
    }

@ToJsonString ao ser colocada, converte o valor da respetiva propriedade para uma string:

    data class Mix(val name: String = "") {
        val list = arrayListOf(1, 2, 3, 4)
        val listChar = arrayListOf('c', 'f', 'g')
        @ToJsonString
        val number = 0
    }

O resultado final será:
    
    {
	    "list":[
		    1,
		    2,
		    3,
		    4
	    ],
	    "listChar":[
		    "c",
		    "f",
		    "g"
	    ],
	    "name":"TESTE",
	    "number":"0"
    }

@Name(id : String) ao ser colocada, o nome da propriedade passa a ser o valor de id:

    data class Mix(val name: String = "") {
        val list = arrayListOf(1, 2, 3, 4)
        val listChar = arrayListOf('c', 'f', 'g')
        @Name("novonome")
        val number = 0
    }

O resultado final será:

    {
        "list":[
            1,
            2,
            3,
            4
        ],
	    "listChar":[
		    "c",
		    "f",
		    "g"
	    ],
        "novonome": 0,
        "name":"TESTE"
    }

Existe outra forma de criar objetos Json, por exemplo:

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

O código acima irá criar o seguinte objeto:

    {
        "number":1, 
        "boolean":true, 
        "teste":{
            "number":5
        }, 
        "array":[
            true, 
            false
        ], 
        "arr2":[
            {
                "numero":101101, 
                "nome":"Dave Farley", 
                "internacional":true
            }, 
            {
                "numero":101102, 
                "nome":"Martin Fowler", 
                "internacional":true
            }
        ], 
        "null":null
    }


Também é possivel aplicar o padrão de desenho Visitante nesta biblioteca, ou seja,
é possível fazer o varrimento da estrutura de dados de forma hierárquica, como por exemplo:
obter todos os valores guardados em propriedades com identificador “numero”

    val searchfor = SearchForArray("numero")
    rootObject.toJSON().accept(searchfor)
    assertEquals(2, searchfor.list.size)

obter todos os objetos que têm as propriedades numero e nome

    val searchfor = SearchForObject("numero", "nome")
    rootObject.toJSON().accept(searchfor)
    assertEquals(2, searchfor.list.size)


verificar que o modelo obedece a determinada estrutura, por exemplo:
a propriedade nome apenas tem String values,

    val cs = CheckStructure("nome", String::class)
    rootObject.toJSON().accept(cs)
    assertEquals(expected = true, cs.valid)
o cs.valid é true se o modelo obedecer à estrutura definida, caso contrário é false.


a propriedade arr2 consiste num array onde todos os objetos (arrayObject : ObjectJSON) têm a mesma estrutura,

    val cas = CheckArrayStructure("arr2", arrayObject)
    rootObject.toJSON().accept(cas)
    assertEquals(expected = true, cas.valid)
o cs.valid é true se o modelo obedece à estrutura definida, caso contrário é false.


Nesta biblioteca os elementos json representativos de um array (ArrayJSON) ou de um objeto (ObjectJSON) são observáveis. 
Os observados desses elementos irão ser notificados caso haja alterações às propriedades de um objeto ou aos elementos de um array.

    interface CompositeJsonObserver {
    
        fun elementAdded(children: JsonElement) {}
    
        fun elementRemoved(children: JsonElement) {}

        fun updateJSON() {}
    }





