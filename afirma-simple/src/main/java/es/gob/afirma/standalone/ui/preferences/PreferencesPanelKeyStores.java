package es.gob.afirma.standalone.ui.preferences;

import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_ACCEPTED_POLICIES_ONLY_CERTS;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_ALIAS_ONLY_CERTS;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_CLOSE_KEYSTORE;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_CLOSE_KEYSTORE_TIMEOUT;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_DEFAULT_STORE;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_PRIORITARY_STORE;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_KEYSTORE_SIGN_ONLY_CERTS;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.SimpleKeyStoreManager;
import es.gob.afirma.ui.core.jse.certificateselection.CertificateSelectionDialog;

final class PreferencesPanelKeyStores extends JPanel {

	private static final long serialVersionUID = 3255071607793273334L;

	private static final int MAX_WAIT_TIME = Integer.MAX_VALUE;
	private static final int MIN_WAIT_TIME = 0;
	private static final int STEP_WAIT_TIME = 1;
	private static final int INIT_WAIT_TIME = 0;

	private final JCheckBox onlySignature = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.0")); //$NON-NLS-1$
	private final JCheckBox onlyAlias = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.4")); //$NON-NLS-1$
	private final JCheckBox closeKeyStore = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.21")); //$NON-NLS-1$

    private final JButton configureCertPoliciesButton = new JButton(SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.13")); //$NON-NLS-1$

	SpinnerNumberModel sizeSpinnerModel = new SpinnerNumberModel(
		INIT_WAIT_TIME,
		MIN_WAIT_TIME,
		MAX_WAIT_TIME,
		STEP_WAIT_TIME
	);
	JSpinner sizeSpinner = new JSpinner(this.sizeSpinnerModel);
	String getSelectedTimeout() {
		//Se obtiene en minutos y se devuelve en segundos
		return Integer.toString(Integer.parseInt(this.sizeSpinner.getValue().toString())*60);
	}
	void setSizeSpinnerEnabled(final boolean enable) {
		this.sizeSpinner.setEnabled(enable);
	}
	JSpinner getSizeSpinner() {
		return this.sizeSpinner;
	}

	private final JComboBox<String> prioritaryKeyStoreComboBox = new JComboBox<>(
		new String[] {
			SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.20"), // Ninguno //$NON-NLS-1$
			AOKeyStore.DNIEJAVA.toString(),
			AOKeyStore.CERES.toString(),
			AOKeyStore.TEMD.toString()
		}
	);

	private String getPrioritaryStore() {
		return this.prioritaryKeyStoreComboBox.getSelectedItem().toString();
	}

	private static AOKeyStore[] DEFAULT_STORES;
	static {
		final List<AOKeyStore> stores = new ArrayList<>();
		final Platform.OS os = Platform.getOS();
		if (Platform.OS.WINDOWS.equals(os)) {
			stores.add(AOKeyStore.WINDOWS);
		}
		else if (Platform.OS.MACOSX.equals(os)) {
			stores.add(AOKeyStore.APPLE);
		}
		else {
			stores.add(AOKeyStore.SHARED_NSS);
		}
		if (SimpleKeyStoreManager.isFirefoxAvailable()) {
			stores.add(AOKeyStore.MOZ_UNI);
		}
		DEFAULT_STORES = stores.toArray(new AOKeyStore[0]);
	}

	private final JComboBox<AOKeyStore> defaultStore = new JComboBox<>(DEFAULT_STORES);
	AOKeyStore getDefaultStore() {
		return this.defaultStore.getItemAt(this.defaultStore.getSelectedIndex());
	}

	private final JButton contentButton = new JButton(
		SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.9") //$NON-NLS-1$
	);

	JButton getContentButton() {
		return this.contentButton;
	}

	PreferencesPanelKeyStores(final KeyListener keyListener,
							  final ModificationListener modificationListener,
							  final boolean unprotected) {

		createUI(keyListener, modificationListener, unprotected);
	}

	void createUI(final KeyListener keyListener,
				  final ModificationListener modificationListener,
				  final boolean unprotected) {

        setLayout(new GridBagLayout());

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridy = 0;

        loadPreferences();

        final JPanel keysFilerPanel = new JPanel(new GridBagLayout());
        keysFilerPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.6")) //$NON-NLS-1$
			)
		);

		final GridBagConstraints kfc = new GridBagConstraints();
		kfc.fill = GridBagConstraints.HORIZONTAL;
		kfc.weightx = 1.0;
		kfc.gridy = 0;
		kfc.insets = new Insets(5, 7, 5, 7);

	    this.onlySignature.getAccessibleContext().setAccessibleDescription(
    		SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.2") //$NON-NLS-1$
		);
	    this.onlySignature.setMnemonic('i');
	    this.onlySignature.addItemListener(modificationListener);
	    this.onlySignature.addKeyListener(keyListener);
	    this.onlySignature.setEnabled(unprotected);
	    keysFilerPanel.add(this.onlySignature, kfc);

        kfc.gridy++;

	    this.onlyAlias.getAccessibleContext().setAccessibleDescription(
    		SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.5") //$NON-NLS-1$
		);
	    this.onlyAlias.setMnemonic('s');
	    this.onlyAlias.addItemListener(modificationListener);
	    this.onlyAlias.addKeyListener(keyListener);
	    this.onlyAlias.setEnabled(unprotected);
	    keysFilerPanel.add(this.onlyAlias, kfc);

	    final JPanel trustPanel = new JPanel();
	    trustPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.configureCertPoliciesButton.setMnemonic('F');
		this.configureCertPoliciesButton.getAccessibleContext().setAccessibleDescription(
				SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.14") //$NON-NLS-1$
		);
		this.configureCertPoliciesButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					CertPoliciesDialog.startCertPoliciesDialog((JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, getParent()));
				}
			}
		);
		this.configureCertPoliciesButton.addKeyListener(keyListener);
		this.configureCertPoliciesButton.setEnabled(unprotected);

		final JLabel certPoliciesLabel = new JLabel(
				SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.12") //$NON-NLS-1$
		);
		certPoliciesLabel.addKeyListener(keyListener);
		certPoliciesLabel.setLabelFor(this.configureCertPoliciesButton);

		trustPanel.add(certPoliciesLabel);
	    trustPanel.add(this.configureCertPoliciesButton);

	    kfc.gridy++;
	    kfc.insets = new Insets(0,2,0,0);
	    keysFilerPanel.add(trustPanel, kfc);
	    kfc.insets = new Insets(5, 7, 5, 7);

        final JPanel priorityKeysStorePanel = new JPanel(new GridBagLayout());
        priorityKeysStorePanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.18")) //$NON-NLS-1$
			)
		);

		final GridBagConstraints pksc = new GridBagConstraints();
		pksc.anchor = GridBagConstraints.LINE_START;
		pksc.weightx = 1.0;
		pksc.gridy = 0;
		pksc.gridwidth = 2;
		pksc.fill = GridBagConstraints.HORIZONTAL;
		pksc.insets = new Insets(5, 7, 5, 7);

		final JLabel cpriorityKeysStoreLabel = new JLabel(
				SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.19") //$NON-NLS-1$
		);
		cpriorityKeysStoreLabel.addKeyListener(keyListener);
		cpriorityKeysStoreLabel.setLabelFor(this.prioritaryKeyStoreComboBox);

		this.prioritaryKeyStoreComboBox.addItemListener(modificationListener);
		this.prioritaryKeyStoreComboBox.addKeyListener(keyListener);
		this.prioritaryKeyStoreComboBox.setEnabled(unprotected);

		this.closeKeyStore.addItemListener(
			new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						setSizeSpinnerEnabled(true);
					}
					else {
						setSizeSpinnerEnabled(false);
					}
				}
			}
		);
		this.closeKeyStore.addItemListener(modificationListener);
		this.closeKeyStore.addKeyListener(keyListener);
		this.closeKeyStore.setMnemonic('r');
	    this.closeKeyStore.setEnabled(unprotected);

		final JComponent comp = this.sizeSpinner.getEditor();
	    final JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
	    field.setColumns(7);
	    final DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
	    formatter.setAllowsInvalid(false);
	    this.sizeSpinner.addChangeListener(
			new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					modificationListener.keyReleased(new KeyEvent(getSizeSpinner(), 1, 1, 1, 1, 'a'));
				}
			}
		);
		this.sizeSpinner.getEditor().getComponent(0).addKeyListener(keyListener);
		this.sizeSpinner.setEnabled(this.closeKeyStore.isSelected());
		this.closeKeyStore.setEnabled(unprotected);

		final JPanel closePanel = new JPanel();
		closePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		closePanel.add(this.closeKeyStore);
		closePanel.add(this.sizeSpinner);

		priorityKeysStorePanel.add(cpriorityKeysStoreLabel, pksc);
		pksc.gridy++;
		pksc.gridwidth = 1;
		pksc.fill = GridBagConstraints.NONE;
		priorityKeysStorePanel.add(this.prioritaryKeyStoreComboBox, pksc);
		pksc.gridy++;
		priorityKeysStorePanel.add(closePanel, pksc);

        final JPanel keysStorePanel = new JPanel(new GridBagLayout());
        keysStorePanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.7")) //$NON-NLS-1$
			)
		);

		final GridBagConstraints ksc = new GridBagConstraints();
		ksc.anchor = GridBagConstraints.LINE_START;
		ksc.gridy = 0;
		ksc.insets = new Insets(5, 7, 5, 7);

		this.defaultStore.addItemListener(
			new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						SwingUtilities.invokeLater(() -> AOUIFactory.showMessageDialog(
							PreferencesPanelKeyStores.this,
							SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.16"), //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.17"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE
						));
					}
				}
			}
		);

		this.defaultStore.addItemListener(modificationListener);
		this.defaultStore.addKeyListener(keyListener);
		this.defaultStore.setEnabled(unprotected);

		//TODO: Descomentar una vez se entregue
		keysStorePanel.add(this.defaultStore, ksc);

		this.contentButton.setMnemonic('V');
		this.contentButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent ae) {
					final AOKeyStoreManager ksm;
					try {
						ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
							getDefaultStore(),
							null,
							"default", //$NON-NLS-1$
							getDefaultStore().getStorePasswordCallback(this),
							this
						);

						final CertificateSelectionDialog csd = new CertificateSelectionDialog(
							PreferencesPanelKeyStores.this,
							new AOKeyStoreDialog(
								ksm,
								this,
								true,
								true,
								false
							),
							SimpleAfirmaMessages.getString(
								"PreferencesPanelKeyStores.10", //$NON-NLS-1$
								getDefaultStore().toString()
							),
							SimpleAfirmaMessages.getString(
								"PreferencesPanelKeyStores.15", //$NON-NLS-1$
								getDefaultStore().toString()
							),
							false,
							true
						);
						csd.showDialog();
					}
					catch (final Exception e) {
						AOUIFactory.showErrorMessage(
							this,
							SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.11"), //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.10", getDefaultStore().toString()), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE
						);
						Logger.getLogger("es.gob.afirma").warning("Error al recuperar el almacen por defecto seleccionado: " + e); //$NON-NLS-1$ //$NON-NLS-2$
					}

				}
			}
		);
		this.contentButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanelKeyStores.8") //$NON-NLS-1$
		);
		this.contentButton.addKeyListener(keyListener);
		this.contentButton.setEnabled(unprotected);

		//TODO: Descomentar una vez se entregue
		keysStorePanel.add(this.contentButton, ksc);

		ksc.weightx = 1.0;
		keysStorePanel.add(new JPanel(), ksc);

	    add(keysFilerPanel, c);
	    c.gridy++;
	    add(priorityKeysStorePanel, c);
	    c.gridy++;

	  //TODO: Descomentar una vez se entregue
	    add(keysStorePanel, c);

	    c.weighty = 1.0;
	    c.gridy++;
		add(new JPanel(), c);
	}

	void savePreferences() {
		PreferencesManager.putBoolean(PREFERENCE_KEYSTORE_SIGN_ONLY_CERTS, this.onlySignature.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_KEYSTORE_ALIAS_ONLY_CERTS, this.onlyAlias.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_KEYSTORE_CLOSE_KEYSTORE, this.closeKeyStore.isSelected());
		// Si no se ha indicado que se cierre el almacen, no se guarda el tiempo de cierre para que se configure a infinito
		if (!this.closeKeyStore.isSelected()) {
			PreferencesManager.remove(PREFERENCE_KEYSTORE_CLOSE_KEYSTORE_TIMEOUT);
		}
		else {
			PreferencesManager.put(PREFERENCE_KEYSTORE_CLOSE_KEYSTORE_TIMEOUT, this.sizeSpinner.getValue().toString());
		}
		PreferencesManager.put(
			PREFERENCE_KEYSTORE_DEFAULT_STORE,
			getDefaultStore().name()
		);
		PreferencesManager.put(
			PREFERENCE_KEYSTORE_PRIORITARY_STORE,
			getPrioritaryStore()
		);
	}

	void loadPreferences() {
		this.onlySignature.setSelected(PreferencesManager.getBoolean(PREFERENCE_KEYSTORE_SIGN_ONLY_CERTS, true));
		this.onlyAlias.setSelected(PreferencesManager.getBoolean(PREFERENCE_KEYSTORE_ALIAS_ONLY_CERTS, false));
		this.closeKeyStore.setSelected(PreferencesManager.getBoolean(PREFERENCE_KEYSTORE_CLOSE_KEYSTORE, false));
		this.sizeSpinner.setValue(Integer.valueOf(PreferencesManager.get(PREFERENCE_KEYSTORE_CLOSE_KEYSTORE_TIMEOUT, Integer.toString(MIN_WAIT_TIME))));
		this.defaultStore.setSelectedItem(
			SimpleKeyStoreManager.getDefaultKeyStoreType()
		);

		final AOKeyStore ks = AOKeyStore.getKeyStore(
	    		PreferencesManager.get(
					PreferencesManager.PREFERENCE_KEYSTORE_PRIORITARY_STORE, null
				)
			);
		if (ks != null) {
			this.prioritaryKeyStoreComboBox.setSelectedItem(ks.toString());
		}
		this.configureCertPoliciesButton.setEnabled(PreferencesManager.getBoolean(PREFERENCE_KEYSTORE_ACCEPTED_POLICIES_ONLY_CERTS, false));

	}
}
