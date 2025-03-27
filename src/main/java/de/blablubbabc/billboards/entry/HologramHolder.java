package de.blablubbabc.billboards.entry;

import de.blablubbabc.billboards.listener.HologramInteraction;
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

    public void setLines(List<String> linesStr) {
        Hologram holo = hologram;
        List<ILine<?>> lines = new ArrayList<>();
        for (String s : linesStr) {
            lines.add(createLine(this, s));
        }
        holo.load(lines.toArray(new ILine[0]));
    }

    public static ILine<?> createLine(HologramHolder holo, String text, Object... args) {
        TextLine textLine = new TextLine(holo.hologram.getPlugin(), text, args, true);
        textLine.setClickEvent(player -> HologramInteraction.clickHologram(player, holo));
        return textLine;
    }
}
