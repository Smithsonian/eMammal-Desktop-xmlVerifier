This document will serve as the instruction manual to WCS-xmlVerifier-1.X.jar, and eMammal-xmlVerifier-1.X.jar.  Please refer to it if you, the user, have trouble when executing the program.

The program has three steps: input, processing, and output.  

The input phase is underway when the program is first launched, and indicated by the large text area displaying the message:
“Input a file to be verified. Directories must end with a ‘/‘.  Note that if a file has a space, it will automatically fail. Remove any spaces in filenames.”  As well as the smaller area displaying the message “Input Text Here.”
	As a user, you must input the location of a file or folder in your computer in order to progress.  Input the location of the document(s) that you want to submit for verification into the small box.

The processing phase is occurring while the program is still sending out text into the large box.  Note that the smaller box displays the location of the file(s) and is no longer accessible.
At this point, users wait for the program to move onto the output step. The program will do this automatically.

The output phase begins when the words “Verification Finished” appear in the large box.  The large box will stop outputting text at this time.  During the output phase, the program will create a file called “xmlVerifierlog.csv”. This file can be found in the same directory as the program. This file holds the results of the processing phase and marks whether the files are valid or not. This file should be opened in a spreadsheet application like Microsoft Excel, but it is also possible for it to be opened in a work processor.  Spreadsheets are highly recommended, however.

You may now close the program window.  This will terminate the program.  You do not need to wait for the log file to be created, as it was created during the beginning of the output phase, at the same time the words “Verification Finished” were displayed.  If you have trouble finding the program log, look in the same area that the program itself resides in.