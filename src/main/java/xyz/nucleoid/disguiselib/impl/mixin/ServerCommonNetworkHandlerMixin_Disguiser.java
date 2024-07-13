package xyz.nucleoid.disguiselib.impl.mixin;

import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.disguiselib.impl.packets.ExtendedHandler;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin_Disguiser {
    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Unique
    private boolean disguiselib$skipCheck;

    /**
     * Checks the packet that was sent. If the entity in the packet is disguised, the
     * entity type / id in the packet will be changed.
     * <p>
     * As minecraft client doesn't allow moving if you send it an entity with the same
     * id as player, we send the disguised player another entity, so they will see their
     * own disguise.
     *
     * @param packet packet being sent
     */
    @Inject(
            method = "send",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V"
            ),
            cancellable = true
    )
    private void disguiseEntity(Packet<ClientPlayPacketListener> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        if (!this.disguiselib$skipCheck) {
            if (!(this instanceof ExtendedHandler self)) {
                return;
            }
            if (packet instanceof BundleS2CPacket bundleS2CPacket) {
                if (bundleS2CPacket.getPackets() instanceof ArrayList<Packet<? super ClientPlayPacketListener>> list) {
                    var list2 = new ArrayList<Packet<? super ClientPlayPacketListener>>();
                    var adder = new ArrayList<Packet<ClientPlayPacketListener>>();
                    var atomic = new AtomicBoolean(true);
                    for (var packet2 : list) {
                        atomic.set(true);
                        adder.clear();
                        self.disguiselib$transformPacket(packet2, () -> atomic.set(false), list2::add);

                        if (atomic.get()) {
                            list2.add(packet2);
                        }

                        list2.addAll(adder);
                    }

                    list.clear();
                    list.addAll(list2);
                }
            } else {
                this.disguiselib$skipCheck = true;
                self.disguiselib$transformPacket(packet, ci::cancel, this::sendPacket);
                this.disguiselib$skipCheck = false;
            }
        }
    }

    @Inject(method = "onCustomPayload", at = @At("TAIL"))
    private void onClientBrand(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof BrandCustomPayload && this instanceof ExtendedHandler self) {
            self.disguiselib$onClientBrand();
        }
    }
}
