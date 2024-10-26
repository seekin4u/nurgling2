package nurgling.actions.bots;

import haven.*;
import nurgling.NConfig;
import nurgling.NGameUI;
import nurgling.NUtils;
import nurgling.actions.*;
import nurgling.areas.NArea;
import nurgling.conf.NSmokProp;
import nurgling.tasks.WaitCheckable;
import nurgling.tools.Container;
import nurgling.tools.Context;
import nurgling.tools.Finder;
import nurgling.tools.NAlias;
import nurgling.widgets.SmokingSettings;
import nurgling.widgets.Specialisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static nurgling.widgets.Specialisation.SpecName.logs;

public class Smoking implements Action {
    @Override
    public Results run(NGameUI gui) throws InterruptedException {
        SmokingSettings w = null;
        try {
            NUtils.getUI().core.addTask(new WaitCheckable( NUtils.getGameUI().add((w = new SmokingSettings()), UI.scale(200,200))));
        }
        catch (InterruptedException e)
        {
            throw e;
        }
        finally {
            if(w!=null)
                w.destroy();
        }
        ArrayList<NSmokProp> smokProps = (ArrayList<NSmokProp>)NConfig.get(NConfig.Key.smokeprop);
        if(smokProps==null || smokProps.isEmpty())
        {
            return Results.ERROR("No config");
        }

        NArea.Specialisation slogs = new NArea.Specialisation(Specialisation.SpecName.fuel.toString(),"Log");
        NArea.Specialisation ssmokshed = new NArea.Specialisation(Specialisation.SpecName.smokshed.toString());
        ArrayList<NArea.Specialisation> req = new ArrayList<>();
        req.add(slogs);
        req.add(ssmokshed);
        ArrayList<NArea.Specialisation> opt = new ArrayList<>();
        ArrayList<NSmokProp> cands = new ArrayList<>();
        Pair<Coord2d,Coord2d> logs = NArea.findSpec(slogs).getRCArea();
        Pair<Coord2d,Coord2d> sheds = NArea.findSpec(ssmokshed).getRCArea();
        if(new Validator(req, opt).run(gui).IsSuccess()) {
            for(NSmokProp prop : smokProps) {
                Gob testlog = Finder.findGob(logs,new NAlias(prop.fuel));
                NArea testarea = NArea.findIn(prop.iconName);
                if(testlog!=null && testarea!=null) {
                    cands.add(prop);
                }
            }
            if(cands.isEmpty()) {
                return Results.ERROR("No logs, or input areas not found");
            }
            HashMap<String,ArrayList<NSmokProp>> fuels = new HashMap<String,ArrayList<NSmokProp>>();
            Context context = new Context();
            for(NSmokProp prop : cands) {
                if(!fuels.containsKey(prop.fuel)) {
                    fuels.put(prop.fuel, new ArrayList<>());
                }
                fuels.get(prop.fuel).add(prop);
            }

            ArrayList<Container> containers = new ArrayList<>();

            for (Gob sm : Finder.findGobs(sheds, new NAlias("gfx/terobjs/smokeshed"))) {
                if((sm.ngob.getModelAttribute()&16)==16)
                    continue;
                Container cand = new Container();
                cand.gob = sm;
                cand.cap = "Smoke shed";

                cand.initattr(Container.Space.class);
                cand.initattr(Container.FuelLvl.class);
                cand.getattr(Container.FuelLvl.class).setMaxlvl(5);
                cand.getattr(Container.FuelLvl.class).setAbsMaxlvl(10);
                cand.getattr(Container.FuelLvl.class).setFueltype("Log");
                containers.add(cand);
            }
            if(containers.isEmpty())
            {
                return Results.ERROR("No containers found");
            }

            new FreeContainers(containers).run(gui);

            ArrayList<Gob> lighted = new ArrayList<>();
            for(String fuel : fuels.keySet()) {
                for(NSmokProp prop : fuels.get(fuel)) {
                    new FillContainers(containers,prop.iconName, context).run(gui);
                }
                ArrayList<Container> forRemove = new ArrayList<>();
                ArrayList<Container> forClear = new ArrayList<>();
                for(Container cand : containers) {
                    Container.Space space = cand.getattr(Container.Space.class);
                    if(space.getFreeSpace()>0 && space.getFreeSpace()!=space.getMaxSpace()) {
                        if (space.getFreeSpace() > space.getMaxSpace() / 2) {
                            forClear.add(cand);
                        }
                    } else {
                        if(space.getFreeSpace()!=space.getMaxSpace())
                            forRemove.add(cand);
                    }
                }
                new FreeContainers(forClear).run(gui);
                new FuelByLogs(forRemove, fuel).run(gui);
                for(Container cand : forRemove) {
                    lighted.add(cand.gob);
                }
                containers.removeAll(forRemove);
                for(Container cand : forClear) {
                    Container.Space space = cand.getattr(Container.Space.class);
                    if(space.getFreeSpace()!=space.getMaxSpace())
                        containers.remove(cand);
                }
            }
            new LightGob(lighted,16).run(gui);
        }
        else
        {
            return Results.FAIL();
        }
        return Results.SUCCESS();
    }
}
