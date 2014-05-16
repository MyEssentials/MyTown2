package mytown.core.utils.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sun.org.glassfish.gmbal.Description;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Description("Prefix for admin commands: mytown.adm|Prefix for normal commands: mytown.cmd")
public @interface Permission {
	String value();
}