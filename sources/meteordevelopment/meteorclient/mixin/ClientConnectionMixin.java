package meteordevelopment.meteorclient.mixin;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.TimeoutException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.ServerConnectEndEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.proxies.Proxy;
import net.minecraft.class_2535;
import net.minecraft.class_2548;
import net.minecraft.class_2596;
import net.minecraft.class_2598;
import net.minecraft.class_2602;
import net.minecraft.class_7648;
import net.minecraft.class_8042;
import net.minecraft.class_8762;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/mixin/ClientConnectionMixin.class */
@Mixin({class_2535.class})
public abstract class ClientConnectionMixin {
    @Inject(method = {"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V", shift = At.Shift.BEFORE)}, cancellable = true)
    private void onHandlePacket(ChannelHandlerContext channelHandlerContext, class_2596<?> packet, CallbackInfo ci) {
        if (packet instanceof class_8042) {
            class_8042 bundle = (class_8042) packet;
            Iterator<class_2596<? super class_2602>> it = bundle.method_48324().iterator();
            while (it.hasNext()) {
                if (((PacketEvent.Receive) MeteorClient.EVENT_BUS.post(new PacketEvent.Receive(it.next(), (class_2535) this))).isCancelled()) {
                    it.remove();
                }
            }
            return;
        }
        if (((PacketEvent.Receive) MeteorClient.EVENT_BUS.post(new PacketEvent.Receive(packet, (class_2535) this))).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = {"connect(Ljava/net/InetSocketAddress;ZLnet/minecraft/network/ClientConnection;)Lio/netty/channel/ChannelFuture;"}, at = {@At("HEAD")})
    private static void onConnect(InetSocketAddress address, boolean useEpoll, class_2535 connection, CallbackInfoReturnable<?> cir) {
        MeteorClient.EVENT_BUS.post(ServerConnectEndEvent.get(address));
    }

    @Inject(at = {@At("HEAD")}, method = {"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"}, cancellable = true)
    private void onSendPacketHead(class_2596<?> packet, class_7648 callbacks, CallbackInfo ci) {
        if (((PacketEvent.Send) MeteorClient.EVENT_BUS.post(new PacketEvent.Send(packet, (class_2535) this))).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = {"send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"}, at = {@At("TAIL")})
    private void onSendPacketTail(class_2596<?> packet, @Nullable class_7648 callbacks, CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(new PacketEvent.Sent(packet, (class_2535) this));
    }

    @Inject(method = {"exceptionCaught"}, at = {@At("HEAD")}, cancellable = true)
    private void exceptionCaught(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        AntiPacketKick apk = (AntiPacketKick) Modules.get().get(AntiPacketKick.class);
        if (!(throwable instanceof TimeoutException) && !(throwable instanceof class_2548) && apk.catchExceptions()) {
            if (apk.logExceptions.get().booleanValue()) {
                apk.warning("Caught exception: %s", throwable);
            }
            ci.cancel();
        }
    }

    @Inject(method = {"addHandlers"}, at = {@At("RETURN")})
    private static void onAddHandlers(ChannelPipeline pipeline, class_2598 side, boolean local, class_8762 packetSizeLogger, CallbackInfo ci) {
        Proxy proxy;
        if (side == class_2598.field_11942 && (proxy = Proxies.get().getEnabled()) != null) {
            switch (proxy.type.get()) {
                case Socks4:
                    pipeline.addFirst(new ChannelHandler[]{new Socks4ProxyHandler(new InetSocketAddress(proxy.address.get(), proxy.port.get().intValue()), proxy.username.get())});
                    break;
                case Socks5:
                    pipeline.addFirst(new ChannelHandler[]{new Socks5ProxyHandler(new InetSocketAddress(proxy.address.get(), proxy.port.get().intValue()), proxy.username.get(), proxy.password.get())});
                    break;
            }
        }
    }
}
