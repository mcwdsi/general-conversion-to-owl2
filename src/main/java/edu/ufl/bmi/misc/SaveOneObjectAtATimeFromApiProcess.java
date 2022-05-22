package edu.ufl.bmi.misc;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import edu.ufl.bmi.misc.ApiSourceJsonObjectDataProvider;
import edu.ufl.bmi.misc.DataObject;
import edu.ufl.bmi.misc.DataObjectProvider;

public class SaveOneObjectAtATimeFromApiProcess {

	String apiConfigInfoFileName;
	String localFolder;
	File apiConfigInfoFile;
	File ouptutFolder;

	String objectTypeTxt;
	String baseUrl;
	String allObjectIdsUrl;
	String objectLocatorTxt;
	String uniqueIdFieldName;

	public static void main(String[] args) {
		SaveOneObjectAtATimeFromApiProcess p = new SaveOneObjectAtATimeFromApiProcess(args[0], args[1]);
		p.run();
	}

	public SaveOneObjectAtATimeFromApiProcess(String apiConfigInfoFileName, String localFolder) {
		this.apiConfigInfoFileName = apiConfigInfoFileName;
		this.localFolder = localFolder;

		this.ouptutFolder = new File(this.localFolder);
		if (!this.ouptutFolder.exists()) throw new IllegalArgumentException("output folder " + localFolder + " doesn't exist.");

		this.apiConfigInfoFile = new File(this.apiConfigInfoFileName);
		if (!this.apiConfigInfoFile.exists()) throw new IllegalArgumentException(
				"api config info file " + apiConfigInfoFileName + " does not exist.");
	}

	public void run() {
		try {
			readConfigurationProperties();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(11);
		}
		ApiSourceJsonObjectDataProvider dop = new ApiSourceJsonObjectDataProvider(
			this.baseUrl, this.allObjectIdsUrl, this.baseUrl+this.objectLocatorTxt, this.uniqueIdFieldName);
		dop.initialize();

		for (DataObject dataObject : dop) {
			String keyValue = dataObject.getDataElementValue(uniqueIdFieldName);
			File dof = new File(this.localFolder + File.pathSeparator + keyValue + ".json");
			if (!dof.exists()) {
				try {
					FileWriter fw = new FileWriter(dof);
					fw.write(dataObject.getRawData());
					fw.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

	}

	public void readConfigurationProperties() throws IOException {
    	Properties p = new Properties();
    	p.load(new FileReader(this.apiConfigInfoFile));

    	this.objectTypeTxt = p.getProperty("object_type");
 
		this.baseUrl = p.getProperty("api_base_url");
		System.out.println("baseUrl = " + this.baseUrl);
		this.allObjectIdsUrl = p.getProperty("get_all_object_ids_url");
		System.out.println("allObjectIdsUrl = " + this.allObjectIdsUrl);
		this.objectLocatorTxt = p.getProperty(objectTypeTxt);

		this.uniqueIdFieldName = p.getProperty("unique_id_field");
	}
}