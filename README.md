
#AutoHotkey_L++ (more like L.001)

This is a fork of AutoHotkey_L for experimental purposes. I hope to merge the code back into the main branch after testing. The REAL AutoHotkey is the [Lexicos Version](https://github.com/Lexikos/AutoHotkey_L).  See the GitHub Repository for details. 

All of my changes are geared around better debugging, troubleshooting and logging support.  I'm building a product with this so I use it all day every day and it seems completely stable.  

Please report any issues via the [GitHub Repository](https://github.com/WadeHatler/AutoHotkey_L), or just fork it and fix it ;)

At the moment I only have ANSI builds, but I'll fix the Unicode builds sooner or later.  

---


##How to Use
It's easy to try this out because the AutoHotkey installer places a backup copy of the original files, so you don't even have to bother with that.  If you use the installer, you should find `AutoHotkeyA32.exe`, which is the ANSI version of AHK, and if you normally use the ANSI version, you'll find an identical copy in `AutoHotkey.exe`.  Simply overwrite `AutoHotkey.exe` with the file you can download from my web site.  If it doesn't work, you can just replace `AutoHotkey.exe` with the original file anytime (after killing all scripts).


---

#Debugging Support

##Stack Trace for All Exceptions

All internal exceptions and those thrown using the Exception object, now include a **.StackTrace** field. For example, this sample calls a label, which calls a function, which throws an internal exception: 

#include C:\Palabra\AutoHotkey\Lib
#include AhkLib.ahk

try {
    Gosub RunMe
    Return
} catch G_Exception {
    CrashException("Uncaught Exception", G_Exception)
}

RunMe:
    CDD("\BOGUS")
    return

CDD(Folder) {
    SetWorkingDir %Directory%
}

(In this example, `CrashException` is an AHK Function from my standard AHK Library that just formats the exception nicely and displays it in a debug dialog).

The actual caught exception thrown by `SetWorkingDir` looks like: 

Message: 1
What   : SetWorkingDir
File   : C:\Temp\TestException.ahk
Line   : 16
Stack  :
File                      Line: Context Code
C:\Temp\TestException.ahk   17: CDD()   SetWorkingDir %Directory%
C:\Temp\TestException.ahk   12: RunMe:  CDD("\BOGUS")
C:\Temp\TestException.ahk    5: RunMe:  Gosub,RunMe


The first line is the line the exception happened on, or sometimes the line before or after depending on how the exception happened. The lines below show the call stack back to the start of the program. 

 * The first two columns are self-explanatory. 

 * Context is the method or label active at the time of the exception. For example, the last 2 lines are both from the `RunMe:` label. Labels end in : and functions end in () 

 * Code is the first 80 or so characters of the internal AHK version of the code after parsing. It usually matches the code without comments or whitespace. Multi-line statements are concatenated.

At the moment, this only works with internally thrown exceptions and exceptions thrown using the `Exception` object, so your internal exceptions you generate yourself in `try/catch` blocks should look like: 

Throw Exception("This is Bogus", -1) 

Instead of 

Throw "This is bogus"

The format of the Stack Trace is suspiciously similar to compiler errors, and you can pretty easily make most code editors take you right to the line. I've done so for [Visual SlickEdit](C:\Palabra\AutoHotkey_L\slickedit.com), but not for others.

##Additional Information in Exceptions with A_LastError

For internal commands that set `A_LastError` (e.g. `FileDelete`, etc.), a new `LastError` field is added to the `Exception` object. This field is formatted as **ErrorCode = Error Description** where Error Description is obtained from the `FormatMessage` API call (which is usually but not always right), and **ErrorCode** is just a copy of `A_LastError`. If you want to process the numeric part, you can easily parse it out with a Regex.

For example, if you try to delete a read-only file, it returns an exception object something like: 

Message  : 1
What     : FileDelete
TestCase : Exception LastError
TestRun  : AhkCompilerTest.asx
File     : C:\Palabra\AutoHotkey_L\Tests\AhkCompilerTest.asx
Line     : 149
LastError: 5=Access is denied.


File                                              Line: Context                    Code
C:\Palabra\AutoHotkey_L\Tests\AhkCompilerTest.asx  148: Test_Exception_LastError() FileSetAttrib,+R,%TestFile%
C:\Palabra\AutoHotkey\Lib\UnitTest.ahk             324: TestRunAll()               Func(functionName).()  
C:\Palabra\AutoHotkey_L\Tests\AhkCompilerTest.asx   23: RunMe:                     TestRunAll()  
C:\Palabra\AutoHotkey\Lib\AhkBase.ahk               13: RunMe:                     Gosub,RunMe

##Improvements to [ListLines](http://l.autohotkey.net/docs/commands/ListLines.htm)
I added several improvements to `ListLines` because it seldom has enough information to really be useful. This was the first change I made, although now that I've added the stack trace mentioned above I don't use it as much as I once did.

###ListLines, File

If you pass a file name as the first parameter to `ListLines`, the lines are dumped directly to the file and the UI never shows up. You can then open the file and process it any way you like.  The command is synchronous, so you can follow it with the command to process the file.  If you want to do more detailed preprocessing of the lines, just write the AHK code to scan through the file and make any changes you like.  See below for formatting details that are useful for processing the file.

    ListLines, %A_Temp%\ListLines.txt

###ListLines, Clear

You can clear the current line list anytime you like, which is useful after you've loaded up some massive libraries you don't really want in your debug output.

    ListLines, Clear

###A_ListLines

The new built-in variable `A_ListLines` returns the current ListLines state. **1=On, 0=Off**.  You can use it to save/restore the state.

wasListing := A_ListLines
ListLines, Off
...
ListLines %wasListing%

###ListLines, True/False
You can now set ListLines state with True/False in addition to "On"/"Off".  This makes it easier to save/restore the state of ListLines.

##ListLines Cleanup 
ListLines had a tremendous amount of noise, so I have removed some of that. 

 * Lines that consist of just `}` are removed (_not so sure about this one)_

 * Line numbers always have four digits so they line up well for readability. 

 * The total number of lines that can show up in a file has been increased from 400 to 4000. Note that the original limit of 400 was caused by the ListLines GUI, so the GUI still only shows 400 lines.

 * Filenames in the list have a `;` at the start.  You can save the current state with `ListLines, File.ahk`, and open the file in your favorite text editor and the files will appear as comments which improve readability, and of course the lines themselves are just AHK code so they'll be color coded as well. 


