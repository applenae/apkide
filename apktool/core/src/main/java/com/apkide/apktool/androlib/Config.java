/*
 *  Copyright (C) 2010 Ryszard Wiśniewski <brut.alll@gmail.com>
 *  Copyright (C) 2010 Connor Tumbleson <connor.tumbleson@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.apkide.apktool.androlib;

import static java.util.Objects.requireNonNull;

import com.apkide.apktool.androlib.exceptions.AndrolibException;
import com.apkide.common.Application;
import com.apkide.common.logger.Logger;

public class Config {
    private final static Logger LOGGER = Logger.getLogger(Config.class.getName());

    public final static short DECODE_SOURCES_NONE = 0x0000;
    public final static short DECODE_SOURCES_SMALI = 0x0001;
    public final static short DECODE_SOURCES_SMALI_ONLY_MAIN_CLASSES = 0x0010;

    public final static short DECODE_RESOURCES_NONE = 0x0100;
    public final static short DECODE_RESOURCES_FULL = 0x0101;

    public final static short FORCE_DECODE_MANIFEST_NONE = 0x0000;
    public final static short FORCE_DECODE_MANIFEST_FULL = 0x0001;

    public final static short DECODE_ASSETS_NONE = 0x0000;
    public final static short DECODE_ASSETS_FULL = 0x0001;

    // Build options
    public boolean forceBuildAll = false;
    public boolean forceDeleteFramework = false;
    public boolean debugMode = false;
    public boolean netSecConf = false;
    public boolean verbose = false;
    public boolean copyOriginalFiles = false;
    public boolean updateFiles = false;
    public boolean useAapt2 = true;//没有适配aapt v1 二进制文件
    public boolean noCrunch = false;
    public int forceApi = 0;

    // Decode options
    public short decodeSources = DECODE_SOURCES_SMALI;
    public short decodeResources = DECODE_RESOURCES_FULL;
    public short forceDecodeManifest = FORCE_DECODE_MANIFEST_NONE;
    public short decodeAssets = DECODE_ASSETS_FULL;
    public int apiLevel = 0;
    public boolean analysisMode = false;
    public boolean forceDelete = true;
    public boolean keepBrokenResources = false;
    public boolean baksmaliDebugMode = true;

    // Common options
    public String frameworkDirectory = null;
    public String frameworkTag = null;
    public String aaptPath = "";//不需要外部的 aapt
    public int aaptVersion = 2; // default to v2, 没有适配aapt v1 二进制文件

    // Utility functions
    public boolean isAapt2() {
        return this.useAapt2 || this.aaptVersion == 2;
    }

    private Config() {

    }

    private void setDefaultFrameworkDirectory() {
        frameworkDirectory = requireNonNull(
                Application.get().foundFile("android-framework.jar")
                        .getParentFile()).getAbsolutePath();
    }

    public void setDecodeSources(short mode) throws AndrolibException {
        if (mode != DECODE_SOURCES_NONE && mode != DECODE_SOURCES_SMALI && mode != DECODE_SOURCES_SMALI_ONLY_MAIN_CLASSES) {
            throw new AndrolibException("Invalid decode sources mode: " + mode);
        }
        if (decodeSources == DECODE_SOURCES_NONE && mode == DECODE_SOURCES_SMALI_ONLY_MAIN_CLASSES) {
            LOGGER.info("--only-main-classes cannot be paired with -s/--no-src. Ignoring.");
            return;
        }
        decodeSources = mode;
    }

    public void setDecodeResources(short mode) throws AndrolibException {
        if (mode != DECODE_RESOURCES_NONE && mode != DECODE_RESOURCES_FULL) {
            throw new AndrolibException("Invalid decode resources mode");
        }
        decodeResources = mode;
    }

    public void setForceDecodeManifest(short mode) throws AndrolibException {
        if (mode != FORCE_DECODE_MANIFEST_NONE && mode != FORCE_DECODE_MANIFEST_FULL) {
            throw new AndrolibException("Invalid force decode manifest mode");
        }
        forceDecodeManifest = mode;
    }

    public void setDecodeAssets(short mode) throws AndrolibException {
        if (mode != DECODE_ASSETS_NONE && mode != DECODE_ASSETS_FULL) {
            throw new AndrolibException("Invalid decode asset mode");
        }
        decodeAssets = mode;
    }

    public static Config getDefaultConfig() {
        Config config = new Config();
        config.setDefaultFrameworkDirectory();
        return config;
    }
}
