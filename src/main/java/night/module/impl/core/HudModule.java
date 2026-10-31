package night.module.impl.core;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render2DEvent;
import night.events.impl.SettingChangeEvent;
import night.events.impl.TickEvent;
import night.gui.HudEditorScreen;
import night.manager.FontManager;
import night.manager.HudElementManager;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.Setting;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.PositionSetting;
import night.setting.impl.StringSetting;
import night.util.animation.Animation;
import night.util.animation.EasingUtil;
import night.util.color.ColorUtil;
import night.util.color.TextFormattingUtil;
import night.util.input.KeyNameUtil;
import night.util.render.Render2DUtil;
import night.util.world.BlockInteractionUtil;
import night.util.world.EntityUtil;
import org.joml.Matrix3x2fStack;

@ModuleInfo(
   name = "HUD",
   description = "Renders information about the game and the client on the screen.",
   category = Module.Category.CORE,
   enabled = true,
   drawn = false
)
public class HudModule extends Module {
   private static final Identifier bi = Identifier.fromNamespaceAndPath("night", "textures/gui/logo.png");
   private static final int bj = 64;
   public CategorySetting watermarkCategory = new CategorySetting("Watermark", "The settings for the client's watermark.");
   public BooleanSetting watermark = new BooleanSetting(
      "Watermark", "Enabled", "Renders the client's name and version at the top left.", new CategorySetting.CategoryCondition(this.watermarkCategory), true
   );
   public StringSetting watermarkText = new StringSetting(
      "WatermarkText", "Component", "The client name that will be rendered.", new CategorySetting.CategoryCondition(this.watermarkCategory), "Night"
   );
   public BooleanSetting watermarkLogo = new BooleanSetting(
      "WatermarkLogo",
      "Logo",
      "Renders the client's logo before the watermark text, scaled to the text's own line height.",
      new CategorySetting.CategoryCondition(this.watermarkCategory),
      true
   );
   public BooleanSetting watermarkVersion = new BooleanSetting(
      "WatermarkVersion", "Version", "Renders the client's version after the name.", new CategorySetting.CategoryCondition(this.watermarkCategory), true
   );
   public BooleanSetting watermarkMinecraftVersion = new BooleanSetting(
      "WatermarkMinecraftVersion",
      "MinecraftVersion",
      "Renders the client's minecraft version after the version.",
      new CategorySetting.CategoryCondition(this.watermarkCategory),
      false
   );
   public ModeSetting watermarkVersionMode = new ModeSetting(
      "WatermarkVersionMode",
      "Revision",
      "What to render next to the version: the git revision/hash, or a nightly-build date stamp.",
      new BooleanSetting.BooleanCondition(this.watermarkVersion, true),
      "GitHash",
      new String[]{"GitHash", "Nightly"}
   );
   public BooleanSetting watermarkSync = new BooleanSetting(
      "WatermarkSync", "ColorSync", "Uses the client's color for the version.", new CategorySetting.CategoryCondition(this.watermarkCategory), false
   );
   public CategorySetting welcomerCategory = new CategorySetting("Welcomer", "The settings for the client's welcomer.");
   public BooleanSetting welcomer = new BooleanSetting(
      "Welcomer", "Enabled", "Renders a nice welcome message directed to you.", new CategorySetting.CategoryCondition(this.welcomerCategory), true
   );
   public StringSetting welcomerText = new StringSetting(
      "WelcomerText", "Component", "The message that will be rendered.", new CategorySetting.CategoryCondition(this.welcomerCategory), "Hello, [username]! :^)"
   );
   public BooleanSetting welcomerSync = new BooleanSetting(
      "WelcomerSync", "ColorSync", "Uses the client's color for the username.", new CategorySetting.CategoryCondition(this.welcomerCategory), false
   );
   public CategorySetting moduleListCategory = new CategorySetting("ModuleList", "The settings for the client's list of enabled modules.");
   public BooleanSetting moduleList = new BooleanSetting(
      "ModuleList", "Enabled", "Renders every enabled module in an organized list.", new CategorySetting.CategoryCondition(this.moduleListCategory), true
   );
   public ModeSetting moduleListMode = new ModeSetting(
      "ModuleListMode",
      "Mode",
      "The display mode for the module list.",
      new CategorySetting.CategoryCondition(this.moduleListCategory),
      "Pro",
      new String[]{"Pro", "Newbie"}
   );
   public BooleanSetting metaData = new BooleanSetting(
      "MetaData", "Whether or not to show module metadata in the module list.", new CategorySetting.CategoryCondition(this.moduleListCategory), true
   );
   public BooleanSetting bindOnly = new BooleanSetting(
      "BindOnly", "Only lists modules that have a keybind set.", new CategorySetting.CategoryCondition(this.moduleListCategory), false
   );
   public ModeSetting moduleColor = new ModeSetting(
      "ModuleColor",
      "Color",
      "The color mode for the modules on the list.",
      new CategorySetting.CategoryCondition(this.moduleListCategory),
      "Default",
      new String[]{"Default", "Rainbow", "Random"}
   );
   public ModeSetting moduleListSorting = new ModeSetting(
      "ModuleListSorting",
      "Sorting",
      "The sorting for the modules on the list.",
      new CategorySetting.CategoryCondition(this.moduleListCategory),
      "Width",
      new String[]{"Width", "Alphabetical"}
   );
   public CategorySetting x = new CategorySetting("Player Radar", "The settings for the client's list of players in render distance.");
   public BooleanSetting y = new BooleanSetting(
      "PlayerRadar", "Enabled", "Renders the name of every player in render distance.", new CategorySetting.CategoryCondition(this.x), true
   );
   public NumberSetting z = new NumberSetting(
      "PlayerRadarLimit",
      "Limit",
      "The maximum amount of players that will be listed. Setting it to 0 means removing the limiter.",
      new CategorySetting.CategoryCondition(this.x),
      8,
      0,
      100
   );
   public ModeSetting playerRadarSorting = new ModeSetting(
      "PlayerRadarSorting",
      "Sorting",
      "The sorting for the players on the radar.",
      new CategorySetting.CategoryCondition(this.x),
      "Distance",
      new String[]{"None", "Distance", "Alphabetical"}
   );
   public BooleanSetting playerRadarAntiBot = new BooleanSetting(
      "PlayerRadarAntiBot", "AntiBot", "Prevents bots from being listed on the radar.", new CategorySetting.CategoryCondition(this.x), true
   );
   public BooleanSetting playerIcons = new BooleanSetting(
      "PlayerIcons", "Icons", "Renders the player's head icon next to their name.", new CategorySetting.CategoryCondition(this.x), true
   );
   public BooleanSetting playerRadarDistance = new BooleanSetting(
      "PlayerRadarDistance", "Distance", "Renders the distance between you and the player.", new CategorySetting.CategoryCondition(this.x), true
   );
   public BooleanSetting playerRadarEntityID = new BooleanSetting(
      "PlayerRadarEntityID", "EntityID", "Renders the player's entity ID.", new CategorySetting.CategoryCondition(this.x), false
   );
   public BooleanSetting playerRadarGameMode = new BooleanSetting(
      "PlayerRadarGameMode", "GameType", "Renders the player's current gamemode.", new CategorySetting.CategoryCondition(this.x), true
   );
   public BooleanSetting playerRadarPing = new BooleanSetting(
      "PlayerRadarPing", "Ping", "Renders the player's current latency.", new CategorySetting.CategoryCondition(this.x), true
   );
   public BooleanSetting playerRadarHealth = new BooleanSetting(
      "PlayerRadarHealth", "Health", "Renders the player's current health.", new CategorySetting.CategoryCondition(this.x), true
   );
   public BooleanSetting playerRadarTotems = new BooleanSetting(
      "PlayerRadarTotems", "Totems", "Renders the amount of totems that the player has popped.", new CategorySetting.CategoryCondition(this.x), true
   );
   public BooleanSetting playerRadarTurtlePot = new BooleanSetting(
      "PlayerRadarTurtlePot", "TurtlePotIndicator", "Indicates if the player has Resistance 3 or higher.", new CategorySetting.CategoryCondition(this.x), true
   );
   public CategorySetting itemsCategory = new CategorySetting("Items", "The settings for information about items in your inventory and specific item counters.");
   public BooleanSetting armor = new BooleanSetting(
      "Armor", "Renders the armor you're currently wearing and its status.", new CategorySetting.CategoryCondition(this.itemsCategory), true
   );
   public ModeSetting armorDurability = new ModeSetting(
      "ArmorDurability",
      "Durability",
      "The way that the durability will be rendered in.",
      new BooleanSetting.BooleanCondition(this.armor, true),
      "Both",
      new String[]{"None", "Bar", "Percentage", "Both"}
   );
   public BooleanSetting totemCounter = new BooleanSetting(
      "TotemCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.CategoryCondition(this.itemsCategory), true
   );
   public BooleanSetting crystalCounter = new BooleanSetting(
      "CrystalCounter", "Renders the amount of crystals that you have in your inventory.", new CategorySetting.CategoryCondition(this.itemsCategory), true
   );
   public BooleanSetting xPCounter = new BooleanSetting(
      "XPCounter", "Renders the amount of totems that you have in your inventory.", new CategorySetting.CategoryCondition(this.itemsCategory), true
   );
   public BooleanSetting counterChatOffset = new BooleanSetting(
      "CounterChatOffset",
      "ChatOffset",
      "Offsets the crystal and XP counter's positions whenever the chat is open.",
      new CategorySetting.CategoryCondition(this.itemsCategory),
      false
   );
   public BooleanSetting itemsSmooth = new BooleanSetting(
      "ItemsSmooth",
      "Smooth",
      "Smoothly animates item counts when they change (sliding/lerping).",
      new CategorySetting.CategoryCondition(this.itemsCategory),
      true
   );
   public CategorySetting inventoryCategory = new CategorySetting("Inventory", "The settings for displaying the inventory on the HUD.");
   public BooleanSetting inventory = new BooleanSetting(
      "Inventory",
      "Enabled",
      "Renders your inventory (excluding hotbar) on the screen even when closed.",
      new CategorySetting.CategoryCondition(this.inventoryCategory),
      false
   );
   public BooleanSetting inventoryBackground = new BooleanSetting(
      "InventoryBackground",
      "Background",
      "Renders a dark background behind the inventory grid.",
      new CategorySetting.CategoryCondition(this.inventoryCategory),
      true
   );
   public BooleanSetting inventorySlotBackground = new BooleanSetting(
      "InventorySlotBackground",
      "SlotBoxes",
      "Renders subtle slot boxes for each inventory slot.",
      new CategorySetting.CategoryCondition(this.inventoryCategory),
      true
   );
   public NumberSetting inventoryAlpha = new NumberSetting(
      "InventoryAlpha", "Alpha", "Background opacity (0-255).", new CategorySetting.CategoryCondition(this.inventoryCategory), 140.0, 0.0, 255.0
   );
   public PositionSetting inventoryPosition = new PositionSetting("InventoryPosition", "Drag offset for the inventory HUD element.");
   public CategorySetting informationCategory = new CategorySetting("Information", "The settings for information about the game and the client.");
   public BooleanSetting health = new BooleanSetting(
      "Health", "Renders your current health in the middle of the screen.", new CategorySetting.CategoryCondition(this.informationCategory), false
   );
   public BooleanSetting ping = new BooleanSetting(
      "Ping", "Renders your current latency to the server in milliseconds.", new CategorySetting.CategoryCondition(this.informationCategory), true
   );
   public BooleanSetting tPS = new BooleanSetting(
      "TPS", "Renders the server's current tick-rate.", new CategorySetting.CategoryCondition(this.informationCategory), true
   );
   public BooleanSetting fPS = new BooleanSetting(
      "FPS", "Renders the game's frames per second counter.", new CategorySetting.CategoryCondition(this.informationCategory), true
   );
   public BooleanSetting durability = new BooleanSetting(
      "Durability", "Renders your held item durability.", new CategorySetting.CategoryCondition(this.informationCategory), true
   );
   public ModeSetting speed = new ModeSetting(
      "Speed",
      "Renders the speed that you are currently moving at.",
      new CategorySetting.CategoryCondition(this.informationCategory),
      "Kilometers",
      new String[]{"None", "Meters", "Kilometers"}
   );
   public BooleanSetting uptime = new BooleanSetting(
      "Uptime", "Renders the uptime of the client.", new CategorySetting.CategoryCondition(this.informationCategory), false
   );
   public BooleanSetting serverBrand = new BooleanSetting(
      "ServerBrand", "Renders the brand of the server that you are currently on.", new CategorySetting.CategoryCondition(this.informationCategory), false
   );
   public BooleanSetting informationSync = new BooleanSetting(
      "InformationSync",
      "ColorSync",
      "Uses the client's color for the information elements.",
      new CategorySetting.CategoryCondition(this.informationCategory),
      true
   );
   public BooleanSetting informationChatOffset = new BooleanSetting(
      "InformationChatOffset",
      "ChatOffset",
      "Offsets the rendering when the chat is open.",
      new CategorySetting.CategoryCondition(this.informationCategory),
      true
   );
   public CategorySetting potionsCategory = new CategorySetting("Potions", "The settings for information about potion effects and their status.");
   public BooleanSetting potions = new BooleanSetting(
      "Potions", "Enabled", "Renders the name and status of every potion effect you have.", new CategorySetting.CategoryCondition(this.potionsCategory), true
   );
   public BooleanSetting potionIcons = new BooleanSetting(
      "PotionIcons",
      "Icons",
      "Whether or not to render the icons next to the potion's name.",
      new CategorySetting.CategoryCondition(this.potionsCategory),
      true
   );
   public ModeSetting potionColor = new ModeSetting(
      "PotionColor",
      "Color",
      "The color that will be used in rendering the potion's text.",
      new CategorySetting.CategoryCondition(this.potionsCategory),
      "Enhanced",
      new String[]{"Vanilla", "Enhanced", "Client"}
   );
   public ModeSetting potionSorting = new ModeSetting(
      "PotionSorting",
      "Sorting",
      "The sorting for the potion effects rendered.",
      new CategorySetting.CategoryCondition(this.potionsCategory),
      "Alphabetical",
      new String[]{"None", "Width", "Alphabetical"}
   );
   public ModeSetting vanillaPotions = new ModeSetting(
      "VanillaPotions",
      "The way that the vanilla potion icons will be handled.",
      new CategorySetting.CategoryCondition(this.potionsCategory),
      "Hide",
      new String[]{"Keep", "Move", "Hide"}
   );
   public CategorySetting positionCategory = new CategorySetting("Position", "The settings for information about your current position and velocity.");
   public BooleanSetting coordinates = new BooleanSetting(
      "Coordinates", "Renders your current coordinates.", new CategorySetting.CategoryCondition(this.positionCategory), true
   );
   public BooleanSetting netherCoordinates = new BooleanSetting(
      "NetherCoordinates", "Renders your current coordinates in the alternate dimension.", new BooleanSetting.BooleanCondition(this.coordinates, true), true
   );
   public BooleanSetting fakeCoords = new BooleanSetting(
      "FakeCoords", "Fakes your coordinates to prevent leaking.", new CategorySetting.CategoryCondition(this.positionCategory), false
   );
   public NumberSetting fakeThreshold = new NumberSetting(
      "FakeThreshold",
      "Threshold",
      "Minimum coordinate value required to start faking.",
      new BooleanSetting.BooleanCondition(this.fakeCoords, true),
      1000,
      0,
      100000
   );
   public BooleanSetting direction = new BooleanSetting(
      "Direction", "Renders the current direction that you are facing.", new CategorySetting.CategoryCondition(this.positionCategory), true
   );
   public BooleanSetting positionSync = new BooleanSetting(
      "PositionSync", "ColorSync", "Uses the client's color for the position elements.", new CategorySetting.CategoryCondition(this.positionCategory), true
   );
   public BooleanSetting positionChatOffset = new BooleanSetting(
      "PositionChatOffset", "ChatOffset", "Offsets the text when the chat is open.", new CategorySetting.CategoryCondition(this.positionCategory), true
   );
   public CategorySetting colorCategory = new CategorySetting("Color", "The settings for the coloring of the text.");
   public ModeSetting color = new ModeSetting(
      "Color",
      "The color that will be applied to the text.",
      new CategorySetting.CategoryCondition(this.colorCategory),
      "Default",
      new String[]{"Default", "Rainbow", "Wave", "Custom"}
   );
   public ColorSetting customColor = new ColorSetting(
      "CustomColor", "The color that will be used for the Custom mode.", new ModeSetting.ModeCondition(this.color, "Custom"), ColorUtil.a()
   );
   public ModeSetting rainbow = new ModeSetting(
      "Rainbow", "The mode for the HUD Rainbow.", new ModeSetting.ModeCondition(this.color, "Rainbow"), "Vertical", new String[]{"Vertical", "Horizontal"}
   );
   public NumberSetting rainbowOffset = new NumberSetting(
      "RainbowOffset", "Offset", "The offset that will be applied to the rainbow.", new ModeSetting.ModeCondition(this.color, "Rainbow", "Wave"), 10L, 1L, 50L
   );
   public BooleanSetting inversion = new BooleanSetting(
      "Inversion", "Inverts primary and secondary colors.", new CategorySetting.CategoryCondition(this.colorCategory), false
   );
   public BooleanSetting textGlow = new BooleanSetting(
      "TextGlow", "Glow", "Renders a bloom glow effect for HUD text.", new CategorySetting.CategoryCondition(this.colorCategory), false
   );
   public BooleanSetting informationElement = new BooleanSetting(
      "InformationElement", "Whether the ping/fps/tps/etc information block is shown at all. See HUDEditor.", true
   );
   public PositionSetting watermarkPosition = new PositionSetting("WatermarkPosition", "Drag offset for the watermark HUD element.");
   public PositionSetting welcomerPosition = new PositionSetting("WelcomerPosition", "Drag offset for the welcomer HUD element.");
   public PositionSetting moduleListPosition = new PositionSetting("ModuleListPosition", "Drag offset for the module list HUD element.");
   public PositionSetting playerRadarPosition = new PositionSetting("PlayerRadarPosition", "Drag offset for the player radar HUD element.");
   public PositionSetting armorPosition = new PositionSetting("ArmorPosition", "Drag offset for the armor HUD element.");
   public PositionSetting totemCounterPosition = new PositionSetting("TotemCounterPosition", "Drag offset for the totem counter HUD element.");
   public PositionSetting crystalCounterPosition = new PositionSetting("CrystalCounterPosition", "Drag offset for the crystal counter HUD element.");
   public PositionSetting xPCounterPosition = new PositionSetting("XPCounterPosition", "Drag offset for the XP counter HUD element.");
   public PositionSetting informationPosition = new PositionSetting("InformationPosition", "Drag offset for the information HUD element.");
   public PositionSetting coordinatesPosition = new PositionSetting("CoordinatesPosition", "Drag offset for the coordinates HUD element.");
   public PositionSetting potionsPosition = new PositionSetting("PotionsPosition", "Drag offset for the potions HUD element.");
   public PositionSetting musicHUDPosition = new PositionSetting("MusicHUDPosition", "Drag offset for the Music HUD element.");
   public CategorySetting musicHUDCategory = new CategorySetting("MusicHUD", "Settings for the Music HUD.");
   public BooleanSetting musicHUD = new BooleanSetting(
      "MusicHUD", "Enabled", "Toggle Music HUD display.", new CategorySetting.CategoryCondition(this.musicHUDCategory), true
   );
   public NumberSetting musicHudBarAlpha = new NumberSetting(
      "MusicHudBarAlpha",
      "BarAlpha",
      "Transparency of the visualizer bars (0-255).",
      new CategorySetting.CategoryCondition(this.musicHUDCategory),
      160.0,
      0.0,
      255.0
   );
   public BooleanSetting musicHudGradientBars = new BooleanSetting(
      "MusicHudGradientBars", "GradientBars", "Gradient colored visualizer bars.", new CategorySetting.CategoryCondition(this.musicHUDCategory), true
   );
   public ModeSetting musicHudBackground = new ModeSetting(
      "MusicHudBackground",
      "Background",
      "Background style of the HUD.",
      new CategorySetting.CategoryCondition(this.musicHUDCategory),
      "LiquidGlass",
      new String[]{"LiquidGlass", "Blur", "Default"}
   );
   public NumberSetting musicHudBlurIntensity = new NumberSetting(
      "MusicHudBlurIntensity", "BlurIntensity", "Intensity of the blur effect.", new CategorySetting.CategoryCondition(this.musicHUDCategory), 3.0, 0.0, 50.0
   );
   public BooleanSetting musicHudCompactMode = new BooleanSetting(
      "MusicHudCompactMode", "CompactMode", "Collapse the HUD, limiting its width.", new CategorySetting.CategoryCondition(this.musicHUDCategory), false
   );
   public BooleanSetting musicHudDisk = new BooleanSetting(
      "MusicHudDisk", "Disk", "Enable to show the spinning record.", new CategorySetting.CategoryCondition(this.musicHUDCategory), true
   );
   public BooleanSetting musicHudUltraDisk = new BooleanSetting(
      "MusicHudUltraDisk", "UltraDisk", "Only display the record, hide everything else.", new CategorySetting.CategoryCondition(this.musicHUDCategory), false
   );
   public NumberSetting musicHudDiskSize = new NumberSetting(
      "MusicHudDiskSize", "DiskSize", "Size of the music disk (Ultra Disk).", new CategorySetting.CategoryCondition(this.musicHUDCategory), 100.0, 30.0, 300.0
   );
   public BooleanSetting musicHudTextBloom = new BooleanSetting(
      "MusicHudTextBloom", "TextBloom", "Text bloom glow effect.", new CategorySetting.CategoryCondition(this.musicHUDCategory), true
   );
   public BooleanSetting musicHudTextBloomPlus = new BooleanSetting(
      "MusicHudTextBloomPlus", "TextBloomPlus", "Enhanced Skia text bloom glow.", new CategorySetting.CategoryCondition(this.musicHUDCategory), true
   );
   public ColorSetting musicHudColor = new ColorSetting(
      "MusicHudColor", "Color", "Accent color used for the visualizer.", new CategorySetting.CategoryCondition(this.musicHUDCategory), ColorUtil.a()
   );
   public ModeSetting musicHudButtonEffect = new ModeSetting(
      "MusicHudButtonEffect",
      "ButtonEffect",
      "Visual effect when clicking buttons.",
      new CategorySetting.CategoryCondition(this.musicHUDCategory),
      "Gradient",
      new String[]{"Splash", "Topo", "Gradient"}
   );
   public static HudModule bf;
   private final HudModule.LowArmorToast bk = new HudModule.LowArmorToast();
   private final HudModule.LowArmorToast bl = new HudModule.LowArmorToast();
   private final HudModule.LowArmorToast bm = new HudModule.LowArmorToast();
   private final Animation bn = new Animation(300, EasingUtil.Easing.EASE_OUT_CUBIC);
   private final Animation bo = new Animation(300, EasingUtil.Easing.EASE_OUT_CUBIC);
   private float bp;
   private List<HudModule.ModuleListEntry> bq = new ArrayList<>();
   private List<HudModule.RadarEntry> br = new ArrayList<>();
   private List<HudModule.PotionEntry> bs = new ArrayList<>();
   private final int bt = 2000000 + new Random().nextInt(8000000);
   private final int bu = 2000000 + new Random().nextInt(8000000);
   private static final EquipmentSlot[] bv = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
   private static ItemStack[] bw;
   private final Map<Module, Animation> bx = new HashMap<>();

   public static boolean v() {
      return FabricLoader.getInstance().isModLoaded("night-music") || Night.moduleManager != null && Night.moduleManager.getModuleByName("PlayMusic") != null;
   }

   public HudModule() {
      bf = this;
      HudElementManager.a("Watermark", this.watermark, this.watermarkPosition, this.watermarkCategory);
      HudElementManager.a("Welcomer", this.welcomer, this.welcomerPosition, this.welcomerCategory);
      HudElementManager.a("ModuleList", this.moduleList, this.moduleListPosition, this.moduleListCategory);
      HudElementManager.a("PlayerRadar", this.y, this.playerRadarPosition, this.x);
      HudElementManager.a("Armor", this.armor, this.armorPosition);
      HudElementManager.a("TotemCounter", this.totemCounter, this.totemCounterPosition);
      HudElementManager.a("CrystalCounter", this.crystalCounter, this.crystalCounterPosition);
      HudElementManager.a("XPCounter", this.xPCounter, this.xPCounterPosition);
      HudElementManager.a("Inventory", this.inventory, this.inventoryPosition, this.inventoryCategory);
      HudElementManager.a("Information", this.informationElement, this.informationPosition, this.informationCategory);
      HudElementManager.a("Coordinates", this.coordinates, this.coordinatesPosition, this.positionCategory);
      HudElementManager.a("Potions", this.potions, this.potionsPosition, this.potionsCategory);
      this.musicHUDPosition.setPosition(20.0F, 60.0F);
      this.inventoryPosition.setPosition(20.0F, 120.0F);
   }

   private int a_int_int_int(int var1, int var2) {
      if (!this.fakeCoords.getValue()) {
         return var1;
      }

      if (Math.abs(var1) < this.fakeThreshold.getValue().intValue()) {
         return var1;
      }

      long var3 = var2 + Math.abs(var1) / 3L;
      long var5 = (Math.abs(var1) * 31 + var2) % 2 == 0 ? -var3 : var3;
      if (mc.player != null && mc.player.level() != null && mc.player.level().dimension() == Level.NETHER) {
         var5 /= 8L;
      }

      return (int)var5;
   }

   @EventTarget
   public void a_void_SettingChangeEvent(SettingChangeEvent var1) {
      if (var1.getSetting() == this.textGlow) {
         FontModule var2 = Night.moduleManager != null ? Night.moduleManager.getModule(FontModule.class) : null;
         if (var2 != null && var2.glow.getValue() != this.textGlow.getValue()) {
            var2.glow.setValue(this.textGlow.getValue());
         }
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.moduleList.getValue()) {
            Comparator<Module> var2 = Comparator.comparingInt(var1x -> -Night.fontManager.getStringWidth(this.a_String_Module(var1x)));
            Comparator<Module> var3 = Comparator.comparing(Module::getName);
            boolean var4 = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
            ArrayList var5 = new ArrayList();

            for (Module var8 : Night.moduleManager.getModules().stream().filter(var0 -> var0.drawn.getValue()).filter(var2x -> {
               if (var4) {
                  return var2x.getKeybind() != 0 ? true : !this.bindOnly.getValue() && (var2x.isEnabled() || var2x.getAnimation().getTarget() > 0.01F);
               } else {
                  return this.bindOnly.getValue() && var2x.getKeybind() == 0 ? false : var2x.isEnabled() || var2x.getAnimation().getTarget() > 0.01F;
               }
            }).sorted(this.moduleListSorting.getValue().equalsIgnoreCase("Width") ? var2 : var3).toList()) {
               String var9 = this.a_String_Module(var8);
               var5.add(new HudModule.ModuleListEntry(var8, var9));
            }

            this.bq = var5;
         }

         if (this.y.getValue()) {
            Comparator<Player> var12 = Comparator.comparingDouble(var0 -> mc.player.distanceTo(var0));
            Comparator<Player> var14 = Comparator.comparing(var0 -> var0.getName().getString());
            ArrayList var16 = new ArrayList();

            for (Player var22 : mc.level
               .players()
               .stream()
               .filter(var0 -> var0 != mc.player)
               .filter(var0 -> !EntityUtil.c_bool_Entity(var0))
               .filter(var1x -> !this.playerRadarAntiBot.getValue() || !EntityUtil.a_bool_Player(var1x))
               .filter(var0 -> !Night.moduleManager.getModule(IgnoreNaked.class).isEnabled() || !EntityUtil.b_bool_Player(var0))
               .sorted(this.playerRadarSorting.getValue().equalsIgnoreCase("Distance") ? var12 : var14)
               .limit(this.z.getValue().longValue())
               .toList()) {
               Identifier var24 = null;
               if (this.playerIcons.getValue() && mc.getConnection() != null) {
                  PlayerInfo var26 = mc.getConnection().getPlayerInfo(var22.getName().getString());
                  if (var26 != null) {
                     var24 = var26.getSkin().body().texturePath();
                  }
               }

               String var27 = var22.getName().getString();
               if (this.playerRadarDistance.getValue()) {
                  var27 = var27 + ChatFormatting.WHITE + " " + new DecimalFormat("0.0").format(mc.player.distanceTo(var22));
               }

               if (this.playerRadarEntityID.getValue()) {
                  var27 = var27 + ChatFormatting.WHITE + " " + var22.getId();
               }

               if (this.playerRadarGameMode.getValue()) {
                  var27 = var27 + ChatFormatting.WHITE + " [" + EntityUtil.a_String_GameType(EntityUtil.d_GameType_Player(var22)) + "]";
               }

               if (this.playerRadarPing.getValue()) {
                  var27 = var27 + ChatFormatting.WHITE + " " + EntityUtil.c_int_Player(var22) + "ms";
               }

               if (this.playerRadarHealth.getValue()) {
                  var27 = var27
                     + ColorUtil.a_ChatFormatting_double(var22.getHealth() + var22.getAbsorptionAmount())
                     + " "
                     + new DecimalFormat("0.0").format(var22.getHealth() + var22.getAbsorptionAmount())
                     + ChatFormatting.RESET;
               }

               int var10 = Night.worldStateManager.getTotemPops().getOrDefault(var22.getUUID(), 0);
               if (this.playerRadarTotems.getValue() && var10 > 0) {
                  var27 = var27 + ColorUtil.b_ChatFormatting_int(var10) + " -" + var10 + ChatFormatting.RESET;
               }

               if (this.playerRadarTurtlePot.getValue()) {
                  MobEffectInstance var11 = var22.getEffect(MobEffects.RESISTANCE);
                  if (var11 != null && var11.getAmplifier() >= 2) {
                     var27 = var27 + ChatFormatting.RED + " [TM]" + ChatFormatting.RESET;
                  }
               }

               var16.add(new HudModule.RadarEntry(var22, (String)var27, var24));
            }

            this.br = var16;
         }

         if (this.potions.getValue()) {
            Comparator<MobEffectInstance> var13 = Comparator.comparingInt(
               var0 -> -Night.fontManager.getStringWidth(((MobEffect)var0.getEffect().value()).getDisplayName().getString() + " " + (var0.getAmplifier() + 1))
            );
            Comparator<MobEffectInstance> var15 = Comparator.comparing(var0 -> ((MobEffect)var0.getEffect().value()).getDisplayName().getString());
            ArrayList var17 = new ArrayList();

            for (MobEffectInstance var23 : mc.player
               .getActiveEffects()
               .stream()
               .sorted(this.potionSorting.getValue().equalsIgnoreCase("Width") ? var13 : var15)
               .toList()) {
               String var25 = this.a_String_MobEffectInstance(var23);
               Identifier var28 = null;
               if (this.potionIcons.getValue()) {
                  var28 = Hud.getMobEffectSprite(var23.getEffect());
               }

               var17.add(
                  new HudModule.PotionEntry(
                     var25,
                     var28,
                     this.potionColor.getValue().equalsIgnoreCase("Vanilla")
                        ? new Color(((MobEffect)var23.getEffect().value()).getColor())
                        : (
                           this.potionColor.getValue().equalsIgnoreCase("Enhanced")
                              ? (
                                 EntityUtil.a.containsKey(var23.getEffect().value())
                                    ? EntityUtil.a.get(var23.getEffect().value())
                                    : new Color(((MobEffect)var23.getEffect().value()).getColor())
                              )
                              : null
                        )
                  )
               );
            }

            this.bs = var17;
         }
      }
   }

   @EventTarget
   public void a_void_Render2DEvent(Render2DEvent var1) {
      if (mc.player != null) {
         FontManager.setHudGlowContext(true);

         try {
            this.bp = this.bo.animateTo(mc.gui.screen() instanceof ChatScreen ? 14.0F : 0.0F);
            Render2DUtil.a(
               var1.getContext(),
               2.0F,
               mc.getWindow().getGuiScaledHeight() - this.bp,
               mc.getWindow().getGuiScaledWidth() - 2,
               mc.getWindow().getGuiScaledHeight() + 12 - this.bp,
               new Color(0, 0, 0, (int)((Double)mc.options.textBackgroundOpacity().get() * 255.0))
            );
            this.b_void_Render2DEvent(var1);
            this.c_void_Render2DEvent(var1);
            this.d_void_Render2DEvent(var1);
            this.e_void_Render2DEvent(var1);
            this.j(var1);
            this.k(var1);
            this.l(var1);
            this.m(var1);
         } finally {
            FontManager.setHudGlowContext(false);
         }
      }
   }

   private void b_void_Render2DEvent(Render2DEvent var1) {
      Matrix3x2fStack var2 = var1.getMatrices();
      if (this.watermark.getValue() || this.uptime.getValue()) {
         var2.pushMatrix();
         var2.translate(this.watermarkPosition.getX(), this.watermarkPosition.getY());
         int var3 = 0;
         int var4 = 0;
         if (this.watermark.getValue()) {
            boolean var5 = Night.licenseService != null && Night.licenseService.isStandardRole();
            String var6 = var5 ? "v26" : "26";
            String var7 = var5 && this.watermarkVersion.getValue()
               ? (!this.watermarkVersionMode.getValue().equals("GitHash") ? "-nightly " + ChatFormatting.GRAY + "(2026-10-17)" : "+490.1c820e8a37")
               : "";
            String var8 = this.watermarkText.getValue()
               + (
                  this.watermarkVersion.getValue()
                     ? (this.watermarkSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE))
                        + " "
                        + var6
                        + (this.watermarkMinecraftVersion.getValue() ? "-mc26.2" : "")
                        + var7
                     : ""
               );
            float var9 = 2.0F;
            if (this.watermarkLogo.getValue()) {
               int var10 = Night.fontManager.getFontHeight();
               int var11 = this.a_Color_float(2.0F).getRGB();
               var1.getContext().blit(RenderPipelines.GUI_TEXTURED, bi, 2, 2, 0.0F, 0.0F, var10, var10, 64, 64, 64, 64, var11);
               var9 = 2 + var10 + 3;
            }

            this.a(var1.getContext(), var8, var9, 2.0F);
            var3 = Math.max(var3, (int)(var9 - 2.0F) + Night.fontManager.getStringWidth(var8));
            var4++;
         }

         if (this.uptime.getValue()) {
            String[] var14 = TextFormattingUtil.a_StringArr_long((System.currentTimeMillis() - Night.START_TIME) / 1000L);
            String var15 = "Uptime " + ChatFormatting.WHITE + var14[0] + ":" + var14[1] + ":" + var14[2];
            this.a(
               var1.getContext(),
               var15,
               2.0F,
               2 + (this.watermark.getValue() ? Night.fontManager.getFontHeight() : 0),
               this.informationSync.getValue() ? null : new Color(170, 170, 170)
            );
            var3 = Math.max(var3, Night.fontManager.getStringWidth(var15));
            var4++;
         }

         HudElementManager.a("Watermark", 2.0F, 2.0F, 2 + var3, 2 + var4 * Night.fontManager.getFontHeight());
         var2.popMatrix();
      }

      if (this.welcomer.getValue()) {
         var2.pushMatrix();
         var2.translate(this.welcomerPosition.getX(), this.welcomerPosition.getY());
         String var12 = this.welcomerText
            .getValue()
            .replace(
               "[username]",
               (this.welcomerSync.getValue() ? "" : (this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.WHITE))
                  + mc.player.getName().getString()
                  + ChatFormatting.RESET
            );
         String var10000 = "Hello, [username]! :^)";
         float var13 = mc.getWindow().getGuiScaledWidth() / 2.0F - Night.fontManager.getStringWidth(var12) / 2.0F;
         this.a(var1.getContext(), var12, var13, 20.0F);
         HudElementManager.a("Welcomer", var13, 20.0F, var13 + Night.fontManager.getStringWidth(var12), 6 + Night.fontManager.getFontHeight());
         var2.popMatrix();
      }
   }

   private void c_void_Render2DEvent(Render2DEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.moduleList.getValue()) {
            float var2 = this.bn
               .animateTo(
                  this.vanillaPotions.getValue().equalsIgnoreCase("Move") && !mc.player.getActiveEffects().isEmpty()
                     ? (EntityUtil.e(mc.player) ? 51 : 25)
                     : 0.0F
               );
            Matrix3x2fStack var3 = var1.getMatrices();
            var3.pushMatrix();
            var3.translate(this.moduleListPosition.getX(), this.moduleListPosition.getY());
            boolean var4 = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
            int var5 = 0;
            int var6 = 0;

            for (HudModule.ModuleListEntry var8 : (Iterable<HudModule.ModuleListEntry>)(Object)this.bq) {
               float var9 = var8.module().isEnabled() || var4 && var8.module().getKeybind() != 0 ? Night.fontManager.getStringWidth(var8.text()) + 2 : 0.0F;
               float var10 = mc.getWindow().getGuiScaledWidth() - var8.module().getAnimation().animateTo(var9);
               float var11 = 2.0F + var2 + var6 * Night.fontManager.getFontHeight();
               this.a(var8.module(), var1.getContext(), var8.text(), var10, var11);
               var5 = Math.max(var5, Night.fontManager.getStringWidth(var8.text()) + 2);
               var6++;
            }

            if (!this.bq.isEmpty()) {
               float var12 = mc.getWindow().getGuiScaledWidth();
               HudElementManager.a("ModuleList", var12 - var5, 2.0F + var2, var12, 2.0F + var2 + this.bq.size() * Night.fontManager.getFontHeight());
            }

            var3.popMatrix();
         }
      }
   }

   private void d_void_Render2DEvent(Render2DEvent var1) {
      if (this.y.getValue()) {
         Matrix3x2fStack var2 = var1.getMatrices();
         var2.pushMatrix();
         var2.translate(this.playerRadarPosition.getX(), this.playerRadarPosition.getY());
         int var3 = 0;
         int var4 = 0;

         for (HudModule.RadarEntry var6 : (Iterable<HudModule.RadarEntry>)(Object)this.br) {
            if (var6.texture() != null) {
               PlayerFaceExtractor.extractRenderState(
                  var1.getContext(),
                  var6.texture(),
                  2,
                  1 + Night.fontManager.getFontHeight() * 2 + (Night.fontManager.getFontHeight() + 1) * var4,
                  Night.fontManager.getFontHeight(),
                  true,
                  false,
                  Color.WHITE.getRGB()
               );
            }

            int var7 = 2 + (var6.texture() != null ? Night.fontManager.getFontHeight() + 2 : 0);
            this.a(
               var1.getContext(),
               var6.text(),
               var7,
               2 + Night.fontManager.getFontHeight() * 2 + (Night.fontManager.getFontHeight() + 1) * var4,
               Night.friendManager.isFriend(var6.player().getName().getString()) ? Night.friendManager.getFriendColor() : null
            );
            var3 = Math.max(var3, var7 + Night.fontManager.getStringWidth(var6.text()));
            var4++;
         }

         if (!this.br.isEmpty()) {
            HudElementManager.a(
               "PlayerRadar",
               2.0F,
               1 + Night.fontManager.getFontHeight() * 2,
               var3,
               1 + Night.fontManager.getFontHeight() * 2 + (Night.fontManager.getFontHeight() + 1) * this.br.size()
            );
         }

         var2.popMatrix();
      }
   }

   private static int a_int_Player_Item(Player var0, Item var1) {
      if (var0 == null) {
         return 0;
      }

      int var2 = 0;

      for (ItemStack var4 : (Iterable<ItemStack>)(Object)var0.getInventory().getNonEquipmentItems()) {
         if (var4.is(var1)) {
            var2 += var4.getCount();
         }
      }

      if (var0.getOffhandItem().is(var1)) {
         var2 += var0.getOffhandItem().getCount();
      }

      return var2;
   }

   private static ItemStack[] w() {
      if (bw == null) {
         bw = new ItemStack[]{
            new ItemStack(Items.NETHERITE_BOOTS),
            new ItemStack(Items.NETHERITE_LEGGINGS),
            new ItemStack(Items.NETHERITE_CHESTPLATE),
            new ItemStack(Items.NETHERITE_HELMET)
         };
      }

      return bw;
   }

   private void e_void_Render2DEvent(Render2DEvent var1) {
      if (mc.player != null) {
         this.f_void_Render2DEvent(var1);
         this.g(var1);
         this.h(var1);
         this.i(var1);
      }
   }

   private void f_void_Render2DEvent(Render2DEvent var1) {
      if (mc.player != null) {
         boolean var2 = mc.gui.screen() instanceof HudEditorScreen;
         int var3 = !mc.player.isEyeInFluid(FluidTags.WATER) && mc.player.getAirSupply() >= mc.player.getMaxAirSupply() ? 0 : 10;
         int var4 = mc.getWindow().getGuiScaledWidth() / 2 + 15;
         int var5 = mc.getWindow().getGuiScaledHeight() - 55 - var3;
         HudElementManager.a("Armor", var4 - 1, var5 - 3, var4 + 71, var5 + 17);
         if (this.armor.getValue() || var2) {
            Matrix3x2fStack var6 = var1.getMatrices();
            var6.pushMatrix();
            var6.translate(this.armorPosition.getX(), this.armorPosition.getY());
            boolean var7 = false;

            for (EquipmentSlot var11 : bv) {
               if (!mc.player.getItemBySlot(var11).isEmpty()) {
                  var7 = true;
                  break;
               }
            }

            if (var7 && this.armor.getValue()) {
               int var19 = 0;

               for (EquipmentSlot var12 : bv) {
                  ItemStack var13 = mc.player.getItemBySlot(var12);
                  if (!var13.isEmpty()) {
                     int var14 = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * var19;
                     int var15 = var5;
                     var1.getContext().item(var13, var14, var15);
                     if (this.armorDurability.getValue().equalsIgnoreCase("Bar") || this.armorDurability.getValue().equalsIgnoreCase("Both")) {
                        var1.getContext().itemDecorations(mc.font, var13, var14, var15);
                     }

                     int var16 = var13.getDamageValue();
                     int var17 = var13.getMaxDamage();
                     if ((this.armorDurability.getValue().equalsIgnoreCase("Percentage") || this.armorDurability.getValue().equalsIgnoreCase("Both"))
                        && var17 > 0) {
                        var6.pushMatrix();
                        var6.scale(0.625F, 0.625F);
                        this.a(
                           var1.getContext(),
                           (var17 - var16) * 100 / var17 + "%",
                           (int)(((mc.getWindow().getGuiScaledWidth() >> 1) + 70 - 18 * var19) * 1.6F),
                           (int)((var5 - 3) * 1.6F - 5.0F),
                           false,
                           new Color(1.0F - (float)(var17 - var16) / var17, (float)(var17 - var16) / var17, 0.0F)
                        );
                        var6.popMatrix();
                     }

                     var19++;
                  }
               }
            } else if (var2) {
               ItemStack[] var18 = w();

               for (int var20 = 0; var20 < var18.length; var20++) {
                  int var22 = mc.getWindow().getGuiScaledWidth() / 2 + 69 - 18 * var20;
                  int var24 = var5;
                  var1.getContext().item(var18[var20], var22, var24);
               }
            }

            var6.popMatrix();
         }
      }
   }

   private void g(Render2DEvent var1) {
      if (mc.player != null) {
         boolean var2 = mc.gui.screen() instanceof HudEditorScreen;
         float var3 = mc.getWindow().getGuiScaledWidth() / 2.0F - 9.0F;
         float var4 = mc.getWindow().getGuiScaledHeight()
            - 55
            - (mc.player.isEyeInFluid(FluidTags.WATER) && mc.gameMode != null && mc.gameMode.getPlayerMode() != GameType.CREATIVE ? 10 : 0);
         HudElementManager.a("TotemCounter", var3 - 1.0F, var4 - 1.0F, var3 + 19.0F, var4 + 19.0F);
         if (this.totemCounter.getValue() || var2) {
            int var5 = a_int_Player_Item(mc.player, Items.TOTEM_OF_UNDYING);
            this.bk.a(var5);
            Matrix3x2fStack var6 = var1.getMatrices();
            var6.pushMatrix();
            var6.translate(this.totemCounterPosition.getX(), this.totemCounterPosition.getY());
            if (!this.totemCounter.getValue() || var5 <= 0 && this.bk.a <= 0) {
               if (var2) {
                  ItemStack var8 = new ItemStack(Items.TOTEM_OF_UNDYING);
                  var6.pushMatrix();
                  var6.translate(var3, var4);
                  var1.getContext().item(var8, 0, 0);
                  if (Night.fontManager != null) {
                     Night.fontManager.drawShadowedText(var1.getContext(), "1", 17 - Night.fontManager.getStringWidth("1"), 9, Color.WHITE);
                  } else {
                     var1.getContext().text(mc.font, "1", 17 - mc.font.width("1"), 9, -1, true);
                  }

                  var6.popMatrix();
               }
            } else {
               ItemStack var7 = new ItemStack(Items.TOTEM_OF_UNDYING);
               var6.pushMatrix();
               var6.translate(var3, var4);
               var1.getContext().item(var7, 0, 0);
               this.bk.a(var1.getContext(), this.itemsSmooth.getValue());
               var6.popMatrix();
            }

            var6.popMatrix();
         }
      }
   }

   private void h(Render2DEvent var1) {
      if (mc.player != null) {
         boolean var2 = mc.gui.screen() instanceof HudEditorScreen;
         float var3 = mc.getWindow().getGuiScaledWidth() / 2.0F + 106.0F;
         float var4 = mc.getWindow().getGuiScaledHeight() - 40 - (this.counterChatOffset.getValue() ? this.bp : 0.0F);
         HudElementManager.a("CrystalCounter", var3 - 1.0F, var4 - 1.0F, var3 + 19.0F, var4 + 19.0F);
         if (this.crystalCounter.getValue() || var2) {
            int var5 = a_int_Player_Item(mc.player, Items.END_CRYSTAL);
            this.bl.a(var5);
            Matrix3x2fStack var6 = var1.getMatrices();
            var6.pushMatrix();
            var6.translate(this.crystalCounterPosition.getX(), this.crystalCounterPosition.getY());
            if (!this.crystalCounter.getValue() || var5 <= 0 && this.bl.a <= 0) {
               if (var2) {
                  ItemStack var8 = new ItemStack(Items.END_CRYSTAL);
                  var6.pushMatrix();
                  var6.translate(var3, var4);
                  var1.getContext().item(var8, 0, 0);
                  if (Night.fontManager != null) {
                     Night.fontManager.drawShadowedText(var1.getContext(), "64", 17 - Night.fontManager.getStringWidth("64"), 9, Color.WHITE);
                  } else {
                     var1.getContext().text(mc.font, "64", 17 - mc.font.width("64"), 9, -1, true);
                  }

                  var6.popMatrix();
               }
            } else {
               ItemStack var7 = new ItemStack(Items.END_CRYSTAL);
               var6.pushMatrix();
               var6.translate(var3, var4);
               var1.getContext().item(var7, 0, 0);
               this.bl.a(var1.getContext(), this.itemsSmooth.getValue());
               var6.popMatrix();
            }

            var6.popMatrix();
         }
      }
   }

   private void i(Render2DEvent var1) {
      if (mc.player != null) {
         boolean var2 = mc.gui.screen() instanceof HudEditorScreen;
         float var3 = mc.getWindow().getGuiScaledWidth() / 2.0F + 106.0F;
         float var4 = mc.getWindow().getGuiScaledHeight() - 20 - (this.counterChatOffset.getValue() ? this.bp : 0.0F);
         HudElementManager.a("XPCounter", var3 - 1.0F, var4 - 1.0F, var3 + 19.0F, var4 + 19.0F);
         if (this.xPCounter.getValue() || var2) {
            int var5 = a_int_Player_Item(mc.player, Items.EXPERIENCE_BOTTLE);
            this.bm.a(var5);
            Matrix3x2fStack var6 = var1.getMatrices();
            var6.pushMatrix();
            var6.translate(this.xPCounterPosition.getX(), this.xPCounterPosition.getY());
            if (!this.xPCounter.getValue() || var5 <= 0 && this.bm.a <= 0) {
               if (var2) {
                  ItemStack var8 = new ItemStack(Items.EXPERIENCE_BOTTLE);
                  var6.pushMatrix();
                  var6.translate(var3, var4);
                  var1.getContext().item(var8, 0, 0);
                  if (Night.fontManager != null) {
                     Night.fontManager.drawShadowedText(var1.getContext(), "64", 17 - Night.fontManager.getStringWidth("64"), 9, Color.WHITE);
                  } else {
                     var1.getContext().text(mc.font, "64", 17 - mc.font.width("64"), 9, -1, true);
                  }

                  var6.popMatrix();
               }
            } else {
               ItemStack var7 = new ItemStack(Items.EXPERIENCE_BOTTLE);
               var6.pushMatrix();
               var6.translate(var3, var4);
               var1.getContext().item(var7, 0, 0);
               this.bm.a(var1.getContext(), this.itemsSmooth.getValue());
               var6.popMatrix();
            }

            var6.popMatrix();
         }
      }
   }

   private void j(Render2DEvent var1) {
      if (mc.player != null) {
         if (this.inventory.getValue()) {
            Matrix3x2fStack var2 = var1.getMatrices();
            var2.pushMatrix();
            var2.translate(this.inventoryPosition.getX(), this.inventoryPosition.getY());
            int var3 = 9 * 18 + 3 * 2;
            int var4 = 3 * 18 + 3 * 2;
            HudElementManager.a("Inventory", 0.0F, 0.0F, var3, var4);
            if (this.inventoryBackground.getValue()) {
               int var5 = (int)Math.clamp(this.inventoryAlpha.getValue().floatValue(), 0.0F, 255.0F);
               Color var6 = new Color(15, 15, 18, var5);
               Color var7 = new Color(255, 255, 255, Math.min(var5, 30));
               Render2DUtil.a(var1.getContext(), 0.0F, 0.0F, var3, var4, var6);
               Render2DUtil.b(var1.getContext(), 0.0F, 0.0F, var3, var4, var7);
            }

            for (int var11 = 9; var11 <= 35; var11++) {
               int var12 = (var11 - 9) % 9;
               int var13 = (var11 - 9) / 9;
               int var8 = 3 + var12 * 18;
               int var9 = 3 + var13 * 18;
               if (this.inventorySlotBackground.getValue()) {
                  Color var10 = new Color(255, 255, 255, 10);
                  Render2DUtil.a(var1.getContext(), var8, var9, var8 + 18, var9 + 18, var10);
               }

               ItemStack var14 = mc.player.getInventory().getItem(var11);
               if (!var14.isEmpty()) {
                  var1.getContext().item(var14, var8 + 1, var9 + 1);
                  var1.getContext().itemDecorations(mc.font, var14, var8 + 1, var9 + 1);
               }
            }

            var2.popMatrix();
         }
      }
   }

   private void k(Render2DEvent var1) {
      if (mc.player != null) {
         if (this.potions.getValue()) {
            Matrix3x2fStack var2 = var1.getMatrices();
            var2.pushMatrix();
            var2.translate(this.potionsPosition.getX(), this.potionsPosition.getY());
            float var3 = this.informationChatOffset.getValue() ? this.bp : 0.0F;
            int var4 = 0;
            int var5 = 0;

            for (HudModule.PotionEntry var7 : (Iterable<HudModule.PotionEntry>)(Object)this.bs) {
               int var8 = Night.fontManager.getStringWidth(var7.text());
               int var9 = var8 + (var7.texture() != null ? Night.fontManager.getFontHeight() + 2 : 0);
               var5 = Math.max(var5, var9);
               int var10 = mc.getWindow().getGuiScaledWidth() - 2 - var8;
               int var11 = mc.getWindow().getGuiScaledHeight() - (int)var3 - 2 - Night.fontManager.getFontHeight() - Night.fontManager.getFontHeight() * var4;
               if (var7.texture() != null) {
                  var2.pushMatrix();
                  var2.translate(var10 - Night.fontManager.getFontHeight() - 2, var11 - 1);
                  var1.getContext()
                     .blitSprite(RenderPipelines.GUI_TEXTURED, var7.texture(), 0, 0, Night.fontManager.getFontHeight(), Night.fontManager.getFontHeight());
                  var2.popMatrix();
               }

               this.a(
                  var1.getContext(),
                  var7.text(),
                  var10,
                  var11,
                  this.potionColor.getValue().equals("Client") && this.color.getValue().equals("Rainbow") && this.rainbow.getValue().equals("Horizontal"),
                  var7.color()
               );
               var4++;
            }

            if (var4 > 0) {
               float var12 = mc.getWindow().getGuiScaledWidth();
               float var13 = mc.getWindow().getGuiScaledHeight() - var3;
               HudElementManager.a("Potions", var12 - 2.0F - var5 - 4.0F, var13 - 2.0F - var4 * Night.fontManager.getFontHeight(), var12, var13);
            }

            var2.popMatrix();
         }
      }
   }

   private void l(Render2DEvent var1) {
      if (mc.player != null) {
         if (this.informationElement.getValue()) {
            Matrix3x2fStack var2 = var1.getMatrices();
            var2.pushMatrix();
            var2.translate(this.informationPosition.getX(), this.informationPosition.getY());
            int var3 = 0;
            float var4 = this.informationChatOffset.getValue() ? this.bp : 0.0F;
            if (this.health.getValue()) {
               String var5 = new DecimalFormat("0").format(mc.player.getHealth() + mc.player.getAbsorptionAmount());
               Color var6 = new Color(
                  TextColor.fromLegacyFormat(ColorUtil.a_ChatFormatting_double(mc.player.getHealth() + mc.player.getAbsorptionAmount())).getValue()
               );
               Night.fontManager
                  .drawOutlinedText(
                     var1.getContext(),
                     var5,
                     mc.getWindow().getGuiScaledWidth() / 2 - Night.fontManager.getStringWidth(var5) / 2,
                     mc.getWindow().getGuiScaledHeight() / 2 + 16,
                     var6,
                     Color.BLACK
                  );
            }

            ArrayList var12 = new ArrayList();
            if (this.ping.getValue()) {
               var12.add(this.x() + "Ping " + this.y() + Night.serverManager.getPing() + "ms");
            }

            if (this.fPS.getValue()) {
               var12.add(this.x() + "FPS " + this.y() + Night.renderPlacementManager.getCrystalsPerSecond());
            }

            if (this.durability.getValue()) {
               var12.add("Durability " + (mc.player.getMainHandItem().getMaxDamage() - mc.player.getMainHandItem().getDamageValue()));
            }

            if (!this.speed.getValue().equalsIgnoreCase("None")) {
               var12.add(
                  this.x()
                     + "Speed "
                     + this.y()
                     + new DecimalFormat("0.00")
                        .format(
                           EntityUtil.a_double_Entity_SpeedUnit(
                              mc.player, this.speed.getValue().equalsIgnoreCase("Meters") ? EntityUtil.SpeedUnit.METERS : EntityUtil.SpeedUnit.KILOMETERS
                           )
                        )
                     + (this.speed.getValue().equalsIgnoreCase("Meters") ? "m/s" : "km/h")
               );
            }

            if (this.serverBrand.getValue()) {
               var12.add(this.x() + "Brand " + this.y() + Night.serverManager.getServerBrand());
            }

            float var13 = Night.serverManager.getTps();
            if (this.tPS.getValue()) {
               var12.add(this.x() + "TPS " + this.y() + (var13 > 19.79 ? "20.00" : new DecimalFormat("00.00").format(var13)));
            }

            if (!var12.isEmpty()) {
               var12.sort(Comparator.comparingInt(Night.fontManager::getStringWidth).reversed());

               for (String var8 : (Iterable<String>)(Object)var12) {
                  if (var8.startsWith("Durability")) {
                     if (mc.player.getMainHandItem().isDamageableItem()) {
                        int var9 = mc.player.getMainHandItem().getMaxDamage();
                        int var10 = mc.player.getMainHandItem().getDamageValue();
                        String var11 = String.valueOf(var9 - var10);
                        this.a(
                           var1.getContext(),
                           this.x() + "Durability ",
                           mc.getWindow().getGuiScaledWidth() - 2 - Night.fontManager.getStringWidth("Durability ") - Night.fontManager.getStringWidth(var11),
                           mc.getWindow().getGuiScaledHeight() - var4 - 2.0F - Night.fontManager.getFontHeight() + var3 * -Night.fontManager.getFontHeight(),
                           this.informationSync.getValue() ? null : new Color(170, 170, 170)
                        );
                        this.a(
                           var1.getContext(),
                           var11,
                           mc.getWindow().getGuiScaledWidth() - 2 - Night.fontManager.getStringWidth(var11),
                           mc.getWindow().getGuiScaledHeight() - var4 - 2.0F - Night.fontManager.getFontHeight() + var3 * -Night.fontManager.getFontHeight(),
                           false,
                           new Color(1.0F - (float)(var9 - var10) / var9, (float)(var9 - var10) / var9, 0.0F)
                        );
                        var3++;
                     }
                  } else {
                     this.a(
                        var1.getContext(),
                        var8,
                        mc.getWindow().getGuiScaledWidth() - 2 - Night.fontManager.getStringWidth(var8),
                        mc.getWindow().getGuiScaledHeight() - var4 - 2.0F - Night.fontManager.getFontHeight() + var3 * -Night.fontManager.getFontHeight(),
                        this.informationSync.getValue() ? null : new Color(170, 170, 170)
                     );
                     var3++;
                  }
               }

               int var14 = 0;

               for (String var17 : (Iterable<String>)(Object)var12) {
                  var14 = Math.max(var14, Night.fontManager.getStringWidth(var17));
               }

               float var16 = mc.getWindow().getGuiScaledWidth();
               float var18 = mc.getWindow().getGuiScaledHeight() - var4;
               HudElementManager.a("Information", var16 - 2.0F - var14, var18 - 2.0F - var3 * Night.fontManager.getFontHeight(), var16, var18);
            }

            var2.popMatrix();
         }
      }
   }

   private void m(Render2DEvent var1) {
      if (mc.player != null && mc.level != null) {
         if (this.coordinates.getValue()) {
            Matrix3x2fStack var2 = var1.getMatrices();
            var2.pushMatrix();
            var2.translate(this.coordinatesPosition.getX(), this.coordinatesPosition.getY());
            float var3 = this.positionChatOffset.getValue() ? this.bp : 0.0F;
            int var4 = 0;
            int var5 = 0;
            int var6 = 0;
            if (this.coordinates.getValue()) {
               int var7 = this.a_int_int_int(mc.player.getBlockX(), this.bt);
               int var8 = this.a_int_int_int(mc.player.getBlockZ(), this.bu);
               String var9 = ""
                  + this.y()
                  + var7
                  + (
                     this.netherCoordinates.getValue()
                        ? ChatFormatting.GRAY + " [" + this.y() + BlockInteractionUtil.a_int_int(var7) + ChatFormatting.GRAY + "]"
                        : ""
                  )
                  + (!this.inversion.getValue() && !this.positionSync.getValue() ? ChatFormatting.GRAY : ChatFormatting.RESET)
                  + ", "
                  + this.y()
                  + mc.player.getBlockY()
                  + (!this.inversion.getValue() && !this.positionSync.getValue() ? ChatFormatting.GRAY : ChatFormatting.RESET)
                  + ", "
                  + this.y()
                  + var8
                  + (
                     this.netherCoordinates.getValue()
                        ? ChatFormatting.GRAY + " [" + this.y() + BlockInteractionUtil.a_int_int(var8) + ChatFormatting.GRAY + "]"
                        : ""
                  );
               this.a(var1.getContext(), var9, 2.0F, mc.getWindow().getGuiScaledHeight() - var3 - var4 - Night.fontManager.getFontHeight() - 2.0F);
               var5 = Math.max(var5, Night.fontManager.getStringWidth(var9));
               var4 += Night.fontManager.getFontHeight();
               var6++;
            }

            if (this.direction.getValue()) {
               String var10 = this.x()
                  + BlockInteractionUtil.a_String_float(mc.player.getYRot())
                  + (this.inversion.getValue() ? this.y() : ChatFormatting.GRAY)
                  + " ["
                  + (this.inversion.getValue() ? this.y() : ChatFormatting.WHITE)
                  + BlockInteractionUtil.b_String_float(mc.player.getYRot())
                  + (this.inversion.getValue() ? this.y() : ChatFormatting.GRAY)
                  + "]";
               this.a(
                  var1.getContext(),
                  var10,
                  2.0F,
                  mc.getWindow().getGuiScaledHeight() - var3 - var4 - Night.fontManager.getFontHeight() - 2.0F,
                  this.positionSync.getValue() ? null : Color.WHITE
               );
               var5 = Math.max(var5, Night.fontManager.getStringWidth(var10));
               var6++;
            }

            float var11 = mc.getWindow().getGuiScaledHeight() - var3;
            HudElementManager.a("Coordinates", 2.0F, var11 - 2.0F - var6 * Night.fontManager.getFontHeight(), 2 + var5, var11 - 2.0F);
            var2.popMatrix();
         }
      }
   }

   private static Color a(Color var0, Color var1, float var2) {
      float var3 = Math.max(0.0F, Math.min(1.0F, var2));
      int var4 = Math.round(var0.getRed() + var3 * (var1.getRed() - var0.getRed()));
      int var5 = Math.round(var0.getGreen() + var3 * (var1.getGreen() - var0.getGreen()));
      int var6 = Math.round(var0.getBlue() + var3 * (var1.getBlue() - var0.getBlue()));
      int var7 = Math.round(var0.getAlpha() + var3 * (var1.getAlpha() - var0.getAlpha()));
      return new Color(var4, var5, var6, var7);
   }

   private void a(Module var1, GuiGraphicsExtractor var2, String var3, float var4, float var5) {
      if (!this.moduleListMode.getValue().equalsIgnoreCase("Newbie")) {
         if (!var1.isEnabled()) {
            this.a(var2, var3, var4, var5, false, new Color(170, 170, 170));
         } else {
            Color var12 = this.a_Color_float(var5);
            if (this.moduleColor.getValue().equalsIgnoreCase("Rainbow")) {
               long var13 = (long)var5 / Night.fontManager.getFontHeight() * this.rainbowOffset.getValue().longValue() * 10L;
               var12 = ColorUtil.a_Color_long(var13);
            } else if (this.moduleColor.getValue().equals("Random")) {
               var12 = ColorUtil.a_Color_String(var1.getName());
            }

            this.a(
               var2,
               var3,
               var4,
               var5,
               (this.moduleColor.getValue().equals("Rainbow") || this.moduleColor.getValue().equals("Default") && this.color.getValue().equals("Rainbow"))
                  && this.rainbow.getValue().equals("Horizontal"),
               var12
            );
         }
      } else {
         Animation var6 = this.bx
            .computeIfAbsent(
               var1, var0 -> new Animation(var0.isEnabled() ? 1.0F : 0.0F, var0.isEnabled() ? 1.0F : 0.0F, 350, EasingUtil.Easing.EASE_IN_OUT_CUBIC)
            );
         float var7 = var6.animateTo(var1.isEnabled() ? 1.0F : 0.0F);
         Color var9 = this.a_Color_float(var5);
         if (this.moduleColor.getValue().equalsIgnoreCase("Rainbow")) {
            long var10 = (long)var5 / Night.fontManager.getFontHeight() * this.rainbowOffset.getValue().longValue() * 10L;
            var9 = ColorUtil.a_Color_long(var10);
         } else if (this.moduleColor.getValue().equals("Random")) {
            var9 = ColorUtil.a_Color_String(var1.getName());
         }

         boolean var14 = (
               this.moduleColor.getValue().equals("Rainbow") || this.moduleColor.getValue().equals("Default") && this.color.getValue().equals("Rainbow")
            )
            && this.rainbow.getValue().equals("Horizontal");
         this.a(var1, var2, var4, var5, var7, var9, var14);
      }
   }

   private void a(Module var1, GuiGraphicsExtractor var2, float var3, float var4, float var5, Color var6, boolean var7) {
      String var8 = var1.getKeybind() != 0 ? "[" + KeyNameUtil.a_String_int(var1.getKeybind()) + "] " : "";
      String var9 = var1.getName();
      String var10 = !var1.getDisplayInfo().isEmpty() && this.metaData.getValue() ? " [" + var1.getDisplayInfo() + "]" : "";
      String var11 = var8 + var9 + var10;
      if (!var11.isEmpty()) {
         Color var12 = new Color(170, 170, 170);
         Color var13 = Color.WHITE;
         float var14 = Night.fontManager.getStringWidth(var11);
         float var15 = 0.0F;
         MutableComponent var16 = Component.empty();
         int var17 = var8.length();
         int var18 = var9.length();

         for (int var19 = 0; var19 < var11.length(); var19++) {
            char var20 = var11.charAt(var19);
            String var21 = String.valueOf(var20);
            float var22 = Night.fontManager.getStringWidth(var21);
            float var23 = var14 > 0.0F ? (var15 + var22 / 2.0F) / var14 : 0.0F;
            var15 += var22;
            Color var24 = var12;
            Color var25;
            if (var19 < var17) {
               if (var20 != '[' && var20 != ']' && var20 != ' ') {
                  var25 = var13;
               } else {
                  var25 = var12;
               }
            } else if (var19 < var17 + var18) {
               if (var7) {
                  var25 = ColorUtil.a_Color_long(var19 * this.rainbowOffset.getValue().longValue() * 5L);
               } else {
                  var25 = var6;
               }
            } else if (var20 != '[' && var20 != ']' && var20 != ' ') {
               var25 = var13;
            } else {
               var25 = var12;
            }

            float var26 = Math.max(0.0F, Math.min(1.0F, (var5 * (1.0F + 0.35F) - var23) / 0.35F));
            float var27 = var26 * var26 * (3.0F - 2.0F * var26);
            Color var28 = a(var24, var25, var27);
            var16.append(Component.literal(var21).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(var28.getRGB()))));
         }

         Matrix3x2fStack var29 = var2.pose();
         var29.pushMatrix();
         var29.translate(var3, var4);
         Night.fontManager.drawFormattedText(var2, var16.getVisualOrderText(), 0, 0, Color.WHITE);
         var29.popMatrix();
      }
   }

   private void a(GuiGraphicsExtractor var1, String var2, float var3, float var4) {
      this.a(var1, var2, var3, var4, null);
   }

   public void a(GuiGraphicsExtractor var1, String var2, float var3, float var4, Color var5) {
      this.a(var1, var2, var3, var4, this.color.getValue().equals("Rainbow") && this.rainbow.getValue().equals("Horizontal"), var5);
   }

   public void a(GuiGraphicsExtractor var1, String var2, float var3, float var4, boolean var5, Color var6) {
      if (var6 == null) {
         var6 = this.a_Color_float(var4);
      }

      Matrix3x2fStack var7 = var1.pose();
      var7.pushMatrix();
      var7.translate(var3, var4);
      if (var5) {
         Night.fontManager.drawRainbowText(var1, var2, 0, 0, this.rainbowOffset.getValue().longValue() * 5L);
      } else {
         Night.fontManager.drawShadowedText(var1, var2, 0, 0, var6);
      }

      var7.popMatrix();
   }

   private Color a_Color_float(float var1) {
      if (this.color.getValue().equalsIgnoreCase("Rainbow")) {
         long var4 = (long)var1 / Night.fontManager.getFontHeight() * this.rainbowOffset.getValue().longValue() * 10L;
         return ColorUtil.a_Color_long(var4);
      } else if (this.color.getValue().equalsIgnoreCase("Wave")) {
         long var2 = (long)var1 / Night.fontManager.getFontHeight() * this.rainbowOffset.getValue().longValue() * 20L;
         return ColorUtil.a(ColorUtil.d(), var2);
      } else {
         return this.color.getValue().equalsIgnoreCase("Custom") ? ColorUtil.a(this.customColor.getColor(), 255) : ColorUtil.d();
      }
   }

   @Override
   public Setting getSetting(String var1) {
      return !var1.equalsIgnoreCase("ModuleListMode") && !var1.equalsIgnoreCase("Mode") ? super.getSetting(var1) : this.moduleListMode;
   }

   private String a_String_Module(Module var1) {
      boolean var2 = this.moduleListMode.getValue().equalsIgnoreCase("Newbie");
      String var3 = "";
      if (var2 && var1.getKeybind() != 0) {
         var3 = ChatFormatting.GRAY + "[" + ChatFormatting.WHITE + KeyNameUtil.a_String_int(var1.getKeybind()) + ChatFormatting.GRAY + "] ";
      }

      return !var2 && !var1.isEnabled()
         ? var3
            + ChatFormatting.GRAY
            + var1.getName()
            + (
               !var1.getDisplayInfo().isEmpty() && this.metaData.getValue()
                  ? ChatFormatting.DARK_GRAY + " [" + ChatFormatting.GRAY + var1.getDisplayInfo() + ChatFormatting.DARK_GRAY + "]"
                  : ""
            )
         : var3
            + var1.getName()
            + (
               !var1.getDisplayInfo().isEmpty() && this.metaData.getValue()
                  ? ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + var1.getDisplayInfo() + ChatFormatting.GRAY + "]"
                  : ""
            );
   }

   private String a_String_MobEffectInstance(MobEffectInstance var1) {
      String var2;
      if (var1.isInfiniteDuration()) {
         var2 = "**:**";
      } else {
         int var3 = Math.round(var1.getDuration() / mc.level.tickRateManager().tickrate());
         var2 = String.format("%02d:%02d", var3 / 60, var3 % 60);
      }

      return ((MobEffect)var1.getEffect().value()).getDisplayName().getString() + " " + (var1.getAmplifier() + 1) + " " + ChatFormatting.WHITE + var2;
   }

   private ChatFormatting x() {
      return this.inversion.getValue() ? ChatFormatting.GRAY : ChatFormatting.RESET;
   }

   private ChatFormatting y() {
      return this.inversion.getValue() ? ChatFormatting.RESET : ChatFormatting.WHITE;
   }

   static class LowArmorToast {
      int a = -1;
      private int b = -1;
      private long c = 0L;
      private int d = 0;

      public void a(int var1) {
         if (this.a == -1) {
            this.a = var1;
            this.b = var1;
         } else {
            if (var1 != this.a) {
               this.b = this.a;
               this.a = var1;
               this.d = var1 < this.b ? -1 : 1;
               this.c = System.currentTimeMillis();
            }
         }
      }

      public void a(GuiGraphicsExtractor var1, boolean var2) {
         if (this.a > 0 || this.b > 0) {
            String var3 = String.valueOf(Math.max(0, this.a));
            int var4 = Night.fontManager != null ? Night.fontManager.getFontHeight() : 9;
            long var5 = System.currentTimeMillis() - this.c;
            float var7 = this.c == 0L ? 1.0F : Math.min(1.0F, (float)var5 / 320.0F);
            if (var2 && !(var7 >= 1.0F) && this.b != -1 && this.b != this.a) {
               float var20 = EasingUtil.a(var7, EasingUtil.Easing.EASE_OUT_CUBIC);
               String var21 = String.valueOf(Math.max(0, this.b));
               int var22 = Night.fontManager != null ? Night.fontManager.getStringWidth(var3) : Minecraft.getInstance().font.width(var3);
               int var11 = Night.fontManager != null ? Night.fontManager.getStringWidth(var21) : Minecraft.getInstance().font.width(var21);
               float var12 = 17 - var22;
               float var13 = 17 - var11;
               float var14 = var4 + 3;
               float var15;
               float var16;
               if (this.d < 0) {
                  var15 = var20 * var14;
                  var16 = -(1.0F - var20) * var14;
               } else {
                  var15 = -var20 * var14;
                  var16 = (1.0F - var20) * var14;
               }

               int var17 = Math.max(0, Math.min(255, Math.round((1.0F - var20) * 255.0F)));
               int var18 = Math.max(0, Math.min(255, Math.round(var20 * 255.0F)));
               if (var17 > 5) {
                  Color var19 = new Color(255, 255, 255, var17);
                  if (Night.fontManager != null) {
                     Night.fontManager.drawShadowedText(var1, var21, (int)var13, (int)(9.0F + var15), var19);
                  } else {
                     var1.text(Minecraft.getInstance().font, var21, (int)var13, (int)(9.0F + var15), var17 << 24 | 16777215, true);
                  }
               }

               if (var18 > 5) {
                  Color var23 = new Color(255, 255, 255, var18);
                  if (Night.fontManager != null) {
                     Night.fontManager.drawShadowedText(var1, var3, (int)var12, (int)(9.0F + var16), var23);
                  } else {
                     var1.text(Minecraft.getInstance().font, var3, (int)var12, (int)(9.0F + var16), var18 << 24 | 16777215, true);
                  }
               }
            } else {
               int var8 = Night.fontManager != null ? Night.fontManager.getStringWidth(var3) : Minecraft.getInstance().font.width(var3);
               float var9 = 17 - var8;
               float var10 = 9.0F;
               if (Night.fontManager != null) {
                  Night.fontManager.drawShadowedText(var1, var3, (int)var9, (int)var10, Color.WHITE);
               } else {
                  var1.text(Minecraft.getInstance().font, var3, (int)var9, (int)var10, -1, true);
               }
            }
         }
      }
   }

   public record ModuleListEntry(Module module, String text) {
   }

   public record PotionEntry(String text, Identifier texture, Color color) {
   }

   public record RadarEntry(Player player, String text, Identifier texture) {
   }
}
