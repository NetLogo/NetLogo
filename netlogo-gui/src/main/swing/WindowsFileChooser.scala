// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import com.sun.jna.{ Library, Native, Pointer, WString }
import com.sun.jna.platform.win32.{ Guid, Ole32, User32, W32Errors, WinNT, WTypes }
import com.sun.jna.platform.win32.COM.Unknown
import com.sun.jna.platform.win32.WinDef.{ DWORD, DWORDByReference }
import com.sun.jna.ptr.PointerByReference

import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

// this looks sketchy but is actually robust because these GUIDs are
// hard-coded into the Windows kernel (Isaac B 7/10/25)
private object GUID {
  val IID_IShellItem = new Guid.IID("43826D1E-E718-42EE-BC55-A1E261C37BFE")
  val CLSID_FileOpenDialog = new Guid.CLSID("DC1C5A9C-E88A-4dde-A5A1-60F82A20AEF7")
  val IID_IFileOpenDialog = new Guid.IID("D57C7288-D4AD-4768-BE02-9D969532D960")
  val CLSID_FileSaveDialog = new Guid.CLSID("C0B4E2F3-BA21-4773-8DBA-335EC946EB8B")
  val IID_IFileSaveDialog = new Guid.IID("84BCCD23-5FDE-4CDB-AEA4-AF64B83D78AB")
}

// shell32 is loaded to provide a helper function for converting a String file path to an IShellItem,
// which is an internal object representation required by Windows (Isaac B 7/10/25)
private object Shell32 {
  lazy val INSTANCE = Native.load("shell32", classOf[Shell32]).asInstanceOf[Shell32]
}

private trait Shell32 extends Library {
  def SHCreateItemFromParsingName(path: WString, ctx: Pointer, iid: Guid.REFIID,
                                  pointer: PointerByReference): WinNT.HRESULT
}

private class IShellItem(pointer: Pointer) extends Unknown(pointer) {
  // 0x80058000 means this method should return a filesystem path if it exists (Isaac B 7/10/25)
  def getDisplayName(path: PointerByReference): WinNT.HRESULT =
    _invokeNativeObject(5, Array(pointer, 0x80058000, path), classOf[WinNT.HRESULT]).asInstanceOf[WinNT.HRESULT]
}

// this is a slightly modified mirror of the Win32 IFileDialog interface, which applies to both open
// and save file dialogs. the first argument to _invokeNativeObject is the index in the Win32 IFileDialog
// vtable, found in ShObjIdl_core.h (Isaac B 7/10/25)
private class IFileDialog(pointer: Pointer) extends Unknown(pointer) {
  def setTitle(title: String): WinNT.HRESULT =
    _invokeNativeObject(17, Array(pointer, new WString(title)), classOf[WinNT.HRESULT]).asInstanceOf[WinNT.HRESULT]

  def setFileName(file: String): WinNT.HRESULT =
    _invokeNativeObject(15, Array(pointer, new WString(file)), classOf[WinNT.HRESULT]).asInstanceOf[WinNT.HRESULT]

  def setDefaultFolder(folder: String): WinNT.HRESULT = {
    val iid = new Guid.REFIID(GUID.IID_IShellItem)
    val shellItem = new PointerByReference

    val result: WinNT.HRESULT = Shell32.INSTANCE.SHCreateItemFromParsingName(new WString(folder), null, iid, shellItem)

    if (!W32Errors.SUCCEEDED(result))
      return result

    _invokeNativeObject(11, Array(pointer, shellItem.getValue), classOf[WinNT.HRESULT]).asInstanceOf[WinNT.HRESULT]
  }

  def setOptions(options: Seq[Long]): WinNT.HRESULT = {
    val optionsPointer = new DWORDByReference

    val result: WinNT.HRESULT = _invokeNativeObject(10, Array(pointer, optionsPointer), classOf[WinNT.HRESULT])
                                  .asInstanceOf[WinNT.HRESULT]

    if (!W32Errors.SUCCEEDED(result))
      return result

    optionsPointer.setValue(new DWORD(optionsPointer.getValue.longValue | options.reduce(_ | _)))

    _invokeNativeObject(9, Array(pointer, optionsPointer), classOf[WinNT.HRESULT])
      .asInstanceOf[WinNT.HRESULT]
  }

  def setFileTypes(fileTypes: Seq[(String, String)]): WinNT.HRESULT = {
    val spec = fileTypes.map { (name, spec) =>
      new FilterSpec {
        setSpec(name, spec)
      }
    }.toArray

    _invokeNativeObject(4, Array(pointer, spec.size, spec), classOf[WinNT.HRESULT])
      .asInstanceOf[WinNT.HRESULT]
  }

  def show(): WinNT.HRESULT =
    _invokeNativeObject(3, Array(pointer, User32.INSTANCE.GetForegroundWindow()), classOf[WinNT.HRESULT])
      .asInstanceOf[WinNT.HRESULT]

  def getResult(item: PointerByReference): WinNT.HRESULT =
    _invokeNativeObject(20, Array(pointer, item), classOf[WinNT.HRESULT]).asInstanceOf[WinNT.HRESULT]

  def release(): Long =
    _invokeNativeObject(2, Array(pointer), classOf[Long]).asInstanceOf[Long]

  def setDefaultExtension(extension: String): WinNT.HRESULT =
    _invokeNativeObject(22, Array(pointer, new WString(extension)), classOf[WinNT.HRESULT]).asInstanceOf[WinNT.HRESULT]
}

class WindowsFileChooser extends NativeFileChooser {
  private var dialog: Option[IFileDialog] = None

  if (!W32Errors.SUCCEEDED(Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED)))
    throw NativeLibraryException("Failed to initialize COM")

  override def showOpenDialog(frame: Frame): Int = {
    val dialogPointer = new PointerByReference

    if (!W32Errors.SUCCEEDED(Ole32.INSTANCE.CoCreateInstance(GUID.CLSID_FileOpenDialog, null,
                                                             WTypes.CLSCTX_INPROC_SERVER, GUID.IID_IFileOpenDialog,
                                                             dialogPointer)))
      throw NativeLibraryException("Failed to create open file dialog")

    val wrapper = new IFileDialog(dialogPointer.getValue)

    dialog = Option(wrapper)

    wrapper.setTitle(title)

    startFile match {
      case Some(file) =>
        wrapper.setFileName(file.toString)

      case _ =>
        startDirectory.foreach(directory => wrapper.setDefaultFolder(directory.toString))
    }

    if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY)
      wrapper.setOptions(Seq(0x20))

    fileTypes.foreach(wrapper.setFileTypes)

    if (W32Errors.SUCCEEDED(wrapper.show())) {
      JFileChooser.APPROVE_OPTION
    } else {
      JFileChooser.CANCEL_OPTION
    }
  }

  override def showSaveDialog(frame: Frame): Int = {
    val dialogPointer = new PointerByReference

    if (!W32Errors.SUCCEEDED(Ole32.INSTANCE.CoCreateInstance(GUID.CLSID_FileSaveDialog, null,
                                                             WTypes.CLSCTX_INPROC_SERVER, GUID.IID_IFileSaveDialog,
                                                             dialogPointer)))
      throw NativeLibraryException("Failed to create save file dialog")

    val wrapper = new IFileDialog(dialogPointer.getValue)

    dialog = Option(wrapper)

    wrapper.setTitle(title)

    startFile match {
      case Some(file) =>
        wrapper.setFileName(file.toString)

      case _ =>
        startDirectory.foreach(directory => wrapper.setDefaultFolder(directory.toString))
    }

    if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY)
      wrapper.setOptions(Seq(0x20))

    fileTypes.foreach(wrapper.setFileTypes)

    defaultExtension.foreach { extension =>
      wrapper.setDefaultExtension(extension)
    }

    if (W32Errors.SUCCEEDED(wrapper.show())) {
      JFileChooser.APPROVE_OPTION
    } else {
      JFileChooser.CANCEL_OPTION
    }
  }

  override def getSelectedFile: File = {
    dialog match {
      case Some(wrapper) =>
        val itemPointer = new PointerByReference

        if (!W32Errors.SUCCEEDED(wrapper.getResult(itemPointer)))
          return null

        val shellItem = new IShellItem(itemPointer.getValue)
        val path = new PointerByReference

        if (!W32Errors.SUCCEEDED(shellItem.getDisplayName(path)))
          return null

        val file = new File(path.getValue.getWideString(0))

        Ole32.INSTANCE.CoTaskMemFree(path.getPointer)

        file

      case _ =>
        null
    }
  }

  override def cleanup(): Unit = {
    dialog.foreach(_.release())
    dialog = None

    Ole32.INSTANCE.CoUninitialize()
  }
}
