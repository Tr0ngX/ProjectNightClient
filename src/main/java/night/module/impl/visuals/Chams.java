package night.module.impl.visuals;

import java.awt.Color;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.CategorySetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.duck.ChamsRenderStateDuck;
import night.util.render.GlintRenderLayer;
import night.util.world.EntityUtil;

@ModuleInfo(name = "Chams", description = "Adds a customizable render on top of the default Minecraft rendering.", category = Module.Category.VISUALS)
public class Chams extends Module {
   public NumberSetting range = new NumberSetting("Range", "The maximum range at which chams will be rendered.", 64.0F, 1.0F, 256.0F);
   public CategorySetting entitiesCategory = new CategorySetting("Entities", "The category for settings related to chams rendered on living entities.");
   public BooleanSetting players = new BooleanSetting(
      "Players", "Renders the chams on player entities.", new CategorySetting.CategoryCondition(this.entitiesCategory), true
   );
   public BooleanSetting hostiles = new BooleanSetting(
      "Hostiles", "Renders the chams on hostile entities.", new CategorySetting.CategoryCondition(this.entitiesCategory), false
   );
   public BooleanSetting passives = new BooleanSetting(
      "Passives", "Renders the chams on passive entities.", new CategorySetting.CategoryCondition(this.entitiesCategory), false
   );
   public BooleanSetting entityPulse = new BooleanSetting(
      "EntityPulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.CategoryCondition(this.entitiesCategory), false
   );
   public BooleanSetting entityShine = new BooleanSetting(
      "EntityShine", "Shine", "Adds a shine effect to the chams.", new CategorySetting.CategoryCondition(this.entitiesCategory), false
   );
   public BooleanSetting entityGlint = new BooleanSetting(
      "EntityGlint", "Glint", "Adds a scrolling texture overlay to living entities.", new CategorySetting.CategoryCondition(this.entitiesCategory), false
   );
   public ColorSetting entityGlintColor = new ColorSetting(
      "EntityGlintColor",
      "GlintColor",
      "Color tint for the entity glint overlay.",
      new BooleanSetting.BooleanCondition(this.entityGlint, true),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 180), false, false)
   );
   public NumberSetting entityGlintSpeedU = new NumberSetting(
      "EntityGlintSpeedU",
      "GlintSpeedU",
      "Horizontal scroll speed of entity glint.",
      new BooleanSetting.BooleanCondition(this.entityGlint, true),
      0.25F,
      -5.0F,
      5.0F
   );
   public NumberSetting entityGlintSpeedV = new NumberSetting(
      "EntityGlintSpeedV",
      "GlintSpeedV",
      "Vertical scroll speed of entity glint.",
      new BooleanSetting.BooleanCondition(this.entityGlint, true),
      0.1F,
      -5.0F,
      5.0F
   );
   public NumberSetting entityGlintScale = new NumberSetting(
      "EntityGlintScale",
      "GlintScale",
      "Scale of the glint texture on entity models.",
      new BooleanSetting.BooleanCondition(this.entityGlint, true),
      1.0F,
      0.1F,
      5.0F
   );
   public ModeSetting entityGlintDirection = new ModeSetting(
      "EntityGlintDirection",
      "GlintDirection",
      "Projection mode for entity glint UVs.",
      new BooleanSetting.BooleanCondition(this.entityGlint, true),
      "Diagonal",
      new String[]{"Diagonal", "Horizontal", "Vertical"}
   );
   public ModeSetting entityMode = new ModeSetting(
      "EntityMode",
      "Mode",
      "The rendering that will be applied to living entities.",
      new CategorySetting.CategoryCondition(this.entitiesCategory),
      "Both",
      new String[]{"Fill", "Outline", "Both"}
   );
   public ColorSetting entityFillColor = new ColorSetting(
      "EntityFillColor",
      "FillColor",
      "The color that will be used for the fill rendering.",
      new ModeSetting.ModeCondition(this.entityMode, "Fill", "Both"),
      ColorUtil.b()
   );
   public ColorSetting entityOutlineColor = new ColorSetting(
      "EntityOutlineColor",
      "OutlineColor",
      "The color that will be used for the outline rendering.",
      new ModeSetting.ModeCondition(this.entityMode, "Outline", "Both"),
      ColorUtil.c()
   );
   public ModeSetting friendMode = new ModeSetting(
      "FriendMode",
      "The mode for the friend color.",
      new ModeSetting.ModeCondition(this.entityMode, "Fill", "Both"),
      "Default",
      new String[]{"Default", "Custom", "Sync"}
   );
   public ColorSetting friendFillColor = new ColorSetting(
      "FriendFillColor",
      "FriendFillColor",
      "The color that will be used for the fill rendering on friends.",
      new ModeSetting.ModeCondition(this.entityMode, "Fill", "Both"),
      new ColorSetting.ColorValue(new Color(85, 255, 255, ColorUtil.b().getColor().getAlpha()), false, false)
   );
   public ColorSetting friendOutlineColor = new ColorSetting(
      "FriendOutlineColor",
      "FriendOutlineColor",
      "The color that will be used for the outline rendering on friends.",
      new ModeSetting.ModeCondition(this.entityMode, "Outline", "Both"),
      new ColorSetting.ColorValue(new Color(85, 255, 255, ColorUtil.c().getColor().getAlpha()), false, false)
   );
   public BooleanSetting x = new BooleanSetting(
      "DamageModify", "Changes the color of the chams when a player takes damage.", new ModeSetting.ModeCondition(this.entityMode, "Fill", "Both"), false
   );
   public ColorSetting y = new ColorSetting(
      "DamageColor",
      "The color to apply on chams when a player takes damage.",
      new BooleanSetting.BooleanCondition(this.x, true),
      new ColorSetting.ColorValue(new Color(255, 0, 0), false, false)
   );
   public NumberSetting z = new NumberSetting(
      "EntityOpacity",
      "Opacity",
      "How see-through the target's real skin renders (100 = untouched).",
      new CategorySetting.CategoryCondition(this.entitiesCategory),
      100,
      0,
      100
   );
   public CategorySetting crystalsCategory = new CategorySetting("Crystals", "The category for settings related to crystal chams.");
   public BooleanSetting crystals = new BooleanSetting(
      "Crystals", "Enabled", "Renders the chams on crystal entities.", new CategorySetting.CategoryCondition(this.crystalsCategory), true
   );
   public BooleanSetting crystalPulse = new BooleanSetting(
      "CrystalPulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.CategoryCondition(this.crystalsCategory), false
   );
   public BooleanSetting crystalShine = new BooleanSetting(
      "CrystalShine", "Shine", "Adds a shine effect on crystal chams.", new CategorySetting.CategoryCondition(this.crystalsCategory), false
   );
   public BooleanSetting crystalGlint = new BooleanSetting(
      "CrystalGlint", "Glint", "Adds a scrolling texture overlay to crystals.", new CategorySetting.CategoryCondition(this.crystalsCategory), false
   );
   public ColorSetting crystalGlintColor = new ColorSetting(
      "CrystalGlintColor",
      "GlintColor",
      "Color tint for the crystal glint overlay.",
      new BooleanSetting.BooleanCondition(this.crystalGlint, true),
      new ColorSetting.ColorValue(new Color(255, 255, 255, 180), false, false)
   );
   public NumberSetting crystalGlintSpeedU = new NumberSetting(
      "CrystalGlintSpeedU",
      "GlintSpeedU",
      "Horizontal scroll speed of crystal glint.",
      new BooleanSetting.BooleanCondition(this.crystalGlint, true),
      0.25F,
      -5.0F,
      5.0F
   );
   public NumberSetting crystalGlintSpeedV = new NumberSetting(
      "CrystalGlintSpeedV",
      "GlintSpeedV",
      "Vertical scroll speed of crystal glint.",
      new BooleanSetting.BooleanCondition(this.crystalGlint, true),
      0.1F,
      -5.0F,
      5.0F
   );
   public NumberSetting crystalGlintScale = new NumberSetting(
      "CrystalGlintScale",
      "GlintScale",
      "Scale of the glint texture on crystal models.",
      new BooleanSetting.BooleanCondition(this.crystalGlint, true),
      1.0F,
      0.1F,
      5.0F
   );
   public ModeSetting crystalGlintDirection = new ModeSetting(
      "CrystalGlintDirection",
      "GlintDirection",
      "Projection mode for crystal glint UVs.",
      new BooleanSetting.BooleanCondition(this.crystalGlint, true),
      "Diagonal",
      new String[]{"Diagonal", "Horizontal", "Vertical"}
   );
   public ModeSetting crystalMode = new ModeSetting(
      "CrystalMode",
      "Mode",
      "The rendering that will be applied to crystal entities.",
      new CategorySetting.CategoryCondition(this.crystalsCategory),
      "Both",
      new String[]{"Fill", "Outline", "Both"}
   );
   public ColorSetting crystalFillColor = new ColorSetting(
      "CrystalFillColor",
      "FillColor",
      "The color that will be used for the fill rendering.",
      new ModeSetting.ModeCondition(this.crystalMode, "Fill", "Both"),
      ColorUtil.b()
   );
   public ColorSetting crystalOutlineColor = new ColorSetting(
      "CrystalOutlineColor",
      "OutlineColor",
      "The color that will be used for the outline rendering.",
      new ModeSetting.ModeCondition(this.crystalMode, "Outline", "Both"),
      ColorUtil.c()
   );
   public NumberSetting crystalOpacity = new NumberSetting(
      "CrystalOpacity",
      "Opacity",
      "How see-through the crystal's real texture renders (100 = untouched).",
      new CategorySetting.CategoryCondition(this.crystalsCategory),
      100,
      0,
      100
   );
   public CategorySetting vehiclesCategory = new CategorySetting("Vehicles", "The category for settings related to boat/minecart chams.");
   public BooleanSetting vehicles = new BooleanSetting(
      "Vehicles", "Enabled", "Renders the chams on all boat and minecart types.", new CategorySetting.CategoryCondition(this.vehiclesCategory), true
   );
   public BooleanSetting vehiclePulse = new BooleanSetting(
      "VehiclePulse", "Pulse", "Adds a pulsing effect to the chams opacity.", new CategorySetting.CategoryCondition(this.vehiclesCategory), false
   );
   public BooleanSetting vehicleShine = new BooleanSetting(
      "VehicleShine", "Shine", "Adds a shine effect on vehicle chams.", new CategorySetting.CategoryCondition(this.vehiclesCategory), false
   );
   public ModeSetting vehicleMode = new ModeSetting(
      "VehicleMode",
      "Mode",
      "The rendering that will be applied to boat/minecart entities.",
      new CategorySetting.CategoryCondition(this.vehiclesCategory),
      "Both",
      new String[]{"Fill", "Outline", "Both"}
   );
   public ColorSetting vehicleFillColor = new ColorSetting(
      "VehicleFillColor",
      "FillColor",
      "The color that will be used for the fill rendering.",
      new ModeSetting.ModeCondition(this.vehicleMode, "Fill", "Both"),
      ColorUtil.b()
   );
   public ColorSetting vehicleOutlineColor = new ColorSetting(
      "VehicleOutlineColor",
      "OutlineColor",
      "The color that will be used for the outline rendering.",
      new ModeSetting.ModeCondition(this.vehicleMode, "Outline", "Both"),
      ColorUtil.c()
   );
   public NumberSetting vehicleOpacity = new NumberSetting(
      "VehicleOpacity",
      "Opacity",
      "How see-through the vehicle's real texture renders (100 = untouched).",
      new CategorySetting.CategoryCondition(this.vehiclesCategory),
      100,
      0,
      100
   );

   @EventTarget
   public void a_void_Render3DEvent(Render3DEvent var1) {
      if (this.entityGlint.getValue()) {
         GlintRenderLayer.b.a = this.entityGlintSpeedU.getValue().floatValue();
         GlintRenderLayer.b.b = this.entityGlintSpeedV.getValue().floatValue();
         GlintRenderLayer.b.c = this.entityGlintScale.getValue().floatValue();
         GlintRenderLayer.b.d = this.entityGlintDirection.getValue();
      }

      if (this.crystalGlint.getValue()) {
         GlintRenderLayer.c.a = this.crystalGlintSpeedU.getValue().floatValue();
         GlintRenderLayer.c.b = this.crystalGlintSpeedV.getValue().floatValue();
         GlintRenderLayer.c.c = this.crystalGlintScale.getValue().floatValue();
         GlintRenderLayer.c.d = this.crystalGlintDirection.getValue();
      }

      if (this.entityGlint.getValue() || this.crystalGlint.getValue()) {
         GlintRenderLayer.a();
      }
   }

   public boolean a_bool_Entity(Entity var1) {
      if (var1 == null || mc.player == null) {
         return true;
      }

      if (var1 == mc.player) {
         return true;
      }

      float var2 = this.range.getValue().floatValue() * (float)EntityUtil.b_double_int(8);
      return mc.player.distanceToSqr(var1) <= var2 * var2;
   }

   public boolean b_bool_Entity(Entity var1) {
      if (!this.a_bool_Entity(var1)) {
         return false;
      } else if (this.players.getValue() && var1.getType() == EntityTypes.PLAYER) {
         return true;
      } else {
         return this.hostiles.getValue() && EntityUtil.g(var1) ? true : this.passives.getValue() && EntityUtil.h(var1) && !EntityUtil.g(var1);
      }
   }

   public void a(LivingEntity var1, ChamsRenderStateDuck var2) {
      boolean var3 = this.entityMode.getValue().equals("Fill") || this.entityMode.getValue().equals("Both");
      boolean var4 = this.entityMode.getValue().equals("Outline") || this.entityMode.getValue().equals("Both");
      var2.night$setChams(
         var3,
         this.a(var1, this.entityFillColor.getColor(), this.friendFillColor.getColor()).getRGB(),
         var4,
         this.a(var1, this.entityOutlineColor.getColor(), this.friendOutlineColor.getColor()).getRGB(),
         this.entityShine.getValue()
      );
      var2.night$setChamsRealAlpha(this.z.getValue().floatValue() / 100.0F);
      if (this.entityGlint.getValue()) {
         var2.night$setChamsGlint(true, this.entityGlintColor.getColor().getRGB(), GlintRenderLayer.b);
      }
   }

   public void a_void_ChamsRenderStateDuck(ChamsRenderStateDuck var1) {
      boolean var2 = this.crystalMode.getValue().equals("Fill") || this.crystalMode.getValue().equals("Both");
      boolean var3 = this.crystalMode.getValue().equals("Outline") || this.crystalMode.getValue().equals("Both");
      Color var4 = this.crystalPulse.getValue() ? ColorUtil.a_Color_Color(this.crystalFillColor.getColor()) : this.crystalFillColor.getColor();
      Color var5 = this.crystalPulse.getValue() ? ColorUtil.a_Color_Color(this.crystalOutlineColor.getColor()) : this.crystalOutlineColor.getColor();
      var1.night$setChams(var2, var4.getRGB(), var3, var5.getRGB(), this.crystalShine.getValue());
      var1.night$setChamsRealAlpha(this.crystalOpacity.getValue().floatValue() / 100.0F);
      if (this.crystalGlint.getValue()) {
         var1.night$setChamsGlint(true, this.crystalGlintColor.getColor().getRGB(), GlintRenderLayer.c);
      }
   }

   public void b_void_ChamsRenderStateDuck(ChamsRenderStateDuck var1) {
      boolean var2 = this.vehicleMode.getValue().equals("Fill") || this.vehicleMode.getValue().equals("Both");
      boolean var3 = this.vehicleMode.getValue().equals("Outline") || this.vehicleMode.getValue().equals("Both");
      Color var4 = this.vehiclePulse.getValue() ? ColorUtil.a_Color_Color(this.vehicleFillColor.getColor()) : this.vehicleFillColor.getColor();
      Color var5 = this.vehiclePulse.getValue() ? ColorUtil.a_Color_Color(this.vehicleOutlineColor.getColor()) : this.vehicleOutlineColor.getColor();
      var1.night$setChams(var2, var4.getRGB(), var3, var5.getRGB(), this.vehicleShine.getValue());
      var1.night$setChamsRealAlpha(this.vehicleOpacity.getValue().floatValue() / 100.0F);
   }

   private Color a(LivingEntity var1, Color var2, Color var3) {
      boolean var4 = this.x.getValue() && var1.hurtTime > 0;
      Color var6;
      if (var1 instanceof Player var5 && Night.friendManager.isFriend(var5.getName().getString()) && !this.friendMode.getValue().equals("Sync")) {
         var6 = var4
            ? ColorUtil.a(this.y.getColor(), var3.getAlpha())
            : (this.friendMode.getValue().equals("Default") ? Night.friendManager.getFriendColor(var3.getAlpha()) : var3);
      } else {
         var6 = var4 ? ColorUtil.a(this.y.getColor(), var2.getAlpha()) : var2;
      }

      return this.entityPulse.getValue() ? ColorUtil.a_Color_Color(var6) : var6;
   }
}
