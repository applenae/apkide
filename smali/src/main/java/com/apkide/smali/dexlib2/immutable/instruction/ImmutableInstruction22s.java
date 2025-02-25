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

package com.apkide.smali.dexlib2.immutable.instruction;

import androidx.annotation.NonNull;

import com.apkide.smali.dexlib2.Format;
import com.apkide.smali.dexlib2.Opcode;
import com.apkide.smali.dexlib2.iface.instruction.formats.Instruction22s;
import com.apkide.smali.dexlib2.util.Preconditions;

public class ImmutableInstruction22s extends ImmutableInstruction implements Instruction22s {
    public static final Format FORMAT = Format.Format22s;

    protected final int registerA;
    protected final int registerB;
    protected final int literal;

    public ImmutableInstruction22s(@NonNull Opcode opcode,
                                   int registerA,
                                   int registerB,
                                   int literal) {
        super(opcode);
        this.registerA = Preconditions.checkNibbleRegister(registerA);
        this.registerB = Preconditions.checkNibbleRegister(registerB);
        this.literal = Preconditions.checkShortLiteral(literal);
    }

    public static ImmutableInstruction22s of(Instruction22s instruction) {
        if (instruction instanceof ImmutableInstruction22s) {
            return (ImmutableInstruction22s)instruction;
        }
        return new ImmutableInstruction22s(
                instruction.getOpcode(),
                instruction.getRegisterA(),
                instruction.getRegisterB(),
                instruction.getNarrowLiteral());
    }

    @Override public int getRegisterA() { return registerA; }
    @Override public int getRegisterB() { return registerB; }
    @Override public int getNarrowLiteral() { return literal; }
    @Override public long getWideLiteral() { return literal; }

    @Override public Format getFormat() { return FORMAT; }
}