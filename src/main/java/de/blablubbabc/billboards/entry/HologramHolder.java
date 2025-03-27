package de.blablubbabc.billboards.entry;

import org.holoeasy.hologram.Hologram;
import org.holoeasy.line.ILine;
import org.holoeasy.line.ITextLine;
import org.holoeasy.line.TextLine;

import java.util.ArrayList;
import java.util.List;

public class HologramHolder {
    public final String id;
    public Hologram hologram;
    public HologramHolder(String id) {
        this.id = id;
    }

    public List<String> getLines() {
        Hologram holo = hologram;
        List<String> holoLines = new ArrayList<>();
        for (ILine<?> l : holo.getLines()) {
            if (l instanceof ITextLine) {
                TextLine line = ((ITextLine) l).getTextLine();
                holoLines.add(line.getObj());
                if (holoLines.size() >= 4) break;
            }
        }
        return holoLines;
    }
}
