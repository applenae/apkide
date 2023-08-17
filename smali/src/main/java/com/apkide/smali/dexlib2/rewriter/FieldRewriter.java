/*
 * Copyright 2014, Google LLC
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

package com.apkide.smali.dexlib2.rewriter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apkide.smali.dexlib2.HiddenApiRestriction;
import com.apkide.smali.dexlib2.base.reference.BaseFieldReference;
import com.apkide.smali.dexlib2.iface.Annotation;
import com.apkide.smali.dexlib2.iface.Field;
import com.apkide.smali.dexlib2.iface.value.EncodedValue;

import java.util.Set;

public class FieldRewriter implements Rewriter<Field> {
    @NonNull
    protected final Rewriters rewriters;

    public FieldRewriter(@NonNull Rewriters rewriters) {
        this.rewriters = rewriters;
    }

    @NonNull @Override public Field rewrite(@NonNull Field field) {
        return new RewrittenField(field);
    }

    protected class RewrittenField extends BaseFieldReference implements Field {
        @NonNull protected Field field;

        public RewrittenField(@NonNull Field field) {
            this.field = field;
        }

        @Override @NonNull public String getDefiningClass() {
            return rewriters.getFieldReferenceRewriter().rewrite(field).getDefiningClass();
        }

        @Override @NonNull public String getName() {
            return rewriters.getFieldReferenceRewriter().rewrite(field).getName();
        }

        @Override @NonNull public String getType() {
            return rewriters.getFieldReferenceRewriter().rewrite(field).getType();
        }

        public int getAccessFlags() {
            return field.getAccessFlags();
        }

        @Nullable public EncodedValue getInitialValue() {
            return RewriterUtils.rewriteNullable(rewriters.getEncodedValueRewriter(), field.getInitialValue());
        }

        @NonNull public Set<? extends Annotation> getAnnotations() {
            return RewriterUtils.rewriteSet(rewriters.getAnnotationRewriter(), field.getAnnotations());
        }

        @NonNull @Override public Set<HiddenApiRestriction> getHiddenApiRestrictions() {
            return field.getHiddenApiRestrictions();
        }
    }
}
