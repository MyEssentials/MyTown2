package mytown.new_protection.segment;

import mytown.MyTown;
import mytown.entities.flag.FlagType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * A part of the protection that protects against a specific thing.
 */
public class Segment {
    public Class<?> theClass;
    public FlagType flag;
    public Map<String, List<Getter>> extraGettersMap;

    public Segment(Class<?> theClass, Map<String, List<Getter>> extraGettersMap) {
        this.theClass = theClass;
        this.extraGettersMap = extraGettersMap;
    }

    /**
     * From a list of Getters it tries to get an integer from the specified object
     *
     * @param getterList
     * @return
     */
    public Object getInfoFromGetters(List<Getter> getterList, Object object, Class<?> type) {
        Object lastInstance = object;
        try {
            for (Getter getter : getterList) {
                switch (getter.type) {
                    case field:
                        Field fieldObject = lastInstance.getClass().getField(getter.element);
                        fieldObject.setAccessible(true);
                        lastInstance = fieldObject.get(lastInstance);
                        break;
                    case method:
                        Method methodObject = lastInstance.getClass().getDeclaredMethod(getter.element);
                        methodObject.setAccessible(true);
                        lastInstance = methodObject.invoke(lastInstance);
                        break;
                }
            }
            if(!type.isAssignableFrom(lastInstance.getClass()))
                throw new RuntimeException("[Segment: "+ this.theClass.getName() +"] Got wrong type of class at a getter! Expected: " + type.getName());
            return lastInstance;
        } catch(NoSuchFieldException nfex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.getName() +"] Encountered a problem when getting a field from " + object.toString());
            nfex.printStackTrace();
        } catch (IllegalAccessException iaex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.getName() +"] This type of thing should not happen.");
            iaex.printStackTrace();
        } catch (NoSuchMethodException nmex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.getName() +"] Encountered a problem when getting a method from " + object.toString());
            nmex.printStackTrace();
        } catch (InvocationTargetException itex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.getName() +"] The returned object was not of the expected type!");
            itex.printStackTrace();
        }
        return null;
    }
}
