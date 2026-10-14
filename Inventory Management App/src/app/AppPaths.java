
package app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {

    private AppPaths() {
    }

    public static Path getAppDir() {

        // الأولوية للمسار الذي يحدده Launcher عند تشغيل JAR.
        String configured =
                System.getProperty("app.home");

        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured)
                    .toAbsolutePath()
                    .normalize();
        }

        try {
            Path location = Paths.get(
                    AppPaths.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).toAbsolutePath().normalize();

            // التشغيل من JAR.
            if (Files.isRegularFile(location)) {
                return location.getParent();
            }

            // التشغيل من IDE:
            // البحث عن مجلد المشروع الذي يحتوي src و data.
            Path current = location;

            while (current != null) {

                if (Files.isDirectory(current.resolve("src"))
                        && Files.isDirectory(
                        current.resolve("data"))) {
                    return current;
                }

                // بعض إعدادات IDE تضع الكلاسات داخل
                // out/production أو target/classes.
                if (Files.isDirectory(current.resolve("data"))
                        && Files.isDirectory(
                        current.resolve("src"))) {
                    return current;
                }

                current = current.getParent();
            }

            // مسار احتياطي للتشغيل من IDE.
            return Paths.get(
                    System.getProperty("user.dir")
            ).toAbsolutePath().normalize();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "تعذر تحديد مجلد التطبيق",
                    ex
            );
        }
    }

    public static Path resolve(String relativePath) {

        Path path = Paths.get(relativePath);

        if (path.isAbsolute()) {
            return path.normalize();
        }

        return getAppDir()
                .resolve(path)
                .normalize();
    }

    public static InputStream openResource(
            String resourcePath
    ) throws IOException {

        String name = resourcePath.startsWith("/")
                ? resourcePath.substring(1)
                : resourcePath;

        ClassLoader loader =
                Thread.currentThread()
                        .getContextClassLoader();

        InputStream stream = loader == null
                ? null
                : loader.getResourceAsStream(name);

        if (stream == null) {
            stream = AppPaths.class.getResourceAsStream(
                    "/" + name
            );
        }

        if (stream != null) {
            return stream;
        }

        Path external = getAppDir()
                .resolve("resources")
                .resolve(name)
                .normalize();

        if (Files.isRegularFile(external)) {
            return Files.newInputStream(external);
        }

        return null;
    }
}
