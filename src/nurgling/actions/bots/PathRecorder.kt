package nurgling.actions.bots

import haven.UI
import nurgling.NGameUI
import nurgling.NUtils
import nurgling.actions.Action
import nurgling.actions.Results
import nurgling.tasks.WaitCheckable
import nurgling.widgets.bots.BotAssembler
import nurgling.widgets.bots.CoordinateRecorder

class PathRecorder : Action {
    @Throws(InterruptedException::class)
    override fun run(gui: NGameUI): Results {
        NUtils.getUI().core.addTask(WaitCheckable(NUtils.getGameUI().add((CoordinateRecorder()), UI.scale(200, 200))))
        return Results.SUCCESS()
    }
}
