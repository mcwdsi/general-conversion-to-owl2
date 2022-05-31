package edu.ufl.bmi.ontology;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.semanticweb.owlapi.model.IRI;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import org.apache.jena.ext.xerces.util.XMLChar;

public class RdfIriRepositoryWithJena implements IriRepository {

	String repositoryFileName;
	InputStream fileIn;
	Model m;
	long counter;
	String iriPrefix;

	public RdfIriRepositoryWithJena(String fileName, String iriPrefix) {
		this.repositoryFileName = fileName;
		this.iriPrefix = iriPrefix;
	}

	@Override
	public Set<IRI> queryIris(IRI namedGraphIri, HashMap<IRI, String> propertyValuePairs) {
		StringBuilder queryTxt = new StringBuilder("select ?x\nwhere {\n");
		Iterator<IRI> i = propertyValuePairs.keySet().iterator();
		boolean first = true;
		while (i.hasNext()) {
			IRI propIri = i.next();
			String value = propertyValuePairs.get(propIri);
			if (value == null) {
				System.err.println("query IRIs, value is null for " + propIri);
			}
			String validValue = convertToXmlValid(value);
			if (!first)
				queryTxt.append(".\n");
			queryTxt.append("\t?x <");
			queryTxt.append(propIri.toString());
			queryTxt.append("> '");
			queryTxt.append(validValue.replace("'","\\'"));
			queryTxt.append("' ");
			first = false;


		}
		queryTxt.append("\n}");

		//if (queryTxt.indexOf("role") > -1) System.out.println("\nQuery text:\n" + queryTxt + "\n");
		//System.err.println("query is\n" + queryTxt + "\n\n");
		HashSet<IRI> result = new HashSet<IRI>();
		Query query = QueryFactory.create(queryTxt.toString()) ;

  		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
   			ResultSet results = qexec.execSelect();
   			System.out.print("QUERY RESULT(S): ");
   			for ( ; results.hasNext() ; ) {
     			QuerySolution soln = results.nextSolution() ;
    			RDFNode x = soln.get("x") ;       // Get a result variable by name.
    			result.add(IRI.create(x.toString()));
    			System.out.print(x + "\t");
    		}
    		System.out.println("\n");
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
		return result;
	}

	@Override
	public Set<String> queryAnnotationValueForIri(IRI individualIri, IRI annotationIri) {
		StringBuilder queryTxt = new StringBuilder("select ?x\nwhere {\n");
		queryTxt.append("\t<");
		queryTxt.append(individualIri);
		queryTxt.append(">  ");
		queryTxt.append("<");
		queryTxt.append(annotationIri);
		queryTxt.append(">  ?x\n}");

		HashSet<String> result = new HashSet<String>();
		Query query = QueryFactory.create(queryTxt.toString()) ;

  		try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
   			ResultSet results = qexec.execSelect();
   			System.out.print("QUERY RESULT(S): ");
   			for ( ; results.hasNext() ; ) {
     			QuerySolution soln = results.nextSolution() ;
    			RDFNode x = soln.get("x") ;       // Get a result variable by name.
    			result.add(x.toString());
    			System.out.print(x + "\t");
    		}
    		System.out.println("\n");
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
		return result;
	}

	public void initialize() {
		try {
			m = ModelFactory.createDefaultModel();
			File f = new File(repositoryFileName);
			if (f.exists() && !f.isDirectory()) {
				fileIn = new FileInputStream(f);
				m.read(fileIn, null);
				fileIn.close();

				Query query = QueryFactory.create("select (max(?x) as ?max_iri) where { ?x ?y ?z . FILTER(STRSTARTS(STR(?x), \"" +
							this.iriPrefix + "\"))}");
				System.out.println("query is: \n\n" + query.toString() + "\n\n");
				try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
					ResultSet results = qexec.execSelect();
					//System.out.println("QUERY RESULT(S): ");
					for ( ; results.hasNext() ; ) {
						QuerySolution soln = results.nextSolution();
						RDFNode max_iri = soln.get("max_iri");
						if (max_iri.isResource()) {
							Resource r = max_iri.asResource();
							//System.out.print(max_iri + "\t" + r.getLocalName());
							String localName = r.getLocalName();
							String[] flds = localName.split("_");
							counter = Long.parseLong(flds[1])+1L;
							//System.out.println("\tcounter == " + counter);
						} else {
							System.out.print("NOT A RESOURCE");
						}
					}
					System.out.println();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getIriCounter() {
		return counter;
	}

	public void addIris(IRI theIri, IRI namedGraphIri, HashMap<IRI, String> propertyValuePairs) {
		Resource theResource = m.createResource(theIri.toString());
		Iterator<IRI> keys = propertyValuePairs.keySet().iterator();
		while (keys.hasNext()) {
			IRI propertyIri = keys.next();
			String value = propertyValuePairs.get(propertyIri);
			String validValue = convertToXmlValid(value);
			Property property = m.getProperty(propertyIri.toString());
			theResource.addProperty(property, validValue);
		}
	}

	protected String convertToXmlValid(String s) {
		s=s.replace("\r","\\r");
		s=s.replace("\n","\\n");
		s=s.replace("\\", "\\\\");
		s=s.replace("\"", "\\\"");
		s=s.replace("\b", "\\b");
		s=s.replace("\f", "\\f");
		s=s.replace("\t", "\\t");
		char[] c = s.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (char ci : c) {
			if (XMLChar.isValid(ci))
				sb.append(ci);
			else {
				System.err.println("Bad XML char " + ci + " in " + s);
			}
		}
		return sb.toString();
	}

	public void writeFile() {
		try {
			OutputStream os = new FileOutputStream(new File(repositoryFileName));
			m.write(os);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}