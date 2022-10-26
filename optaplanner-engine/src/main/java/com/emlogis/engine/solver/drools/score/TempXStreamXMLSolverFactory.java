package com.emlogis.engine.solver.drools.score;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.impl.solver.XStreamXmlSolverFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

/**
 * Temporary copy of XStreamXMLSolverFactory class to be used until
 * OptaPlanner updated to a version that includes alias, aliasAttribute and 
 * addDefaultImplementation into XStreamXMLSolverFactory clas
 * @author emlogis
 *
 */
public class TempXStreamXMLSolverFactory extends SolverFactory {
    /**
     * Builds the {@link XStream} setup which is used to read/write solver
     * configs and benchmark configs. It should never be used to read/write
     * {@link Solution} instances. Use XStreamSolutionFileIO for that instead.
     * 
     * @return never null.
     */
    /**
     * @return
     */
    public static XStream buildXStream() {
	XStream xStream = new XStream();
	xStream.setMode(XStream.ID_REFERENCES);
	xStream.aliasSystemAttribute("xStreamId", "id");
	xStream.aliasSystemAttribute("xStreamRef", "reference");
	xStream.processAnnotations(SolverConfig.class);
	return xStream;
    }

    private XStream xStream;
    private SolverConfig solverConfig = null;

    public TempXStreamXMLSolverFactory() {
	xStream = buildXStream();
    }

    public void alias(String alias, Class clazz) {
	xStream.alias(alias, clazz);
    }

    public void aliasAttribute(Class clazz, String attributeName, String alias) {
	xStream.aliasAttribute(clazz, attributeName, alias);
    }

    public void addDefaultImplementation(Class defaultImplementation, Class ofType) {
	xStream.addDefaultImplementation(defaultImplementation, ofType);
    }

    public void addXStreamAnnotations(Class... xStreamAnnotations) {
	xStream.processAnnotations(xStreamAnnotations);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @param solverConfigResource
     *            never null, a classpath resource as defined by
     *            {@link ClassLoader#getResource(String)}
     * @return this
     */
    public TempXStreamXMLSolverFactory configure(String solverConfigResource) {
	InputStream in = getClass().getClassLoader().getResourceAsStream(solverConfigResource);
	if (in == null) {
	    String errorMessage = "The solverConfigResource (" + solverConfigResource
		    + ") does not exist in the classpath.";
	    if (solverConfigResource.startsWith("/")) {
		errorMessage += "\nAs from 6.1, a classpath resource should not start with a slash (/)."
			+ " A solverConfigResource now adheres to ClassLoader.getResource(String)."
			+ " Remove the leading slash from the solverConfigResource if you're upgrading from 6.0.";
	    }
	    throw new IllegalArgumentException(errorMessage);
	}
	try {
	    return configure(in);
	} catch (ConversionException e) {
	    throw new IllegalArgumentException("Unmarshalling of solverConfigResource (" + solverConfigResource
		    + ") fails.", e);
	}
    }

    public TempXStreamXMLSolverFactory configure(File solverConfigFile) {
	try {
	    return configure(new FileInputStream(solverConfigFile));
	} catch (FileNotFoundException e) {
	    throw new IllegalArgumentException("The solverConfigFile (" + solverConfigFile + ") was not found.", e);
	}
    }

    public TempXStreamXMLSolverFactory configure(InputStream in) {
	Reader reader = null;
	try {
	    reader = new InputStreamReader(in, "UTF-8");
	    return configure(reader);
	} catch (UnsupportedEncodingException e) {
	    throw new IllegalStateException("This vm does not support UTF-8 encoding.", e);
	} finally {
	    IOUtils.closeQuietly(reader);
	    IOUtils.closeQuietly(in);
	}
    }

    public TempXStreamXMLSolverFactory configure(Reader reader) {
	solverConfig = (SolverConfig) xStream.fromXML(reader);
	return this;
    }

    public SolverConfig getSolverConfig() {
	if (solverConfig == null) {
	    throw new IllegalStateException("The solverConfig (" + solverConfig + ") is null,"
		    + " call configure(...) first.");
	}
	return solverConfig;
    }

    public Solver buildSolver() {
	if (solverConfig == null) {
	    throw new IllegalStateException("The solverConfig (" + solverConfig + ") is null,"
		    + " call configure(...) first.");
	}
	return solverConfig.buildSolver();
    }

}
