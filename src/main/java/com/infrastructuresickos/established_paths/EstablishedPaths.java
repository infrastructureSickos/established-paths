package com.infrastructuresickos.established_paths;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EstablishedPaths.MOD_ID)
public class EstablishedPaths {
    public static final String MOD_ID = "established_paths";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public EstablishedPaths() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, EPConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new PathEvents());
        LOGGER.info("EstablishedPaths initialized");
    }
}
