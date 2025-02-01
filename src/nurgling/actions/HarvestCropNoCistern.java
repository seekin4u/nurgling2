package nurgling.actions;

import haven.*;
import nurgling.NGameUI;
import nurgling.NUtils;
import nurgling.areas.NArea;
import nurgling.tasks.NoGob;
import nurgling.tools.Finder;
import nurgling.tools.NAlias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class HarvestCropNoCistern implements Action{

    final NArea field;
    final NArea seed;

    final NArea trougha;

    final NAlias crop;
    final NAlias iseed;

    int stage;

    public HarvestCropNoCistern(NArea field, NArea seedtrough, NAlias crop, NAlias iseed, int stage) {
        this.field = field;
        this.seed = seedtrough;
        this.trougha = seedtrough;
        this.crop = crop;
        this.iseed = iseed;
        this.stage = stage;
    }

    @Override
    public Results run(NGameUI gui) throws InterruptedException {

        ArrayList<Gob> barrels = Finder.findGobs(seed, new NAlias("barrel"));
        Gob trough = Finder.findGob(trougha, new NAlias("gfx/terobjs/trough"));

        if (barrels.isEmpty())
            return Results.ERROR("No barrel for seed");
//        if (trough == null)
//            return Results.ERROR("No trough for seed");
        HashMap<Gob, AtomicBoolean> barrelInfo = new HashMap();
        for(Gob barrel : barrels) {
            TransferToBarrel tb;
            (tb = new TransferToBarrel(barrel, iseed)).run(gui);
            barrelInfo.put(barrel, new AtomicBoolean(tb.isFull()));
        }
        if (!gui.getInventory().getItems(iseed).isEmpty()) {
            new TransferToTrough(trough, iseed).run(gui);
        }

        Coord start = gui.map.player().rc.dist(field.getArea().br.mul(MCache.tilesz)) < gui.map.player().rc.dist(field.getArea().ul.mul(MCache.tilesz)) ? field.getArea().br.sub(1, 1) : field.getArea().ul;
        Coord pos = new Coord(start);
        boolean rev = (pos.equals(field.getArea().ul));

        boolean revdir = rev;

        while (!Finder.findGobs(field, crop, stage).isEmpty() || !Finder.findGobs(field, new NAlias("gfx/terobjs/plants/fallowplant"), 0).isEmpty() ) {
                if (!rev) {
                    while (pos.x >= field.getArea().ul.x) {
                        AtomicBoolean setDir = new AtomicBoolean(true);
                        if (revdir) {
                            while (pos.y <= field.getArea().br.y - 1) {
                                Coord endPos = new Coord(Math.max(pos.x - 2, field.getArea().ul.x), Math.min(pos.y + 1, field.getArea().br.y - 1));
                                Area harea = new Area(pos, endPos, true);
                                Coord2d endp = harea.ul.mul(MCache.tilesz).add( MCache.tilesz.x + MCache.tilehsz.x, MCache.tilehsz.y).sub(0,MCache.tileqsz.y);
                                harvest(gui, barrelInfo, trough, harea, revdir, endp, setDir);
                                pos.y += 2;
                            }
                            pos.y = field.getArea().br.y - 1;
                        } else {
                            while (pos.y >= field.getArea().ul.y) {
                                Coord endPos = new Coord(Math.max(pos.x - 2, field.getArea().ul.x), Math.max(pos.y - 1, field.getArea().ul.y));
                                Area harea = new Area(pos, endPos, true);
                                Coord2d endp = harea.br.mul(MCache.tilesz).add(MCache.tilehsz.x, MCache.tilehsz.y).sub(MCache.tilesz.x, 0).add(0,MCache.tileqsz.y);
                                harvest(gui, barrelInfo, trough, harea, revdir,endp , setDir);
                                pos.y -= 2;
                            }
                            pos.y = field.getArea().ul.y;
                        }
                        revdir = !revdir;
                        pos.x -= 3;
                    }
                } else {
                    while (pos.x <= field.getArea().br.x - 1) {
                        AtomicBoolean setDir = new AtomicBoolean(true);
                        if (revdir) {
                            while (pos.y <= field.getArea().br.y - 1) {
                                Coord endPos = new Coord(Math.min(pos.x + 2, field.getArea().br.x - 1), Math.min(pos.y + 1, field.getArea().br.y - 1));
                                Area harea = new Area(pos, endPos, true);
                                Coord2d endp = harea.ul.mul(MCache.tilesz).add(MCache.tilehsz.x+MCache.tilesz.x, MCache.tilehqsz.y + MCache.tileqsz.y);
                                harvest(gui, barrelInfo, trough, harea, revdir, endp, setDir);
                                pos.y += 2;
                            }
                            pos.y = field.getArea().br.y - 1;
                        } else {
                            while (pos.y >= field.getArea().ul.y) {
                                Coord endPos = new Coord(Math.min(pos.x + 2, field.getArea().br.x - 1), Math.max(pos.y - 1, field.getArea().ul.y));
                                Area harea = new Area(pos, endPos, true);
                                Coord2d endp = harea.br.mul(MCache.tilesz).add(MCache.tilehsz).sub(MCache.tilesz.x, 0).add(0,MCache.tileqsz.y);
                                harvest(gui, barrelInfo, trough, harea, revdir, endp, setDir);
                                pos.y -= 2;
                            }
                            pos.y = field.getArea().ul.y;
                        }
                        revdir = !revdir;
                        pos.x += 3;
                    }
                }



        }

        if (!gui.getInventory().getItems(iseed).isEmpty()) {
            for(Gob barrel : barrelInfo.keySet()) {
                if (!gui.getInventory().getItems(iseed).isEmpty()) {
                    if (!barrelInfo.get(barrel).get()) {
                        TransferToBarrel tb;
                        (tb = new TransferToBarrel(barrel, iseed)).run(gui);
                        barrelInfo.put(barrel, new AtomicBoolean(tb.isFull()));
                    }
                }
            }
            if (!gui.getInventory().getItems(iseed).isEmpty()) {
                new TransferToTrough(trough, iseed).run(gui);
            }
        }
        return Results.SUCCESS();
    }


    private Results harvest(NGameUI gui, HashMap<Gob,AtomicBoolean> barrelInfo, Gob trough, Area area, boolean rev, Coord2d target_coord, AtomicBoolean setDir) throws InterruptedException {
        if (gui.getInventory().getFreeSpace() <= 5) {
            for(Gob barrel : barrelInfo.keySet()) {
                if (!gui.getInventory().getItems(iseed).isEmpty()) {
                    if (!barrelInfo.get(barrel).get()) {
                        TransferToBarrel tb;
                        (tb = new TransferToBarrel(barrel, iseed)).run(gui);
                        barrelInfo.put(barrel, new AtomicBoolean(tb.isFull()));
                    }
                }
            }
            if (!gui.getInventory().getItems(iseed).isEmpty()) {
                return Results.ERROR("");
            }
        }
        if(NUtils.getStamina()<0.35)
            if(!new Drink(0.9,false).run(gui).isSuccess)
                throw new InterruptedException();
        Gob plant;
        plant = Finder.findGob(target_coord.div(MCache.tilesz).floor(),crop, stage);
        if(plant == null)
        {
            plant = Finder.findGob(target_coord.div(MCache.tilesz).floor(),new NAlias("gfx/terobjs/plants/fallowplant"), 0);
        }
        if(plant!=null) {
            if(PathFinder.isAvailable(target_coord)) {
                new PathFinder(target_coord).run(NUtils.getGameUI());
                if (setDir.get()) {
                    if (rev)
                        new SetDir(new Coord2d(0, 1)).run(gui);
                    else
                        new SetDir(new Coord2d(0, -1)).run(gui);
                    setDir.set(false);
                }
            }
            else
            {
                new PathFinder(plant).run(NUtils.getGameUI());
            }
            new SelectFlowerAction("Harvest", plant).run(gui);
            NUtils.getUI().core.addTask(new NoGob(plant.id));
        }
        ArrayList<Gob> plants;
        while (!(plants = Finder.findGobs(area,crop,stage)).isEmpty())
        {
            plant = plants.get(0);
            new PathFinder(plant).run(gui);
            new SelectFlowerAction("Harvest", plant).run(gui);
            NUtils.getUI().core.addTask(new NoGob(plant.id));
        }

        while (!(plants = Finder.findGobs(area,new NAlias("gfx/terobjs/plants/fallowplant"), 0)).isEmpty())
        {
            plant = plants.get(0);
            new PathFinder(plant).run(gui);
            new SelectFlowerAction("Harvest", plant).run(gui);
            NUtils.getUI().core.addTask(new NoGob(plant.id));
        }
        return Results.SUCCESS();
    }
}
