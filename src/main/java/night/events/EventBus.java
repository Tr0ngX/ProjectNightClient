package night.events;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class EventBus {
   private final Map<Object, List<EventListener>> instanceListenerCache = new ConcurrentHashMap<>();
   private final Map<Class<?>, List<EventListener>> staticListenerCache = new ConcurrentHashMap<>();
   private final Map<Class<?>, List<EventListener>> subscribers = new ConcurrentHashMap<>();

   public void register(Object var1) {
      Class var2 = var1.getClass();

      for (EventListener var4 : (Iterable<EventListener>)(Object)this.findListeners(var2, var1)) {
         Map var5 = this.subscribers;
         Class var6 = var4.getEventType();
         List var7 = (List)((Map)var5).computeIfAbsent(var6, new Function<Class<?>, List<EventListener>>() {
            public List<EventListener> apply(Class<?> var1) {
               return new CopyOnWriteArrayList<>();
            }
         });
         EventListener var8 = var4;
         this.insertByPriority(var7, var8);
      }
   }

   public void unregister(Object var1) {
      Class var2 = var1.getClass();

      for (EventListener var4 : (Iterable<EventListener>)(Object)this.findListeners(var2, var1)) {
         Map var5 = this.subscribers;
         Class var6 = var4.getEventType();
         List var7 = (List)var5.get(var6);
         if (var7 != null) {
            List var8 = var7;
            EventListener var9 = var4;
            var8.remove(var9);
         }
      }
   }

   public void post(Event var1) {
      Map var2 = this.subscribers;
      Class var3 = var1.getClass();
      List var4 = (List)var2.get(var3);
      if (var4 != null) {
         Iterator var5 = var4.iterator();

         while (var5.hasNext()) {
            ((EventListener)var5.next()).invoke(var1);
         }
      }
   }

   private List<EventListener> findListeners(final Class<?> var1, final Object var2) {
      Function var3 = new Function<Object, List<EventListener>>() {
         public List<EventListener> apply(Object var1x) {
            CopyOnWriteArrayList var2x = new CopyOnWriteArrayList();
            EventBus var3x = EventBus.this;
            CopyOnWriteArrayList var4 = var2x;
            Class var5 = var1;
            Object var6 = var2;
            var3x.collectListeners(var4, var5, var6);
            return var2x;
         }
      };
      if (var2 == null) {
         Map var4 = this.staticListenerCache;
         Function var5 = var3;
         return (List<EventListener>)var4.computeIfAbsent(var1, var5);
      }

      Iterator var6 = this.instanceListenerCache.keySet().iterator();

      while (var6.hasNext()) {
         if (var6.next() == var2) {
            return this.instanceListenerCache.get(var2);
         }
      }

      List var7 = (List)var3.apply(var2);
      Map var8 = this.instanceListenerCache;
      List var9 = var7;
      var8.put(var2, var9);
      return var7;
   }

   void collectListeners(List<EventListener> var1, Class<?> var2, Object var3) {
      Method[] var4 = var2.getDeclaredMethods();
      int var5 = var4.length;
      int var6 = 0;

      while (true) {
         int var7 = var6;
         int var8 = var5;
         if (var7 >= var8) {
            if (var2.getSuperclass() != null) {
               Class var12 = var2.getSuperclass();
               this.collectListeners(var1, var12, var3);
            }

            return;
         }

         Method[] var9 = var4;
         int var10 = var6;
         Method var11 = var9[var10];
         if (this.isValidListenerMethod(var11)) {
            Method var13 = var11;
            var1.add(new EventListener(var2, var3, var13));
         }

         var6++;
      }
   }

   private boolean isValidListenerMethod(Method var1) {
      if (!var1.isAnnotationPresent(EventTarget.class)) {
         return false;
      }

      if (var1.getReturnType() != void.class) {
         return false;
      }

      if (var1.getParameterCount() != 1) {
         return false;
      }

      boolean var2;
      if (!var1.getParameters()[0].getType().isPrimitive()) {
         var2 = true;
      } else {
         var2 = false;
      }

      return var2;
   }

   private void insertByPriority(List<EventListener> var1, EventListener var2) {
      int var3 = 0;

      while (true) {
         int var4 = var3;
         int var5 = var1.size();
         if (var4 >= var5) {
            break;
         }

         int var6 = var2.getPriority();
         int var7 = var3;
         int var8 = ((EventListener)var1.get(var7)).getPriority();
         if (var6 > var8) {
            break;
         }

         var3++;
      }

      int var9 = var3;
      var1.add(var9, var2);
   }
}
