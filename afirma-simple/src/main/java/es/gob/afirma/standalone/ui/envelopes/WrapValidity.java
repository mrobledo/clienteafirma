package es.gob.afirma.standalone.ui.envelopes;

/**
 * Distintos estados de validez que puede adoptar un sobre electr&oacute;nico.
 */
public enum WrapValidity {
	VALID,
	INVALID_SIGNATURE,
	INVALID_CERT,
	INVALID_STRUCTURE,
	INVALID_MAC,
	UNKNOWN
}
