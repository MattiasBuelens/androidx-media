/*
 * Copyright 2022 The Android Open Source Project
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
package androidx.media3.transformer.mh;

import static androidx.media3.common.MimeTypes.VIDEO_DOLBY_VISION;
import static androidx.media3.common.MimeTypes.VIDEO_H265;
import static androidx.media3.common.util.Assertions.checkState;
import static androidx.media3.test.utils.TestUtil.retrieveTrackFormat;
import static androidx.media3.transformer.AndroidTestUtil.FORCE_TRANSCODE_VIDEO_EFFECTS;
import static androidx.media3.transformer.AndroidTestUtil.MP4_ASSET_1080P_5_SECOND_HLG10;
import static androidx.media3.transformer.AndroidTestUtil.MP4_ASSET_1080P_5_SECOND_HLG10_FORMAT;
import static androidx.media3.transformer.AndroidTestUtil.MP4_ASSET_720P_4_SECOND_HDR10;
import static androidx.media3.transformer.AndroidTestUtil.MP4_ASSET_720P_4_SECOND_HDR10_FORMAT;
import static androidx.media3.transformer.AndroidTestUtil.MP4_ASSET_DOLBY_VISION_HDR;
import static androidx.media3.transformer.AndroidTestUtil.MP4_ASSET_DOLBY_VISION_HDR_FORMAT;
import static androidx.media3.transformer.AndroidTestUtil.recordTestSkipped;
import static androidx.media3.transformer.Composition.HDR_MODE_KEEP_HDR;
import static androidx.media3.transformer.Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import android.content.Context;
import android.net.Uri;
import androidx.media3.common.C;
import androidx.media3.common.ColorInfo;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.Util;
import androidx.media3.transformer.AndroidTestUtil;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.EncoderUtil;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportTestResult;
import androidx.media3.transformer.TransformationRequest;
import androidx.media3.transformer.Transformer;
import androidx.media3.transformer.TransformerAndroidTestRunner;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

/**
 * {@link Transformer} instrumentation test for applying an {@linkplain
 * Composition#HDR_MODE_KEEP_HDR HDR frame edit}.
 */
@RunWith(AndroidJUnit4.class)
public final class HdrEditingTest {

  @Rule public final TestName testName = new TestName();

  private String testId;

  @Before
  public void setUpTestId() {
    testId = testName.getMethodName();
  }

  @Test
  public void export_transmuxHdr10File() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();

    if (Util.SDK_INT < 24) {
      // TODO: b/285543404 - Remove suppression once we can transmux H.265/HEVC before API 24.
      recordTestSkipped(context, testId, /* reason= */ "Can't transmux H.265/HEVC before API 24");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context,
        testId,
        /* inputFormat= */ MP4_ASSET_720P_4_SECOND_HDR10_FORMAT,
        /* outputFormat= */ null)) {
      return;
    }

    Transformer transformer = new Transformer.Builder(context).build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_720P_4_SECOND_HDR10));

    ExportTestResult exportTestResult =
        new TransformerAndroidTestRunner.Builder(context, transformer)
            .build()
            .run(testId, mediaItem);
    @C.ColorTransfer
    int actualColorTransfer =
        retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
            .colorInfo
            .colorTransfer;
    assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_ST2084);
  }

  @Test
  public void export_transmuxHlg10File() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();

    if (Util.SDK_INT < 24) {
      // TODO: b/285543404 - Remove suppression once we can transmux H.265/HEVC before API 24.
      recordTestSkipped(context, testId, /* reason= */ "Can't transmux H.265/HEVC before API 24");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context,
        testId,
        /* inputFormat= */ MP4_ASSET_1080P_5_SECOND_HLG10_FORMAT,
        /* outputFormat= */ null)) {
      return;
    }

    Transformer transformer = new Transformer.Builder(context).build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_1080P_5_SECOND_HLG10));

    ExportTestResult exportTestResult =
        new TransformerAndroidTestRunner.Builder(context, transformer)
            .build()
            .run(testId, mediaItem);
    @C.ColorTransfer
    int actualColorTransfer =
        retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
            .colorInfo
            .colorTransfer;
    assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_HLG);
  }

  @Test
  public void export_transmuxDolbyVisionFile() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();

    assumeTrue(Util.SDK_INT >= 24);

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context,
        testId,
        /* inputFormat= */ MP4_ASSET_DOLBY_VISION_HDR_FORMAT,
        /* outputFormat= */ null)) {
      return;
    }

    Transformer transformer = new Transformer.Builder(context).build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_DOLBY_VISION_HDR));

    ExportTestResult exportTestResult =
        new TransformerAndroidTestRunner.Builder(context, transformer)
            .build()
            .run(testId, mediaItem);
    Format trackFormat =
        retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO);
    @C.ColorTransfer
    int actualColorTransfer = trackFormat.colorInfo.colorTransfer;
    assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_HLG);
    String actualMimeType = trackFormat.sampleMimeType;
    assertThat(actualMimeType).isEqualTo(VIDEO_DOLBY_VISION);
  }

  @Test
  public void exportAndTranscode_hdr10File_whenHdrEditingIsSupported() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    Format format = MP4_ASSET_720P_4_SECOND_HDR10_FORMAT;
    if (!deviceSupportsHdrEditing(VIDEO_H265, format.colorInfo)) {
      recordTestSkipped(context, testId, /* reason= */ "Device lacks HDR10 editing support.");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context, testId, /* inputFormat= */ format, /* outputFormat= */ format)) {
      return;
    }

    Transformer transformer = new Transformer.Builder(context).build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_720P_4_SECOND_HDR10));
    EditedMediaItem editedMediaItem =
        new EditedMediaItem.Builder(mediaItem).setEffects(FORCE_TRANSCODE_VIDEO_EFFECTS).build();

    ExportTestResult exportTestResult =
        new TransformerAndroidTestRunner.Builder(context, transformer)
            .build()
            .run(testId, editedMediaItem);
    @C.ColorTransfer
    int actualColorTransfer =
        retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
            .colorInfo
            .colorTransfer;
    assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_ST2084);
  }

  @Test
  public void exportAndTranscode_hlg10File_whenHdrEditingIsSupported() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    Format format = MP4_ASSET_1080P_5_SECOND_HLG10_FORMAT;
    if (!deviceSupportsHdrEditing(VIDEO_H265, format.colorInfo)) {
      recordTestSkipped(context, testId, /* reason= */ "Device lacks HLG10 editing support.");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context, testId, /* inputFormat= */ format, /* outputFormat= */ format)) {
      return;
    }

    Transformer transformer = new Transformer.Builder(context).build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_1080P_5_SECOND_HLG10));
    EditedMediaItem editedMediaItem =
        new EditedMediaItem.Builder(mediaItem).setEffects(FORCE_TRANSCODE_VIDEO_EFFECTS).build();

    ExportTestResult exportTestResult =
        new TransformerAndroidTestRunner.Builder(context, transformer)
            .build()
            .run(testId, editedMediaItem);
    @C.ColorTransfer
    int actualColorTransfer =
        retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
            .colorInfo
            .colorTransfer;
    assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_HLG);
  }

  @Test
  public void exportAndTranscode_dolbyVisionFile_whenHdrEditingIsSupported() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    Format format = MP4_ASSET_DOLBY_VISION_HDR_FORMAT;
    if (!deviceSupportsHdrEditing(VIDEO_H265, format.colorInfo)) {
      recordTestSkipped(context, testId, /* reason= */ "Device lacks HLG10 editing support.");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context, testId, /* inputFormat= */ format, /* outputFormat= */ format)) {
      return;
    }

    Transformer transformer = new Transformer.Builder(context).build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_DOLBY_VISION_HDR));
    EditedMediaItem editedMediaItem =
        new EditedMediaItem.Builder(mediaItem).setEffects(FORCE_TRANSCODE_VIDEO_EFFECTS).build();

    ExportTestResult exportTestResult =
        new TransformerAndroidTestRunner.Builder(context, transformer)
            .build()
            .run(testId, editedMediaItem);
    @C.ColorTransfer
    int actualColorTransfer =
        retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
            .colorInfo
            .colorTransfer;
    assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_HLG);
  }

  @Test
  public void exportAndTranscode_hdr10File_whenHdrEditingUnsupported_toneMapsOrThrows()
      throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    Format format = MP4_ASSET_720P_4_SECOND_HDR10_FORMAT;
    if (deviceSupportsHdrEditing(VIDEO_H265, format.colorInfo)) {
      recordTestSkipped(context, testId, /* reason= */ "Device supports HDR10 editing.");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context, testId, /* inputFormat= */ format, /* outputFormat= */ null)) {
      return;
    }

    AtomicBoolean isFallbackListenerInvoked = new AtomicBoolean();
    AtomicBoolean isToneMappingFallbackApplied = new AtomicBoolean();
    Transformer transformer =
        new Transformer.Builder(context)
            .addListener(
                new Transformer.Listener() {
                  @Override
                  public void onFallbackApplied(
                      MediaItem inputMediaItem,
                      TransformationRequest originalTransformationRequest,
                      TransformationRequest fallbackTransformationRequest) {
                    isFallbackListenerInvoked.set(true);
                    assertThat(originalTransformationRequest.hdrMode).isEqualTo(HDR_MODE_KEEP_HDR);
                    isToneMappingFallbackApplied.set(
                        fallbackTransformationRequest.hdrMode
                            == HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL);
                  }
                })
            .build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_720P_4_SECOND_HDR10));
    EditedMediaItem editedMediaItem =
        new EditedMediaItem.Builder(mediaItem).setEffects(FORCE_TRANSCODE_VIDEO_EFFECTS).build();

    try {
      ExportTestResult exportTestResult =
          new TransformerAndroidTestRunner.Builder(context, transformer)
              .build()
              .run(testId, editedMediaItem);
      assertThat(isToneMappingFallbackApplied.get()).isTrue();
      @C.ColorTransfer
      int actualColorTransfer =
          retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
              .colorInfo
              .colorTransfer;
      assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_SDR);
    } catch (ExportException exception) {
      if (exception.getCause() != null) {
        @Nullable String message = exception.getCause().getMessage();
        if (message != null
            && (Objects.equals(message, "Decoding HDR is not supported on this device.")
                || message.contains(
                    "OpenGL ES 3.0 context support is required for HDR input or output.")
                || Objects.equals(message, "Device lacks YUV extension support."))) {
          return;
        }
      }
      throw exception;
    }
  }

  @Test
  public void exportAndTranscode_hlg10File_whenHdrEditingUnsupported_toneMapsOrThrows()
      throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    Format format = MP4_ASSET_1080P_5_SECOND_HLG10_FORMAT;
    if (deviceSupportsHdrEditing(VIDEO_H265, format.colorInfo)) {
      recordTestSkipped(context, testId, /* reason= */ "Device supports HLG10 editing.");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context, testId, /* inputFormat= */ format, /* outputFormat= */ null)) {
      return;
    }

    AtomicBoolean isToneMappingFallbackApplied = new AtomicBoolean();
    Transformer transformer =
        new Transformer.Builder(context)
            .addListener(
                new Transformer.Listener() {
                  @Override
                  public void onFallbackApplied(
                      MediaItem inputMediaItem,
                      TransformationRequest originalTransformationRequest,
                      TransformationRequest fallbackTransformationRequest) {
                    assertThat(originalTransformationRequest.hdrMode).isEqualTo(HDR_MODE_KEEP_HDR);
                    isToneMappingFallbackApplied.set(
                        fallbackTransformationRequest.hdrMode
                            == HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL);
                  }
                })
            .build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_1080P_5_SECOND_HLG10));
    EditedMediaItem editedMediaItem =
        new EditedMediaItem.Builder(mediaItem).setEffects(FORCE_TRANSCODE_VIDEO_EFFECTS).build();

    try {
      ExportTestResult exportTestResult =
          new TransformerAndroidTestRunner.Builder(context, transformer)
              .build()
              .run(testId, editedMediaItem);
      assertThat(isToneMappingFallbackApplied.get()).isTrue();
      @C.ColorTransfer
      int actualColorTransfer =
          retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
              .colorInfo
              .colorTransfer;
      assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_SDR);
    } catch (ExportException exception) {
      if (exception.getCause() != null) {
        @Nullable String message = exception.getCause().getMessage();
        if (message != null
            && (Objects.equals(message, "Decoding HDR is not supported on this device.")
                || message.contains(
                    "OpenGL ES 3.0 context support is required for HDR input or output.")
                || Objects.equals(message, "Device lacks YUV extension support."))) {
          return;
        }
      }
      throw exception;
    }
  }

  @Test
  public void exportAndTranscode_dolbyVisionFile_whenHdrEditingUnsupported_toneMapsOrThrows()
      throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    Format format = MP4_ASSET_DOLBY_VISION_HDR_FORMAT;
    if (deviceSupportsHdrEditing(VIDEO_H265, format.colorInfo)) {
      recordTestSkipped(context, testId, /* reason= */ "Device supports Dolby Vision editing.");
      return;
    }

    if (AndroidTestUtil.skipAndLogIfFormatsUnsupported(
        context, testId, /* inputFormat= */ format, /* outputFormat= */ null)) {
      return;
    }

    AtomicBoolean isFallbackListenerInvoked = new AtomicBoolean();
    AtomicBoolean isToneMappingFallbackApplied = new AtomicBoolean();
    Transformer transformer =
        new Transformer.Builder(context)
            .addListener(
                new Transformer.Listener() {
                  @Override
                  public void onFallbackApplied(
                      MediaItem inputMediaItem,
                      TransformationRequest originalTransformationRequest,
                      TransformationRequest fallbackTransformationRequest) {
                    isFallbackListenerInvoked.set(true);
                    assertThat(originalTransformationRequest.hdrMode).isEqualTo(HDR_MODE_KEEP_HDR);
                    isToneMappingFallbackApplied.set(
                        fallbackTransformationRequest.hdrMode
                            == HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL);
                  }
                })
            .build();
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(MP4_ASSET_DOLBY_VISION_HDR));
    EditedMediaItem editedMediaItem =
        new EditedMediaItem.Builder(mediaItem).setEffects(FORCE_TRANSCODE_VIDEO_EFFECTS).build();

    try {
      ExportTestResult exportTestResult =
          new TransformerAndroidTestRunner.Builder(context, transformer)
              .build()
              .run(testId, editedMediaItem);
      assertThat(isToneMappingFallbackApplied.get()).isTrue();
      @C.ColorTransfer
      int actualColorTransfer =
          retrieveTrackFormat(context, exportTestResult.filePath, C.TRACK_TYPE_VIDEO)
              .colorInfo
              .colorTransfer;
      assertThat(actualColorTransfer).isEqualTo(C.COLOR_TRANSFER_SDR);
    } catch (ExportException exception) {
      if (exception.getCause() != null) {
        @Nullable String message = exception.getCause().getMessage();
        if (message != null
            && (Objects.equals(message, "Decoding HDR is not supported on this device.")
            || message.contains(
            "OpenGL ES 3.0 context support is required for HDR input or output.")
            || Objects.equals(message, "Device lacks YUV extension support."))) {
          return;
        }
      }
      throw exception;
    }
  }

  private static boolean deviceSupportsHdrEditing(String mimeType, ColorInfo colorInfo) {
    checkState(ColorInfo.isTransferHdr(colorInfo));
    return !EncoderUtil.getSupportedEncodersForHdrEditing(mimeType, colorInfo).isEmpty();
  }
}
