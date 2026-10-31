package night.manager;

import java.util.ArrayList;
import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.util.MinecraftInstance;
import night.util.world.BlockInteractionUtil;

public class WaypointManager implements MinecraftInstance {
   private final ArrayList<WaypointManager.Waypoint> waypoints = new ArrayList<>();

   public boolean hasWaypoint(String var1) {
      return this.waypoints.stream().anyMatch(var1x -> var1x.getName().equalsIgnoreCase(var1));
   }

   public void addWaypoint(String var1, Vec3 var2) {
      String var3 = BlockInteractionUtil.b();
      String var4 = Night.serverManager.getServerHost();
      this.addWaypoint(var1, var2, var3, var4);
   }

   public void addWaypoint(String var1, Vec3 var2, String var3, String var4) {
      if (!this.hasWaypoint(var1)) {
         this.waypoints.add(new WaypointManager.Waypoint(var1, var2, var3, var4));
      }
   }

   public void removeWaypoint(String var1) {
      this.waypoints.removeIf(var1x -> var1x.getName().equalsIgnoreCase(var1));
   }

   public void clearWaypoints() {
      this.waypoints.clear();
   }

   @Generated
   public ArrayList<WaypointManager.Waypoint> getWaypoints() {
      return this.waypoints;
   }

   public static class Waypoint {
      private final String name;
      private final Vec3 pos;
      private final String dimension;
      private final String server;

      public Waypoint(String var1, Vec3 var2, String var3, String var4) {
         this.name = var1;
         this.pos = var2;
         this.dimension = var3;
         this.server = var4;
      }

      @Generated
      public String getName() {
         return this.name;
      }

      @Generated
      public Vec3 getPos() {
         return this.pos;
      }

      @Generated
      public String getDimension() {
         return this.dimension;
      }

      @Generated
      public String getServer() {
         return this.server;
      }
   }
}
