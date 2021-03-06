/* 
 * org.osjava.threads.ExtendedRunnable
 *
 * $Id$
 * $Rev$
 * $Date$
 * $Author$
 *
 * Created on Aug 01, 2002
 *
 * Copyright (c) 2002-2003, Robert M. Zigweid
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * + Neither the name of the OSJava-Threads nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.osjava.threads;

/**
 * Interface extension to {@link Runnable} which adds the requesite method 
 * {@link #setAbort(boolean)} to {@link ExtendedThread}s and or their Runnables.
 * 
 * @author Robert M. Zigweid
 * @version $Revision$ $Date$
 */

public interface ExtendedRunnable extends Runnable {

    /**
     * Cause an {@link ExtendedRunnable} that has a continual loop to abort.
     * 
     * @param abort boolean value determining whether or not the thread is to 
     *              be aborted, or can be set to halt a previously declared 
     *              abort
     */
    void setAbort(boolean abort);

    /** 
     * Get the current status of whether or not the ExtendedRunnable is
     * supposed to abort.
     * 
     * @return a boolean value indicating whether or not the ExtendedRunnable
     *         is to cease execution.
     */
    boolean isAborting();

    /**
     * Send a notify() to the Thread.  This is done in such a way that if 
     * the Thread wraps a Runnable object, that is notified instead.  This 
     * method takes care of all synchronization issues.
     */
    void wakeup();
    
}
