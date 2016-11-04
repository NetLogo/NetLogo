# NetLogo {{version}}

{{date}}

## Release notes

See our [online release notes](https://github.com/NetLogo/NetLogo/wiki/Release-notes).

## Upgrading

Most models created in earlier versions of NetLogo will work in
this release with only minor updates to the code.  If you have
trouble, please consult our
[Transition Guide](http://ccl.northwestern.edu/netlogo/docs/transition.html).

## Community

Our [resources page](http://ccl.northwestern.edu/netlogo/resources.shtml)
contains links to NetLogo mailing lists, places to get help, books,
add-ons, and more.

## How to run

### Windows

Choose NetLogo from the Start menu.

### Mac

Double click on the NetLogo icon in the NetLogo folder.  (You may wish
to drag this icon to the dock for easy access.)

### Linux et al

You may be able to just double-click `NetLogo` in your file manager.
Or, from the command line, typical Unix shell commands would be:

    $ cd netlogo-{{version}}-*
    $ ./NetLogo

## Citing

If you use or refer to NetLogo in a publication, we ask that you cite
it.  The correct citation is:

> Wilensky, U. (1999). NetLogo. http://ccl.northwestern.edu/netlogo.
> Center for Connected Learning and Computer-Based Modeling,
> Northwestern University, Evanston, IL.

For HubNet, cite:

> Wilensky, U. & Stroup, W., 1999. HubNet.
> http://ccl.northwestern.edu/netlogo/hubnet.html. Center
> for Connected Learning and Computer-Based Modeling, Northwestern
> University. Evanston, IL.

For models in the Models Library, the correct citation is included in
the "Credits and References" section of each model's Info tab.

## Acknowledgments

The CCL gratefully acknowledges almost two decades of support for our
NetLogo work. Much of that support came from the National Science
Foundation -- grant numbers REC-9814682 and REC-0126227, with further
support from REC-0003285, REC-0115699, DRL-0196044, CCF-ITR-0326542,
DRL-REC/ROLE-0440113, SBE-0624318, EEC-0648316, IIS-0713619,
DRL-RED-9552950, DRL-REC-9632612, DRL-DRK12-1020101, IIS-1441552,
CNS-1441016, CNS-1441041, IIS-1438813, and REC-1343873.  Additional
support came from the Spencer Foundation, Texas Instruments, the
Brady Fund, and the Northwestern Institute on Complex Systems.

## License

NetLogo  
Copyright (C) 1999-2016 Uri Wilensky

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301, USA.

### Commercial licensing

Commercial licenses are also available. To inquire about commercial
licenses, please contact Uri Wilensky at
[uri@northwestern.edu](mailto:uri@northwestern.edu).

### User Manual license

NetLogo User Manual  
Copyright (C) 1999-2016 Uri Wilensky

This work is licensed under the Creative Commons
Attribution-ShareAlike 3.0 Unported License. To view a copy of this
license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send
a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain
View, California, 94041, USA.

## Third party licenses

### Scala

Much of NetLogo is written in the Scala language and uses the
Scala standard libraries.  The license for Scala is as follows:

> Copyright (c) 2002-2011 EPFL, Lausanne, unless otherwise specified.
> All rights reserved.
>
> This software was developed by the Programming Methods Laboratory of the
> Swiss Federal Institute of Technology (EPFL), Lausanne, Switzerland.
>
> Permission to use, copy, modify, and distribute this software in source
> or binary form for any purpose with or without fee is hereby granted,
> provided that the following conditions are met:
>
> 1. Redistributions of source code must retain the above copyright
>   notice, this list of conditions and the following disclaimer.
> 2. Redistributions in binary form must reproduce the above copyright
>    notice, this list of conditions and the following disclaimer in the
>    documentation and/or other materials provided with the distribution
> 3. Neither the name of the EPFL nor the names of its contributors
>    may be used to endorse or promote products derived from this
>    software without specific prior written permission.
>
> THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
>ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
> IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
> ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
> FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIA
> DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
> SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
> CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
> LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
> OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
>SUCH DAMAGE.

### MersenneTwisterFast

For random number generation, NetLogo uses the MersenneTwisterFast class
by Sean Luke.  The copyright for that code is as follows:

> Copyright (c) 2003 by Sean Luke.  
> Portions copyright (c) 1993 by Michael Lecuyer  
>All rights reserved.
>
> Redistribution and use in source and binary forms, with or without
> modification, are permitted provided that the following conditions ar
> met:
>
> - Redistributions of source code must retain the above copyright
>   notice, this list of conditions and the following disclaimer.
> - Redistributions in binary form must reproduce the above copyright
>   notice, this list of conditions and the following disclaimer in the
>   documentation and/or other materials provided with the distribution.
> - Neither the name of the copyright owners, their employers, nor the
>   names of its contributors may be used to endorse or promote products
>   derived from this software without specific prior written
>   permission.
>
> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
> "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
> LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
> A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT
> OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
> SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
> LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
> DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
> THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
> (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
> OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

### Colt

Parts of this software (specifically, the random-gamma primitive) are
based on code from the Colt library
(http://acs.lbl.gov/~hoschek/colt/).  The copyright for
that code is as follows:

> Copyright 1999 CERN - European Organization for Nuclear Research.
> Permission to use, copy, modify, distribute and sell this software and
> its documentation for any purpose is hereby granted without fee,
> provided that the above copyright notice appear in all copies and that
> both that copyright notice and this permission notice appear in
> supporting documentation.  CERN makes no representations about the
> suitability of this software for any purpose.  It is provided "as is"
> without expressed or implied warranty.

### JHotDraw

For the system dynamics modeler, NetLogo uses the JHotDraw library:

> Copyright (c) 1996, 1997 by IFA Informatik and Erich Gamma.  The
> library is covered by the GNU LGPL (Lesser General Public License).
> The text of that license is included in the "docs" folder which
> accompanies the NetLogo download, and is also available from
> http://www.gnu.org/copyleft/lesser.html .

### MovieEncoder

For movie-making, NetLogo uses code adapted from
sim.util.media.MovieEncoder.java by Sean Luke, distributed under the
MASON Open Source License. The copyright for that code is as follows:

> This software is Copyright 2003 by Sean Luke. Portions Copyright 2003
> by Gabriel Catalin Balan, Liviu Panait, Sean Paus, and Dan Kuebrich.
> All Rights Reserved
>
> Developed in Conjunction with the George Mason University Center for
> Social Complexity
>
> By using the source code, binary code files, or related data included
> in this distribution, you agree to the following terms of usage for
> this software distribution. All but a few source code files in this
> distribution fall under this license; the exceptions contain open
> source licenses embedded in the source code files themselves. In this
> license the Authors means the Copyright Holders listed above, and the
> license itself is Copyright 2003 by Sean Luke.
>
> The Authors hereby grant you a world-wide, royalty-free, non-exclusive
> license, subject to third party intellectual property claims:
>
> to use, reproduce, modify, display, perform, sublicense and distribute
> all or any portion of the source code or binary form of this software
> or related data with or without modifications, or as part of a larger
> work; and under patents now or hereafter owned or controlled by the
> Authors, to make, have made, use and sell ("Utilize") all or any
> portion of the source code or binary form of this software or related
> data, but solely to the extent that any such patent is reasonably
> necessary to enable you to Utilize all or any portion of the source
> code or binary form of this software or related data, and not to any
> greater extent that may be necessary to Utilize further modifications
> or combinations.
>
> In return you agree to the following conditions:
> 
> If you redistribute all or any portion of the source code of this
> software or related data, it must retain the above copyright notice
> and this license and disclaimer. If you redistribute all or any
> portion of this code in binary form, you must include the above
> copyright notice and this license and disclaimer in the documentation
> and/or other materials provided with the distribution, and must
> indicate the use of this software in a prominent, publically
> accessible location of the larger work. You must not use the Authors's
> names to endorse or promote products derived from this software
> without the specific prior written permission of the Authors.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
> EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
> MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
> IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS, NOR THEIR
> EMPLOYERS, NOR GEORGE MASON UNIVERSITY, BE LIABLE FOR ANY CLAIM,
> DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
> OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
> THE USE OR OTHER DEALINGS IN THE SOFTWARE.

### JpegImagesToMovie

For movie-making, NetLogo uses code adapted from
JpegImagesToMovie.java by Sun Microsystems:

> Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
>
> Sun grants you ("Licensee") a non-exclusive, royalty free, license to
> use, modify and redistribute this software in source and binary code
> form, provided that i) this copyright notice and license appear on all
> copies of the software; and ii) Licensee does not utilize the software
> in a manner which is disparaging to Sun.
>
> This software is provided "AS IS," without a warranty of any kind. ALL
> EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
> INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
> PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
> ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
> AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS
> DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY
> LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
> CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
> REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
> INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
> POSSIBILITY OF SUCH DAMAGES.
>
> This software is not designed or intended for use in on-line control
> of aircraft, air traffic, aircraft navigation or aircraft
> communications; or in the design, construction, operation or
> maintenance of any nuclear facility. Licensee represents and warrants
> that it will not use or redistribute the Software for such purposes.

### JOGL

For 3D graphics rendering, NetLogo uses JOGL and GlueGen,
Java APIs for OpenGL. For more information about JOGL
and GlueGen, see https://jogamp.org/.  The libraries are
distributed under the following licenses:

> The JOGL source code is mostly licensed under the 'New BSD 2-Clause License',
> however it contains other licensed material as well.
>
> Other licensed material is compatible with the 'New BSD 2-Clause License',
> if not stated otherwise.
>
> 'New BSD 2-Clause License' incompatible materials are optional, they are:
>
>     A.7) The JOGL source tree _may_ contain code from Oculus VR, Inc.
>          which is covered by it's own permissive Oculus VR Rift SDK Software License.
>          (Optional, see A.7 below for details)
>
> Below you find a detailed list of licenses used in this project.
>
> +++
>
> The content of folder 'make/lib' contains build- and test-time only
> Java binaries (JAR) to ease the build setup.
> Each JAR file has it's corresponding LICENSE file containing the
> source location and license text. None of these binaries are contained in any way
> by the generated and deployed JOGL binaries.
>
> +++
>
> L.1) The JOGL source tree contains code from the JogAmp Community
>      which is covered by the Simplified BSD 2-clause license:
>
>    Copyright 2010 JogAmp Community. All rights reserved.
>
>    Redistribution and use in source and binary forms, with or without modification, are
>    permitted provided that the following conditions are met:
>
>       1. Redistributions of source code must retain the above copyright notice, this list of
>          conditions and the following disclaimer.
>
>       2. Redistributions in binary form must reproduce the above copyright notice, this list
>          of conditions and the following disclaimer in the documentation and/or other materials
>          provided with the distribution.
>
>    THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
>    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
>    FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
>    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
>    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
>    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
>    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
>    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
>    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
>
>    The views and conclusions contained in the software and documentation are those of the
>    authors and should not be interpreted as representing official policies, either expressed
>    or implied, of JogAmp Community.
>
>    You can address the JogAmp Community via:
>        Web                http://jogamp.org/
>        Forum/Mailinglist  http://forum.jogamp.org
>        Chatrooms
>          IRC              irc.freenode.net #jogamp
>          Jabber           conference.jabber.org room: jogamp (deprecated!)
>        Repository         http://jogamp.org/git/
>        Email              mediastream _at_ jogamp _dot_ org
>
>
> L.2) The JOGL source tree contains code from Sun Microsystems, Inc.
>      which is covered by the New BSD 3-clause license:
>
>    Copyright (c) 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
>
>    Redistribution and use in source and binary forms, with or without
>    modification, are permitted provided that the following conditions are
>    met:
>
>    - Redistribution of source code must retain the above copyright
>      notice, this list of conditions and the following disclaimer.
>
>    - Redistribution in binary form must reproduce the above copyright
>      notice, this list of conditions and the following disclaimer in the
>      documentation and/or other materials provided with the distribution.
>
>    Neither the name of Sun Microsystems, Inc. or the names of
>    contributors may be used to endorse or promote products derived from
>    this software without specific prior written permission.
>
>    This software is provided "AS IS," without a warranty of any kind. ALL
>    EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
>    INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
>    PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
>    MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
>    ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
>    DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
>    ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
>    DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
>    DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
>    ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
>    SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
>
>    You acknowledge that this software is not designed or intended for use
>    in the design, construction, operation or maintenance of any nuclear
>    facility.
>
> L.3) The JOGL source tree contains code ported from the OpenGL sample
>      implementation by Silicon Graphics, Inc. This code is licensed under
>      the SGI Free Software License B, Version 2.0
>
>    License Applicability. Except to the extent portions of this file are
>    made subject to an alternative license as permitted in the SGI Free
>    Software License B, Version 2.0 (the "License"), the contents of this
>    file are subject only to the provisions of the License. You may not use
>    this file except in compliance with the License. You may obtain a copy
>    of the License at Silicon Graphics, Inc., attn: Legal Services, 1600
>    Amphitheatre Parkway, Mountain View, CA 94043-1351, or at:
>
>    http://oss.sgi.com/projects/FreeB
>    http://oss.sgi.com/projects/FreeB/SGIFreeSWLicB.2.0.pdf
>    Or within this repository: doc/licenses/SGIFreeSWLicB.2.0.pdf
>
>    Note that, as provided in the License, the Software is distributed on an
>    "AS IS" basis, with ALL EXPRESS AND IMPLIED WARRANTIES AND CONDITIONS
>    DISCLAIMED, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTIES AND
>    CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY, FITNESS FOR A
>    PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
>
> L.4) The JOGL source tree contains code from the LWJGL project which is
>      similarly covered by the New BSD 3-clause license:
>
>    Copyright (c) 2002-2004 LWJGL Project
>    All rights reserved.
>
>    Redistribution and use in source and binary forms, with or without
>    modification, are permitted provided that the following conditions are
>    met:
>
>    * Redistributions of source code must retain the above copyright
>      notice, this list of conditions and the following disclaimer.
>
>    * Redistributions in binary form must reproduce the above copyright
>      notice, this list of conditions and the following disclaimer in the
>      documentation and/or other materials provided with the distribution.
>
>    * Neither the name of 'LWJGL' nor the names of
>      its contributors may be used to endorse or promote products derived
>      from this software without specific prior written permission.
>
>    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
>    "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
>    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
>    PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
>    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
>    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
>    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
>    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
>    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
>    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
>    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
>
> L.5) The JOGL source tree also contains a Java port of Brian Paul's Tile
>      Rendering library, used with permission of the author under the
>      New BSD 3-clause license instead of the original LGPL:
>
>    Copyright (c) 1997-2005 Brian Paul. All Rights Reserved.
>
>    Redistribution and use in source and binary forms, with or without
>    modification, are permitted provided that the following conditions are
>    met:
>
>    - Redistribution of source code must retain the above copyright
>      notice, this list of conditions and the following disclaimer.
>
>    - Redistribution in binary form must reproduce the above copyright
>      notice, this list of conditions and the following disclaimer in the
>      documentation and/or other materials provided with the distribution.
>
>    Neither the name of Brian Paul or the names of contributors may be
>    used to endorse or promote products derived from this software
>    without specific prior written permission.
>
>    This software is provided "AS IS," without a warranty of any
>    kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
>    WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
>    FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
>    EXCLUDED. THE COPYRIGHT HOLDERS AND CONTRIBUTORS SHALL NOT BE
>    LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
>    MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO
>    EVENT WILL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY
>    LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
>    CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
>    REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
>    INABILITY TO USE THIS SOFTWARE, EVEN IF THE COPYRIGHT HOLDERS OR
>    CONTRIBUTORS HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
>
> A.1) The JOGL source tree also contains header files from Khronos,
>      reflecting OpenKODE, EGL, OpenGL ES1, OpenGL ES2 and OpenGL.
>
>    http://www.khronos.org/legal/license/
>
>    Files:
>      make/stub_includes/opengl/**
>      make/stub_includes/egl/**
>      make/stub_includes/khr/**
>      make/stub_includes/openmax/**
>
>    Copyright (c) 2007-2010 The Khronos Group Inc.
>
>    Permission is hereby granted, free of charge, to any person obtaining a
>    copy of this software and/or associated documentation files (the
>    "Materials"), to deal in the Materials without restriction, including
>    without limitation the rights to use, copy, modify, merge, publish,
>    distribute, sublicense, and/or sell copies of the Materials, and to
>    permit persons to whom the Materials are furnished to do so, subject to
>    the following conditions:
>
>    The above copyright notice and this permission notice shall be included
>    in all copies or substantial portions of the Materials.
>
>    THE MATERIALS ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
>    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
>    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
>    IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
>    CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
>    TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
>    MATERIALS OR THE USE OR OTHER DEALINGS IN THE MATERIALS.
>
>
> A.2) The JOGL source tree contains code from The Apache Software Foundation
>      which is covered by the Apache License Version 2.0
>
>    Apache Harmony - Open Source Java SE
>    =====================================
>
>    <http://harmony.apache.org/>
>
>    Author: The Apache Software Foundation (http://www.apache.org/).
>
>    Copyright 2006, 2010 The Apache Software Foundation.
>
>    Apache License Version 2.0, January 2004
>    http://www.apache.org/licenses/LICENSE-2.0
>    Or within this repository: doc/licenses/Apache.LICENSE-2.0
>
>    Files:
>     src/jogamp/graph/geom/plane/AffineTransform.java
>     src/jogamp/graph/geom/plane/IllegalPathStateException.java
>     src/jogamp/graph/geom/plane/NoninvertibleTransformException.java
>     src/jogamp/graph/geom/plane/PathIterator.java
>     src/jogamp/graph/geom/plane/Path2D.java
>     src/jogamp/graph/math/plane/Crossing.java
>     src/org/apache/harmony/misc/HashCode.java
>
>
> A.3) The JOGL source tree contains code from David Schweinsberg
>      which is covered by the Apache License Version 1.1 and Version 2.0
>
>    Typecast
>    ========
>
>    Typecast is a font development environment for OpenType font technology.
>
>    <https://java.net/projects/typecast>
>
>    Author: David Schweinsberg
>
>    Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
>
>    Apache Licenses
>    http://www.apache.org/licenses/
>
>    Apache License Version 1.1
>    http://www.apache.org/licenses/LICENSE-1.1
>    Or within this repository: doc/licenses/Apache.LICENSE-1.1
>    Files:
>     src/jogl/classes/jogamp/graph/font/typecast/ot/*
>     src/jogl/classes/jogamp/graph/font/typecast/ot/table/*
>
>    Apache License Version 2.0
>    http://www.apache.org/licenses/LICENSE-2.0
>    Or within this repository: doc/licenses/Apache.LICENSE-2.0
>     src/jogl/classes/jogamp/graph/font/typecast/ot/*
>     src/jogl/classes/jogamp/graph/font/typecast/ot/mac/*
>     src/jogl/classes/jogamp/graph/font/typecast/ot/table/*
>     src/jogl/classes/jogamp/graph/font/typecast/tt/engine/*
>
> A.4) The JOGL source tree contains fonts from Ubuntu
>      which is covered by the UBUNTU FONT LICENCE Version 1.0
>
>    Ubuntu Font Family
>    ==================
>
>    The Ubuntu Font Family are libre fonts funded by Canonical Ltd on behalf of the Ubuntu project.
>
>    <http://font.ubuntu.com/>
>
>    Copyright 2010 Canonical Ltd.
>    Licensed under the Ubuntu Font Licence 1.0
>
>    Author: Canonical Ltd., Dalton Maag
>
>    UBUNTU FONT LICENCE
>    Version 1.0
>    http://font.ubuntu.com/ufl/ubuntu-font-licence-1.0.txt
>    Or within this repository: doc/licenses/ubuntu-font-licence-1.0.txt
>
>    Files:
>     src/jogamp/graph/font/fonts/ubuntu/*
>
> A.5) The JOGL source tree also contains header files from NVIDIA,
>      reflecting Cg.
>
>    Files:
>      make/stub_includes/cg/CG/**
>
>    Copyright (c) 2002, NVIDIA Corporation
>
>    NVIDIA Corporation("NVIDIA") supplies this software to you in consideration
>    of your agreement to the following terms, and your use, installation,
>    modification or redistribution of this NVIDIA software constitutes
>    acceptance of these terms.  If you do not agree with these terms, please do
>    not use, install, modify or redistribute this NVIDIA software.
>
>    In consideration of your agreement to abide by the following terms, and
>    subject to these terms, NVIDIA grants you a personal, non-exclusive license,
>    under NVIDIA's copyrights in this original NVIDIA software (the "NVIDIA
>    Software"), to use, reproduce, modify and redistribute the NVIDIA
>    Software, with or without modifications, in source and/or binary forms;
>    provided that if you redistribute the NVIDIA Software, you must retain the
>    copyright notice of NVIDIA, this notice and the following text and
>    disclaimers in all such redistributions of the NVIDIA Software. Neither the
>    name, trademarks, service marks nor logos of NVIDIA Corporation may be used
>    to endorse or promote products derived from the NVIDIA Software without
>    specific prior written permission from NVIDIA.  Except as expressly stated
>    in this notice, no other rights or licenses express or implied, are granted
>    by NVIDIA herein, including but not limited to any patent rights that may be
>    infringed by your derivative works or by other works in which the NVIDIA
>    Software may be incorporated. No hardware is licensed hereunder.
>
>    THE NVIDIA SOFTWARE IS BEING PROVIDED ON AN "AS IS" BASIS, WITHOUT
>    WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING
>    WITHOUT LIMITATION, WARRANTIES OR CONDITIONS OF TITLE, NON-INFRINGEMENT,
>    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR ITS USE AND OPERATION
>    EITHER ALONE OR IN COMBINATION WITH OTHER PRODUCTS.
>
>    IN NO EVENT SHALL NVIDIA BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL,
>    EXEMPLARY, CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOST
>    PROFITS; PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
>    PROFITS; OR BUSINESS INTERRUPTION) OR ARISING IN ANY WAY OUT OF THE USE,
>    REPRODUCTION, MODIFICATION AND/OR DISTRIBUTION OF THE NVIDIA SOFTWARE,
>    HOWEVER CAUSED AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING
>    NEGLIGENCE), STRICT LIABILITY OR OTHERWISE, EVEN IF NVIDIA HAS BEEN ADVISED
>    OF THE POSSIBILITY OF SUCH DAMAGE.
>
> A.6) The JOGL source tree contains code from Hernan J. Gonzalez and Shawn Hartsock
>      which is covered by the Apache License Version 2.0
>
>    PNGJ
>    ====
>
>    PNGJ: Java library for reading and writing PNG images.
>
>    Version 1.12  (3 Dec 2012)
>
>    <http://code.google.com/p/pngj/>
>
>    Author: Hernan J. Gonzalez and Shawn Hartsock
>
>    Copyright (C) 2004 The Apache Software Foundation. All rights reserved.
>
>    Apache Licenses
>    http://www.apache.org/licenses/
>
>    Apache License Version 2.0
>    http://www.apache.org/licenses/LICENSE-2.0
>    Or within this repository: doc/licenses/Apache.LICENSE-2.0
>     src/jogl/classes/jogamp/opengl/util/pngj/**
>
>
> A.7) The JOGL source tree _may_ contain code from Oculus VR, Inc.
>      which is covered by it's own permissive Oculus VR Rift SDK Software License.
>
>     This code _can_ be included to produce a binding
>     and hence support for the Oculus VR Rift.
>
>     The code is included _and_ it's build artifacts will be released,
>     if the git sub-module oculusvr-sdk is included in the jogl source repository
>     as true for current official JogAmp builds and releases!
>
>     If using JogAmp JOGL builds with oculusvr-sdk support,
>     but the user prefers to _not_ use it for license or other reasons,
>     the user can simply remove the artifacts 'jar/atomics/oculusvr*jar'.
>     No other produced artifact is affected.
>
>     While the Oculus VR Rift SDK Software License is permissive,
>     it's differences to the New BSD license shall be mentioned, see below!
>
>     +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
>
>     Copyright Â© 2014 Oculus VR, Inc. All rights reserved.
>
>     Oculus VR, Inc. Software Development Kit License Agreement
>
>     Human-Readable Summary:
>
>      - You are Free to:
>
>         - Use, modify, and distribute the Oculus VR Rift SDK in source and binary
>           form with your applications/software.
>
>      - With the Following Restrictions:
>
>         - You can only distribute or re-distribute the source code to LibOVR in
>           whole, not in part.
>
>         - Modifications to the Oculus VR Rift SDK in source or binary form must
>           be shared with Oculus VR.
>
>         - If your applications cause health and safety issues, you may lose your
>           right to use the Oculus VR Rift SDK, including LibOVR.
>
>         - The Oculus VR Rift SDK may not be used to interface with unapproved commercial
>           virtual reality mobile or non-mobile products or hardware.
>
>      - This human-readable Summary is not a license. It is simply a convenient
>        reference for understanding the full Oculus VR Rift SDK License Agreement.
>        The Summary is written as a user-friendly interface to the full Oculus VR Rift
>        SDK License below. This Summary itself has no legal value, and its contents do
>        not appear in the actual license.
>
>        Full-length Legal Copy may be found at:
>          http://www.oculusvr.com/licenses/LICENSE-3.1
>          http://jogamp.org/git/?p=oculusvr-sdk.git;a=blob;f=LICENSE.txt;hb=HEAD
>          Or within this repository: oculusvr-sdk/LICENSE.txt


> The GlueGen source code is mostly licensed under the New BSD 2-clause license,
> however it contains other licensed material as well.
>
> Other licensed material is compatible with the 'New BSD 2-Clause License',
> if not stated otherwise.
>
> 'New BSD 2-Clause License' incompatible materials are optional, they are:
>
>     NONE
>
> Below you find a detailed list of licenses used in this project.
>
> +++
>
> The content of folder 'make/lib' contains build-time only
> Java binaries (JAR) to ease the build setup.
> Each JAR file has it's corresponding LICENSE file containing the
> source location and license text. None of these binaries are contained in any way
> by the generated and deployed GlueGen binaries.
>
> +++
>
> L.1) The GlueGen source tree contains code from the JogAmp Community
>      which is covered by the Simplified BSD 2-clause license:
>
>    Copyright 2010 JogAmp Community. All rights reserved.
>
>    Redistribution and use in source and binary forms, with or without modification, are
>    permitted provided that the following conditions are met:
>
>       1. Redistributions of source code must retain the above copyright notice, this list of
>          conditions and the following disclaimer.
>
>       2. Redistributions in binary form must reproduce the above copyright notice, this list
>          of conditions and the following disclaimer in the documentation and/or other materials
>          provided with the distribution.
>
>    THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
>    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
>    FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
>    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
>    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
>    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
>    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
>    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
>    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
>
>    The views and conclusions contained in the software and documentation are those of the
>    authors and should not be interpreted as representing official policies, either expressed
>    or implied, of JogAmp Community.
>
>    You can address the JogAmp Community via:
>        Web                http://jogamp.org/
>        Forum/Mailinglist  http://jogamp.762907.n3.nabble.com/
>        Chatrooms
>          IRC              irc.freenode.net #jogamp
>          Jabber           conference.jabber.org room: jogamp (deprecated!)
>        Repository         http://jogamp.org/git/
>        Email              mediastream _at_ jogamp _dot_ org
>
>
> L.2) The GlueGen source tree contains code from Sun Microsystems, Inc.
>      which is covered by the New BSD 3-clause license:
>
>    Copyright (c) 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
>
>    Redistribution and use in source and binary forms, with or without
>    modification, are permitted provided that the following conditions are
>    met:
>
>    - Redistribution of source code must retain the above copyright
>      notice, this list of conditions and the following disclaimer.
>
>    - Redistribution in binary form must reproduce the above copyright
>      notice, this list of conditions and the following disclaimer in the
>      documentation and/or other materials provided with the distribution.
>
>    Neither the name of Sun Microsystems, Inc. or the names of
>    contributors may be used to endorse or promote products derived from
>    this software without specific prior written permission.
>
>    This software is provided "AS IS," without a warranty of any kind. ALL
>    EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
>    INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
>    PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
>    MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
>    ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
>    DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
>    ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
>    DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
>    DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
>    ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
>    SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
>
>    You acknowledge that this software is not designed or intended for use
>    in the design, construction, operation or maintenance of any nuclear
>    facility.
>
> L.3) The GlueGen source tree contains CGRAM http://www.antlr.org/grammar/cgram/,
>      a ANSI-C parser implementation using ANTLR, which is being used
>      in the compile time part only.
>      It is covered by the Original BSD 4-clause license:
>
>     Copyright (c) 1998-2000, Non, Inc.
>     All rights reserved.
>
>     Redistribution and use in source and binary forms, with or without
>     modification, are permitted provided that the following conditions are met:
>
>         Redistributions of source code must retain the above copyright
>         notice, this list of conditions, and the following disclaimer.
>
>         Redistributions in binary form must reproduce the above copyright
>         notice, this list of conditions, and the following disclaimer in
>         the documentation and/or other materials provided with the
>         distribution.
>
>         All advertising materials mentioning features or use of this
>         software must display the following acknowledgement:
>
>             This product includes software developed by Non, Inc. and
>             its contributors.
>
>         Neither name of the company nor the names of its contributors
>         may be used to endorse or promote products derived from this
>         software without specific prior written permission.
>
>     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS
>     IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
>     THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
>     PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COMPANY OR CONTRIBUTORS BE
>     LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
>     CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
>     SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
>     INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
>     CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
>     ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
>     POSSIBILITY OF SUCH DAMAGE.
>
> A.1) The GlueGen source tree contains code from The Apache Software Foundation
>      which is covered by the Apache License Version 2.0
>
>    Apache Harmony - Open Source Java SE
>    =====================================
>
>    <http://harmony.apache.org/>
>
>    Author: The Apache Software Foundation (http://www.apache.org/).
>
>    Copyright 2006, 2010 The Apache Software Foundation.
>
>    Apache License Version 2.0, January 2004
>    http://www.apache.org/licenses/LICENSE-2.0
>    Or within this repository: doc/licenses/Apache.LICENSE-2.0
>
>    Files:
>     - src/java/com/jogamp/common/net/Uri.java
>       (derived from java.net.URI.Helper and heavily modified)
>
> A.2) The GlueGen source tree contains code from Ben Mankin, a.k.a 'Shevek',
>      which is covered by the Apache License Version 2.0
>
>    JCPP - A Java C Preprocessor
>    =============================
>
>    <http://www.anarres.org/projects/jcpp/>
>    <https://github.com/shevek/jcpp>
>
>    Author: Ben Mankin, a.k.a 'Shevek' (http://www.anarres.org/about/).
>
>    Copyright (c) 2007-2008, Shevek
>
>    Apache License Version 2.0, January 2004
>    http://www.apache.org/licenses/LICENSE-2.0
>    Or within this repository: doc/licenses/Apache.LICENSE-2.0
>
>    Files:
>     The complete git submodule 'jcpp',
>     which is a patched version of the original mentioned above.
>
>    Used for the compile-time module gluegen.jar only.


### Matrix3D

For 3D matrix operations, NetLogo uses the Matrix3D class.  It is
distributed under the following license:

> Copyright (c) 1994-1996 Sun Microsystems, Inc. All Rights Reserved.
>
> Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
> modify and redistribute this software in source and binary code form,
> provided that i) this copyright notice and license appear on all copies of
> the software; and ii) Licensee does not utilize the software in a manner
> which is disparaging to Sun.
>
> This software is provided "AS IS," without a warranty of any kind. ALL
> EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
> IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
> NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
> LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
> OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
> LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
> INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
> CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
> OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
> POSSIBILITY OF SUCH DAMAGES.
>
> This software is not designed or intended for use in on-line control of
> aircraft, air traffic, aircraft navigation or aircraft communications; or in
> the design, construction, operation or maintenance of any nuclear
> facility. Licensee represents and warrants that it will not use or
> redistribute the Software for such purposes.

### ASM

For Java bytecode generation, NetLogo uses the ASM library.  It is
distributed under the following license:

> Copyright (c) 2000-2010 INRIA, France Telecom  
> All rights reserved.
>
> Redistribution and use in source and binary forms, with or without
> modification, are permitted provided that the following conditions
> are met:
>
> 1. Redistributions of source code must retain the above copyright
>    notice, this list of conditions and the following disclaimer.
> 2. Redistributions in binary form must reproduce the above copyright
>    notice, this list of conditions and the following disclaimer in the
>    documentation and/or other materials provided with the distribution.
> 3. Neither the name of the copyright holders nor the names of its
>    contributors may be used to endorse or promote products derived from
>    this software without specific prior written permission.
>
> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
> AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
> IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
> ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
> LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
> CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
> SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
> INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
> CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
> ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
> THE POSSIBILITY OF SUCH DAMAGE.

### Log4j

For logging, NetLogo uses the Log4j library.  The copyright and license
for the library are as follows:

> Copyright 2007 The Apache Software Foundation
>
> Licensed under the Apache License, Version 2.0 (the "License"); you
> may not use this file except in compliance with the License.  You may
> obtain a copy of the License at
> http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.

### PicoContainer

For dependency injection, NetLogo uses the PicoContainer library.  The
copyright and license for the library are as follows:

> Copyright (c) 2003-2006, PicoContainer Organization  
> All rights reserved.
>
> Redistribution and use in source and binary forms, with or without
> modification, are permitted provided that the following conditions are met:
>
> - Redistributions of source code must retain the above copyright notice, this
>   list of conditions and the following disclaimer.
> - Redistributions in binary form must reproduce the above copyright notice,
>   this list of conditions and the following disclaimer in the documentation
>   and/or other materials provided with the distribution.
> - Neither the name of the PicoContainer Organization nor the names of its
>   contributors may be used to endorse or promote products derived from this
>   software without specific prior written permission.
>
> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
> AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
> IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
> ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
> LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
> CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
> SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
> INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
> CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
> ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
> POSSIBILITY OF SUCH DAMAGE.

### Pegdown & Parboiled

For the Info tab, NetLogo uses the Pegdown and Parboiled libraries.

The copyright and license for Pegdown are as follows:

> pegdown - Copyright (C) 2010-2011 Mathias Doenitz
>
> Based on peg-markdown - markdown in c, implemented using PEG grammar
> Copyright (c) 2008 John MacFarlane (http://github.com/jgm/peg-markdown)
>
> pegdown is released under the Apache License 2.0.
> (http://www.apache.org/licenses/LICENSE-2.0)

The copyright and license for Parboiled are as follows:

> parboiled - Copyright (C) 2009-2011 Mathias Doenitz
>
> This product includes software developed by
> Mathias Doenitz (http://www.parboiled.org/).
>
> pegdown is released under the Apache License 2.0.
> (http://www.apache.org/licenses/LICENSE-2.0)

### RSyntaxTextArea

The NetLogo editor uses the RSyntaxTextArea library.

The copyright and license for RSyntaxTextArea are as follows:

> Copyright (c) 2012, Robert Futrell
> All rights reserved.
>
> Redistribution and use in source and binary forms, with or without
> modification, are permitted provided that the following conditions are met:
>     * Redistributions of source code must retain the above copyright
>       notice, this list of conditions and the following disclaimer.
>     * Redistributions in binary form must reproduce the above copyright
>       notice, this list of conditions and the following disclaimer in the
>       documentation and/or other materials provided with the distribution.
>     * Neither the name of the author nor the names of its contributors may
>       be used to endorse or promote products derived from this software
>       without specific prior written permission.
>
> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
> ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
> WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
> DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
> DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
> (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
> LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
> ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
> (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
> SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


### Apache HTTPClient and JSON.simple

For the Modeling Commons functionality, Netlogo uses the Apache HTTPClient and JSON.simple libraries.

The copyright and license for Apache HTTPClient are as follows:

> Copyright (c) 1999-2015 The Apache Software Foundation. All Rights Reserved.
>
> Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
>
>     http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

The copyright and license for JSON.simple are as follows:

> Copyright (c) Yidong Fang
>
> Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
>
>     http://www.apache.org/licenses/LICENSE-2.0
>
> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

### JCodec

The NetLogo vid extension makes use of the JCodec library.
The copyright and license for JCodec are as follows:

>  Redistribution  and  use  in   source  and   binary   forms,  with  or  without
>  modification, are permitted provided  that the following  conditions  are  met:
>
>  Redistributions of  source code  must  retain the above  copyright notice, this
>  list of conditions and the following disclaimer. Redistributions in binary form
>  must  reproduce  the above  copyright notice, this  list of conditions  and the
>  following disclaimer in the documentation and/or other  materials provided with
>  the distribution.
>
>  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
>  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,  BUT NOT LIMITED TO, THE  IMPLIED
>  WARRANTIES  OF  MERCHANTABILITY  AND  FITNESS  FOR  A  PARTICULAR  PURPOSE  ARE
>  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
>  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES
>  (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF SUBSTITUTE GOODS  OR SERVICES;
>  LOSS OF USE, DATA, OR PROFITS;  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
>  ANY  THEORY  OF  LIABILITY,  WHETHER  IN  CONTRACT,  STRICT LIABILITY,  OR TORT
>  (INCLUDING  NEGLIGENCE OR OTHERWISE)  ARISING IN ANY WAY OUT OF THE USE OF THIS
>  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

### Guava

The NetLogo ls extension makes use of the Guava library.

Guava is released under the Apache License 2.0 (<a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">http://www.apache.org/licenses/LICENSE-2.0</a>)

### Gephi

The nw extension makes use of the Gephi library.
Gephi is licensed under the following terms:

>  Gephi Dual License Header and License Notice
>
>  The Gephi Consortium elects to use only the GNU General Public License version 3 (GPL) for any software where a choice of GPL license versions are made available with the language indicating that GPLv3 or any later version may be used, or where a choice of which version of the GPL is applied is unspecified.
>
>  For more information on the license please see: the Gephi License FAQs.
>
>  License headers are available on http://www.opensource.org/licenses/CDDL-1.0 and http://www.gnu.org/licenses/gpl.html.

