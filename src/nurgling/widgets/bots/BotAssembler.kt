package nurgling.widgets.bots

import com.google.gson.GsonBuilder
import haven.*
import nurgling.NUtils
import nurgling.actions.bots.BotAction
import nurgling.actions.bots.BotAction.*
import nurgling.actions.bots.BotActionSerializer
import nurgling.actions.bots.SelectGobAction
import java.io.File
import java.io.FileWriter
import kotlin.math.max

class BotAssembler : Window(Coord(300, 400), "Bot Assembler"), Checkable {
    private val actions: MutableList<BotAction> = ArrayList()
    private lateinit var prev: Widget
    private val addPathAction: Button
    private val addOpenGateAction: Button
    private val addChooseRoadAction: Button
    private val addGenericInteractAction: Button
    private val addHearthBackAction: Button
    private val removeLastAction: Button
    private val saveButton: Button
    private val actionList: Listbox<BotAction>
    private val nameField: TextEntry

    init {
        prev = add(Label("Bot Actions:"))

        actionList = object : Listbox<BotAction>(280, 10, UI.scale(16)) {
            override fun listitem(i: Int): BotAction {
                return actions[i]
            }

            override fun listitems(): Int {
                return actions.size
            }

            override fun drawitem(g: GOut, action: BotAction?, i: Int) {
                g.text(action.toString(), Coord.z)
            }
        }
        prev = add<Listbox<BotAction>>(actionList, prev.pos("bl").add(UI.scale(0, 5)))

        addPathAction = object : Button(UI.scale(90), "Add Path") {
            override fun click() {
                openPathActionDialog()
            }
        }
        prev = add<Button>(addPathAction, prev.pos("bl").add(UI.scale(0, 5)))

        addOpenGateAction = object : Button(UI.scale(90), "Add Open Gate") {
            override fun click() {
                NUtils.getGameUI().msg("Select object to interact")
                Thread {
                    val selectGob = SelectGobAction()
                    try {
                        val result = selectGob.run(ui.gui)
                        if (result.IsSuccess()) {
                            val gob = selectGob.result
                            val grid = NUtils.getGameUI().map.glob.map.getgrid(gob.rc)
                            val gridPos = gob.getgridpos()
                            actions.add(TraverseGate(grid.id,gridPos.x,gridPos.y))

                            actionList.sb.`val` = actions.size - 1
                        } else {
                            NUtils.getGameUI().msg("Failed to select object to interact")
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }

        add<Button>(addOpenGateAction, prev.pos("ur").add(UI.scale(5, 0)))

        addChooseRoadAction = object : Button(UI.scale(110), "Add Choose Road") {
            override fun click() {
                openChooseRoadDialog()
            }
        }
        prev = add<Button>(addChooseRoadAction, prev.pos("bl").add(UI.scale(0, 5)))

        addGenericInteractAction = object : Button(UI.scale(90), "Add Interact") {
            override fun click() {
                NUtils.getGameUI().msg("Select object to interact")
                Thread {
                    val selectGob = SelectGobAction()
                    try {
                        val result = selectGob.run(ui.gui)
                        if (result.IsSuccess()) {
                            actions.add(GenericInteract(selectGob.result))
                            actionList.sb.`val` = actions.size - 1
                        } else {
                            NUtils.getGameUI().msg("Failed to select object to interact")
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }.start()
            }
        }
        add<Button>(addGenericInteractAction, prev.pos("ur").add(UI.scale(5, 0)))

        addHearthBackAction = object : Button(UI.scale(110), "Add Hearth Back") {
            override fun click() {
                actions.add(BotAction.HearthBack)
                actionList.sb.`val` = actions.size - 1
            }
        }
        prev = add<Button>(addHearthBackAction, prev.pos("bl").add(UI.scale(0, 5)))

        removeLastAction = object : Button(UI.scale(90), "Remove Last") {
            override fun click() {
                if (!actions.isEmpty()) {
                    actions.removeAt(actions.size - 1)
                    actionList.sb.`val` = max(0.0, (actions.size - 1).toDouble()).toInt()
                }
            }
        }
        add<Button>(removeLastAction, prev.pos("ur").add(UI.scale(5, 0)))

        prev = add(Label("Filename:"), prev.pos("bl").add(UI.scale(0, 10)))

        nameField = object : TextEntry(280, "") {
            override fun activate(text: String) {
                saveBotConfiguration()
            }
        }
        prev = add<TextEntry>(nameField, prev.pos("bl").add(UI.scale(0, 2)))

        saveButton = object : Button(UI.scale(70), "Save") {
            override fun click() {
                saveBotConfiguration()
            }
        }
        add<Button>(saveButton, prev.pos("bl").add(UI.scale(0, 5)))

        pack()
    }

    private fun openPathActionDialog() {
        parent.add(object : Window(Coord(400, 400), "Path Action") {
            init {
                var prev: Widget = add(Label("Path:"))
                val pathList = addFileList(this, "bots/paths", 150, 5, prev.pos("bl").add(0, 2))

                prev = add(Label("Forageables:"), pathList.pos("bl").add(0, 5))
                val forageablesList = addFileList(this, "bots/forageables", 150, 5, prev.pos("bl").add(0, 2))

                prev = add(Label("Avoidables:"), forageablesList.pos("bl").add(0, 5))
                val avoidablesList = addFileList(this, "bots/avoidables", 150, 5, prev.pos("bl").add(0, 2))

                add(object : Button(UI.scale(70), "Add") {
                    override fun click() {
                        val path = pathList.sel
                        val forageables = forageablesList.sel
                        val avoidables = avoidablesList.sel

                        if (path != null) {
                            val pathAction: BotAction = BotAction.Path(
                                path,
                                if (forageables != null) forageables else "",
                                if (avoidables != null) avoidables else ""
                            )
                            actions.add(pathAction)
                            actionList.sb.`val` = actions.size - 1
                            parent.destroy()
                        } else {
                            ui.msg("Please select a path.")
                        }
                    }
                }, avoidablesList.pos("bl").add(0, 5))
                pack()
            }
        }, UI.scale(200, 200))
    }

    private fun addFileList(
        widget: Widget,
        dirPath: String,
        width: Int,
        visibleItems: Int,
        pos: Coord
    ): Listbox<String> {
        val files = loadFiles(dirPath)
        val list: Listbox<String> = object : Listbox<String>(width, visibleItems, UI.scale(16)) {
            override fun listitem(i: Int): String {
                return files[i]
            }

            override fun listitems(): Int {
                return files.size
            }

            override fun drawitem(g: GOut, item: String, i: Int) {
                g.text(item, Coord.z)
            }
        }
        widget.add(list, pos)
        return list
    }

    private fun loadFiles(dirPath: String): List<String> {
        val files: MutableList<String> = ArrayList()
        val dir = File(dirPath)
        if (dir.exists() && dir.isDirectory) {
            val fileList = dir.listFiles()
            if (fileList != null) {
                for (file in fileList) {
                    if (file.isFile) {
                        files.add(file.name)
                    }
                }
            }
        }
        return files
    }

    private fun openChooseRoadDialog() {
        parent.add(object : Window(Coord(250, 150), "Choose Road Action") {
            init {
                val prev: Widget = add(Label("Road name:"))
                val roadEntry = add(TextEntry(230, ""), prev.pos("bl").add(0, 2))
                add(object : Button(UI.scale(70), "Add") {
                    override fun click() {
//                    BotAction chooseRoadAction = BotAction.chooseRoad()
//                    chooseRoadAction.road = roadEntry.text();
                        actions.add(ChooseRoad(null, roadEntry.text()))
                        actionList.sb.`val` = actions.size - 1
                        parent.destroy()
                    }
                }, roadEntry.pos("bl").add(0, 5))
                pack()
            }
        }, UI.scale(200, 200))
    }

    private fun saveBotConfiguration() {
        val name = nameField.text().trim { it <= ' ' }
        if (name.isNotEmpty() && actions.isNotEmpty()) {
            try {
                val dir = File("bots/bot")
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val file = File(dir, "$name.json")
                val gson = GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(BotAction::class.java, BotActionSerializer())
                    .create()

                FileWriter(file).use {
                    it.write("[\n")
                    actions.map { action -> gson.toJson(action, BotAction::class.java) }.joinToString("\n,")
                        .let { it1 -> it.write(it1) }
                    it.write("\n]")

                }
                ui.msg("Bot configuration saved successfully!")
            } catch (e: Exception) {
                ui.msg("Error saving bot configuration: " + e.message)
            }
        } else {
            ui.msg("Please enter a name and add at least one action.")
        }
    }

    override fun check(): Boolean {
        return true
    }

    override fun wdgmsg(msg: String, vararg args: Any) {
        if (msg == "close") {
            hide()
        }
        super.wdgmsg(msg, *args)
    }
}