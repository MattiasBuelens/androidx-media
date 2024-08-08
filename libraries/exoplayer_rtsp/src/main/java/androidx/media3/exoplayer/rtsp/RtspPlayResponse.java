/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.exoplayer.rtsp;

import com.google.common.collect.ImmutableList;
import java.util.List;

/** Represents an RTSP PLAY response. */
/* package */ final class RtspPlayResponse {
  /** The response's status code. */
  public final int status;

  /** The playback start timing, {@link RtspSessionTiming#DEFAULT} if not present. */
  public final RtspSessionTiming sessionTiming;

  /** The list of {@link RtspTrackTiming} representing the {@link RtspHeaders#RTP_INFO} header. */
  public final ImmutableList<RtspTrackTiming> trackTimingList;

  /**
   * Creates a new instance.
   *
   * @param status The response's status code.
   * @param sessionTiming The {@link RtspSessionTiming}, pass {@link RtspSessionTiming#DEFAULT} if
   *     not present.
   * @param trackTimingList The list of {@link RtspTrackTiming} representing the {@link
   *     RtspHeaders#RTP_INFO} header.
   */
  public RtspPlayResponse(
      int status, RtspSessionTiming sessionTiming, List<RtspTrackTiming> trackTimingList) {
    this.status = status;
    this.sessionTiming = sessionTiming;
    this.trackTimingList = ImmutableList.copyOf(trackTimingList);
  }
}
