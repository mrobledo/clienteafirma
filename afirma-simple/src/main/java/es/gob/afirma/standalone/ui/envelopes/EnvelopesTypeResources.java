package es.gob.afirma.standalone.ui.envelopes;

import es.gob.afirma.standalone.SimpleAfirmaMessages;

public enum EnvelopesTypeResources {

	SIGNED(			SimpleAfirmaMessages.getString("MenuDigitalEnvelope.6")), //$NON-NLS-1$
	//AUTHENTICATED(	SimpleAfirmaMessages.getString("MenuDigitalEnvelope.5")), //$NON-NLS-1$
	SIMPLE(			SimpleAfirmaMessages.getString("MenuDigitalEnvelope.7")); //$NON-NLS-1$

	private final String envTypeName;

	private EnvelopesTypeResources(final String name) {
		this.envTypeName = name;
	}

	@Override
	public String toString() {
		return this.envTypeName;
	}
}