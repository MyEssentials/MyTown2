package mytown.protection.mixin;

import myessentials.entities.BlockPos;
import mytown.entities.Resident;
import mytown.new_datasource.MyTownUniverse;
import mytown.protection.ProtectionManager;
import mytown.protection.segment.SegmentItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.PacketPartPlacement;

/**
 * Mixin which demonstrates the use of the {@link ModifyArg} and {@link Redirect} annotations.
 */
@Mixin(PacketPartPlacement.class)
public abstract class MixinPacketPartPlacement {
	@Shadow
	private int x;
	
	@Shadow
	private int y;

	@Shadow
	private int z;

	@Shadow
	private int face;

	@Shadow
    float eyeHeight;

    /**
     * <p>{@link Redirect} annotations allow a method call to be proxied or even completely suppressed by redirecting the original method call to the
     * annotated method.</p>
     *
     * <p>In this example, the {@link MobSpawnerBaseLogic#resetTimer} method is hooked and redirected to this handler. The signature of the hook
     * method must match the redirected method precisely with the addition of a new first argument which must match the type of the invocation's
     * target, in this case {@link MobSpawnerBaseLogic}. This first variable accepts the reference that the method was going to be invoked upon prior
     * to being redirected.</p>
     *
     * <p>The benefit with {@link Redirect} versus ordinary method call injections, is that the call to the method can be conditionally suppressed if
     * required, and also allows a more sophisticated version of {@link ModifyArg} to be enacted since all parameters are available to the hook method
     * and can be altered as required.</p>
     *
     * <p>For <em>static</em> methods the handler must also be <em>static</em>, and the first argument can be omitted.</p>
     *
     * @param this$0 this$0
     */
    @Redirect(method = "serverPacketData()V", at = @At(value = "INVOKE", target = "Lappeng.core.sync.packets.PacketPartPlacement;serverPacketData()V"))
    private void onServerPacketData(PacketPartPlacement this$0, INetworkInfo manager, AppEngPacket packet, EntityPlayer player) {
        Resident res = MyTownUniverse.instance.getOrMakeResident(player);
        ItemStack stack = player.getHeldItem();
        BlockPos bp = new BlockPos(x, y, z, player.dimension);

        for(SegmentItem segment : ProtectionManager.segmentsItem.get(stack.getItem().getClass())) {
            if(!segment.shouldInteract(stack, res, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, bp, face)) {
                return;
            }
        }
        this.serverPacketData(manager, packet, player);
    }

    @Shadow
    private void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player) {
    }

    ;
}
