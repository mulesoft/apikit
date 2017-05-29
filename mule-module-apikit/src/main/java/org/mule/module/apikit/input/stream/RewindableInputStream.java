/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.input.stream;

import java.io.IOException;
import java.io.InputStream;

public class RewindableInputStream extends InputStream implements Rewindable {

  private Block head;
  /**
   * If curBlockAvail > 0, then there are curBlockAvail bytes available to be
   * returned starting at curBlockPos in curBlock.buf.
   */
  private int curBlockAvail;
  private Block curBlock;
  private int curBlockPos;
  private Block lastBlock;
  /**
   * true unless willNotRewind has been called
   */
  private boolean saving = true;
  private final InputStream in;
  private boolean pretendClosed = false;
  /**
   * true if we have got an EOF from the underlying InputStream
   */
  private boolean eof;

  static class Block {

    Block next;
    final byte[] buf;
    int used = 0;

    static final int MIN_SIZE = 1024;

    Block(int minSize) {
      buf = new byte[Math.max(MIN_SIZE, minSize)];
    }

    Block() {
      this(0);
    }

    void append(byte b) {
      buf[used++] = b;
    }

    void append(byte[] b, int off, int len) {
      System.arraycopy(b, off, buf, used, len);
      used += len;
    }
  }

  public RewindableInputStream(InputStream in) {
    if (in == null) {
      throw new NullPointerException();
    }
    this.in = in;
  }

  public void close() throws IOException {
    if (saving) {
      curBlockAvail = 0;
      curBlock = null;
      pretendClosed = true;
    } else {
      head = null;
      curBlock = null;
      lastBlock = null;
      saving = false;
      curBlockAvail = 0;
      in.close();
    }
  }

  public void rewind() {
    if (!saving) {
      throw new IllegalStateException("rewind() after willNotRewind()");
    }
    pretendClosed = false;
    if (head == null) {
      return;
    }
    curBlock = head;
    curBlockPos = 0;
    curBlockAvail = curBlock.used;
  }

  public boolean canRewind() {
    return saving;
  }

  public void willNotRewind() {
    saving = false;
    head = null;
    lastBlock = null;
    if (pretendClosed) {
      pretendClosed = false;
      try {
        in.close();
      } catch (IOException e) {
      }
    }
  }

  public int read() throws IOException {
    if (curBlockAvail > 0) {
      int blockIndex = curBlock.buf[curBlockPos++] & 0xFF;
      --curBlockAvail;
      if (curBlockAvail == 0) {
        curBlock = curBlock.next;
        if (curBlock != null) {
          curBlockPos = 0;
          curBlockAvail = curBlock.used;
        }
      }
      return blockIndex;
    }
    int c = in.read();
    if (saving && c != -1) {
      if (lastBlock == null) {
        lastBlock = head = new Block();
      } else if (lastBlock.used == lastBlock.buf.length) {
        lastBlock = lastBlock.next = new Block();
      }
      lastBlock.append((byte) c);
    }
    return c;
  }

  public int read(byte b[], int off, int len) throws IOException {
    if (curBlockAvail == 0 && !saving) {
      return in.read(b, off, len);
    }
    if (b == null) {
      throw new NullPointerException();
    }
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    int nRead = 0;
    if (curBlockAvail != 0) {
      for (;;) {
        if (len == 0) {
          return nRead;
        }
        b[off++] = curBlock.buf[curBlockPos++];
        --len;
        nRead++;
        --curBlockAvail;
        if (curBlockAvail == 0) {
          curBlock = curBlock.next;
          if (curBlock == null) {
            break;
          }
          curBlockAvail = curBlock.used;
          curBlockPos = 0;
        }
      }
    }
    if (len == 0) {
      return nRead;
    }
    if (eof) {
      return nRead > 0 ? nRead : -1;
    }
    try {
      int n = in.read(b, off, len);
      if (n < 0) {
        eof = true;
        return nRead > 0 ? nRead : -1;
      }
      nRead += n;
      if (saving) {
        if (lastBlock == null) {
          lastBlock = head = new Block(n);

        } else if (lastBlock.buf.length - lastBlock.used < n) {
          if (lastBlock.used != lastBlock.buf.length) {
            int free = lastBlock.buf.length - lastBlock.used;
            lastBlock.append(b, off, free);
            off += free;
            n -= free;
          }
          lastBlock = lastBlock.next = new Block(n);
        }
        lastBlock.append(b, off, n);
      }
    } catch (IOException e) {
      eof = true;
      if (nRead == 0) {
        throw e;
      }
    }
    return nRead;
  }

  public int available() throws IOException {
    if (curBlockAvail == 0) {
      return in.available();
    }
    int n = curBlockAvail;
    for (Block b = curBlock.next; b != null; b = b.next) {
      n += b.used;
    }
    return n + in.available();
  }

}
