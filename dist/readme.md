# NetLogo @@@VERSION@@@

@@@DATE@@@

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

You may be able to just double-click `netlogo.sh` in your file manager.
Or, from the command line, typical Unix shell commands would be:

    $ cd @@@UNIXNAME@@@
    $ ./netlogo.sh

A Desktop Entry file is also available, so you can link to it from
your desktop. Try using the following commands:
    $ cd @@@UNIXNAME@@@
    $ ln -s netlogo.desktop ~/Desktop/

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
Copyright (C) 1999-2015 Uri Wilensky

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
Copyright (C) 1999-2015 Uri Wilensky

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

### MRJAdapter

This software uses the MRJ Adapter library:

> Copyright (c) 2003-2005 Steve Roy <sroy@roydesign.net>.  The library
> is covered by the Artistic License.  MRJ Adapter is available from
> https://mrjadapter.dev.java.net/ .

### Quaqua

This software uses the Quaqua Look and Feel library:

> Copyright (c) 2003-2005 Werner Randelshofer,
> http://www.randelshofer.ch/, werner.randelshofer@bluewin.ch, All
> Rights Reserved.  The library is covered by the GNU LGPL (Lesser
> General Public License).  This license is available online from
> http://www.gnu.org/copyleft/lesser.html and is also included with
> every download of NetLogo (in the "docs" folder).

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

For 3D graphics rendering, NetLogo uses JOGL, a Java API for OpenGL.
For more information about JOGL, see http://jogl.dev.java.net/.
The library is distributed under the BSD license:

> Copyright (c) 2003-2006 Sun Microsystems, Inc. All Rights Reserved.
>
> Redistribution and use in source and binary forms, with or without
> modification, are permitted provided that the following conditions are
> met:
>
> - Redistribution of source code must retain the above copyright
>   notice, this list of conditions and the following disclaimer.
> - Redistribution in binary form must reproduce the above copyright
>   notice, this list of conditions and the following disclaimer in the
>   documentation and/or other materials provided with the distribution.
>
> Neither the name of Sun Microsystems, Inc. or the names of
> contributors may be used to endorse or promote products derived from
> this software without specific prior written permission.
>
> This software is provided "AS IS," without a warranty of any kind. ALL
> EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
> INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
> PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
> MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
> ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
> DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
> ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
> DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
> DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
> ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
> SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
>
> You acknowledge that this software is not designed or intended for use
> in the design, construction, operation or maintenance of any nuclear
> facility.

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
