package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.RenderBlockEntityEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.events.world.ParticleEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.ParticleTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_2185;
import net.minecraft.class_2248;
import net.minecraft.class_2343;
import net.minecraft.class_2396;
import net.minecraft.class_2398;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/NoRender.class */
public class NoRender extends Module {
    private final SettingGroup sgOverlay;
    private final SettingGroup sgHUD;
    private final SettingGroup sgWorld;
    private final SettingGroup sgEntity;
    private final Setting<Boolean> noPortalOverlay;
    private final Setting<Boolean> noSpyglassOverlay;
    private final Setting<Boolean> noNausea;
    private final Setting<Boolean> noPumpkinOverlay;
    private final Setting<Boolean> noPowderedSnowOverlay;
    private final Setting<Boolean> noFireOverlay;
    private final Setting<Boolean> noLiquidOverlay;
    private final Setting<Boolean> noInWallOverlay;
    private final Setting<Boolean> noVignette;
    private final Setting<Boolean> noGuiBackground;
    private final Setting<Boolean> noTotemAnimation;
    private final Setting<Boolean> noEatParticles;
    private final Setting<Boolean> noHurtCam;
    private final Setting<Boolean> noEnchantGlint;
    private final Setting<Boolean> noBossBar;
    private final Setting<Boolean> noScoreboard;
    private final Setting<Boolean> noCrosshair;
    private final Setting<Boolean> noTitle;
    private final Setting<Boolean> noHeldItemName;
    private final Setting<Boolean> noObfuscation;
    private final Setting<Boolean> noPotionIcons;
    private final Setting<Boolean> noMessageSignatureIndicator;
    private final Setting<Boolean> noWeather;
    private final Setting<Boolean> noBlindness;
    private final Setting<Boolean> noDarkness;
    private final Setting<Boolean> noFog;
    private final Setting<Boolean> noEnchTableBook;
    private final Setting<Boolean> noSignText;
    private final Setting<Boolean> noBlockBreakParticles;
    private final Setting<Boolean> noBlockBreakOverlay;
    private final Setting<Boolean> noSkylightUpdates;
    private final Setting<Boolean> noBeaconBeams;
    private final Setting<Boolean> noFallingBlocks;
    private final Setting<Boolean> noCaveCulling;
    private final Setting<Boolean> noMapMarkers;
    private final Setting<Boolean> noMapContents;
    private final Setting<BannerRenderMode> bannerRender;
    private final Setting<Boolean> noFireworkExplosions;
    private final Setting<List<class_2396<?>>> particles;
    private final Setting<Boolean> noBarrierInvis;
    private final Setting<Boolean> noTextureRotations;
    private final Setting<List<class_2248>> blockEntities;
    private final Setting<Set<class_1299<?>>> entities;
    private final Setting<Boolean> dropSpawnPacket;
    private final Setting<Boolean> noArmor;
    private final Setting<Boolean> noInvisibility;
    private final Setting<Boolean> noGlowing;
    private final Setting<Boolean> noMobInSpawner;
    private final Setting<Boolean> noDeadEntities;
    private final Setting<Boolean> noNametags;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/render/NoRender$BannerRenderMode.class */
    public enum BannerRenderMode {
        Everything,
        Pillar,
        None
    }

    public NoRender() {
        super(Categories.Render, "no-render", "Disables certain animations or overlays from rendering.");
        this.sgOverlay = this.settings.createGroup("Overlay");
        this.sgHUD = this.settings.createGroup("HUD");
        this.sgWorld = this.settings.createGroup("World");
        this.sgEntity = this.settings.createGroup("Entity");
        this.noPortalOverlay = this.sgOverlay.add(new BoolSetting.Builder().name("portal-overlay").description("Disables rendering of the nether portal overlay.").defaultValue(false).build());
        this.noSpyglassOverlay = this.sgOverlay.add(new BoolSetting.Builder().name("spyglass-overlay").description("Disables rendering of the spyglass overlay.").defaultValue(false).build());
        this.noNausea = this.sgOverlay.add(new BoolSetting.Builder().name("nausea").description("Disables rendering of the nausea overlay.").defaultValue(false).build());
        this.noPumpkinOverlay = this.sgOverlay.add(new BoolSetting.Builder().name("pumpkin-overlay").description("Disables rendering of the pumpkin head overlay").defaultValue(false).build());
        this.noPowderedSnowOverlay = this.sgOverlay.add(new BoolSetting.Builder().name("powdered-snow-overlay").description("Disables rendering of the powdered snow overlay.").defaultValue(false).build());
        this.noFireOverlay = this.sgOverlay.add(new BoolSetting.Builder().name("fire-overlay").description("Disables rendering of the fire overlay.").defaultValue(false).build());
        this.noLiquidOverlay = this.sgOverlay.add(new BoolSetting.Builder().name("liquid-overlay").description("Disables rendering of the liquid overlay.").defaultValue(false).build());
        this.noInWallOverlay = this.sgOverlay.add(new BoolSetting.Builder().name("in-wall-overlay").description("Disables rendering of the overlay when inside blocks.").defaultValue(false).build());
        this.noVignette = this.sgOverlay.add(new BoolSetting.Builder().name("vignette").description("Disables rendering of the vignette overlay.").defaultValue(false).build());
        this.noGuiBackground = this.sgOverlay.add(new BoolSetting.Builder().name("gui-background").description("Disables rendering of the GUI background overlay.").defaultValue(false).build());
        this.noTotemAnimation = this.sgOverlay.add(new BoolSetting.Builder().name("totem-animation").description("Disables rendering of the totem animation when you pop a totem.").defaultValue(false).build());
        this.noEatParticles = this.sgOverlay.add(new BoolSetting.Builder().name("eating-particles").description("Disables rendering of eating particles.").defaultValue(false).build());
        this.noHurtCam = this.sgOverlay.add(new BoolSetting.Builder().name("hurt-cam").description("Disables camera shake when taking damage.").defaultValue(false).build());
        this.noEnchantGlint = this.sgOverlay.add(new BoolSetting.Builder().name("enchantment-glint").description("Disables rending of the enchantment glint.").defaultValue(false).build());
        this.noBossBar = this.sgHUD.add(new BoolSetting.Builder().name("boss-bar").description("Disable rendering of boss bars.").defaultValue(false).build());
        this.noScoreboard = this.sgHUD.add(new BoolSetting.Builder().name("scoreboard").description("Disable rendering of the scoreboard.").defaultValue(false).build());
        this.noCrosshair = this.sgHUD.add(new BoolSetting.Builder().name("crosshair").description("Disables rendering of the crosshair.").defaultValue(false).build());
        this.noTitle = this.sgHUD.add(new BoolSetting.Builder().name("title").description("Disables rendering of the title.").defaultValue(false).build());
        this.noHeldItemName = this.sgHUD.add(new BoolSetting.Builder().name("held-item-name").description("Disables rendering of the held item name.").defaultValue(false).build());
        this.noObfuscation = this.sgHUD.add(new BoolSetting.Builder().name("obfuscation").description("Disables obfuscation styling of characters.").defaultValue(false).build());
        this.noPotionIcons = this.sgHUD.add(new BoolSetting.Builder().name("potion-icons").description("Disables rendering of status effect icons.").defaultValue(false).build());
        this.noMessageSignatureIndicator = this.sgHUD.add(new BoolSetting.Builder().name("message-signature-indicator").description("Disables chat message signature indicator on the left of the message.").defaultValue(false).build());
        this.noWeather = this.sgWorld.add(new BoolSetting.Builder().name("weather").description("Disables rendering of weather.").defaultValue(false).build());
        this.noBlindness = this.sgWorld.add(new BoolSetting.Builder().name("blindness").description("Disables rendering of blindness.").defaultValue(false).build());
        this.noDarkness = this.sgWorld.add(new BoolSetting.Builder().name("darkness").description("Disables rendering of darkness.").defaultValue(false).build());
        this.noFog = this.sgWorld.add(new BoolSetting.Builder().name("fog").description("Disables rendering of fog.").defaultValue(false).build());
        this.noEnchTableBook = this.sgWorld.add(new BoolSetting.Builder().name("enchantment-table-book").description("Disables rendering of books above enchanting tables.").defaultValue(false).build());
        this.noSignText = this.sgWorld.add(new BoolSetting.Builder().name("sign-text").description("Disables rendering of text on signs.").defaultValue(false).build());
        this.noBlockBreakParticles = this.sgWorld.add(new BoolSetting.Builder().name("block-break-particles").description("Disables rendering of block-break particles.").defaultValue(false).build());
        this.noBlockBreakOverlay = this.sgWorld.add(new BoolSetting.Builder().name("block-break-overlay").description("Disables rendering of block-break overlay.").defaultValue(false).build());
        this.noSkylightUpdates = this.sgWorld.add(new BoolSetting.Builder().name("skylight-updates").description("Disables rendering of skylight updates.").defaultValue(false).build());
        this.noBeaconBeams = this.sgWorld.add(new BoolSetting.Builder().name("beacon-beams").description("Disables rendering of beacon beams.").defaultValue(false).build());
        this.noFallingBlocks = this.sgWorld.add(new BoolSetting.Builder().name("falling-blocks").description("Disables rendering of falling blocks.").defaultValue(false).build());
        this.noCaveCulling = this.sgWorld.add(new BoolSetting.Builder().name("cave-culling").description("Disables Minecraft's cave culling algorithm.").defaultValue(false).onChanged(b -> {
            this.mc.field_1769.method_3279();
        }).build());
        this.noMapMarkers = this.sgWorld.add(new BoolSetting.Builder().name("map-markers").description("Disables markers on maps.").defaultValue(false).build());
        this.noMapContents = this.sgWorld.add(new BoolSetting.Builder().name("map-contents").description("Disable rendering of maps.").defaultValue(false).build());
        this.bannerRender = this.sgWorld.add(new EnumSetting.Builder().name("banners").description("Changes rendering of banners.").defaultValue(BannerRenderMode.Everything).build());
        this.noFireworkExplosions = this.sgWorld.add(new BoolSetting.Builder().name("firework-explosions").description("Disables rendering of firework explosions.").defaultValue(false).build());
        this.particles = this.sgWorld.add(new ParticleTypeListSetting.Builder().name("particles").description("Particles to not render.").build());
        this.noBarrierInvis = this.sgWorld.add(new BoolSetting.Builder().name("barrier-invisibility").description("Disables barriers being invisible when not holding one.").defaultValue(false).build());
        this.noTextureRotations = this.sgWorld.add(new BoolSetting.Builder().name("texture-rotations").description("Changes texture rotations and model offsets to use a constant value instead of the block position.").defaultValue(false).onChanged(b2 -> {
            this.mc.field_1769.method_3279();
        }).build());
        this.blockEntities = this.sgWorld.add(new BlockListSetting.Builder().name("block-entities").description("Block entities (chest, shulker block, etc.) to not render.").filter(block -> {
            return (block instanceof class_2343) && !(block instanceof class_2185);
        }).build());
        this.entities = this.sgEntity.add(new EntityTypeListSetting.Builder().name("entities").description("Disables rendering of selected entities.").build());
        this.dropSpawnPacket = this.sgEntity.add(new BoolSetting.Builder().name("drop-spawn-packets").description("WARNING! Drops all spawn packets of entities selected in the above list.").defaultValue(false).build());
        this.noArmor = this.sgEntity.add(new BoolSetting.Builder().name("armor").description("Disables rendering of armor on entities.").defaultValue(false).build());
        this.noInvisibility = this.sgEntity.add(new BoolSetting.Builder().name("invisibility").description("Shows invisible entities.").defaultValue(false).build());
        this.noGlowing = this.sgEntity.add(new BoolSetting.Builder().name("glowing").description("Disables rendering of the glowing effect").defaultValue(false).build());
        this.noMobInSpawner = this.sgEntity.add(new BoolSetting.Builder().name("spawner-entities").description("Disables rendering of spinning mobs inside of mob spawners").defaultValue(false).build());
        this.noDeadEntities = this.sgEntity.add(new BoolSetting.Builder().name("dead-entities").description("Disables rendering of dead entities").defaultValue(false).build());
        this.noNametags = this.sgEntity.add(new BoolSetting.Builder().name("nametags").description("Disables rendering of entity nametags").defaultValue(false).build());
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onActivate() {
        if (this.noCaveCulling.get().booleanValue() || this.noTextureRotations.get().booleanValue()) {
            this.mc.field_1769.method_3279();
        }
    }

    @Override // meteordevelopment.meteorclient.systems.modules.Module
    public void onDeactivate() {
        if (this.noCaveCulling.get().booleanValue() || this.noTextureRotations.get().booleanValue()) {
            this.mc.field_1769.method_3279();
        }
    }

    public boolean noPortalOverlay() {
        return isActive() && this.noPortalOverlay.get().booleanValue();
    }

    public boolean noSpyglassOverlay() {
        return isActive() && this.noSpyglassOverlay.get().booleanValue();
    }

    public boolean noNausea() {
        return isActive() && this.noNausea.get().booleanValue();
    }

    public boolean noPumpkinOverlay() {
        return isActive() && this.noPumpkinOverlay.get().booleanValue();
    }

    public boolean noFireOverlay() {
        return isActive() && this.noFireOverlay.get().booleanValue();
    }

    public boolean noLiquidOverlay() {
        return isActive() && this.noLiquidOverlay.get().booleanValue();
    }

    public boolean noPowderedSnowOverlay() {
        return isActive() && this.noPowderedSnowOverlay.get().booleanValue();
    }

    public boolean noInWallOverlay() {
        return isActive() && this.noInWallOverlay.get().booleanValue();
    }

    public boolean noVignette() {
        return isActive() && this.noVignette.get().booleanValue();
    }

    public boolean noGuiBackground() {
        return isActive() && this.noGuiBackground.get().booleanValue();
    }

    public boolean noTotemAnimation() {
        return isActive() && this.noTotemAnimation.get().booleanValue();
    }

    public boolean noEatParticles() {
        return isActive() && this.noEatParticles.get().booleanValue();
    }

    public boolean noHurtCam() {
        return isActive() && this.noHurtCam.get().booleanValue();
    }

    public boolean noEnchantGlint() {
        return isActive() && this.noEnchantGlint.get().booleanValue();
    }

    public boolean noBossBar() {
        return isActive() && this.noBossBar.get().booleanValue();
    }

    public boolean noScoreboard() {
        return isActive() && this.noScoreboard.get().booleanValue();
    }

    public boolean noCrosshair() {
        return isActive() && this.noCrosshair.get().booleanValue();
    }

    public boolean noTitle() {
        return isActive() && this.noTitle.get().booleanValue();
    }

    public boolean noHeldItemName() {
        return isActive() && this.noHeldItemName.get().booleanValue();
    }

    public boolean noObfuscation() {
        return isActive() && this.noObfuscation.get().booleanValue();
    }

    public boolean noPotionIcons() {
        return isActive() && this.noPotionIcons.get().booleanValue();
    }

    public boolean noMessageSignatureIndicator() {
        return isActive() && this.noMessageSignatureIndicator.get().booleanValue();
    }

    public boolean noWeather() {
        return isActive() && this.noWeather.get().booleanValue();
    }

    public boolean noBlindness() {
        return isActive() && this.noBlindness.get().booleanValue();
    }

    public boolean noDarkness() {
        return isActive() && this.noDarkness.get().booleanValue();
    }

    public boolean noFog() {
        return isActive() && this.noFog.get().booleanValue();
    }

    public boolean noEnchTableBook() {
        return isActive() && this.noEnchTableBook.get().booleanValue();
    }

    public boolean noSignText() {
        return isActive() && this.noSignText.get().booleanValue();
    }

    public boolean noBlockBreakParticles() {
        return isActive() && this.noBlockBreakParticles.get().booleanValue();
    }

    public boolean noBlockBreakOverlay() {
        return isActive() && this.noBlockBreakOverlay.get().booleanValue();
    }

    public boolean noSkylightUpdates() {
        return isActive() && this.noSkylightUpdates.get().booleanValue();
    }

    public boolean noBeaconBeams() {
        return isActive() && this.noBeaconBeams.get().booleanValue();
    }

    public boolean noFallingBlocks() {
        return isActive() && this.noFallingBlocks.get().booleanValue();
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        if (this.noCaveCulling.get().booleanValue()) {
            event.cancel();
        }
    }

    public boolean noMapMarkers() {
        return isActive() && this.noMapMarkers.get().booleanValue();
    }

    public boolean noMapContents() {
        return isActive() && this.noMapContents.get().booleanValue();
    }

    public BannerRenderMode getBannerRenderMode() {
        return !isActive() ? BannerRenderMode.Everything : this.bannerRender.get();
    }

    public boolean noFireworkExplosions() {
        return isActive() && this.noFireworkExplosions.get().booleanValue();
    }

    @EventHandler
    private void onAddParticle(ParticleEvent event) {
        if (!this.noWeather.get().booleanValue() || event.particle.method_10295() != class_2398.field_11242) {
            if (!this.noFireworkExplosions.get().booleanValue() || event.particle.method_10295() != class_2398.field_11248) {
                if (this.particles.get().contains(event.particle.method_10295())) {
                    event.cancel();
                    return;
                }
                return;
            }
            event.cancel();
            return;
        }
        event.cancel();
    }

    public boolean noBarrierInvis() {
        return isActive() && this.noBarrierInvis.get().booleanValue();
    }

    public boolean noTextureRotations() {
        return isActive() && this.noTextureRotations.get().booleanValue();
    }

    @EventHandler
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        if (this.blockEntities.get().contains(event.blockEntity.method_11010().method_26204())) {
            event.cancel();
        }
    }

    public boolean noEntity(class_1297 entity) {
        return isActive() && this.entities.get().contains(entity.method_5864());
    }

    public boolean noEntity(class_1299<?> entity) {
        return isActive() && this.entities.get().contains(entity);
    }

    public boolean getDropSpawnPacket() {
        return isActive() && this.dropSpawnPacket.get().booleanValue();
    }

    public boolean noArmor() {
        return isActive() && this.noArmor.get().booleanValue();
    }

    public boolean noInvisibility() {
        return isActive() && this.noInvisibility.get().booleanValue();
    }

    public boolean noGlowing() {
        return isActive() && this.noGlowing.get().booleanValue();
    }

    public boolean noMobInSpawner() {
        return isActive() && this.noMobInSpawner.get().booleanValue();
    }

    public boolean noDeadEntities() {
        return isActive() && this.noDeadEntities.get().booleanValue();
    }

    public boolean noNametags() {
        return isActive() && this.noNametags.get().booleanValue();
    }

    public boolean noWorldBorder() {
        return false;
    }
}
