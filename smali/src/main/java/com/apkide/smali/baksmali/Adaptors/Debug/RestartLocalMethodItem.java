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

package com.apkide.smali.baksmali.Adaptors.Debug;

import androidx.annotation.NonNull;

import com.apkide.smali.baksmali.Adaptors.ClassDefinition;
import com.apkide.smali.baksmali.Adaptors.RegisterFormatter;
import com.apkide.smali.baksmali.formatter.BaksmaliWriter;
import com.apkide.smali.dexlib2.iface.debug.RestartLocal;

import java.io.IOException;

public class RestartLocalMethodItem extends DebugMethodItem {
    @NonNull
    private final ClassDefinition classDef;
    @NonNull private final RestartLocal restartLocal;
    @NonNull private final RegisterFormatter registerFormatter;

    public RestartLocalMethodItem(@NonNull ClassDefinition classDef, int codeAddress, int sortOrder,
                                  @NonNull RegisterFormatter registerFormatter, @NonNull RestartLocal restartLocal) {
        super(codeAddress, sortOrder);
        this.classDef = classDef;
        this.restartLocal = restartLocal;
        this.registerFormatter = registerFormatter;
    }

    @Override
    public boolean writeTo(BaksmaliWriter writer) throws IOException {
        writer.write(".restart local ");
        registerFormatter.writeTo(writer, restartLocal.getRegister());

        String name = restartLocal.getName();
        String type = restartLocal.getType();
        String signature = restartLocal.getSignature();
        if (name != null || type != null || signature != null) {
            writer.write("    # ");
            LocalFormatter.writeLocal(writer, name, type, signature);
        }
        return true;
    }
}
