/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emlogis.engine.solver.drools.score;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

import org.apache.commons.io.IOUtils;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.optaplanner.benchmark.config.PlannerBenchmarkConfig;
import org.optaplanner.core.impl.solver.XStreamXmlSolverFactory;

/**
 * Temporary modification of optaplanner benchmarker class
 * to allow setting of alias. 
 * 
 * @see PlannerBenchmarkFactory
 */
public class TempXStreamXmlPlannerBenchmarkFactory extends PlannerBenchmarkFactory {

    private XStream xStream;
    private PlannerBenchmarkConfig plannerBenchmarkConfig = null;

    public TempXStreamXmlPlannerBenchmarkFactory() {
        xStream = XStreamXmlSolverFactory.buildXStream();
        xStream.processAnnotations(PlannerBenchmarkConfig.class);
    }

    // ************************************************************************
    // Configure methods
    // ************************************************************************
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

    /**
     * @param benchmarkConfigResource never null, a classpath resource
     * as defined by {@link ClassLoader#getResource(String)}
     * @return this
     */
    public TempXStreamXmlPlannerBenchmarkFactory configure(String benchmarkConfigResource) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(benchmarkConfigResource);
        if (in == null) {
            String errorMessage = "The benchmarkConfigResource (" + benchmarkConfigResource
                    + ") does not exist in the classpath.";
            if (benchmarkConfigResource.startsWith("/")) {
                errorMessage += "\nAs from 6.1, a classpath resource should not start with a slash (/)."
                        + " A benchmarkConfigResource now adheres to ClassLoader.getResource(String)."
                        + " Remove the leading slash from the benchmarkConfigResource if you're upgrading from 6.0.";
            }
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            return configure(in);
        } catch (ConversionException e) {
            throw new IllegalArgumentException("Unmarshalling of benchmarkConfigResource (" + benchmarkConfigResource
                    + ") fails.", e);
        }
    }

    public TempXStreamXmlPlannerBenchmarkFactory configure(File benchmarkConfigFile) {
        try {
            return configure(new FileInputStream(benchmarkConfigFile));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("The benchmarkConfigFile (" + benchmarkConfigFile
                    + ") was not found.", e);
        }
    }

    public TempXStreamXmlPlannerBenchmarkFactory configure(InputStream in) {
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

    public TempXStreamXmlPlannerBenchmarkFactory configure(Reader reader) {
        plannerBenchmarkConfig = (PlannerBenchmarkConfig) xStream.fromXML(reader);
        return this;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public PlannerBenchmarkConfig getPlannerBenchmarkConfig() {
        if (plannerBenchmarkConfig == null) {
            throw new IllegalStateException("The plannerBenchmarkConfig (" + plannerBenchmarkConfig + ") is null," +
                    " call configure(...) first.");
        }
        return plannerBenchmarkConfig;
    }

    public PlannerBenchmark buildPlannerBenchmark() {
        if (plannerBenchmarkConfig == null) {
            throw new IllegalStateException("The plannerBenchmarkConfig (" + plannerBenchmarkConfig + ") is null," +
                    " call configure(...) first.");
        }
        return plannerBenchmarkConfig.buildPlannerBenchmark();
    }

}
