package mytown.protection.segment;

import com.google.gson.internal.LazilyParsedNumber;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.Result;
import myessentials.entities.Volume;
import mytown.entities.Resident;
import mytown.util.exceptions.GetterException;

/**
 * Segment that protects against an Event
 */
public class SegmentEvent extends Segment {
    protected Result result = Result.DEFAULT;

    public boolean check(Event ev) {
        Resident owner = getOwner(ev);
        int range = getRange(ev);
        int dim = getDim(ev);
        int x = getX(ev);
        int y = getY(ev);
        int z = getZ(ev);
        boolean canceled = false;

        if (range == 0) {
            canceled = !hasPermissionAtLocation(owner, dim, x, y, z);
        } else {
            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
            canceled = !hasPermissionAtLocation(owner, dim, rangeBox);
        }

        if (canceled) {
            if (ev.isCancelable()) ev.setCanceled(true);
            if (result != Result.DEFAULT && ev.hasResult()) ev.setResult(result);
        }

        return canceled;
    }

    private int getInt(Event event, String getter, int def) {
        if (!getters.contains(getter)) {
            return def;
        }
        try {
            Object intObj = null;
            intObj = getters.get(getter).invoke(Object.class, event, event);
            if (intObj instanceof LazilyParsedNumber) {
                return ((LazilyParsedNumber)intObj).intValue();
            } else if (intObj instanceof Double) {
                return (int)((Double)intObj + 0.5);
            } else if (intObj instanceof Integer) {
                return (Integer)intObj;
            }
        } catch (GetterException ex) {
        }
        return def;
    }

    private int getX(Event event) {
        return getInt(event, "x", 0);
    }

    private int getY(Event event) {
        return getInt(event, "y", 0);
    }

    private int getZ(Event event) {
        return getInt(event, "z", 0);
    }

    private int getDim(Event event) {
        return getInt(event, "dim", 0);
    }
}
