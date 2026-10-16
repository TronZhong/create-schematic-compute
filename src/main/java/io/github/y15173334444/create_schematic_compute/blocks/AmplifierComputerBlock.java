package io.github.y15173334444.create_schematic_compute.blocks;

import io.github.y15173334444.create_schematic_compute.SchematicCompute;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * 功放电脑方块：图宿主（内置 NBS 音乐编辑器 + 音频频道发布）。走 GraphBlock 继承线
 * （ProgramComputer 同款骨架）。
 * <p>Amplifier computer block: a graph host (built-in NBS music editor + audio channel
 * publish). Same skeleton as {@link ProgramComputerBlock}.</p>
 */
public class AmplifierComputerBlock extends BaseEntityBlock implements IWrenchable {
    public static final MapCodec<AmplifierComputerBlock> CODEC = simpleCodec(AmplifierComputerBlock::new);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** 碰撞体 = 底座静态体（机箱 Y0–3 + 键盘面 Y3–4 + 蓝件），X/Z 满幅 0–16。
     *  与朝向、开合都**刻意无关**：底座四向对称；开合不进碰撞，免得图启停时把站在机器上的
     *  实体顶起来。顶面 0.25 格 → 玩家自动上台阶走到机器上，开盖的盖子当装饰穿人。
     *  Collision = the static base slab (chassis + keyboard deck + brackets), full 0–16
     *  footprint, independent of facing AND of LIT on purpose: the base is four-way
     *  symmetric, and keeping the lid out of collision avoids shoving entities that stand
     *  on the machine when the graph starts/stops. A 0.25-block top lets players step up
     *  onto it; the open lid is decoration and is walked through. */
    private static final VoxelShape COLLISION = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

    /** 轮廓 / 瞄准靶 = 整机包络（单盒、与朝向无关）：底座 + 开盖盖子（旋转后 AABB 到 Y13.24 / Z16.75）。
     *  **必须含盖子区域** —— 屏幕长在盖子上，而玩家瞄准/右击走的是 {@code getShape}
     *  （{@code ClipContext.Block.OUTLINE}）：只给底座会让点屏幕点不到方块、GUI 打不开。
     *  Outline / aim target = the whole-machine envelope (single box, facing-independent):
     *  base plus the open lid's rotated AABB. It MUST cover the lid — the screen lives on it
     *  and block targeting runs through {@code getShape}; a base-only shape would leave the
     *  screen unclickable and the GUI unopenable. */
    private static final VoxelShape OUTLINE = Block.box(0.0, 0.0, 0.0, 16.0, 13.25, 16.75);

    public AmplifierComputerBlock(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(FACING, Direction.NORTH));
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { super.createBlockStateDefinition(b); b.add(LIT, FACING); }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AmplifierComputerBlockEntity(pos, state); }
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        if (l.isClientSide()) return null;
        return createTickerHelper(t, SchematicCompute.AMPLIFIER_COMPUTER_BE.get(), (lv, p, st, be) -> be.tick());
    }
    @Override protected RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return OUTLINE; }
    @Override protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return COLLISION; }
    @Override
    protected InteractionResult useWithoutItem(BlockState s, Level l, BlockPos p, Player pl, BlockHitResult h) {
        if (l.isClientSide()) {
            if (l.getBlockEntity(p) instanceof AmplifierComputerBlockEntity) openScreen(p);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
    private static void openScreen(BlockPos p) {
        net.minecraft.client.Minecraft.getInstance().setScreen(new AmplifierComputerScreen(p));
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, be, player, context.getItemInHand());
        if (be != null) {
            for (ItemStack stack : drops) {
                if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                    be.saveToItem(stack, level.registryAccess());
                    break;
                }
            }
        }
        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                if (player != null) player.getInventory().placeItemBackInInventory(stack);
                else Block.popResource(level, pos, stack);
            }
        }
        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
        level.destroyBlock(pos, false);
        IWrenchable.playRemoveSound(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.SUCCESS;
    }
}
