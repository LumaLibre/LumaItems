package dev.lumas.lumaitems.configuration;

import com.google.common.base.Preconditions;
import dev.lumas.lumaitems.LumaItems;
import dev.lumas.lumaitems.configuration.serdes.AstralSetClassTransformer;
import dev.lumas.lumaitems.configuration.serdes.EnchantmentTransformer;
import dev.lumas.lumaitems.configuration.serdes.LocationTransformer;
import dev.lumas.lumaitems.configuration.serdes.PairedEnchantmentTransformer;
import dev.lumas.lumaitems.registry.Registry;
import dev.lumas.lumaitems.registry.RegistryCrafter;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.nio.file.Path;

public class ConfigManager implements RegistryCrafter.Extension<OkaeriConfig> {

    public static final Path DATA_FOLDER = LumaItems.getInstance().getDataPath();

    @Override
    public <T extends OkaeriConfig> T craft(Class<?> clazz) {
        File annotation = clazz.getAnnotation(File.class);
        if (annotation == null) {
            throw new IllegalStateException("OkaeriFile must be annotated with @File");
        }

        String fileName = annotation.value();
        Preconditions.checkNotNull(fileName, "Dynamic file name could not be resolved for " + clazz.getName());
        Path bindFile = DATA_FOLDER.resolve(fileName);


        return eu.okaeri.configs.ConfigManager.create((Class<T>) clazz, (it) -> {
            it.configure(configurer -> {
                configurer.configurer(new YamlSnakeYamlConfigurer(), new StandardSerdes());
                configurer.removeOrphans(true);
                configurer.bindFile(bindFile);
                configurer.serdes(serdes -> {
                    serdes.add(new LocationTransformer());
                    serdes.add(new EnchantmentTransformer());
                    serdes.add(new PairedEnchantmentTransformer());
                    serdes.add(new AstralSetClassTransformer());
                });
            });

            it.saveDefaults();
            it.load(true);
        });
    }


    public static <T extends OkaeriFile> T get(Class<T> clazz) {
        return Registry.CONFIGS.values().stream()
                .filter(it -> it.getClass().equals(clazz))
                .map(it -> (T) it)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No config found for class " + clazz.getName()));
    }

}
