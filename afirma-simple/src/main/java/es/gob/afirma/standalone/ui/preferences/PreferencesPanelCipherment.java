package es.gob.afirma.standalone.ui.preferences;

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

	private final JCheckBox onlyEncipherment = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanelCipherment.21")); //$NON-NLS-1$
	private boolean isOnlyEncipherment() {
		return this.onlyEncipherment.isSelected();
	}

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

	private final JButton checkUriButton = new JButton(SimpleAfirmaMessages.getString("PreferencesPanelCipherment.16")); //$NON-NLS-1$

	PreferencesPanelCipherment(final KeyListener keyListener,
					            final ItemListener modificationListener,
					            final boolean unprotected) {
		createUI(keyListener, modificationListener, unprotected);
	}

	void savePreferences() {
		PreferencesManager.putBoolean(PreferencesManager.PREFERENCE_CIPHERMENT_ONLY_CYPHER_CERTS, isOnlyEncipherment());
		PreferencesManager.put(PreferencesManager.PREFERENCE_CIPHERMENT_ALGORITHM, getSelectedCipherAlgorithm());
		PreferencesManager.put(PreferencesManager.PREFERENCE_CIPHERMENT_METHOD, getSelectedAccessMethod());
		if (getURI() != null && !getURI().trim().isEmpty()) {
			if (checkURI()) {
				PreferencesManager.put(PreferencesManager.PREFERENCE_CIPHERMENT_URI, getURI());
			}
			else {
				AOUIFactory.showErrorMessage(
						getParent(),
						SimpleAfirmaMessages.getString("PreferencesPanelCipherment.18"),  //$NON-NLS-1$
						SimpleAfirmaMessages.getString("PreferencesPanelCipherment.19"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE
						);
			}
		}
	}

	void loadPreferences() {

		this.onlyEncipherment.setSelected(
				PreferencesManager.getBoolean(
						PreferencesManager.PREFERENCE_CIPHERMENT_ONLY_CYPHER_CERTS,
						true
						)
				);

		this.cipherAlgorithms.setSelectedItem(
				PreferencesManager.get(
						PreferencesManager.PREFERENCE_CIPHERMENT_ALGORITHM,
						CIPHER_ALGOS[0]
						)
				);

		this.accessMethods.setSelectedItem(
				PreferencesManager.get(
						PreferencesManager.PREFERENCE_CIPHERMENT_METHOD,
						ACCESS_METHODS[0]
						)
				);

		this.directoryURI.setText(
				PreferencesManager.get(
						PreferencesManager.PREFERENCE_CIPHERMENT_URI,
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
			SimpleAfirmaMessages.getString("PreferencesPanelCipherment.0") //$NON-NLS-1$
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
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanelCipherment.1")) //$NON-NLS-1$
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
				BorderFactory.createEmptyBorder(), SimpleAfirmaMessages.getString("PreferencesPanelCipherment.2") //$NON-NLS-1$
			)
		);

		final GridBagConstraints kupc = new GridBagConstraints();
		kupc.fill = GridBagConstraints.HORIZONTAL;
		kupc.weightx = 1.0;
		kupc.gridx = 0;
		kupc.gridy = 0;
		kupc.insets = new Insets(5, 0, 0, 7);

		this.onlyEncipherment.getAccessibleContext().setAccessibleDescription(
				SimpleAfirmaMessages.getString("PreferencesPanelCipherment.22") //$NON-NLS-1$
				);
		this.onlyEncipherment.setMnemonic('r');
		this.onlyEncipherment.addItemListener(modificationListener);
		this.onlyEncipherment.addKeyListener(keyListener);
		this.onlyEncipherment.setEnabled(unprotected);

		kupc.gridy++;
		keyUsagesPanel.add(this.onlyEncipherment, kupc);

		final JPanel algorithmPanel = new JPanel(new GridBagLayout());
		algorithmPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), SimpleAfirmaMessages.getString("PreferencesPanelCipherment.12") //$NON-NLS-1$
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
				BorderFactory.createEmptyBorder(), SimpleAfirmaMessages.getString("PreferencesPanelCipherment.13") //$NON-NLS-1$
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
			SimpleAfirmaMessages.getString("PreferencesPanelCipherment.14") //$NON-NLS-1$
		);
		directoryURILabel.setLabelFor(this.directoryURI);

		this.directoryURI.addKeyListener(keyListener);
		this.directoryURI.setEnabled(unprotected);

		this.checkUriButton.setMnemonic('b');
		this.checkUriButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanelCipherment.17") //$NON-NLS-1$
		);
		this.checkUriButton.addActionListener(
			new ActionListener () {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (checkURI()) {
						AOUIFactory.showMessageDialog(
							getParent(),
							SimpleAfirmaMessages.getString("PreferencesPanelCipherment.20"),  //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanelCipherment.19"), //$NON-NLS-1$
							JOptionPane.INFORMATION_MESSAGE
						);
					}
					else {
						AOUIFactory.showErrorMessage(
							getParent(),
							SimpleAfirmaMessages.getString("PreferencesPanelCipherment.18"),  //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanelCipherment.19"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE
						);
					}
				}
			}
		);

		final JLabel methodLabel = new JLabel(
			SimpleAfirmaMessages.getString("PreferencesPanelCipherment.15") //$NON-NLS-1$
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
