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

import com.apkide.smali.dexlib2.base.BaseExceptionHandler;
import com.apkide.smali.dexlib2.iface.ExceptionHandler;
import com.apkide.smali.util.ImmutableConverter;
import com.google.common.collect.ImmutableList;

public class ImmutableExceptionHandler extends BaseExceptionHandler implements ExceptionHandler {
    @Nullable protected final String exceptionType;
    protected final int handlerCodeAddress;

    public ImmutableExceptionHandler(@Nullable String exceptionType,
                                     int handlerCodeAddress) {
        this.exceptionType = exceptionType;
        this.handlerCodeAddress = handlerCodeAddress;
    }

    public static ImmutableExceptionHandler of(ExceptionHandler exceptionHandler) {
        if (exceptionHandler instanceof ImmutableExceptionHandler) {
            return (ImmutableExceptionHandler)exceptionHandler;
        }
        return new ImmutableExceptionHandler(
                exceptionHandler.getExceptionType(),
                exceptionHandler.getHandlerCodeAddress());
    }

    @Nullable @Override public String getExceptionType() { return exceptionType; }
    @Override public int getHandlerCodeAddress() { return handlerCodeAddress; }

    @NonNull
    public static ImmutableList<ImmutableExceptionHandler> immutableListOf(
            @Nullable Iterable<? extends ExceptionHandler> list) {
        return CONVERTER.toList(list);
    }

    private static final ImmutableConverter<ImmutableExceptionHandler, ExceptionHandler> CONVERTER =
            new ImmutableConverter<ImmutableExceptionHandler, ExceptionHandler>() {
                @Override
                protected boolean isImmutable(@NonNull ExceptionHandler item) {
                    return item instanceof ImmutableExceptionHandler;
                }

                @NonNull
                @Override
                protected ImmutableExceptionHandler makeImmutable(@NonNull ExceptionHandler item) {
                    return ImmutableExceptionHandler.of(item);
                }
            };
}
