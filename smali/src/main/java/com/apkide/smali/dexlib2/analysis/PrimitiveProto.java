/*
 * Copyright 2013, Google LLC
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

package com.apkide.smali.dexlib2.analysis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkide.smali.dexlib2.iface.Method;
import com.apkide.smali.dexlib2.iface.reference.FieldReference;
import com.apkide.smali.dexlib2.iface.reference.MethodReference;
import com.apkide.smali.util.ExceptionWithContext;

public class PrimitiveProto implements TypeProto {
    protected final ClassPath classPath;
    protected final String type;

    public PrimitiveProto(@NonNull ClassPath classPath, @NonNull String type) {
        this.classPath = classPath;
        this.type = type;
    }

    @Override public String toString() { return type; }
    @NonNull @Override public ClassPath getClassPath() { return classPath; }
    @NonNull @Override public String getType() { return type; }
    @Override public boolean isInterface() { return false; }
    @Override public boolean implementsInterface(@NonNull String iface) { return false; }
    @Nullable @Override public String getSuperclass() { return null; }
    @NonNull @Override public TypeProto getCommonSuperclass(@NonNull TypeProto other) {
        throw new ExceptionWithContext("Cannot call getCommonSuperclass on PrimitiveProto");
    }

    @Override
    @Nullable
    public FieldReference getFieldByOffset(int fieldOffset) {
        return null;
    }

    @Override
    @Nullable
    public Method getMethodByVtableIndex(int vtableIndex) {
        return null;
    }

    @Override public int findMethodIndexInVtable(@NonNull MethodReference method) {
        return -1;
    }
}
