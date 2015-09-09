package mytown.api.container;

import mytown.protection.segment.getter.Getter;

import java.util.ArrayList;
import java.util.Iterator;

public class GettersContainer extends ArrayList<Getter> {

    public Getter get(String getterName) {
        for(Getter getter : this) {
            if(getter.getName().equals(getterName)) {
                return getter;
            }
        }
        return null;
    }

    public boolean remove(String getterName) {
        for(Iterator<Getter> it = iterator(); it.hasNext(); ) {
            Getter getter = it.next();
            if(getter.getName().equals(getterName)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean contains(String getterName) {
        for(Getter getter : this) {
            if(getter.getName().equals(getterName)) {
                return true;
            }
        }
        return false;
    }

}
