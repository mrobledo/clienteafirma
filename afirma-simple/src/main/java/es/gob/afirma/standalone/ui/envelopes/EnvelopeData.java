package es.gob.afirma.standalone.ui.envelopes;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.ArrayList;
import java.util.List;

import es.gob.afirma.keystores.AOKeyStoreManager;

/** Datos de configuraci&oacute;n del sobre digital.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class EnvelopeData {

	private String filePath = null;

	private EnvelopesTypeResources envelopeType = null;

	private PrivateKeyEntry senderPrivateKeyEntry = null;

	private AOKeyStoreManager senderKeyStoreManager = null;

	private String senderCertificateAlias = null;

	private String signatureAlgorithm = null;

	private final List<CertificateDestiny> certificateRecipientsList = new ArrayList<>();


	String getFilePath() {
		return this.filePath;
	}

	/** Establece la ruta hacia el fichero a ensobrar.
	 * @param path Ruta hacia el fichero a ensobrar. */
	public void setFilePath(final String path) {
		this.filePath = path;
	}

	EnvelopesTypeResources getEnvelopeType() {
		return this.envelopeType;
	}
	public void setEnvelopeType(final EnvelopesTypeResources type) {
		this.envelopeType = type;
	}

	String getSignatureAlgorithm() {
		return this.signatureAlgorithm;
	}
	void setSignatureAlgorithm(final String algo) {
		this.signatureAlgorithm = algo;
	}

	List<CertificateDestiny> getCertificateRecipientsList() {
		return this.certificateRecipientsList;
	}
	void clearCertificateRecipientsList() {
		this.certificateRecipientsList.clear();
	}

	public PrivateKeyEntry getSenderPrivateKeyEntry() {
		return this.senderPrivateKeyEntry;
	}

	public void setSenderPrivateKeyEntry(final PrivateKeyEntry senderPrivateKeyEntry) {
		this.senderPrivateKeyEntry = senderPrivateKeyEntry;
	}

	public AOKeyStoreManager getSenderKeyStoreManager() {
		return this.senderKeyStoreManager;
	}

	public void setSenderKeyStoreManager(final AOKeyStoreManager senderKeyStoreManager) {
		this.senderKeyStoreManager = senderKeyStoreManager;
	}

	public String getSenderCertificateAlias() {
		return this.senderCertificateAlias;
	}

	public void setSenderCertificateAlias(final String senderCertificateAlias) {
		this.senderCertificateAlias = senderCertificateAlias;
	}
}
