package nurgling.actions;

import haven.*;
import haven.res.ui.tt.cn.CustomName;
import nurgling.NGItem;
import nurgling.NGameUI;
import nurgling.NUtils;
import nurgling.tasks.*;
import nurgling.tools.NAlias;

import java.util.ArrayList;

public class TransferToBarrel implements Action{

    Gob barrel;
    NAlias items;

    int th = 9000;

    double total = 0;
    public TransferToBarrel(Gob barrel, NAlias items) {
        this.barrel = barrel;
        this.items = items;
    }

    public TransferToBarrel(Gob barrel, NAlias items, int th) {
        this(barrel, items);
        this.th = th;
    }

    @Override
    public Results run(NGameUI gui) throws InterruptedException {

        if(barrel==null){
            return Results.ERROR("NULL BARREL");
        }
        new PathFinder( barrel ).run (gui);
        if ( !(new OpenTargetContainer (  "Barrel",barrel ).run ( gui ).isSuccess) ) {
            return Results.ERROR("OPEN FAIL");
        }
        double barrelCont = gui.getBarrelContent();
        total+=barrelCont;
        if(barrelCont>-1 && barrelCont < th) {

            ArrayList<WItem> witems = gui.getInventory().getItems(items);
            ArrayList<WItem> targetItems = new ArrayList<>();
            double sum = 0;
            for (WItem item : witems) {
                if (sum + barrelCont > th) {
                    break;
                }
                for (ItemInfo inf : item.item.info) {
                    if (inf instanceof GItem.Amount) {
                        if(sum + ((GItem.Amount) inf).itemnum()<10000) {
                            sum += ((GItem.Amount) inf).itemnum();
                            targetItems.add(item);
                            break;
                        }
                    }
                    if (inf instanceof CustomName)
                    {
                        if(((CustomName) inf).count>0 && sum + ((CustomName) inf).count<100) {
                            sum+=((CustomName)inf).count;
                            targetItems.add(item);
                            break;
                        }
                    }
                }
            }
            total+=sum;

            if(!targetItems.isEmpty()) {
                NUtils.takeItemToHand(targetItems.get(0));
                if(witems.size() == targetItems.size()) {
                    if(barrelCont == 0)
                    {
                        NUtils.activateItem(barrel, true);
                        if (targetItems.size()>1) {
                            NUtils.getUI().core.addTask(new NotThisInHand(NUtils.getGameUI().vhand));
                        }
                    }
                    NUtils.dropsame(barrel);
                    NUtils.getUI().core.addTask(new WaitItems(NUtils.getGameUI().getInventory(), items, 0));
                }
                else
                {
                    for (int i = 0; i < targetItems.size(); i++) {
                        NUtils.activateItem(barrel, true);
                        if (i + 1 < targetItems.size()) {
                            NUtils.getUI().core.addTask(new NotThisInHand(NUtils.getGameUI().vhand));
                        }
                    }
                    NUtils.getUI().core.addTask(new WaitItems(NUtils.getGameUI().getInventory(), items, witems.size() - targetItems.size() - 1));


                    if (NUtils.getGameUI().vhand != null ) {
                        NUtils.getUI().core.addTask(new WaitItemInHand());
                        gui.getInventory().dropOn(gui.getInventory().findFreeCoord(NUtils.getGameUI().vhand));
                    }
                }
            }
        }
        new CloseTargetContainer ( "Barrel" ).run ( gui );
        return Results.SUCCESS();
    }

    public boolean isFull()
    {
        return total>th;
    }
}
