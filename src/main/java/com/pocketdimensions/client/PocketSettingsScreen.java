package com.pocketdimensions.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pocketdimensions.PocketDimensionsMod;
import com.pocketdimensions.network.ModNetworking;
import com.pocketdimensions.network.packet.PacketDeletePocket;
import com.pocketdimensions.network.packet.PacketUpdateSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PocketSettingsScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(PocketDimensionsMod.MODID, "textures/gui/pocket_settings.png");
    private final UUID ownerUUID;
    private final String ownerName;
    private boolean chunkLoaderActive, isPublic;
    private int skyTheme, weatherVisual;
    private final List<String> allowedNames;
    private final boolean isOP;

    private boolean deleteConfirming = false;
    private int deleteTicksRemaining = 200; // 10s
    private Button deleteBtn, cancelDeleteBtn, chunkLoaderBtn, skyThemeBtn, weatherBtn, accessBtn, addFriendBtn;
    private EditBox friendInput;
    private static final String[] THEMES = {"Starry Midnight", "Ethereal Aurora", "Twilight Sunset", "Mystic Abyss", "Solar Dawn"};
    private static final String[] WEATHERS = {"Clear Sky", "Gentle Rain", "Cozy Snowfall"};

    public PocketSettingsScreen(UUID u, String n, boolean c, int s, int w, boolean pub, List<String> al, boolean op) {
        super(Component.translatable("gui.pocketdimensions.title"));
        this.ownerUUID = u;
        this.ownerName = n;
        this.chunkLoaderActive = c;
        this.skyTheme = s;
        this.weatherVisual = w;
        this.isPublic = pub;
        this.allowedNames = new ArrayList<>(al);
        this.isOP = op;
    }

    @Override
    protected void init() {
        int left = (this.width - 220) / 2, top = (this.height - 190) / 2;

        chunkLoaderBtn = Button.builder(Component.literal("Chunk Loader: " + (chunkLoaderActive ? "§aENABLED" : "§cDISABLED")), b -> {
            chunkLoaderActive = !chunkLoaderActive;
            b.setMessage(Component.literal("Chunk Loader: " + (chunkLoaderActive ? "§aENABLED" : "§cDISABLED")));
            save("", "");
        }).bounds(left + 15, top + 32, 190, 20).build();
        addRenderableWidget(chunkLoaderBtn);

        skyThemeBtn = Button.builder(Component.literal("Sky Theme: §b" + THEMES[skyTheme]), b -> {
            skyTheme = (skyTheme + 1) % THEMES.length;
            b.setMessage(Component.literal("Sky Theme: §b" + THEMES[skyTheme]));
            PocketClientVisuals.setVisuals(skyTheme, weatherVisual);
            save("", "");
        }).bounds(left + 15, top + 56, 190, 20).build();
        addRenderableWidget(skyThemeBtn);

        weatherBtn = Button.builder(Component.literal("Weather: §e" + WEATHERS[weatherVisual]), b -> {
            weatherVisual = (weatherVisual + 1) % WEATHERS.length;
            b.setMessage(Component.literal("Weather: §e" + WEATHERS[weatherVisual]));
            PocketClientVisuals.setVisuals(skyTheme, weatherVisual);
            save("", "");
        }).bounds(left + 15, top + 80, 190, 20).build();
        addRenderableWidget(weatherBtn);

        accessBtn = Button.builder(Component.literal("Access: " + (isPublic ? "§aPUBLIC" : "§6WHITELIST ONLY")), b -> {
            isPublic = !isPublic;
            b.setMessage(Component.literal("Access: " + (isPublic ? "§aPUBLIC" : "§6WHITELIST ONLY")));
            save("", "");
        }).bounds(left + 15, top + 104, 190, 20).build();
        addRenderableWidget(accessBtn);

        friendInput = new EditBox(this.font, left + 15, top + 128, 120, 18, Component.literal("Player Name"));
        addRenderableWidget(friendInput);

        addFriendBtn = Button.builder(Component.literal("Add Friend"), b -> {
            String nm = friendInput.getValue().trim();
            if (!nm.isEmpty() && !allowedNames.contains(nm.toLowerCase())) {
                allowedNames.add(nm.toLowerCase());
                save(nm, "");
                friendInput.setValue("");
            }
        }).bounds(left + 140, top + 127, 65, 20).build();
        addRenderableWidget(addFriendBtn);

        deleteBtn = Button.builder(Component.literal("§cDelete Dimension"), b -> {
            if (!deleteConfirming) {
                deleteConfirming = true;
                deleteTicksRemaining = 200;
                cancelDeleteBtn.visible = true;
            } else if (deleteTicksRemaining <= 0) {
                ModNetworking.CHANNEL.sendToServer(new PacketDeletePocket(ownerUUID));
                onClose();
            }
        }).bounds(left + 15, top + 155, 130, 20).build();
        addRenderableWidget(deleteBtn);

        cancelDeleteBtn = Button.builder(Component.literal("Cancel"), b -> {
            deleteConfirming = false;
            b.visible = false;
            deleteBtn.setMessage(Component.literal("§cDelete Dimension"));
        }).bounds(left + 150, top + 155, 55, 20).build();
        cancelDeleteBtn.visible = false;
        addRenderableWidget(cancelDeleteBtn);
    }

    @Override
    public void tick() {
        super.tick();
        if (deleteConfirming && deleteTicksRemaining > 0) {
            deleteTicksRemaining--;
            int sec = (int) Math.ceil(deleteTicksRemaining / 20.0);
            deleteBtn.setMessage(deleteTicksRemaining > 0 ? Component.literal("§eWait (" + sec + "s)...") : Component.literal("§4§lCONFIRM DELETE§r"));
        }
    }

    private void save(String add, String rem) {
        ModNetworking.CHANNEL.sendToServer(new PacketUpdateSettings(ownerUUID, chunkLoaderActive, skyTheme, weatherVisual, isPublic, add, rem));
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int left = (this.width - 220) / 2, top = (this.height - 190) / 2;
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        g.blit(GUI_TEXTURE, left, top, 0, 0, 220, 190);
        g.drawCenteredString(this.font, Component.literal("§6§lPocket Dimension Settings§r"), this.width / 2, top + 8, 0xFFFFFF);
        g.drawString(this.font, Component.literal(isOP ? "§7Managing: §f" + ownerName + " §e[OP]" : "§7Owner: §f" + ownerName), left + 16, top + 20, 0xDDDDDD);
        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
