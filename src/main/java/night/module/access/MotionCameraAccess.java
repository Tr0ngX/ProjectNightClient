package night.module.access;

public interface MotionCameraAccess {
   boolean isEnabled();

   boolean isSmoothPerspective();

   boolean shouldSmoothCamera();

   boolean shouldRenderThirdPerson();

   double getInterpolatedDistance();

   double getCameraX();

   double getCameraY();

   double getCameraZ();
}
