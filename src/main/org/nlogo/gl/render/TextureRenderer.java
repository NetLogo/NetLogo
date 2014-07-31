// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render;

import org.nlogo.api.World;

import javax.media.opengl.GL2;

class TextureRenderer {
  int texture = 0;
  int textureSize = 0;
  boolean newTexture = false;

  byte[][] tiles = null;

  final org.nlogo.api.World world;

  TextureRenderer(World world) {
    this.world = world;
  }

  void deleteTexture() {
    textureSize = 0;
  }

  // as I've been commenting I realize it may have been easier to just have different cases
  // for different tile arrangements rather than this massive crazy generalized method.
  // oh well.  ev 6/3/05
  void renderTextureTiles(GL2 gl, int width, int height, int textureSize,
                          int[] colors, boolean dirty) {
    float scale = Renderer.WORLD_SCALE;

    gl.glPushMatrix();

    // if we are following/riding an agent, translate the patches so the followed agent's
    // location is at the center of the world.  Then calculate the "wrapping-line" which
    // is where the patches overflows off the world. (note that there can only be at most
    // two lines).  The wrapping line is actually in a wierd set of coordinates scaled
    // between 0 and width/height.  The booleans indicate which side of the world the
    // patches are overflowing on. jrn 6/8/05
    float wrapXLine = 0.0f;
    float wrapYLine = 0.0f;
    boolean wrapXLeft = true;
    boolean wrapYDown = true;
    double worldWidth = world.worldWidth();
    double worldHeight = world.worldHeight();
    double oxcor = world.followOffsetX();
    double oycor = world.followOffsetY();

    if (oxcor + worldWidth < worldWidth) {
      wrapXLine = (float) (oxcor + worldWidth);
      wrapXLeft = false;
    } else if (oxcor > 0.0f) {
      wrapXLine = (float) oxcor;
    }

    wrapXLine *= (width / (float) worldWidth);

    if (oycor + worldHeight < worldHeight) {
      wrapYLine = (float) (oycor + worldHeight);
      wrapYDown = false;
    } else if (oycor > 0.0f) {
      wrapYLine = (float) oycor;
    }

    wrapYLine *= (height / (float) worldHeight);

    // translate patches so it is centered around followed agent
    gl.glTranslatef
        ((float) (-oxcor * scale), (float) (-oycor * scale), 0.0f);

    // these are extremely poor names but I'm really running out
    // of names these essentially keep track of whether the texture is
    // bigger than the world or the other way around and we use them in calculations
    // it's different from tileHeight and Width in that it doesn't change per
    // tile.
    float tw = Math.min((width), textureSize);
    float th = Math.min((height), textureSize);

    // when world size is less than the size of a texture scale down
    // by screen-size otherwise scale down by texture size * ( ratio of screen size
    // to world size ) note that for the patch texture this is 1 and for the
    // drawing texture this is 1 / patch-size
    gl.glScalef(tw * ((float) world.worldWidth() / (float) width),
        th * ((float) world.worldHeight() / (float) height), 1);

    // translate to the upper left hand corner
    // when there is only one tile this all works out to zero
    // otherwise it's the ratio of difference between the world-size and the
    // texture width and the texture width / 2 ( because we want the center )
    //( again if world < texture it's all zero )
    gl.glTranslatef(-(width - tw) * scale / (2 * tw),
        (height - th) * scale / (2 * th), 0.0f);

    int i = 0;
    // keep track of the width and height of the current tile
    int tileWidth = Math.min(width, textureSize);
    int tileHeight = Math.min(height, textureSize);

    // It easier for the yOffset to just count down since we are rendering the tiles
    // from top to bottom anyway - jrn 6/8/05
    for (int yOffset = height; yOffset > 0;) {
      gl.glPushMatrix();
      for (int xOffset = 0; xOffset < width;) {
        tileWidth = Math.min((width - xOffset), textureSize);
        tileHeight = Math.min(yOffset, textureSize);
        // refill the texture as rarely as possible.  However, if we have more
        // than one tile we have to do it all the time.  it might be better to have lots of
        // textures too but managing that seemed even more complicated + using more
        // and more resources dunno.  we'll see if we need it.
        if (dirty || tiles.length > 1) {
          tiles[i] = TextureUtils.reuseTexture(gl, tileWidth, tileHeight,
              xOffset, (height - yOffset),
              width, colors, tiles[i]);
        }

        gl.glPushMatrix();

        // the only time that this scale really matters is if we are tiling but the
        // tile is smaller than texture size ( the last tile ) in that case we want to
        // shrink the plane that we are rendering on.
        gl.glScalef(((tileWidth) / tw), ((tileHeight) / th), 1.0f);

        // we need to figure out if the wrapping line(s) intersect the tile, which will
        // cause the tile to split into 2/4's.  The *Split vars indicate where the tile
        // is being split on a scale from 0 to 1 (where 0,0 is top-left corner of the
        // tile) - jrn 6/8/05
        int horzPieces = 1;
        float horzSplit = 1.0f;
        int vertPieces = 1;
        float vertSplit = 1.0f;

        if ((xOffset < wrapXLine) && (xOffset + tileWidth > wrapXLine)) {
          horzPieces = 2;
          horzSplit = (wrapXLine - xOffset) / tileWidth;
        }
        if ((yOffset > wrapYLine) && (yOffset - tileHeight < wrapYLine)) {
          vertPieces = 2;
          vertSplit = (yOffset - wrapYLine) / tileHeight;
        }

        // render each piece of the tile
        for (int h = 0; h < horzPieces; h++) {
          gl.glPushMatrix();

          // locate the size and center (relative to top-left) of the piece
          float horzPieceSize = (h == 0) ? horzSplit : (1.0f - horzSplit);
          float horzPieceCenter = (horzPieceSize / 2) + ((h == 0) ?
              0.0f : horzSplit);

          // we need to translate from the center of the tile to the
          // center of the piece
          gl.glTranslatef((scale * (horzPieceCenter - 0.5f)), 0.0f, 0.0f);

          // check if the piece is off the world (and should be wrapped)
          if ((xOffset + tileWidth * horzPieceCenter < wrapXLine)
              && wrapXLeft) {
            gl.glTranslatef((width * scale / tileWidth), 0.0f, 0.0f);
          } else if ((xOffset + tileWidth * horzPieceCenter > wrapXLine)
              && !wrapXLeft) {
            gl.glTranslatef(-(width * scale / tileWidth), 0.0f, 0.0f);
          }

          for (int v = 0; v < vertPieces; v++) {
            gl.glPushMatrix();

            // locate the size and center (relative to top-left) of the piece
            float vertPieceSize = (v == 0) ? vertSplit : (1.0f - vertSplit);
            float vertPieceCenter = (vertPieceSize / 2) + ((v == 0) ?
                0.0f : vertSplit);

            // we need to translate from the center of the tile to the
            // center of the piece
            gl.glTranslatef(0.0f, (scale * ((1 - vertPieceCenter) - 0.5f)),
                0.0f);

            // check if the piece is off the world (and should be wrapped)
            if ((yOffset - tileHeight * vertPieceCenter < wrapYLine)
                && wrapYDown) {
              gl.glTranslatef(0.0f, (height * scale / tileHeight), 0.0f);
            } else if ((yOffset - tileHeight * vertPieceCenter > wrapYLine)
                && !wrapYDown) {
              gl.glTranslatef(0.0f, -(height * scale / tileHeight), 0.0f);
            }

            // render the piece
            TextureUtils.renderInPlane(gl, horzPieceSize, vertPieceSize, 1.0f,
                (horzPieceSize * tileWidth), (vertPieceSize * tileHeight),
                textureSize, ((h == 0) ? 0.0f : (horzSplit * tileWidth)),
                ((v == 0) ? 0.0f : (vertSplit * tileHeight)));

            gl.glPopMatrix();
          }
          gl.glPopMatrix();
        }

        gl.glPopMatrix();

        xOffset += textureSize;
        tileWidth = Math.min((width - xOffset), textureSize);
        // move to the location for the next tile
        // I figured this out somewhat by knowing the answers to the two important cases
        // ( tileSize = textureWidth and tileSize < textureWidth ) and intuition
        // in the first case we want that icky expression to evaluate to 1
        // ( by guessing ) I figured out that 0.3 moves us over "one texture".  I'm sure this
        // makes sense given earlier scaling but it's hard for me to wrap my mind around
        // the state enough to explain it.
        // in the second case we have to remember that the image is acutally not on the left
        // side of the texture ( curses ) so we want to move over less than before
        gl.glTranslatef((scale * (1 - ((((float) textureSize - (float) tileWidth) /
            textureSize)) / 2)),
            0.0f, 0.0f);
        i++;
      }
      gl.glPopMatrix();

      yOffset -= textureSize;
      tileHeight = Math.min(yOffset, textureSize);
      // y is reversed
      gl.glTranslatef(0.0f,
          -(scale * (1 - ((((float) textureSize - (float) tileHeight) /
              textureSize)) / 2)),
          0.0f);
    }
    gl.glPopMatrix();
  }

}
