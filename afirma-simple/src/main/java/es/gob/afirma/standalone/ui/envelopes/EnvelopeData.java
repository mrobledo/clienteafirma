package es.gob.afirma.standalone.ui.envelopes;

import java.util.ArrayList;
import java.util.List;

/** Datos de configuraci&oacute;n del sobre digital.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class EnvelopeData {

	private String filePath = null;
	String getFilePath() {
		return this.filePath;
	}

	/** Establece la ruta hacia el fichero a ensobrar.
	 * @param path Ruta hacia el fichero a ensobrar. */
	public void setFilePath(final String path) {
		this.filePath = path;
	}

	private EnvelopesTypeResources envelopeType = null;
	EnvelopesTypeResources getEnvelopeType() {
		return this.envelopeType;
	}
	void setEnvelopeType(final EnvelopesTypeResources type) {
		this.envelopeType = type;
	}

	private String signatureAlgorithm = null;
	String getSignatureAlgorithm() {
		return this.signatureAlgorithm;
	}
	void setSignatureAlgorithm(final String algo) {
		this.signatureAlgorithm = algo;
	}

	private final List<CertificateDestiny> certificateRecipientsList = new ArrayList<>();
	List<CertificateDestiny> getCertificateRecipientsList() {
		return this.certificateRecipientsList;
	}
	void addCertificateRecipients(final List<CertificateDestiny> newRecipients) {
		if (newRecipients != null) {
			this.certificateRecipientsList.addAll(newRecipients);
		}
	}

}
