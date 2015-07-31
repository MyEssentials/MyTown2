package mytown.test;

import myessentials.MyEssentialsCore;
import mytown.MyTown;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 */
public class TestMain {

    public static String path = System.getProperty("user.dir") + "/src/test/resources/config";

    public static void main() {
        PrintStream ps = new PrintStream(new FileOutputStream(FileDescriptor.out));
        MyTown.instance = new MyTown();
        MyTown.instance.LOG = new SimpleLogger("MyTown2-Test", Level.INFO, true, true, false, false, "DD/MM/YY", null, PropertiesUtil.getProperties(), ps);

        ps = new PrintStream(new FileOutputStream(FileDescriptor.out));
        MyEssentialsCore.instance = new MyEssentialsCore();
        MyEssentialsCore.instance.LOG = new SimpleLogger("MyEssentials-Core-Test", Level.INFO, true, true, false, false, "DD/MM/YY", null, PropertiesUtil.getProperties(), ps);
    }
}
