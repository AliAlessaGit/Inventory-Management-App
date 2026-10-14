import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Launcher {

    // الاحتفاظ بمحمّل المكتبات طوال فترة تشغيل التطبيق.
    private static URLClassLoader appClassLoader;

    private Launcher() {
    }

    public static void main(String[] args) {

        try {
            // موقع ملف JAR الذي شُغّل فعلياً.
            Path codeLocation = Paths.get(
                    Launcher.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).toAbsolutePath().normalize();

            boolean runningFromJar =
                    Files.isRegularFile(codeLocation)
                            && codeLocation.getFileName()
                            .toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".jar");

            // إذا كان تشغيل البرنامج من JAR:
            // مجلد التطبيق هو المجلد الذي يحتوي على JAR.
            Path appDir = runningFromJar
                    ? codeLocation.getParent()
                    : findAppDirectory(codeLocation);

            if (appDir == null) {
                appDir = Paths.get(
                        System.getProperty("user.dir")
                ).toAbsolutePath().normalize();
            }

            // مشاركة المسار مع بقية كود التطبيق.
            System.setProperty(
                    "app.home",
                    appDir.toString()
            );

            List<URL> urls = new ArrayList<>();

            // إضافة التطبيق نفسه أو مجلد الكلاسات.
            urls.add(codeLocation.toUri().toURL());

            // تحميل كل ملفات JAR الموجودة في lib،
            // بما فيها المجلدات الفرعية.
            Path libDir = appDir.resolve("lib");

            if (Files.isDirectory(libDir)) {

                try (Stream<Path> files =
                             Files.walk(libDir)) {

                    List<Path> jars = files
                            .filter(Files::isRegularFile)
                            .filter(path -> path.getFileName()
                                    .toString()
                                    .toLowerCase(Locale.ROOT)
                                    .endsWith(".jar"))
                            .sorted()
                            .collect(Collectors.toList());

                    for (Path jar : jars) {
                        urls.add(jar.toUri().toURL());
                    }
                }
            }

            // إضافة resources إلى مسارات البحث.
            Path resourcesDir =
                    appDir.resolve("resources");

            if (Files.isDirectory(resourcesDir)) {
                urls.add(resourcesDir.toUri().toURL());
            }

            // استخدام محمّل منفصل كي يستطيع تحميل
            // MainFrame ومكتباته من المسارات الجديدة.
            ClassLoader parent =
                    ClassLoader.getSystemClassLoader()
                            .getParent();

            appClassLoader = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    parent
            );

            Thread.currentThread()
                    .setContextClassLoader(appClassLoader);

            // لا تستخدم MainFrame مباشرة كنقطة تشغيل
            // في MANIFEST.MF؛ يتم تحميله هنا.
            Class<?> mainClass = Class.forName(
                    "MainFrame",
                    true,
                    appClassLoader
            );

            Method mainMethod = mainClass.getMethod(
                    "main",
                    String[].class
            );

            mainMethod.invoke(null, (Object) args);

        } catch (Throwable ex) {

            Throwable error = ex;

            if (ex instanceof InvocationTargetException
                    && ex.getCause() != null) {
                error = ex.getCause();
            }

            error.printStackTrace();

            try {
                JOptionPane.showMessageDialog(
                        null,
                        "تعذر تشغيل البرنامج أو تحميل المكتبات.\n"
                                + error,
                        "خطأ في تشغيل التطبيق",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (Throwable ignored) {
                // يظل الخطأ الأصلي مطبوعاً في الطرفية.
            }
        }
    }

    private static Path findAppDirectory(
            Path codeLocation
    ) {

        Path current = codeLocation;

        while (current != null) {

            if (Files.isDirectory(current.resolve("data"))
                    || Files.isDirectory(current.resolve("lib"))
                    || Files.isDirectory(
                    current.resolve("resources"))) {
                return current;
            }

            current = current.getParent();
        }

        return codeLocation;
    }
}
