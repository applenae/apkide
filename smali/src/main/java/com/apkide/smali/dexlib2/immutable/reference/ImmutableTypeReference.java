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

package com.apkide.smali.dexlib2.immutable.reference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkide.smali.dexlib2.base.reference.BaseTypeReference;
import com.apkide.smali.dexlib2.iface.reference.TypeReference;
import com.apkide.smali.util.ImmutableConverter;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class ImmutableTypeReference extends BaseTypeReference implements ImmutableReference {
    @NonNull
    protected final String type;

    public ImmutableTypeReference(String type) {
        this.type = type;
    }

    @NonNull
    public static ImmutableTypeReference of(@NonNull TypeReference typeReference) {
        if (typeReference instanceof ImmutableTypeReference) {
            return (ImmutableTypeReference)typeReference;
        }
        return new ImmutableTypeReference(typeReference.getType());
    }

    @NonNull @Override public String getType() { return type; }

    @NonNull
    public static ImmutableList<ImmutableTypeReference> immutableListOf(@Nullable List<? extends TypeReference> list) {
        return CONVERTER.toList(list);
    }

    private static final ImmutableConverter<ImmutableTypeReference, TypeReference> CONVERTER =
            new ImmutableConverter<ImmutableTypeReference, TypeReference>() {
                @Override
                protected boolean isImmutable(@NonNull TypeReference item) {
                    return item instanceof ImmutableTypeReference;
                }

                @NonNull
                @Override
                protected ImmutableTypeReference makeImmutable(@NonNull TypeReference item) {
                    return ImmutableTypeReference.of(item);
                }
            };
}
