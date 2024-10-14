package nurgling.widgets.bots

import haven.*
import java.io.File

class BotLoader : Window(Coord(300, 400), "Bot Loader"), Checkable {
    private lateinit var prev: Widget
    private val botList: Listbox<String>
    private val loadButton: Button
    private var selectedFilename: String? = null
    var selectedBot : String? = null
    init {
        prev = add(Label("Available Bots:"))

        botList = object : Listbox<String>(280, 10, UI.scale(16)) {
            override fun listitem(i: Int): String {
                return botFiles[i]
            }

            override fun listitems(): Int {
                return botFiles.size
            }

            override fun drawitem(g: GOut, item: String?, i: Int) {
                g.text(item ?: "", Coord.z)
            }

            override fun change(item: String?) {
                selectedFilename = item
            }
        }
        prev = add(botList, prev.pos("bl").add(UI.scale(0, 5)))

        loadButton = object : Button(UI.scale(70), "Load") {
            override fun click() {
                if (selectedFilename != null) {
                    selectedBot = selectedFilename
                    ui.msg("Loading bot: $selectedBot")
                    hide()
                } else {
                    ui.msg("Please select a bot to load.")
                }
            }
        }
        add(loadButton, prev.pos("bl").add(UI.scale(0, 5)))

        pack()
    }

    val botFiles by lazy {
        val files: MutableList<String> = ArrayList()
        val dir = File("bots/bot")
        if (dir.exists() && dir.isDirectory) {
            val fileList = dir.listFiles { file -> file.isFile && file.extension == "json" }
            if (fileList != null) {
                for (file in fileList) {
                    files.add(file.nameWithoutExtension)
                }
            }
        }
        files
    }

    override fun check(): Boolean {
        return selectedBot != null
    }

    override fun wdgmsg(msg: String, vararg args: Any) {
        if (msg == "close") {
            hide()
        }
        super.wdgmsg(msg, *args)
    }
}