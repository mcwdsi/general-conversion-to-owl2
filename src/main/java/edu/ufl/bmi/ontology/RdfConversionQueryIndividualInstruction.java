package edu.ufl.bmi.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import edu.ufl.bmi.misc.DataObject;
import edu.ufl.bmi.misc.IriLookup;

public class RdfConversionQueryIndividualInstruction extends RdfConversionInstruction {

	/*
		Here, the variableName is the varible in the file currently being processed, whose
			value we will use to query for the IRI of an already existing individual 
			created while we processed a previous file.

		Loosely speaking (and drawing analogy to relational data), this field is the
			foreigh key.
	*/
	String variableName;
	IRI classIri;
	IRI annotationPropertyIri;
	AnnotationValueBuilder avb;
	boolean alwaysCreate;
	ArrayList<ArrayList<String>> conditions;
	IriRepository iriRepository;
	String iriRepositoryPrefix;

	String iriPrefix;
	String queryIriPrefix;
	String externalFileFieldName;
	String externalFileRowTypeName;
	String lookupValueFieldName;
	String lookupVariableName;
	//int lookupValueFieldIndex;
	
	public RdfConversionQueryIndividualInstruction(IriLookup iriMap, OWLDataFactory odf, String variableName, 
			IriRepository iriRepository, String iriRepositoryPrefix, String externalFileFieldName, String externalFileRowTypeName, String iriPrefix,
			String lookupValueFieldName) {
		super(iriMap, odf);
		this.variableName = variableName.replaceFirst("[\\[]","");
		if (variableName.endsWith("]")) {
			int len = this.variableName.length();
			this.variableName = this.variableName.substring(0, len-1);
			if (this.variableName == null) {
				System.err.println("Lookup variable name is null: " + lookupValueFieldName + ", " + externalFileFieldName + ", "
						+ externalFileRowTypeName + ", " + iriPrefix);
			}
		}
		
		//this.variableName = (variableName.startsWith("[")) ? variableName.replace("[","").replace("]","") : variableName ;
		this.iriRepository = iriRepository;
		this.iriRepositoryPrefix = iriRepositoryPrefix;
		this.externalFileFieldName = externalFileFieldName;
		this.externalFileRowTypeName = externalFileRowTypeName;
		this.iriPrefix = iriPrefix;
		this.queryIriPrefix = this.iriPrefix + this.externalFileRowTypeName;
		//this.lookupValueFieldName = lookupValueFieldName.replace("[","").replace("]","");
		this.lookupValueFieldName = lookupValueFieldName.replaceFirst("[\\[]","");
		if (lookupValueFieldName.endsWith("]")) {
			int len = this.lookupValueFieldName.length();
			this.lookupValueFieldName = this.lookupValueFieldName.substring(0, len-1);
		}
		//System.out.println(lookupValueFieldName);
		//this.lookupValueFieldIndex = fieldNameToIndex.get(this.lookupValueFieldName); 
	}

/*
	@Override
	public void execute(OWLNamedIndividual rowIndividual, ArrayList<String> recordFields, HashMap<String, OWLNamedIndividual> variables, OWLOntology oo) {		
		HashMap<IRI, String> repoAnnotations = new HashMap<IRI, String>();
		IRI externalFieldIri = IRI.create(queryIriPrefix + "/" + externalFileFieldName);
		repoAnnotations.put(externalFieldIri, recordFields.get(fieldNameToIndex.get(this.lookupValueFieldName)));
		IRI externalVarNameIri = IRI.create(queryIriPrefix + "/variableName");
		repoAnnotations.put(externalVarNameIri, "row individual");
		
		Set<IRI> resultSet = iriRepository.queryIris(null, repoAnnotations);
		int resultCount = resultSet.size();
		if (resultCount > 1) {
			throw new RuntimeException("resultSet should be size 1, but got " + resultCount);
		}
		OWLNamedIndividual oni = (resultCount == 1) ? 
			odf.getOWLNamedIndividual(resultSet.iterator().next()) :
			null;
			
		//System.out.println("Adding the following to variables: " + variableName + "\t" + oni);
		if (oni != null) variables.put(variableName, oni);
		//iriRepository.addIris(oni.getIRI(), null, repoAnnotations);
	}
*/

	public void setLookupVariableName(String lookupVariableName) {
		this.lookupVariableName = lookupVariableName;
	}

	public void execute(OWLNamedIndividual rowIndividual, DataObject dataObject, DataObject parentObject, HashMap<String, OWLNamedIndividual> variables, OWLOntology oo) {
		//if for some reason we already found it, or maybe in some circumstances I haven't foreseen yet, created it, then just return
		if (variables.containsKey(this.lookupVariableName) && variables.get(this.lookupVariableName) != null) return;

		HashMap<IRI, String> repoAnnotations = new HashMap<IRI, String>();
		IRI externalFieldIri = IRI.create(queryIriPrefix + "/" + externalFileFieldName);
		String lookupValue = dataObject.getDataElementValue(lookupValueFieldName);
		//System.out.println("query instruction is looking for " + lookupValueFieldName + " == " + lookupValue);
		repoAnnotations.put(externalFieldIri, lookupValue);
		IRI externalVarNameIri = IRI.create(queryIriPrefix + "/variableName");
		if (this.lookupVariableName == null) repoAnnotations.put(externalVarNameIri, "row individual");
		else repoAnnotations.put(externalVarNameIri, this.lookupVariableName);

		//System.err.println("query on " + this.lookupValueFieldName + " = "+ lookupValue + ", lookup variable is " + this.externalFileFieldName);
		//System.err.println("\t"+externalFieldIri);
		
		Set<IRI> resultSet = iriRepository.queryIris(null, repoAnnotations);
		int resultCount = resultSet.size();
		if (resultCount > 1) {
			//throw new RuntimeException("resultSet should be size 1, but got " + resultCount);
			System.err.println("resultSet size should be 1, but is " + resultCount);
			System.err.println("\tlookup variable is " + this.lookupVariableName);
			System.err.println("\tlookup value is " + lookupValue);

		}
		//OWLNamedIndividual oni = (resultCount == 1) ? 
		//	odf.getOWLNamedIndividual(resultSet.iterator().next()) :
		//	null;
		
		OWLNamedIndividual oni = (resultCount > 0) ? 
			odf.getOWLNamedIndividual(resultSet.iterator().next()) :
			null;

		//System.err.println("resultCount is " + resultCount+" and oni is " + oni);
		
			
		//System.out.println("Adding the following to variables: " + variableName + "\t" + oni);
		if (oni != null) variables.put(variableName, oni);
		//iriRepository.addIris(oni.getIRI(), null, repoAnnotations);		
	}
}