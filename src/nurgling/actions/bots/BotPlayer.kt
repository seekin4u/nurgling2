package nurgling.actions.bots

import haven.Coord2d
import haven.Gob
import haven.UI
import nurgling.NGameUI
import nurgling.NUtils
import nurgling.actions.Action
import nurgling.actions.PathFinder
import nurgling.actions.Results
import nurgling.tasks.WaitCheckable
import nurgling.widgets.bots.BotLoader
import nurgling.widgets.bots.BotLoaderHelper

class BotPlayer : Action {

    @Throws(InterruptedException::class)
    override fun run(gui: NGameUI): Results {

        val botLoader = BotLoader()
        NUtils.getUI().core.addTask(WaitCheckable(NUtils.getGameUI().add((botLoader), UI.scale(200, 200))))

        val bot = botLoader.selectedBot

        if (bot == null) {
            gui.msg("No bot selected")
            return Results.FAIL()
        }
        val botActions = BotLoaderHelper.loadBot(bot)

        gui.msg("Starting bot player")

        for (action in botActions) {
            val result = executeAction(action, gui)
            if (!result.IsSuccess()) {
                gui.msg("Action failed: " + action.javaClass.simpleName)
                return result
            }
        }

        gui.msg("Bot player finished successfully")
        return Results.SUCCESS()
    }

    @Throws(InterruptedException::class)
    private fun executeAction(action: BotAction, gui: NGameUI): Results {
        return when (action) {
            is BotAction.ChooseRoad -> TODO()
            is BotAction.GenericInteract -> TODO()
            BotAction.HearthBack -> try {
                NUtils.hfout()
                return Results.SUCCESS()
            } catch (e: Exception) {
                e.printStackTrace()
                return Results.FAIL()
            }

            is BotAction.Path -> PathPlayer(action.path, action.forageables, action.avoidables).run(gui)
            is BotAction.TraverseGate ->{
                gui.msg("Traversing gate")
                val gridId = action.grid
                val grid = NUtils.getGameUI().map.glob.map.findGrid(gridId)
                val gob: Gob? = NUtils.getUI().gui.getGob(grid, Coord2d(action.x, action.y))
                if (gob == null) {
                    gui.msg("Gate not found")
                    return Results.FAIL()
                }

                // Open the gate
                NUtils.rclickGob(gob)
                Thread.sleep(1000) // Wait for the gate to open

                // Determine gate direction
                val playerPos = NUtils.player().rc
                val gatePos = gob.rc
                val direction = getGateDirection(playerPos, gatePos)

                // Move through the gate
                val moveOffset = when (direction) {
                    "north" -> Coord2d(0.0, -5.0)
                    "south" -> Coord2d(0.0, 5.0)
                    "east" -> Coord2d(5.0, 0.0)
                    "west" -> Coord2d(-5.0, 0.0)
                    else -> Coord2d(0.0, 0.0)
                }
                val targetPos = gatePos.add(moveOffset)
                PathFinder(targetPos).run(gui)

                // Close the gate
                NUtils.rclickGob(gob)

                Results.SUCCESS()
            }
        }
    }

    fun getGateDirection(playerPos: Coord2d, gatePos: Coord2d): String {
        val dx = gatePos.x - playerPos.x
        val dy = gatePos.y - playerPos.y
        return when {
            Math.abs(dx) > Math.abs(dy) -> if (dx > 0) "east" else "west"
            else -> if (dy > 0) "south" else "north"
        }
    }
}

