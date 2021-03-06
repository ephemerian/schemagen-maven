/*****************************************************************************
 * File:    SchemagenMojo.java
 * Project: schemagen
 * Created: 22 Mar 2010
 * By:      ian
 *
 * Copyright (c) 2010-11 Epimorphics Ltd. See LICENSE file for license terms.
 *****************************************************************************/

// Package
///////////////

package org.openjena.tools.schemagen;


// Imports
///////////////

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jena.schemagen;
import jena.schemagen.SchemagenOptions.OPT;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/**
 * <p>Maven plugin to execute Jena schemagen as part of a Jena-based
 * project build cycle
 * </p>
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 *
 * Maven Mojo options
 * @goal translate
 * @phase generate-sources
*/
public class SchemagenMojo
    extends AbstractMojo
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** Default output location */
    public static final String GENERATED_SOURCES = File.separator + "generated-sources";

    /** Default pattern for includes */

    /** Name of default options element */
    public static final String DEFAULT_OPTIONS_ELEM = "default";

    /***********************************/
    /* Static variables                */
    /***********************************/

    /**
     * Target directory
     * @parameter property="project.build.directory"
     */
    private static String projectBuildDir;

    /** Return the value of <code>${project.build.directory}</code> */
    public static String getProjectBuildDir() {
        return projectBuildDir;
    }

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /**
     * Array of file patterns to include in processing
     * @parameter alias="includes"
     */
    private String[] includes = new String[0];

    /**
     * Array of file patterns to exclude from processing
     * @parameter alias="excludes"
     */
    private String[] excludes = new String[0];

    /**
     * Options for individual files
     * @parameter alias="fileOptions"
     */
    private List<Source> fileOptions;

    /**
     * The current base directory of the project
     * @parameter property="basedir"
     */
    private File baseDir;

    /** The default options object, if any */
    private SchemagenOptions defaultOptions;

    /** Map of source options, indexed by name */
    private Map<String, SchemagenOptions> optIndex = new HashMap<String, SchemagenOptions>();

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    public void execute() throws MojoExecutionException, MojoFailureException {
        // set the default defaults
        defaultOptions = new SchemagenOptions.DefaultSchemagenOptions();

        getLog().info( "Starting schemagen execute() ...");

        // next process the various options specs
        if( fileOptions != null ){
	        for (Source p: fileOptions) {
	            if (p.isDefaultOptions()) {
	                handleDefaultOptions( p );
	            }
	            else {
	                handleOption( p );
	            }
	        }
    	}
    
        if( defaultOptions == null ){
        	handleDefaultOptions( new Source() );        	
        }
        
        // then the files themselves
        for (String fileName: matchFileNames()) {
            processFile( fileName );
        }
    }

    /**
     * Return a list of the file names to be processed by schemagen. These are
     * determined by processing the Ant style paths given in the <code>includes</code>
     * and <code>excludes</code> parameters.
     *
     * @return Non-null but possibly empty list of files to process, sorted into lexical order
     */
    protected List<String> matchFileNames() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setExcludes( excludes );
        ds.setIncludes( includes );
        ds.setBasedir( getBaseDir() );
        ds.scan();

		List<String> files = new ArrayList<String>( Arrays.asList( ds.getIncludedFiles() ) );
        Collections.sort( files );
        
        //add http includes
        for( String include : includes ){
        	if( include.startsWith("http:") || include.startsWith("https:")){
        		files.add( include );
        	}
        }
        return files;
    }


    /**
     * Return the default options structure, or null
     * @return The default options
     */
    protected SchemagenOptions getDefaultOptions() {
        return defaultOptions;
    }

    /**
     * Handle the default options by creating a default options object and assigning
     * the options values from the given source object.
     * @param defOptionsSource The source object containing the default options
     */
    protected void handleDefaultOptions( Source defOptionsSource ) {
        if (defaultOptions != null) {
            defOptionsSource.setParent( defaultOptions );
        }
        defaultOptions = defOptionsSource;
    }

    /**
     * Process the given options specification for one of the input files
     * by attaching the default options and indexing.
     *
     * @param optionSpec Specification of the options for a given file
     */
    protected void handleOption( Source optionSpec ) {
        if (optionSpec.getFileName() != null) {
            optionSpec.setParent( getDefaultOptions() );
            optIndex.put( optionSpec.getFileName(), optionSpec );
        }
        else {
            getLog().info( "ignoring <source> element because the fileName is not specified" );
        }
    }

    /**
     * Delegate the processing of the given file to schemagen itself
     * @param fileName
     */
    protected void processFile( String fileName )
        throws MojoExecutionException
    {
    	//fix windows paths
    	if( File.separator.equals("\\") ){
    		fileName = fileName.replaceAll( "\\\\", "/" );
    	}
    	
        getLog().info( "processFile with " + fileName );
        getLog().info( optIndex.keySet().toString() );
        SchemagenOptions so = optIndex.get( fileName );
        getLog().info( "so = " + so );

        // if we have no options carrier for this file, we create one to contain
        // the name of the input file, and link it to the defaults
        String soFileName;
        if (so == null) {
            so = new Source();
            soFileName = fileName;
            so.setParent( getDefaultOptions() );
        } else {
        	soFileName = so.getOption( OPT.INPUT ).asLiteral().getString();
        }

        getLog().info( "input before adjustment: " + soFileName );
        
        boolean relative = !(soFileName.startsWith( "http:" ) || soFileName.startsWith( "https:" )
        		|| soFileName.startsWith( "file:" ));
        getLog().info( "relative = " + relative );
        getLog().info( "baseDir = " + baseDir );
        getLog().info( "getBaseDir() = " + getBaseDir() );
        soFileName = relative ? "file:" + baseDir + File.separator + soFileName : soFileName;
        getLog().info( "input after adjustment: " + soFileName );
        Resource input = ResourceFactory.createResource( soFileName );
        so.setOption( OPT.INPUT, input );

        getLog().info( "about to call run(): " );
        ensureTargetDirectory( so );
        new SchemagenAdapter().run( so );
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    public void setExcludes( String[] excludes ) {
        this.excludes = excludes;
    }

    public void setIncludes( String[] includes ) {
        this.includes = includes;
    }

    /**
     * Append the given string to the array of included file patterns
     * @param incl File pattern string to append to <code>this.includes</code>
     */
    public void addIncludes( String incl ) {
        String[] incls = new String[this.includes.length + 1];
        int i = 0;
        for (String s: this.includes) {
            incls[i++] = s;
        }
        incls[i] = incl;

        this.includes = incls;
    }

    /**
     * Append the given string to the array of excluded file patterns
     * @param excl File pattern string to append to <code>this.excludes</code>
     */
    public void addExcludes( String excl ) {
        String[] excls = new String[this.excludes.length + 1];
        int i = 0;
        for (String s: this.excludes) {
            excls[i++] = s;
        }
        excls[i] = excl;
        this.excludes = excls;
    }

    /**
     * Return the base directory for the plugin, which should be supplied
     * by plexus, but if not we default to the current working directory.
     *
     * @return The base directory as a file
     */
    protected File getBaseDir() {
        return (baseDir == null) ? new File(".").getAbsoluteFile() : baseDir;
    }

    /**
     * Ensure that the output directory exists
     */
    protected void ensureTargetDirectory( SchemagenOptions so )
        throws MojoExecutionException
    {
        File gs = new File( so.getOutputOption() );

        if (!gs.exists()) {
            gs.mkdirs();
        }
        else if (!gs.isDirectory()) {
            getLog().error( "The output location is not a directory: " + gs.getPath() );
            throw new MojoExecutionException( "Already exists as file: " + gs.getPath() );
        }
        else if (!gs.canWrite()) {
            getLog().error( "Output directory exists but is not writable: " + gs.getPath() );
            throw new MojoExecutionException( "Not writable: " + gs.getPath() );
        }
    }


    /***********************************/
    /* Inner classes                   */
    /***********************************/

    /**
     * Adapter class to invoke the schemagen tool with a given set of options
     */
    protected class SchemagenAdapter
        extends schemagen
    {
        public void run( SchemagenOptions options ) {
            go( options );
        }
    }
}

