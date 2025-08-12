package thederpgamer.lorefulloot.lua.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface LuaCallable {
}
