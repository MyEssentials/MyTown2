package mytown.protection.segment.enums;

/**
 * Created by AfterWind on 1/3/2015.
 * The type of entity we are protecting against
 */
public enum EntityType {
    /**
     * Anything that is checked every tick, in order to prevent griefing through entities.
     */
    tracked,
    /**
     * Anything that has some kind of value of having and needs to bne protected from right/left clicking or death by other players.
     */
    protect,
    /**
     * Anything that can be fired by a player and attack another and it's trespassing the pvp flag.
     * This does not apply to things like Arrows, Snowballs etc.
     */
    pvp
}
