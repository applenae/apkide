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

package com.apkide.smali.dexlib2.writer.util;

import androidx.annotation.NonNull;

import com.apkide.smali.dexlib2.base.value.BaseArrayEncodedValue;
import com.apkide.smali.dexlib2.base.value.BaseMethodHandleEncodedValue;
import com.apkide.smali.dexlib2.base.value.BaseMethodTypeEncodedValue;
import com.apkide.smali.dexlib2.iface.reference.CallSiteReference;
import com.apkide.smali.dexlib2.iface.reference.MethodHandleReference;
import com.apkide.smali.dexlib2.iface.reference.MethodProtoReference;
import com.apkide.smali.dexlib2.iface.value.ArrayEncodedValue;
import com.apkide.smali.dexlib2.iface.value.EncodedValue;
import com.apkide.smali.dexlib2.immutable.value.ImmutableStringEncodedValue;
import com.google.common.collect.Lists;

import java.util.List;

public class CallSiteUtil {
    public static ArrayEncodedValue getEncodedCallSite(CallSiteReference callSiteReference) {
        return new BaseArrayEncodedValue() {
            @NonNull
            @Override
            public List<? extends EncodedValue> getValue() {
                List<EncodedValue> encodedCallSite = Lists.newArrayList();

                encodedCallSite.add(new BaseMethodHandleEncodedValue() {
                    @NonNull
                    @Override
                    public MethodHandleReference getValue() {
                        return callSiteReference.getMethodHandle();
                    }
                });
                encodedCallSite.add(new ImmutableStringEncodedValue(callSiteReference.getMethodName()));
                encodedCallSite.add(new BaseMethodTypeEncodedValue() {
                    @NonNull
                    @Override
                    public MethodProtoReference getValue() {
                        return callSiteReference.getMethodProto();
                    }
                });
                encodedCallSite.addAll(callSiteReference.getExtraArguments());
                return encodedCallSite;
            }
        };
    }
}
