package night.module.impl.visuals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.PacketReceiveEvent;
import night.events.impl.Render3DEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.impl.misc.FakePlayer;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.render.MemeKillEffectRenderer;
import night.util.world.EntityUtil;
import org.joml.Vector3f;

@ModuleInfo(name = "DeathEffects", description = "Renders certain effects on players when they die or disconnect.", category = Module.Category.VISUALS)
public class DeathEffects extends Module {
   public ModeSetting mode = new ModeSetting("Mode", "The effect that will be rendered.", "Both", new String[]{"Lightning", "Overlay", "Meme", "Both"});
   public BooleanSetting lightningOnKill = new BooleanSetting(
      "LightningOnKill", "Spawn lightning bolt when a player dies near you.", new ModeSetting.ModeCondition(this.mode, "Lightning", "Both"), true
   );
   public BooleanSetting lightningOnDisconnect = new BooleanSetting(
      "LightningOnDisconnect", "Spawn lightning bolt when a player disconnects near you.", new ModeSetting.ModeCondition(this.mode, "Lightning", "Both"), true
   );
   public NumberSetting lightningAmount = new NumberSetting(
      "LightningAmount", "The amount of lightning bolts to spawn.", new ModeSetting.ModeCondition(this.mode, "Lightning", "Both"), 1, 1, 10
   );
   public NumberSetting disconnectRange = new NumberSetting("DisconnectRange", "Range to detect player disconnects.", 64.0, 1.0, 256.0);
   public BooleanSetting debug = new BooleanSetting("Debug", "Logs every tracked player that vanishes and whether an effect fired.", false);
   public BooleanSetting overlayOnKill = new BooleanSetting(
      "OverlayOnKill", "Show freeze overlay when a player dies near you.", new ModeSetting.ModeCondition(this.mode, "Overlay"), true
   );
   public BooleanSetting overlayOnDisconnect = new BooleanSetting(
      "OverlayOnDisconnect", "Show freeze overlay when a player disconnects near you.", new ModeSetting.ModeCondition(this.mode, "Overlay"), true
   );
   public NumberSetting overlayDuration = new NumberSetting(
      "OverlayDuration", "Duration of the freeze overlay in milliseconds.", new ModeSetting.ModeCondition(this.mode, "Overlay"), 3000, 100, 10000
   );
   public NumberSetting overlayFadeDuration = new NumberSetting(
      "OverlayFadeDuration", "Fade duration of the freeze overlay in milliseconds.", new ModeSetting.ModeCondition(this.mode, "Overlay"), 2000, 100, 5000
   );
   public NumberSetting overlayAlpha = new NumberSetting(
      "OverlayAlpha",
      "Transparency of the freeze overlay (0-255, higher = more transparent).",
      new ModeSetting.ModeCondition(this.mode, "Overlay"),
      200,
      0,
      255
   );
   public BooleanSetting memeOnKill = new BooleanSetting(
      "MemeOnKill", "Show the meme effect when a player dies near you.", new ModeSetting.ModeCondition(this.mode, "Meme", "Both"), true
   );
   public BooleanSetting memeOnDisconnect = new BooleanSetting(
      "MemeOnDisconnect", "Show the meme effect when a player disconnects near you.", new ModeSetting.ModeCondition(this.mode, "Meme", "Both"), true
   );
   public NumberSetting memeSize = new NumberSetting(
      "MemeSize", "Half-size of the circle/arrow, in blocks.", new ModeSetting.ModeCondition(this.mode, "Meme", "Both"), 1.0, 0.3, 3.0
   );
   private static final Identifier s = Identifier.fromNamespaceAndPath("night", "vine");
   private static final SoundEvent t = SoundEvent.createVariableRangeEvent(s);
   private final Map<UUID, Vec3> u = new HashMap<>();
   private final Set<UUID> v = new HashSet<>();
   private long w = 0L;
   private boolean x = false;
   private int y = -90000;

   @Override
   public void onEnable() {
      this.u.clear();
      this.v.clear();
      this.w = 0L;
      this.x = false;
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         mc.player.setTicksFrozen(0);
      }

      this.x = false;
      this.w = 0L;
      this.u.clear();
      this.v.clear();
   }

   private boolean a_bool_Player(Player var1) {
      if (var1 == null || var1 == mc.player) {
         return true;
      }

      if (var1.getId() < 0) {
         return true;
      }

      if (EntityUtil.c_bool_Entity(var1)) {
         return true;
      }

      FakePlayer var2 = Night.moduleManager != null ? Night.moduleManager.getModule(FakePlayer.class) : null;
      return var2 != null && var2.isEnabled() && var1 == var2.z();
   }

   @EventTarget
   public void a_void_PacketReceiveEvent(PacketReceiveEvent var1) {
      if (mc.level != null && mc.player != null) {
         if (var1.getPacket() instanceof ClientboundEntityEventPacket var3 && var3.getEventId() == 3) {
            mc.execute(() -> {
               if (mc.level != null && mc.player != null) {
                  try {
                     if (var3.getEntity(mc.level) instanceof Player var3x && !this.a_bool_Player(var3x) && mc.player.distanceToSqr(var3x) <= 100.0) {
                        if ((this.mode.getValue().equals("Lightning") || this.mode.getValue().equals("Both")) && this.lightningOnKill.getValue()) {
                           this.a_void_Vec3(new Vec3(var3x.getX(), var3x.getY(), var3x.getZ()));
                        }

                        if (this.mode.getValue().equals("Overlay") && this.overlayOnKill.getValue()) {
                           this.w();
                        }

                        if ((this.mode.getValue().equals("Meme") || this.mode.getValue().equals("Both")) && this.memeOnKill.getValue()) {
                           this.b(new Vec3(var3x.getX(), var3x.getY() + var3x.getBbHeight() * 0.5, var3x.getZ()));
                        }
                     }
                  } catch (Exception var4) {
                  }
               }
            });
         }
      }
   }

   @EventTarget
   public void a_void_TickEvent(TickEvent var1) {
      if (mc.player != null && mc.level != null) {
         this.v();
         HashSet var2 = new HashSet();

         for (Player var4 : (Iterable<Player>)(Object)mc.level.players()) {
            if (!this.a_bool_Player(var4)) {
               var2.add(var4.getUUID());
               this.u.put(var4.getUUID(), new Vec3(var4.getX(), var4.getY(), var4.getZ()));
            }
         }

         for (UUID var9 : new HashSet<>(this.v)) {
            if (!var2.contains(var9) && this.u.containsKey(var9)) {
               Vec3 var5 = this.u.get(var9);
               double var6 = Math.sqrt(
                  Math.pow(mc.player.getX() - var5.x, 2.0) + Math.pow(mc.player.getY() - var5.y, 2.0) + Math.pow(mc.player.getZ() - var5.z, 2.0)
               );
               if (this.debug.getValue() && Night.chatManager != null) {
                  Night.chatManager
                     .sendTaggedMessage(
                        var9
                           + " vanished at "
                           + String.format("%.1f", var6)
                           + " blocks (range "
                           + this.disconnectRange.getValue()
                           + ", mode "
                           + this.mode.getValue()
                           + ")",
                        this.getName()
                     );
               }

               if (var6 <= this.disconnectRange.getValue().doubleValue()) {
                  if ((this.mode.getValue().equals("Meme") || this.mode.getValue().equals("Both")) && this.memeOnDisconnect.getValue()) {
                     this.b(var5.add(0.0, 0.9, 0.0));
                  }

                  if ((this.mode.getValue().equals("Lightning") || this.mode.getValue().equals("Both")) && this.lightningOnDisconnect.getValue()) {
                     this.a_void_Vec3(var5);
                  }

                  if (this.mode.getValue().equals("Overlay") && this.overlayOnDisconnect.getValue()) {
                     this.w();
                  }
               }

               this.u.remove(var9);
            }
         }

         this.v.clear();
         this.v.addAll(var2);
      }
   }

   private void v() {
      if (this.x && mc.player != null) {
         long var1 = System.currentTimeMillis() - (this.w - this.overlayDuration.getValue().longValue() - this.overlayFadeDuration.getValue().longValue());
         long var3 = this.overlayDuration.getValue().longValue() + this.overlayFadeDuration.getValue().longValue();
         if (var1 < this.overlayDuration.getValue().longValue()) {
            int var5 = (int)(140.0 * (this.overlayAlpha.getValue().doubleValue() / 255.0));
            mc.player.setTicksFrozen(var5);
         } else if (var1 < var3) {
            long var9 = var1 - this.overlayDuration.getValue().longValue();
            double var6 = 1.0 - (double)var9 / this.overlayFadeDuration.getValue().longValue();
            int var8 = (int)(140.0 * (this.overlayAlpha.getValue().doubleValue() / 255.0) * var6);
            mc.player.setTicksFrozen(var8);
         } else {
            this.x = false;
            this.w = 0L;
            mc.player.setTicksFrozen(0);
         }
      } else if (mc.player != null
         && mc.player.getTicksFrozen() > 0
         && mc.level != null
         && !mc.level.getBlockState(mc.player.blockPosition()).is(Blocks.POWDER_SNOW)) {
         mc.player.setTicksFrozen(0);
      }
   }

   private void w() {
      this.x = true;
      this.w = System.currentTimeMillis();
   }

   private void a_void_Vec3(Vec3 var1) {
      if (mc.level != null) {
         if (!mc.isSameThread()) {
            mc.execute(() -> this.a_void_Vec3(var1));
         } else {
            int var2 = this.lightningAmount.getValue().intValue();

            for (int var3 = 0; var3 < var2; var3++) {
               LightningBolt var4 = new LightningBolt(EntityTypes.LIGHTNING_BOLT, mc.level);
               var4.setPos(var1.x, var1.y, var1.z);
               var4.setId(this.y--);
               mc.level.addEntity(var4);
            }
         }
      }
   }

   private void b(Vec3 var1) {
      MemeKillEffectRenderer.a.a_void_Vector3f_float(new Vector3f((float)var1.x, (float)var1.y, (float)var1.z), this.memeSize.getValue().floatValue());
      if (mc.getSoundManager() != null) {
         mc.getSoundManager().play(SimpleSoundInstance.forUI(t, 1.0F, 1.0F));
      }
   }

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.mode.getValue().equals("Meme") || this.mode.getValue().equals("Both")) {
         MemeKillEffectRenderer.a.a(var1.getMatrices());
      }
   }
}
