package jp.matatabi.torismpshop;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.jetbrains.annotations.NotNull;

public class ToriSmpShopLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        // ここにライブラリを追加していく
        // 例: resolver.addDependency(new DefaultArtifact("com.google.code.gson:gson:2.10.1"));

        classpathBuilder.addLibrary(resolver);
    }
}