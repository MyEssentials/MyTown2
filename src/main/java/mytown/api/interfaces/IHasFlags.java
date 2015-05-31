package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

/**
 * Represents an object that can hold Flags
 */
public interface IHasFlags {

    void addFlag(Flag flag);

    boolean hasFlag(FlagType type);

    ImmutableList<Flag> getFlags();

    Flag getFlag(FlagType type);

    boolean removeFlag(FlagType type);

    Object getValue(FlagType type);

    /**
     * Gets the value of the specified flag at the coordinates
     */
    Object getValueAtCoords(int dim, int x, int y, int z, FlagType type);

}
