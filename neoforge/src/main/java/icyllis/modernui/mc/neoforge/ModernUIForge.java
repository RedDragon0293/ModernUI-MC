/*
 * Modern UI.
 * Copyright (C) 2019-2024 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc.neoforge;

import com.mojang.blaze3d.systems.RenderSystem;
import icyllis.modernui.ModernUI;
import icyllis.modernui.mc.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;

import static icyllis.modernui.ModernUI.*;

/**
 * Mod class. INTERNAL.
 *
 * @author BloCamLimb
 */
@Mod(ModernUI.ID)
public final class ModernUIForge extends ModernUIMod {

    //public static final int BOOTSTRAP_ENABLE_DEBUG_INJECTORS = 0x4;

    //static volatile boolean sInterceptTipTheScales;

    //public static boolean sRemoveMessageSignature;
    //public static boolean sSecureProfilePublicKey;

    // mod-loading thread
    public ModernUIForge() {
        if (!FMLEnvironment.production) {
            ModernUIMod.sDevelopment = true;
            LOGGER.debug(MARKER, "Auto detected in FML development environment");
        } else if (ModernUI.class.getSigners() == null) {
            LOGGER.warn(MARKER, "Signature is missing");
        }

        // TipTheScales doesn't work with OptiFine
        if (ModList.get().isLoaded("tipthescales") && !ModernUIMod.sOptiFineLoaded) {
            //sInterceptTipTheScales = true;
            LOGGER.fatal(MARKER, "Detected TipTheScales without OptiFine");
            warnSetup("You should remove TipTheScales, Modern UI already includes its features, " +
                    "and Modern UI is also compatible with OptiFine");
        }
        if (ModList.get().isLoaded("reblured")) {
            LOGGER.fatal(MARKER, "Detected ReBlurred");
            warnSetup("You should remove ReBlurred, Modern UI already includes its features, " +
                    "and Modern UI has better performance than it");
        }
        sLegendaryTooltipsLoaded = ModList.get().isLoaded("legendarytooltips");

        Config.initCommonConfig(
                spec -> ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec,
                        ModernUI.NAME_CPT + "/common.toml")
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(
                (Consumer<ModConfigEvent>) event -> Config.reloadCommon(event.getConfig())
        );
        LocalStorage.init();

        if (FMLEnvironment.dist.isClient()) {
            Loader.init();
        }

        /*if ((getBootstrapLevel() & BOOTSTRAP_ENABLE_DEBUG_INJECTORS) != 0) {
            MinecraftForge.EVENT_BUS.register(EventHandler.ClientDebug.class);
            LOGGER.debug(MARKER, "Enable Modern UI debug injectors");
        }*/

        LOGGER.info(MARKER, "Initialized Modern UI");
    }

    public static void warnSetup(String key, Object... args) {
        ModLoader.get().addWarning(new ModLoadingWarning(null, ModLoadingStage.SIDED_SETUP, key, args));
    }

    private static class Loader {

        @SuppressWarnings("resource")
        public static void init() {
            new Client();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client extends ModernUIClient {

        static {
            assert FMLEnvironment.dist.isClient();
        }

        private Client() {
            super();
            Config.initClientConfig(
                    spec -> ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, spec,
                            ModernUI.NAME_CPT + "/client.toml")
            );
            Config.initTextConfig(
                    spec -> ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, spec,
                            ModernUI.NAME_CPT + "/text.toml")
            );
            FontResourceManager.getInstance();
            if (isTextEngineEnabled()) {
                ModernUIText.init();
                LOGGER.info(MARKER, "Initialized Modern UI text engine");
            }
            FMLJavaModLoadingContext.get().getModEventBus().addListener(
                    (Consumer<ModConfigEvent>) event -> Config.reloadAnyClient(event.getConfig())
            );
            if (ModernUIMod.sDevelopment) {
                FMLJavaModLoadingContext.get().getModEventBus().register(Registration.ModClientDev.class);
            }
            LOGGER.info(MARKER, "Initialized Modern UI client");
        }

        @Override
        protected void checkFirstLoadTypeface() {
            if (RenderSystem.isOnRenderThread() || Minecraft.getInstance().isSameThread()) {
                LOGGER.error(MARKER,
                        "Loading typeface on the render thread, but it should be on a worker thread.\n"
                                + "Don't report to Modern UI, but to other mods as displayed in stack trace.",
                        new Exception("Loading typeface at the wrong mod loading stage")
                                .fillInStackTrace());
            }
        }

        @SuppressWarnings("ConstantValue")
        @Nonnull
        @Override
        protected Locale onGetSelectedLocale() {
            // Minecraft can be null if we're running DataGen
            // LanguageManager can be null if this method is being called too early
            Minecraft minecraft;
            LanguageManager languageManager;
            if ((minecraft = Minecraft.getInstance()) != null &&
                    (languageManager = minecraft.getLanguageManager()) != null) {
                return languageManager.getJavaLocale();
            }
            return super.onGetSelectedLocale();
        }
    }
}
