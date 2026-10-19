## Usage

The GoGo Extension comes preinstalled when you download and install NetLogo. To use the extension in your model, add this line to the top of your Code tab:

```NetLogo
extensions [ gogo ]
```

If your model already uses other extensions, then it already has an extensions line in it, so just add gogo to the list.

If you are using a GoGo board 6, you will also need to initialize the extension by typing the following command into the command center:

```
gogo:init "gogo6"
```

You can see if the GoGo board is connected by seeing if it will beep when you type into the command center:

```
gogo:beep
```

Or, you can see whether one or more HID-based gogos are on and attached to the computer by typing the following into the command center:

```NetLogo
gogo:howmany-gogos
```

For examples that use the GoGo extension see GoGoMonitor and GoGoMonitorSimple models in the Models Library and the example projects at the bottom of this page.

## Changes

Compared to previous versions of the GoGo extension, this version offers:

- **Improved robustness**.  With prior versions of the GoGo extension, crashes were fairly common due to problems in the USB-Serial stack across platforms.  The switch to HID improved robustness, and the new extension also uses a "daemon" architecture which shields NetLogo from any problems that may occur in direct communication with the GoGo board.  The result is a substantial reduction in the number of crashes of NetLogo.
- **No Installation of Drivers**. Because the new GoGo firmware presents the board as an HID device, the extension could be written so as not to require installing drivers.  This means there is no need for the user to have administrator rights on the computer.
- **Directionality for Motors**. The board now has polarity-ensuring output connectors, so that "counterclockwise" or "clockwise" can now be specified in code.

