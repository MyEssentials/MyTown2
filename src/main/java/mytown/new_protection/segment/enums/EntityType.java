package mytown.new_protection.segment.enums;

/**
 * Created by AfterWind on 1/3/2015.
 * The type of entity we are protecting against
 */
public enum EntityType {
    /**
     * Anything that can kill the player without him attacking it.
     */
    hostile,
    /**
     * Anything that has some kind of value of having and needs to bne protected from right/left clicking or death by other players.
     */
    passive,
    /**
     * Anything that explodes.
     */
    explosive,
    /**
     * Anything that can be fired by a player and attack another and it's trespassing the pvp flag.
     * This does not apply to things like Arrows, Snowballs etc.
     */
    pvp
}
