import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * NightRenamer — Comprehensive ASM Semantic Remapper for Night Client v26.
 *
 * Maps every obfuscated class, inner class, enum constant, record component,
 * setting field, event field/method, manager field/method, module field/method,
 * and local variable slot into clean, self-documenting Java identifiers.
 */
public class NightRenamer {

    private static final Map<String, ClassNode> classes = new LinkedHashMap<>();
    private static final Map<String, String> classMap = new HashMap<>();
    private static final Map<String, String> fieldMap = new HashMap<>();
    private static final Map<String, String> methodMap = new HashMap<>();
    private static final Map<String, String> annotationMethodMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java NightRenamer <inputDir> <outputDir>");
            System.exit(1);
        }
        Path inDir = Paths.get(args[0]);
        Path outDir = Paths.get(args[1]);

        // 1. Load all .class files
        List<Path> classFiles;
        try (var stream = Files.walk(inDir)) {
            classFiles = stream.filter(p -> p.toString().endsWith(".class")).sorted().toList();
        }
        for (Path p : classFiles) {
            byte[] bytes = Files.readAllBytes(p);
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            classes.put(cn.name, cn);
        }
        System.out.println("Loaded " + classes.size() + " classes.");

        // 2. Build class mappings
        initClassMappings();
        resolveInnerClassMappings();

        // 3. Build explicit semantic field & method mappings
        initSemanticFieldAndMethodMappings();

        // 4. Auto-discover Enum constant names & $values()
        discoverEnumMappings();

        // 5. Auto-discover Record component names & accessors
        discoverRecordMappings();

        // 6. Auto-discover Setting field names in Modules (fc) and ProxyModules (mh)
        discoverSettingFieldMappings();

        // 7. Auto-discover Event fields and getter/setter methods (bk..cz)
        discoverEventMappings();

        // 8. Auto-discover PingBypass packet fields & getters (mq..nh)
        discoverPingBypassPacketMappings();

        // 8b. Auto-disambiguate overloaded methods and private superclass methods
        disambiguateOverloadsAndPrivateMethods();

        // 9. Compact local variable slots and strip leftover synthetic bridge artifacts if needed
        for (ClassNode cn : classes.values()) {
            for (MethodNode mn : cn.methods) {
                compactLocalVariables(cn, mn);
            }
        }

        // 10. Create inheritance-aware Remapper and remap all classes
        Remapper remapper = new SemanticRemapper();

        int written = 0;
        for (ClassNode cn : classes.values()) {
            ClassNode remappedNode = new ClassNode();
            ClassRemapper classRemapper = new CustomClassRemapper(remappedNode, remapper);
            cn.accept(classRemapper);

            // Fix SourceFile attribute to match new simple class name
            String mappedClassName = remapper.mapType(cn.name);
            String topLevelName = mappedClassName;
            int dollarIdx = topLevelName.indexOf('$');
            if (dollarIdx > 0) {
                topLevelName = topLevelName.substring(0, dollarIdx);
            }
            int slashIdx = topLevelName.lastIndexOf('/');
            String simpleTopName = slashIdx >= 0 ? topLevelName.substring(slashIdx + 1) : topLevelName;
            remappedNode.sourceFile = simpleTopName + ".java";

            ClassWriter cw = new ClassWriter(0);
            remappedNode.accept(cw);

            Path targetPath = outDir.resolve(mappedClassName + ".class");
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, cw.toByteArray());
            written++;
        }

        // Copy non-class resources if any exist in inDir
        try (var stream = Files.walk(inDir)) {
            for (Path p : stream.filter(Files::isRegularFile).filter(p -> !p.toString().endsWith(".class")).toList()) {
                Path rel = inDir.relativize(p);
                Path dest = outDir.resolve(rel);
                Files.createDirectories(dest.getParent());
                Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        System.out.println("Successfully remapped and wrote " + written + " classes to " + outDir);
        System.out.println("Total class mappings:  " + classMap.size());
        System.out.println("Total field mappings:  " + fieldMap.size());
        System.out.println("Total method mappings: " + methodMap.size());
    }

    private static void initClassMappings() {
        // Commands
        mapClass("ab", "night/command/Command");
        mapClass("ac", "night/command/CommandManager");
        mapClass("ad", "night/command/CommandInfo");
        mapClass("ae", "night/command/impl/AllCommand");
        mapClass("af", "night/command/impl/BindCommand");
        mapClass("ag", "night/command/impl/ChatNotifyCommand");
        mapClass("ah", "night/command/impl/ConfigCommand");
        mapClass("ai", "night/command/impl/CoordsCommand");
        mapClass("aj", "night/command/impl/DrawnCommand");
        mapClass("ak", "night/command/impl/FakePlayerCommand");
        mapClass("al", "night/command/impl/FolderCommand");
        mapClass("am", "night/command/impl/FriendCommand");
        mapClass("an", "night/command/impl/GrabCommand");
        mapClass("ao", "night/command/impl/HelpCommand");
        mapClass("ap", "night/command/impl/ItemDropCommand");
        mapClass("aq", "night/command/impl/KitCommand");
        mapClass("ar", "night/command/impl/MacroCommand");
        mapClass("as", "night/command/impl/ModuleCommand");
        mapClass("at", "night/command/impl/ModulesCommand");
        mapClass("au", "night/command/impl/PeekCommand");
        mapClass("av", "night/command/impl/PrefixCommand");
        mapClass("aw", "night/command/impl/SayCommand");
        mapClass("ax", "night/command/impl/StashFinderCommand");
        mapClass("ay", "night/command/impl/ToggleCommand");
        mapClass("az", "night/command/impl/WaypointCommand");

        // Security & Loader
        mapClass("ba", "night/security/AESGCMDecryptor");
        mapClass("bb", "night/security/EncryptedAssetLoader");
        mapClass("bc", "night/security/JarSignatureVerifier");
        mapClass("bd", "night/security/ClasspathScanner");
        mapClass("be", "night/security/EncryptedClassLoader");
        mapClass("bf", "night/security/EncryptedModuleBootstrap");
        mapClass("bf$aa", "night/security/EncryptedModuleBootstrap$PayloadLoadException");
        mapClass("ed", "night/security/WindowsDpapiProtector");
        mapClass("ee", "night/security/HardwareIdGenerator");
        mapClass("ef", "night/security/LicenseHttpClient");
        mapClass("ef$aa", "night/security/LicenseHttpClient$HeartbeatResponse");
        mapClass("eg", "night/security/LicenseCacheStorage");
        mapClass("eh", "night/security/LicenseService");
        mapClass("ei", "night/security/Ed25519Verifier");
        mapClass("ej", "night/security/EnvironmentIntegrityGuard");

        // Event System
        mapClass("bg", "night/events/Event");
        mapClass("bh", "night/events/EventBus");
        mapClass("bi", "night/events/EventListener");
        mapClass("bi$aa", "night/events/EventListener$ReflectiveConsumer");
        mapClass("bj", "night/events/EventTarget");
        mapClass("bk", "night/events/impl/AttackBlockEvent");
        mapClass("bl", "night/events/impl/AttackEntityEvent");
        mapClass("bm", "night/events/impl/BreakBlockEvent");
        mapClass("bn", "night/events/impl/ChangeHandEvent");
        mapClass("bo", "night/events/impl/CameraBobEvent");
        mapClass("bp", "night/events/impl/ReachDistanceEvent");
        mapClass("bq", "night/events/impl/ChatSendEvent");
        mapClass("br", "night/events/impl/ClientConnectEvent");
        mapClass("bs", "night/events/impl/ClientDisconnectEvent");
        mapClass("bt", "night/events/impl/RotationUpdateEvent");
        mapClass("bu", "night/events/impl/CommandExecuteEvent");
        mapClass("bv", "night/events/impl/ConsumeItemEvent");
        mapClass("bw", "night/events/impl/BlockPlaceEvent");
        mapClass("bx", "night/events/impl/EntitySpawnEvent");
        mapClass("by", "night/events/impl/InputUpdateEvent");
        mapClass("bz", "night/events/impl/ItemUseEvent");
        mapClass("ca", "night/events/impl/KeyInputEvent");
        mapClass("cb", "night/events/impl/MouseButtonEvent");
        mapClass("cc", "night/events/impl/PacketReceiveEvent");
        mapClass("cd", "night/events/impl/PacketSendEvent");
        mapClass("cd$aa", "night/events/impl/PacketSendEvent$Post");
        mapClass("ce", "night/events/impl/PlayerJoinEvent");
        mapClass("cf", "night/events/impl/PlayerDeathEvent");
        mapClass("cg", "night/events/impl/PlayerLeaveEvent");
        mapClass("ch", "night/events/impl/PlayerJumpEvent");
        mapClass("ch$aa", "night/events/impl/PlayerJumpEvent$Post");
        mapClass("ci", "night/events/impl/BlockBreakProgressEvent");
        mapClass("cj", "night/events/impl/PlayerMoveEvent");
        mapClass("ck", "night/events/impl/TotemPopEvent");
        mapClass("cl", "night/events/impl/PushOutOfBlocksEvent");
        mapClass("cm", "night/events/impl/UpdateWalkingPlayerEvent");
        mapClass("cn", "night/events/impl/FireworkTickEvent");
        mapClass("co", "night/events/impl/Render2DEvent");
        mapClass("cp", "night/events/impl/Render3DEvent");
        mapClass("cp$aa", "night/events/impl/Render3DEvent$Post");
        mapClass("cq", "night/events/impl/RenderHandEvent");
        mapClass("cr", "night/events/impl/ServerConnectEvent");
        mapClass("cs", "night/events/impl/SettingChangeEvent");
        mapClass("ct", "night/events/impl/TargetDeathEvent");
        mapClass("cu", "night/events/impl/TickEvent");
        mapClass("cu$aa", "night/events/impl/TickEvent$Post");
        mapClass("cv", "night/events/impl/ModuleToggleEvent");
        mapClass("cw", "night/events/impl/WindowResizeEvent");
        mapClass("cx", "night/events/impl/MouseUpdateEvent");
        mapClass("cy", "night/events/impl/MotionSyncEvent");
        mapClass("cy$aa", "night/events/impl/MotionSyncEvent$Post");
        mapClass("cz", "night/events/impl/UpdateVelocityEvent");

        // GUI & ClickGUI & Account Screens
        mapClass("da", "night/gui/ClickGuiScreen");
        mapClass("db", "night/gui/HudEditorScreen");
        mapClass("db$aa", "night/gui/HudEditorScreen$CategoryGroupButton");
        mapClass("dc", "night/gui/ImageSelectorScreen");
        mapClass("dd", "night/gui/PeekChestMenu");
        mapClass("de", "night/gui/PeekContainerScreen");
        mapClass("df", "night/gui/WhitelistEditorScreen");
        mapClass("df$aa", "night/gui/WhitelistEditorScreen$EntryItem");
        mapClass("dg", "night/gui/component/GuiComponent");
        mapClass("dh", "night/gui/component/DescriptionPanel");
        mapClass("di", "night/gui/component/ExpandableComponent");
        mapClass("dj", "night/gui/component/CategoryFrame");
        mapClass("dk", "night/gui/component/PingBypassFrame");
        mapClass("dl", "night/gui/component/SearchBarComponent");
        mapClass("dm", "night/gui/screen/AccountsButtonHelper");
        mapClass("dn", "night/gui/component/impl/KeybindComponent");
        mapClass("do", "night/gui/component/impl/BooleanComponent");
        mapClass("dp", "night/gui/component/impl/CategorySettingComponent");
        mapClass("dq", "night/gui/component/impl/ColorComponent");
        mapClass("dr", "night/gui/component/impl/HudElementComponent");
        mapClass("ds", "night/gui/component/impl/ImageSettingComponent");
        mapClass("dt", "night/gui/component/impl/ModeComponent");
        mapClass("du", "night/gui/component/impl/ModuleComponent");
        mapClass("dv", "night/gui/component/impl/NumberComponent");
        mapClass("dw", "night/gui/component/impl/ProxyModuleComponent");
        mapClass("dx", "night/gui/component/impl/StringComponent");
        mapClass("dy", "night/gui/component/impl/WhitelistComponent");
        mapClass("dy$aa", "night/gui/component/impl/WhitelistComponent$ColoredSegment");
        mapClass("dy$ab", "night/gui/component/impl/WhitelistComponent$MatchSpan");
        mapClass("dz", "night/gui/screen/AccountManagerScreen");
        mapClass("ea", "night/gui/screen/AddOfflineAccountScreen");
        mapClass("eb", "night/gui/screen/MicrosoftLoginScreen");
        mapClass("ec", "night/gui/screen/NightMainMenuScreen");
        mapClass("ec$aa", "night/gui/screen/NightMainMenuScreen$MenuButton");

        // Managers
        mapClass("ek", "night/manager/AccountManager");
        mapClass("el", "night/manager/SpeedManager");
        mapClass("em", "night/manager/ChatManager");
        mapClass("en", "night/manager/ConfigManager");
        mapClass("eo", "night/manager/FontManager");
        mapClass("ep", "night/manager/FriendManager");
        mapClass("eq", "night/manager/HudElementManager");
        mapClass("eq$aa", "night/manager/HudElementManager$HudEntry");
        mapClass("er", "night/manager/CustomImageManager");
        mapClass("er$aa", "night/manager/CustomImageManager$CachedImage");
        mapClass("es", "night/manager/MacroManager");
        mapClass("et", "night/manager/NightUserManager");
        mapClass("eu", "night/manager/PositionManager");
        mapClass("ev", "night/manager/RenderPlacementManager");
        mapClass("ev$aa", "night/manager/RenderPlacementManager$AnimatedTarget");
        mapClass("ew", "night/manager/RotationManager");
        mapClass("ex", "night/manager/ServerManager");
        mapClass("ey", "night/manager/TargetManager");
        mapClass("ey$aa", "night/manager/TargetManager$TrackedTarget");
        mapClass("ez", "night/manager/TickTaskManager");
        mapClass("fa", "night/manager/WaypointManager");
        mapClass("fa$aa", "night/manager/WaypointManager$Waypoint");
        mapClass("fb", "night/manager/WorldStateManager");

        // Module Base & Access Interfaces
        mapClass("fc", "night/module/Module");
        mapClass("fc$aa", "night/module/Module$Category");
        mapClass("fd", "night/module/ModuleManager");
        mapClass("fe", "night/module/ModuleInfo");
        mapClass("ff", "night/module/access/AmbienceAccess");
        mapClass("fg", "night/module/access/AutoEnchantAccess");
        mapClass("fh", "night/module/access/AutoWalkAccess");
        mapClass("fi", "night/module/access/FOVModifierAccess");
        mapClass("fj", "night/module/access/FastClimbAccess");
        mapClass("fk", "night/module/access/FreecamAccess");
        mapClass("fl", "night/module/access/FullBrightAccess");
        mapClass("fm", "night/module/access/HoleSnapAccess");
        mapClass("fn", "night/module/access/MotionCameraAccess");
        mapClass("fo", "night/module/access/NoJumpDelayAccess");
        mapClass("fp", "night/module/access/PatchAccess");
        mapClass("fq", "night/module/access/ReachAccess");
        mapClass("fr", "night/module/access/SpeedAccess");
        mapClass("fs", "night/module/access/SprintAccess");
        mapClass("ft", "night/module/access/ViewClipAccess");

        // Combat Modules
        mapClass("fu", "night/module/impl/combat/AntiPearl");
        mapClass("fu$aa", "night/module/impl/combat/AntiPearl$PearlPrediction");
        mapClass("fu$ab", "night/module/impl/combat/AntiPearl$CandidateBlock");
        mapClass("fv", "night/module/impl/combat/AntiPhase");
        mapClass("fw", "night/module/impl/combat/AutoArmor");
        mapClass("fx", "night/module/impl/combat/AutoBowRelease");
        mapClass("fy", "night/module/impl/combat/AutoCrystal");
        mapClass("fy$aa", "night/module/impl/combat/AutoCrystal$DamageResult");
        mapClass("fy$ab", "night/module/impl/combat/AutoCrystal$PlaceTarget");
        mapClass("fz", "night/module/impl/combat/AutoLog");
        mapClass("ga", "night/module/impl/combat/AutoMace");
        mapClass("gb", "night/module/impl/combat/AutoMine");
        mapClass("gb$aa", "night/module/impl/combat/AutoMine$MineCandidate");
        mapClass("gc", "night/module/impl/combat/AutoPot");
        mapClass("gd", "night/module/impl/combat/AutoTotem");
        mapClass("ge", "night/module/impl/combat/AutoTrap");
        mapClass("ge$aa", "night/module/impl/combat/AutoTrap$TrapCalculation");
        mapClass("gf", "night/module/impl/combat/AutoWeb");
        mapClass("gg", "night/module/impl/combat/BedAura");
        mapClass("gg$aa", "night/module/impl/combat/BedAura$BedTarget");
        mapClass("gg$ab", "night/module/impl/combat/BedAura$DirectionalBedTarget");
        mapClass("gg$ac", "night/module/impl/combat/BedAura$BedPlacement");
        mapClass("gh", "night/module/impl/combat/Blocker");
        mapClass("gh$aa", "night/module/impl/combat/Blocker$QueuedBlock");
        mapClass("gh$ab", "night/module/impl/combat/Blocker$ProtectArea");
        mapClass("gh$ac", "night/module/impl/combat/Blocker$CrystalBlockPair");
        mapClass("gi", "night/module/impl/combat/CombatPlace");
        mapClass("gj", "night/module/impl/combat/Criticals");
        mapClass("gk", "night/module/impl/combat/CrystalRaytraceHelper");
        mapClass("gk$aa", "night/module/impl/combat/CrystalRaytraceHelper$HitCandidate");
        mapClass("gl", "night/module/impl/combat/HoleFill");
        mapClass("gl$aa", "night/module/impl/combat/HoleFill$FillCalculation");
        mapClass("gm", "night/module/impl/combat/KillAura");
        mapClass("gn", "night/module/impl/combat/KillSay");
        mapClass("go", "night/module/impl/combat/MaceSpoof");
        mapClass("gp", "night/module/impl/combat/Mainhand");
        mapClass("gq", "night/module/impl/combat/ObsidianFarmer");
        mapClass("gq$aa", "night/module/impl/combat/ObsidianFarmer$Stage");
        mapClass("gr", "night/module/impl/combat/OpponentScaffold");
        mapClass("gr$aa", "night/module/impl/combat/OpponentScaffold$ScaffoldTarget");
        mapClass("gs", "night/module/impl/combat/PearlChase");
        mapClass("gs$aa", "night/module/impl/combat/PearlChase$ThrowSolution");
        mapClass("gs$ab", "night/module/impl/combat/PearlChase$SimulatedLanding");
        mapClass("gs$ac", "night/module/impl/combat/PearlChase$AngleTrajectory");
        mapClass("gt", "night/module/impl/combat/PistonCrystal");
        mapClass("gu", "night/module/impl/combat/PistonKick");
        mapClass("gu$aa", "night/module/impl/combat/PistonKick$PushPlan");
        mapClass("gv", "night/module/impl/combat/SelfBow");
        mapClass("gw", "night/module/impl/combat/SelfFill");
        mapClass("gx", "night/module/impl/combat/SelfTrap");
        mapClass("gy", "night/module/impl/combat/SpearKill");
        mapClass("gz", "night/module/impl/combat/Suicide");
        mapClass("ha", "night/module/impl/combat/Surround");
        mapClass("ha$aa", "night/module/impl/combat/Surround$MinedBlockTracker");
        mapClass("ha$ab", "night/module/impl/combat/Surround$CrystalBreakEntry");

        // Core Modules
        mapClass("hb", "night/module/impl/core/Capes");
        mapClass("hc", "night/module/impl/core/ClickGuiModule");
        mapClass("hd", "night/module/impl/core/ColorModule");
        mapClass("he", "night/module/impl/core/CommandsModule");
        mapClass("hf", "night/module/impl/core/FontModule");
        mapClass("hg", "night/module/impl/core/FriendModule");
        mapClass("hh", "night/module/impl/core/HudEditorModule");
        mapClass("hi", "night/module/impl/core/HudModule");
        mapClass("hi$aa", "night/module/impl/core/HudModule$ModuleListEntry");
        mapClass("hi$ab", "night/module/impl/core/HudModule$RadarEntry");
        mapClass("hi$ac", "night/module/impl/core/HudModule$PotionEntry");
        mapClass("hi$ad", "night/module/impl/core/HudModule$LowArmorToast");
        mapClass("hj", "night/module/impl/core/IRCModule");
        mapClass("hj$aa", "night/module/impl/core/IRCModule$IRCWebSocketListener");
        mapClass("hk", "night/module/impl/core/IgnoreNaked");
        mapClass("hl", "night/module/impl/core/MainMenuModule");
        mapClass("hm", "night/module/impl/core/NoMiddleClick");
        mapClass("hn", "night/module/impl/core/PatchModule");
        mapClass("ho", "night/module/impl/core/PingBypassModule");
        mapClass("ho$aa", "night/module/impl/core/PingBypassModule$PendingPacket");
        mapClass("hp", "night/module/impl/core/DiscordRPCModule");
        mapClass("hq", "night/module/impl/core/RendersModule");
        mapClass("hr", "night/module/impl/core/RotationsModule");

        // Misc Modules
        mapClass("hs", "night/module/impl/misc/Announcer");
        mapClass("ht", "night/module/impl/misc/AutoEscape");
        mapClass("hu", "night/module/impl/misc/AutoLogin");
        mapClass("hv", "night/module/impl/misc/AutoReconnect");
        mapClass("hw", "night/module/impl/misc/AutoRespawn");
        mapClass("hx", "night/module/impl/misc/BetterChat");
        mapClass("hy", "night/module/impl/misc/ExtraTab");
        mapClass("hz", "night/module/impl/misc/FOVModifier");
        mapClass("ia", "night/module/impl/misc/FakePlayer");
        mapClass("ia$aa", "night/module/impl/misc/FakePlayer$RecordedFrame");
        mapClass("ia$ab", "night/module/impl/misc/FakePlayer$RecordState");
        mapClass("ib", "night/module/impl/misc/FastLatency");
        mapClass("ic", "night/module/impl/misc/Handshake");
        mapClass("id", "night/module/impl/misc/LagNotify");
        mapClass("ie", "night/module/impl/misc/MouseFix");
        mapClass("if", "night/module/impl/misc/NoPacketKick");
        mapClass("ig", "night/module/impl/misc/NoSoundLag");
        mapClass("ih", "night/module/impl/misc/Notifications");
        mapClass("ih$aa", "night/module/impl/misc/Notifications$ToastNotification");
        mapClass("ij", "night/module/impl/misc/Scaffold");
        mapClass("ik", "night/module/impl/misc/ScreenshotModule");
        mapClass("il", "night/module/impl/misc/ShulkerInfo");
        mapClass("im", "night/module/impl/misc/SoundsModule");
        mapClass("in", "night/module/impl/misc/Spammer");
        mapClass("io", "night/module/impl/misc/Welcomer");

        // Movement Modules
        mapClass("ip", "night/module/impl/movement/AirStuck");
        mapClass("iq", "night/module/impl/movement/Anchor");
        mapClass("ir", "night/module/impl/movement/AntiVoid");
        mapClass("is", "night/module/impl/movement/ArrowControls");
        mapClass("it", "night/module/impl/movement/AutoWalk");
        mapClass("iu", "night/module/impl/movement/BounceDebug");
        mapClass("iv", "night/module/impl/movement/ElytraFly");
        mapClass("iw", "night/module/impl/movement/EntityControl");
        mapClass("ix", "night/module/impl/movement/FakeLag");
        mapClass("iy", "night/module/impl/movement/FastClimb");
        mapClass("iz", "night/module/impl/movement/FastSwim");
        mapClass("ja", "night/module/impl/movement/FastWeb");
        mapClass("jb", "night/module/impl/movement/HighJump");
        mapClass("jc", "night/module/impl/movement/HitboxDesync");
        mapClass("jd", "night/module/impl/movement/HoleSnap");
        mapClass("je", "night/module/impl/movement/InventoryControl");
        mapClass("jf", "night/module/impl/movement/NoFall");
        mapClass("jg", "night/module/impl/movement/NoJumpDelay");
        mapClass("jh", "night/module/impl/movement/NoSlow");
        mapClass("ji", "night/module/impl/movement/Phase");
        mapClass("ji$aa", "night/module/impl/movement/Phase$PearlAngle");
        mapClass("jj", "night/module/impl/movement/ReverseStep");
        mapClass("jk", "night/module/impl/movement/Speed");
        mapClass("jl", "night/module/impl/movement/Sprint");
        mapClass("jm", "night/module/impl/movement/Step");
        mapClass("jn", "night/module/impl/movement/TickShift");
        mapClass("jo", "night/module/impl/movement/Velocity");

        // Player & KingMC Modules
        mapClass("jp", "night/module/impl/player/AirPlace");
        mapClass("jq", "night/module/impl/player/AntiHunger");
        mapClass("jr", "night/module/impl/kingmc/AutoEnchant");
        mapClass("jr$aa", "night/module/impl/kingmc/AutoEnchant$GearType");
        mapClass("js", "night/module/impl/kingmc/AutoShop");
        mapClass("js$aa", "night/module/impl/kingmc/AutoShop$BuyOrder");
        mapClass("js$ab", "night/module/impl/kingmc/AutoShop$ShopItem");
        mapClass("js$ac", "night/module/impl/kingmc/AutoShop$ShopState");
        mapClass("jt", "night/module/impl/player/ChestStealer");
        mapClass("jt$aa", "night/module/impl/player/ChestStealer$ActionMode");
        mapClass("ju", "night/module/impl/player/FastPlace");
        mapClass("jv", "night/module/impl/player/InventoryCleaner");
        mapClass("jw", "night/module/impl/player/InventorySorter");
        mapClass("jx", "night/module/impl/player/KeyAction");
        mapClass("jy", "night/module/impl/kingmc/KingMCTotem");
        mapClass("jz", "night/module/impl/player/MultiTask");
        mapClass("ka", "night/module/impl/player/NoBreakDelay");
        mapClass("kb", "night/module/impl/player/NoEntityTrace");
        mapClass("kc", "night/module/impl/player/NoInteract");
        mapClass("kd", "night/module/impl/player/NoRotate");
        mapClass("ke", "night/module/impl/player/Reach");
        mapClass("kf", "night/module/impl/player/Rekit");
        mapClass("kf$aa", "night/module/impl/player/Rekit$RekitState");
        mapClass("kf$ab", "night/module/impl/player/Rekit$KitSlotEntry");
        mapClass("kg", "night/module/impl/player/Replenish");
        mapClass("kh", "night/module/impl/player/RotationLock");
        mapClass("ki", "night/module/impl/player/SpeedMine");
        mapClass("ki$aa", "night/module/impl/player/SpeedMine$MineTask");
        mapClass("ki$ab", "night/module/impl/player/SpeedMine$MineCandidate");
        mapClass("ki$ac", "night/module/impl/player/SpeedMine$FadeAnimation");
        mapClass("ki$ad", "night/module/impl/player/SpeedMine$TargetMineEntry");
        mapClass("kj", "night/module/impl/player/Swing");
        mapClass("kk", "night/module/impl/player/ThrowItem");
        mapClass("kl", "night/module/impl/player/TimerModule");
        mapClass("km", "night/module/impl/player/XCarry");

        // Visuals Modules
        mapClass("kn", "night/module/impl/visuals/Ambience");
        mapClass("ko", "night/module/impl/visuals/ArrowTracers");
        mapClass("kp", "night/module/impl/visuals/AspectRatio");
        mapClass("kq", "night/module/impl/visuals/Atmosphere");
        mapClass("kr", "night/module/impl/visuals/BlockHighlight");
        mapClass("ks", "night/module/impl/visuals/BreakHighlight");
        mapClass("ks$aa", "night/module/impl/visuals/BreakHighlight$BreakingBlockEntry");
        mapClass("kt", "night/module/impl/visuals/Chams");
        mapClass("ku", "night/module/impl/visuals/ChestScan");
        mapClass("kv", "night/module/impl/visuals/DeathEffects");
        mapClass("kw", "night/module/impl/visuals/ESP");
        mapClass("kw$aa", "night/module/impl/visuals/ESP$ChunkListener");
        mapClass("kx", "night/module/impl/visuals/Freecam");
        mapClass("ky", "night/module/impl/visuals/FullBright");
        mapClass("kz", "night/module/impl/visuals/HoleESP");
        mapClass("la", "night/module/impl/visuals/Icons");
        mapClass("la$aa", "night/module/impl/visuals/Icons$IconMarker");
        mapClass("la$aa$aa", "night/module/impl/visuals/Icons$IconMarker$IconType");
        mapClass("lb", "night/module/impl/visuals/LogoutSpot");
        mapClass("lb$aa", "night/module/impl/visuals/LogoutSpot$LogoutEntry");
        mapClass("lb$ab", "night/module/impl/visuals/LogoutSpot$PlayerSnapshot");
        mapClass("lc", "night/module/impl/visuals/MotionCamera");
        mapClass("ld", "night/module/impl/visuals/NameTags");
        mapClass("le", "night/module/impl/visuals/NoRender");
        mapClass("lf", "night/module/impl/visuals/ParticlesModule");
        mapClass("lg", "night/module/impl/visuals/PhaseESP");
        mapClass("lg$aa", "night/module/impl/visuals/PhaseESP$PhaseBlock");
        mapClass("lg$ab", "night/module/impl/visuals/PhaseESP$BlockSafety");
        mapClass("lh", "night/module/impl/visuals/PopChams");
        mapClass("li", "night/module/impl/visuals/ShadersModule");
        mapClass("lj", "night/module/impl/visuals/StashFinder");
        mapClass("lj$aa", "night/module/impl/visuals/StashFinder$ChunkScanListener");
        mapClass("lk", "night/module/impl/visuals/TextESP");
        mapClass("lk$aa", "night/module/impl/visuals/TextESP$TemporaryLabel");
        mapClass("ll", "night/module/impl/visuals/Tracers");
        mapClass("lm", "night/module/impl/visuals/Trails");
        mapClass("lm$aa", "night/module/impl/visuals/Trails$TrailPoint");
        mapClass("ln", "night/module/impl/visuals/Trajectories");
        mapClass("lo", "night/module/impl/visuals/ViewClip");
        mapClass("lp", "night/module/impl/visuals/ViewModel");
        mapClass("lq", "night/module/impl/visuals/VoidESP");
        mapClass("lr", "night/module/impl/visuals/chestscan/HopperChainAnalyzer");
        mapClass("ls", "night/module/impl/visuals/chestscan/ChestScanStorage");
        mapClass("ls$aa", "night/module/impl/visuals/chestscan/ChestScanStorage$ChestFillState");
        mapClass("ls$ab", "night/module/impl/visuals/chestscan/ChestScanStorage$SavedChestEntry");
        mapClass("lt", "night/module/impl/visuals/stashfinder/StashFinderStorage");
        mapClass("lt$aa", "night/module/impl/visuals/stashfinder/StashFinderStorage$SavedStashData");
        mapClass("lt$ab", "night/module/impl/visuals/stashfinder/StashFinderStorage$StashEntry");
        mapClass("lu", "night/module/impl/visuals/stashfinder/StashWebhookNotifier");
        mapClass("lv", "night/module/impl/visuals/stashfinder/StructureChunkFilter");
        mapClass("lv$aa", "night/module/impl/visuals/stashfinder/StructureChunkFilter$StructureRule");

        // PingBypass Subsystem
        mapClass("lw", "night/pingbypass/PingBypassConfig");
        mapClass("lx", "night/pingbypass/PingBypassState");
        mapClass("ly", "night/pingbypass/PingBypassTimer");
        mapClass("lz", "night/pingbypass/handler/ProxyConfigurationPacketListener");
        mapClass("ma", "night/pingbypass/handler/ProxyHandshakePacketListener");
        mapClass("mb", "night/pingbypass/handler/ProxyLoginPacketListener");
        mapClass("mb$aa", "night/pingbypass/handler/ProxyLoginPacketListener$LoginState");
        mapClass("mc", "night/pingbypass/handler/ProxyPasswordPacketListener");
        mapClass("md", "night/pingbypass/handler/ProxyPlayPacketListener");
        mapClass("me", "night/pingbypass/handler/ProxyStatusPacketListener");
        mapClass("mf", "night/pingbypass/handler/ProxyWaitingPacketListener");
        mapClass("mg", "night/pingbypass/ClientInputForwarder");
        mapClass("mh", "night/pingbypass/module/ProxyModule");
        mapClass("mi", "night/pingbypass/module/ProxyModuleManager");
        mapClass("mj", "night/pingbypass/module/ProxyModuleSyncHelper");
        mapClass("mk", "night/pingbypass/module/impl/ProxyAutoCrystal");
        mapClass("mk$aa", "night/pingbypass/module/impl/ProxyAutoCrystal$PlaceTarget");
        mapClass("ml", "night/pingbypass/module/impl/ProxyAutoTotem");
        mapClass("mm", "night/pingbypass/protocol/PingBypassPayload");
        mapClass("mn", "night/pingbypass/protocol/PingBypassPacket");
        mapClass("mo", "night/pingbypass/protocol/PacketHandler");
        mapClass("mp", "night/pingbypass/protocol/PingBypassProtocol");
        mapClass("mq", "night/pingbypass/protocol/c2s/C2SFriendSyncPacket");
        mapClass("mr", "night/pingbypass/protocol/c2s/C2SInventoryActionPacket");
        mapClass("ms", "night/pingbypass/protocol/c2s/C2SConnectServerPacket");
        mapClass("mt", "night/pingbypass/protocol/c2s/C2SModuleTogglePacket");
        mapClass("mu", "night/pingbypass/protocol/c2s/C2SPlayerInputPacket");
        mapClass("mv", "night/pingbypass/protocol/c2s/C2SPasswordResponsePacket");
        mapClass("mw", "night/pingbypass/protocol/c2s/C2SSettingUpdatePacket");
        mapClass("mx", "night/pingbypass/protocol/c2s/C2SStayConnectedPacket");
        mapClass("my", "night/pingbypass/protocol/c2s/C2SMineBlockPacket");
        mapClass("mz", "night/pingbypass/protocol/s2c/S2CStatusMessagePacket");
        mapClass("na", "night/pingbypass/protocol/s2c/S2CMineProgressPacket");
        mapClass("nb", "night/pingbypass/protocol/s2c/S2CModuleStatePacket");
        mapClass("nc", "night/pingbypass/protocol/s2c/S2CPasswordRequestPacket");
        mapClass("nd", "night/pingbypass/protocol/s2c/S2CRenderPlacePosPacket");
        mapClass("ne", "night/pingbypass/protocol/s2c/S2CProxyDisconnectPacket");
        mapClass("nf", "night/pingbypass/protocol/s2c/S2CSettingSyncPacket");
        mapClass("ng", "night/pingbypass/protocol/s2c/S2CHeldSlotSyncPacket");
        mapClass("nh", "night/pingbypass/protocol/c2s/C2SWindowClickPacket");
        mapClass("ni", "night/pingbypass/world/LobbyWorldSender");
        mapClass("nj", "night/pingbypass/network/PingBypassSessionConnection");
        mapClass("nk", "night/pingbypass/PingBypassServer");
        mapClass("nk$aa", "night/pingbypass/PingBypassServer$LazyHolder");
        mapClass("nl", "night/pingbypass/network/RubberbandTracker");
        mapClass("nm", "night/pingbypass/world/VanillaRegistryCache");
        mapClass("nn", "night/pingbypass/network/S2CPacketForwarder");
        mapClass("no", "night/pingbypass/network/BackendTransferRewirer");
        mapClass("np", "night/pingbypass/world/WorldStateReplayer");

        // Settings
        mapClass("nr", "night/setting/Setting");
        mapClass("nr$aa", "night/setting/Setting$VisibilityCondition");
        mapClass("ns", "night/setting/impl/KeybindSetting");
        mapClass("ns$aa", "night/setting/impl/KeybindSetting$KeyCondition");
        mapClass("nt", "night/setting/impl/BooleanSetting");
        mapClass("nt$aa", "night/setting/impl/BooleanSetting$BooleanCondition");
        mapClass("nu", "night/setting/impl/CategorySetting");
        mapClass("nu$aa", "night/setting/impl/CategorySetting$CategoryCondition");
        mapClass("nv", "night/setting/impl/ColorSetting");
        mapClass("nv$aa", "night/setting/impl/ColorSetting$ColorValue");
        mapClass("nv$ab", "night/setting/impl/ColorSetting$ColorCondition");
        mapClass("nv$ab$aa", "night/setting/impl/ColorSetting$ColorCondition$PropertyType");
        mapClass("nw", "night/setting/impl/ImageSetting");
        mapClass("nx", "night/setting/impl/ModeSetting");
        mapClass("nx$aa", "night/setting/impl/ModeSetting$ModeCondition");
        mapClass("ny", "night/setting/impl/NumberSetting");
        mapClass("ny$aa", "night/setting/impl/NumberSetting$NumberType");
        mapClass("ny$ab", "night/setting/impl/NumberSetting$NumberCondition");
        mapClass("ny$ab$aa", "night/setting/impl/NumberSetting$NumberCondition$Comparison");
        mapClass("nz", "night/setting/impl/PositionSetting");
        mapClass("oa", "night/setting/impl/RangeNumberSetting");
        mapClass("ob", "night/setting/impl/StringSetting");
        mapClass("ob$aa", "night/setting/impl/StringSetting$StringCondition");
        mapClass("oc", "night/setting/impl/SubBindSetting");
        mapClass("od", "night/setting/impl/WhitelistSetting");
        mapClass("od$aa", "night/setting/impl/WhitelistSetting$WhitelistType");

        // Utilities
        mapClass("oe", "night/util/RenderQueue");
        mapClass("of", "night/util/MinecraftInstance");
        mapClass("og", "night/util/MathLerpUtil");
        mapClass("oh", "night/util/animation/Animation");
        mapClass("oi", "night/util/animation/EasingUtil");
        mapClass("oi$aa", "night/util/animation/EasingUtil$Easing");
        mapClass("oj", "night/util/color/ChatColorUtil");
        mapClass("ok", "night/util/color/ColorUtil");
        mapClass("ol", "night/util/font/CustomFontRenderer");
        mapClass("ol$aa", "night/util/font/CustomFontRenderer$GlyphPlacement");
        mapClass("om", "night/util/font/GlyphMetrics");
        mapClass("on", "night/util/font/GlyphPage");
        mapClass("oo", "night/util/render/ChamsVertexConsumer");
        mapClass("op", "night/util/render/ClickGuiBackgroundShader");
        mapClass("op$aa", "night/util/render/ClickGuiBackgroundShader$RenderTargetTexture");
        mapClass("oq", "night/util/render/CustomImageTexture");
        mapClass("or", "night/util/render/EspShaderRenderer");
        mapClass("or$aa", "night/util/render/EspShaderRenderer$ShaderUniformConfig");
        mapClass("os", "night/util/render/GlintRenderLayer");
        mapClass("os$aa", "night/util/render/GlintRenderLayer$GlintParams");
        mapClass("os$ab", "night/util/render/GlintRenderLayer$GlintVertex");
        mapClass("os$ac", "night/util/render/GlintRenderLayer$GlintQuad");
        mapClass("ot", "night/util/render/GuiQuadRenderState");
        mapClass("ou", "night/util/render/ItemRenderCache");
        mapClass("ou$aa", "night/util/render/ItemRenderCache$CachedItemState");
        mapClass("ov", "night/util/render/MemeKillEffectRenderer");
        mapClass("ov$aa", "night/util/render/MemeKillEffectRenderer$MemeParticle");
        mapClass("ow", "night/util/render/MenuBackgroundShader");
        mapClass("ow$aa", "night/util/render/MenuBackgroundShader$RenderTargetTexture");
        mapClass("ox", "night/util/render/NeekeriPatternShader");
        mapClass("ox$aa", "night/util/render/NeekeriPatternShader$RenderTargetTexture");
        mapClass("oy", "night/util/resource/NightBuiltinResourcePack");
        mapClass("oz", "night/util/render/Particle3DRenderer");
        mapClass("oz$aa", "night/util/render/Particle3DRenderer$BaseParticle");
        mapClass("oz$ab", "night/util/render/Particle3DRenderer$LinearParticle");
        mapClass("oz$ac", "night/util/render/Particle3DRenderer$OrbitalParticle");
        mapClass("oz$ad", "night/util/render/Particle3DRenderer$Particle");
        mapClass("pa", "night/util/render/Render2DUtil");
        mapClass("pb", "night/util/render/Render3DUtil");
        mapClass("pb$aa", "night/util/render/Render3DUtil$BufferedVertex");
        mapClass("pb$ab", "night/util/render/Render3DUtil$BufferedPrimitive");
        mapClass("pc", "night/util/render/SkyShaderRenderer");
        mapClass("pd", "night/util/render/StarCaptureTarget");
        mapClass("pe", "night/util/render/TextGlowRenderer");
        mapClass("pe$aa", "night/util/render/TextGlowRenderer$RenderPassMode");
        mapClass("pe$ab", "night/util/render/TextGlowRenderer$GlowGuiRenderState");
        mapClass("pf", "night/util/input/KeyCodeParser");
        mapClass("pg", "night/util/input/KeyNameUtil");
        mapClass("ph", "night/util/world/DamageUtil");
        mapClass("pi", "night/util/inventory/EnderChestMemory");
        mapClass("pj", "night/util/inventory/EnchantmentUtil");
        mapClass("pk", "night/util/world/EntityUtil");
        mapClass("pk$aa", "night/util/world/EntityUtil$SpeedUnit");
        mapClass("pl", "night/util/world/ExtrapolationUtil");
        mapClass("pm", "night/util/world/HoleUtil");
        mapClass("pm$aa", "night/util/world/HoleUtil$HoleInfo");
        mapClass("pm$ab", "night/util/world/HoleUtil$HoleSafety");
        mapClass("pm$ac", "night/util/world/HoleUtil$HoleType");
        mapClass("pn", "night/util/world/RegistryLookupUtil");
        mapClass("po", "night/util/inventory/InventoryUtil");
        mapClass("po$aa", "night/util/inventory/InventoryUtil$SwapRecord");
        mapClass("pp", "night/util/world/PacketAndRenderUtil");
        mapClass("pp$aa", "night/util/world/PacketAndRenderUtil$SwingSnapshot");
        mapClass("pq", "night/util/movement/MovementUtil");
        mapClass("pr", "night/util/network/PacketPredictionUtil");
        mapClass("pr$aa", "night/util/network/PacketPredictionUtil$SequencedPacketCreator");
        mapClass("ps", "night/util/world/BlockPlaceUtil");
        mapClass("pt", "night/util/module/ModuleRegistryHelper");
        mapClass("pu", "night/util/world/PositionUtil");
        mapClass("pv", "night/util/world/BlockInteractionUtil");
        mapClass("pv$aa", "night/util/world/BlockInteractionUtil$DirectionDistance");
        mapClass("pv$ab", "night/util/world/BlockInteractionUtil$PlacedBlockTimestamp");
        mapClass("pw", "night/util/inventory/ShulkerSearchUtil");
        mapClass("px", "night/util/render/PlaceRenderEntry");
        mapClass("py", "night/util/world/WorldLoadState");
        mapClass("pz", "night/util/duck/ChamsRenderStateDuck");
        mapClass("qa", "night/util/duck/GuiMessageDuck");
        mapClass("qb", "night/util/duck/GuiMessageLineDuck");
        mapClass("qc", "night/util/duck/EntityRenderStateSelfDuck");
        mapClass("qd", "night/util/math/RotationJitterUtil");
        mapClass("qe", "night/util/rotation/RotationRequest");
        mapClass("qf", "night/util/rotation/Rotation");
        mapClass("qg", "night/util/rotation/RotationMathUtil");
        mapClass("qh", "night/util/math/RollingCounter");
        mapClass("qi", "night/util/io/FileUtil");
        mapClass("qj", "night/util/io/ClipboardImageTransferable");
        mapClass("qk", "night/util/math/MathUtil");
        mapClass("ql", "night/util/concurrent/ThreadManager");
        mapClass("ql$aa", "night/util/concurrent/ThreadManager$WorkerThread");
        mapClass("qm", "night/util/math/TimerUtil");
        mapClass("qn", "night/util/math/NanoTimer");
        mapClass("qo", "night/util/color/SpecialChatFormatting");
        mapClass("qp", "night/util/color/TextFormattingUtil");
    }

    private static void resolveInnerClassMappings() {
        for (String name : classes.keySet()) {
            if (!classMap.containsKey(name) && name.contains("$")) {
                int idx = name.lastIndexOf('$');
                String outer = name.substring(0, idx);
                String inner = name.substring(idx + 1);
                if (classMap.containsKey(outer)) {
                    classMap.put(name, classMap.get(outer) + "$" + inner);
                }
            }
        }
    }

    private static void initSemanticFieldAndMethodMappings() {
        // 1. night/Night
        mapField("night/Night", "a", "J", "START_TIME");
        mapField("night/Night", "b", "Lbh;", "EVENT_BUS");
        mapField("night/Night", "c", "Lorg/slf4j/Logger;", "LOGGER");
        mapField("night/Night", "C", "Ljava/util/concurrent/ExecutorService;", "UPDATE_EXECUTOR");
        mapField("night/Night", "d", "Ljava/lang/String;", "updateStatus");
        mapField("night/Night", "e", "Lem;", "chatManager");
        mapField("night/Night", "f", "Leo;", "fontManager");
        mapField("night/Night", "g", "Lep;", "friendManager");
        mapField("night/Night", "h", "Lfb;", "worldStateManager");
        mapField("night/Night", "i", "Leu;", "positionManager");
        mapField("night/Night", "j", "Lew;", "rotationManager");
        mapField("night/Night", "k", "Lex;", "serverManager");
        mapField("night/Night", "l", "Lev;", "renderPlacementManager");
        mapField("night/Night", "m", "Ley;", "targetManager");
        mapField("night/Night", "n", "Les;", "macroManager");
        mapField("night/Night", "o", "Lez;", "tickTaskManager");
        mapField("night/Night", "p", "Lfa;", "waypointManager");
        mapField("night/Night", "q", "Lel;", "speedManager");
        mapField("night/Night", "r", "Let;", "nightUserManager");
        mapField("night/Night", "s", "Ler;", "customImageManager");
        mapField("night/Night", "t", "Lfd;", "moduleManager");
        mapField("night/Night", "u", "Lac;", "commandManager");
        mapField("night/Night", "v", "Len;", "configManager");
        mapField("night/Night", "w", "Lek;", "accountManager");
        mapField("night/Night", "x", "Leh;", "licenseService");
        mapField("night/Night", "y", "Lda;", "clickGui");
        mapField("night/Night", "z", "Llw;", "pingBypassConfig");
        mapField("night/Night", "A", "Lnk;", "pingBypassServer");
        mapField("night/Night", "B", "Lmi;", "proxyModuleManager");
        mapMethod("night/Night", "a", "()V", "initManagers");
        mapMethod("night/Night", "b", "()V", "postInit");
        mapMethod("night/Night", "c", "()V", "checkForUpdates");

        // 2. MinecraftInstance (of)
        mapField("of", "bg", "Lnet/minecraft/client/Minecraft;", "mc");

        // 3. Command (ab), CommandManager (ac), CommandInfo (ad)
        mapField("ab", "a", "Ljava/lang/String;", "name");
        mapField("ab", "b", "Ljava/lang/String;", "displayName");
        mapField("ab", "c", "Ljava/lang/String;", "description");
        mapField("ab", "d", "Ljava/lang/String;", "syntax");
        mapField("ab", "e", "Ljava/util/List;", "aliases");
        mapMethod("ab", "a", "([Ljava/lang/String;)V", "execute");
        mapMethod("ab", "b", "([Ljava/lang/String;)Ljava/util/List;", "getSuggestions");
        mapMethod("ab", "a", "()Ljava/util/List;", "getModuleNames");
        mapMethod("ab", "b", "()V", "sendSyntax");
        mapMethod("ab", "c", "()Ljava/lang/String;", "getName");
        mapMethod("ab", "d", "()Ljava/lang/String;", "getDisplayName");
        mapMethod("ab", "e", "()Ljava/lang/String;", "getDescription");
        mapMethod("ab", "f", "()Ljava/lang/String;", "getSyntax");
        mapMethod("ab", "g", "()Ljava/util/List;", "getAliases");
        mapMethod("ab", "a", "(Ljava/lang/String;)V", "setName");
        mapMethod("ab", "b", "(Ljava/lang/String;)V", "setDisplayName");

        mapField("ac", "a", "Ljava/util/ArrayList;", "commands");
        mapField("ac", "b", "Ljava/lang/String;", "prefix");
        mapField("ac", "c", "Lcom/mojang/brigadier/CommandDispatcher;", "dispatcher");
        mapMethod("ac", "a", "(Lab;)V", "registerCommand");
        mapMethod("ac", "a", "(Lbq;)V", "onChatSend");
        mapMethod("ac", "a", "(Ljava/lang/String;)V", "executeCommand");
        mapMethod("ac", "b", "(Ljava/lang/String;)Lab;", "getCommand");
        mapMethod("ac", "a", "()Ljava/util/ArrayList;", "getCommands");
        mapMethod("ac", "b", "()Ljava/lang/String;", "getPrefix");
        mapMethod("ac", "c", "(Ljava/lang/String;)V", "setPrefix");
        mapMethod("ac", "c", "()Lcom/mojang/brigadier/CommandDispatcher;", "getDispatcher");

        mapAnnotationMethod("ad", "a", "name");
        mapAnnotationMethod("ad", "b", "displayName");
        mapAnnotationMethod("ad", "c", "description");
        mapAnnotationMethod("ad", "d", "syntax");
        mapAnnotationMethod("ad", "e", "aliases");

        // 4. EventBus (bh), EventListener (bi), EventTarget (bj)
        mapField("bh", "a", "Ljava/util/Map;", "instanceListenerCache");
        mapField("bh", "b", "Ljava/util/Map;", "staticListenerCache");
        mapField("bh", "c", "Ljava/util/Map;", "subscribers");
        mapMethod("bh", "a", "(Ljava/lang/Object;)V", "register");
        mapMethod("bh", "b", "(Ljava/lang/Object;)V", "unregister");
        mapMethod("bh", "a", "(Lbg;)V", "post");
        mapMethod("bh", "a", "(Ljava/lang/Class;Ljava/lang/Object;)Ljava/util/List;", "findListeners");
        mapMethod("bh", "a", "(Ljava/util/List;Ljava/lang/Class;Ljava/lang/Object;)V", "collectListeners");
        mapMethod("bh", "a", "(Ljava/lang/reflect/Method;)Z", "isValidListenerMethod");
        mapMethod("bh", "a", "(Ljava/util/List;Lbi;)V", "insertByPriority");

        mapField("bi", "a", "Ljava/lang/Class;", "eventType");
        mapField("bi", "b", "Ljava/util/function/Consumer;", "consumer");
        mapField("bi", "c", "I", "priority");
        mapMethod("bi", "a", "(Ljava/lang/Object;)V", "invoke");
        mapMethod("bi", "a", "()Ljava/lang/Class;", "getEventType");
        mapMethod("bi", "b", "()I", "getPriority");

        mapAnnotationMethod("bj", "a", "priority");

        // 5. Module (fc), ModuleManager (fd), ModuleInfo (fe), Access Interfaces (ff..ft)
        mapField("fc", "e", "Ljava/lang/String;", "name");
        mapField("fc", "f", "Ljava/lang/String;", "description");
        mapField("fc", "g", "Lfc$aa;", "category");
        mapField("fc", "h", "Z", "persistent");
        mapField("fc", "i", "Z", "proxySupported");
        mapField("fc", "j", "Z", "enabled");
        mapField("fc", "k", "Ljava/util/List;", "settings");
        mapField("fc", "a", "Lnt;", "chatNotify");
        mapField("fc", "b", "Lnt;", "drawn");
        mapField("fc", "c", "Lns;", "bind");
        mapField("fc", "d", "Lnx;", "proxyMode");
        mapField("fc", "l", "Loh;", "animation");

        mapMethod("fc", "a_", "()Z", "nullCheck");
        mapMethod("fc", "b_", "()Z", "isProxyDelegated");
        mapMethod("fc", "c_", "()Z", "shouldDelegateToProxy");
        mapMethod("fc", "d_", "()Z", "isRunningOnProxyServer");
        mapMethod("fc", "e", "()V", "onEnable");
        mapMethod("fc", "h_", "()V", "onDisable");
        mapMethod("fc", "g_", "()Ljava/lang/String;", "getDisplayInfo");
        mapMethod("fc", "e_", "()Ljava/lang/String;", "getProxySuffix");
        mapMethod("fc", "a", "(Z)V", "setEnabled");
        mapMethod("fc", "a", "(ZZ)V", "setEnabled");
        mapMethod("fc", "f_", "()I", "getKeybind");
        mapMethod("fc", "a", "(I)V", "setKeybind");
        mapMethod("fc", "j", "()V", "resetSettings");
        mapMethod("fc", "a", "(Ljava/lang/String;)Lnr;", "getSetting");
        mapMethod("fc", "b", "(Z)V", "sendToggleToProxy");
        mapMethod("fc", "k", "()Ljava/lang/String;", "getName");
        mapMethod("fc", "l", "()Ljava/lang/String;", "getDescription");
        mapMethod("fc", "m", "()Lfc$aa;", "getCategory");
        mapMethod("fc", "n", "()Z", "isPersistent");
        mapMethod("fc", "o", "()Z", "isProxySupported");
        mapMethod("fc", "p", "()Z", "isEnabled");
        mapMethod("fc", "q", "()Ljava/util/List;", "getSettings");
        mapMethod("fc", "r", "()Lnt;", "getChatNotify");
        mapMethod("fc", "s", "()Lnt;", "getDrawn");
        mapMethod("fc", "t", "()Lnx;", "getProxyMode");
        mapMethod("fc", "u", "()Loh;", "getAnimation");

        mapField("fd", "a", "Ljava/util/List;", "modules");
        mapField("fd", "b", "Ljava/util/Map;", "modulesByClass");
        mapMethod("fd", "b", "(Ljava/lang/Class;)V", "instantiateModule");
        mapMethod("fd", "a", "(Lfc;)V", "registerModule");
        mapMethod("fd", "a", "(Lca;)V", "onKeyInput");
        mapMethod("fd", "a", "(I)V", "handleSubBinds");
        mapMethod("fd", "a", "(Lcb;)V", "onMouseButton");
        mapMethod("fd", "a", "(Lcu;)V", "onTick");
        mapMethod("fd", "a", "(Lfc$aa;)Ljava/util/List;", "getModulesByCategory");
        mapMethod("fd", "a", "(Ljava/lang/String;)Lfc;", "getModuleByName");
        mapMethod("fd", "a", "(Ljava/lang/Class;)Lfc;", "getModule");
        mapMethod("fd", "a", "()Ljava/util/List;", "getModules");

        mapAnnotationMethod("fe", "a", "name");
        mapAnnotationMethod("fe", "b", "description");
        mapAnnotationMethod("fe", "c", "category");
        mapAnnotationMethod("fe", "d", "persistent");
        mapAnnotationMethod("fe", "e", "enabled");
        mapAnnotationMethod("fe", "f", "drawn");
        mapAnnotationMethod("fe", "g", "bind");
        mapAnnotationMethod("fe", "h", "proxySupported");

        // Access Interfaces (ff..ft)
        mapMethod("ff", "p", "()Z", "isEnabled");
        mapMethod("ff", "a", "()Ljava/awt/Color;", "getAmbienceColor");
        mapMethod("ff", "b", "()Z", "isLightmapEnabled");
        mapMethod("ff", "c", "()Z", "isSkyEnabled");
        mapMethod("ff", "d", "()Z", "isBlocksEnabled");

        mapMethod("fg", "p", "()Z", "isEnabled");
        mapMethod("fg", "a", "()Z", "isSuppressingGui");
        mapMethod("fg", "a", "(Lnet/minecraft/client/gui/screens/Screen;)Z", "shouldSuppressScreen");

        mapMethod("fh", "p", "()Z", "isEnabled");
        mapMethod("fh", "a", "()Z", "isBackward");

        mapMethod("fi", "p", "()Z", "isEnabled");
        mapMethod("fi", "a", "()F", "getFov");

        mapMethod("fj", "p", "()Z", "isEnabled");
        mapMethod("fj", "a", "()F", "getClimbSpeed");

        mapMethod("fk", "p", "()Z", "isEnabled");
        mapMethod("fk", "a", "()Z", "isRotateEnabled");
        mapMethod("fk", "a", "(DD)V", "changeLookDirection");
        mapMethod("fk", "b", "()F", "getCameraYaw");
        mapMethod("fk", "c", "()F", "getCameraPitch");
        mapMethod("fk", "d", "()D", "getCameraX");
        mapMethod("fk", "g", "()D", "getCameraY");
        mapMethod("fk", "h", "()D", "getCameraZ");

        mapMethod("fl", "p", "()Z", "isEnabled");
        mapMethod("fl", "a", "()Ljava/lang/String;", "getMode");

        mapMethod("fm", "p", "()Z", "isEnabled");
        mapMethod("fm", "a", "()Z", "isStepEnabled");

        mapMethod("fn", "p", "()Z", "isEnabled");
        mapMethod("fn", "a", "()Z", "isSmoothPerspective");
        mapMethod("fn", "b", "()Z", "shouldSmoothCamera");
        mapMethod("fn", "c", "()Z", "shouldRenderThirdPerson");
        mapMethod("fn", "d", "()D", "getInterpolatedDistance");
        mapMethod("fn", "f", "()D", "getCameraX");
        mapMethod("fn", "g", "()D", "getCameraY");
        mapMethod("fn", "h", "()D", "getCameraZ");

        mapMethod("fo", "p", "()Z", "isEnabled");
        mapMethod("fo", "a", "()I", "getDelayTicks");

        mapMethod("fp", "a", "()Z", "isGrimAttackVelocityEnabled");

        mapMethod("fq", "p", "()Z", "isEnabled");
        mapMethod("fq", "a", "()D", "getReachDistance");

        mapMethod("fr", "p", "()Z", "isEnabled");
        mapMethod("fr", "a", "()Ljava/lang/String;", "getMode");
        mapMethod("fr", "b", "()F", "getVanillaSpeed");

        mapMethod("fs", "p", "()Z", "isEnabled");
        mapMethod("fs", "a", "()Z", "canSprint");
        mapMethod("fs", "b", "()Z", "isGrimSprintActive");
        mapMethod("fs", "c", "()I", "getStrafeSide");
        mapMethod("fs", "d", "()F", "getSprintYaw");
        mapMethod("fs", "h", "()Z", "isInstantMode");
        mapMethod("fs", "i", "()Z", "isGrimMode");

        mapMethod("ft", "p", "()Z", "isEnabled");
        mapMethod("ft", "a", "()Z", "isExtendEnabled");
        mapMethod("ft", "b", "()F", "getDistance");

        // 6. Settings (nr..od)
        mapField("nr", "a", "Ljava/lang/String;", "name");
        mapField("nr", "b", "Ljava/lang/String;", "displayName");
        mapField("nr", "c", "Ljava/lang/String;", "description");
        mapField("nr", "d", "Lnr$aa;", "visibility");
        mapMethod("nr", "a", "()Ljava/lang/String;", "getName");
        mapMethod("nr", "b", "()Ljava/lang/String;", "getDisplayName");
        mapMethod("nr", "c", "()Ljava/lang/String;", "getDescription");
        mapMethod("nr", "d", "()Lnr$aa;", "getVisibility");

        mapField("nr$aa", "a", "Lnr;", "parentSetting");
        mapField("nr$aa", "b", "Z", "visible");
        mapMethod("nr$aa", "a", "()V", "update");
        mapMethod("nr$aa", "b", "()Lnr;", "getParentSetting");
        mapMethod("nr$aa", "c", "()Z", "isVisible");
        mapMethod("nr$aa", "a", "(Z)V", "setVisible");

        mapField("nt", "a", "Z", "value");
        mapField("nt", "b", "Z", "defaultValue");
        mapField("nt", "c", "Loh;", "animation");
        mapMethod("nt", "e", "()F", "getAnimationProgress");
        mapMethod("nt", "f", "()Z", "getValue");
        mapMethod("nt", "a", "(Z)V", "setValue");
        mapMethod("nt", "g", "()Z", "getDefaultValue");
        mapMethod("nt", "h", "()V", "reset");

        mapField("ny", "a", "Ljava/lang/Number;", "value");
        mapField("ny", "b", "Ljava/lang/Number;", "defaultValue");
        mapField("ny", "c", "Ljava/lang/Number;", "minValue");
        mapField("ny", "d", "Ljava/lang/Number;", "maxValue");
        mapField("ny", "e", "Ljava/lang/Number;", "step");
        mapField("ny", "f", "Z", "allowZero");
        mapMethod("ny", "a", "(Ljava/lang/Number;)V", "setValue");
        mapMethod("ny", "e", "()V", "reset");
        mapMethod("ny", "f", "()Lny$aa;", "getNumberType");
        mapMethod("ny", "g", "()Ljava/lang/Number;", "getValue");
        mapMethod("ny", "h", "()Ljava/lang/Number;", "getDefaultValue");
        mapMethod("ny", "i", "()Ljava/lang/Number;", "getMinValue");
        mapMethod("ny", "j", "()Ljava/lang/Number;", "getMaxValue");
        mapMethod("ny", "k", "()Ljava/lang/Number;", "getStep");
        mapMethod("ny", "l", "()Z", "isAllowZero");

        mapField("oa", "a", "I", "secondValue");
        mapField("oa", "b", "I", "defaultSecondValue");
        mapMethod("oa", "m", "()I", "getFirstValue");
        mapMethod("oa", "n", "()I", "getSecondValue");
        mapMethod("oa", "a", "(I)V", "setSecondValue");
        mapMethod("oa", "a", "(II)V", "setRange");
        mapMethod("oa", "o", "()Ljava/lang/String;", "getRangeString");
        mapMethod("oa", "a", "(Ljava/lang/String;)V", "parseRange");

        mapField("nx", "a", "Ljava/lang/String;", "value");
        mapField("nx", "b", "Ljava/lang/String;", "defaultValue");
        mapField("nx", "c", "Ljava/util/List;", "modes");
        mapMethod("nx", "a", "(Ljava/lang/String;)V", "setValue");
        mapMethod("nx", "e", "()V", "reset");
        mapMethod("nx", "f", "()Ljava/lang/String;", "getValue");
        mapMethod("nx", "g", "()Ljava/lang/String;", "getDefaultValue");
        mapMethod("nx", "h", "()Ljava/util/List;", "getModes");

        mapField("ob", "a", "Ljava/lang/String;", "value");
        mapField("ob", "b", "Ljava/lang/String;", "defaultValue");
        mapMethod("ob", "e", "()V", "reset");
        mapMethod("ob", "a", "(Ljava/lang/String;)V", "setValue");
        mapMethod("ob", "f", "()Ljava/lang/String;", "getValue");
        mapMethod("ob", "g", "()Ljava/lang/String;", "getDefaultValue");

        mapField("nw", "a", "Ljava/lang/String;", "value");
        mapField("nw", "b", "Ljava/lang/String;", "defaultValue");
        mapMethod("nw", "e", "()V", "reset");
        mapMethod("nw", "a", "(Ljava/lang/String;)V", "setValue");
        mapMethod("nw", "f", "()Ljava/lang/String;", "getValue");
        mapMethod("nw", "g", "()Ljava/lang/String;", "getDefaultValue");

        mapField("ns", "a", "[Ljava/lang/String;", "MODES");
        mapField("ns", "b", "I", "key");
        mapField("ns", "c", "I", "defaultKey");
        mapField("ns", "d", "Ljava/lang/String;", "mode");
        mapField("ns", "e", "Z", "allowHoldMode");
        mapMethod("ns", "e", "()Lns;", "disableHoldMode");
        mapMethod("ns", "f", "()V", "reset");
        mapMethod("ns", "a", "(I)V", "setKey");
        mapMethod("ns", "g", "()V", "cycleMode");
        mapMethod("ns", "a", "(Ljava/lang/String;)V", "setMode");
        mapMethod("ns", "h", "()I", "getKey");
        mapMethod("ns", "i", "()I", "getDefaultKey");
        mapMethod("ns", "j", "()Ljava/lang/String;", "getMode");
        mapMethod("ns", "k", "()Z", "isAllowHoldMode");
        mapMethod("ns", "a", "(Z)V", "setAllowHoldMode");

        mapField("oc", "b", "Lnt;", "targetSetting");
        mapMethod("oc", "l", "()V", "trigger");
        mapMethod("oc", "m", "()V", "notifyBindChange");
        mapMethod("oc", "n", "()Lnt;", "getTargetSetting");

        mapField("nv", "a", "Lnv$aa;", "value");
        mapField("nv", "b", "Lnv$aa;", "defaultValue");
        mapMethod("nv", "e", "()Ljava/awt/Color;", "getColor");
        mapMethod("nv", "a", "(J)Ljava/awt/Color;", "getColor");
        mapMethod("nv", "a", "(Lnv$aa;)V", "setColorValue");
        mapMethod("nv", "a", "(Ljava/awt/Color;)V", "setColor");
        mapMethod("nv", "f", "()I", "getAlpha");
        mapMethod("nv", "g", "()Z", "isSync");
        mapMethod("nv", "a", "(Z)V", "setSync");
        mapMethod("nv", "h", "()Z", "isRainbow");
        mapMethod("nv", "b", "(Z)V", "setRainbow");
        mapMethod("nv", "i", "()V", "reset");
        mapMethod("nv", "j", "()Lnv$aa;", "getColorValue");
        mapMethod("nv", "k", "()Lnv$aa;", "getDefaultColorValue");

        mapField("nv$aa", "a", "Ljava/awt/Color;", "color");
        mapField("nv$aa", "b", "Z", "sync");
        mapField("nv$aa", "c", "Z", "rainbow");
        mapMethod("nv$aa", "a", "()Ljava/awt/Color;", "getColor");
        mapMethod("nv$aa", "b", "()Z", "isSync");
        mapMethod("nv$aa", "c", "()Z", "isRainbow");
        mapMethod("nv$aa", "a", "(Ljava/awt/Color;)V", "setColor");
        mapMethod("nv$aa", "a", "(Z)V", "setSync");
        mapMethod("nv$aa", "b", "(Z)V", "setRainbow");

        mapField("nu", "a", "Z", "open");
        mapField("nu", "b", "Loh;", "animation");
        mapMethod("nu", "e", "()F", "getAnimationProgress");
        mapMethod("nu", "f", "()Z", "isOpen");
        mapMethod("nu", "g", "()Loh;", "getAnimation");
        mapMethod("nu", "a", "(Z)V", "setOpen");

        mapField("nz", "a", "F", "x");
        mapField("nz", "b", "F", "y");
        mapMethod("nz", "a", "(FF)V", "setPosition");
        mapMethod("nz", "e", "()V", "reset");
        mapMethod("nz", "f", "()F", "getX");
        mapMethod("nz", "g", "()F", "getY");
        mapMethod("nz", "a", "(F)V", "setX");
        mapMethod("nz", "b", "(F)V", "setY");

        mapField("od", "a", "Lod$aa;", "type");
        mapField("od", "b", "Ljava/util/Set;", "entries");
        mapMethod("od", "a", "(Ljava/lang/Object;)V", "add");
        mapMethod("od", "b", "(Ljava/lang/Object;)V", "remove");
        mapMethod("od", "c", "(Ljava/lang/Object;)Z", "contains");
        mapMethod("od", "e", "()Ljava/util/List;", "getEntryIds");
        mapMethod("od", "f", "()Ljava/util/List;", "getPotions");
        mapMethod("od", "g", "()V", "clear");
        mapMethod("od", "h", "()Lod$aa;", "getType");
        mapMethod("od", "i", "()Ljava/util/Set;", "getEntries");

        // 7. GUI Components (dg, di, dj)
        mapField("dg", "a", "Lnr;", "setting");
        mapField("dg", "b", "Ldj;", "parentFrame");
        mapField("dg", "c", "I", "x");
        mapField("dg", "d", "I", "y");
        mapField("dg", "e", "I", "height");
        mapField("dg", "f", "I", "horizontalPadding");
        mapField("dg", "g", "I", "textOffset");
        mapField("dg", "h", "Ljava/lang/String;", "description");
        mapField("dg", "i", "Z", "visible");
        mapField("dg", "j", "Loh;", "hoverAnimation");
        mapMethod("dg", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", "render");
        mapMethod("dg", "a", "(DDI)V", "mouseClicked");
        mapMethod("dg", "b", "(DDI)V", "mouseReleased");
        mapMethod("dg", "a", "(III)V", "keyPressed");
        mapMethod("dg", "a", "(DDDD)V", "mouseScrolled");
        mapMethod("dg", "a", "(DDIDD)V", "mouseDragged");
        mapMethod("dg", "a", "(CI)V", "charTyped");
        mapMethod("dg", "e", "()I", "getWidth");
        mapMethod("dg", "a", "(DD)Z", "isHovered");
        mapMethod("dg", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;DD)V", "beginHoverTransform");
        mapMethod("dg", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", "endHoverTransform");
        mapMethod("dg", "f", "()V", "playClickSound");
        mapMethod("dg", "g", "()Lnr;", "getSetting");
        mapMethod("dg", "h", "()Ldj;", "getParentFrame");
        mapMethod("dg", "i", "()I", "getX");
        mapMethod("dg", "j", "()I", "getY");
        mapMethod("dg", "k", "()I", "getHeight");
        mapMethod("dg", "l", "()I", "getHorizontalPadding");
        mapMethod("dg", "m", "()I", "getTextOffset");
        mapMethod("dg", "n", "()Ljava/lang/String;", "getDescription");
        mapMethod("dg", "o", "()Z", "isVisible");
        mapMethod("dg", "b", "(I)V", "setX");
        mapMethod("dg", "c", "(I)V", "setY");
        mapMethod("dg", "a", "(Z)V", "setVisible");

        mapMethod("di", "a", "()Ljava/lang/String;", "getSearchName");
        mapMethod("di", "b", "()Z", "isExpanded");
        mapMethod("di", "c", "()F", "getExpandProgress");
        mapMethod("di", "d", "()Ljava/util/List;", "getSubComponents");
        mapMethod("di", "a", "(I)V", "setExpandedHeight");
        mapMethod("di", "a", "(Ljava/lang/String;)V", "setSearchQuery");

        mapField("dj", "c", "Lfc$aa;", "category");
        mapField("dj", "d", "Ljava/lang/String;", "customTitle");
        mapField("dj", "e", "I", "x");
        mapField("dj", "f", "I", "y");
        mapField("dj", "g", "I", "width");
        mapField("dj", "h", "I", "headerHeight");
        mapField("dj", "i", "I", "contentHeight");
        mapField("dj", "j", "I", "visibleHeight");
        mapField("dj", "k", "I", "dragOffsetX");
        mapField("dj", "l", "I", "dragOffsetY");
        mapField("dj", "m", "I", "padding");
        mapField("dj", "a", "Z", "expanded");
        mapField("dj", "b", "Z", "dragging");
        mapField("dj", "n", "Ljava/util/ArrayList;", "components");
        mapField("dj", "o", "F", "scrollOffset");
        mapField("dj", "p", "F", "targetScrollOffset");
        mapField("dj", "q", "J", "lastRenderTime");
        mapField("dj", "r", "Loh;", "expandAnimation");
        mapMethod("dj", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", "render");
        mapMethod("dj", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIFLjava/lang/String;)V", "render");
        mapMethod("dj", "a", "(DDI)V", "mouseClicked");
        mapMethod("dj", "b", "(DDI)V", "mouseReleased");
        mapMethod("dj", "a", "(DDDD)V", "mouseScrolled");
        mapMethod("dj", "a", "(DDIDD)V", "mouseDragged");
        mapMethod("dj", "a", "(III)V", "keyPressed");
        mapMethod("dj", "a", "(CI)V", "charTyped");
        mapMethod("dj", "a", "(DD)Z", "isHeaderHovered");
        mapMethod("dj", "a", "()Lfc$aa;", "getCategory");
        mapMethod("dj", "b", "()I", "getX");
        mapMethod("dj", "c", "()I", "getY");
        mapMethod("dj", "d", "()I", "getWidth");
        mapMethod("dj", "e", "()I", "getHeaderHeight");
        mapMethod("dj", "f", "()I", "getContentHeight");
        mapMethod("dj", "g", "()I", "getDragOffsetX");
        mapMethod("dj", "h", "()I", "getDragOffsetY");
        mapMethod("dj", "i", "()I", "getPadding");
        mapMethod("dj", "j", "()Z", "isExpanded");
        mapMethod("dj", "k", "()Z", "isDragging");
        mapMethod("dj", "l", "()Ljava/util/ArrayList;", "getComponents");
        mapMethod("dj", "a", "(I)V", "setX");
        mapMethod("dj", "b", "(I)V", "setY");
        mapMethod("dj", "c", "(I)V", "setContentHeight");
        mapMethod("dj", "d", "(I)V", "setDragOffsetX");
        mapMethod("dj", "e", "(I)V", "setDragOffsetY");
        mapMethod("dj", "a", "(Z)V", "setExpanded");
        mapMethod("dj", "b", "(Z)V", "setDragging");

        // 8. Managers (ek..fb)
        mapField("ek", "a", "Lcom/google/gson/Gson;", "GSON");
        mapField("ek", "b", "Ljava/util/List;", "accounts");
        mapField("ek", "c", "Lnight/accounts/Account;", "currentAccount");
        mapMethod("ek", "a", "()V", "loadAccounts");
        mapMethod("ek", "b", "()V", "saveAccounts");
        mapMethod("ek", "a", "(Lnight/accounts/Account;)Z", "login");
        mapMethod("ek", "a", "(Ljava/lang/String;)Lnight/accounts/Account;", "loginOffline");
        mapMethod("ek", "b", "(Lnight/accounts/Account;)V", "addAccount");
        mapMethod("ek", "c", "(Lnight/accounts/Account;)V", "removeAccount");
        mapMethod("ek", "c", "()Ljava/lang/String;", "getCurrentUsername");
        mapMethod("ek", "d", "()Ljava/util/List;", "getAccounts");

        mapField("el", "a", "Lel;", "INSTANCE");
        mapField("el", "d", "Lqm;", "boostTimer");
        mapField("el", "e", "Lqm;", "activeTimer");
        mapField("el", "b", "D", "explosionSpeed");
        mapField("el", "c", "Z", "boostActive");
        mapMethod("el", "a", "(Lcc;)V", "onPacketReceive");
        mapMethod("el", "a", "(Lcu;)V", "onTick");
        mapMethod("el", "a", "(Z)D", "getExplosionBoost");

        mapField("em", "a", "Z", "suppressSend");
        mapField("em", "b", "Ljava/util/List;", "queuedMessages");
        mapMethod("em", "a", "(Lcu;)V", "onTick");
        mapMethod("em", "a", "(Ljava/lang/String;)V", "sendMessage");
        mapMethod("em", "a", "(Ljava/lang/String;Ljava/lang/String;)V", "sendReplaceableMessage");
        mapMethod("em", "b", "(Ljava/lang/String;Ljava/lang/String;)V", "sendTaggedMessage");
        mapMethod("em", "a", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "sendReplaceableTaggedMessage");
        mapMethod("em", "b", "(Ljava/lang/String;)V", "sendInfo");
        mapMethod("em", "c", "(Ljava/lang/String;)V", "sendWarning");
        mapMethod("em", "d", "(Ljava/lang/String;)V", "sendError");
        mapMethod("em", "e", "(Ljava/lang/String;)V", "queueMessage");
        mapMethod("em", "f", "(Ljava/lang/String;)V", "addRawClientMessage");
        mapMethod("em", "c", "(Ljava/lang/String;Ljava/lang/String;)V", "addIdentifiedClientMessage");
        mapMethod("em", "a", "(Lnet/minecraft/network/chat/Component;)V", "addComponentMessage");
        mapMethod("em", "g", "(Ljava/lang/String;)V", "removeMessagesByIdentifier");

        mapField("en", "a", "Ljava/lang/String;", "currentConfig");
        mapField("en", "b", "Z", "loading");
        mapMethod("en", "a", "()V", "loadAll");
        mapMethod("en", "b", "()V", "saveAll");
        mapMethod("en", "c", "()V", "saveOnShutdown");
        mapMethod("en", "d", "()V", "saveActiveConfig");
        mapMethod("en", "e", "()V", "loadWaypoints");
        mapMethod("en", "f", "()V", "loadGeneral");
        mapMethod("en", "g", "()V", "saveGeneral");
        mapMethod("en", "h", "()V", "saveWaypoints");
        mapMethod("en", "a", "(Ljava/lang/String;)V", "loadConfig");
        mapMethod("en", "i", "()V", "loadActiveConfig");
        mapMethod("en", "b", "(Ljava/lang/String;)V", "saveConfig");
        mapMethod("en", "j", "()Lcom/google/gson/JsonObject;", "serializeModules");
        mapMethod("en", "a", "(Lfc;)Lcom/google/gson/JsonObject;", "serializeModule");
        mapMethod("en", "a", "(Lfc;Z)Lcom/google/gson/JsonObject;", "serializeModule");
        mapMethod("en", "k", "()Ljava/lang/String;", "getCurrentConfig");
        mapMethod("en", "l", "()Z", "isLoading");

        mapField("eo", "a", "Lol;", "customFont");
        mapField("eo", "b", "Ljava/lang/ThreadLocal;", "hudGlowContext");
        mapField("eo", "c", "Ljava/lang/ThreadLocal;", "renderDepth");
        mapMethod("eo", "a", "()V", "enterRender");
        mapMethod("eo", "b", "()V", "exitRender");
        mapMethod("eo", "c", "()Z", "isInRender");
        mapMethod("eo", "a", "(Z)V", "setHudGlowContext");
        mapMethod("eo", "d", "()Z", "isHudGlowContext");
        mapMethod("eo", "e", "()Z", "isGlowEnabled");
        mapMethod("eo", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/lang/String;IILjava/awt/Color;)V", "drawText");
        mapMethod("eo", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/lang/String;IILjava/awt/Color;Z)V", "drawText");
        mapMethod("eo", "b", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/lang/String;IILjava/awt/Color;)V", "drawShadowedText");
        mapMethod("eo", "b", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/lang/String;IILjava/awt/Color;Z)V", "drawShadowedText");
        mapMethod("eo", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/util/FormattedCharSequence;IILjava/awt/Color;)V", "drawFormattedText");
        mapMethod("eo", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/util/FormattedCharSequence;IILjava/awt/Color;Z)V", "drawFormattedText");
        mapMethod("eo", "a", "(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;IILjava/awt/Color;)V", "drawWorldText");
        mapMethod("eo", "a", "(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFLjava/awt/Color;Z)V", "drawWorldText");
        mapMethod("eo", "b", "(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;IILjava/awt/Color;)V", "drawPlainWorldText");
        mapMethod("eo", "b", "(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFLjava/awt/Color;Z)V", "drawPlainWorldText");
        mapMethod("eo", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/lang/String;IILjava/awt/Color;Ljava/awt/Color;)V", "drawOutlinedText");
        mapMethod("eo", "a", "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/lang/String;IIJ)V", "drawRainbowText");
        mapMethod("eo", "a", "(Ljava/lang/String;)I", "getStringWidth");
        mapMethod("eo", "f", "()I", "getFontHeight");
        mapMethod("eo", "g", "()F", "getShadowOffset");
        mapMethod("eo", "h", "()Lol;", "getCustomFont");
        mapMethod("eo", "a", "(Lol;)V", "setCustomFont");

        mapField("ep", "a", "Ljava/util/ArrayList;", "friends");
        mapMethod("ep", "a", "(Ljava/lang/String;)Z", "isFriend");
        mapMethod("ep", "b", "(Ljava/lang/String;)V", "addFriend");
        mapMethod("ep", "c", "(Ljava/lang/String;)V", "removeFriend");
        mapMethod("ep", "a", "()V", "clearFriends");
        mapMethod("ep", "b", "()Z", "isFriendlyFireEnabled");
        mapMethod("ep", "d", "(Ljava/lang/String;)V", "sendFriendMessage");
        mapMethod("ep", "c", "()Ljava/awt/Color;", "getFriendColor");
        mapMethod("ep", "a", "(I)Ljava/awt/Color;", "getFriendColor");
        mapMethod("ep", "d", "()Ljava/util/ArrayList;", "getFriends");

        mapField("er", "a", "Ljava/nio/file/Path;", "IMAGES_DIR");
        mapField("er", "b", "Lnet/minecraft/resources/Identifier;", "EFFECT_CUSTOM_IMAGE_ID");
        mapField("er", "c", "Lnet/minecraft/resources/Identifier;", "CUSTOM_IMAGE_ID");
        mapField("er", "d", "Ljava/util/Map;", "cachedImages");
        mapField("er", "e", "Ljava/lang/String;", "activeImageName");
        mapMethod("er", "a", "()V", "registerTextures");
        mapMethod("er", "b", "()Ljava/lang/String;", "readSavedImageName");
        mapMethod("er", "c", "()V", "ensureDirectoryExists");
        mapMethod("er", "a", "(Ljava/io/File;)Lcom/mojang/blaze3d/platform/NativeImage;", "readNativeImage");
        mapMethod("er", "a", "(Ljava/lang/String;)Ljava/io/File;", "resolveImageFile");
        mapMethod("er", "d", "()Ljava/util/List;", "listImageFiles");
        mapMethod("er", "b", "(Ljava/lang/String;)Ler$aa;", "getOrLoadPreview");
        mapMethod("er", "c", "(Ljava/lang/String;)V", "setActiveImage");
        mapMethod("er", "e", "()Ljava/lang/String;", "getActiveImageName");

        mapField("es", "a", "Ljava/util/HashMap;", "macros");
        mapMethod("es", "a", "(Lca;)V", "onKeyInput");
        mapMethod("es", "a", "(Lcb;)V", "onMouseButton");
        mapMethod("es", "a", "(I)Ljava/lang/String;", "getMacroByKey");
        mapMethod("es", "a", "(Ljava/lang/String;)I", "getKeyByMacro");
        mapMethod("es", "b", "(I)Z", "hasMacroForKey");
        mapMethod("es", "a", "(Ljava/lang/String;I)V", "addMacro");
        mapMethod("es", "b", "(Ljava/lang/String;)V", "removeMacro");
        mapMethod("es", "a", "()V", "clearMacros");
        mapMethod("es", "b", "()Ljava/util/HashMap;", "getMacros");

        mapField("et", "a", "Ljava/lang/String;", "BASE_URL");
        mapField("et", "b", "Lcom/google/gson/Gson;", "GSON");
        mapField("et", "c", "Ljava/net/http/HttpClient;", "httpClient");
        mapField("et", "d", "Ljava/util/Set;", "nightUsers");
        mapField("et", "e", "Ljava/util/concurrent/ScheduledExecutorService;", "pollExecutor");
        mapMethod("et", "a", "(Lnet/minecraft/world/entity/player/Player;)Z", "isNightUser");
        mapMethod("et", "a", "(Ljava/lang/String;)Z", "isNightUser");

        mapField("eu", "a", "D", "serverX");
        mapField("eu", "b", "D", "serverY");
        mapField("eu", "c", "D", "serverZ");
        mapField("eu", "d", "Z", "positionInitialized");
        mapField("eu", "e", "Z", "onGround");
        mapField("eu", "f", "Z", "sprinting");
        mapField("eu", "g", "Z", "sneaking");
        mapField("eu", "h", "I", "heldSlot");
        mapMethod("eu", "a", "(Lcd;)V", "onPacketSend");
        mapMethod("eu", "a", "()Lnet/minecraft/world/phys/Vec3;", "getServerEyePosition");
        mapMethod("eu", "b", "()D", "getX");
        mapMethod("eu", "c", "()D", "getY");
        mapMethod("eu", "d", "()D", "getZ");
        mapMethod("eu", "e", "()Z", "isOnGround");
        mapMethod("eu", "f", "()I", "getHeldSlot");

        mapField("ev", "a", "Ljava/util/List;", "RENDER_QUEUE");
        mapField("ev", "c", "Lqh;", "crystalCounter");
        mapField("ev", "d", "I", "crystalsPerSecond");
        mapField("ev", "b", "Ljava/util/concurrent/CopyOnWriteArrayList;", "placedBlocks");
        mapField("ev", "e", "Lev$aa;", "crystalRenderTarget");
        mapField("ev", "f", "Lnet/minecraft/core/BlockPos;", "lastTargetPos");
        mapField("ev", "g", "Lnet/minecraft/world/phys/Vec3;", "slidePos");
        mapField("ev", "h", "J", "slideStartTime");
        mapMethod("ev", "a", "(Lco;)V", "onRender2D");
        mapMethod("ev", "a", "(Lcp;)V", "onRender3DPlaceBlocks");
        mapMethod("ev", "b", "(Lcp;)V", "onRender3DCrystalTarget");
        mapMethod("ev", "a", "(Lcp$aa;)V", "onRender3DPost");
        mapMethod("ev", "a", "(Lnet/minecraft/core/BlockPos;)V", "setCrystalTarget");
        mapMethod("ev", "a", "()I", "getCrystalsPerSecond");
        mapMethod("ev", "b", "()Ljava/util/concurrent/CopyOnWriteArrayList;", "getPlacedBlocks");

        mapField("ew", "c", "Lqf;", "currentRotation");
        mapField("ew", "d", "Lfc;", "activeModule");
        mapField("ew", "g", "F", "serverYaw");
        mapField("ew", "h", "F", "serverPitch");
        mapField("ew", "a", "Z", "spoofingRotation");
        mapField("ew", "b", "Z", "flushingQueue");
        mapMethod("ew", "a", "(Lfc;)I", "getModulePriority");
        mapMethod("ew", "a", "([FI)V", "rotate");
        mapMethod("ew", "a", "(FFI)V", "rotate");
        mapMethod("ew", "a", "([FLfc;I)V", "rotate");
        mapMethod("ew", "a", "(FFLfc;I)V", "rotate");
        mapMethod("ew", "a", "(FF)Z", "canRotate");
        mapMethod("ew", "a", "(FFLnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", "canRotate");
        mapMethod("ew", "a", "([F)V", "sendRotationPacket");
        mapMethod("ew", "a", "()Z", "isRecentlyRotated");
        mapMethod("ew", "b", "(FF)V", "sendRotationPacket");
        mapMethod("ew", "a", "(FFZ)V", "sendRotationPacket");
        mapMethod("ew", "b", "([F)V", "sendPosRotPacket");
        mapMethod("ew", "b", "()V", "beginBatch");
        mapMethod("ew", "c", "(FF)V", "setBatchRotation");
        mapMethod("ew", "c", "()V", "endBatch");
        mapMethod("ew", "d", "(FF)V", "sendOrBatchPosRot");
        mapMethod("ew", "a", "(Ljava/lang/String;[F)V", "rotateByMode");
        mapMethod("ew", "d", "()Z", "hasRecentRotation");
        mapMethod("ew", "e", "()[F", "getInterpolatedRotation");
        mapMethod("ew", "f", "()Lqf;", "getCurrentRotation");
        mapMethod("ew", "g", "()Lfc;", "getActiveModule");
        mapMethod("ew", "h", "()F", "getServerYaw");
        mapMethod("ew", "i", "()F", "getServerPitch");
        mapMethod("ew", "j", "()Z", "isBatching");

        mapField("ex", "a", "Lqm;", "setbackTimer");
        mapField("ex", "b", "Lqm;", "packetTimer");
        mapField("ex", "c", "[F", "tpsSamples");
        mapField("ex", "d", "I", "sampleIndex");
        mapField("ex", "e", "J", "lastTimePacketMs");
        mapField("ex", "f", "J", "connectTimeMs");
        mapField("ex", "g", "Lit/unimi/dsi/fastutil/Pair;", "lastServerConnection");
        mapMethod("ex", "a", "()F", "getTps");
        mapMethod("ex", "b", "()I", "getPingTicks");
        mapMethod("ex", "c", "()I", "getPing");
        mapMethod("ex", "d", "()Ljava/lang/String;", "getServerBrand");
        mapMethod("ex", "e", "()Ljava/lang/String;", "getServerHost");
        mapMethod("ex", "f", "()Lqm;", "getSetbackTimer");
        mapMethod("ex", "g", "()Lqm;", "getPacketTimer");
        mapMethod("ex", "h", "()Lit/unimi/dsi/fastutil/Pair;", "getLastServerConnection");

        mapField("ey", "a", "Ljava/util/ArrayList;", "targets");
        mapMethod("ey", "a", "(Lnet/minecraft/world/entity/player/Player;)Z", "isTarget");

        mapField("ez", "a", "Ljava/util/ArrayList;", "queuedTasks");

        mapField("fa", "a", "Ljava/util/ArrayList;", "waypoints");
        mapMethod("fa", "a", "(Ljava/lang/String;)Z", "hasWaypoint");
        mapMethod("fa", "a", "(Ljava/lang/String;Lnet/minecraft/world/phys/Vec3;)V", "addWaypoint");
        mapMethod("fa", "a", "(Ljava/lang/String;Lnet/minecraft/world/phys/Vec3;Ljava/lang/String;Ljava/lang/String;)V", "addWaypoint");
        mapMethod("fa", "b", "(Ljava/lang/String;)V", "removeWaypoint");
        mapMethod("fa", "a", "()V", "clearWaypoints");
        mapMethod("fa", "b", "()Ljava/util/ArrayList;", "getWaypoints");

        mapField("fa$aa", "a", "Ljava/lang/String;", "name");
        mapField("fa$aa", "b", "Lnet/minecraft/world/phys/Vec3;", "pos");
        mapField("fa$aa", "c", "Ljava/lang/String;", "dimension");
        mapField("fa$aa", "d", "Ljava/lang/String;", "server");
        mapMethod("fa$aa", "a", "()Ljava/lang/String;", "getName");
        mapMethod("fa$aa", "b", "()Lnet/minecraft/world/phys/Vec3;", "getPos");
        mapMethod("fa$aa", "c", "()Ljava/lang/String;", "getDimension");
        mapMethod("fa$aa", "d", "()Ljava/lang/String;", "getServer");

        mapField("fb", "c", "[Lnet/minecraft/core/Vec3i;", "SPHERE_OFFSETS");
        mapField("fb", "d", "[I", "SPHERE_RADII_INDICES");
        mapField("fb", "e", "Ljava/util/Map;", "totemPops");
        mapField("fb", "f", "Ljava/util/Set;", "deadPlayers");
        mapField("fb", "g", "F", "timerMultiplier");
        mapField("fb", "h", "Lqm;", "switchTimer");
        mapField("fb", "i", "Ljava/util/Set;", "placedPositions");
        mapField("fb", "j", "Ljava/util/Map;", "entityRemoveCallbacks");
        mapField("fb", "k", "Lnet/minecraft/world/phys/Vec3;", "lastVelocity");
        mapMethod("fb", "a", "(Lnet/minecraft/core/BlockPos;)V", "recordPlacedPosition");
        mapMethod("fb", "a", "()Ljava/util/Set;", "getPlacedPositions");
        mapMethod("fb", "a", "(ILjava/lang/Runnable;)V", "onEntityRemoved");
        mapMethod("fb", "a", "(D)I", "getSpherePointCount");
        mapMethod("fb", "a", "(I)Lnet/minecraft/core/Vec3i;", "getSphereOffset");
        mapMethod("fb", "b", "()Ljava/util/Map;", "getTotemPops");
        mapMethod("fb", "c", "()F", "getTimerMultiplier");
        mapMethod("fb", "a", "(F)V", "setTimerMultiplier");
        mapMethod("fb", "d", "()Lqm;", "getSwitchTimer");
        mapMethod("fb", "e", "()Lnet/minecraft/world/phys/Vec3;", "getLastVelocity");

        // 9. PingBypass classes (mh, mi, mn, mm, lw, nk)
        mapField("mh", "a", "Ljava/lang/String;", "name");
        mapField("mh", "b", "Z", "enabled");
        mapMethod("mh", "a", "()Ljava/lang/String;", "getName");
        mapMethod("mh", "b", "()Z", "isEnabled");
        mapMethod("mh", "a", "(Z)V", "setEnabled");
        mapMethod("mh", "c", "()V", "onEnable");
        mapMethod("mh", "d", "()V", "onDisable");
        mapMethod("mh", "e", "()V", "onTick");
        mapMethod("mh", "f", "()Ljava/util/List;", "getSettings");
        mapMethod("mh", "a", "(Ljava/lang/String;)Lnr;", "getSetting");

        mapField("mi", "a", "Ljava/util/Set;", "SUPPORTED_MODULES");
        mapField("mi", "b", "Ljava/util/List;", "modules");
        mapMethod("mi", "a", "(Lmh;)V", "register");
        mapMethod("mi", "a", "(Ljava/lang/String;)Lmh;", "getModule");
        mapMethod("mi", "a", "()V", "onTick");

        mapMethod("mn", "a", "()I", "getPacketId");
        mapMethod("mn", "a", "(Lnet/minecraft/network/FriendlyByteBuf;)V", "write");

        mapField("mm", "a", "Lnet/minecraft/resources/Identifier;", "CHANNEL_ID");
        mapField("mm", "b", "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;", "TYPE");
        mapField("mm", "c", "Lnet/minecraft/network/codec/StreamCodec;", "CODEC");
        mapField("mm", "d", "[B", "data");
        mapMethod("mm", "a", "()Lnet/minecraft/network/FriendlyByteBuf;", "toBuffer");
        mapMethod("mm", "a", "(Lmn;)Lmm;", "fromPacket");
        mapMethod("mm", "b", "()Lmm;", "empty");
        mapMethod("mm", "c", "()[B", "data");

        mapField("lw", "a", "Lorg/slf4j/Logger;", "LOGGER");
        mapField("lw", "b", "Ljava/util/Properties;", "properties");
        mapField("lw", "c", "Ljava/nio/file/Path;", "configPath");
        mapField("lw", "d", "Z", "serverMode");
        mapMethod("lw", "a", "()V", "load");
        mapMethod("lw", "b", "()Z", "isServerMode");
        mapMethod("lw", "c", "()Ljava/lang/String;", "getBindIp");
        mapMethod("lw", "d", "()I", "getPort");
        mapMethod("lw", "e", "()Ljava/lang/String;", "getPassword");
        mapMethod("lw", "f", "()Z", "hasPassword");

        mapField("nk", "a", "Lorg/slf4j/Logger;", "LOGGER");
        mapField("nk", "d", "Ljava/util/List;", "channels");
        mapField("nk", "e", "Ljava/util/List;", "connections");
        mapField("nk", "f", "Llw;", "config");
        mapField("nk", "g", "Lmp;", "protocol");
        mapField("nk", "h", "Z", "stayConnected");
        mapField("nk", "i", "Z", "running");
        mapField("nk", "j", "Lnet/minecraft/network/Connection;", "backendConnection");
        mapField("nk", "k", "Lnm;", "registryCache");
        mapMethod("nk", "a", "(Ljava/net/InetAddress;I)V", "bind");
        mapMethod("nk", "a", "()V", "tick");
        mapMethod("nk", "b", "()V", "shutdown");
        mapMethod("nk", "c", "()Z", "isStayConnected");
        mapMethod("nk", "d", "()Ljava/util/List;", "getConnections");
        mapMethod("nk", "a", "(Z)V", "setStayConnected");
        mapMethod("nk", "e", "()Lnet/minecraft/network/Connection;", "getBackendConnection");
        mapMethod("nk", "a", "(Lnet/minecraft/network/Connection;)V", "setBackendConnection");
        mapMethod("nk", "f", "()Lnm;", "getRegistryCache");

        // 10. Common Utilities (qm, qn, qf, qe, qh, oh, oi, ok, oj, pq, po)
        mapField("qm", "a", "J", "lastTimeMs");
        mapMethod("qm", "a", "(Ljava/lang/Number;)Z", "passedMs");
        mapMethod("qm", "a", "()J", "getElapsedMs");
        mapMethod("qm", "b", "()V", "reset");

        mapField("qn", "a", "J", "timeNs");
        mapMethod("qn", "a", "(J)Z", "passedNs");
        mapMethod("qn", "a", "()V", "reset");
        mapMethod("qn", "b", "()V", "clear");

        mapField("qf", "a", "F", "yaw");
        mapField("qf", "b", "F", "pitch");
        mapMethod("qf", "a", "()F", "getYaw");
        mapMethod("qf", "b", "()F", "getPitch");
        mapMethod("qf", "a", "(F)V", "setYaw");
        mapMethod("qf", "b", "(F)V", "setPitch");

        mapField("qe", "a", "F", "yaw");
        mapField("qe", "b", "F", "pitch");
        mapField("qe", "c", "Lfc;", "module");
        mapField("qe", "d", "I", "priority");
        mapField("qe", "e", "J", "timestamp");
        mapMethod("qe", "a", "()F", "getYaw");
        mapMethod("qe", "b", "()F", "getPitch");
        mapMethod("qe", "c", "()Lfc;", "getModule");
        mapMethod("qe", "d", "()I", "getPriority");
        mapMethod("qe", "e", "()J", "getTimestamp");

        mapField("qh", "a", "Ljava/util/concurrent/ConcurrentLinkedQueue;", "timestamps");
        mapMethod("qh", "a", "()V", "increment");
        mapMethod("qh", "b", "()I", "getCount");
        mapMethod("qh", "c", "()V", "reset");

        mapField("oh", "a", "F", "value");
        mapField("oh", "b", "F", "target");
        mapField("oh", "c", "J", "lastUpdateTime");
        mapField("oh", "d", "Loi$aa;", "easing");
        mapField("oh", "e", "I", "duration");
        mapMethod("oh", "a", "()F", "getValue");
        mapMethod("oh", "b", "()F", "getTarget");
        mapMethod("oh", "a", "(F)F", "animateTo");
        mapMethod("oh", "a", "(Loi$aa;)V", "setEasing");
        mapMethod("oh", "c", "()J", "getLastUpdateTime");
        mapMethod("oh", "b", "(F)V", "setValue");
        mapMethod("oh", "c", "(F)V", "setTarget");

        mapField("eh", "j", "Ljava/lang/String;", "licenseKey");
        mapField("eh", "k", "Ljava/lang/String;", "role");
        mapField("eh", "l", "Ljava/lang/String;", "uid");
        mapMethod("eh", "a", "()Z", "isUnlocked");
        mapMethod("eh", "b", "()Ljava/lang/String;", "getRole");
        mapMethod("eh", "c", "()Z", "isStandardRole");
        mapMethod("eh", "d", "()V", "start");
        mapMethod("eh", "e", "()J", "getHeartbeatAgeMs");
        mapMethod("eh", "f", "()V", "shutdown");
    }

    private static void discoverEnumMappings() {
        for (ClassNode cn : classes.values()) {
            if ((cn.access & Opcodes.ACC_ENUM) == 0) continue;

            // Find <clinit> and inspect NEW + LDC (enum constant name) + INVOKESPECIAL <init> + PUTSTATIC
            MethodNode clinit = null;
            for (MethodNode mn : cn.methods) {
                if ("<clinit>".equals(mn.name)) {
                    clinit = mn;
                    break;
                }
            }
            if (clinit != null) {
                String lastEnumString = null;
                boolean afterNew = false;
                for (AbstractInsnNode insn = clinit.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.NEW && insn instanceof TypeInsnNode tin && tin.desc.equals(cn.name)) {
                        afterNew = true;
                        lastEnumString = null;
                    } else if (afterNew && insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s && lastEnumString == null) {
                        lastEnumString = s;
                    } else if (afterNew && insn.getOpcode() == Opcodes.PUTSTATIC && insn instanceof FieldInsnNode fin
                            && fin.owner.equals(cn.name) && fin.desc.equals("L" + cn.name + ";")) {
                        if (lastEnumString != null && isObfuscatedShortName(fin.name)) {
                            String validConst = sanitizeEnumConstant(lastEnumString);
                            mapField(cn.name, fin.name, fin.desc, validConst);
                        }
                        afterNew = false;
                        lastEnumString = null;
                    }
                }
            }

            // Also check for synthetic values array field `[L<enum>;` and `$values()` method `()[L<enum>;`
            String arrayDesc = "[L" + cn.name + ";";
            for (FieldNode fn : cn.fields) {
                if (fn.desc.equals(arrayDesc) && isObfuscatedShortName(fn.name)) {
                    mapField(cn.name, fn.name, fn.desc, "$VALUES");
                }
            }
            for (MethodNode mn : cn.methods) {
                if (mn.desc.equals("()" + arrayDesc) && !"values".equals(mn.name) && isObfuscatedShortName(mn.name)) {
                    mapMethod(cn.name, mn.name, mn.desc, "$values");
                }
            }
        }
    }

    private static String sanitizeEnumConstant(String raw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        if (sb.isEmpty() || !Character.isJavaIdentifierStart(sb.charAt(0))) {
            sb.insert(0, '_');
        }
        return sb.toString();
    }

    private static void discoverRecordMappings() {
        for (ClassNode cn : classes.values()) {
            if (!"java/lang/Record".equals(cn.superName)) continue;
            cn.access |= Opcodes.ACC_RECORD;
            if (cn.recordComponents == null) {
                cn.recordComponents = new ArrayList<>();
            }
            if (cn.recordComponents.isEmpty()) {
                for (FieldNode fn : cn.fields) {
                    if ((fn.access & Opcodes.ACC_STATIC) == 0) {
                        fn.access = (fn.access & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
                        RecordComponentNode rcn = new RecordComponentNode(fn.name, fn.desc, fn.signature);
                        rcn.visibleAnnotations = fn.visibleAnnotations;
                        rcn.invisibleAnnotations = fn.invisibleAnnotations;
                        rcn.visibleTypeAnnotations = fn.visibleTypeAnnotations;
                        rcn.invisibleTypeAnnotations = fn.invisibleTypeAnnotations;
                        cn.recordComponents.add(rcn);
                    }
                }
            } else {
                for (FieldNode fn : cn.fields) {
                    if ((fn.access & Opcodes.ACC_STATIC) == 0) {
                        fn.access = (fn.access & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
                    }
                }
            }
            if (cn.recordComponents.isEmpty()) continue;

            Set<String> usedNames = new HashSet<>();
            for (RecordComponentNode rcn : cn.recordComponents) {
                if (!isObfuscatedShortName(rcn.name)) {
                    usedNames.add(rcn.name);
                }
            }

            for (RecordComponentNode rcn : cn.recordComponents) {
                String oldName = rcn.name;
                String desc = rcn.descriptor;
                if (!isObfuscatedShortName(oldName)) continue;

                String existingField = fieldMap.get(cn.name + "." + oldName + ":" + desc);
                String newName;
                if (existingField != null) {
                    newName = existingField;
                } else {
                    String baseName = suggestNameFromDescriptor(desc, rcn.signature);
                    newName = deduplicateName(baseName, usedNames);
                }
                usedNames.add(newName);
                mapField(cn.name, oldName, desc, newName);
                mapMethod(cn.name, oldName, "()" + desc, newName);
            }
        }
    }

    private static String suggestNameFromDescriptor(String desc, String signature) {
        return switch (desc) {
            case "Lnet/minecraft/core/BlockPos;" -> "pos";
            case "Lnet/minecraft/core/Direction;" -> "direction";
            case "Lnet/minecraft/world/entity/player/Player;" -> "player";
            case "Lnet/minecraft/world/phys/Vec3;" -> "vec";
            case "Lnet/minecraft/world/phys/AABB;" -> "box";
            case "Lnet/minecraft/resources/Identifier;" -> "texture";
            case "Ljava/awt/Color;" -> "color";
            case "Lnet/minecraft/world/level/block/Block;" -> "block";
            case "Lnet/minecraft/world/entity/Pose;" -> "pose";
            case "Lnet/minecraft/network/Connection;" -> "connection";
            case "Lorg/joml/Matrix4f;" -> "matrix";
            case "Lorg/joml/Matrix3x2fc;" -> "pose";
            case "Lfc;" -> "module";
            case "Lnt;" -> "enabledSetting";
            case "Lnz;" -> "positionSetting";
            case "Lnu;" -> "categorySetting";
            case "Lqm;" -> "timer";
            case "Ljava/lang/String;" -> "text";
            case "Ljava/util/List;" -> "list";
            case "Ljava/util/Map;" -> "map";
            case "Z" -> "flag";
            case "I" -> "count";
            case "J" -> "timestamp";
            case "F" -> "value";
            case "D" -> "distance";
            default -> {
                if (desc.startsWith("L") && desc.endsWith(";")) {
                    String internal = desc.substring(1, desc.length() - 1);
                    String mapped = classMap.getOrDefault(internal, internal);
                    int idx = Math.max(mapped.lastIndexOf('/'), mapped.lastIndexOf('$'));
                    String simple = idx >= 0 ? mapped.substring(idx + 1) : mapped;
                    yield Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
                }
                yield "value";
            }
        };
    }

    private static void discoverSettingFieldMappings() {
        Set<String> settingTypes = Set.of("nr", "ns", "nt", "nu", "nv", "nw", "nx", "ny", "nz", "oa", "ob", "oc", "od");

        for (ClassNode cn : classes.values()) {
            if (!"fc".equals(cn.superName) && !"mh".equals(cn.superName)) continue;

            // Find <init>
            for (MethodNode mn : cn.methods) {
                if (!"<init>".equals(mn.name)) continue;

                Set<String> usedFieldNames = new HashSet<>();
                // Reserve parent fields
                usedFieldNames.addAll(List.of("name", "description", "category", "persistent", "proxySupported",
                        "enabled", "settings", "chatNotify", "drawn", "bind", "proxyMode", "animation", "mc"));

                String currentCategoryPrefix = "";
                String pendingSettingType = null;
                List<String> pendingStrings = new ArrayList<>();

                for (AbstractInsnNode insn = mn.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.NEW && insn instanceof TypeInsnNode tin) {
                        if (settingTypes.contains(tin.desc)) {
                            pendingSettingType = tin.desc;
                            pendingStrings.clear();
                        }
                    } else if (pendingSettingType != null && insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
                        pendingStrings.add(s);
                    } else if (pendingSettingType != null && insn.getOpcode() == Opcodes.PUTFIELD && insn instanceof FieldInsnNode fin
                            && fin.owner.equals(cn.name)) {
                        String fieldDescType = fin.desc.startsWith("L") && fin.desc.endsWith(";")
                                ? fin.desc.substring(1, fin.desc.length() - 1) : "";
                        if (settingTypes.contains(fieldDescType) && !pendingStrings.isEmpty() && isObfuscatedShortName(fin.name)) {
                            String settingName = pendingStrings.getFirst();
                            String camel = toCamelCaseIdentifier(settingName);
                            if ("nu".equals(fieldDescType)) {
                                currentCategoryPrefix = sanitizePascalCase(settingName);
                                if (!camel.endsWith("Category")) {
                                    camel = camel + "Category";
                                }
                            } else if (("enabled".equals(camel) || "mode".equals(camel) || "color".equals(camel) || "speed".equals(camel))
                                    && usedFieldNames.contains(camel) && !currentCategoryPrefix.isEmpty()) {
                                camel = Character.toLowerCase(currentCategoryPrefix.charAt(0))
                                        + currentCategoryPrefix.substring(1)
                                        + Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
                            }
                            String uniqueName = deduplicateName(camel, usedFieldNames);
                            usedFieldNames.add(uniqueName);
                            mapField(cn.name, fin.name, fin.desc, uniqueName);
                        }
                        pendingSettingType = null;
                        pendingStrings.clear();
                    }
                }
            }
        }
    }

    private static void discoverEventMappings() {
        // Explicit mappings for events bk..cz
        mapField("bk", "a", "Lnet/minecraft/core/BlockPos;", "pos");
        mapField("bk", "b", "Lnet/minecraft/core/Direction;", "direction");
        mapMethod("bk", "a", "()Lnet/minecraft/core/BlockPos;", "getPos");

        mapField("bl", "a", "Lnet/minecraft/world/entity/player/Player;", "player");
        mapField("bl", "b", "Lnet/minecraft/world/entity/Entity;", "entity");
        mapMethod("bl", "a", "()Lnet/minecraft/world/entity/player/Player;", "getPlayer");
        mapMethod("bl", "b", "()Lnet/minecraft/world/entity/Entity;", "getEntity");

        mapField("bm", "a", "Lnet/minecraft/core/BlockPos;", "pos");
        mapField("bo", "a", "F", "bobMultiplier");
        mapField("bp", "a", "F", "reach");

        mapField("bq", "a", "Ljava/lang/String;", "message");
        mapMethod("bq", "a", "()Ljava/lang/String;", "getMessage");

        mapField("bt", "a", "Lqf;", "rotation");
        mapField("bt", "b", "Lfc;", "module");
        mapMethod("bt", "a", "(F)V", "setYaw");
        mapMethod("bt", "b", "(F)V", "setPitch");
        mapMethod("bt", "a", "()Lfc;", "getModule");
        mapMethod("bt", "a", "(Lfc;)V", "setModule");

        mapField("bu", "a", "Ljava/lang/String;", "command");
        mapMethod("bu", "a", "()Ljava/lang/String;", "getCommand");

        mapField("bv", "a", "Lnet/minecraft/world/item/ItemStack;", "stack");
        mapMethod("bv", "a", "()Lnet/minecraft/world/item/ItemStack;", "getStack");

        mapField("bw", "a", "Lnet/minecraft/core/BlockPos;", "pos");
        mapMethod("bw", "a", "()Lnet/minecraft/core/BlockPos;", "getPos");

        mapField("bx", "a", "Lnet/minecraft/world/entity/Entity;", "entity");
        mapMethod("bx", "a", "()Lnet/minecraft/world/entity/Entity;", "getEntity");

        mapField("by", "a", "Z", "up");
        mapField("by", "b", "Z", "down");
        mapField("by", "c", "Z", "left");
        mapField("by", "d", "Z", "right");
        mapField("by", "e", "Z", "jumping");
        mapField("by", "f", "Z", "sneaking");
        mapField("by", "g", "Z", "sprinting");

        mapField("ca", "a", "I", "key");
        mapField("ca", "b", "I", "modifiers");
        mapMethod("ca", "a", "()I", "getKey");
        mapMethod("ca", "b", "()I", "getModifiers");

        mapField("cb", "a", "I", "button");
        mapMethod("cb", "a", "()I", "getButton");

        mapField("cc", "a", "Lnet/minecraft/network/protocol/Packet;", "packet");
        mapField("cc", "b", "Lnet/minecraft/network/Connection;", "connection");
        mapMethod("cc", "a", "()Lnet/minecraft/network/protocol/Packet;", "getPacket");
        mapMethod("cc", "b", "()Lnet/minecraft/network/Connection;", "getConnection");

        mapField("cd", "a", "Lnet/minecraft/network/protocol/Packet;", "packet");
        mapMethod("cd", "a", "()Lnet/minecraft/network/protocol/Packet;", "getPacket");
        mapField("cd$aa", "a", "Lnet/minecraft/network/protocol/Packet;", "packet");
        mapMethod("cd$aa", "a", "()Lnet/minecraft/network/protocol/Packet;", "getPacket");

        mapField("ce", "a", "Ljava/util/UUID;", "uuid");
        mapField("ce", "b", "Ljava/lang/String;", "name");
        mapMethod("ce", "a", "()Ljava/util/UUID;", "getUuid");
        mapMethod("ce", "b", "()Ljava/lang/String;", "getName");

        mapField("cf", "a", "Lnet/minecraft/world/entity/player/Player;", "player");
        mapMethod("cf", "a", "()Lnet/minecraft/world/entity/player/Player;", "getPlayer");

        mapField("cg", "a", "Ljava/util/UUID;", "uuid");
        mapField("cg", "b", "Ljava/lang/String;", "name");
        mapMethod("cg", "a", "()Ljava/util/UUID;", "getUuid");
        mapMethod("cg", "b", "()Ljava/lang/String;", "getName");

        mapField("ci", "a", "I", "breakerId");
        mapField("ci", "b", "Lnet/minecraft/core/BlockPos;", "pos");
        mapMethod("ci", "a", "()I", "getBreakerId");
        mapMethod("ci", "b", "()Lnet/minecraft/core/BlockPos;", "getPos");

        mapField("cj", "a", "Lnet/minecraft/world/entity/MoverType;", "type");
        mapField("cj", "b", "Lnet/minecraft/world/phys/Vec3;", "movement");
        mapMethod("cj", "a", "(D)V", "setY");
        mapMethod("cj", "b", "(D)V", "setX");
        mapMethod("cj", "a", "()Lnet/minecraft/world/entity/MoverType;", "getType");
        mapMethod("cj", "b", "()Lnet/minecraft/world/phys/Vec3;", "getMovement");
        mapMethod("cj", "a", "(Lnet/minecraft/world/phys/Vec3;)V", "setMovement");

        mapField("ck", "a", "Lnet/minecraft/world/entity/player/Player;", "player");
        mapField("ck", "b", "I", "pops");
        mapMethod("ck", "a", "()Lnet/minecraft/world/entity/player/Player;", "getPlayer");
        mapMethod("ck", "b", "()I", "getPops");

        mapField("cl", "a", "Lnet/minecraft/world/phys/Vec3;", "pushVector");
        mapField("cn", "a", "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;", "firework");

        mapField("co", "a", "Lnet/minecraft/client/gui/GuiGraphicsExtractor;", "context");
        mapField("co", "b", "F", "tickDelta");
        mapMethod("co", "a", "()Lorg/joml/Matrix3x2fStack;", "getMatrices");
        mapMethod("co", "b", "()Lnet/minecraft/client/gui/GuiGraphicsExtractor;", "getContext");
        mapMethod("co", "c", "()F", "getTickDelta");

        mapField("cp", "a", "Lcom/mojang/blaze3d/vertex/PoseStack;", "matrices");
        mapField("cp", "b", "F", "tickDelta");
        mapMethod("cp", "a", "()Lcom/mojang/blaze3d/vertex/PoseStack;", "getMatrices");
        mapMethod("cp", "b", "()F", "getTickDelta");

        mapField("cp$aa", "a", "Lcom/mojang/blaze3d/vertex/PoseStack;", "matrices");
        mapField("cp$aa", "b", "F", "tickDelta");
        mapMethod("cp$aa", "a", "()Lcom/mojang/blaze3d/vertex/PoseStack;", "getMatrices");
        mapMethod("cp$aa", "b", "()F", "getTickDelta");

        mapField("cr", "a", "Lnet/minecraft/client/multiplayer/resolver/ServerAddress;", "address");
        mapField("cr", "b", "Lnet/minecraft/client/multiplayer/ServerData;", "info");
        mapMethod("cr", "a", "()Lnet/minecraft/client/multiplayer/resolver/ServerAddress;", "getAddress");
        mapMethod("cr", "b", "()Lnet/minecraft/client/multiplayer/ServerData;", "getInfo");

        mapField("cs", "a", "Lnr;", "setting");
        mapMethod("cs", "a", "()Lnr;", "getSetting");

        mapField("ct", "a", "Lnet/minecraft/world/entity/player/Player;", "player");
        mapMethod("ct", "a", "()Lnet/minecraft/world/entity/player/Player;", "getPlayer");

        mapField("cv", "a", "Lfc;", "module");
        mapField("cv", "b", "Z", "enabled");

        mapField("cw", "a", "I", "width");
        mapField("cw", "b", "I", "height");
        mapField("cw", "c", "I", "framebufferWidth");
        mapField("cw", "d", "I", "framebufferHeight");

        mapField("cx", "a", "I", "button");
        mapField("cx", "b", "I", "action");
        mapField("cx", "c", "I", "modifiers");

        mapField("cz", "a", "Lnet/minecraft/world/phys/Vec3;", "movementInput");
        mapField("cz", "b", "F", "speed");
        mapField("cz", "c", "Lnet/minecraft/world/phys/Vec3;", "velocity");
        mapMethod("cz", "a", "()Lnet/minecraft/world/phys/Vec3;", "getMovementInput");
        mapMethod("cz", "b", "()F", "getSpeed");
        mapMethod("cz", "c", "()Lnet/minecraft/world/phys/Vec3;", "getVelocity");
        mapMethod("cz", "a", "(Lnet/minecraft/world/phys/Vec3;)V", "setVelocity");
    }

    private static void discoverPingBypassPacketMappings() {
        mapField("mq", "a", "Ljava/util/List;", "friends");
        mapMethod("mq", "b", "()Ljava/util/List;", "getFriends");

        mapField("mr", "a", "I", "actionType");
        mapField("mr", "b", "I", "slot");
        mapField("mr", "c", "I", "button");

        mapField("ms", "a", "Ljava/lang/String;", "host");
        mapField("ms", "b", "I", "port");

        mapField("mt", "a", "Ljava/lang/String;", "moduleName");
        mapField("mt", "b", "Z", "enabled");
        mapMethod("mt", "b", "()Ljava/lang/String;", "getModuleName");
        mapMethod("mt", "c", "()Z", "isEnabled");

        mapField("mu", "a", "Z", "inventoryOpen");
        mapMethod("mu", "b", "()Z", "isInventoryOpen");

        mapField("mv", "a", "Ljava/lang/String;", "password");

        mapField("mw", "a", "Ljava/lang/String;", "moduleName");
        mapField("mw", "b", "Ljava/lang/String;", "settingName");
        mapField("mw", "c", "Ljava/lang/String;", "value");
        mapMethod("mw", "b", "()Ljava/lang/String;", "getModuleName");
        mapMethod("mw", "c", "()Ljava/lang/String;", "getSettingName");
        mapMethod("mw", "d", "()Ljava/lang/String;", "getValue");

        mapField("mx", "a", "Z", "stayConnected");
        mapMethod("mx", "b", "()Z", "isStayConnected");

        mapField("my", "a", "Lnet/minecraft/core/BlockPos;", "pos");
        mapMethod("my", "b", "()Lnet/minecraft/core/BlockPos;", "getPos");

        mapField("mz", "a", "Ljava/lang/String;", "message");
        mapMethod("mz", "b", "()Ljava/lang/String;", "getMessage");

        mapField("na", "a", "Z", "hasPrimary");
        mapField("na", "b", "Lnet/minecraft/core/BlockPos;", "primaryPos");
        mapField("na", "c", "F", "primaryProgress");
        mapField("na", "d", "Z", "hasSecondary");
        mapField("na", "e", "Lnet/minecraft/core/BlockPos;", "secondaryPos");
        mapField("na", "f", "F", "secondaryProgress");
        mapMethod("na", "b", "()Lnet/minecraft/core/BlockPos;", "getPrimaryPos");
        mapMethod("na", "c", "()F", "getPrimaryProgress");
        mapMethod("na", "d", "()Lnet/minecraft/core/BlockPos;", "getSecondaryPos");
        mapMethod("na", "e", "()F", "getSecondaryProgress");

        mapField("nb", "a", "Ljava/lang/String;", "moduleName");
        mapField("nb", "b", "Z", "enabled");
        mapMethod("nb", "b", "()Ljava/lang/String;", "getModuleName");
        mapMethod("nb", "c", "()Z", "isEnabled");

        mapField("nd", "a", "Lnet/minecraft/core/BlockPos;", "pos");
        mapMethod("nd", "b", "()Lnet/minecraft/core/BlockPos;", "getPos");

        mapField("ne", "a", "Ljava/lang/String;", "reason");
        mapMethod("ne", "b", "()Ljava/lang/String;", "getReason");

        mapField("nf", "a", "Ljava/lang/String;", "moduleName");
        mapField("nf", "b", "Ljava/lang/String;", "settingName");
        mapField("nf", "c", "Ljava/lang/String;", "value");
        mapMethod("nf", "b", "()Ljava/lang/String;", "getModuleName");
        mapMethod("nf", "c", "()Ljava/lang/String;", "getSettingName");
        mapMethod("nf", "d", "()Ljava/lang/String;", "getValue");

        mapField("ng", "a", "I", "slot");
        mapMethod("ng", "b", "()I", "getSlot");

        mapField("nh", "a", "I", "containerId");
        mapField("nh", "b", "I", "slotNum");
        mapField("nh", "c", "I", "buttonNum");
        mapField("nh", "d", "Ljava/lang/String;", "clickType");
        mapMethod("nh", "b", "()I", "getContainerId");
        mapMethod("nh", "c", "()I", "getSlotNum");
        mapMethod("nh", "d", "()I", "getButtonNum");
        mapMethod("nh", "e", "()Lnet/minecraft/world/inventory/ContainerInput;", "getClickType");
    }

    private static void disambiguateOverloadsAndPrivateMethods() {
        // 1. Rename private methods in Module (fc) so no subclass method accidentally collides with or "overrides" them
        ClassNode fcNode = classes.get("fc");
        if (fcNode != null) {
            for (MethodNode mn : fcNode.methods) {
                if ((mn.access & Opcodes.ACC_PRIVATE) != 0 && !mn.name.startsWith("<")) {
                    String key = "fc." + mn.name + mn.desc;
                    if (!methodMap.containsKey(key)) {
                        mapMethod("fc", mn.name, mn.desc, "modulePrivate_" + mn.name);
                    }
                }
            }
        }

        // 2. Fix anonymous inner classes implementing java.util.function.Function (e.g., bh$1, bh$2)
        // or java.util.Comparator (e.g., fb$1) where the bridge method `apply(Object)Object` / `compare(Object,Object)I`
        // calls an obfuscated typed method `a(...)`
        for (ClassNode cn : classes.values()) {
            if (cn.interfaces != null) {
                if (cn.interfaces.contains("java/util/function/Function")) {
                    for (MethodNode mn : cn.methods) {
                        if (isObfuscatedShortName(mn.name) && Type.getArgumentTypes(mn.desc).length == 1) {
                            mapMethod(cn.name, mn.name, mn.desc, "apply");
                        }
                    }
                }
                if (cn.interfaces.contains("java/util/Comparator")) {
                    for (MethodNode mn : cn.methods) {
                        if (isObfuscatedShortName(mn.name) && Type.getArgumentTypes(mn.desc).length == 2
                                && Type.getReturnType(mn.desc) == Type.INT_TYPE) {
                            mapMethod(cn.name, mn.name, mn.desc, "compare");
                        }
                    }
                }
            }
            // Restore abstract method on mo (PacketHandler) if needed
            if ("mo".equals(cn.name) && (cn.access & Opcodes.ACC_INTERFACE) != 0) {
                if (cn.methods.isEmpty()) {
                    cn.methods.add(new MethodNode(
                            Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                            "handle",
                            "(Lmn;Lnet/minecraft/network/Connection;)V",
                            "(TT;Lnet/minecraft/network/Connection;)V",
                            null
                    ));
                }
                for (MethodNode mn : cn.methods) {
                    mapMethod("mo", mn.name, mn.desc, "handle");
                }
            }
        }

        // 3. Disambiguate overloaded methods within the same class that have the same mapped name
        // and the same number of parameters where reference types could collide or return types differ!
        for (ClassNode cn : classes.values()) {
            // Group non-constructor, non-bridge methods by (mappedName, argCount)
            Map<String, List<MethodNode>> groups = new LinkedHashMap<>();
            for (MethodNode mn : cn.methods) {
                if (mn.name.startsWith("<") || (mn.access & Opcodes.ACC_BRIDGE) != 0) continue;
                String mapped = methodMap.getOrDefault(cn.name + "." + mn.name + mn.desc, mn.name);
                int argCount = Type.getArgumentTypes(mn.desc).length;
                groups.computeIfAbsent(mapped + "#" + argCount, k -> new ArrayList<>()).add(mn);
            }

            for (List<MethodNode> group : groups.values()) {
                if (group.size() <= 1) continue;
                // Check if any two methods in `group` have different return types OR compatible/object parameter types at the same position
                boolean hasConflict = false;
                for (int i = 0; i < group.size(); i++) {
                    for (int j = i + 1; j < group.size(); j++) {
                        MethodNode m1 = group.get(i);
                        MethodNode m2 = group.get(j);
                        Type r1 = Type.getReturnType(m1.desc);
                        Type r2 = Type.getReturnType(m2.desc);
                        if (!r1.equals(r2)) {
                            hasConflict = true;
                            break;
                        }
                        // Even if return types match, if one method is a synthetic lambda and another is an instance/static method with compatible reference params
                        if (((m1.access ^ m2.access) & Opcodes.ACC_SYNTHETIC) != 0) {
                            hasConflict = true;
                            break;
                        }
                    }
                    if (hasConflict) break;
                }
                if (!hasConflict) continue;

                // Disambiguate every method in `group` that doesn't already have an explicit non-obfuscated mapping
                // AND doesn't override a superclass/interface method!
                Set<String> usedInClass = new HashSet<>();
                for (MethodNode mn : cn.methods) {
                    usedInClass.add(methodMap.getOrDefault(cn.name + "." + mn.name + mn.desc, mn.name));
                }
                for (int i = 0; i < group.size(); i++) {
                    MethodNode mn = group.get(i);
                    String key = cn.name + "." + mn.name + mn.desc;
                    if (methodMap.containsKey(key)) continue;
                    if (overridesSuperMethod(cn, mn.name, mn.desc)) continue;

                    String retSimple = simpleTypeTag(Type.getReturnType(mn.desc));
                    StringBuilder paramTag = new StringBuilder();
                    for (Type pt : Type.getArgumentTypes(mn.desc)) {
                        paramTag.append("_").append(simpleTypeTag(pt));
                    }
                    String candidate = mn.name + "_" + retSimple + paramTag;
                    if (usedInClass.contains(candidate)) {
                        candidate = candidate + "_" + i;
                    }
                    usedInClass.add(candidate);
                    mapMethod(cn.name, mn.name, mn.desc, candidate);
                }
            }
        }
    }

    private static boolean overridesSuperMethod(ClassNode cn, String name, String desc) {
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (cn.superName != null) queue.add(cn.superName);
        if (cn.interfaces != null) queue.addAll(cn.interfaces);
        while (!queue.isEmpty()) {
            String sup = queue.poll();
            if (sup == null || !visited.add(sup)) continue;
            ClassNode scn = classes.get(sup);
            if (scn != null) {
                for (MethodNode smn : scn.methods) {
                    if (smn.name.equals(name) && smn.desc.equals(desc) && (smn.access & Opcodes.ACC_PRIVATE) == 0) {
                        return true;
                    }
                }
                if (scn.superName != null) queue.add(scn.superName);
                if (scn.interfaces != null) queue.addAll(scn.interfaces);
            }
        }
        return false;
    }

    private static String simpleTypeTag(Type t) {
        return switch (t.getSort()) {
            case Type.VOID -> " void".trim();
            case Type.BOOLEAN -> "bool";
            case Type.CHAR -> "char";
            case Type.BYTE -> "byte";
            case Type.SHORT -> "short";
            case Type.INT -> "int";
            case Type.FLOAT -> "float";
            case Type.LONG -> "long";
            case Type.DOUBLE -> "double";
            case Type.ARRAY -> simpleTypeTag(t.getElementType()) + "Arr";
            case Type.OBJECT -> {
                String internal = t.getInternalName();
                String mapped = classMap.getOrDefault(internal, internal);
                int idx = Math.max(mapped.lastIndexOf('/'), mapped.lastIndexOf('$'));
                yield idx >= 0 ? mapped.substring(idx + 1) : mapped;
            }
            default -> "val";
        };
    }

    /**
     * Compacts sparse local variable indices (caused by obfuscator stack spilling)
     * into contiguous 0..N slots so Vineflower emits var1, var2, var3 instead of var214.
     */
    private static void compactLocalVariables(ClassNode cn, MethodNode mn) {
        if (mn.instructions == null || mn.instructions.size() == 0) return;

        boolean isStatic = (mn.access & Opcodes.ACC_STATIC) != 0;
        Type[] argTypes = Type.getArgumentTypes(mn.desc);

        Map<Integer, Integer> slotMap = new HashMap<>();
        int nextSlot = 0;
        if (!isStatic) {
            slotMap.put(0, 0);
            nextSlot = 1;
        }
        for (Type t : argTypes) {
            slotMap.put(nextSlot, nextSlot);
            nextSlot += t.getSize();
        }

        // First pass: map all used local variable slots in order of appearance
        for (AbstractInsnNode insn = mn.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof VarInsnNode vin) {
                int oldSlot = vin.var;
                int size = (vin.getOpcode() == Opcodes.LLOAD || vin.getOpcode() == Opcodes.DLOAD
                        || vin.getOpcode() == Opcodes.LSTORE || vin.getOpcode() == Opcodes.DSTORE) ? 2 : 1;
                if (!slotMap.containsKey(oldSlot)) {
                    slotMap.put(oldSlot, nextSlot);
                    nextSlot += size;
                }
                vin.var = slotMap.get(oldSlot);
            } else if (insn instanceof IincInsnNode iinc) {
                int oldSlot = iinc.var;
                if (!slotMap.containsKey(oldSlot)) {
                    slotMap.put(oldSlot, nextSlot);
                    nextSlot += 1;
                }
                iinc.var = slotMap.get(oldSlot);
            }
        }

        mn.maxLocals = nextSlot;
        if (mn.localVariables != null) {
            mn.localVariables.clear();
        }
    }

    private static boolean isObfuscatedShortName(String name) {
        return name.length() <= 3 && !name.equals("x") && !name.equals("y") && !name.equals("z") && !name.equals("id") && !name.equals("ip") && !name.equals("mc");
    }

    private static String toCamelCaseIdentifier(String raw) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (sb.isEmpty()) {
                    sb.append(Character.toLowerCase(c));
                } else if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(c);
                }
            } else {
                capitalizeNext = true;
            }
        }
        if (sb.isEmpty()) {
            return "setting";
        }
        // Lowercase leading uppercase run (e.g. "FOV" -> "fov", "ESPMode" -> "espMode", "HWater" -> "hWater")
        String res = sb.toString();
        if (res.length() > 1 && Character.isUpperCase(res.charAt(1))) {
            int i = 0;
            while (i < res.length() && Character.isUpperCase(res.charAt(i))) {
                i++;
            }
            if (i == res.length()) {
                res = res.toLowerCase();
            } else if (i > 1) {
                res = res.substring(0, i - 1).toLowerCase() + res.substring(i - 1);
            }
        }
        if (!Character.isJavaIdentifierStart(res.charAt(0))) {
            res = "s" + res;
        }
        if (isJavaKeyword(res)) {
            res = res + "Setting";
        }
        return res;
    }

    private static String sanitizePascalCase(String raw) {
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(cap ? Character.toUpperCase(c) : c);
                cap = false;
            } else {
                cap = true;
            }
        }
        return sb.toString();
    }

    private static boolean isJavaKeyword(String s) {
        return Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
                "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for",
                "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
                "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
                "true", "false", "null").contains(s);
    }

    private static String deduplicateName(String base, Set<String> used) {
        if (!used.contains(base)) return base;
        int idx = 2;
        while (used.contains(base + idx)) {
            idx++;
        }
        return base + idx;
    }

    private static void mapClass(String oldName, String newName) {
        classMap.put(oldName, newName);
    }

    private static void mapField(String owner, String name, String desc, String newName) {
        fieldMap.put(owner + "." + name + ":" + desc, newName);
    }

    private static void mapMethod(String owner, String name, String desc, String newName) {
        methodMap.put(owner + "." + name + desc, newName);
    }

    private static void mapAnnotationMethod(String owner, String name, String newName) {
        annotationMethodMap.put(owner + "." + name, newName);
        ClassNode cn = classes.get(owner);
        if (cn != null) {
            for (MethodNode mn : cn.methods) {
                if (mn.name.equals(name)) {
                    methodMap.put(owner + "." + name + mn.desc, newName);
                }
            }
        }
    }

    /**
     * Inheritance-aware ASM Remapper.
     */
    private static class SemanticRemapper extends Remapper {

        @Override
        public String map(String internalName) {
            return classMap.getOrDefault(internalName, internalName);
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            String resolved = resolveField(owner, name, descriptor, new HashSet<>());
            return resolved != null ? resolved : name;
        }

        private String resolveField(String owner, String name, String descriptor, Set<String> visited) {
            if (owner == null || !visited.add(owner)) return null;
            String direct = fieldMap.get(owner + "." + name + ":" + descriptor);
            if (direct != null) return direct;

            ClassNode cn = classes.get(owner);
            if (cn != null) {
                if (cn.superName != null) {
                    String sup = resolveField(cn.superName, name, descriptor, visited);
                    if (sup != null) return sup;
                }
                if (cn.interfaces != null) {
                    for (String itf : cn.interfaces) {
                        String res = resolveField(itf, name, descriptor, visited);
                        if (res != null) return res;
                    }
                }
            }
            return null;
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            if ("<init>".equals(name) || "<clinit>".equals(name)) return name;
            String resolved = resolveMethod(owner, name, descriptor, new HashSet<>());
            return resolved != null ? resolved : name;
        }

        private String resolveMethod(String owner, String name, String descriptor, Set<String> visited) {
            return resolveMethod(owner, name, descriptor, visited, true);
        }

        private String resolveMethod(String owner, String name, String descriptor, Set<String> visited, boolean isDirectOwner) {
            if (owner == null || !visited.add(owner)) return null;
            ClassNode cn = classes.get(owner);
            if (!isDirectOwner && cn != null) {
                for (MethodNode mn : cn.methods) {
                    if (mn.name.equals(name) && mn.desc.equals(descriptor) && (mn.access & Opcodes.ACC_PRIVATE) != 0) {
                        return null;
                    }
                }
            }
            String direct = methodMap.get(owner + "." + name + descriptor);
            if (direct != null) return direct;

            if (cn != null) {
                if (cn.superName != null) {
                    String sup = resolveMethod(cn.superName, name, descriptor, visited, false);
                    if (sup != null) return sup;
                }
                if (cn.interfaces != null) {
                    for (String itf : cn.interfaces) {
                        String res = resolveMethod(itf, name, descriptor, visited, false);
                        if (res != null) return res;
                    }
                }
            }
            return null;
        }

        @Override
        public String mapAnnotationAttributeName(String descriptor, String name) {
            if (descriptor != null && descriptor.startsWith("L") && descriptor.endsWith(";")) {
                String owner = descriptor.substring(1, descriptor.length() - 1);
                String mapped = annotationMethodMap.get(owner + "." + name);
                if (mapped != null) return mapped;
            }
            return name;
        }

        @Override
        public String mapRecordComponentName(String owner, String name, String descriptor) {
            String mapped = fieldMap.get(owner + "." + name + ":" + descriptor);
            return mapped != null ? mapped : name;
        }
    }

    /**
     * Custom ClassRemapper that also remaps bootstrap method arguments for records
     * (ObjectMethods.bootstrap field names string "a;b;c" -> "pos;player;damage")
     * and Enum.valueOf(Class, String) calls if enum constants are referenced dynamically.
     */
    private static class CustomClassRemapper extends ClassRemapper {

        private String currentOriginalClassName;

        public CustomClassRemapper(ClassVisitor classVisitor, Remapper remapper) {
            super(Opcodes.ASM9, classVisitor, remapper);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.currentOriginalClassName = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
            return new MethodRemapper(Opcodes.ASM9, methodVisitor, this.remapper) {
                @Override
                public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                    // Check if this is java/lang/runtime/ObjectMethods.bootstrap for a Record
                    if ("java/lang/runtime/ObjectMethods".equals(bootstrapMethodHandle.getOwner())
                            && "bootstrap".equals(bootstrapMethodHandle.getName())
                            && bootstrapMethodArguments.length >= 2
                            && bootstrapMethodArguments[1] instanceof String fieldNamesStr) {
                        Object[] newArgs = bootstrapMethodArguments.clone();
                        List<String> newNames = new ArrayList<>();
                        for (int i = 2; i < newArgs.length; i++) {
                            if (newArgs[i] instanceof Handle h) {
                                String mappedName = remapper.mapFieldName(h.getOwner(), h.getName(), h.getDesc());
                                newNames.add(mappedName);
                            }
                        }
                        if (!newNames.isEmpty()) {
                            newArgs[1] = String.join(";", newNames);
                        }
                        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, newArgs);
                        return;
                    }
                    super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                }
            };
        }
    }
}
