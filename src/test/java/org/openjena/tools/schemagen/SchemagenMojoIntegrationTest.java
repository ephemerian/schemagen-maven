/*****************************************************************************
 * File:    SchemagenMojoIntegrationTest.java
 * Project: schemagen
 * Created: 25 May 2010
 * By:      ian
 *
 * Copyright (c) 2010 Epimorphics Ltd. All rights reserved.
 *****************************************************************************/

// Package
///////////////

package org.openjena.tools.schemagen;


// Imports
///////////////


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Integration tests for schemagen mojo: test that running the plugin produces
 * the right results</p>
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class SchemagenMojoIntegrationTest
    extends SchemagenMojoIntegrationTestBase
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( SchemagenMojoIntegrationTest.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Before
    public void setUp() throws Exception {
        //
    }

    /**
     * Basic integration test: ensure that the output appears in the generated-sources directory
     * @throws Exception
     */
    @Test
    public void schemagenIntegrationTest0()
        throws Exception
    {
        // Get dummy Maven project from /src/test/resources/...
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/schemagen-integration-0" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );

        /*
         * The Command Line Options (CLI) are passed to the
         * verifier as a list.
         */
        List<String> cliOptions = new ArrayList<String>();
        verifier.setCliOptions( cliOptions );

        verifier.executeGoal( "generate-sources" );
        verifier.verifyErrorFreeLog();

        // no package specified, so output is just in generated-sources
        String gSource = asAbsoluteFileName( verifier, "target/generated-sources/Test1.java" );

        // note that ?s modifier makes . pattern match newlines
        verifier.assertFileMatches( gSource, "(?s).*public static final Resource Cls.*" );

        /*
         * Reset the streams before executing the verifier
         * again.
         */
        verifier.resetStreams();
    }

    /**
     * Integration test: ensure that the output appears in the right package
     * @throws Exception
     */
    @Test
    public void schemagenIntegrationTest1()
        throws Exception
    {
        // Get dummy Maven project from /src/test/resources/...
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/schemagen-integration-1" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );

        /*
         * The Command Line Options (CLI) are passed to the
         * verifier as a list.
         */
        List<String> cliOptions = new ArrayList<String>();
        verifier.setCliOptions( cliOptions );

        verifier.executeGoal( "generate-sources" );
        verifier.verifyErrorFreeLog();

        // no package specified, so output is just in generated-sources
        String gSource = asAbsoluteFileName( verifier, "target/generated-sources/org/example/test/Test1.java" );

        // note that ?s modifier makes . pattern match newlines
        verifier.assertFileMatches( gSource, "(?s).*package org.example.test.*public static final Resource Cls.*" );

        /*
         * Reset the streams before executing the verifier
         * again.
         */
        verifier.resetStreams();
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

