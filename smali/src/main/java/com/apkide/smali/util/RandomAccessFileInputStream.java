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

package com.apkide.smali.util;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RandomAccessFileInputStream extends InputStream {
    private int filePosition;
    @NonNull
    private final RandomAccessFile raf;

    public RandomAccessFileInputStream(@NonNull RandomAccessFile raf, int filePosition) {
        this.filePosition = filePosition;
        this.raf = raf;
    }

    @Override public int read() throws IOException {
        raf.seek(filePosition);
        filePosition++;
        return raf.read();
    }

    @Override public int read(byte[] bytes) throws IOException {
        raf.seek(filePosition);
        int bytesRead = raf.read(bytes);
        filePosition += bytesRead;
        return bytesRead;
    }

    @Override public int read(byte[] bytes, int offset, int length) throws IOException {
        raf.seek(filePosition);
        int bytesRead = raf.read(bytes, offset, length);
        filePosition += bytesRead;
        return bytesRead;
    }

    @Override public long skip(long l) throws IOException {
        int skipBytes = Math.min((int)l, available());
        filePosition += skipBytes;
        return skipBytes;
    }

    @Override public int available() throws IOException {
        return (int)raf.length() - filePosition;
    }

    @Override public boolean markSupported() {
        return false;
    }
}
