package xyz.nucleoid.disguiselib.impl.mixin.accessor;

import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.world.ServerChunkLoadingManager$EntityTracker")
public interface EntityTrackerEntryAccessor {
    @Accessor("entry")
    EntityTrackerEntry getEntry();

    @Accessor("listeners")
    Set<PlayerAssociatedNetworkHandler> getListeners();
}
