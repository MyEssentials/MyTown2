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
    List<Flag> getFlags();

    /**
     * Gets the flag with the type specified
     *
     * @param type
     * @return
     */
    Flag getFlag(FlagType type);

}
