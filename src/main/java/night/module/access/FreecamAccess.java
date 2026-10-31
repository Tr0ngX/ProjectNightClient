package night.module.access;

public interface FreecamAccess {
   boolean isEnabled();

   boolean isRotateEnabled();

   void changeLookDirection(double var1, double var3);

   float getCameraYaw();

   float getCameraPitch();

   double getCameraX();

   double getCameraY();

   double getCameraZ();
}
