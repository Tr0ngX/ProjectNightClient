package night.module.access;

public interface SprintAccess {
   boolean isEnabled();

   boolean canSprint();

   boolean isGrimSprintActive();

   int getStrafeSide();

   float getSprintYaw();

   boolean isInstantMode();

   boolean isGrimMode();
}
