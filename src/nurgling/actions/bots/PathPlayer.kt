package nurgling.actions.bots

import haven.Coord2d
import haven.MCache
import nurgling.NGameUI
import nurgling.NUtils
import nurgling.actions.Action
import nurgling.actions.Forage
import nurgling.actions.PathFinder
import nurgling.actions.Results
import nurgling.tools.Finder
import nurgling.tools.NAlias
import nurgling.widgets.bots.BotLoaderHelper

class PathPlayer(
    val path: String,
    val forageablesPath: String,
    val avoidablesPath: String
) : Action {

    @Throws(InterruptedException::class)
    override fun run(gui: NGameUI): Results {

        val coords = BotLoaderHelper.loadCoordinates(path)
        val forageables = BotLoaderHelper.loadForageables(forageablesPath)
        val avoidables = BotLoaderHelper.loadAvoidables(avoidablesPath)
        val range = 3.0
        val startingPosition = gui.map.player().rc

        for (coord in coords) {
            var pos = coord.add(startingPosition)
            val poscoord = pos.div(MCache.tilesz).floor()
            pos = Coord2d(
                (poscoord.x * MCache.tilesz.x + MCache.tilesz.x / 2),
                (poscoord.y * MCache.tilesz.y + MCache.tilesz.y / 2)
            )
            gui.msg("Moving to $pos")
            val foundAvoidables = Finder.findGobs(NAlias(avoidables))
            PathFinder(pos).run(gui)//, foundAvoidables, range).run(gui)

            val foundForageables = Finder.findGobs(NAlias(forageables))
            if (foundForageables.isNotEmpty()) {
                NUtils.getUI().msg("Found forageables")
                Forage(foundForageables,avoidables).run(gui)
            }
        }

        return Results.SUCCESS()
    }

}
