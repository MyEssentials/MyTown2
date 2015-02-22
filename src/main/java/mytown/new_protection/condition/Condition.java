package mytown.new_protection.condition;

import mytown.new_protection.segment.Getter;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/9/2015.
 * The condition that needs to be met when checking a segment from a protection.
 */
public class Condition {

    private String conditionString;

    public Condition(String conditionString) {
        this.conditionString = conditionString;
    }
}
