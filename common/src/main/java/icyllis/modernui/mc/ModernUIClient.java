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

package icyllis.modernui.mc;

import icyllis.arc3d.engine.DriverBugWorkarounds;
import icyllis.modernui.ModernUI;
import icyllis.modernui.graphics.text.*;
import icyllis.modernui.text.Typeface;
import icyllis.modernui.view.WindowManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

public abstract class ModernUIClient extends ModernUI {

    private static volatile ModernUIClient sInstance;

    private static volatile boolean sBootstrap;

    private static Properties getBootstrapProperties() {
        if (!sBootstrap) {
            synchronized (ModernUIClient.class) {
                if (!sBootstrap) {
                    Path path = MuiPlatform.get().getBootstrapPath();
                    if (Files.exists(path)) {
                        try (var is = Files.newInputStream(path, StandardOpenOption.READ)) {
                            props.load(is);
                        } catch (IOException e) {
                            LOGGER.error(MARKER, "Failed to load bootstrap file", e);
                        }
                    } else {
                        try {
                            Files.createFile(path);
                        } catch (IOException e) {
                            LOGGER.error(MARKER, "Failed to create bootstrap file", e);
                        }
                    }
                    sBootstrap = true;
                }
            }
        }
        return props;
    }

    public static volatile boolean sInventoryPause;
    public static volatile boolean sRemoveTelemetrySession;
    public static volatile float sFontScale = 1;
    public static volatile boolean sUseColorEmoji;
    public static volatile boolean sEmojiShortcodes = true;
    public static volatile String sFirstFontFamily;
    public static volatile List<? extends String> sFallbackFontFamilyList;
    // font dir/paths to register
    public static volatile List<? extends String> sFontRegistrationList;

    protected volatile Typeface mTypeface;
    protected volatile FontFamily mFirstFontFamily;

    protected ModernUIClient() {
        super();
        sInstance = this;
    }

    public static ModernUIClient getInstance() {
        return sInstance;
    }

    public static boolean areShadersEnabled() {
        if (ModernUIMod.isOptiFineLoaded()) {
            if (OptiFineIntegration.isShaderPackLoaded()) {
                return true;
            }
        }
        if (ModernUIMod.isIrisApiLoaded()) {
            return IrisApiIntegration.isShaderPackInUse();
        }
        return false;
    }

    public static String getBootstrapProperty(String key) {
        Properties props = getBootstrapProperties();
        if (props != null) {
            return props.getProperty(key);
        }
        return null;
    }

    public static void setBootstrapProperty(String key, String value) {
        Properties props = getBootstrapProperties();
        if (props != null) {
            props.setProperty(key, value);
            try (var os = Files.newOutputStream(MuiPlatform.get().getBootstrapPath(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                props.store(os, "Modern UI bootstrap file");
            } catch (IOException e) {
                LOGGER.error(MARKER, "Failed to write bootstrap file", e);
            }
        }
    }

    @Nullable
    public static DriverBugWorkarounds getGpuDriverBugWorkarounds() {
        Properties props = getBootstrapProperties();
        if (props != null) {
            Map<String, Boolean> map = new HashMap<>();
            props.forEach((k, v) -> {
                if (k instanceof String key && v instanceof String value) {
                    Boolean state;
                    if ("true".equalsIgnoreCase(value) ||
                            "yes".equalsIgnoreCase(value) ||
                            "enable".equalsIgnoreCase(value)) {
                        state = Boolean.TRUE;
                    } else if ("false".equalsIgnoreCase(value) ||
                            "no".equalsIgnoreCase(value) ||
                            "disable".equalsIgnoreCase(value)) {
                        state = Boolean.FALSE;
                    } else {
                        return;
                    }
                    if (key.startsWith("arc3d_driverBugWorkarounds_")) { // <- length == 27
                        map.put(key.substring(27), state);
                    }
                }
            });
            if (!map.isEmpty()) {
                return new DriverBugWorkarounds(map);
            }
        }
        return null;
    }

    public static void loadFonts(String first,
                                 @Nonnull Collection<String> fallbacks,
                                 @Nonnull Set<FontFamily> selected,
                                 @Nonnull Consumer<FontFamily> firstSetter,
                                 boolean firstLoad) {
        if (firstLoad) {
            var fontManager = FontResourceManager.getInstance();
            var registrationList = sFontRegistrationList;
            if (registrationList != null) {
                for (var value : new LinkedHashSet<>(registrationList)) {
                    File file = new File(value.replaceAll("\\\\", "/"));
                    final File[] entries;
                    if (file.isDirectory()) {
                        entries = file.listFiles((dir, name) -> name.endsWith(".ttf") ||
                                name.endsWith(".otf") ||
                                name.endsWith(".ttc") ||
                                name.endsWith(".otc"));
                        if (entries == null) {
                            continue;
                        }
                    } else {
                        entries = new File[]{file};
                    }
                    for (File entry : entries) {
                        try {
                            FontFamily[] families = FontFamily.createFamilies(entry, /*register*/true);
                            for (var f : families) {
                                fontManager.onFontRegistered(f);
                                LOGGER.info(MARKER, "Registered font '{}', path '{}'",
                                        f.getFamilyName(), entry);
                            }
                        } catch (Exception e) {
                            LOGGER.error(MARKER, "Failed to register font '{}'",
                                    entry, e);
                        }
                    }
                }
            }
            var directory = Minecraft.getInstance().getResourcePackDirectory();
            try (var paths = Files.newDirectoryStream(directory)) {
                for (var p : paths) {
                    String name = p.getFileName().toString();
                    if (name.endsWith(".ttf") ||
                            name.endsWith(".otf") ||
                            name.endsWith(".ttc") ||
                            name.endsWith(".otc")) {
                        p = p.toAbsolutePath();
                        try {
                            FontFamily[] families = FontFamily.createFamilies(p.toFile(), /*register*/true);
                            for (var f : families) {
                                fontManager.onFontRegistered(f);
                                LOGGER.info(MARKER, "Registered font '{}', path '{}'",
                                        f.getFamilyName(), p);
                            }
                        } catch (Exception e) {
                            LOGGER.error(MARKER, "Failed to register font '{}'",
                                    p, e);
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error(MARKER, "Failed to open resource pack directory", e);
            }
            var resources = Minecraft.getInstance().getResourceManager();
            for (var entry : resources.listResourceStacks("font",
                    res -> {
                        if (res.getNamespace().equals(ModernUI.ID)) {
                            String p = res.getPath();
                            return p.endsWith(".ttf") ||
                                    p.endsWith(".otf") ||
                                    p.endsWith(".ttc") ||
                                    p.endsWith(".otc");
                        }
                        return false;
                    }).entrySet()) {
                for (var resource : entry.getValue()) {
                    try (var inputStream = resource.open()) {
                        FontFamily[] families = FontFamily.createFamilies(inputStream, /*register*/true);
                        for (var f : families) {
                            fontManager.onFontRegistered(f);
                            LOGGER.info(MARKER, "Registered font '{}', location '{}' in pack: '{}'",
                                    f.getFamilyName(), entry.getKey(), resource.sourcePackId());
                        }
                    } catch (Exception e) {
                        LOGGER.error(MARKER, "Failed to register font '{}' in pack: '{}'",
                                entry.getKey(), resource.sourcePackId(), e);
                    }
                }
            }
        }
        boolean hasFail = loadSingleFont(first, selected, firstSetter);
        for (String fallback : fallbacks) {
            hasFail |= loadSingleFont(fallback, selected, null);
        }
        if (hasFail && ModernUIMod.isDeveloperMode()) {
            LOGGER.debug(MARKER, "Available system font families:\n{}",
                    String.join("\n", FontFamily.getSystemFontMap().keySet()));
        }
    }

    private static boolean loadSingleFont(String value,
                                          @Nonnull Set<FontFamily> selected,
                                          @Nullable Consumer<FontFamily> firstSetter) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        try {
            File f = new File(value.replaceAll("\\\\", "/"));
            FontFamily family = FontFamily.createFamily(f, /*register*/false);
            selected.add(family);
            LOGGER.debug(MARKER, "Font '{}' was loaded with config value '{}' as LOCAL FILE",
                    family.getFamilyName(), value);
            if (firstSetter != null) {
                firstSetter.accept(family);
            }
            return true;
        } catch (Exception ignored) {
        }
        FontFamily family = FontFamily.getSystemFontWithAlias(value);
        if (family == null) {
            Optional<FontFamily> optional = FontFamily.getSystemFontMap().values().stream()
                    .filter(f -> f.getFamilyName().equalsIgnoreCase(value))
                    .findFirst();
            if (optional.isPresent()) {
                family = optional.get();
            }
        }
        if (family != null) {
            selected.add(family);
            LOGGER.debug(MARKER, "Font '{}' was loaded with config value '{}' as SYSTEM FONT",
                    family.getFamilyName(), value);
            if (firstSetter != null) {
                firstSetter.accept(family);
            }
            return true;
        }
        LOGGER.info(MARKER, "Font '{}' failed to load or invalid", value);
        return false;
    }

    @Nonnull
    @Override
    protected Typeface onGetSelectedTypeface() {
        if (mTypeface != null) {
            return mTypeface;
        }
        synchronized (this) {
            // should be a worker thread
            if (mTypeface == null) {
                checkFirstLoadTypeface();
                mTypeface = loadTypefaceInternal(this::setFirstFontFamily, true);
                // do some warm-up, but do not block ourselves
                var paint = new FontPaint();
                paint.setFont(mTypeface);
                paint.setLocale(Locale.ROOT);
                paint.setFontSize(12);
                Minecraft.getInstance().tell(() -> LayoutCache.getOrCreate(new char[]{'M'},
                        0, 1, 0, 1, false, paint, 0));
                LOGGER.info(MARKER, "Loaded typeface: {}", mTypeface);
            }
        }
        return mTypeface;
    }

    protected abstract void checkFirstLoadTypeface();

    // reload just Typeface on main thread, called after loaded
    public void reloadTypeface() {
        synchronized (this) {
            boolean firstLoad = mTypeface == null;
            mFirstFontFamily = null;
            mTypeface = loadTypefaceInternal(this::setFirstFontFamily, firstLoad);
            LOGGER.info(MARKER, "{} typeface: {}", firstLoad ? "Loaded" : "Reloaded", mTypeface);
        }
    }

    public void reloadFontStrike() {
        Minecraft.getInstance().submit(
                () -> FontResourceManager.getInstance().reloadAll());
    }

    @Nonnull
    private static Typeface loadTypefaceInternal(
            @Nonnull Consumer<FontFamily> firstSetter,
            boolean firstLoad) {
        Set<FontFamily> families = new LinkedHashSet<>();
        if (sUseColorEmoji) {
            var emojiFont = FontResourceManager.getInstance().getEmojiFont();
            if (emojiFont != null) {
                var colorEmojiFamily = new FontFamily(
                        emojiFont
                );
                families.add(colorEmojiFamily);
            }
        }
        String first = sFirstFontFamily;
        List<? extends String> configs = sFallbackFontFamilyList;
        if (first != null || configs != null) {
            var fallbacks = new LinkedHashSet<String>();
            if (configs != null) {
                fallbacks.addAll(configs);
            }
            if (first != null) {
                fallbacks.remove(first);
            }
            loadFonts(first, fallbacks, families,
                    firstSetter, firstLoad);
        }
        return Typeface.createTypeface(families.toArray(new FontFamily[0]));
    }

    @Nullable
    public FontFamily getFirstFontFamily() {
        return mFirstFontFamily;
    }

    protected void setFirstFontFamily(FontFamily firstFontFamily) {
        mFirstFontFamily = firstFontFamily;
    }

    @Nonnull
    @Override
    public InputStream getResourceStream(@Nonnull String namespace, @Nonnull String path) throws IOException {
        return Minecraft.getInstance().getResourceManager().open(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    @Nonnull
    @Override
    public ReadableByteChannel getResourceChannel(@Nonnull String namespace, @Nonnull String path) throws IOException {
        return Channels.newChannel(getResourceStream(namespace, path));
    }

    @Override
    public WindowManager getWindowManager() {
        return UIManager.getInstance().getDecorView();
    }
}
