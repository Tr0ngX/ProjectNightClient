package night.events;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import lombok.Generated;
import night.Night;

public class EventListener {
   private final Class<?> eventType;
   private Consumer<Object> consumer;
   private final int priority;

   public EventListener(Class<?> var1, Object var2, Method var3) {
      Class var4 = var3.getParameters()[0].getType();
      this.eventType = var4;
      int var5 = var3.getAnnotation(EventTarget.class).priority();
      this.priority = var5;

      try {
         Class var6 = var3.getParameters()[0].getType();
         MethodType var7 = MethodType.methodType(void.class, var6);
         Lookup var8 = MethodHandles.lookup();
         String var9 = var3.getName();
         MethodType var10 = var7;
         MethodHandle var11 = var8.findVirtual(var1, var9, var10);
         MethodType var12 = MethodType.methodType(Consumer.class, var1);
         Lookup var13 = MethodHandles.lookup();
         MethodType var14 = var12;
         MethodType var15 = MethodType.methodType(void.class, Object.class);
         MethodHandle var16 = var11;
         MethodType var17 = var7;
         this.consumer = (Consumer)LambdaMetafactory.metafactory(var13, "accept", var14, var15, var16, var17).getTarget().invoke((Object)var2);
      } catch (Throwable var22) {
         String var19 = var1.getName();
         String var20 = var3.getName();
         Night.LOGGER.error("Fast event dispatch failed for " + var19 + "." + var20 + ", falling back to reflection", var22);
         if (!var3.canAccess(var2)) {
            var3.setAccessible(true);
         }

         this.consumer = new EventListener.ReflectiveConsumer(var3, var2);
      }
   }

   public void invoke(Object var1) {
      this.consumer.accept(var1);
   }

   @Generated
   public Class<?> getEventType() {
      return this.eventType;
   }

   @Generated
   public int getPriority() {
      return this.priority;
   }

   static final class ReflectiveConsumer implements Consumer<Object> {
      private final Method a;
      private final Object b;

      ReflectiveConsumer(Method var1, Object var2) {
         this.a = var1;
         this.b = var2;
      }

      @Override
      public void accept(Object var1) {
         try {
            Method var2 = this.a;
            Object var3 = this.b;
            Object[] var4 = new Object[]{var1};
            var2.invoke(var3, var4);
         } catch (Exception var6) {
            Night.LOGGER.error("The Event System threw an exception!", var6);
         }
      }
   }
}
