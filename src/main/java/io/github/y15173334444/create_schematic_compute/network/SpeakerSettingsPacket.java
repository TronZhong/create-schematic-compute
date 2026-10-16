package io.github.y15173334444.create_schematic_compute.network;

import io.github.y15173334444.create_schematic_compute.SchematicCompute;
import io.github.y15173334444.create_schematic_compute.blocks.SpeakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 音响设置（C→S）：增益/半径/静音。应用到 {@link SpeakerBlockEntity}。
 * 图不在这里动——图内节点由玩家在编辑器里摆放（旧 band 字段随「空图种默认图」一并移除，
 * 2026-10-11）。
 * <p>Speaker settings (client→server): gain / radius / mute, applied to the
 * {@link SpeakerBlockEntity}. The graph is untouched — its nodes are authored by the player in
 * the editor (the old band field went with the empty-graph default seeding, removed 2026-10-11).</p>
 */
public record SpeakerSettingsPacket(BlockPos pos, float gain, int radius, boolean mute)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SpeakerSettingsPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SchematicCompute.MOD_ID, "speaker_settings"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeakerSettingsPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, SpeakerSettingsPacket::pos,
        ByteBufCodecs.FLOAT, SpeakerSettingsPacket::gain,
        ByteBufCodecs.VAR_INT, SpeakerSettingsPacket::radius,
        ByteBufCodecs.BOOL, SpeakerSettingsPacket::mute,
        SpeakerSettingsPacket::new);

    @Override public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (!(sp.level() instanceof ServerLevel sl)) return;
            if (!SablePacketHelper.isWithinReachableRange(sp, pos, 16384.0)) return;
            if (sl.getBlockEntity(pos) instanceof SpeakerBlockEntity be) {
                be.applySettings(gain, radius, mute);
            }
        });
    }
}
