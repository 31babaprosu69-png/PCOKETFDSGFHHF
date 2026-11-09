package com.pocketdimensions.item;

import com.pocketdimensions.dimension.ModDimensions;
import com.pocketdimensions.dimension.PocketData;
import com.pocketdimensions.dimension.PocketSavedData;
import com.pocketdimensions.network.ModNetworking;
import com.pocketdimensions.network.packet.PacketOpenSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PocketKeyItem extends Item {
    public static final int COOLDOWN_TICKS = 800; // 40 seconds

    public PocketKeyItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                PocketSavedData sd = PocketSavedData.get(sp.server);
                PocketData pd = sd.getOrCreatePocket(sp);

                ModNetworking.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        new PacketOpenSettings(
                                pd.getOwnerUUID(),
                                pd.getOwnerName(),
                                pd.isChunkLoaderActive(),
                                pd.getSkyTheme(),
                                pd.getWeatherVisual(),
                                pd.isPublic(),
                                new ArrayList<>(pd.getAllowedPlayerNames()),
                                sp.hasPermissions(2)
                        )
                );
            }
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
        }

        if (level.dimension().equals(ModDimensions.POCKET_LEVEL)) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                PocketSavedData sd = PocketSavedData.get(sp.server);
                PocketData pd = sd.getPocketByOwner(sp.getUUID());
                if (pd == null) {
                    pd = sd.getPocketAt(sp.chunkPosition().x, sp.chunkPosition().z);
                }

                if (pd != null) {
                    PocketSavedData.teleportOut(sp, pd, sp.server);
                } else {
                    ServerLevel ow = sp.server.overworld();
                    sp.teleportTo(ow, ow.getSharedSpawnPos().getX() + 0.5, ow.getSharedSpawnPos().getY() + 1.0, ow.getSharedSpawnPos().getZ() + 0.5, 0.0f, 0.0f);
                    sp.sendSystemMessage(Component.translatable("message.pocketdimensions.leaving"));
                }
            }
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("message.pocketdimensions.recharging", 40));
            }
            return InteractionResultHolder.fail(held);
        }

        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            PocketSavedData sd = PocketSavedData.get(sp.server);
            PocketData pd = sd.getOrCreatePocket(sp);

            pd.setReturnLocation(sp.level().dimension(), sp.getX(), sp.getY(), sp.getZ(), sp.getYRot(), sp.getXRot());
            sd.setDirty();

            ServerLevel pl = sp.server.getLevel(ModDimensions.POCKET_LEVEL);
            if (pl != null) {
                sp.teleportTo(pl, pd.spawnBlockX() + 0.5, pd.spawnBlockY(), pd.spawnBlockZ() + 0.5, 0.0f, 0.0f);
                sp.fallDistance = 0.0f;
                sp.setDeltaMovement(Vec3.ZERO);
                sp.sendSystemMessage(Component.translatable("message.pocketdimensions.entering"));

                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            }
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.pocketdimensions.pocket_key.desc").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• Right-Click: Enter / Exit Dimension").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("• Shift + Right-Click: Settings GUI").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("• Soulbound to UUID (Loss-Safe)").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
