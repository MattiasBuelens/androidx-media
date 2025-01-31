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
package androidx.media3.effect;

import android.content.Context;
import android.view.SurfaceView;
import androidx.media3.common.ColorInfo;
import androidx.media3.common.DebugViewProvider;
import androidx.media3.common.util.UnstableApi;

/** {@link GlEffect} that renders to a {@link SurfaceView} provided by {@link DebugViewProvider}. */
@UnstableApi
public final class DebugViewEffect implements GlEffect {

  private final DebugViewProvider debugViewProvider;

  /**
   * Creates a new instance.
   *
   * @param debugViewProvider The class that provides the {@link SurfaceView} that the debug preview
   *     will be rendered to.
   */
  public DebugViewEffect(DebugViewProvider debugViewProvider) {
    this.debugViewProvider = debugViewProvider;
  }

  /**
   * Throws {@link UnsupportedOperationException} because {@link #toGlShaderProgram(Context,
   * ColorInfo)} should be used instead.
   */
  @Override
  public GlShaderProgram toGlShaderProgram(Context context, boolean useHdr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GlShaderProgram toGlShaderProgram(Context context, ColorInfo colorInfo) {
    return new DebugViewShaderProgram(context, debugViewProvider, colorInfo);
  }
}
