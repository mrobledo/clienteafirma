package es.gob.afirma.cert.signvalidation;

public interface SignValider {

	/** Valida una firma XML.
     * @param sign Firma a validar
     * @return Validez de la firma. */
    SignValidity validate(final byte[] sign);
}
