package mytown.entities.interfaces;

import com.google.common.collect.ImmutableList;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;

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
     * Checks if there is a flag with the name given
     *
     * @return
     */
    boolean hasFlag(FlagType type);

    /**
     * Gets the immutable list of flags
     *
     * @return
     */
    ImmutableList<Flag> getFlags();

    /**
     * Gets the flag with the name specified
     *
     * @param name
     * @return
     */
    Flag getFlag(FlagType type);

}
