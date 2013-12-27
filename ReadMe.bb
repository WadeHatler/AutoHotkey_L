
[size=120][color=#0000FF][b]AutoHotkey_L++ (more like L.0001)[/b][/color][/size]

I have created a small fork of AutoHotkey_L for experimental purposes with several new debugging features. I hope to merge the code back into the main branch after more testing. The REAL AutoHotkey is the [url=https://github.com/Lexikos/AutoHotkey_L]Lexicos Version on GitHub[/url].

All my changes are geared around better debugging, troubleshooting and logging support.  I'm building a product with this so I use it all day every day and it's stable for me.

Please report any issues via the [url=https://github.com/WadeHatler/AutoHotkey_L]GitHub Repository[/url], or the forum thread - or just fork it and fix it ;)

At the moment I only have ANSI builds, but I'll fix the Unicode builds soon (or you are welcome to do so).   

[size=120][color=#0000FF][b]How to Use[/b][/color][/size]
It's easy to try this out because the AutoHotkey installer places a backup copy of the original files in your AutoHotkey folder, so it's really easy to revert back by simply overwriting a file.  If you use the installer, you should find [c]AutoHotkeyA32.exe[/c] and [c]AutoHotkeyW32.exe[/c] in your AutoHotkey folder.  The installer copies one of those files to [c]AutoHotkey.exe[/c] based on which option you choose during installation, but you can switch from one to the other by simply overwriting [c]AutoHotkey.exe[/c] with the version you want (after closing all scripts obviously), or you can run different scripts with different versions by running the specific executable you want for a particular script.

To test this version, you can save it as [c]AutoHotkey.exe[/c] and use it for everything (what I do), or you can save it to a temp file rename it, and then only run it when you want to test my changes.  Either way works fine, and you can always revert by simply copying the correct original file to [c]AutoHotkey.exe[/c].

Download the executable from [url=http://palabraapps.com/AutoHotkey/Wade-Hatler-AutoHotkey_L.001.zip]my web site[/url] and either overwrite [c]AutoHotkey.exe[/c] or save it in a temp folder, rename it and move it into the main AutoHotkey folder.  Whether you keep it as [c]AutoHotkey.exe[/c] or rename it to something like [c]AutoHotkeyBonzai.exe[/c], it should be in the AutoHotkey folder so script variables like [c]A_ScriptDir[/c] work as expected.

[size=120][color=#0000FF][b]Debugging Support[/b][/color][/size]

[size=110][color=#101010][b]Stack Trace for All Exceptions[/b][/color][/size]

All internal exceptions and those thrown using the Exception object, now include a [b].StackTrace[/b] field that shows the stack trace back to the beginning of execution, similar to .NET or Python. For example, this sample calls a label, which calls a function, which throws an internal exception: 

[code=autohotkey]
try {
    Gosub RunMe
    Return
} catch ex {
    CrashException("Uncaught Exception", ex)
}

RunMe:
    CDD("\BOGUS")
    return

CDD(Folder) {
    SetWorkingDir %Directory%
}
[/code]

The exception thrown by [c]SetWorkingDir[/c] looks like: 

[code=autohotkey]
Message: 1
What   : SetWorkingDir
File   : C:\Temp\TestException.ahk
Line   : 16
Stack  :
File                      Line: Context Code
C:\Temp\TestException.ahk   17: CDD()   SetWorkingDir %Directory%
C:\Temp\TestException.ahk   12: RunMe:  CDD("\BOGUS")
C:\Temp\TestException.ahk    5: RunMe:  Gosub,RunMe
[/code]


The first line of the stack trace lists the source line the exception happened on, or sometimes the line before or after depending on how the exception happened. The lines below show the call stack back to the start of the program, with every gosub or function call creating one entry. 
[list=1]
[*] The first two columns are self-explanatory. 
[*] Context is the method or label active at the time of the exception. For example, the last 2 lines are both from the [c]RunMe:[/c] label. Labels end in : and functions end in () 
[*] Code is the first 80 or so characters of the internal AHK version of the code after parsing. It usually matches the code without comments or whitespace. Multi-line statements are concatenated.[/list]

[i]In the examples, CrashException is an AHK Function from my standard AHK Library that just formats the exception nicely and displays it in a debug dialog.  It just produces the message you see in the second box.  This is the pattern I use on all my programs.  I want all exceptions caught and reported, so I have a small header file that inserts the lines above ending with RunMe, and I put that at the top of every AHK script.
[/i]

At the moment, this only works with internally thrown exceptions and exceptions thrown using the [c]Exception[/c] object, so your exceptions you generate yourself in [c]try/catch[/c] blocks should look like: 

[c]    Throw Exception("This is Bogus", -1) [/c]

Instead of 

[c]    Throw "This is bogus"
[/c]
The format of the Stack Trace is suspiciously similar to compiler errors, and you can pretty easily make most code editors take you right to the line. I've done so for [url=http://www.slickedit.com/]Visual SlickEdit[/url], but not for others.  You can probably convert it to any editor format you like with a few lines of AHK code.


[size=110][color=#101010][b]Additional Information in Exceptions with A_LastError[/b][/color][/size]

For internal commands that set [c]A_LastError[/c] (e.g. [c]FileDelete[/c], etc.), a new [c]LastError[/c] field is added to the [c]Exception[/c] object. This field is formatted as [b]ErrorCode = Error Description[/b] where Error Description is obtained from the [url=http://bit.ly/12H3FTI][c]FormatMessage[/c][/url] API call (which is usually but not always right), and [b]ErrorCode[/b] is just a copy of [c]A_LastError[/c]. If you want to process the numeric part, you can just use [c]A_LastError[/c] which will always match the numeric part of the error, or parse the numeric part with a Regex.

For example, if you try to delete a read-only file, it returns an exception object something like: 
[code=autohotkey]

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
[/code]

This one isn't perfect, because I haven't gone back and cleared [c]A_LastError[/c] in all functions that don't use it, so some exceptions will show a nonsense [c]LastError[/c] field from the last error that happened to occur, even if it wasn't in the current statement.  I'll be adding code to clear this in a later release.

[size=110][color=#101010][b]Improvements to ListLines[/b][/color][/size]
I added several improvements to [c]ListLines[/c] because it seldom has enough information to really be useful. This was the first change I made, although now that I've added the stack trace mentioned above I don't use it as much as I once did.

[size=110][color=#101010][b]ListLines, File[/b][/color][/size]

If you pass a file name as the first parameter to [c]ListLines[/c], the lines are dumped directly to the file and the UI never shows up. You can then open the file and process it any way you like.  The command is synchronous, so you can follow it with the command to process the file.  If you want to do more detailed preprocessing of the lines, just write the AHK code to scan through the file and make any changes you like.  See below for formatting details that are useful for processing the file.

[code=autohotkey]
    ListLines, %A_Temp%\ListLines.txt
[/code]

[size=110][color=#101010][b]ListLines, Clear[/b][/color][/size]

You can clear the current line list anytime you like, which is useful after you've loaded up some massive libraries you don't really want in your debug output.

[code=autohotkey]
    ListLines, Clear
[/code]

[size=110][color=#101010][b]A_ListLines[/b][/color][/size]

The new built-in variable [c]A_ListLines[/c] returns the current ListLines state. [b]1=On, 0=Off[/b].  You can use it to save/restore the state.

[code=autohotkey]
wasListing := A_ListLines
ListLines, Off
...
ListLines %wasListing%
[/code]

[size=110][color=#101010][b]ListLines, True/False[/b][/color][/size]
You can now set ListLines state with True/False in addition to "On"/"Off".  This makes it easier to save/restore the state of ListLines.


[size=110][color=#101010][b]ListLines Cleanup [/b][/color][/size]
I added some features to make the lines easier to read and process.  
[list=1]
[*] Line numbers always have four digits so they line up well for readability. 
[*] The total number of lines that can show up in a file has been increased from 400 to 4000. Note that the original limit of 400 was caused by the ListLines GUI, so the GUI still only shows 400 lines.
[*] Filenames in the list have a [c];[/c] at the start. If you list them to a file and open them in an editor with AHK support, they'll show up as comments, which makes them stand out more.  Of course the lines themselves are just AHK code so they'll be color coded as well. [/list]
