package mytown.api.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

import java.util.List;

/**
 * Created by AfterWind on 8/26/2014.
 * Why do we need interfaces for this?
 */
public interface IHasFlags {
    /**
     * Adds a flag to the list
     *
     * @param flag
     */
    void addFlag(Flag flag);

    /**
     * Checks if there is a flag with the type given
     *
     * @return
     */
    boolean hasFlag(FlagType type);

    /**
     * Gets the list of flags
     *
     * @return
     */
    ImmutableList<Flag> getFlags();

    /**
     * Gets the flag with the type specified
     *
     * @param type
     * @return
     */
    Flag getFlag(FlagType type);

    /**
     * Removes the flag
     *
     * @param type
     * @return
     */
    boolean removeFlag(FlagType type);

    /**
     * Gets the value of the specified flag
     *
     * @param type
     * @return
     */
    Object getValue(FlagType type);

    /**
     * Gets the value of the specified flag at the coordinates
     *
     * @param dim
     * @param x
     * @param y
     * @param z
     * @param type
     * @return
     */
    Object getValueAtCoords(int dim, int x, int y, int z, FlagType type);

}
