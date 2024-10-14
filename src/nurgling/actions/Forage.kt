package nurgling.actions

import haven.Coord2d
import haven.Gob
import nurgling.NGameUI
import nurgling.NUtils
import nurgling.tools.Finder
import nurgling.tools.NAlias

class Forage(
    private val forageables: List<Gob>,
    private val avoidables: List<String>,
) : Action {
    private var initialPosition: Coord2d? = null

    @Throws(InterruptedException::class)
    override fun run(gui: NGameUI): Results {
        initialPosition = gui.map.player().rc

        for (forageable in forageables) {
            // Move to the forageable
            val pathFinder = PathFinder(forageable.rc)
            pathFinder.run(gui)

            // Pick the forageable
            val res = SelectFlowerAction("Pick", forageable).run(gui)

            waitForHandEmpty()
            // Wait for the picking animation to complete
            Thread.sleep(1000)
        }

        val foundAvoidables = Finder.findGobs(NAlias(avoidables))
        // Return to the initial position
        val returnPath = PathFinder(initialPosition)//, foundAvoidables, 50.0)
        returnPath.run(gui)

        return Results.SUCCESS()
    }

    @Throws(InterruptedException::class)
    private fun waitForHandEmpty() {
        val startTime = System.currentTimeMillis()
        val timeout: Long = 5000 // 5 seconds timeout

        while (System.currentTimeMillis() - startTime < timeout) {
            if (NUtils.getGameUI().vhand == null) {
                return  // Hand is empty
            }
            Thread.sleep(100) // Check every 100ms
        }

        // If we reach here, the hand didn't become empty within the timeout period
        NUtils.getGameUI().msg("Warning: Hand did not become empty within expected time.")
    }

}