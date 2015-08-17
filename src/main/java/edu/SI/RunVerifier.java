/*
 * Copyright 2015 Smithsonian Institution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.You may obtain a copy of
 * the License at: http://www.apache.org/licenses/
 *
 * This software and accompanying documentation is supplied without
 * warranty of any kind. The copyright holder and the Smithsonian Institution:
 * (1) expressly disclaim any warranties, express or implied, including but not
 * limited to any implied warranties of merchantability, fitness for a
 * particular purpose, title or non-infringement; (2) do not assume any legal
 * liability or responsibility for the accuracy, completeness, or usefulness of
 * the software; (3) do not represent that use of the software would not
 * infringe privately owned rights; (4) do not warrant that the software
 * is error-free or will be maintained, supported, updated or enhanced;
 * (5) will not be liable for any indirect, incidental, consequential special
 * or punitive damages of any kind or nature, including but not limited to lost
 * profits or loss of data, on any basis arising from contract, tort or
 * otherwise, even if any of the parties has been warned of the possibility of
 * such loss or damage.
 *
 *
 * This distribution includes several third-party libraries, each with their own
 * license terms. For a complete copy of all copyright and license terms, including
 * those of third-party libraries, please see the product release notes.
 */
package edu.SI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import javax.swing.*;

public class RunVerifier {
	public static void main(String[] args) throws URISyntaxException, IOException{
		PrintWriter log;
		String loc;
		String nl=System.getProperty("line.separator");
		final List<String> holder = new LinkedList<String>();

		final JTextField input = new JTextField();
		JTextArea output = new JTextArea(24,12);
		JFrame console = new JFrame("xmlVerifier");
		JScrollPane scroll = new JScrollPane(output);

		console.setLayout(new BorderLayout());
		console.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		output.setEditable(false);
		output.setLineWrap(true);			//GUI creation and setup
		output.setPreferredSize(new Dimension(1000, 300));
		output.append("Input a file or directory to be verified. Directories must end in a '/'." + nl);
        output.append("Note that if a file has a space, it will automatically fail. Remove any spaces in filenames.");
		console.add(output, BorderLayout.CENTER);

		scroll.setPreferredSize(new Dimension(1000, 300));
		JViewport view = new JViewport();
		view.setView(output);
		scroll.setViewport(view);
		console.add(scroll, BorderLayout.LINE_END);

		input.setPreferredSize(new Dimension(400, 25));
		input.setText("Input text here");
		console.add(input, BorderLayout.PAGE_END);
		console.pack();
		console.setVisible(true);

		input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                synchronized (holder) {                        //ActionEvent is called by user entering File(s) location
                    holder.add(input.getText());
                    holder.notify();
                }
                input.setEnabled(false);
            }
        });

		log = new PrintWriter(new File("xmlVerifierLog.csv"));	//This log will end up in the same directory as the .jar
		log.println("File,Location,Pass(Y/N),Error,ErrorLocation,Row,Column");

		synchronized (holder){
			try {
				while (holder.isEmpty())
					holder.wait();					//Pauses the thread while the user inputs the file to verify.
			} catch (InterruptedException e) {		//is probably magic.
				output.append("System Error."+nl);
			}
		}
		loc=holder.get(0);
		output.setText("");
		output.setAutoscrolls(true);
		if(loc.substring(loc.length()-1, loc.length()).equals("/")){
															//Tests whether the input is a file or directory
				File count = new File(loc);
            if(count.list()==null)
                output.append("There are no files in the specified directory."+nl);
				String[] names = count.list();
			output.setPreferredSize(new Dimension(750, 25*names.length)); //Extends the output field as far as it needs to go
			scroll.setPreferredSize(new Dimension(750, 25*names.length));
				for(int j=names.length-1;j>0;j--){
					names[j]=loc+names[j];
				new xmlVerifier(names[j], log, output);} 	//Multiple files method
			}
			else{
				new xmlVerifier(loc, log, output);  	//Single file method
		}
		log.close();
		output.append("Verification finished.");
	}
}
