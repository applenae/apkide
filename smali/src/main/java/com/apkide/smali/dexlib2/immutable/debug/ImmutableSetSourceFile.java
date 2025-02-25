/*
 * Copyright 2012, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.apkide.smali.dexlib2.immutable.debug;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkide.smali.dexlib2.DebugItemType;
import com.apkide.smali.dexlib2.base.reference.BaseStringReference;
import com.apkide.smali.dexlib2.iface.debug.SetSourceFile;
import com.apkide.smali.dexlib2.iface.reference.StringReference;

public class ImmutableSetSourceFile extends ImmutableDebugItem implements SetSourceFile {
    @Nullable protected final String sourceFile;

    public ImmutableSetSourceFile(int codeAddress,
                                  @Nullable String sourceFile) {
        super(codeAddress);
        this.sourceFile = sourceFile;
    }

    @NonNull
    public static ImmutableSetSourceFile of (@NonNull SetSourceFile setSourceFile) {
        if (setSourceFile instanceof ImmutableSetSourceFile) {
            return (ImmutableSetSourceFile)setSourceFile;
        }
        return new ImmutableSetSourceFile(
                setSourceFile.getCodeAddress(),
                setSourceFile.getSourceFile());
    }

    @Nullable @Override public String getSourceFile() { return sourceFile; }

    @Nullable @Override public StringReference getSourceFileReference() {
        return sourceFile==null?null:new BaseStringReference() {
            @NonNull @Override public String getString() {
                return sourceFile;
            }
        };
    }


    @Override public int getDebugItemType() { return DebugItemType.SET_SOURCE_FILE; }
}
