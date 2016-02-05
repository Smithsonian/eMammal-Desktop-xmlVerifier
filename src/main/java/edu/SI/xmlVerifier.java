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
 *
 */
package edu.SI;

import java.util.LinkedList;

import javax.swing.*;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.List;
import java.io.*;

public class xmlVerifier{
    /**
     * Checks if the file is valid XML before proceeding with validation
     * @param place1 File location on local file system
     * @param log Log file, located as the same directory as the .jar. Detailed output.
     * @param out Generic output, in this case a JTextarea
     */
	public xmlVerifier(String place1, PrintWriter log, JTextArea out){
		String localPlace;
		String nl = System.getProperty("line.separator");
		if(place1.lastIndexOf("/")==-1) //If file/directory location does not include a "/", as in foo.xml
			localPlace=place1;
		else {
			localPlace = place1.substring(place1.lastIndexOf("/", place1.length() - 1));
		}
		if(place1.length()<4) {
            out.append("Invalid Filename on file " + localPlace + nl);
            log.println(localPlace+","+place1+","+"N"+","+"Invalid filename"+","+"N/A"+","+"N/A"+","+"N/A");
            return;
        }                                       //statements exist to crash the program smoothly
        if(!place1.substring(place1.length()-4, place1.length()).equals(".xml")){
			out.append(localPlace + " has an incorrect file extension. Skipping file " + localPlace + "."+nl);
            log.println(localPlace+","+place1+","+"N"+","+"Invalid file extension"+","+"N/A"+","+"N/A"+","+"N/A");
            return;}
	try {
		Verify(place1, log, out);
	} 			//above exception should not happen ever
	  catch (SAXException e1){
		  out.append("Error reading file " + localPlace + ". It may be corrupt or invalid." + nl);
          log.println(localPlace + "," + place1 + "," + "N" + "," + "Corrupt/Invalid file" + "," + "N/A" + "," + "N/A" + "," + "N/A");
	  }
 /*   catch (Exception e1){                                   //DEBUG PURPOSES ONLY
        StackTraceElement[] e3=e1.getStackTrace();
        for(int x=0;x<e3.length;x++)
            out.append(e3[x].getLineNumber()+e3[x].getMethodName()+" "+e3[x].getClassName());                                                   //XSD Validation
    }*/
	}

    /**
     * Validates manifests against XML schema and schematron.
     * @param place File location on local file system
     * @param log Log file, located as the same directory as the .jar. Detailed output.
     * @param out Generic output, in this case a JTextarea
     * @throws SAXException Thrown if the file given is not valid XML, it should have been caught earlier however.
     */
	public void Verify(String place, PrintWriter log, JTextArea out) throws SAXException
	{
		String line, line2;
        boolean errIsNull=false;
        boolean tron=true;
        boolean sch=false;
        boolean result=true;
        List<String> Error= new LinkedList<String>();
        List<String> Row = new LinkedList<String>();
        List<String> Column = new LinkedList<String>();
        List<String> ErrLoc = new LinkedList<String>();
		final String nl = System.getProperty("line.separator");

		ClassLoader cl = this.getClass().getClassLoader();
		InputStream Schematron = cl.getClass().getResourceAsStream("/Unified_WCSDeploymentManifest.sch");
		InputStream XSD = cl.getClass().getResourceAsStream("/Unified_WCSDeploymentManifest.xsd");  //fetches resources from jar file
        String localPlace = place.substring(place.lastIndexOf("/")+1, place.length());
		File tronFile = convertToFile(Schematron);
		try{
			Process proc = Runtime.getRuntime().exec("java -cp * org.probatron.Driver "+place+" "+tronFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		for(;;){
                line = in.readLine();
                line2 = in.readLine();
            if(!line.contains("xml version=")){
                if(line2.equals(""))
                    break;
                Error.add(tronErr(line2));
                Row.add(tronRow(line));
                Column.add(tronCol(line));
                ErrLoc.add(tronErrLoc(line));
                tron=false;
                }
            }
		while(!errIsNull){
            int count=0;
            count++;
            String Eline = err.readLine();
            if(Eline==null)
                errIsNull=true;
            if(count>1) {
                tron = false;
                out.append("Error: Schematron validation could not be completed. The file may be of an invalid type." + nl);
            }
		}proc.destroy();
		} catch (Exception e){
            log.println(localPlace + "," + place + ",N,Invalid File location,N/A,N/A,N/A");
            log.println(localPlace + "," + place + "," + "N" + "," + "Invalid File location" + "," + "N/A" + "," + "N/A" + "," + "N/A");
		}

		Source xmlFile = new StreamSource(new File(place));
		Source schemaSource = new StreamSource(XSD);
		System.setProperty("java.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		SchemaFactory schemaFactory = SchemaFactory
		    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(schemaSource);
		Validator validator = schema.newValidator();
		final List<SAXParseException> exceptions = new LinkedList<SAXParseException>();
		  validator.setErrorHandler(new ErrorHandler() //Used to count the errors rather than having the validator hit an error and stop
          {
              public void warning(SAXParseException exception) throws SAXException {
                  exceptions.add(exception);
              }

              public void fatalError(SAXParseException exception) throws SAXException {
                  exceptions.add(exception);
              }

              public void error(SAXParseException exception) throws SAXException {
                  exceptions.add(exception);
              }
          });
		  try {
			validator.validate(xmlFile);
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			  out.append(localPlace+": XML Schema Validation failed. Invalid file or file location."+nl);
              log.println(localPlace + "," + place + "," + "N" + "," + "Invalid file or file location" + "," + "N/A" + "," + "N/A" + "," + "N/A");
			  return;
		}
		  if(exceptions.size()==0)
              sch=true;
		  else
              for (int i = 0; i < exceptions.size(); i++) {
                  Error.add(xsdError(exceptions.get(i).toString()));
                  Row.add(xsdRow(exceptions.get(i).toString()));
                  Column.add(xsdCol(exceptions.get(i).toString()));
                  ErrLoc.add("N/A");
                  if (xsdError(exceptions.get(i).toString()) != "")
                      sch = false;
                  //loop returns errors
              }
        if(sch && tron)
            out.append(localPlace+" has PASSED validation."+nl);
        else{
                out.append(localPlace + " has FAILED validation.  Errors have been noted in the log." + nl);
                result=false;
            }
        bustCommas(Error);
        bustCommas(ErrLoc);
        bustCommas(Row);
        bustCommas(Column);
        if(result)
            log.println(localPlace + "," + place + "," + "Y" + "," +"N/A" +  "," +"N/A"+  "," +"N/A"+ ","+"N/A");
         for(int l=0; l<Error.size();l++)
             log.println(localPlace + "," + place + "," + "N" + "," + Error.get(l) + "," + ErrLoc.get(l) + "," + Row.get(l) + "," + Column.get(l));
    }

    /**
     * Converts InputStream of schema to File so it can be processed by third-party program
     * @param in InputStream of schema
     * @return File of schema
     */
	public File convertToFile(InputStream in){
		File tronFile = new File("temp.sch");
		tronFile.deleteOnExit();
		try {
			OutputStream os = new FileOutputStream(tronFile);

			byte[] buffer = new byte[1024];
			int bytesRead;
			//read from in to buffer
			while((bytesRead = in.read(buffer)) !=-1){
				os.write(buffer, 0, bytesRead);
			}
			in.close();
			//flush OutputStream to write any buffered data to file
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}return tronFile;
	}
    /**
     * Fetches column number of error from output
     * @param str the output
     * @return the column number
     */
    String xsdCol(String str){
        return str.substring(str.indexOf(":", str.indexOf("columnNumber"))+1, str.indexOf(";", str.indexOf("columnNumber")));
    }

    /**
     * Puts every item in each List in quotes to prevent comma issues in .csv
     * @param list List that needs to be fool-proofed
     * @return Same List with brand new quotes around each element
     */
    List<String> bustCommas(List<String> list){
        for(int k=0;k<list.size();k++){
            list.set(k,"\""+list.get(k)+"\"");
        }
        return list;
    }
    /**
     * Fetches line number of error from output
     * @param str the output
     * @return the line number
     */
    String xsdRow(String str){
        return str.substring(str.indexOf("lineNumber:")+11,str.indexOf(";", str.indexOf("lineNumber:")-1));
    }
    /**
     * Fetches error summary from output
     * @param str the output
     * @return the summary
     */
    String xsdError(String str){
        return str.substring(str.indexOf(":", str.indexOf("columnNumber:")+14)+1,str.length());
    }
    /**
     * Fetches error summary from output
     * @param str the output
     * @return the summary
     */
    String tronErr(String str){
        String p1=""; String p2="";
        if(str.contains("svrl:text"))
            p2 = str.substring(str.indexOf("svrl:text")+10, str.lastIndexOf("svrl:text")-2);
        if((p1 + p2).equals(""))
            return str;
        return p1+p2;
    }
    /**
     * Fetches column number of error from output
     * @param str the output
     * @return the column number
     */
    String tronCol(String str){
        String result= str.substring(str.indexOf("col=")+4, str.lastIndexOf(">"));
        while(result.contains("\""))
            result=result.substring(0,result.indexOf("\""))+result.substring(result.indexOf("\"")+1, result.length());
        return result;}
    /**
     * Fetches line number of error from output
     * @param str the output
     * @return the line number
     */
    String tronRow(String str){
        String result= str.substring(str.indexOf("line=")+5,str.lastIndexOf("col=")-1);
        while(result.contains("\""))
            result=result.substring(0,result.indexOf("\""))+result.substring(result.indexOf("\"")+1, result.length());
        return result;}

    /**
     * Fetches the location of the error in the file
     * @param str the output
     * @return error location
     */
    String tronErrLoc(String str){
        return str.substring(str.indexOf("location=\"")+10, str.indexOf("\"", str.indexOf("location=\"")+11));
    }

}
