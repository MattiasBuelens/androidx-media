/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.exoplayer.video;

import static androidx.media3.container.ObuParser.OBU_FRAME;
import static androidx.media3.container.ObuParser.OBU_PADDING;
import static androidx.media3.container.ObuParser.OBU_SEQUENCE_HEADER;
import static androidx.media3.container.ObuParser.OBU_TEMPORAL_DELIMITER;
import static androidx.media3.container.ObuParser.split;

import androidx.annotation.Nullable;
import androidx.media3.container.ObuParser;
import androidx.media3.container.ObuParser.FrameHeader;
import androidx.media3.container.ObuParser.SequenceHeader;
import java.nio.ByteBuffer;
import java.util.List;

/** An AV1 bitstream parser that identifies frames that are not depended on. */
/* package */ final class Av1SampleDependencyParser {
  @Nullable private SequenceHeader sequenceHeader;

  /**
   * Returns the new sample {@linkplain ByteBuffer#limit() limit} after deleting any frames that are
   * not used as reference.
   *
   * <p>Each AV1 temporal unit must have exactly one shown frame. Other frames in the temporal unit
   * that aren't shown are used as reference, but the shown frame may not be used as reference.
   * Frequently, the shown frame is the last frame in the temporal unit.
   *
   * <p>If the last frame in the temporal unit is a non-reference {@link ObuParser#OBU_FRAME}, this
   * method returns a new {@link ByteBuffer#limit()} value that would leave only the frames used as
   * reference in the input {@code sample}.
   *
   * <p>See <a href=https://aomediacodec.github.io/av1-spec/#ordering-of-obus>Ordering of OBUs</a>.
   *
   * @param sample The sample data for one AV1 temporal unit.
   */
  public int sampleLimitAfterSkippingNonReferenceFrame(ByteBuffer sample) {
    List<ObuParser.Obu> obuList = split(sample);
    updateSequenceHeaders(obuList);
    int skippedFramesCount = 0;
    int last = obuList.size() - 1;
    while (last >= 0 && canSkipObu(obuList.get(last))) {
      if (obuList.get(last).type == OBU_FRAME) {
        skippedFramesCount++;
      }
      last--;
    }
    if (skippedFramesCount > 1) {
      return sample.limit();
    }
    if (last >= 0) {
      return obuList.get(last).payload.limit();
    }
    return sample.position();
  }

  /** Updates the parser state with the next sample data. */
  public void queueInputBuffer(ByteBuffer sample) {
    updateSequenceHeaders(split(sample));
  }

  private boolean canSkipObu(ObuParser.Obu obu) {
    if (obu.type == OBU_TEMPORAL_DELIMITER || obu.type == OBU_PADDING) {
      return true;
    }
    if (obu.type == OBU_FRAME && sequenceHeader != null) {
      FrameHeader frameHeader = FrameHeader.parse(sequenceHeader, obu);
      return frameHeader != null && !frameHeader.isDependedOn();
    }
    return false;
  }

  private void updateSequenceHeaders(List<ObuParser.Obu> obuList) {
    for (int i = 0; i < obuList.size(); ++i) {
      if (obuList.get(i).type == OBU_SEQUENCE_HEADER) {
        sequenceHeader = SequenceHeader.parse(obuList.get(i));
      }
    }
  }
}
