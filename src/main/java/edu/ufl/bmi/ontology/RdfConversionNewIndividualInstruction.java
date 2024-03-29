package edu.ufl.bmi.ontology;

import java.net.URI;

import java.text.ParsePosition;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import edu.ufl.bmi.misc.DataObject;
import edu.ufl.bmi.misc.IriLookup;

public class RdfConversionNewIndividualInstruction extends RdfConversionInstruction {

	String variableName;
	IRI classIri;
	IRI annotationPropertyIri;
	AnnotationValueBuilder avb;
	boolean alwaysCreate;
	ArrayList<ArrayList<String>> conditions;
	IriRepository iriRepository;
	String iriRepositoryPrefix;
	String uniqueIdFieldName;
	int uniqueIdFieldIndex;
	IRI uniqueIdFieldIri;
	String iriSourceVariableName;

	boolean constructIriFromDate;
	ZoneId zoneId;
	DateTimeFormatter dtf;
	
	public RdfConversionNewIndividualInstruction(IriLookup iriMap, OWLDataFactory odf, String variableName, 
		String classIriTxt, String annotationPropertyTxt, String annotationValueInstruction,
		IriRepository iriRepository, String iriRepositoryPrefix, String uniqueIdFieldName) {
		super(iriMap, odf);
		this.variableName = variableName.replace("[","").replace("]","");
		this.classIri = iriMap.lookupClassIri(classIriTxt);
		if (this.classIri==null) System.err.println("Cannot find IRI for class handle '"+classIriTxt+"'");
		this.annotationPropertyIri = iriMap.lookupAnnPropIri(annotationPropertyTxt);
		this.avb = new AnnotationValueBuilder(annotationValueInstruction);
		this.alwaysCreate = true;
		this.iriRepository = iriRepository;
		this.iriRepositoryPrefix = iriRepositoryPrefix;
		this.uniqueIdFieldName = uniqueIdFieldName;
		this.uniqueIdFieldIri = IRI.create(iriRepositoryPrefix + "/" + uniqueIdFieldName);
		this.iriSourceVariableName = null;
		this.constructIriFromDate = false;
	}

	public RdfConversionNewIndividualInstruction(IriLookup iriMap, OWLDataFactory odf, String variableName, 
		String classIriTxt, String annotationPropertyTxt, String annotationValueInstruction, String creationCondition, IriRepository iriRepository,
		String iriRepositoryPrefix, String uniqueIdFieldName) {
		super(iriMap, odf);
		this.variableName = variableName.replace("[","").replace("]","");
		this.classIri = iriMap.lookupClassIri(classIriTxt);
		if (this.classIri==null) System.err.println("Cannot find IRI for class handle '"+classIriTxt+"'");
		this.annotationPropertyIri = iriMap.lookupAnnPropIri(annotationPropertyTxt);
		this.avb = new AnnotationValueBuilder(annotationValueInstruction);
		this.alwaysCreate = false;
		this.iriRepository = iriRepository;
		this.iriRepositoryPrefix = iriRepositoryPrefix;
		this.uniqueIdFieldName = uniqueIdFieldName;
		this.uniqueIdFieldIri = IRI.create(iriRepositoryPrefix + "/" + uniqueIdFieldName);
		this.setCreationConditionLogic(creationCondition);
		this.iriSourceVariableName = null;
		this.constructIriFromDate = false;
	}

/*
	@Override
	public void execute(OWLNamedIndividual rowIndividual, ArrayList<String> recordFields, HashMap<String, OWLNamedIndividual> variables, OWLOntology oo) {
		if (alwaysCreate || evaluateCondition(recordFields)) {
			String annotationValue = avb.buildAnnotationValue(recordFields);
			if (validFieldValue(annotationValue)) {
				HashMap<IRI, String> repoAnnotations = new HashMap<IRI, String>();
				IRI varNameIri = IRI.create(iriRepositoryPrefix + "/variableName");
				repoAnnotations.put(varNameIri, variableName);
				IRI rdfLabelIri = iriMap.lookupAnnPropIri("label");
				repoAnnotations.put(rdfLabelIri, annotationValue);
				repoAnnotations.put(this.uniqueIdFieldIri, recordFields.get(uniqueIdFieldIndex));
				
				Set<IRI> resultSet = iriRepository.queryIris(null, repoAnnotations);
				int resultCount = resultSet.size();
				if (resultCount > 1) {
					throw new RuntimeException("resultSet should be size 1, but got " + resultCount);
				}

				OWLNamedIndividual oni = (resultCount == 1) ? 
					GenericOwl2Converter.createNamedIndividualWithIriTypeAndLabel(resultSet.iterator().next(), oo, classIri, annotationPropertyIri, 
						annotationValue) :
					GenericOwl2Converter.createNamedIndividualWithTypeAndLabel(oo, classIri, annotationPropertyIri, 
						annotationValue);
				//System.out.println("Adding the following to variables: " + variableName + "\t" + oni);
				variables.put(variableName, oni);
				iriRepository.addIris(oni.getIRI(), null, repoAnnotations);

			}
		}
	}

	public boolean evaluateCondition(ArrayList<String> recordFields) {
		boolean result = false;  // only set to true if one of the sublists has all true conditions
		for (ArrayList<String> condition : conditions) {
			boolean subConditionResult = true;  //any false within the list will change the "and" to false
			for (String subcondition : condition) {
				//check ==, !=, not-empty.  We can't handle any nesting at the moment, but we don't have the need, either.
				if (subcondition.contains("==")) {
					String[] flds = subcondition.split("==");
					String field = flds[0].trim().replace("[","").replace("]","");
					String value = flds[1];
					subConditionResult = subConditionResult && recordFields.get(fieldNameToIndex.get(field)).equals(value);
				} else if (subcondition.contains("!=")) {
					String[] flds = subcondition.split("!=");
					String field = flds[0].trim().replace("[","").replace("]","");
					String value = flds[1];
					subConditionResult = subConditionResult && !recordFields.get(fieldNameToIndex.get(field)).equals(value);
				} else if (subcondition.contains("not-empty")) {
					String field = subcondition.replace("not-empty","").trim().replace("[","").replace("]","");
					subConditionResult = subConditionResult && !recordFields.get(fieldNameToIndex.get(field)).isEmpty();
				}
			}
			result = result || subConditionResult;
		}
		return result;
	}
	*/

	public void execute(OWLNamedIndividual rowIndividual, DataObject dataObject, DataObject parentObject, HashMap<String, OWLNamedIndividual> variables, OWLOntology oo) {
		if (alwaysCreate || evaluateCondition(dataObject)) {
			String annotationValue = avb.buildAnnotationValue(dataObject, parentObject);
			if (validFieldValue(annotationValue)) {
				HashMap<IRI, String> repoAnnotations = new HashMap<IRI, String>();
				IRI varNameIri = IRI.create(iriRepositoryPrefix + "/variableName");
				repoAnnotations.put(varNameIri, variableName);
				IRI rdfLabelIri = iriMap.lookupAnnPropIri("label");
				repoAnnotations.put(rdfLabelIri, annotationValue);
				//System.err.println("newIndividual query, variableName=" + variableName + " and annotationValue=" + annotationValue);
				if (parentObject == null) {
					repoAnnotations.put(this.uniqueIdFieldIri, dataObject.getDataElementValue(uniqueIdFieldName));
				} else {
					repoAnnotations.put(this.uniqueIdFieldIri, parentObject.getDataElementValue(uniqueIdFieldName));
				}
				//System.err.println("newIndividual query, variableName=" + this.uniqueIdFieldIri + " and value=" +
				//		dataObject.getDataElementValue(uniqueIdFieldName)); 
				
				Set<IRI> resultSet = iriRepository.queryIris(null, repoAnnotations);
				int resultCount = resultSet.size();
				if (resultCount > 1) {
					throw new RuntimeException("resultSet should be size 1, but got " + resultCount);
				}

				IRI individualIri = null;
				if (resultCount == 1) {
					individualIri = resultSet.iterator().next();
				} else if (this.iriSourceVariableName != null && !this.iriSourceVariableName.isEmpty()) {
					String iriTxt = (this.constructIriFromDate) ? buildIriTxtFromDate(dataObject) :
						dataObject.getDataElementValue(this.iriSourceVariableName); 
					System.out.println("Got value of " + iriTxt + " for " + this.iriSourceVariableName + " data element.");
					individualIri = IRI.create(iriTxt);
					try {
						URI iriAsUri = individualIri.toURI();
					} catch (IllegalArgumentException ile) {
						ile.printStackTrace();
						System.err.println("Bad IRI: " + iriTxt + ", not using it but instead minting new IRI.");
						individualIri = null;
					}
				}

				OWLNamedIndividual oni = (individualIri != null) ? 
					GenericOwl2Converter.createNamedIndividualWithIriTypeAndLabel(individualIri, oo, classIri, annotationPropertyIri, 
						annotationValue) :
					GenericOwl2Converter.createNamedIndividualWithTypeAndLabel(oo, classIri, annotationPropertyIri, 
						annotationValue);
				//System.out.println("Adding the following to variables: " + variableName + "\t" + oni);
				//if (variableName.contains("grant")) System.err.println("putting variable " + variableName + ", oni="+oni);
				variables.put(variableName, oni);
				iriRepository.addIris(oni.getIRI(), null, repoAnnotations);

			}
		}		
	}

	public boolean evaluateCondition(DataObject dataObject) {
		boolean result = false;  // only set to true if one of the sublists has all true conditions
		for (ArrayList<String> condition : conditions) {
			boolean subConditionResult = true;  //any false within the list will change the "and" to false
			for (String subcondition : condition) {
				//check ==, !=, not-empty.  We can't handle any nesting at the moment, but we don't have the need, either.
				if (subcondition.contains("==")) {
					String[] flds = subcondition.split("==");
					String field = flds[0].trim().replace("[","").replace("]","");
					String value = flds[1];
					subConditionResult = subConditionResult && dataObject.getDataElementValue(field).equals(value);
				} else if (subcondition.contains("!=")) {
					String[] flds = subcondition.split("!=");
					String field = flds[0].trim().replace("[","").replace("]","");
					String value = flds[1];
					subConditionResult = subConditionResult && !dataObject.getDataElementValue(field).equals(value);
				} else if (subcondition.contains("not-empty")) {
					String field = subcondition.replace("not-empty","").trim().replace("[","").replace("]","");
					subConditionResult = subConditionResult && !dataObject.getDataElementValue(field).isEmpty();
				} else if (subcondition.contains("empty")) {
					String field = subcondition.replace("empty","").trim().replace("[","").replace("]","");
					subConditionResult = subConditionResult && dataObject.getDataElementValue(field).isEmpty();
				}
			}
			result = result || subConditionResult;
		}
		return result;
	}

	public void setIriSourceVariableName(String iriSourceVariableName) {
		if (iriSourceVariableName.startsWith("fromDate")) {
			this.constructIriFromDate = true;
			this.dtf = DateTimeFormatter.ofPattern("[MM/dd/yyyy][yyyy-MM-dd]");
			this.zoneId = ZoneId.systemDefault();
			iriSourceVariableName = iriSourceVariableName.replace("fromDate(","");
			iriSourceVariableName = iriSourceVariableName.replace(")","");
			System.out.println("from date source variable name remaining: " + iriSourceVariableName);
		}
		this.iriSourceVariableName = iriSourceVariableName.replace("[","").replace("]","");
		System.out.println("Variable whose value is the IRI we'll give to individuals: "  + this.iriSourceVariableName);
	}

	protected String buildIriTxtFromDate(DataObject dataObject) {
		String dateValueTxt = dataObject.getDataElementValue(this.iriSourceVariableName);
		System.err.print("dateValueTxt="+dateValueTxt);
       
        LocalDate ld = LocalDate.parse(dateValueTxt, dtf);
        System.err.print(", as ISO = " + DateTimeFormatter.ISO_LOCAL_DATE.format(ld));
        //System.out.println(DateTimeFormatter.ISO_OFFSET_DATE.format(odt));
        ZonedDateTime odt = ZonedDateTime.of(ld, LocalTime.now(), zoneId);
        String localPart = DateTimeFormatter.ISO_OFFSET_DATE.format(odt);
        System.err.println(", and now with offset: " + localPart);		

		return "http://time.org/gregorian/"+localPart;
	}

	public void setCreationConditionLogic(String creationConditionLogic) {
		/*
	 	 *  Need a data structure.  Could have list of lists.  If we take "and" as preference over "or", then we'd split on the "or" first.
	 	 *		that would give the outside list.  Splitting on "and" within the outside list creates a list entry inside each list.
	 	 *
	 	 *	Therefore, only one entry in the list of lists must evaluate to true for the entire expression to be true, then 
	 	 *		for any entry in the list of lists, all its entries must be true.  (inner lists are "and", outer list is "or")
		*/
		String[] flds = creationConditionLogic.split(" or ",-1);
		conditions = new ArrayList<ArrayList<String>>(flds.length);
		for (String fld : flds) {
			ArrayList<String> condition = new ArrayList<String>();
			if (!fld.isEmpty()) {
				String[] subflds = fld.split(" and ", -1);
				for (String subfld : subflds) {
					if (!subfld.isEmpty()) {
						condition.add(subfld);
					} else {
						condition.add("true");  // blanks don't make the "and" false
					}
				}
			} else {
				condition.add("false");  //blanks don't make the "or" true
			}
			conditions.add(condition);
		}
	}

}
