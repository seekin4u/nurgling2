package nurgling.actions.bots;

import haven.*;
import nurgling.NGameUI;
import nurgling.NMapView;
import nurgling.NUtils;
import nurgling.actions.Action;
import nurgling.actions.Results;

public class SelectGobAction implements Action {

    public SelectGobAction() {

    }

    Gob result;

    @Override
    public Results run(NGameUI gui) throws InterruptedException {
        if (!((NMapView) NUtils.getGameUI().map).isSelectingGobMode.get()) {
            ((NMapView) NUtils.getGameUI().map).isSelectingGobMode.set(true);

            nurgling.tasks.SelectGob sa;
            NUtils.getUI().core.addTask(sa = new nurgling.tasks.SelectGob());
            if (sa.getResult() != null) {
                result = sa.getResult();
                ((NMapView) NUtils.getGameUI().map).isSelectingGobMode.set(false);
            }
        }
        else
        {
            return Results.FAIL();
        }
        return Results.SUCCESS();
    }

    public Gob getResult() {
        return result;
    }

}
