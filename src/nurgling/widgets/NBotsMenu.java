package nurgling.widgets;

import haven.*;
import nurgling.*;
import nurgling.actions.*;
import nurgling.actions.bots.*;
import nurgling.actions.test.*;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NBotsMenu extends Widget
{
    final static String dir_path = "nurgling/bots/icons/";
    public NBotsMenu()
    {
        NLayout resources = new NLayout("resources");
        resources.elements.add(new NButton("choper", new Chopper()));
        resources.elements.add(new NButton("pblocks", new PrepareBlocks()));
        resources.elements.add(new NButton("pboards", new PrepareBoards()));
        resources.elements.add(new NButton("log", new TransferLog()));
        resources.elements.add(new NButton("clay", new ClayDigger()));
        resources.elements.add(new NButton("bark", new CollectBark()));
        resources.elements.add(new NButton("bough", new CollectBough()));
        resources.elements.add(new NButton("leaf", new CollectLeaf()));
        addLayout(resources);
        NLayout productions = new NLayout("productions");
        productions.elements.add(new NButton("smelter", new SmelterAction()));
        productions.elements.add(new NButton("backer", new BackerAction()));
        productions.elements.add(new NButton("ugardenpot", new UnGardentPotAction()));
        addLayout(productions);
        NLayout battle = new NLayout("battle");
        battle.elements.add(new NButton("reagro", new Reagro()));
        addLayout(battle);
        NLayout farming = new NLayout("farming");
        farming.elements.add(new NButton("turnip", new TurnipsFarmer()));
        farming.elements.add(new NButton("hemp", new HempFarmer()));
        farming.elements.add(new NButton("flax", new FlaxFarmer()));
        farming.elements.add(new NButton("goats", new GoatsAction()));
        farming.elements.add(new NButton("sheeps", new SheepsAction()));
        farming.elements.add(new NButton("pigs", new PigsAction()));
        farming.elements.add(new NButton("cows", new nurgling.actions.bots.CowsAction()));
        addLayout(farming);
        NLayout utils = new NLayout("utils");
        utils.elements.add(new NButton("shieldsword", new EquipShieldSword()));
        utils.elements.add(new NButton("filwater", new FillWaterskins()));
        utils.elements.add(new NButton("unbox", new FreeContainersInArea()));
        addLayout(utils);
        if(NUtils.getUI().core.debug)
        {
            NLayout tests = new NLayout("utils");
            tests.elements.add(new NButton("shieldsword", new TESTLiftDrop()));
            tests.elements.add(new NButton("shieldsword", new TESTbranchinvtransferpacks()));
            tests.elements.add(new NButton("shieldsword", new TESTtakehanddporop()));
            tests.elements.add(new NButton("shieldsword", new TESTselectfloweraction()));
            tests.elements.add(new NButton("shieldsword", new TESTpf()));
            tests.elements.add(new NButton("shieldsword", new NomadWalker()));
            tests.elements.add(new NButton("shieldsword", new NomadCalibration()));
            addLayout(tests);
            NLayout nords = new NLayout("utils");
            nords.elements.add(new NButton("shieldsword", new NomadCalibration() ));
            nords.elements.add(new NButton("shieldsword", new NomadWalker() ));
            nords.elements.add(new NButton("shieldsword", new NomadSailor() ));
            nords.elements.add(new NButton("shieldsword", new NomadOisterer("./oyster1.dat") ));
            nords.elements.add(new NButton("shieldsword", new NomadOisterer("./oyster2.dat") ));
            nords.elements.add(new NButton("shieldsword", new OisterFounder() ));
            nords.elements.add(new NButton("shieldsword", new NomadOistererSnekkja("./oyster1.dat") ));
            addLayout(nords);

        }
        showLayouts();
        pack();
    }
    NButton dragging = null;
    @Override
    public void draw(GOut g, boolean strict) {
        super.draw(g, strict);
        if(dragging != null) {
            BufferedImage ds = dragging.btn.up;
            Coord dssz = new Coord(ds.getWidth(),ds.getHeight());
            ui.drawafter(new UI.AfterDraw() {
                public void draw(GOut g) {
                    g.reclip(ui.mc.sub(dssz.div(2)), dssz);
                    g.image(new TexI(ds), ui.mc );
                }
            });
        }
    }
    private NButton bhit(Coord c) {
        for(NLayout lay : layouts)
        {
            for(NButton b : lay.elements)
            {
                if(b.btn.visible())
                {
                    if(c.x <= b.btn.c.x + b.btn.sz.x && c.y <= b.btn.c.y + b.btn.sz.y && c.x >= b.btn.c.x && c.y >= b.btn.c.y)
                        return b;
                }
            }
        }
        return(null);
    }
    public void mousemove(Coord c) {
        if((dragging == null) && (pressed != null)) {
            NButton h = bhit(c);
            if(h != pressed) {
                dragging = pressed;
                if(dragging.btn.d!=null)
                {
                    dragging.btn.d.remove();
                    dragging.btn.d=null;
                }
            }
        }
        super.mousemove(c);
    }

    private NButton pressed = null;
    private UI.Grab grab = null;
    public boolean mouseup(Coord c, int button) {
        NButton h = bhit(c);
        if((button == 1) && (grab != null)) {
            if(dragging != null) {
                ui.dropthing(ui.root, ui.mc, dragging);
                pressed = null;
                dragging = null;
            } else if(pressed != null) {
                if(pressed == h) {
                    pressed.btn.click();
                }
                pressed = null;
            }
            grab.remove();
            grab = null;
        }
        return(super.mouseup(c,button));
    }

    public boolean mousedown(Coord c, int button) {
        NButton h = bhit(c);
        if((button == 1) && (h != null)) {
            pressed = h;
            grab = ui.grabmouse(this);
        }
        boolean res = super.mousedown(c,button);
        if(pressed!=null)
        {
            if(pressed.btn.d!=null)
            {
                pressed.btn.d.remove();
                pressed.btn.d=null;
            }
        }
        return res;
    }

    void addLayout(NLayout lay){
        int count = 0;
        for(NButton btn: lay.elements)
        {
            add(btn.btn, new Coord(0, (btn.btn.sz.y + UI.scale(2)) * count++));
            btn.btn.hide();
        }
        add(lay.btn);
        lay.btn.hide();
        layouts.add(lay);
    }

    public void showLayouts(){
        for(NLayout lay : layouts)
        {
            lay.hideElements();
        }
        int w = 0;
        int h = 0;
        for (NLayout lay : layouts)
        {
            lay.btn.move(new Coord(w * UI.scale(34), h * UI.scale(34)));
            lay.btn.show();
            if (h > 8)
            {
                w += 1;
                h = 0;
            }
            else
            {
                h += 1;
            }
        }
        if(parent!=null)
            parent.resize(new Coord((w + 1) * UI.scale(34), layouts.size() * UI.scale(34)).add(NDraggableWidget.delta));
    }

    public void hideLayouts(){
        for (NLayout lay : layouts)
        {
            lay.btn.hide();
        }
    }

    ArrayList<NLayout> layouts = new ArrayList<>();

    public NButton find(String path) {
        for (NLayout lay : layouts)
        {
            for(NButton element: lay.elements)
            {
                if(element.path!=null && element.path.equals(path))
                    return element;
            }
        }
        return null;
    }

    public class NButton
    {
        public final IButton btn;
        public String path;
        NButton(String path, Action action)
        {
            this.path = path;
            btn = new IButton(Resource.loadsimg(dir_path + path + "/u"), Resource.loadsimg(dir_path + path + "/d"), Resource.loadsimg(dir_path + path + "/h")).action(
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            start(path, action);
                        }
                    });
        }

        private NButton()
        {
            btn = new IButton(Resource.loadsimg(dir_path + "back" + "/u"), Resource.loadsimg(dir_path +  "back" + "/d"), Resource.loadsimg(dir_path +  "back" + "/h")){
                @Override
                public void click() {
                    super.click();
                    showLayouts();
                }
            };

        }



        void start(String path, Action action)
        {
            Thread t;
            (t = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        showLayouts();
                        action.run(NUtils.getGameUI());
                    }
                    catch (InterruptedException e)
                    {
                        NUtils.getGameUI().msg(path + ":" + "STOPPED");
                    }
                    finally
                    {
                        if(action instanceof ActionWithFinal)
                        {
                            ((ActionWithFinal)action).endAction();
                        }
                    }
                }
            }, path)).start();
            NUtils.getGameUI().biw.addObserve(t);
        }
    };

    class NLayout
    {
        public final IButton btn;

        ArrayList<NButton> elements = new ArrayList<>();

        public NLayout(String path)
        {
            this.btn = new IButton(Resource.loadsimg(dir_path + path + "/u"),Resource.loadsimg(dir_path + path + "/d"),Resource.loadsimg(dir_path + path + "/h")).action(new Runnable()
            {
                @Override
                public void run()
                {
                    hideLayouts();
                    showElements();
                }
            });
            elements.add(new NButton());
        }

        void hideElements()
        {
            for (NButton element : elements)
            {
                element.btn.hide();
            }
        }

        void showElements()
        {
            int w = 0;
            int h = 0;
            for (NButton element : elements)
            {
                element.btn.move(new Coord(w * UI.scale(34), h * UI.scale(34)));
                if (h > 8)
                {
                    w += 1;
                    h = 0;
                }
                else
                {
                    h += 1;
                }
                element.btn.show();
            }
            parent.resize(new Coord((w + 1) * UI.scale(34), (w > 0 ? 9 : h + 1) * UI.scale(34)).add(NDraggableWidget.delta));
        }
    };
}
