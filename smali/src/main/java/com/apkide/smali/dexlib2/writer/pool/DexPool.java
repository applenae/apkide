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

package com.apkide.smali.dexlib2.writer.pool;

import androidx.annotation.NonNull;

import com.apkide.smali.dexlib2.Opcodes;
import com.apkide.smali.dexlib2.ValueType;
import com.apkide.smali.dexlib2.iface.Annotation;
import com.apkide.smali.dexlib2.iface.AnnotationElement;
import com.apkide.smali.dexlib2.iface.ClassDef;
import com.apkide.smali.dexlib2.iface.DexFile;
import com.apkide.smali.dexlib2.iface.Field;
import com.apkide.smali.dexlib2.iface.reference.CallSiteReference;
import com.apkide.smali.dexlib2.iface.reference.FieldReference;
import com.apkide.smali.dexlib2.iface.reference.MethodHandleReference;
import com.apkide.smali.dexlib2.iface.reference.MethodProtoReference;
import com.apkide.smali.dexlib2.iface.reference.MethodReference;
import com.apkide.smali.dexlib2.iface.reference.StringReference;
import com.apkide.smali.dexlib2.iface.reference.TypeReference;
import com.apkide.smali.dexlib2.iface.value.AnnotationEncodedValue;
import com.apkide.smali.dexlib2.iface.value.ArrayEncodedValue;
import com.apkide.smali.dexlib2.iface.value.BooleanEncodedValue;
import com.apkide.smali.dexlib2.iface.value.ByteEncodedValue;
import com.apkide.smali.dexlib2.iface.value.CharEncodedValue;
import com.apkide.smali.dexlib2.iface.value.DoubleEncodedValue;
import com.apkide.smali.dexlib2.iface.value.EncodedValue;
import com.apkide.smali.dexlib2.iface.value.EnumEncodedValue;
import com.apkide.smali.dexlib2.iface.value.FieldEncodedValue;
import com.apkide.smali.dexlib2.iface.value.FloatEncodedValue;
import com.apkide.smali.dexlib2.iface.value.IntEncodedValue;
import com.apkide.smali.dexlib2.iface.value.LongEncodedValue;
import com.apkide.smali.dexlib2.iface.value.MethodEncodedValue;
import com.apkide.smali.dexlib2.iface.value.MethodHandleEncodedValue;
import com.apkide.smali.dexlib2.iface.value.MethodTypeEncodedValue;
import com.apkide.smali.dexlib2.iface.value.ShortEncodedValue;
import com.apkide.smali.dexlib2.iface.value.StringEncodedValue;
import com.apkide.smali.dexlib2.iface.value.TypeEncodedValue;
import com.apkide.smali.dexlib2.writer.DexWriter;
import com.apkide.smali.dexlib2.writer.io.DexDataStore;
import com.apkide.smali.dexlib2.writer.io.FileDataStore;
import com.apkide.smali.util.ExceptionWithContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class DexPool extends DexWriter<CharSequence, StringReference, CharSequence, TypeReference,
        MethodProtoReference, FieldReference, MethodReference, PoolClassDef,
        CallSiteReference, MethodHandleReference, Annotation, Set<? extends Annotation>,
        TypeListPool.Key<? extends Collection<? extends CharSequence>>, Field, PoolMethod,
        ArrayEncodedValue, EncodedValue, AnnotationElement, StringPool, TypePool, ProtoPool, FieldPool, MethodPool,
        ClassPool, CallSitePool, MethodHandlePool, TypeListPool, AnnotationPool, AnnotationSetPool, EncodedArrayPool> {

    private final BasePool<?, ?>[] sections = new BasePool<?, ?>[] {
            stringSection,
            typeSection,
            protoSection,
            fieldSection,
            methodSection,
            classSection,
            callSiteSection,
            methodHandleSection,

            typeListSection,
            annotationSection,
            annotationSetSection,
            encodedArraySection,
    };

    public DexPool(Opcodes opcodes) {
        super(opcodes);
    }

    @NonNull
    @Override protected SectionProvider getSectionProvider() {
        return new DexPoolSectionProvider();
    }

    public static void writeTo(@NonNull DexDataStore dataStore, @NonNull DexFile input)
            throws IOException {
        DexPool dexPool = new DexPool(input.getOpcodes());
        for (ClassDef classDef: input.getClasses()) {
            dexPool.internClass(classDef);
        }
        dexPool.writeTo(dataStore);
    }

    public static void writeTo(@NonNull String path, @NonNull DexFile input) throws IOException {
        DexPool dexPool = new DexPool(input.getOpcodes());
        for (ClassDef classDef: input.getClasses()) {
            dexPool.internClass(classDef);
        }
        dexPool.writeTo(new FileDataStore(new File(path)));
    }

    /**
     * Interns a class into this DexPool
     * @param classDef The class to intern
     */
    public void internClass(ClassDef classDef) {
        classSection.intern(classDef);
    }

    /**
     * Creates a marked state that can be returned to by calling reset()
     *
     * This is useful to rollback the last added class if it causes a method/field/type overflow
     */
    public void mark() {
        for (Markable section: sections) {
            section.mark();
        }
    }

    /**
     * Resets to the last marked state
     *
     * This is useful to rollback the last added class if it causes a method/field/type overflow
     */
    public void reset() {
        for (Markable section: sections) {
            section.reset();
        }
    }

    @Override protected void writeEncodedValue(@NonNull InternalEncodedValueWriter writer,
                                               @NonNull EncodedValue encodedValue) throws IOException {
        switch (encodedValue.getValueType()) {
            case ValueType.ANNOTATION:
                AnnotationEncodedValue annotationEncodedValue = (AnnotationEncodedValue)encodedValue;
                writer.writeAnnotation(annotationEncodedValue.getType(), annotationEncodedValue.getElements());
                break;
            case ValueType.ARRAY:
                ArrayEncodedValue arrayEncodedValue = (ArrayEncodedValue)encodedValue;
                writer.writeArray(arrayEncodedValue.getValue());
                break;
            case ValueType.BOOLEAN:
                writer.writeBoolean(((BooleanEncodedValue)encodedValue).getValue());
                break;
            case ValueType.BYTE:
                writer.writeByte(((ByteEncodedValue)encodedValue).getValue());
                break;
            case ValueType.CHAR:
                writer.writeChar(((CharEncodedValue)encodedValue).getValue());
                break;
            case ValueType.DOUBLE:
                writer.writeDouble(((DoubleEncodedValue)encodedValue).getValue());
                break;
            case ValueType.ENUM:
                writer.writeEnum(((EnumEncodedValue)encodedValue).getValue());
                break;
            case ValueType.FIELD:
                writer.writeField(((FieldEncodedValue)encodedValue).getValue());
                break;
            case ValueType.FLOAT:
                writer.writeFloat(((FloatEncodedValue)encodedValue).getValue());
                break;
            case ValueType.INT:
                writer.writeInt(((IntEncodedValue)encodedValue).getValue());
                break;
            case ValueType.LONG:
                writer.writeLong(((LongEncodedValue)encodedValue).getValue());
                break;
            case ValueType.METHOD:
                writer.writeMethod(((MethodEncodedValue)encodedValue).getValue());
                break;
            case ValueType.NULL:
                writer.writeNull();
                break;
            case ValueType.SHORT:
                writer.writeShort(((ShortEncodedValue)encodedValue).getValue());
                break;
            case ValueType.STRING:
                writer.writeString(((StringEncodedValue)encodedValue).getValue());
                break;
            case ValueType.TYPE:
                writer.writeType(((TypeEncodedValue)encodedValue).getValue());
                break;
            case ValueType.METHOD_TYPE:
                writer.writeMethodType(((MethodTypeEncodedValue) encodedValue).getValue());
                break;
            case ValueType.METHOD_HANDLE:
                writer.writeMethodHandle(((MethodHandleEncodedValue) encodedValue).getValue());
                break;
            default:
                throw new ExceptionWithContext("Unrecognized value type: %d", encodedValue.getValueType());
        }
    }

    void internEncodedValue(@NonNull EncodedValue encodedValue) {
        switch (encodedValue.getValueType()) {
            case ValueType.ANNOTATION:
                AnnotationEncodedValue annotationEncodedValue = (AnnotationEncodedValue)encodedValue;
                typeSection.intern(annotationEncodedValue.getType());
                for (AnnotationElement element: annotationEncodedValue.getElements()) {
                    stringSection.intern(element.getName());
                    internEncodedValue(element.getValue());
                }
                break;
            case ValueType.ARRAY:
                for (EncodedValue element: ((ArrayEncodedValue)encodedValue).getValue()) {
                    internEncodedValue(element);
                }
                break;
            case ValueType.STRING:
                stringSection.intern(((StringEncodedValue)encodedValue).getValue());
                break;
            case ValueType.TYPE:
                typeSection.intern(((TypeEncodedValue)encodedValue).getValue());
                break;
            case ValueType.ENUM:
                fieldSection.intern(((EnumEncodedValue)encodedValue).getValue());
                break;
            case ValueType.FIELD:
                fieldSection.intern(((FieldEncodedValue)encodedValue).getValue());
                break;
            case ValueType.METHOD:
                methodSection.intern(((MethodEncodedValue)encodedValue).getValue());
                break;
            case ValueType.METHOD_HANDLE:
                methodHandleSection.intern(((MethodHandleEncodedValue)encodedValue).getValue());
                break;
            case ValueType.METHOD_TYPE:
                protoSection.intern(((MethodTypeEncodedValue)encodedValue).getValue());
                break;
        }
    }

    protected class DexPoolSectionProvider extends SectionProvider {
        @NonNull @Override public StringPool getStringSection() {
            return new StringPool(DexPool.this);
        }

        @NonNull @Override public TypePool getTypeSection() {
            return new TypePool(DexPool.this);
        }

        @NonNull @Override public ProtoPool getProtoSection() {
            return new ProtoPool(DexPool.this);
        }

        @NonNull @Override public FieldPool getFieldSection() {
            return new FieldPool(DexPool.this);
        }

        @NonNull @Override public MethodPool getMethodSection() {
            return new MethodPool(DexPool.this);
        }

        @NonNull @Override public ClassPool getClassSection() {
            return new ClassPool(DexPool.this);
        }

        @NonNull @Override public CallSitePool getCallSiteSection() {
            return new CallSitePool(DexPool.this);
        }

        @NonNull @Override public MethodHandlePool getMethodHandleSection() {
            return new MethodHandlePool(DexPool.this);
        }

        @NonNull @Override public TypeListPool getTypeListSection() {
            return new TypeListPool(DexPool.this);
        }

        @NonNull @Override public AnnotationPool getAnnotationSection() {
            return new AnnotationPool(DexPool.this);
        }

        @NonNull @Override public AnnotationSetPool getAnnotationSetSection() {
            return new AnnotationSetPool(DexPool.this);
        }

        @NonNull @Override public EncodedArrayPool getEncodedArraySection() {
            return new EncodedArrayPool(DexPool.this);
        }
    }
}
