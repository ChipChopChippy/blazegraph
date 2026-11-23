package com.bigdata.gom.gpo;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * The BasicSkin provides standard type wrappers to help with conversions
 * 
 * @author Martyn Cutcher
 */
public class BasicSkin implements IGenericSkin {
	
	final protected IGPO m_gpo;
	final protected ValueFactory m_vf;
	
	public BasicSkin(final IGPO gpo) {
		m_gpo = gpo;
		m_vf = gpo.getObjectManager().getValueFactory();
	}
	
	public void rollback() {
		((GPO) m_gpo).setMaterialized(false); // forces reload
	}
	
	public void setValue(final IRI property, final Value value) {
    	m_gpo.setValue(property, value);
    }

    public void setValue(final IRI property, final String value) {
    	m_gpo.setValue(property, m_vf.createLiteral(value));
    }

    public void setValue(final IRI property, final int value) {
    	m_gpo.setValue(property, m_vf.createLiteral(value));
    }

    public void setValue(final IRI property, final double value) {
    	m_gpo.setValue(property, m_vf.createLiteral(value));
    }

    public void setValue(final IRI property, final XMLGregorianCalendar value) {
    	m_gpo.setValue(property, m_vf.createLiteral(value));
    }
    
	public void setValue(final String property, final Value value) {
    	m_gpo.setValue(m_vf.createIRI(property), value);
    }

    public void setValue(final String property, final String value) {
    	m_gpo.setValue(m_vf.createIRI(property), m_vf.createLiteral(value));
    }

    public void setValue(final String property, final int value) {
    	m_gpo.setValue(m_vf.createIRI(property), m_vf.createLiteral(value));
    }

    public void setValue(final String property, final double value) {
    	m_gpo.setValue(m_vf.createIRI(property), m_vf.createLiteral(value));
    }

    public void setValue(final String property, final XMLGregorianCalendar value) {
    	m_gpo.setValue(m_vf.createIRI(property), m_vf.createLiteral(value));
    }

	@Override
	public IGPO asGeneric() {
		return m_gpo;
	}

	public int getIntValue(final IRI key) {
		final Value v = m_gpo.getValue(key);
		
		if (v instanceof Literal) {
			return ((Literal) v).intValue();
		} else {	
			return 0;
		}
	}

	public double getDoubleValue(final IRI key) {
		final Value v = m_gpo.getValue(key);
		
		if (v instanceof Literal) {
			return ((Literal) v).doubleValue();
		} else {	
			return 0;
		}
	}

	public boolean getBooleanValue(final IRI key) {
		final Value v = m_gpo.getValue(key);
		
		if (v instanceof Literal) {
			return ((Literal) v).booleanValue();
		} else {	
			return false;
		}
	}

	public String getStringValue(final IRI key) {
		final Value v = m_gpo.getValue(key);
		
		if (v instanceof Literal) {
			return ((Literal) v).stringValue();
		} else {	
			return null;
		}
	}

	public IGPO getGPOValue(final IRI key) {
		final Value v = m_gpo.getValue(key);
		
		if (v instanceof Resource) {
			return m_gpo.getObjectManager().getGPO((Resource) v);
		} else {	
			return null;
		}
	}

	public int getInt(final String key) {
		return getIntValue(m_vf.createIRI(key));
	}

	public double getDouble(final String key) {
		return getDoubleValue(m_vf.createIRI(key));
	}

	public boolean getBoolean(final String key) {
		return getBooleanValue(m_vf.createIRI(key));
	}

	public String getString(final String key) {
		return getStringValue(m_vf.createIRI(key));
	}

}
