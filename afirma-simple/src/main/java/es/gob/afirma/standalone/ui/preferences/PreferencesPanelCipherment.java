package es.gob.afirma.standalone.ui.preferences;

import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_ALGORITHM;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_CRLSIGN;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_DATAENCIPHERMENT;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_DECIPHERONLY;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_DIGITALSIGNATURE;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_ENCIPHERONLY;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_KEYAGREEMENT;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_KEYCERTSIGN;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_KEYENCHIPERMENT;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_METHOD;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_NONREPUDIATION;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_CIPHERMENT_URI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.SimpleAfirmaMessages;

final class PreferencesPanelCipherment extends JPanel {

	private static final long serialVersionUID = -6602008231996534490L;
	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String[] CIPHER_ALGOS = new String[] {
			"AES256", //$NON-NLS-1$
			"Algoritmo 2", //$NON-NLS-1$
			"Algoritmo 3", //$NON-NLS-1$
	};

	private static final String[] ACCESS_METHODS = new String[] {
			"Metodo 1", //$NON-NLS-1$
			"Metodo 2", //$NON-NLS-1$
			"Metodo 3", //$NON-NLS-1$
	};

	private final JCheckBox digitalSignature = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.3"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_DIGITALSIGNATURE, true)
	);

	private final JCheckBox nonRepudiation = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.4"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_NONREPUDIATION, false)
	);

	private final JCheckBox keyEncipherment = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.5"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_KEYENCHIPERMENT, true)
	);

	private final JCheckBox dataEncipherment = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.6"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_DATAENCIPHERMENT, false)
	);

	private final JCheckBox keyAgreement = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.7"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_KEYAGREEMENT, false)
	);

	private final JCheckBox keyCertSign = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.8"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_KEYCERTSIGN, false)
	);

	private final JCheckBox cRLSign = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.9"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_CRLSIGN, false)
	);

	private final JCheckBox encipherOnly = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.10"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_ENCIPHERONLY, false)
	);

	private final JCheckBox decipherOnly = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelCiperment.11"), //$NON-NLS-1$
		PreferencesManager.getBoolean(PREFERENCE_CIPHERMENT_DECIPHERONLY, false)
	);

	private final JComboBox<String> cipherAlgorithms = new JComboBox<>(CIPHER_ALGOS);
	String getSelectedCipherAlgorithm() {
		return this.cipherAlgorithms.getSelectedItem().toString();
	}

	private final JComboBox<String> accessMethods = new JComboBox<>(ACCESS_METHODS);
	String getSelectedAccessMethod() {
		return this.accessMethods.getSelectedItem().toString();
	}

	private final JTextField directoryURI = new JTextField();
	String getURI() {
		return this.directoryURI.getText();
	}

	private final JButton checkUriButton = new JButton(SimpleAfirmaMessages.getString("PreferencesPanelCiperment.16")); //$NON-NLS-1$

	PreferencesPanelCipherment(final KeyListener keyListener,
					            final ItemListener modificationListener,
					            final boolean unprotected) {
		createUI(keyListener, modificationListener, unprotected);
	}

	void savePreferences() {
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_DIGITALSIGNATURE, this.digitalSignature.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_NONREPUDIATION, this.nonRepudiation.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_KEYENCHIPERMENT, this.keyEncipherment.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_DATAENCIPHERMENT, this.dataEncipherment.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_KEYAGREEMENT, this.keyAgreement.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_KEYCERTSIGN, this.keyCertSign.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_CRLSIGN, this.cRLSign.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_ENCIPHERONLY, this.encipherOnly.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_CIPHERMENT_DECIPHERONLY, this.decipherOnly.isSelected());
		PreferencesManager.put(PREFERENCE_CIPHERMENT_ALGORITHM, getSelectedCipherAlgorithm());
		PreferencesManager.put(PREFERENCE_CIPHERMENT_METHOD, getSelectedAccessMethod());
		if (checkURI()) {
			PreferencesManager.put(PREFERENCE_CIPHERMENT_URI, getURI());
		}
		else {
			AOUIFactory.showErrorMessage(
				getParent(),
				SimpleAfirmaMessages.getString("PreferencesPanelCiperment.18"),  //$NON-NLS-1$
				SimpleAfirmaMessages.getString("PreferencesPanelCiperment.19"), //$NON-NLS-1$
				JOptionPane.ERROR_MESSAGE
			);
		}

	}

	void loadPreferences() {
		this.cipherAlgorithms.setSelectedItem(
			PreferencesManager.get(
				PREFERENCE_CIPHERMENT_ALGORITHM,
				CIPHER_ALGOS[0]
			)
		);

		this.accessMethods.setSelectedItem(
			PreferencesManager.get(
				PREFERENCE_CIPHERMENT_METHOD,
				ACCESS_METHODS[0]
			)
		);

		this.directoryURI.setText(
			PreferencesManager.get(
				PREFERENCE_CIPHERMENT_URI,
				"" //$NON-NLS-1$
			)
		);
	}

	boolean checkURI() {
		if (getURI() != null && !getURI().trim().isEmpty()) {
			try {
				final URL url = new URL(getURI());
				url.toURI();
				return true;
			}
			catch(final Exception e) {
				LOGGER.info("La URI introducida no es correcta: " + e); //$NON-NLS-1$
			}
		}
		return false;
	}

	void createUI(final KeyListener keyListener,
				  final ItemListener modificationListener,
				  final boolean unprotected) {


		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanelCiperment.0") //$NON-NLS-1$
		);

		setLayout(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;

		final JPanel cipherConfigPanel = new JPanel(new GridBagLayout());
		cipherConfigPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanelCiperment.1")) //$NON-NLS-1$
			)
		);

		final GridBagConstraints ccc = new GridBagConstraints();
		ccc.fill = GridBagConstraints.HORIZONTAL;
		ccc.weightx = 1.0;
		ccc.gridx = 0;
		ccc.gridy = 0;

		final JPanel keyUsagesPanel = new JPanel(new GridBagLayout());
		keyUsagesPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), SimpleAfirmaMessages.getString("PreferencesPanelCiperment.2") //$NON-NLS-1$
			)
		);

		final GridBagConstraints kupc = new GridBagConstraints();
		kupc.fill = GridBagConstraints.HORIZONTAL;
		kupc.weightx = 1.0;
		kupc.gridx = 0;
		kupc.gridy = 0;
		kupc.insets = new Insets(5, 0, 0, 7);

		this.digitalSignature.addKeyListener(keyListener);
		this.digitalSignature.addItemListener(modificationListener);
		this.digitalSignature.setMnemonic('s');
		this.digitalSignature.setEnabled(unprotected);
		this.nonRepudiation.addKeyListener(keyListener);
		this.nonRepudiation.addItemListener(modificationListener);
		this.nonRepudiation.setMnemonic('n');
		this.nonRepudiation.setEnabled(unprotected);
		this.keyEncipherment.addKeyListener(keyListener);
		this.keyEncipherment.addItemListener(modificationListener);
		this.keyEncipherment.setMnemonic('k');
		this.keyEncipherment.setEnabled(unprotected);
		this.dataEncipherment.addKeyListener(keyListener);
		this.dataEncipherment.addItemListener(modificationListener);
		this.dataEncipherment.setMnemonic('d');
		this.dataEncipherment.setEnabled(unprotected);
		this.keyAgreement.addKeyListener(keyListener);
		this.keyAgreement.addItemListener(modificationListener);
		this.keyAgreement.setMnemonic('t');
		this.keyAgreement.setEnabled(unprotected);
		this.keyCertSign.addKeyListener(keyListener);
		this.keyCertSign.addItemListener(modificationListener);
		this.keyCertSign.setMnemonic('y');
		this.keyCertSign.setEnabled(unprotected);
		this.cRLSign.addKeyListener(keyListener);
		this.cRLSign.addItemListener(modificationListener);
		this.cRLSign.setMnemonic('r');
		this.cRLSign.setEnabled(unprotected);
		this.encipherOnly.addKeyListener(keyListener);
		this.encipherOnly.addItemListener(modificationListener);
		this.encipherOnly.setMnemonic('h');
		this.encipherOnly.setEnabled(unprotected);
		this.decipherOnly.addKeyListener(keyListener);
		this.decipherOnly.addItemListener(modificationListener);
		this.decipherOnly.setMnemonic('o');
		this.decipherOnly.setEnabled(unprotected);

		keyUsagesPanel.add(this.digitalSignature, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.nonRepudiation, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.keyEncipherment, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.dataEncipherment, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.keyAgreement, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.keyCertSign, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.cRLSign, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.encipherOnly, kupc);
		kupc.gridy++;
		keyUsagesPanel.add(this.decipherOnly, kupc);

		final JPanel algorithmPanel = new JPanel(new GridBagLayout());
		algorithmPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), SimpleAfirmaMessages.getString("PreferencesPanelCiperment.12") //$NON-NLS-1$
			)
		);

		final GridBagConstraints apc = new GridBagConstraints();
		apc.fill = GridBagConstraints.NONE;
		apc.anchor = GridBagConstraints.LINE_START;
		apc.weightx = 1.0;
		apc.gridx = 0;
		apc.gridy = 0;
		apc.insets = new Insets(5, 0, 0, 7);

		this.cipherAlgorithms.addKeyListener(keyListener);
		this.cipherAlgorithms.addItemListener(modificationListener);
		this.cipherAlgorithms.setEnabled(unprotected);

		algorithmPanel.add(this.cipherAlgorithms, apc);

		final JPanel repositoryPanel = new JPanel(new GridBagLayout());
		repositoryPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), SimpleAfirmaMessages.getString("PreferencesPanelCiperment.13") //$NON-NLS-1$
			)
		);

		final GridBagConstraints rpc = new GridBagConstraints();
		rpc.fill = GridBagConstraints.HORIZONTAL;
		rpc.anchor = GridBagConstraints.LINE_START;
		rpc.weightx = 1.0;
		rpc.gridx = 0;
		rpc.gridy = 0;
		rpc.insets = new Insets(5, 5, 0, 7);

		final JLabel directoryURILabel = new JLabel(
			SimpleAfirmaMessages.getString("PreferencesPanelCiperment.14") //$NON-NLS-1$
		);
		directoryURILabel.setLabelFor(this.directoryURI);

		this.directoryURI.addKeyListener(keyListener);
		this.directoryURI.setEnabled(unprotected);

		this.checkUriButton.setMnemonic('b');
		this.checkUriButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanelCiperment.17") //$NON-NLS-1$
		);
		this.checkUriButton.addActionListener(
			new ActionListener () {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (checkURI()) {
						AOUIFactory.showMessageDialog(
							getParent(),
							SimpleAfirmaMessages.getString("PreferencesPanelCiperment.20"),  //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanelCiperment.19"), //$NON-NLS-1$
							JOptionPane.INFORMATION_MESSAGE
						);
					}
					else {
						AOUIFactory.showErrorMessage(
							getParent(),
							SimpleAfirmaMessages.getString("PreferencesPanelCiperment.18"),  //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanelCiperment.19"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			}
		);

		final JLabel methodLabel = new JLabel(
			SimpleAfirmaMessages.getString("PreferencesPanelCiperment.15") //$NON-NLS-1$
		);
		methodLabel.setLabelFor(this.accessMethods);

		this.accessMethods.addKeyListener(keyListener);
		this.accessMethods.addItemListener(modificationListener);
		this.accessMethods.setEnabled(unprotected);

		repositoryPanel.add(directoryURILabel, rpc);
		rpc.gridy++;
		rpc.insets = new Insets(5, 0, 0, 7);
		repositoryPanel.add(this.directoryURI, rpc);
		rpc.fill = GridBagConstraints.NONE;
		rpc.gridx++;
		rpc.gridwidth = GridBagConstraints.REMAINDER;
		rpc.weightx = 0.0;
		repositoryPanel.add(this.checkUriButton, rpc);
		rpc.gridx--;
		rpc.gridwidth  = GridBagConstraints.LINE_START;
		rpc.gridy++;
		rpc.insets = new Insets(10, 5, 0, 7);
		repositoryPanel.add(methodLabel, rpc);
		rpc.insets = new Insets(5, 0, 0, 7);
		rpc.gridy++;
		repositoryPanel.add(this.accessMethods, rpc);


		cipherConfigPanel.add(keyUsagesPanel, ccc);
		ccc.gridy++;
		ccc.insets = new Insets(20, 0, 0, 0);
		cipherConfigPanel.add(algorithmPanel, ccc);
		ccc.insets = new Insets(20, 0, 0, 0);
		ccc.gridy++;
		cipherConfigPanel.add(repositoryPanel, ccc);

		add(cipherConfigPanel, gbc);
		gbc.gridy++;
		gbc.weighty = 1.0;
		add(new JPanel(), gbc);
		loadPreferences();
	}
}
