package es.gob.afirma.cert.signvalidation;

import java.io.IOException;

public interface SignValider {

	/** Valida una firma del tipo del validador instanciado.
     * @param sign Firma a validar
     * @return Validez de la firma. */
    SignValidity validate(final byte[] sign) throws IOException;
}
