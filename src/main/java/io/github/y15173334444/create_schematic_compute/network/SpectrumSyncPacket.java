package io.github.y15173334444.create_schematic_compute.network;

import io.github.y15173334444.create_schematic_compute.SchematicCompute;
import io.github.y15173334444.create_schematic_compute.blocks.AmplifierComputerBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端→客户端：功放电脑屏幕的频谱柱电平（{@link io.github.y15173334444.create_schematic_compute.graph.AudioSpectrum#BARS}
 * 个字节，0..255）。
 * <p>发往追踪玩家、20 Hz（只在电平变化时发包，静默后自然停发）；客户端只做「上一刻/本刻」
 * 两帧缓存的插值显示，不参与取数。</p>
 * <p>Server→client: the amplifier screen's spectrum bar levels (one byte per bar, 0..255), sent
 * to tracking players at 20 Hz (only on change, so a silent song stops the traffic); the client
 * keeps just the previous/current frames for interpolation and does no analysis.</p>
 */
public record SpectrumSyncPacket(BlockPos pos, byte[] levels) implements CustomPacketPayload {

    public static final Type<SpectrumSyncPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(SchematicCompute.MOD_ID, "spectrum_sync"));

    public static final StreamCodec<ByteBuf, SpectrumSyncPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SpectrumSyncPacket::pos,
        ByteBufCodecs.BYTE_ARRAY, SpectrumSyncPacket::levels,
        SpectrumSyncPacket::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player().level().getBlockEntity(pos) instanceof AmplifierComputerBlockEntity abe) {
                abe.acceptSpectrum(levels);
            }
        });
    }
}
