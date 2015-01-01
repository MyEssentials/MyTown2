package mytown.new_protection.segment;

import mytown.MyTown;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by AfterWind on 1/1/2015.
 * Segment that protects against a TileEntity
 */
public class SegmentTileEntity extends Segment implements IBlockModifier {

    public List<Getter>[] getters;
    public Shape shape;

    public SegmentTileEntity(Class<?> theClass, List<Getter>[] getters, Shape shape) {
        // List 0 = x1, List 1 = y1 etc...
        super(theClass);
        this.getters = getters;
        this.shape = shape;
    }

    /**
     * From a list of Getters it tries to get an integer from the specified object
     *
     * @param getterList
     * @param lastInstance
     * @return
     */
    public int getIntFromGetters(List<Getter> getterList, Object lastInstance) {
        Object theObject = lastInstance;
        try {
            for (Getter getter : getterList) {
                switch (getter.type) {
                    case fieldInt:
                        Field fieldInt = lastInstance.getClass().getField(getter.element);
                        fieldInt.setAccessible(true);
                        return fieldInt.getInt(lastInstance);
                    case fieldObject:
                        Field fieldObject = lastInstance.getClass().getField(getter.element);
                        fieldObject.setAccessible(true);
                        lastInstance = fieldObject.get(lastInstance);
                        break;
                    case methodInt:
                        Method methodInt = lastInstance.getClass().getDeclaredMethod(getter.element);
                        methodInt.setAccessible(true);
                        return (Integer)methodInt.invoke(lastInstance);
                    case methodObject:
                        Method methodObject = lastInstance.getClass().getDeclaredMethod(getter.element);
                        methodObject.setAccessible(true);
                        lastInstance = methodObject.invoke(lastInstance);
                        break;
                }
            }
        } catch(NoSuchFieldException nfex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.toString() +"] Encountered a problem when getting a field from " + theObject.toString());
            nfex.printStackTrace();
        } catch (IllegalAccessException iaex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.toString() +"] This type of thing should not happen.");
            iaex.printStackTrace();
        } catch (NoSuchMethodException nmex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.toString() +"] Encountered a problem when getting a method from " + theObject.toString());
            nmex.printStackTrace();
        } catch (InvocationTargetException itex) {
            MyTown.instance.log.error("[Segment:"+ this.theClass.toString() +"] The returned object was not of the expected type!");
            itex.printStackTrace();
        }
        throw new RuntimeException("Failed to get integer for object " + theObject.toString());
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public int getX1(TileEntity te) {
        return getIntFromGetters(getters[0], te);
    }

    @Override
    public int getZ1(TileEntity te) {
        return getIntFromGetters(getters[1], te);
    }

    @Override
    public int getX2(TileEntity te) {
        return getIntFromGetters(getters[2], te);
    }


    @Override
    public int getZ2(TileEntity te) {
        return getIntFromGetters(getters[3], te);
    }
}
