package night.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {
   String name();

   String description() default "No description.";

   Module.Category category();

   boolean persistent() default false;

   boolean enabled() default false;

   boolean drawn() default true;

   int bind() default 0;

   boolean proxySupported() default false;
}
