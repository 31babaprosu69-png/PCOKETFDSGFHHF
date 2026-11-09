package com.pocketdimensions.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pocketdimensions.dimension.ModDimensions;
import com.pocketdimensions.dimension.PocketData;
import com.pocketdimensions.dimension.PocketSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PocketCommands {
    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("pocket")
            .then(Commands.literal("tp").requires(s -> s.hasPermission(2)).then(Commands.argument("target", EntityArgument.player()).executes(ctx -> {
                ServerPlayer t = EntityArgument.getPlayer(ctx, "target");
                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                PocketData pd = PocketSavedData.get(sp.server).getPocketByOwner(t.getUUID());
                if (pd == null) {
                    ctx.getSource().sendFailure(Component.literal("No pocket found for player."));
                    return 0;
                }
                ServerLevel pl = sp.server.getLevel(ModDimensions.POCKET_LEVEL);
                if (pl != null) {
                    sp.teleportTo(pl, pd.spawnBlockX() + 0.5, pd.spawnBlockY(), pd.spawnBlockZ() + 0.5, 0, 0);
                    sp.sendSystemMessage(Component.literal("Teleported to " + t.getScoreboardName() + "'s pocket dimension."));
                }
                return 1;
            })))
            .then(Commands.literal("visit").then(Commands.argument("owner", StringArgumentType.word()).executes(ctx -> {
                String o = StringArgumentType.getString(ctx, "owner");
                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                PocketData pd = PocketSavedData.get(sp.server).getPocketByOwnerName(o);
                if (pd == null) {
                    ctx.getSource().sendFailure(Component.literal("No pocket found for " + o));
                    return 0;
                }
                if (!pd.isAllowed(sp)) {
                    ctx.getSource().sendFailure(Component.literal("No permission to visit " + o));
                    return 0;
                }
                ServerLevel pl = sp.server.getLevel(ModDimensions.POCKET_LEVEL);
                if (pl != null) {
                    sp.teleportTo(pl, pd.spawnBlockX() + 0.5, pd.spawnBlockY(), pd.spawnBlockZ() + 0.5, 0, 0);
                    sp.sendSystemMessage(Component.literal("Visiting " + o + "'s pocket dimension!"));
                }
                return 1;
            })))
            .then(Commands.literal("leave").executes(ctx -> {
                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                if (!sp.level().dimension().equals(ModDimensions.POCKET_LEVEL)) {
                    ctx.getSource().sendFailure(Component.literal("Not in a pocket dimension."));
                    return 0;
                }
                PocketSavedData sd = PocketSavedData.get(sp.server);
                PocketData pd = sd.getPocketAt(sp.chunkPosition().x, sp.chunkPosition().z);
                if (pd == null) pd = sd.getPocketByOwner(sp.getUUID());
                if (pd != null) {
                    PocketSavedData.teleportOut(sp, pd, sp.server);
                }
                return 1;
            }))
            .then(Commands.literal("invite").then(Commands.argument("friend", StringArgumentType.word()).executes(ctx -> {
                String f = StringArgumentType.getString(ctx, "friend");
                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                PocketSavedData sd = PocketSavedData.get(sp.server);
                PocketData pd = sd.getOrCreatePocket(sp);
                pd.addAllowedPlayer(f);
                sd.setDirty();
                ctx.getSource().sendSuccess(() -> Component.literal("Invited " + f), true);
                return 1;
            })))
            .then(Commands.literal("uninvite").then(Commands.argument("friend", StringArgumentType.word()).executes(ctx -> {
                String f = StringArgumentType.getString(ctx, "friend");
                ServerPlayer sp = ctx.getSource().getPlayerOrException();
                PocketSavedData sd = PocketSavedData.get(sp.server);
                PocketData pd = sd.getPocketByOwner(sp.getUUID());
                if (pd != null) {
                    pd.removeAllowedPlayer(f);
                    sd.setDirty();
                    ctx.getSource().sendSuccess(() -> Component.literal("Uninvited " + f), true);
                }
                return 1;
            })))
            .then(Commands.literal("chunkloader").requires(s -> s.hasPermission(2)).then(Commands.argument("owner", StringArgumentType.word()).then(Commands.argument("enable", BoolArgumentType.bool()).executes(ctx -> {
                String o = StringArgumentType.getString(ctx, "owner");
                boolean en = BoolArgumentType.getBool(ctx, "enable");
                PocketSavedData sd = PocketSavedData.get(ctx.getSource().getServer());
                PocketData pd = sd.getPocketByOwnerName(o);
                if (pd != null) {
                    sd.setChunkLoader(pd.getOwnerUUID(), en, ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(() -> Component.literal("Set chunkloader for " + o + " to " + en), true);
                    return 1;
                }
                ctx.getSource().sendFailure(Component.literal("Pocket not found."));
                return 0;
            }))))
            .then(Commands.literal("list").requires(s -> s.hasPermission(2)).executes(ctx -> {
                for (PocketData p : PocketSavedData.get(ctx.getSource().getServer()).getAllPockets()) {
                    ctx.getSource().sendSuccess(() -> Component.literal("- " + p.getOwnerName() + " (ChunkLoader: " + (p.isChunkLoaderActive() ? "ON" : "OFF") + ")"), false);
                }
                return 1;
            }))
        );
    }
}
