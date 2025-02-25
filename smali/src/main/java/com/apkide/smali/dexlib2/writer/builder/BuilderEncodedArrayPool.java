/*
 * Copyright 2018, Google LLC
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

package com.apkide.smali.dexlib2.writer.builder;

import androidx.annotation.NonNull;

import com.apkide.smali.dexlib2.iface.value.ArrayEncodedValue;
import com.apkide.smali.dexlib2.writer.EncodedArraySection;
import com.apkide.smali.dexlib2.writer.builder.BuilderEncodedValues.BuilderArrayEncodedValue;
import com.apkide.smali.dexlib2.writer.builder.BuilderEncodedValues.BuilderEncodedValue;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class BuilderEncodedArrayPool extends BaseBuilderPool implements
        EncodedArraySection<BuilderArrayEncodedValue, BuilderEncodedValue> {
    @NonNull
    private final ConcurrentMap<ArrayEncodedValue, BuilderArrayEncodedValue> internedItems =
            Maps.newConcurrentMap();

    public BuilderEncodedArrayPool(@NonNull DexBuilder dexBuilder) {
        super(dexBuilder);
    }

    @NonNull public BuilderArrayEncodedValue internArrayEncodedValue(@NonNull ArrayEncodedValue arrayEncodedValue) {
        BuilderArrayEncodedValue builderArrayEncodedValue = internedItems.get(arrayEncodedValue);
        if (builderArrayEncodedValue != null) {
            return builderArrayEncodedValue;
        }

        builderArrayEncodedValue = (BuilderArrayEncodedValue)dexBuilder.internEncodedValue(arrayEncodedValue);
        BuilderArrayEncodedValue previous = internedItems.putIfAbsent(
                builderArrayEncodedValue, builderArrayEncodedValue);
        return previous == null ? builderArrayEncodedValue : previous;
    }

    @Override
    public int getItemOffset(@NonNull BuilderArrayEncodedValue builderArrayEncodedValue) {
        return builderArrayEncodedValue.offset;
    }

    @NonNull
    @Override
    public Collection<? extends Map.Entry<? extends BuilderArrayEncodedValue, Integer>> getItems() {
        return new BuilderMapEntryCollection<BuilderArrayEncodedValue>(internedItems.values()) {
            @Override
            protected int getValue(@NonNull BuilderArrayEncodedValue builderArrayEncodedValue) {
                return builderArrayEncodedValue.offset;
            }

            @Override
            protected int setValue(@NonNull BuilderArrayEncodedValue key, int value) {
                int prev = key.offset;
                key.offset = value;
                return prev;
            }
        };
    }

    @Override
    public List<? extends BuilderEncodedValue> getEncodedValueList(BuilderArrayEncodedValue builderArrayEncodedValue) {
        return builderArrayEncodedValue.elements;
    }
}
