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

package com.apkide.smali.dexlib2.immutable.instruction;

import androidx.annotation.NonNull;

import com.apkide.smali.dexlib2.Format;
import com.apkide.smali.dexlib2.Opcode;
import com.apkide.smali.dexlib2.iface.instruction.formats.Instruction35mi;
import com.apkide.smali.dexlib2.util.Preconditions;

public class ImmutableInstruction35mi extends ImmutableInstruction implements Instruction35mi {
    public static final Format FORMAT = Format.Format35mi;

    protected final int registerCount;
    protected final int registerC;
    protected final int registerD;
    protected final int registerE;
    protected final int registerF;
    protected final int registerG;
    protected final int inlineIndex;

    public ImmutableInstruction35mi(@NonNull Opcode opcode,
                                    int registerCount,
                                    int registerC,
                                    int registerD,
                                    int registerE,
                                    int registerF,
                                    int registerG,
                                    int inlineIndex) {
        super(opcode);
        this.registerCount = Preconditions.check35cAnd45ccRegisterCount(registerCount);
        this.registerC = (registerCount>0) ? Preconditions.checkNibbleRegister(registerC) : 0;
        this.registerD = (registerCount>1) ? Preconditions.checkNibbleRegister(registerD) : 0;
        this.registerE = (registerCount>2) ? Preconditions.checkNibbleRegister(registerE) : 0;
        this.registerF = (registerCount>3) ? Preconditions.checkNibbleRegister(registerF) : 0;
        this.registerG = (registerCount>4) ? Preconditions.checkNibbleRegister(registerG) : 0;
        this.inlineIndex = Preconditions.checkInlineIndex(inlineIndex);
    }

    public static ImmutableInstruction35mi of(Instruction35mi instruction) {
        if (instruction instanceof ImmutableInstruction35mi) {
            return (ImmutableInstruction35mi)instruction;
        }
        return new ImmutableInstruction35mi(
                instruction.getOpcode(),
                instruction.getRegisterCount(),
                instruction.getRegisterC(),
                instruction.getRegisterD(),
                instruction.getRegisterE(),
                instruction.getRegisterF(),
                instruction.getRegisterG(),
                instruction.getInlineIndex());
    }

    @Override public int getRegisterCount() { return registerCount; }
    @Override public int getRegisterC() { return registerC; }
    @Override public int getRegisterD() { return registerD; }
    @Override public int getRegisterE() { return registerE; }
    @Override public int getRegisterF() { return registerF; }
    @Override public int getRegisterG() { return registerG; }
    @Override public int getInlineIndex() { return inlineIndex; }

    @Override public Format getFormat() { return FORMAT; }
}
