/// LICENSES

// This class is based on sim.util.media.MovieEncoder by Sean Luke:
/**
 * MASON Open Source License
 *
 * License Agreement
 *
 *
 * This software is Copyright 2003 by Sean Luke. Portions Copyright 2003 by
 * Gabriel Catalin Balan, Liviu Panait, Sean Paus, and Dan Kuebrich. All
 * Rights Reserved
 *
 * Developed in Conjunction with the George Mason University Center for Social
 * Complexity
 *
 * By using the source code, binary code files, or related data included in
 * this distribution, you agree to the following terms of usage for this
 * software distribution. All but a few source code files in this distribution
 * fall under this license; the exceptions contain open source licenses
 * embedded in the source code files themselves. In this license the Authors
 * means the Copyright Holders listed above, and the license itself is
 * Copyright 2003 by Sean Luke.
 *
 * The Authors hereby grant you a world-wide, royalty-free, non-exclusive
 * license, subject to third party intellectual property claims:
 *
 * to use, reproduce, modify, display, perform, sublicense and distribute all
 * or any portion of the source code or binary form of this software or
 * related data with or without modifications, or as part of a larger work;
 * and under patents now or hereafter owned or controlled by the Authors, to
 * make, have made, use and sell ("Utilize") all or any portion of the source
 * code or binary form of this software or related data, but solely to the
 * extent that any such patent is reasonably necessary to enable you to
 * Utilize all or any portion of the source code or binary form of this
 * software or related data, and not to any greater extent that may be
 * necessary to Utilize further modifications or combinations.
 *
 *
 * In return you agree to the following conditions:
 *
 * If you redistribute all or any portion of the source code of this software
 * or related data, it must retain the above copyright notice and this license
 * and disclaimer. If you redistribute all or any portion of this code in
 * binary form, you must include the above copyright notice and this license
 * and disclaimer in the documentation and/or other materials provided with
 * the distribution, and must indicate the use of this software in a
 * prominent, publically accessible location of the larger work. You must not
 * use the Authors's names to endorse or promote products derived from this
 * software without the specific prior written permission of the Authors.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS, NOR THEIR EMPLOYERS, NOR GEORGE MASON
 * UNIVERSITY, BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **/


/// Some of this code was snarfed from
/// http://java.sun.com/products/java-media/jmf/2.1.1/solutions/JpegImagesToMovie.java
/// Here's the license to that code. -- SL

/*
 * @(#)JpegImagesToMovie.java   1.3 01/03/13
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package org.nlogo.awt;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;
import javax.media.util.ImageToBuffer;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Encodes a sequence of BufferedImages into a QuickTime movie.
 * Requires JMF 2.1.1.
 * <p>Parts of these classes are loosely based on MovieEncoder.java by Sean Luke
 * which is in turn based on JpegImagesToMovie.java from Sun.
 * Both licenses are attached.
 */
public strictfp class JMFMovieEncoder
    implements MovieEncoder,
    DataSinkListener, ControllerListener, java.io.Serializable {

  private boolean isSetup = false;
  private boolean stopped = false;

  private float frameRate;
  private final String fileName;

  private Processor processor;
  private JMFMovieEncoderDataSource source;
  private DataSink sink;
  private int numFrames = 0;
  private java.io.File outFile;

  private Dimension frameSize;
  private Format format;
  private int type;

  /**
   * Creates a new movie encoder.
   *
   * @param frameRate frame rate in frames per second.
   */
  public JMFMovieEncoder(int frameRate, String fileName) {
    this.frameRate = frameRate;
    this.fileName = fileName;
  }

  public synchronized void setFrameRate(float frameRate)
      throws IOException {
    if (isSetup) {
      throw new IOException("Can't set framerate after setup");
    }

    this.frameRate = frameRate;
  }

  public synchronized float getFrameRate() {
    return frameRate;
  }

  public synchronized Dimension getFrameSize() {
    return frameSize;
  }

  public synchronized String getFormat() {
    return format.toString();
  }

  public synchronized void add(BufferedImage image)
      throws IOException {

    if (numFrames == 0) {
      setup(image);
    }

    if (!stopped) {
      image = preProcess(image);
      source.add(image);
      numFrames++;
    }
  }

  public boolean isSetup() {
    return isSetup;
  }

  /**
   * Sets everything up. Called the first time an image is added
   * to this movie encoder.
   *
   * @param image a typical image; used to determine frame format and size
   */
  protected synchronized void setup(BufferedImage image)
      throws IOException {
    try {
      frameSize = new Dimension(image.getWidth(), image.getHeight());
      type = image.getType();
      numFrames = 0;

      // Figure out available formats and pick the first one
      // We're using frameRate here but I don't think that should matter
      format = ImageToBuffer.createBuffer(image, frameRate).getFormat();
      source = new JMFMovieEncoderDataSource(format, frameRate);
      processor = Manager.createProcessor(source);
      processor.addControllerListener(this);
      processor.configure();
      if (!waitForState(processor, Processor.Configured)) {
        throw new IOException("JMFMovieEncoder error: Failed to configure processor");
      }

      processor.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

      TrackControl tcs[] = processor.getTrackControls();

      // Take the first supported format
      Format f[] = tcs[0].getSupportedFormats();

      //System.out.println("Supported formats:");
      //for (int j=0;j<f.length;j++) { System.out.println(f[j]); }

      if (f == null || f.length <= 0) {
        throw new IOException("The mux does not support the input format: " + tcs[0].getFormat());
      }
      tcs[0].setFormat(f[0]);
      format = f[0];

      //System.out.println("Codecs for " + format + ":");
      //Vector v =
      //javax.media.PlugInManager.getPlugInList(format, null,
      //javax.media.PlugInManager.CODEC);
      //java.util.Iterator it = v.iterator();
      //while(it.hasNext()) { System.out.println(it.next()); }

      // Realize the processor
      processor.realize();
      if (!waitForState(processor, Controller.Realized)) {
        throw new IOException("Failed to realize processor");
      }

      // Set up the sink
      outFile = new java.io.File(fileName);
      sink = Manager.createDataSink(processor.getDataOutput(),
          new MediaLocator(toURL(outFile)));
      sink.addDataSinkListener(this);
      sink.open();
      processor.start();
      sink.start();
      isSetup = true;
    } catch (javax.media.MediaException ex) {
      // we recast all media exceptions as ioexceptions since we don't want
      // any other NetLogo class to have to know about JMF
      throw new IOException("Cannot setup movie: " + ex);
    }
  }

  public synchronized void stop() {
    if (stopped) {
      return;
    }

    stopped = true;

    if (isSetup) {
      source.finish();

      waitForFileDone(); // wait for EndOfStream event.
      sink.close();

      processor.removeControllerListener(this);
    }


  }

  public synchronized void cancel() {
    if (stopped) {
      return;
    }
    stopped = true;

    if (isSetup) {
      source.finish();

      waitForFileDone(); // wait for EndOfStream event.
      sink.close();

      processor.removeControllerListener(this);
      stopped = true;  // just in case the dataSinkUpdate didn't do it -- interruptedException maybe

      // delete the file
      outFile.delete();
    }
  }

  public int getNumFrames() {
    return numFrames;
  }

  /**
   * Ensures that each frame is the same format and size.
   * Called on each image before it's added to the movie.
   */
  protected BufferedImage preProcess(BufferedImage image) {
    if (image.getWidth() != frameSize.width || image.getHeight() != frameSize.height || image.getType() != type) {
      // if there's a size or type mismatch, shlop the image onto one that fits
      BufferedImage temp = new BufferedImage(frameSize.width, frameSize.height, type);
      Graphics2D g = temp.createGraphics();
      g.drawImage(image, 0, 0, null);
      image = temp;
    }
    return image;
  }


  private final Object waitSync = new Object();
  private boolean stateTransitionOK = true;

  /**
   * Blocks until the processor has transitioned to the given state.
   *
   * @return false if the transition failed.
   */
  private boolean waitForState(Processor p, int state) {
    synchronized (waitSync) {
      try {
        while (p.getState() < state && stateTransitionOK) {
          waitSync.wait();
        }
      } catch (InterruptedException ex) {
        ignore(ex);
      }
    }
    return stateTransitionOK;
  }

  /**
   * From ControllerListener.
   */
  public void controllerUpdate(ControllerEvent evt) {
    if (evt instanceof ConfigureCompleteEvent ||
        evt instanceof RealizeCompleteEvent ||
        evt instanceof PrefetchCompleteEvent) {
      synchronized (waitSync) {
        stateTransitionOK = true;
        waitSync.notifyAll();
      }
    } else if (evt instanceof ResourceUnavailableEvent) {
      synchronized (waitSync) {
        stateTransitionOK = false;
        waitSync.notifyAll();
      }
    } else if (evt instanceof EndOfMediaEvent) {
      evt.getSourceController().stop();
      evt.getSourceController().close();
    }
  }


  private final Object waitFileSync = new Object();
  private boolean fileDone = false;
  private boolean fileSuccess = true;

  /**
   * Blocks until file writing is done.
   */
  private boolean waitForFileDone() {
    synchronized (waitFileSync) {
      try {
        while (!fileDone) {
          waitFileSync.wait();
        }
      } catch (InterruptedException ex) {
        ignore(ex);
      }
    }
    return fileSuccess;
  }


  /**
   * Event handler for the file writer.
   */
  public void dataSinkUpdate(DataSinkEvent evt) {
    if (evt instanceof EndOfStreamEvent) {
      synchronized (waitFileSync) {
        fileDone = true;
        waitFileSync.notifyAll();
      }
    } else if (evt instanceof DataSinkErrorEvent) {
      synchronized (waitFileSync) {
        fileDone = true;
        fileSuccess = false;
        waitFileSync.notifyAll();
      }
    }
  }

  ///

  private static void ignore(Throwable t) // NOPMD ok to ignore
  {
    // do nothing, but you could put debugging output here,
    // or a debugger breakpoint... - ST 5/14/03
  }

  // for 4.1 we have too much fragile, difficult-to-understand,
  // under-tested code involving URLs -- we can't get rid of our
  // uses of toURL() until 4.2, the risk of breakage is too high.
  // so for now, at least we make this a separate method so the
  // SuppressWarnings annotation is narrowly targeted. - ST 12/7/09
  @SuppressWarnings("deprecation")
  private static java.net.URL toURL(java.io.File file)
      throws java.net.MalformedURLException {
    return file.toURL();
  }

}


/**
 * A simple data source wraps up the image stream.
 */
class JMFMovieEncoderDataSource extends PullBufferDataSource {
  JMFMovieEncoderDataStream[] streams;

  public JMFMovieEncoderDataSource(Format format, float frameRate) {
    streams = new JMFMovieEncoderDataStream[1];
    streams[0] = new JMFMovieEncoderDataStream(format, frameRate);
  }

  @Override
  public String getContentType() {
    return ContentDescriptor.RAW;
  }

  @Override
  public PullBufferStream[] getStreams() {
    return streams;
  }

  @Override
  public Time getDuration() {
    return DURATION_UNKNOWN;
  }

  public void add(Image i) {
    streams[0].write(i);
  }

  public void finish() {
    streams[0].finish();
  }

  // empty methods
  @Override
  public Object[] getControls() {
    return new Object[0];
  }

  @Override
  public Object getControl(String type) {
    return null;
  }

  @Override
  public void setLocator(MediaLocator source) {
  }

  @Override
  public MediaLocator getLocator() {
    return null;
  }

  @Override
  public void connect() {
  }

  @Override
  public void disconnect() {
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

}

/**
 * JMFMovieEncoderDataStream
 * Provides the underlying JMF Processor with images (converted to Buffers) for it to
 * encode and write out to disk as it sees fit.
 */
// Our stream from which the processor requests images.  Here's how it works.
// You put an image in the stream with write().  This is blocking -- we have to
// wait until any existing image in there has been flushed out.
// Additionally, the underlying processor is reading images [as buffers] with read(),
// and it's blocking waiting for us to provide stuff.  The blocks are handled with
// spin-waits (25ms sleeps) because I'm being lazy. -- SL
class JMFMovieEncoderDataStream implements PullBufferStream {
  // we won't have a buffered mechanism here -- instead we'll assume
  // that there is one, and exactly one, image waiting
  Buffer buffer = null;
  Format format;
  boolean ended = false;
  boolean endAcknowledged = false;
  float frameRate;

  JMFMovieEncoderDataStream(Format format, float frameRate) {
    this.frameRate = frameRate;
    this.format = format;
  }

  void finish() {
    synchronized (this) {
      ended = true;
    }
  }

  // blocks on write
  void write(Image i) {
    Buffer b = ImageToBuffer.createBuffer(i, frameRate);

    // spin-wait, ugh
    while (checkWriteBlock()) {
      try {
        Thread.sleep(25);
      } catch (InterruptedException e) {
        return;
      }
    }
    synchronized (this) {
      buffer = b;
    }
  }

  synchronized boolean checkWriteBlock() {
    return (buffer != null);
  }

  synchronized boolean checkReadBlock() {
    return (buffer == null && !ended);
  }

  public boolean willReadBlock() {
    return false;  // liar liar pants on fire
  }

  // could block on read
  public void read(Buffer buf) {
    // spin-wait, ugh
    while (checkReadBlock()) {
      try {
        Thread.sleep(25);
      } catch (InterruptedException e) {
        System.out.println("ugh.");
      }
    }

    // Check if we need to close up shop
    synchronized (this) {
      if (ended) {
        // We are done.  Set EndOfMedia.
        buf.setEOM(true);
        buf.setOffset(0);
        buf.setLength(0);
        endAcknowledged = true;
      } else {
        // load the data
        buf.setData(buffer.getData());
        buf.setLength(buffer.getLength());
        buf.setOffset(0);
        buf.setFormat(format);
        buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME | Buffer.FLAG_NO_DROP);  // must write the frame
      }
      buffer = null;
    }
  }

  // returns the buffered image format
  public Format getFormat() {
    return format;
  }

  public ContentDescriptor getContentDescriptor() {
    return new ContentDescriptor(ContentDescriptor.RAW);
  }

  public boolean endOfStream() {
    return ended; // or should it be endAcknowledged?
  }

  // empty methods
  public long getContentLength() {
    return 0;
  }

  public Object[] getControls() {
    return new Object[0];
  }

  public Object getControl(String type) {
    return null;
  }

}
