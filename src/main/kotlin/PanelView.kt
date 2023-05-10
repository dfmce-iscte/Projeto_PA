import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel

interface PanelViewObserver {
    fun panelAdded() {}

    fun panelRemoved(panel : PanelView) {}
}

class PanelView(private val parent : PanelView? = null) : JPanel(){

    private val observers : MutableList<PanelViewObserver> = mutableListOf()

    init {
        println("Panel View init")
        addMouseListener(MouseClick())
        println("AddMouseListener")
        parent?.addChildren(object : PanelViewObserver {
            override fun panelRemoved(panel : PanelView) {
                remove(panel)
            }
        })
    }

    private fun addChildren(observer: PanelViewObserver) {
        observers.add(observer)
    }


    inner class MouseClick() : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            println("mouseCLicked")
            if (e?.button == MouseEvent.BUTTON3) {
                println("d")
                val addOrDelete = JPanel()
                val addButton = JButton("add")
                addButton.addActionListener {
                    observers.forEach { it.panelAdded()}
                }
                val deleteButton = JButton("delete")
                deleteButton.addActionListener {
                    observers.forEach {
                        it.panelRemoved(this@PanelView)
                    }
                }
                addOrDelete.add(addButton)
                addOrDelete.add(deleteButton)
            }
        }
    }
}