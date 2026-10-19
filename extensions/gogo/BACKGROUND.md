# NetLogoLab and the GoGo Board Extension

## What is NetLogoLab?

NetLogoLab is the technological infrastructure that connects NetLogo and the physical world. It can be used for robotics, interactive art, scientific investigations, and model validation. This infrastructure was created at the CCL by [Paulo Blikstein](http://www.blikstein.com/paulo) and Uri Wilensky in 2005 as part of the [Bifocal Modeling](https://tltlab.org/bifocal-modeling/) project. For more information, please check the new and [old](http://ccl.northwestern.edu/netlogolab/) websites, where you will find academic papers, models, and demos.

NetLogoLab is comprised of the following software and hardware components:

1.  A NetLogo extension to control a robotics or data-logging board.
2.  A robotics or data-logging board (also know as a serial interface board, or analog-to-digital board).
3.  Sensor and actuator toolkits.
4.  NetLogo models.

NetLogo's robotics/data-logging board of choice is the [GoGo Board](http://www.gogoboard.org/), an open-source, easy-to-build, low-cost interface designed by [Arnan Sipitakiat](http://alumni.media.mit.edu/~arnans/) and [Paulo Blikstein](http://www.blikstein.com/paulo), first at the MIT Media Lab, then at Northwestern's CCL, and is in continuous refinement. Other robotics hardware can be used with NetLogo, including those that are commercially available, such as [Arduino](http://www.arduino.cc/) boards, [Vernier](http://www.vernier.com/) and [Pasco](http://www.pasco.com/) sensors and actuators, [Phidgets](http://www.phidgets.com/), and [VEX](http://www.vexrobotics.com/) kits, but specific extensions have not yet been developed for each of those platforms. So far, only the GoGo Board extension is available with NetLogo's standard distribution.

### The GoGo Board NetLogo extension

The GoGo Extension for NetLogo provides primitives to communicate with a GoGo Board. This enables the user to connect NetLogo with the physical world using sensors, motors, light bulbs, LEDs, relays and other devices.

### GoGo Board: a low-cost robotics and data-logging board

A [GoGo Board](http://www.gogoboard.org/) is a low cost, general purpose serial interface board especially designed to be used in school and for educational projects. It was created by [Arnan Sipitakiat](http://alumni.media.mit.edu/~arnans/) and [Paulo Blikstein](http://www.blikstein.com/paulo) at the [MIT Media Lab](http://www.media.mit.edu/) in 2001, and has been actively developed since then. It is currently used in over 10 countries, such as the United States, China, Thailand, South Korea, Brazil, Portugal, Mexico, Malaysia, and Egypt. For more information see the [the gogo board about page](https://gogoboard.org/about/).

Up to 8 sensors (i.e., temperature, light, pressure) and 4 output devices (i.e., motors, light bulbs, LEDs, relays) can be connected to the board simultaneously. The board also has a connector for add-on devices (such as displays, Bluetooth or ZigBee wireless modules, voice recorders, real-time clock, and GPS).

### Sensor and actuator toolkits

NetLogo models can interact with the physical world in two ways. First, they can gather data from the environment. This information can be used by the model to change or calibrate its behavior. This data is gathered using electronic sensors, which can measure a wide range of phenomena: temperature, light, touch (see pictures below), pH, chemical concentration, pressure, etc. See the [Gogo docs here](https://docs.gogoboard.org/#/en/sensor/sensor-set) for info on the the sensors that are included with a GoGo board kit.

The second mode of interaction between NetLogo and the physical world is the control of output devices, or "actuators" - motors, light bulbs (see pictures below), LEDs, and more complex devices that include these outputs such as toys, remote controlled cars, electrical appliances, and automated laboratory equipment. See the [Using Output Ports](https://docs.gogoboard.org/#/en/04-output) section of the GoGo docs for more info.


The GoGo board also comes with [Terminal Connectors](https://docs.gogoboard.org/#/en/sensor/sensor-set?id=terminal-connector-) to wire in additional sensor that don't come included. Additional sensors and actuators can be found through online retailers such as [Digikey](http://www.digikey.com/), [Mouser](http://www.mouser.com/), [Phidgets](http://www.phidgets.com/), [Spark Fun](http://www.sparkfun.com/), and [Solarbotics](http://www.solarbotics.com/).

### NetLogo models

To make use of the GoGo Board extension and the NetLogoLab framework, users need to create NetLogo models using the special primitives made available by the extension. Later in this document, we will provide examples of models that do this.

## How to get a GoGo Board?

Gogo Boards can be purchased from the [SEED Foundation](https://www.seeedstudio.com/GoGo-Board-Kit-p-2717.html).

## Installing and testing the GoGo Extension

The GoGo Board connects with the computer via the USB port. Turn the GoGo Board on using the switch behind the power connector: the board will beep twice and the red light will turn on.

### Windows

Plug in your GoGo board and go to [https://code.gogoboard.org/](https://code.gogoboard.org/). The website should prompt you to download the gogo plugin for Windows. You can now try using the NetLogo GoGo extension.

### Mac OS X

The GoGo extension requires no special installation on Mac OS X.

### Linux

Many versions of Linux require no special installation.
