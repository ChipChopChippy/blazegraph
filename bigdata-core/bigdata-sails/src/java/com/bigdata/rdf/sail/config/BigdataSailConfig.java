
package com.bigdata.rdf.sail.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.AbstractSailImplConfig;

public class BigdataSailConfig extends AbstractSailImplConfig {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String propertiesFile;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BigdataSailConfig(final String type) {
		super(type);
	}
	
	/*---------*
	 * Methods *
	 *---------*/

	public String getPropertiesFile() {
		return propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

    public Properties getProperties() 
            throws FileNotFoundException, IOException {
        
        if (propertiesFile == null) {
            return new Properties();
        }
        
        FileInputStream is = new FileInputStream(new File(propertiesFile));
        Properties props = new Properties();
        props.load(is);
        return props;
        
    }
    
	@Override
	public Resource export(Model graph)
	{
		Resource implNode = super.export(graph);

		if (propertiesFile != null) {
			graph.add(implNode, BigdataConfigSchema.PROPERTIES, 
                    org.eclipse.rdf4j.model.impl.SimpleValueFactory.getInstance()
                            .createLiteral(propertiesFile));
		}

		return implNode;
	}

	@Override
	public void parse(Model graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);

        Literal propertiesLit = Models.objectLiteral(
                graph.filter(implNode, BigdataConfigSchema.PROPERTIES, null))
                .orElseThrow(() -> new SailConfigException("Properties file required"));
        setPropertiesFile(propertiesLit.getLabel());
	}
}
