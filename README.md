#### This fork added support for 1.21 fabric. Download it on [Release](https://github.com/RedDragon0293/ModernUI-MC/releases). Requires Forge-Config-Api-Port 21.0.0 or above.
#### 这个分支添加了对1.21 fabric的支持。请前往[Release](https://github.com/RedDragon0293/ModernUI-MC/releases)页面下载。要求Forge-Config-Api-Port版本为21.0.0或更高。
我一直在等作者适配，结果等了实在太久忍无可忍，决定亲自出马
#### ~~别咕咕了 球球了~~

---
# Modern UI for Minecraft
[![CurseForge](http://cf.way2muchnoise.eu/full_352491_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/modern-ui)
[![CurseForge](http://cf.way2muchnoise.eu/versions/For%20Minecraft_352491_all.svg)](https://www.curseforge.com/minecraft/mc-mods/modern-ui)
### Description
Modern UI for Minecraft, is a Minecraft Mod that is based on [Modern UI Core Framework](https://github.com/BloCamLimb/ModernUI) and Modern UI Core Extensions.
It provides Modern UI bootstrap program in Minecraft environment and Modding API based on Forge/Fabric, 
to make powerful Graphical User Interface in Minecraft.

This Mod also includes a powerful text layout engine and text rendering system designed for Minecraft.
This engine provides appropriate methods for processing Unicode text and gives you more readable text in any scale, in 2D/3D. In details:
* Real-time preview and reload TrueType/OpenType fonts
* A better font fallback implementation
* Anti-aliasing text and FreeType font hinting
* Use improved SDF text rendering in 2D/3D (also use batch rendering)
* Compute exact font size in device space for native glyph rendering
* Use Google Noto Color Emoji and support all the Unicode 15.0 Emoji
* Configurable bidirectional text heuristic algorithm
* Configurable text shadow and raw font size
* Unicode line breaking and CSS line-break & word-break
* Fast, exact and asynchronous Unicode text layout computation
* Faster and more memory efficient rectangle packing algorithm for glyphs
* Use real grayscale texture (1 byte-per-pixel, whereas Minecraft is 4 bpp)
* Compatible with OptiFine, Sodium (Rubidium), Iris (Oculus) and many mods
* Compatible with Minecraft's JSON font definition (bitmap fonts, TTF fonts)

Additionally, this Mod provides many utilities which improve game performance and gaming experience. Currently, we have:
* Changing screen background color
* Gaussian blur to screen backdrop image
* Fade-in animation to screen background
* More window modes: Fullscreen (borderless), Maximized (borderless)
* Framerate limit and master volume fading on window inactive (out of focus) and minimized
* Pausing single-player game when Inventory is open
* Changing GUI scale option to Slider and providing hint text
* Playing a "Ding" sound when Minecraft loads and reaches the Main Menu
* Enabling smooth scrolling in Vanilla's Selection List and Forge's Scroll Panel
* Pressing "C" to Zoom that is the same as OptiFine
* Undo/Redo and Unicode word iterator for all text fields
* Playing local music, allowing to seek and view spectrum
* Support Discord/Slack/GitHub/IamCal/JoyPixels emoji shortcodes in Chatting
* A fancy tooltip style
  + Choose rounded border or normal border (with anti-aliasing)
  + Add title break and control title line spacing
  + Center the title line, support RTL layout direction
  + Exactly position the tooltip to pixel grid (smooth movement)
  + Change background color and border color (with gradient and animation)

Releases for Minecraft Mod are available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modern-ui) and
[Modrinth](https://modrinth.com/mod/modern-ui).  
For historical reasons, issues should go to Core Repo's [Issue Tracker](https://github.com/BloCamLimb/ModernUI/issues). 
If you have any questions, feel free to join our [Discord](https://discord.gg/kmyGKt2) server.
### License
* Modern UI for Minecraft
  - Copyright (C) 2019-2024 BloCamLimb et al.
  - [![License](https://img.shields.io/badge/License-LGPL--3.0--or--later-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.en.html)
* Additional Assets
  - [source-han-sans](https://github.com/adobe-fonts/source-han-sans) by Adobe, licensed under the OFL-1.1
  - [jetbrains-mono](https://www.jetbrains.com/lp/mono/) by JetBrains, licensed under the OFL-1.1

#### Gradle configuration
```
repositories {
    maven {
        name 'IzzelAliz Maven'
        url 'https://maven.izzel.io/releases/'
    }
}
```
##### ForgeGradle 5
```
configurations {
    library
    implementation.extendsFrom library
}
minecraft.runs.all {
    lazyToken('minecraft_classpath') {
        configurations.library.copyRecursive().resolve().collect { it.absolutePath }.join(File.pathSeparator)
    }
}
dependencies {
    // Modern UI core framework
    library "icyllis.modernui:ModernUI-Core:${modernui_version}"
    // Modern UI core extensions
    library "icyllis.modernui:ModernUI-Markdown:${modernui_version}"
    // Modern UI for Minecraft Forge
    implementation fg.deobf("icyllis.modernui:ModernUI-Forge:${minecraft_version}-${modernui_version}.+")
}
```
Add these if you have not [MixinGradle](https://github.com/SpongePowered/MixinGradle):
```
minecraft {
    runs {
        client {
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
        }
        server {
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
        }
        // apply to data if you have datagen
    }
}
```
You need to regenerate run configurations if you make any changes on this.
#### Building Modern UI for Minecraft
Modern UI for Minecraft requires the latest [Modern UI](https://github.com/BloCamLimb/ModernUI) codebase to build.
You should clone `ModernUI` into the same parent directory of `ModernUI-MC` and ensure they're up-to-date.
Checkout `ModernUI/master` branch or a stable branch.
### Screenshots (maybe outdated, try yourself)
![ModernUI-MC-Diagram.png](https://s2.loli.net/2024/03/30/kMTXKdpPLbmctJv.png)  
New Tooltip  
![new tooltip.png](https://s2.loli.net/2024/03/30/VhyoFPAD2Js1HWO.png)  
Center Screen  
![2024-03-30_16.17.11.png](https://s2.loli.net/2024/03/30/vLBTWNgqZXhE6Vi.png)
Markdown  
![markdown](https://cdn.modrinth.com/data/3sjzyvGR/images/989a77ba61c62ff580a30dcf158e391080b949bd.png)  
Texts  
![fast text](https://cdn.modrinth.com/data/3sjzyvGR/images/d27f5d77555fd3f45392f5b8eb28efcb80f0b677.png)
![new4](https://s2.loli.net/2022/03/06/TM5dVKnpqNvDiJH.png)  
Navigation  
![new5](https://s2.loli.net/2022/03/06/hwAoHTgZNWBvEdq.png)  
Graphics  
![new3.gif](https://i.loli.net/2021/09/27/yNsL98XtpKP7UVA.gif)  
Audio visualization  
![new2](https://i.loli.net/2021/09/24/TJjyzd6oOf5pPcq.png)  
Out-of-date widgets  
![a](https://i.loli.net/2020/05/15/fYAow29d4JtqaGu.png)
![b](https://i.loli.net/2020/04/10/LDBFc1qo5wtnS8u.png)
