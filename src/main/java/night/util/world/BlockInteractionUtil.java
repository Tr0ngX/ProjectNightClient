package night.util.world;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import night.Night;
import night.mixins.accessors.ClientPlayerEntityAccessor;
import night.module.impl.combat.CrystalRaytraceHelper;
import night.module.impl.core.RotationsModule;
import night.module.impl.player.SpeedMine;
import night.pingbypass.PingBypassState;
import night.pingbypass.protocol.PingBypassPayload;
import night.pingbypass.protocol.c2s.C2SMineBlockPacket;
import night.util.MinecraftInstance;
import night.util.network.PacketPredictionUtil;
import night.util.render.PlaceRenderEntry;
import night.util.rotation.RotationMathUtil;
import night.utils.minecraft.ImageShaderUtil;

public class BlockInteractionUtil implements MinecraftInstance {
   public static Set<Block> a = Sets.newHashSet(
      new Block[]{
         Blocks.CHEST,
         Blocks.TRAPPED_CHEST,
         Blocks.ENDER_CHEST,
         Blocks.SHULKER_BOX,
         Blocks.BARREL,
         Blocks.RESPAWN_ANCHOR,
         Blocks.CRAFTER,
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.WHITE),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.ORANGE),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.MAGENTA),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_BLUE),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.YELLOW),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIME),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.PINK),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.GRAY),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_GRAY),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.CYAN),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.PURPLE),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLUE),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BROWN),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.GREEN),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.RED),
         (Block)Blocks.DYED_SHULKER_BOX.pick(DyeColor.BLACK),
         Blocks.ANVIL,
         Blocks.CHIPPED_ANVIL,
         Blocks.DAMAGED_ANVIL,
         Blocks.BELL,
         Blocks.OAK_BUTTON,
         Blocks.ACACIA_BUTTON,
         Blocks.BIRCH_BUTTON,
         Blocks.DARK_OAK_BUTTON,
         Blocks.JUNGLE_BUTTON,
         Blocks.SPRUCE_BUTTON,
         Blocks.STONE_BUTTON,
         Blocks.COMPARATOR,
         Blocks.REPEATER,
         Blocks.OAK_FENCE_GATE,
         Blocks.SPRUCE_FENCE_GATE,
         Blocks.BIRCH_FENCE_GATE,
         Blocks.JUNGLE_FENCE_GATE,
         Blocks.DARK_OAK_FENCE_GATE,
         Blocks.ACACIA_FENCE_GATE,
         Blocks.BREWING_STAND,
         Blocks.DISPENSER,
         Blocks.DROPPER,
         Blocks.LEVER,
         Blocks.NOTE_BLOCK,
         Blocks.JUKEBOX,
         Blocks.BEACON,
         (Block)Blocks.BED.pick(DyeColor.BLACK),
         (Block)Blocks.BED.pick(DyeColor.BLUE),
         (Block)Blocks.BED.pick(DyeColor.BROWN),
         (Block)Blocks.BED.pick(DyeColor.CYAN),
         (Block)Blocks.BED.pick(DyeColor.GRAY),
         (Block)Blocks.BED.pick(DyeColor.GREEN),
         (Block)Blocks.BED.pick(DyeColor.LIGHT_BLUE),
         (Block)Blocks.BED.pick(DyeColor.LIGHT_GRAY),
         (Block)Blocks.BED.pick(DyeColor.LIME),
         (Block)Blocks.BED.pick(DyeColor.MAGENTA),
         (Block)Blocks.BED.pick(DyeColor.ORANGE),
         (Block)Blocks.BED.pick(DyeColor.PINK),
         (Block)Blocks.BED.pick(DyeColor.PURPLE),
         (Block)Blocks.BED.pick(DyeColor.RED),
         (Block)Blocks.BED.pick(DyeColor.WHITE),
         (Block)Blocks.BED.pick(DyeColor.YELLOW),
         Blocks.FURNACE,
         Blocks.OAK_DOOR,
         Blocks.SPRUCE_DOOR,
         Blocks.BIRCH_DOOR,
         Blocks.JUNGLE_DOOR,
         Blocks.ACACIA_DOOR,
         Blocks.DARK_OAK_DOOR,
         Blocks.CAKE,
         Blocks.ENCHANTING_TABLE,
         Blocks.DRAGON_EGG,
         Blocks.HOPPER,
         Blocks.REPEATING_COMMAND_BLOCK,
         Blocks.COMMAND_BLOCK,
         Blocks.CHAIN_COMMAND_BLOCK,
         Blocks.CRAFTING_TABLE,
         Blocks.ACACIA_TRAPDOOR,
         Blocks.BIRCH_TRAPDOOR,
         Blocks.DARK_OAK_TRAPDOOR,
         Blocks.JUNGLE_TRAPDOOR,
         Blocks.OAK_TRAPDOOR,
         Blocks.SPRUCE_TRAPDOOR,
         Blocks.CAKE,
         Blocks.ACACIA_SIGN,
         Blocks.ACACIA_WALL_SIGN,
         Blocks.BIRCH_SIGN,
         Blocks.BIRCH_WALL_SIGN,
         Blocks.DARK_OAK_SIGN,
         Blocks.DARK_OAK_WALL_SIGN,
         Blocks.JUNGLE_SIGN,
         Blocks.JUNGLE_WALL_SIGN,
         Blocks.OAK_SIGN,
         Blocks.OAK_WALL_SIGN,
         Blocks.SPRUCE_SIGN,
         Blocks.SPRUCE_WALL_SIGN,
         Blocks.CRIMSON_SIGN,
         Blocks.CRIMSON_WALL_SIGN,
         Blocks.WARPED_SIGN,
         Blocks.WARPED_WALL_SIGN,
         Blocks.BLAST_FURNACE,
         Blocks.SMOKER,
         Blocks.CARTOGRAPHY_TABLE,
         Blocks.GRINDSTONE,
         Blocks.LECTERN,
         Blocks.LOOM,
         Blocks.STONECUTTER,
         Blocks.SMITHING_TABLE
      }
   );
   private static ItemStack c;
   private static final Map<BlockPos, Long> d = new ConcurrentHashMap<>();
   private static final Map<BlockPos, BlockInteractionUtil.PlacedBlockTimestamp> e = new ConcurrentHashMap<>();
   public static volatile long b = 0L;
   private static final String[] f = new String[]{"South", "South West", "West", "North West", "North", "North East", "East", "South East"};
   private static final String[] g = new String[]{"+Z", "-X +Z", "-X", "-X -Z", "-Z", "+X -Z", "+X", "+X +Z"};

   public static boolean a_bool_BlockState(BlockState var0) {
      if (var0 != null && !var0.isAir()) {
         Block var1 = var0.getBlock();
         return a.contains(var1)
            ? true
            : var1 instanceof BaseEntityBlock
               || var1 instanceof ShulkerBoxBlock
               || var1 instanceof ChestBlock
               || var1 instanceof EnderChestBlock
               || var1 instanceof AnvilBlock
               || var1 instanceof CraftingTableBlock
               || var1 instanceof DoorBlock
               || var1 instanceof TrapDoorBlock
               || var1 instanceof FenceGateBlock
               || var1 instanceof ButtonBlock
               || var1 instanceof LeverBlock
               || var1 instanceof AbstractFurnaceBlock
               || var1 instanceof BedBlock
               || var1 instanceof SignBlock
               || var1 instanceof WallSignBlock
               || var1 instanceof CrafterBlock
               || var1 instanceof RespawnAnchorBlock
               || var1 instanceof BarrelBlock;
      } else {
         return false;
      }
   }

   public static boolean a(BlockPos var0, Direction var1, InteractionHand var2, boolean var3, boolean var4, boolean var5) {
      return a(var0, var1, var2, null, var3 ? "Normal" : "None", var4, var5);
   }

   public static boolean a(BlockPos var0, Direction var1, InteractionHand var2, String var3, boolean var4, boolean var5) {
      return a(var0, var1, var2, null, var3, var4, var5);
   }

   public static void a_void_BlockPos_Block(BlockPos var0, Block var1) {
      if (var0 != null && var1 != null) {
         long var2 = System.currentTimeMillis();
         if (e.size() > 256) {
            e.entrySet().removeIf(var2x -> var2 - var2x.getValue().timestamp() >= 1000L);
         }

         e.put(var0.immutable(), new BlockInteractionUtil.PlacedBlockTimestamp(var1, var2));
      }
   }

   public static void a_void_BlockPos(BlockPos var0) {
      if (var0 != null) {
         e.remove(var0);
      }
   }

   public static void a() {
      e.clear();
   }

   public static BlockState b_BlockState_BlockPos(BlockPos var0) {
      if (e.isEmpty()) {
         return null;
      } else {
         BlockInteractionUtil.PlacedBlockTimestamp var1 = e.get(var0);
         if (var1 == null) {
            return null;
         } else if (System.currentTimeMillis() - var1.timestamp() >= 1000L) {
            e.remove(var0);
            return null;
         } else {
            return var1.block().defaultBlockState();
         }
      }
   }

   public static void a_void_BlockPos_InteractionHand_bool_bool(BlockPos var0, InteractionHand var1, boolean var2, boolean var3) {
      a_void_BlockPos_InteractionHand_String_bool(var0, var1, var2 ? "Normal" : "None", var3);
   }

   public static void a_void_BlockPos_InteractionHand_String_bool(BlockPos var0, InteractionHand var1, String var2, boolean var3) {
      if (var0 != null && mc.player != null && mc.level != null && mc.getConnection() != null) {
         long var4 = System.currentTimeMillis();
         long var6 = Math.max(50L, Night.serverManager.getPing() * 2L);
         Long var8 = d.get(var0);
         if (var8 != null) {
            if (var4 - var8 < var6) {
               return;
            }

            d.remove(var0);
         }

         if (d.size() > 256) {
            d.entrySet().removeIf(var4x -> var4 - var4x.getValue() >= var6);
         }

         Vec3 var9 = Vec3.atCenterOf(var0).add(0.0, 0.5, 0.0);
         if (var2 != null && !var2.equalsIgnoreCase("None") && !var2.equalsIgnoreCase("false")) {
            float[] var10 = RotationMathUtil.a(var9);
            if (!Night.rotationManager.canRotate(var10[0], var10[1])) {
               return;
            }
         }

         d.put(var0, var4);
         if (var3) {
            PlaceRenderEntry var11 = new PlaceRenderEntry(var0);
            if (!Night.renderPlacementManager.placedBlocks.contains(var11)) {
               Night.renderPlacementManager.placedBlocks.add(var11);
            }
         }

         BlockHitResult var12 = new BlockHitResult(var9, Direction.UP, var0, false);
         PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemOnPacket(var1, var12, var2x));
         mc.getConnection().send(PacketAndRenderUtil.a_Packet_InteractionHand(var1));
      }
   }

   public static boolean a(BlockPos var0, Direction var1, InteractionHand var2, Runnable var3, String var4, boolean var5, boolean var6) {
      if (var0 != null && var1 != null) {
         BlockPos var7 = var0.relative(var1);
         if (mc.level != null && mc.player != null) {
            if (a_bool_BlockState(mc.level.getBlockState(var7)) || a_bool_BlockState(mc.level.getBlockState(var0))) {
               b = System.currentTimeMillis();
            }

            Direction var8 = var1.getOpposite();
            Vec3 var9 = c(var7, var8, mc.player.getEyePosition());
            float var10 = Night.rotationManager.getServerYaw();
            float var11 = Night.rotationManager.getServerPitch();
            float var12 = (float)RotationMathUtil.a_double_Entity_Vec3(mc.player, var9);
            float var13 = (float)RotationMathUtil.b_double_Entity_Vec3(mc.player, var9);
            boolean var14 = var4 != null && !var4.equalsIgnoreCase("None") && !var4.equalsIgnoreCase("false");
            if (var14 && !Night.rotationManager.canRotate(var12, var13, var7, var8)) {
               return false;
            }

            Night.worldStateManager.recordPlacedPosition(var0);
            if (var5) {
               Direction var15 = var1;
               Runnable var16 = () -> a(var0, var15, var2, var3, var4, var5, var6);
               if (a_bool_BlockPos_Vec3_Vec3_Runnable_String(var0, mc.player.getEyePosition(), var9, var16, var4)) {
                  return false;
               }
            }

            if (var3 != null) {
               var3.run();
            }

            boolean var21 = (a_bool_BlockState(mc.level.getBlockState(var7)) || a_bool_BlockState(mc.level.getBlockState(var0))) && !mc.player.isShiftKeyDown();
            if (var21) {
               Input var22 = mc.player.input != null ? mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
               Input var17 = new Input(var22.forward(), var22.backward(), var22.left(), var22.right(), var22.jump(), true, var22.sprint());
               mc.getConnection().send(new ServerboundPlayerInputPacket(var17));
               if (mc.player instanceof ClientPlayerEntityAccessor var19) {
                  var19.setLastSentInput(var17);
               }
            }

            BlockHitResult var23 = new BlockHitResult(var9, var8, var7, false);
            PacketPredictionUtil.a_void_SequencedPacketCreator(var2x -> new ServerboundUseItemOnPacket(var2, var23, var2x));
            mc.getConnection().send(PacketAndRenderUtil.a_Packet_InteractionHand(var2));
            if (var14 && !Night.rotationManager.isBatching() && Night.moduleManager.getModule(RotationsModule.class).snapBack.getValue()) {
               Night.rotationManager.sendRotationPacket(var10, var11);
            }

            Night.worldStateManager.getSwitchTimer().reset();
            if (var21) {
               Input var24 = mc.player.input != null ? mc.player.input.keyPresses : new Input(false, false, false, false, false, false, false);
               mc.getConnection().send(new ServerboundPlayerInputPacket(var24));
               if (mc.player instanceof ClientPlayerEntityAccessor var28) {
                  var28.setLastSentInput(var24);
               }
            }

            if (var6) {
               PlaceRenderEntry var25 = new PlaceRenderEntry(var0);
               if (!Night.renderPlacementManager.placedBlocks.contains(var25)) {
                  Night.renderPlacementManager.placedBlocks.add(var25);
                  if (PingBypassState.d && Night.pingBypassConfig != null && Night.pingBypassConfig.isServerMode() && Night.pingBypassServer != null) {
                     ClientboundCustomPayloadPacket var29 = new ClientboundCustomPayloadPacket(PingBypassPayload.fromPacket(new C2SMineBlockPacket(var0)));

                     for (Connection var20 : (Iterable<Connection>)(Object)Night.pingBypassServer.getConnections()) {
                        if (var20.isConnected()) {
                           var20.send(var29);
                        }
                     }
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean c_bool_BlockPos(BlockPos var0) {
      return a_bool_BlockPos_bool(var0, false);
   }

   public static boolean a_bool_BlockPos_bool(BlockPos var0, boolean var1) {
      if (mc.level == null || mc.player == null) {
         return false;
      }

      if (mc.level.isOutsideBuildHeight(var0)) {
         return false;
      }

      if (!mc.level.getBlockState(var0).canBeReplaced()) {
         return false;
      }

      boolean var2 = var0.getY() < Math.floor(mc.player.getBoundingBox().minY + 0.01);
      boolean var3 = new AABB(var0).contains(mc.player.position().x, mc.player.getBoundingBox().minY + 0.1, mc.player.position().z);
      double var4 = !var2 && var0.getY() == (int)Math.floor(mc.player.getBoundingBox().minY) ? 0.05 : 0.25;
      boolean var6 = !var1 && !var2 && (var3 || !EntityUtil.a_bool_Entity(mc.player) && mc.player.getBoundingBox().deflate(var4).intersects(new AABB(var0)));
      return var6
         ? false
         : mc.level
            .getEntities((Entity)null, new AABB(var0), var0x -> true)
            .stream()
            .noneMatch(
               var3x -> !(var3x instanceof EndCrystal)
                  && !(var3x instanceof ExperienceOrb)
                  && !(var3x instanceof ItemEntity)
                  && !(var3x instanceof AbstractArrow)
                  && (!var3x.equals(mc.player) || !var1 && !var2 && var6)
                  && !EntityUtil.c_bool_Entity(var3x)
            );
   }

   public static boolean d_bool_BlockPos(BlockPos var0) {
      if (mc.level == null) {
         return false;
      } else {
         return !mc.level.getBlockState(var0).canBeReplaced()
            ? false
            : mc.level
               .getEntities((Entity)null, new AABB(var0), var0x -> true)
               .stream()
               .noneMatch(
                  var0x -> !(var0x instanceof EndCrystal)
                     && !(var0x instanceof ExperienceOrb)
                     && !(var0x instanceof ItemEntity)
                     && !(var0x instanceof AbstractArrow)
                     && !EntityUtil.c_bool_Entity(var0x)
               );
      }
   }

   public static boolean e_bool_BlockPos(BlockPos var0) {
      if (mc.level == null) {
         return false;
      }

      BlockState var1 = mc.level.getBlockState(var0);
      return !var1.isAir() && !var1.canBeReplaced() ? var1.getDestroySpeed(mc.level, var0) == 0.0F : false;
   }

   public static void a_void_BlockPos_Direction(BlockPos var0, Direction var1) {
      if (mc.level != null && mc.player != null) {
         mc.getConnection().send(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, var0, var1 != null ? var1 : Direction.UP));
         mc.getConnection().send(new ServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, var0, var1 != null ? var1 : Direction.UP));
         mc.getConnection().send(PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND));
      }
   }

   public static Direction b_Direction_BlockPos_bool(BlockPos var0, boolean var1) {
      return b_Direction_BlockPos_List_bool(var0, null, var1);
   }

   public static BlockPos a_BlockPos_BlockPos_List_bool_int(BlockPos var0, List<BlockPos> var1, boolean var2, int var3) {
      return a_BlockPos_BlockPos_List_bool_int_int(var0, var1, var2, var3, Integer.MAX_VALUE);
   }

   public static BlockPos a_BlockPos_BlockPos_List_bool_int_int(BlockPos var0, List<BlockPos> var1, boolean var2, int var3, int var4) {
      if (mc.level != null && mc.player != null && var0 != null) {
         for (int var5 = 1; var5 <= var3; var5++) {
            for (Direction var9 : Direction.values()) {
               if (var9 != Direction.UP || var0.getY() + var5 <= var4) {
                  BlockPos var10 = var0.relative(var9, var5);
                  if (var10.getY() <= var4
                     && mc.player.isWithinBlockInteractionRange(var10, 0.0)
                     && a_bool_BlockPos_bool(var10, false)
                     && c(var10, var1, var2, var4) != null) {
                     return var10;
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean a_bool_BlockPos_Direction_Vec3(BlockPos var0, Direction var1, Vec3 var2) {
      return switch (var1) {
         case UP -> var2.y > var0.getY() + 0.99;
         case DOWN -> var2.y < var0.getY() + 0.01;
         case EAST -> var2.x > var0.getX() + 0.99;
         case WEST -> var2.x < var0.getX() + 0.01;
         case SOUTH -> var2.z > var0.getZ() + 0.99;
         case NORTH -> var2.z < var0.getZ() + 0.01;
         default -> throw new MatchException(null, null);
      };
   }

   public static Vec3 a_Vec3_BlockPos_Direction_double_double(BlockPos var0, Direction var1, double var2, double var4) {
      double var6 = var0.getX();
      double var8 = var0.getY();
      double var10 = var0.getZ();

      return switch (var1) {
         case UP -> new Vec3(var6 + var2, var8 + 0.999, var10 + var4);
         case DOWN -> new Vec3(var6 + var2, var8 + 0.001, var10 + var4);
         case EAST -> new Vec3(var6 + 0.999, var8 + var2, var10 + var4);
         case WEST -> new Vec3(var6 + 0.001, var8 + var2, var10 + var4);
         case SOUTH -> new Vec3(var6 + var2, var8 + var4, var10 + 0.999);
         case NORTH -> new Vec3(var6 + var2, var8 + var4, var10 + 0.001);
         default -> throw new MatchException(null, null);
      };
   }

   public static Vec3 a_Vec3_BlockPos_Direction_Vec3_double_double(BlockPos var0, Direction var1, Vec3 var2, double var3, double var5) {
      Vec3 var7 = a_Vec3_BlockPos_Direction_double_double(var0, var1, var3, var5);
      BlockHitResult var8 = mc.level.clip(new ClipContext(var2, var7, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, mc.player));
      return var8.getType() == Type.BLOCK && var8 instanceof BlockHitResult var9 && var9.getBlockPos().equals(var0) && var9.getDirection() == var1
         ? var9.getLocation()
         : null;
   }

   public static Vec3 b_Vec3_BlockPos_Direction_Vec3(BlockPos var0, Direction var1, Vec3 var2) {
      if (mc.level != null && mc.player != null) {
         if (!a_bool_BlockPos_Direction_Vec3(var0, var1, var2)) {
            return null;
         }

         Vec3 var3 = a_Vec3_BlockPos_Direction_Vec3_double_double(var0, var1, var2, 0.5, 0.5);
         if (var3 != null) {
            return var3;
         }

         double[][] var4 = new double[][]{{0.0, 0.5}, {1.0, 0.5}, {0.5, 0.0}, {0.5, 1.0}, {0.0, 0.0}, {1.0, 1.0}, {0.0, 1.0}, {1.0, 0.0}};
         Vec3 var5 = null;
         double var6 = Double.MAX_VALUE;

         for (double[] var11 : var4) {
            for (int var12 = 1; var12 <= 8; var12++) {
               double var13 = 1.0 - 1.0 / (1 << var12);
               double var15 = 0.5 + (var11[0] - 0.5) * var13;
               double var17 = 0.5 + (var11[1] - 0.5) * var13;
               Vec3 var19 = a_Vec3_BlockPos_Direction_Vec3_double_double(var0, var1, var2, var15, var17);
               if (var19 != null) {
                  double var20 = var2.distanceToSqr(var19);
                  if (var20 < var6) {
                     var6 = var20;
                     var5 = var19;
                  }
                  break;
               }
            }
         }

         return var5;
      } else {
         return null;
      }
   }

   public static Vec3 c(BlockPos var0, Direction var1, Vec3 var2) {
      Vec3 var3 = b_Vec3_BlockPos_Direction_Vec3(var0, var1, var2);
      return var3 != null ? var3 : Vec3.atCenterOf(var0).add(var1.getStepX() * 0.45, var1.getStepY() * 0.45, var1.getStepZ() * 0.45);
   }

   public static List<Direction> a_List_BlockPos_List_bool(BlockPos var0, List<BlockPos> var1, boolean var2) {
      if (mc.level != null && mc.player != null) {
         Vec3 var3 = mc.player.getEyePosition();
         ArrayList var4 = new ArrayList();
         new ArrayList();

         for (Direction var8 : Direction.values()) {
            BlockPos var9 = var0.relative(var8);
            BlockState var10 = mc.level.getBlockState(var9);
            if ((!var10.canBeReplaced() || var1 != null && var1.contains(var9))
               && var10.getFluidState().isEmpty()
               && mc.player.isWithinBlockInteractionRange(var9, 0.0)) {
               Vec3 var11 = Vec3.atCenterOf(var0).relative(var8, 0.5);
               if (var2) {
                  Direction var12 = var8.getOpposite();
                  if (!a_bool_BlockPos_Direction_Vec3(var9, var12, var3)) {
                     continue;
                  }

                  Vec3 var13 = b_Vec3_BlockPos_Direction_Vec3(var9, var12, var3);
                  if (var13 == null) {
                     continue;
                  }

                  var11 = var13;
               }

               double var17 = var3.distanceToSqr(var11);
               if (a.contains(var10.getBlock())) {
                  var17 += 20.0;
               }

               var4.add(new BlockInteractionUtil.DirectionDistance(var8, var17));
            }
         }

         ((List<BlockInteractionUtil.DirectionDistance>)var4).sort(Comparator.comparingDouble(var0x -> var0x.distance));
         ArrayList var14 = new ArrayList();

         for (BlockInteractionUtil.DirectionDistance var16 : (Iterable<BlockInteractionUtil.DirectionDistance>)(Object)var4) {
            var14.add(var16.direction);
         }

         return var14;
      } else {
         return Collections.emptyList();
      }
   }

   public static Direction b_Direction_BlockPos_List_bool(BlockPos var0, List<BlockPos> var1, boolean var2) {
      return c(var0, var1, var2, Integer.MAX_VALUE);
   }

   public static List<Direction> b(BlockPos var0, List<BlockPos> var1, boolean var2, int var3) {
      List var4 = a_List_BlockPos_List_bool(var0, var1, var2);
      if (var3 == Integer.MAX_VALUE) {
         return var4;
      }

      ArrayList var5 = new ArrayList();

      for (Direction var7 : (Iterable<Direction>)(Object)var4) {
         if (var0.relative(var7).getY() <= var3) {
            var5.add(var7);
         }
      }

      return var5;
   }

   public static Direction c(BlockPos var0, List<BlockPos> var1, boolean var2, int var3) {
      List var4 = b(var0, var1, var2, var3);
      return var4.isEmpty() ? null : (Direction)var4.get(0);
   }

   public static boolean a_bool_BlockPos_Vec3_Vec3_Runnable_String(BlockPos var0, Vec3 var1, Vec3 var2, Runnable var3, String var4) {
      AABB var5 = var1 != null && var2 != null ? new AABB(var1, var2).inflate(0.5) : new AABB(var0);

      List var6;
      try {
         var6 = mc.level
            .getEntities(
               (Entity)null,
               var5,
               var3x -> var3x instanceof EndCrystal
                  && (var1 == null || var2 == null || new AABB(var0).intersects(var3x.getBoundingBox()) || var3x.getBoundingBox().clip(var1, var2).isPresent())
            );
      } catch (ConcurrentModificationException var11) {
         return false;
      }

      if (var6.isEmpty()) {
         return false;
      }

      boolean var7 = var4 != null && !var4.equalsIgnoreCase("None") && !var4.equalsIgnoreCase("false");

      for (Entity var9 : (Iterable<Entity>)(Object)var6) {
         if (var9.isAlive() && !var9.isRemoved() && !(mc.player.getEyePosition().distanceToSqr(var9.position()) > 36.0)) {
            if (var7) {
               float[] var10 = RotationMathUtil.a(var9.getBoundingBox().getCenter());
               if (!Night.rotationManager.canRotate(var10[0], var10[1])) {
                  continue;
               }
            }

            mc.player.connection.send(new ServerboundAttackPacket(var9.getId()));
            mc.player.connection.send(PacketAndRenderUtil.a_Packet_InteractionHand(InteractionHand.MAIN_HAND));
            if (var3 != null) {
               Night.worldStateManager.onEntityRemoved(var9.getId(), var3);
            }
         }
      }

      return true;
   }

   public static Vec3 b_Vec3_BlockPos_Direction(BlockPos var0, Direction var1) {
      return Vec3.atCenterOf(var0).add(var1.getStepX() / 2.0, var1.getStepY() / 2.0, var1.getStepZ() / 2.0);
   }

   public static Direction c(BlockPos var0, boolean var1) {
      if (var1) {
         if (mc.player.getY() >= var0.getY()) {
            return Direction.UP;
         }

         BlockHitResult var2 = mc.level
            .clip(
               new ClipContext(mc.player.getEyePosition(), Vec3.atCenterOf(var0), net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player)
            );
         return var2 != null && var2.getType() == Type.BLOCK && var2.getDirection() != null ? var2.getDirection() : h(var0);
      } else {
         return h(var0);
      }
   }

   private static Direction h(BlockPos var0) {
      Direction var1 = null;
      Vec3 var2 = null;

      for (Direction var6 : Direction.values()) {
         Vec3 var7 = b_Vec3_BlockPos_Direction(var0, var6);
         if (var1 == null) {
            var1 = var6;
            var2 = var7;
         } else if (mc.player.distanceToSqr(var7) < mc.player.distanceToSqr(var2)) {
            var1 = var6;
            var2 = var7;
         }
      }

      return var1;
   }

   public static HitResult a_HitResult_float_float_double_double_double(float var0, float var1, double var2, double var4, double var6) {
      Vec3 var8 = new Vec3(
         Mth.sin(-var0 * (float) (Math.PI / 180.0)) * Mth.cos(var1 * (float) (Math.PI / 180.0)),
         -Mth.sin(var1 * (float) (Math.PI / 180.0)),
         Mth.cos(-var0 * (float) (Math.PI / 180.0)) * Mth.cos(var1 * (float) (Math.PI / 180.0))
      );
      BlockHitResult var9 = mc.level
         .clip(
            new ClipContext(
               new Vec3(var2, var4, var6),
               new Vec3(var2 + var8.x * 5.0, var4 + var8.y * 5.0, var6 + var8.z * 5.0),
               net.minecraft.world.level.ClipContext.Block.OUTLINE,
               Fluid.NONE,
               mc.player
            )
         );
      Vec3 var10 = new Vec3(var2, var4 + mc.player.getEyeHeight(mc.player.getPose()), var6);
      double var11 = 25.0;
      if (var9 != null) {
         var11 = var9.getLocation().distanceToSqr(var10);
      }

      Vec3 var13 = var10.add(var8.x * 5.0, var8.y * 5.0, var8.z * 5.0);
      AABB var14 = new AABB(var2 - 0.3, var4, var6 - 0.3, var2 + 0.3, var4 + 1.8, var6 + 0.3).expandTowards(var8.scale(5.0)).inflate(1.0, 1.0, 1.0);
      EntityHitResult var15 = ProjectileUtil.getEntityHitResult(mc.player, var10, var13, var14, var0x -> !var0x.isSpectator() && var0x.isPickable(), var11);
      return (HitResult)(var15 != null && (var10.distanceToSqr(var15.getLocation()) < var11 || var9 == null) && var15.getEntity() instanceof LivingEntity
         ? var15
         : var9);
   }

   public static boolean a_bool_Entity(Entity var0) {
      if (var0 != null && mc.player != null && mc.level != null) {
         AABB var1 = var0.getBoundingBox();
         Vec3 var2 = new Vec3(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
         if (a_bool_Vec3_Vec3(var2, var1.getCenter())) {
            return true;
         } else if (a_bool_Vec3_Vec3(var2, new Vec3(var1.getCenter().x, var1.maxY - 0.1, var1.getCenter().z))) {
            return true;
         } else if (a_bool_Vec3_Vec3(var2, new Vec3(var1.getCenter().x, var1.minY + 0.1, var1.getCenter().z))) {
            return true;
         } else {
            double var3 = var1.minX + 0.1;
            double var5 = var1.maxX - 0.1;
            double var7 = var1.minY + 0.1;
            double var9 = var1.maxY - 0.1;
            double var11 = var1.minZ + 0.1;
            double var13 = var1.maxZ - 0.1;
            if (a_bool_Vec3_Vec3(var2, new Vec3(var3, var9, var11))) {
               return true;
            } else if (a_bool_Vec3_Vec3(var2, new Vec3(var5, var9, var11))) {
               return true;
            } else if (a_bool_Vec3_Vec3(var2, new Vec3(var3, var9, var13))) {
               return true;
            } else if (a_bool_Vec3_Vec3(var2, new Vec3(var5, var9, var13))) {
               return true;
            } else if (a_bool_Vec3_Vec3(var2, new Vec3(var3, var7, var11))) {
               return true;
            } else if (a_bool_Vec3_Vec3(var2, new Vec3(var5, var7, var11))) {
               return true;
            } else if (a_bool_Vec3_Vec3(var2, new Vec3(var3, var7, var13))) {
               return true;
            } else {
               return a_bool_Vec3_Vec3(var2, new Vec3(var5, var7, var13))
                  ? true
                  : f(BlockPos.containing(var1.getCenter().x, var1.minY - 0.5, var1.getCenter().z));
            }
         }
      } else {
         return false;
      }
   }

   public static boolean a_bool_Vec3_Vec3(Vec3 var0, Vec3 var1) {
      return mc.level.clip(new ClipContext(var0, var1, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, mc.player)).getType() == Type.MISS;
   }

   public static boolean a_bool_Vec3(Vec3 var0) {
      return a_bool_double_double_double(var0.x, var0.y, var0.z);
   }

   public static boolean a_bool_double_double_double(double var0, double var2, double var4) {
      return mc.level
            .clip(
               new ClipContext(
                  new Vec3(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ()),
                  new Vec3(var0, var2, var4),
                  net.minecraft.world.level.ClipContext.Block.COLLIDER,
                  Fluid.NONE,
                  mc.player
               )
            )
            .getType()
         == Type.MISS;
   }

   public static boolean f(BlockPos var0) {
      if (mc.player != null && mc.level != null) {
         Vec3 var1 = new Vec3(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
         BlockHitResult var2 = mc.level
            .clip(new ClipContext(var1, Vec3.atCenterOf(var0), net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, mc.player));
         if (var2.getType() != Type.MISS && !(var2 instanceof BlockHitResult var3 && var3.getBlockPos().equals(var0))) {
            Vec3 var5 = new Vec3(var0.getX() + 0.5, var0.getY() + 1.0, var0.getZ() + 0.5);
            BlockHitResult var4 = mc.level.clip(new ClipContext(var1, var5, net.minecraft.world.level.ClipContext.Block.COLLIDER, Fluid.NONE, mc.player));
            return var4.getType() != Type.MISS && (!(var4 instanceof BlockHitResult) || !var4.getBlockPos().equals(var0))
               ? CrystalRaytraceHelper.b(var0)
               : true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean a_bool_BlockPosArr(BlockPos... var0) {
      return Arrays.stream(var0).allMatch(var0x -> mc.level.getBlockState(var0x).getBlock().defaultDestroyTime() != -1.0F);
   }

   public static boolean b_bool_BlockPosArr(BlockPos... var0) {
      return Arrays.stream(var0).allMatch(var0x -> mc.level.getBlockState(var0x).canBeReplaced());
   }

   public static int a_int_int(int var0) {
      return mc.player.level().dimension() == Level.NETHER ? var0 * 8 : var0 / 8;
   }

   public static String a_String_float(float var0) {
      return f[c_int_float(var0)];
   }

   public static String b_String_float(float var0) {
      return g[c_int_float(var0)];
   }

   private static int c_int_float(float var0) {
      float var1 = var0 % 360.0F;
      if (var1 < 0.0F) {
         var1 += 360.0F;
      }

      return Math.round(var1 / 45.0F) % 8;
   }

   public static float a_float_Player_BlockState(Player var0, BlockState var1) {
      if (var0 == null) {
         return 0.0F;
      }

      if (c == null) {
         c = new ItemStack(Items.NETHERITE_PICKAXE);
      }

      float var2 = c.getItem().getDestroySpeed(c, var1) + 26.0F;
      return 1.0F / (var2 / var1.getBlock().defaultDestroyTime() / 30.0F) * 50.0F;
   }

   public static float a_float_BlockState_int(BlockState var0, int var1) {
      if (mc.player == null) {
         return 0.0F;
      }

      float var2 = mc.player.getInventory().getItem(var1).getItem().getDestroySpeed(mc.player.getInventory().getItem(var1), var0);
      if (var2 > 1.0F) {
         ItemStack var3 = mc.player.getInventory().getItem(var1);
         int var4 = EnchantmentHelper.getItemEnchantmentLevel(
            mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), var3
         );
         if (var4 > 0 && !var3.isEmpty()) {
            var2 += (float)(StrictMath.pow(var4, 2.0) + 1.0);
         }
      }

      if (mc.player.hasEffect(MobEffects.HASTE)) {
         var2 *= 1.0F + (mc.player.getEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
      }

      if (mc.player.hasEffect(MobEffects.MINING_FATIGUE)) {
         var2 *= (float)Math.pow(0.3F, mc.player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier() + 1);
      }

      SpeedMine var7 = Night.moduleManager != null ? Night.moduleManager.getModule(SpeedMine.class) : null;
      boolean var8 = var7 != null && var7.isEnabled() && var7.liquidCheck.getValue();
      boolean var5 = var7 != null && var7.isEnabled() && var7.airCheck.getValue();
      if (!var8 && mc.player.isEyeInFluid(FluidTags.WATER)) {
         var2 *= (float)mc.player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
      }

      if (!var5 && !mc.player.onGround()) {
         var2 /= 5.0F;
      }

      var2 = var2 < 0.0F ? 0.0F : var2;
      return var2
         / var0.getBlock().defaultDestroyTime()
         / (var0.requiresCorrectToolForDrops() && !mc.player.getInventory().getItem(var1).isCorrectToolForDrops(var0) ? 100 : 30);
   }

   public static boolean b_bool_BlockState(BlockState var0) {
      return var0.getBlock() != Blocks.COBWEB && var0.getBlock() != Blocks.BAMBOO_SAPLING && !var0.canBeReplaced();
   }

   public static boolean a_bool_BlockPos_BlockPos(BlockPos var0, BlockPos var1) {
      if (var0 == null && var1 == null) {
         return true;
      } else {
         return var0 != null && var1 != null ? var0.equals(var1) : false;
      }
   }

   public static String b() {
      return mc.player.level().dimension().identifier().toString().replace("minecraft:", "");
   }

   public static List<Player> g(BlockPos var0) {
      ArrayList var1 = new ArrayList();

      for (Player var3 : (Iterable<Player>)(Object)mc.level.players()) {
         if (var3 != null && !var3.isRemoved() && !EntityUtil.c_bool_Entity(var3) && var3.getBoundingBox().intersects(new AABB(var0))) {
            var1.add(var3);
         }
      }

      return var1;
   }

   public static double b_double_int(int var0) {
      ImageShaderUtil.a();
      return ImageShaderUtil.glGetShaderConstant(var0);
   }

   record DirectionDistance(Direction direction, double distance) {
   }

   record PlacedBlockTimestamp(Block block, long timestamp) {
   }
}
