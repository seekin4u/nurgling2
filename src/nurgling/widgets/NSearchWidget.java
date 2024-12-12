package nurgling.widgets;

import haven.*;
import nurgling.*;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static haven.ItemInfo.catimgs;

public class NSearchWidget extends Widget {
    public final NPopupWidget history;
    public CmdList cmdList;
    TextEntry searchF = null;
    public static final Text.Foundry nfnd = new Text.Foundry(Text.dfont, 10);
    Window helpwnd;
    private static final BufferedImage[] searchbi = new BufferedImage[]{
            Resource.loadsimg("nurgling/hud/buttons/search/u"),
            Resource.loadsimg("nurgling/hud/buttons/search/d"),
            Resource.loadsimg("nurgling/hud/buttons/search/h")};
    private static final BufferedImage[] ssearchbi = new BufferedImage[]{
            Resource.loadsimg("nurgling/hud/buttons/ssearch/u"),
            Resource.loadsimg("nurgling/hud/buttons/ssearch/d"),
            Resource.loadsimg("nurgling/hud/buttons/ssearch/h")};

    private static final Tex[] lsearchbi = new Tex[]{
            Resource.loadtex("nurgling/hud/buttons/lsearch/u"),
            Resource.loadtex("nurgling/hud/buttons/lsearch/d"),
            Resource.loadtex("nurgling/hud/buttons/lsearch/h"),
            Resource.loadtex("nurgling/hud/buttons/lsearch/dh")};

    IButton help;
    IButton save;
    ICheckBox list;
    int tpos_y;

    public Widget create(UI ui, Object[] args) {
        return (new NSearchWidget((Coord) args[0]));
    }

    public NSearchWidget(Coord sz) {
        super(sz);
        searchF = new TextEntry(sz.x, "") {
            @Override
            public boolean keydown(KeyDownEvent e) {
                boolean res = super.keydown(e);
                NUtils.getGameUI().itemsForSearch.install(text());
                return res;
            }
        };

        help = new IButton(searchbi[0], searchbi[1], searchbi[2]) {
            @Override
            public void click() {
                super.click();
                helpwnd.show();
            }
        };
        help.settip(Resource.remote().loadwait("nurgling/hud/buttons/search/u").flayer(Resource.tooltip).t);
        save = new IButton(ssearchbi[0], ssearchbi[1], ssearchbi[2])
        {
            @Override
            public void click() {
                if(!searchF.text().isEmpty()) {
                    createHistoryItem(searchF.text());
                    write();
                    super.click();
                }else {
                    NUtils.getGameUI().error("Input field is empty");
                }
            }
        };
        save.settip(Resource.remote().loadwait("nurgling/hud/buttons/ssearch/u").flayer(Resource.tooltip).t);
        list = new ICheckBox(lsearchbi[0], lsearchbi[1], lsearchbi[2], lsearchbi[3])
        {
            @Override
            public void changed(boolean val) {
                super.changed(val);
            }
        };
        list.settip(Resource.remote().loadwait("nurgling/hud/buttons/lsearch/u").flayer(Resource.tooltip).t);
        tpos_y = searchF.sz.y / 2 - help.sz.y / 2;
        add(help, new Coord(0, tpos_y));
        add(save, new Coord(0, tpos_y));
        add(list, new Coord(0, tpos_y));
        add(searchF, new Coord(help.sz.x + UI.scale(5), 0));
        helpwnd = new Window(new Coord(UI.scale(200), UI.scale(500)), "Help: search") {
            @Override
            public void draw(GOut g) {
                super.draw(g);
                if (helpLayer != null)
                    g.aimage(helpLayer, ca().ul, 0, 0);

            }

            @Override
            public void resize(Coord sz) {
                super.resize(sz);
                if (helpLayer != null)
                    sz = new Coord(helpLayer.sz().x, helpLayer.sz().y);
            }

            @Override
            public void wdgmsg(Widget sender, String msg, Object... args) {
                if (sender == helpwnd) {
                    helpwnd.hide();
                }
            }

        };
        NUtils.getGameUI().add(helpwnd);

        initHelp();
        helpwnd.hide();
        history = NUtils.getGameUI().add(new NPopupWidget(new Coord(UI.scale(200), UI.scale(150)), NPopupWidget.Type.TOP));

        history.pack();
        cmdList = history.add(new CmdList(UI.scale(250, 200)),history.atl);
        read();
    }

    @Override
    public void resize(Coord sz) {
        searchF.resize(sz.x - UI.scale(5) * 3 - help.sz.x * 3);
        this.sz.y = searchF.sz.y;
        this.sz.x = sz.x;
        save.move(new Coord(sz.x - save.sz.x, tpos_y));
        list.move(new Coord(sz.x - save.sz.x - UI.scale(5) - list.sz.x, tpos_y));
        history.resize(new Coord(searchF.sz.x+UI.scale(12), UI.scale(150)));
        cmdList.resize(new Coord(0, UI.scale(120)));
    }

    TexI helpLayer;

    void initHelp() {
        ArrayList<BufferedImage> imgs = new ArrayList<>();
        String[] src = Resource.remote().loadwait("nurgling/hud/wnd/search").flayer(Resource.tooltip).t.split("\\|");
        for (String s : src)
            if (s.contains("$") && !s.contains("$col")) {
                imgs.add(nfnd.render(s).img);
            } else {
                imgs.add(RichText.render(s, 0).img);
            }

        helpLayer = new TexI(catimgs(5, imgs.toArray(new BufferedImage[0])));
        helpwnd.resize(new Coord(helpLayer.sz()));
    }

    @Override
    public void tick(double dt) {
        super.tick(dt);
        history.visible = parent.visible && list.a;
    }
    String path = ((HashDirCache) ResCache.global).base + "\\..\\" +"searchcmd.dat";
    void read() {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                String line = reader.readLine();
                createHistoryItem(line);
            }
            reader.close();
        } catch (IOException ignored) {
        }
    }

    void write() {
        try (OutputStreamWriter file = new OutputStreamWriter(Files.newOutputStream(Paths.get(path)), StandardCharsets.UTF_8)) {
            for(String key : cmdHistory.keySet())
                file.write(key+"\n");
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ConcurrentHashMap<String,CmdItem> cmdHistory = new ConcurrentHashMap<>();
    private int cmdHistoryId = 0;
    public void createHistoryItem(String text)
    {
        CmdItem l = new CmdItem(text);
        cmdHistory.put(text,l);
        cmdList.makeitem(l, cmdHistoryId++, new Coord(40, 20));
    }
    private static final Text.Foundry elf = CharWnd.attrf;
    private static final int elh = elf.height() + UI.scale(2);



    public class CmdItem extends Widget{
        Label text;
        IButton remove;

        @Override
        public void resize(Coord sz) {
            remove.move(new Coord(sz.x - NStyle.removei[0].sz().x - UI.scale(5),  remove.c.y));
            super.resize(sz);
        }

        public CmdItem(String text){
            this.text = add(new Label(text));
            remove = add(new IButton(NStyle.removei[0].back,NStyle.removei[1].back,NStyle.removei[2].back){
                @Override
                public void click() {
                    cmdHistory.remove(text);
                    write();
                }
            },this.text.pos("ur").add(UI.scale(5),UI.scale(1) ));
            remove.settip(Resource.remote().loadwait("nurgling/hud/buttons/removeItem/u").flayer(Resource.tooltip).t);

            pack();
        }
    }


    public class CmdList extends SListBox<CmdItem, Widget> {
        CmdList(Coord sz) {
            super(sz, elh);
        }

        protected List<CmdItem> items() {return(new ArrayList<>(cmdHistory.values()));}


        @Override
        public void resize(Coord sz) {
            super.resize(new Coord(searchF.sz.x-UI.scale(6), sz.y));
        }

        protected Widget makeitem(CmdItem item, int idx, Coord sz) {
            return(new ItemWidget<CmdItem>(this, sz, item) {
                {
                    item.resize(new Coord(searchF.sz.x - NStyle.removei[0].sz().x  + UI.scale(4), item.sz.y));
                    add(item);
                }

                @Override
                public boolean mousedown(MouseDownEvent ev) {
                    boolean psel = sel == item;
                    super.mousedown(ev);
                    if(!psel) {
                        String value = item.text.text();
                        searchF.settext(value);
                        NUtils.getGameUI().itemsForSearch.install(value);
                    }
                    return(true);
                }
            });
        }
    }
}

