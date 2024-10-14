package nurgling.widgets.bots;

import haven.*;
import haven.Button;
import haven.Label;
import haven.Window;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoordinateRecorder extends Window implements Checkable {

    private List<Coord2d> coordinates = new ArrayList<>();
    private Widget prev;
    private Button recordButton;
    private Button removeLastButton;
    private Button saveButton;
    private Listbox<Coord2d> coordList;
    private Coord2d startingCoord;
    private TextEntry nameField;

    public CoordinateRecorder() {
        super(new Coord(200, 240), "Coordinate Recorder");

        prev = add(new Label("Recorded Coordinates:"));

        coordList = new Listbox<Coord2d>(160, 5, UI.scale(16)) {
            @Override
            protected Coord2d listitem(int i) {
                return coordinates.get(i);
            }

            @Override
            protected int listitems() {
                return coordinates.size();
            }

            @Override
            protected void drawitem(GOut g, Coord2d c, int i) {
                g.text(c.toString(), Coord.z);
            }
        };
        prev = add(coordList, prev.pos("bl").add(UI.scale(0, 5)));

        recordButton = new Button(UI.scale(70), "Record") {
            @Override
            public void click() {
                if(startingCoord == null){
                    startingCoord = ui.gui.map.player().rc;
                }else {
                    coordinates.add(ui.gui.map.player().rc.sub(startingCoord));
                    coordList.sb.val = coordinates.size() - 1;
                }
            }
        };
        prev = add(recordButton, prev.pos("bl").add(UI.scale(0, 5)));

        removeLastButton = new Button(UI.scale(80), "Remove Last") {
            @Override
            public void click() {
                if (!coordinates.isEmpty()) {
                    coordinates.remove(coordinates.size() - 1);
                    coordList.sb.val = Math.max(0, coordinates.size() - 1);
                }
            }
        };
        add(removeLastButton, prev.pos("ur").add(UI.scale(5, 0)));

        prev = add(new Label("Filename:"), prev.pos("bl").add(UI.scale(0, 10)));

        nameField = new TextEntry(160, "") {
            @Override
            public void activate(String text) {
                saveCoordinates();
            }
        };
        prev = add(nameField, prev.pos("bl").add(UI.scale(0, 2)));

        saveButton = new Button(UI.scale(70), "Save") {
            @Override
            public void click() {
                saveCoordinates();
            }
        };
        add(saveButton, prev.pos("bl").add(UI.scale(0, 5)));

        pack();
    }

    private void saveCoordinates() {
        String name = nameField.text().trim();
        if (!name.isEmpty() && !coordinates.isEmpty()) {
            try {
                // Create a directory to store coordinate files if it doesn't exist
                File dir = new File("paths_coord");
                if (!dir.exists()) {
                    dir.mkdir();
                }

                File file = new File(dir, name + ".json");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("[");

                    for (int i = 0; i < coordinates.size(); i++) {
                        writer.write(coordinates.get(i).toString());
                        if (i < coordinates.size() - 1) {
                            writer.write(",");
                        }
                    }

                    writer.write("]");
                }

                ui.msg("Coordinates saved successfully!");
            } catch (IOException e) {
                ui.msg("Error saving coordinates: " + e.getMessage());
            }
        } else {
            ui.msg("Please enter a name and record at least one coordinate.");
        }
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void wdgmsg(String msg, Object... args) {
        if (msg.equals("close")) {
            hide();
        }
        super.wdgmsg(msg, args);
    }
}