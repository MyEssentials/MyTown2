package mytown.protection.segment.caller;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;

public class CallerNBT extends Caller {
    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        if(instance instanceof Entity) {
            NBTTagCompound nbt = ((Entity) instance).getEntityData();
            return nbt.getTag(name);
        } else if(instance instanceof TileEntity) {
            NBTTagCompound nbt = new NBTTagCompound();
            ((TileEntity) instance).writeToNBT(nbt);
            return nbt.getTag(name);
        } else if(instance instanceof Item) {
            return ((ItemStack)parameters[0]).getTagCompound().getTag(name);
        } else if(instance instanceof NBTTagCompound) {
            instance = ((NBTTagCompound) instance).getTag(name);
            return getInfoFromNBTBase((NBTBase)instance);
        } else if(instance instanceof NBTTagList) {
            int id = Integer.parseInt(name);

            if(id < 0 || id >= ((NBTTagList) instance).tagCount()) {
                throw new IndexOutOfBoundsException("ID is out of bounds for NBTTagList");
            }

            return ((NBTTagList) instance).getCompoundTagAt(id);
        }
        return null;
    }

    private Object getInfoFromNBTBase(NBTBase instance) {
        if(instance instanceof NBTTagDouble) {
            return ((NBTTagDouble) instance).func_150286_g();
        } else if(instance instanceof NBTTagFloat) {
            return ((NBTTagFloat) instance).func_150288_h();
        } else if(instance instanceof NBTTagInt) {
            return ((NBTTagInt) instance).func_150287_d();
        } else if(instance instanceof NBTTagString) {
            return ((NBTTagString) instance).func_150285_a_();
        }
        return null;
    }
}
