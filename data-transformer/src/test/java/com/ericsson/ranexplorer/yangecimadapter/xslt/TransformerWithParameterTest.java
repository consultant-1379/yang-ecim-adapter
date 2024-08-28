/* ----------------------------------------------------------------------------
 *     Copyright (C) 2018 LM Ericsson Limited.  All rights reserved.
 * ----------------------------------------------------------------------------
 */

package com.ericsson.ranexplorer.yangecimadapter.xslt;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.ericsson.ranexplorer.yangecimadapter.common.services.util.AdapterUtils.removeWhitespaceBetweenTags;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class TransformerWithParameterTest {
    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread().getContextClassLoader().getClass());

    @Test
    public void testTransformWithManagedElementParameter() {

        final XsltTransformer transformer = XsltTransformerFactory.newYangToEcimTransformer("1", "default");
        final String inputFile = "yangToEcim/in/CarrierAggregationFunction_yang.xml";
        final String outputFile = "yangToEcim/out/CarrierAggregationFunction_ecim.xml";
        try {
            final String input = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(inputFile).toURI() )));
            final String output = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(outputFile).toURI() )));
            final String result =  transformer.transformWithDummyRootWrapper(input);
            assertEquals(removeWhitespaceBetweenTags(output), removeWhitespaceBetweenTags(result));
        } catch (final TransformerException | URISyntaxException | IOException exception) {
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testTransformWithSubscriptionTemplateType(){

        final XsltTransformer transformer = XsltTransformerFactory.newYangToEcimTransformer("Test", "subscription");
        final String inputFile = "yangToEcimMasking/in/eutran-cell-fdd_yang.xml";
        final String outputFile = "yangToEcimMasking/out/eutran-cell-fdd_ecim.xml";

        try{
            final String input = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(inputFile).toURI())));
            final String output = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(outputFile).toURI())));
            final String result = transformer.transformWithDummyRootWrapper(input);
            assertEquals(removeWhitespaceBetweenTags(output), removeWhitespaceBetweenTags(result));
        }catch(Exception exception){
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }

    @Test
    public void testTransformWithDefaultTemplateType(){

        final XsltTransformer transformer = XsltTransformerFactory.newYangToEcimTransformer("Test", "default");
        final String inputFile = "yangToEcimMasking/in/eutran-cell-fdd_yang.xml";
        final String output = "<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\">\n" +
                "    <managedElementId>Test</managedElementId>\n" +
                "    <ENodeBFunction xmlns=\"urn:com:ericsson:ecim:Lrat\">\n" +
                "        <eNodeBFunctionId>1</eNodeBFunctionId><EUtranCellFDD/></ENodeBFunction></ManagedElement>";

        try{
            final String input = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(inputFile).toURI())));
            final String result = transformer.transformWithDummyRootWrapper(input);
            assertEquals(removeWhitespaceBetweenTags(output), removeWhitespaceBetweenTags(result));
        }catch (Exception exception){
            logger.error(exception.getMessage(), exception);
            fail();
        }
    }
}
