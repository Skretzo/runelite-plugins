/*
 * @(#)RIFFVIsitor.java  1.0  2005-01-09
 *
 * Copyright (c) 2005 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package com.videorecorder.video.riff;

import com.videorecorder.video.AbortException;
import com.videorecorder.video.ParseException;

/**
 * RIFFVIsitor is notified each time the RIFFParser visits
 * a data chunk and when a group is entered or leaved.
 *
 * @version  1.0  2005-01-09 Created.
 */
public interface RIFFVisitor {
    /** This method is invoked when the parser attempts to enter a group.
     * The visitor can return false, if the parse shall skip the group contents.
     * 
     * @param group
     * @return True to enter the group, false to skip over the group.
     */
    boolean enteringGroup(RIFFChunk group);
    
    /** This method is invoked when the parser enters a group chunk.*/
    void enterGroup(RIFFChunk group) throws ParseException, AbortException;
    
    /** This method is invoked when the parser leaves a group chunk.*/
    void leaveGroup(RIFFChunk group) throws ParseException, AbortException;
    
    /** This method is invoked when the parser has read a data chunk or
     * has skipped a stop chunk.*/
    void visitChunk(RIFFChunk group, RIFFChunk chunk) throws ParseException, AbortException;
}
