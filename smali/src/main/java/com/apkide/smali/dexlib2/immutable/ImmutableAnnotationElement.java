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

package com.apkide.smali.dexlib2.immutable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkide.smali.dexlib2.base.BaseAnnotationElement;
import com.apkide.smali.dexlib2.iface.AnnotationElement;
import com.apkide.smali.dexlib2.iface.value.EncodedValue;
import com.apkide.smali.dexlib2.immutable.value.ImmutableEncodedValue;
import com.apkide.smali.dexlib2.immutable.value.ImmutableEncodedValueFactory;
import com.apkide.smali.util.ImmutableConverter;
import com.google.common.collect.ImmutableSet;

public class ImmutableAnnotationElement extends BaseAnnotationElement {
    @NonNull
    protected final String name;
    @NonNull protected final ImmutableEncodedValue value;

    public ImmutableAnnotationElement(@NonNull String name,
                                      @NonNull EncodedValue value) {
        this.name = name;
        this.value = ImmutableEncodedValueFactory.of(value);
    }

    public ImmutableAnnotationElement(@NonNull String name,
                                      @NonNull ImmutableEncodedValue value) {
        this.name = name;
        this.value = value;
    }

    public static ImmutableAnnotationElement of(AnnotationElement annotationElement) {
        if (annotationElement instanceof ImmutableAnnotationElement) {
            return (ImmutableAnnotationElement)annotationElement;
        }
        return new ImmutableAnnotationElement(
                annotationElement.getName(),
                annotationElement.getValue());
    }

    @NonNull @Override public String getName() { return name; }
    @NonNull @Override public EncodedValue getValue() { return value; }

    @NonNull
    public static ImmutableSet<ImmutableAnnotationElement> immutableSetOf(
            @Nullable Iterable<? extends AnnotationElement> list) {
        return CONVERTER.toSet(list);
    }

    private static final ImmutableConverter<ImmutableAnnotationElement, AnnotationElement> CONVERTER =
            new ImmutableConverter<ImmutableAnnotationElement, AnnotationElement>() {
                @Override
                protected boolean isImmutable(@NonNull AnnotationElement item) {
                    return item instanceof ImmutableAnnotationElement;
                }

                @NonNull
                @Override
                protected ImmutableAnnotationElement makeImmutable(@NonNull AnnotationElement item) {
                    return ImmutableAnnotationElement.of(item);
                }
            };
}
