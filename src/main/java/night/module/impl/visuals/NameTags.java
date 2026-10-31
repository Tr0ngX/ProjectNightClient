package night.module.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.Render3DEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.module.access.FreecamAccess;
import night.module.impl.misc.FakePlayer;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ColorSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.util.color.ColorUtil;
import night.util.render.Render3DUtil;
import night.util.world.EntityUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(
   name = "NameTags",
   description = "Replaces the default Minecraft NameTag with a more visible and customizable one.",
   category = Module.Category.VISUALS
)
public class NameTags extends Module {
   private static final Identifier v = Identifier.fromNamespaceAndPath("night", "textures/gui/logo.png");
   public BooleanSetting gameMode = new BooleanSetting("GameMode", "Renders the player's gamemode.", false);
   public BooleanSetting ping = new BooleanSetting("Ping", "Renders the player's latency to the server.", true);
   public BooleanSetting entityID = new BooleanSetting("EntityID", "Renders the player's entity ID.", false);
   public BooleanSetting health = new BooleanSetting("Health", "Renders the player's health and absorption.", true);
   public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Renders the amount of totems that the player has popped.", true);
   public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having nametags rendered for them.", false);
   public BooleanSetting nightCheck = new BooleanSetting("NightCheck", "Adds an indicator next to the name of other Night Client users.", true);
   public BooleanSetting self = new BooleanSetting("Self", "Also renders a nametag above your own player.", false);
   public BooleanSetting items = new BooleanSetting("Items", "Renders the items that the player is wearing or holding.", true);
   public BooleanSetting enchantments = new BooleanSetting("Enchantments", "Renders the enchantments of the player's items.", false);
   public BooleanSetting durability = new BooleanSetting("Durability", "Renders the durability of the player's items.", true);
   public BooleanSetting itemName = new BooleanSetting("ItemName", "Renders the name of the item that the player is currently holding.", true);
   public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around nametag text.", false);
   public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the nametag rendering.", 30, 10, 100);
   public ModeSetting border = new ModeSetting("Border", "The border that will surround the text.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
   public ColorSetting fillColor = new ColorSetting(
      "FillColor",
      "The color that will be used for the fill rendering.",
      new ModeSetting.ModeCondition(this.border, "Fill", "Both"),
      new ColorSetting.ColorValue(new Color(0, 0, 0, 100), false, false)
   );
   public ColorSetting outlineColor = new ColorSetting(
      "OutlineColor",
      "The color that will be used for the outline rendering.",
      new ModeSetting.ModeCondition(this.border, "Outline", "Both"),
      new ColorSetting.ColorValue(new Color(0, 0, 0, 100), false, false)
   );
   private static final Map<ItemEnchantments, List<String>> w = new ConcurrentHashMap<>();
   private static final Map<Holder<Enchantment>, String> x = new ConcurrentHashMap<>();

   @EventTarget
   public void a_void_Post(Render3DEvent.Post var1) {
      if (mc.level != null && mc.player != null) {
         PoseStack var2 = var1.getMatrices();

         for (Player var4 : (Iterable<Player>)(Object)mc.level.players().stream().sorted(Comparator.comparing(var0 -> -mc.player.distanceTo(var0))).toList()) {
            boolean var5 = ((FreecamAccess)Night.moduleManager.getModuleByName("Freecam")).isEnabled();
            if ((var4 != mc.player || this.self.getValue() && (!mc.options.getCameraType().isFirstPerson() || var5))
               && (!this.antiBot.getValue() || !EntityUtil.a_bool_Player(var4))
               && (
                  !(var4 instanceof RemotePlayer var6)
                     || (Night.moduleManager.getModule(PopChams.class) == null || !Night.moduleManager.getModule(PopChams.class).a_bool_Entity(var6))
                        && (Night.moduleManager.getModule(LogoutSpot.class) == null || !Night.moduleManager.getModule(LogoutSpot.class).a_bool_Entity(var6))
               )
               && Render3DUtil.a_bool_AABB(var4.getBoundingBox())) {
               double var40 = Mth.lerp(var1.getTickDelta(), var4.xo, var4.getX());
               double var7 = Mth.lerp(var1.getTickDelta(), var4.yo, var4.getY()) + (var4.isShiftKeyDown() ? 1.9F : 2.1F);
               double var9 = Mth.lerp(var1.getTickDelta(), var4.zo, var4.getZ());
               Vec3 var11 = new Vec3(
                  var40 - mc.gameRenderer.mainCamera().position().x,
                  var7 - mc.gameRenderer.mainCamera().position().y,
                  var9 - mc.gameRenderer.mainCamera().position().z
               );
               float var12 = (float)Math.sqrt(mc.gameRenderer.mainCamera().position().distanceToSqr(var40, var7, var9));
               float var13 = 0.0018F + this.scale.getValue().intValue() / 10000.0F * var12;
               if (var12 <= 8.0) {
                  var13 = 0.0245F;
               }

               var2.pushPose();
               var2.translate(var11.x, var11.y, var11.z);
               PacketAndRenderUtil.a(var2, mc.gameRenderer.mainCamera().rotation());
               var2.scale(var13, -var13, var13);
               boolean var14 = this.nightCheck.getValue() && Night.nightUserManager != null && Night.nightUserManager.isNightUser(var4);
               String var15 = var4.getName().getString();
               if (this.gameMode.getValue()) {
                  var15 = var15 + " [" + EntityUtil.a_String_GameType(EntityUtil.d_GameType_Player(var4)) + "]";
               }

               if (this.ping.getValue()) {
                  var15 = var15 + " " + EntityUtil.c_int_Player(var4) + "ms";
               }

               if (this.entityID.getValue()) {
                  var15 = var15 + " " + var4.getId();
               }

               if (this.health.getValue()) {
                  int var16 = Math.round(var4.getHealth() + var4.getAbsorptionAmount());
                  var15 = var15 + " " + ColorUtil.a_ChatFormatting_double(var4.getHealth() + var4.getAbsorptionAmount()) + var16 + ChatFormatting.RESET;
               }

               int var41 = Night.worldStateManager.getTotemPops().getOrDefault(var4.getUUID(), 0);
               if (this.totemPops.getValue() && var41 > 0) {
                  var15 = var15 + " " + ColorUtil.b_ChatFormatting_int(var41) + "-" + var41;
               }

               int var17 = Night.fontManager.getStringWidth(var15);
               int var18 = Night.fontManager.getFontHeight();
               int var19 = var14 ? var18 - 1 : 0;
               int var20 = var14 ? 3 : 0;
               int var21 = var17 + var19 + var20;
               if (this.border.getValue().equalsIgnoreCase("Fill") || this.border.getValue().equalsIgnoreCase("Both")) {
                  Render3DUtil.a(var2, -var21 / 2.0F - 2.0F, -var18 - 1, var21 / 2.0F + 2.0F, 1.0F, this.fillColor.getColor());
               }

               if (this.border.getValue().equalsIgnoreCase("Outline") || this.border.getValue().equalsIgnoreCase("Both")) {
                  Render3DUtil.b(var2, -var21 / 2.0F - 2.0F, -var18 - 1, var21 / 2.0F + 2.0F, 1.0F, this.outlineColor.getColor());
               }

               float var22 = -var21 / 2.0F;
               if (var14) {
                  Render3DUtil.a(var2, v, var22, -var18 + 0.5F, var22 + var19, 0.5F, -1);
                  var22 += var19 + var20;
               }

               Color var23 = Night.moduleManager.getModule(FakePlayer.class).isEnabled() && Night.moduleManager.getModule(FakePlayer.class).z() == var4
                  ? new Color(225, 0, 70)
                  : (
                     var4.isShiftKeyDown()
                        ? new Color(255, 170, 0)
                        : (Night.friendManager.isFriend(var4.getName().getString()) ? Night.friendManager.getFriendColor() : Color.WHITE)
                  );
               this.a(var2, var15, (int)var22, -var18, var23);
               float var24 = -var18 - 2;
               if (this.items.getValue()) {
                  ItemStack[] var25 = new ItemStack[6];
                  List[] var26 = new List[6];
                  int var27 = 0;
                  boolean var28 = false;

                  for (int var29 = 0; var29 < 6; var29++) {
                     ItemStack var30 = this.a(var4, var29);
                     var25[var29] = var30;
                     if (!var30.isEmpty()) {
                        var28 = true;
                        if (this.enchantments.getValue()) {
                           List var31 = this.a_List_ItemStack(var30);
                           var26[var29] = var31;
                           if (var31.size() > var27) {
                              var27 = var31.size();
                           }
                        }
                     }
                  }

                  if (var28) {
                     float var45 = var27 * 4.2F;
                     float var46 = Math.max(16.0F, var45);
                     float var47 = -var18 - 4.0F - var46;
                     var24 = var47;

                     for (int var32 = 0; var32 < 6; var32++) {
                        ItemStack var33 = var25[var32];
                        if (var33 != null && !var33.isEmpty()) {
                           int var34 = -54 + var32 * 18 + 1;
                           var2.pushPose();
                           var2.translate(var34 + 8, var47 + 8.0F, -0.5F);
                           var2.scale(16.0F, -16.0F, 1.0E-4F);
                           Render3DUtil.a_void_PoseStack_ItemStack_ItemOwner(var2, var33, var4);
                           var2.popPose();
                        }
                     }

                     for (int var48 = 0; var48 < 6; var48++) {
                        ItemStack var49 = var25[var48];
                        if (var49 != null && !var49.isEmpty()) {
                           int var50 = -54 + var48 * 18 + 1;
                           if (this.durability.getValue() && var49.isDamageableItem()) {
                              float var35 = (float)(var49.getMaxDamage() - var49.getDamageValue()) / var49.getMaxDamage();
                              float var36 = 1.0F - var35;
                              String var37 = Math.round((var49.getMaxDamage() - var49.getDamageValue()) * 100.0F / var49.getMaxDamage()) + "%";
                              float var38 = Night.fontManager.getStringWidth(var37) * 0.5F;
                              var2.pushPose();
                              var2.translate(var50 + 8 - var38 / 2.0F, var47 - 5.5F, 1.0F);
                              var2.scale(0.5F, 0.5F, 1.0F);
                              this.a(var2, var37, 0.0F, 0.0F, new Color(var36, var35, 0.0F));
                              var2.popPose();
                           }

                           if (var49.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                              var2.pushPose();
                              var2.translate(var50 + 1, var47 + 1.0F, 1.0F);
                              var2.scale(0.5F, 0.5F, 1.0F);
                              this.a(var2, "God", 0.0F, 0.0F, new Color(255, 125, 255));
                              var2.popPose();
                           }

                           if (var49.getCount() != 1) {
                              String var51 = String.valueOf(var49.getCount());
                              float var53 = Night.fontManager.getStringWidth(var51);
                              var2.pushPose();
                              var2.translate(var50 + 16 - var53, var47 + 7.0F, 1.0F);
                              var2.scale(1.0F, 1.0F, 1.0F);
                              this.a(var2, var51, 0.0F, 0.0F, Color.WHITE);
                              var2.popPose();
                           }

                           if (this.enchantments.getValue() && var26[var48] != null && !var26[var48].isEmpty()) {
                              List var52 = var26[var48];
                              float var54 = 4.2F;

                              for (int var55 = 0; var55 < var52.size(); var55++) {
                                 String var56 = (String)var52.get(var55);
                                 float var39 = var47 + 8.0F + var55 * var54;
                                 var2.pushPose();
                                 var2.translate(var50 + 1, var39, 1.0F);
                                 var2.scale(0.5F, 0.5F, 1.0F);
                                 this.a(var2, var56, 0.0F, 0.0F, Color.WHITE);
                                 var2.popPose();
                              }
                           }
                        }
                     }

                     if (this.durability.getValue()) {
                        var24 = var47 - 6.0F;
                     }
                  }
               }

               if (this.itemName.getValue() && !var4.getMainHandItem().isEmpty()) {
                  String var42 = var4.getMainHandItem().getHoverName().getString();
                  float var43 = Night.fontManager.getStringWidth(var42) * 0.5F;
                  float var44 = var24 - var18 * 0.5F - 2.0F;
                  var2.pushPose();
                  var2.translate(-var43 / 2.0F, var44, 1.0F);
                  var2.scale(0.5F, 0.5F, 1.0F);
                  this.a(var2, var42, 0.0F, 0.0F, Color.WHITE);
                  var2.popPose();
               }

               var2.popPose();
               Render3DUtil.a_void_List_List_bool(Render3DUtil.b, Render3DUtil.c, false);
               Render3DUtil.b.clear();
               Render3DUtil.c.clear();
            }
         }
      }
   }

   private void a(PoseStack var1, String var2, float var3, float var4, Color var5) {
      if (this.glow.getValue()) {
         Night.fontManager.drawPlainWorldText(var1, var2, var3, var4, var5, true);
      } else {
         Night.fontManager.drawWorldText(var1, var2, var3, var4, var5, false);
      }
   }

   private List<String> a_List_ItemStack(ItemStack var1) {
      if (var1 != null && !var1.isEmpty()) {
         ItemEnchantments var2 = (ItemEnchantments)var1.get(DataComponents.ENCHANTMENTS);
         return var2 != null && !var2.isEmpty() ? w.computeIfAbsent(var2, var0 -> {
            ArrayList var1x = new ArrayList(var0.size());

            for (Entry var3 : (Iterable<Entry>)(Object)var0.entrySet()) {
               String var4 = x.computeIfAbsent((Holder<Enchantment>)var3.getKey(), NameTags::a_String_Holder);
               int var5 = var3.getIntValue();
               var1x.add(var5 > 1 ? var4 + " " + var5 : var4);
            }

            return var1x;
         }) : Collections.emptyList();
      } else {
         return Collections.emptyList();
      }
   }

   private static String a_String_Holder(Holder<Enchantment> var0) {
      String var1 = var0.getRegisteredName();
      int var2 = var1.indexOf(58);
      String var3 = var2 != -1 ? var1.substring(var2 + 1) : var1;
      String var4;
      if (var3.startsWith("protection")) {
         var4 = "Pro";
      } else if (var3.startsWith("blast_protection")) {
         var4 = "Bla";
      } else if (var3.startsWith("fire_protection")) {
         var4 = "Fir";
      } else if (var3.startsWith("projectile_protection")) {
         var4 = "Proj";
      } else if (var3.startsWith("unbreaking")) {
         var4 = "Unb";
      } else if (var3.startsWith("mending")) {
         var4 = "Men";
      } else if (var3.startsWith("thorns")) {
         var4 = "Tho";
      } else if (var3.startsWith("respiration")) {
         var4 = "Res";
      } else if (var3.startsWith("aqua_affinity")) {
         var4 = "Aqu";
      } else if (var3.startsWith("depth_strider")) {
         var4 = "Dep";
      } else if (var3.startsWith("feather_falling")) {
         var4 = "Fea";
      } else if (var3.startsWith("soul_speed")) {
         var4 = "Soul";
      } else if (var3.startsWith("swift_sneak")) {
         var4 = "Sne";
      } else if (var3.startsWith("sharpness")) {
         var4 = "Sha";
      } else if (var3.startsWith("smite")) {
         var4 = "Smi";
      } else if (var3.startsWith("bane_of_arthropods")) {
         var4 = "Boa";
      } else if (var3.startsWith("knockback")) {
         var4 = "Kb";
      } else if (var3.startsWith("fire_aspect")) {
         var4 = "FA";
      } else if (var3.startsWith("looting")) {
         var4 = "Loot";
      } else if (var3.startsWith("sweeping_edge")) {
         var4 = "Swp";
      } else if (var3.startsWith("efficiency")) {
         var4 = "Eff";
      } else if (var3.startsWith("silk_touch")) {
         var4 = "Silk";
      } else if (var3.startsWith("fortune")) {
         var4 = "Fort";
      } else if (var3.startsWith("power")) {
         var4 = "Pow";
      } else if (var3.startsWith("punch")) {
         var4 = "Pun";
      } else if (var3.startsWith("flame")) {
         var4 = "Fla";
      } else if (var3.startsWith("infinity")) {
         var4 = "Inf";
      } else if (var3.startsWith("loyalty")) {
         var4 = "Loy";
      } else if (var3.startsWith("impaling")) {
         var4 = "Imp";
      } else if (var3.startsWith("riptide")) {
         var4 = "Rip";
      } else if (var3.startsWith("channeling")) {
         var4 = "Chan";
      } else if (var3.startsWith("multishot")) {
         var4 = "Multi";
      } else if (var3.startsWith("quick_charge")) {
         var4 = "Quick";
      } else if (var3.startsWith("piercing")) {
         var4 = "Pier";
      } else if (var3.startsWith("density")) {
         var4 = "Den";
      } else if (var3.startsWith("breach")) {
         var4 = "Bre";
      } else if (var3.startsWith("wind_burst")) {
         var4 = "Wind";
      } else if (var3.startsWith("vanishing_curse")) {
         var4 = "Van";
      } else if (var3.startsWith("binding_curse")) {
         var4 = "Bind";
      } else if (var3.length() > 3) {
         var4 = var3.substring(0, 3);
      } else {
         var4 = var3;
      }

      return var4.substring(0, 1).toUpperCase() + var4.substring(1);
   }

   private ItemStack a(Player var1, int var2) {
      return switch (var2) {
         case 0 -> var1.getMainHandItem();
         case 1 -> var1.getItemBySlot(EquipmentSlot.HEAD);
         case 2 -> var1.getItemBySlot(EquipmentSlot.CHEST);
         case 3 -> var1.getItemBySlot(EquipmentSlot.LEGS);
         case 4 -> var1.getItemBySlot(EquipmentSlot.FEET);
         case 5 -> var1.getOffhandItem();
         default -> ItemStack.EMPTY;
      };
   }
}
