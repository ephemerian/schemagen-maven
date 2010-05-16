/*****************************************************************************
 * File:    SchemagenOptions.java
 * Project: schemagen
 * Created: 2 Apr 2010
 * By:      ian
 *
 * Copyright (c) 2010 Epimorphics Ltd. All rights reserved.
 *****************************************************************************/

// Package
///////////////

package org.openjena.tools.schemagen;


// Imports
///////////////

import java.util.*;

import jena.schemagen;
import jena.schemagen.OptionDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.*;


/**
 * <p>An extension to the option class built in to {@link schemagen}, in which we
 * allow a two-level defaults hierarchy. Each option is tested against the local
 * object. If the result is <code>true</code> or non-null, or if the object has
 * no parent options object, then the result stands. Otherwise, the option value
 * is delegated to the parent. This allows us to specify global defaults for an
 * entire group of files to be processed with maven, while still allowing each
 * file to have its own local options.
 * </p>
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
*/
public class SchemagenOptions
    extends schemagen.SchemagenOptionsImpl
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( SchemagenOptions.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The parent options for this options instance */
    private SchemagenOptions parent;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public SchemagenOptions() {
        super( new String[]{} );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Set the parent options object for this object
     * @param parent Parent options object, or null
     */
    protected void setParent( SchemagenOptions parent ) {
        this.parent = parent;
    }

    /**
     * Return the parent options object, or null
     * @return
     */
    public SchemagenOptions getParent() {
        return parent;
    }

    /**
     * Return true if this options object has a parent
     * @return
     */
    public boolean hasParent() {
        return getParent() != null;
    }

    /**
     * Get the value of the given option, as a string. If the option is not defined
     * locally, return the value of the same option of the parent, if the parent
     * is non-null. Otherwise, return <code>null</code>
     * @param option The name of the option to retrieve
     * @return The value of the option as a string, or null if the option is not defined. If
     * the parent is non-null and the option is not defined, delegate the <code>getOption</code>
     * to the parent.
     * @return The string value of the option, or null
     */
    public String getStringOption( OPT option ) {
        String v = getStringValue( option );
        return (v != null) ? v : (parent != null ? parent.getStringOption( option ) : null);
    }

    /**
     * Get the value of the given option, as an RDF node. If the option is not defined
     * locally, return the value of the same option of the parent, if the parent
     * is non-null. Otherwise, return <code>null</code>
     * @param option The name of the option to retrieve
     * @return The value of the option as an RDFNode, or null if the option is not defined. If
     * the parent is non-null and the option is not defined, delegate the <code>getOption</code>
     * to the parent.
     * @return The RDFnode value of the option, or null
     */
    public RDFNode getOption( OPT option ) {
        RDFNode v = getValue( option );
        return (v != null) ? v : (parent != null ? parent.getOption( option ) : null);
    }

    /**
     * Set the value of the given option in the local options list
     * @param optionName The option to set, as a string value
     * @param value
     */
    public void setOption( String optionName, String value ) {
        setOption( asOption( optionName ), value );
    }

    /**
     * Set the value of the given option in the local options list
     * @param option The option to set
     * @param value The string value of the option
     */
    public void setOption( OPT option, String value ) {
        OptionDefinition od = getOpt( option );
        getConfigRoot().addProperty( od.getDeclarationProperty(), value );
    }

    /**
     * Set the value of the given option in the local options list
     * @param option The option to set
     * @param value The Boolean value of the option
     */
    public void setOption( OPT option, boolean value ) {
        OptionDefinition od = getOpt( option );
        getConfigRoot().addProperty( od.getDeclarationProperty(), ResourceFactory.createTypedLiteral( value ) );
    }

    /**
     * Set the value of the given option in the local options list
     * @param option The option to set
     * @param value The Resource value of the option
     */
    public void setOption( OPT option, Resource value ) {
        OptionDefinition od = getOpt( option );
        getConfigRoot().addProperty( od.getDeclarationProperty(), value );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    protected OPT asOption( String optString ) {
        return OPT.valueOf( optString );
    }

    /**
     * Return true if the given option is set to true, either locally or
     * in the parent options object.
     */
    @Override
    protected boolean isTrue( OPT option ) {
        return super.isTrue( option ) || (hasParent() && getParent().isTrue( option ));
    }

    /**
     * Return true if the given option has a value, either locally or
     * in the parent options object.
     */
    @Override
    protected boolean hasValue( OPT option ) {
        return super.hasValue( option ) || (hasParent() && getParent().hasValue( option ));
    }

    /**
     * Return the value of the option or null, , either locally or
     * from the parent options object.
     */
    @Override
    protected RDFNode getValue( OPT option ) {
        RDFNode v = super.getValue( option );
        return (v == null && hasParent()) ? getParent().getValue( option ) : v;
    }

    /**
     * Return the value of the option or null, , either locally or
     * from the parent options object.
     */
    @Override
    protected String getStringValue( OPT option ) {
        String v = super.getStringValue( option );
        return (v == null && hasParent()) ? getParent().getStringValue( option ) : v;
    }

    /**
     * Return true if the given option has a resource value, either locally or
     * in the parent options object.
     */
    @Override
    protected boolean hasResourceValue( OPT option ) {
        return super.hasResourceValue( option ) || (hasParent() && getParent().hasResourceValue( option ));
    }

    /**
     * Return the value of the option or null, , either locally or
     * from the parent options object.
     */
    @Override
    protected Resource getResource( OPT option ) {
        Resource r =  super.getResource( option );
        return (r == null && hasParent()) ? getParent().getResource( option ) : r;
    }

    /**
     * Return all values for the given options as Strings, either locally or
     * from the parent options object.
     */
    @Override
    protected List<String> getAllValues( OPT option ) {
        List<String> l = super.getAllValues( option );
        return (l.isEmpty() && hasParent()) ? getParent().getAllValues( option ) : l;
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

    /**
     * Default options for schemagen if no other options are specified
     */
    public static class DefaultSchemagenOptions
        extends SchemagenOptions
    {
        public DefaultSchemagenOptions() {
            setOption( OPT.OUTPUT, SchemagenMojo.getBuildDir() + "/generated-sources" );
        }
    }
}