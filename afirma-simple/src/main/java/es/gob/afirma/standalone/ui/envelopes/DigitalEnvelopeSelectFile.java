package es.gob.afirma.standalone.ui.envelopes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

/**
 * @author Mariano Mart&iacute;nez
 * Panel para seleccionar un fichero para ensobrar y el tipo de sobre a realizar.
 */
public class DigitalEnvelopeSelectFile extends JPanel {

	private static final long serialVersionUID = -5430415718507253691L;
	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$
	private static final String[] SIGN_ALGORITHM = {
		"SHA512withRSA", //$NON-NLS-1$
		"SHA384withRSA", //$NON-NLS-1$
		"SHA256withRSA",//$NON-NLS-1$
		"SHA1withRSA", //$NON-NLS-1$
	};

	private final JComboBox<EnvelopesTypeResources> envelopeTypes = new JComboBox<>(
		EnvelopesTypeResources.getAllEnvelopesTypeResources()
	);
	EnvelopesTypeResources getSelectedType() {
		return (EnvelopesTypeResources) this.envelopeTypes.getSelectedItem();
	}

	private final JTextField selectedFile = new JTextField();
	void setSelectedFile(final String file) {
		this.selectedFile.setText(file);
	}
	String getSelectedFile() {
		return this.selectedFile.getText();
	}

	private final JButton nextButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.3")); //$NON-NLS-1$
	private final JButton cancelButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.4")); //$NON-NLS-1$
	private final JButton examineButton = new JButton(SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.9")); //$NON-NLS-1$

	private boolean browseDialogOpenned = false;
	public boolean isBrowseDialogOpenned() {
		return this.browseDialogOpenned;
	}

	public void setBrowseDialogOpenned(final boolean browseDialogOpenned) {
		this.browseDialogOpenned = browseDialogOpenned;
	}

	JButton getNextButton() {
		return this.nextButton;
	}

	private final DigitalEnvelopePresentation dialog;
	DigitalEnvelopePresentation getDialog() {
		return this.dialog;
	}

	private final JPanel panelCentral = new JPanel();
	JPanel getPanelCentral() {
		return this.panelCentral;
	}

	private final JPanel panel = new JPanel();
	JPanel getPanel() {
		return this.panel;
	}

	/** Crea el panel para seleccionar el fichero a ensobrar, el tipo de sobre y el algoritmo a utilizar.
	 * @param dl Di&aacute;logo principal del asistente. */
	public DigitalEnvelopeSelectFile(final DigitalEnvelopePresentation dl) {
		this.dialog = dl;
		if (dl != null && dl.getEnvelopeData().getFilePath() != null) {
			setSelectedFile(dl.getEnvelopeData().getFilePath());
		}
		createUI();
	}

	private final JComboBox<String> singAlgorithm = new JComboBox<>(SIGN_ALGORITHM);
	String getSignAlgorithm() {
		return this.singAlgorithm.getSelectedItem().toString();
	}

	/** Crea una ventana con opciones de ensobrado. */
	void createUI() {

		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("DigitalEnvelopeFile.0") //$NON-NLS-1$
		);

		// Panel con el contenido
        final GridBagLayout gbLayout = new GridBagLayout();
        this.panelCentral.setBackground(Color.WHITE);
        this.panelCentral.setLayout(gbLayout);
        this.panelCentral.setBorder(BorderFactory.createEmptyBorder());

    	final JLabel infoLabel = new JLabel(
			SimpleAfirmaMessages.getString("DigitalEnvelopeFile.1") //$NON-NLS-1$
		);

		// Eleccion fichero a ensobrar
		final JLabel envelopeFilesLabel = new JLabel(
			SimpleAfirmaMessages.getString("DigitalEnvelopeFile.2") //$NON-NLS-1$
		);
		envelopeFilesLabel.setLabelFor(this.selectedFile);
		this.selectedFile.setEditable(false);
		this.selectedFile.setFocusable(false);

		// Boton de examinar
		this.examineButton.setMnemonic('X');
		this.examineButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("DigitalEnvelopeFile.3") //$NON-NLS-1$
		);
		this.examineButton.addActionListener(
			new ActionListener() {
				/** {@inheritDoc} */
				@Override
				public void actionPerformed(final ActionEvent ae) {
					setBrowseDialogOpenned(true);
					final File file;
					try {
						file = AOUIFactory.getLoadFiles(
							SimpleAfirmaMessages.getString("DigitalEnvelopeFile.2"), //$NON-NLS-1$
							null,
							null,
							new String[] { },
							SimpleAfirmaMessages.getString("DigitalEnvelopeFile.4"), //$NON-NLS-1$
							false,
							false,
							null,
							getDialog()
						)[0];
					}
					catch (final AOCancelledOperationException e) {
						LOGGER.warning(
							"Operacion cancelada por el usuario: " + e//$NON-NLS-1$
						);
						return;
					}
					setBrowseDialogOpenned(false);

					if (!file.canRead()) {
						LOGGER.warning(
							"No ha podido cargarse el fichero para envolver: " //$NON-NLS-1$
						);
						AOUIFactory.showErrorMessage(
							getDialog(),
							SimpleAfirmaMessages.getString("DigitalEnvelopeFile.6"), //$NON-NLS-1$
							SimpleAfirmaMessages.getString("DigitalEnvelopeFile.5"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE
						);
						return;
					}
					setSelectedFile(file.getAbsolutePath());
					getNextButton().setEnabled(true);
					getNextButton().setFocusable(true);
					getNextButton().requestFocusInWindow();
				}
			}
		);
		this.examineButton.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(final KeyEvent ke) {
				getDialog().keyTyped(ke);
			}

			@Override
			public void keyReleased(final KeyEvent ke) {

				if (isBrowseDialogOpenned()) {
					setBrowseDialogOpenned(false);
				}
				else {
					getDialog().keyReleased(ke);
				}
			}

			@Override
			public void keyPressed(final KeyEvent ke) {
				getDialog().keyPressed(ke);
			}
		});

		// Label con los tipos de ensobrado
		final JLabel typeFilesLabel = new JLabel(
			SimpleAfirmaMessages.getString("DigitalEnvelopeFile.7") //$NON-NLS-1$
		);
		typeFilesLabel.setLabelFor(this.envelopeTypes);

		this.envelopeTypes.addKeyListener(this.dialog);

		// Label con los algoritmos de cifrado
		final JLabel signAlgorithmLabel = new JLabel(
			SimpleAfirmaMessages.getString("DigitalEnvelopeFile.8") //$NON-NLS-1$
		);
		signAlgorithmLabel.setLabelFor(this.singAlgorithm);


		this.singAlgorithm.addKeyListener(this.dialog);

		// Botono de siguiente
		this.nextButton.setMnemonic('S');
		this.nextButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.6") //$NON-NLS-1$
		);
		this.nextButton.addActionListener(
			new ActionListener() {
			/** {@inheritDoc} */
			@Override
			public void actionPerformed(final ActionEvent ae) {
				saveConfiguration();
				getDialog().remove(getPanelCentral());
				getDialog().remove(getPanel());
				getDialog().remove(getDialog().getFilePanel());
				getDialog().getEnvelopeData().setFilePath(getSelectedFile());
				getDialog().getEnvelopeData().setEnvelopeType(getSelectedType());
				getDialog().getEnvelopeData().setSignatureAlgorithm(getSignAlgorithm());
				getDialog().setRecipientsPanel(
					new DigitalEnvelopeRecipients(
						getDialog()
					)
				);
				getDialog().add(getDialog().getRecipientsPanel());
			}
		}
		);
		this.nextButton.setFocusable(false);
		this.nextButton.addKeyListener(this.dialog);
		if (getSelectedFile().isEmpty()) {
			this.nextButton.setEnabled(false);
		}

		// Boton cancelar
		this.cancelButton.setMnemonic('C');
		this.cancelButton.getAccessibleContext().setAccessibleDescription(
 			SimpleAfirmaMessages.getString("DigitalEnvelopePresentation.7") //$NON-NLS-1$
		);
		this.cancelButton.addActionListener(
			new ActionListener() {
				/** {@inheritDoc} */
				@Override
				public void actionPerformed(final ActionEvent ae) {
					getDialog().setVisible(false);
					getDialog().dispose();
				}
			}
		);
		this.cancelButton.addKeyListener(this.dialog);

		this.panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// En Mac OS X el orden de los botones es distinto
		if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			this.panel.add(this.cancelButton);
			this.panel.add(this.nextButton);
		}
		else {
			this.panel.add(this.nextButton);
			this.panel.add(this.cancelButton);
		}

		final JPanel emptyPanel = new JPanel();
		emptyPanel.setBackground(Color.WHITE);

		loadConfiguration();
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(20, 20, 20, 20);
		this.panelCentral.add(infoLabel, c);
		c.insets = new Insets(20, 20, 0, 20);
		c.gridy++;
		c.gridy++;
		this.panelCentral.add(envelopeFilesLabel, c);
		c.insets = new Insets(5, 20, 0, 10);
		c.gridy++;
		c.gridy++;
		this.panelCentral.add(this.selectedFile, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		this.panelCentral.add(this.examineButton, c);
		c.insets = new Insets(20, 20, 0, 10);
		c.gridy++;
		c.weightx = 1.0;
		this.panelCentral.add(typeFilesLabel, c);
		c.insets = new Insets(5, 20, 0, 10);
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		this.panelCentral.add(this.envelopeTypes, c);
		c.gridy++;
		c.insets = new Insets(20, 20, 0, 20);
		this.panelCentral.add(signAlgorithmLabel, c);
		c.gridy++;
		c.insets = new Insets(5, 20, 0, 10);
		this.panelCentral.add(this.singAlgorithm, c);
		c.weighty = 1.0;
		c.gridy++;
		this.panelCentral.add(emptyPanel, c);
		this.dialog.getContentPane().add(this.panelCentral);
		this.dialog.getContentPane().add(this.panel, BorderLayout.PAGE_END);
        this.dialog.revalidate();
        this.dialog.repaint();
        this.examineButton.requestFocusInWindow();
	}

	void loadConfiguration() {
		this.envelopeTypes.setSelectedItem(
				EnvelopesTypeResources.getName(
				Integer.parseInt(
					PreferencesManager.get(
						PreferencesManager.PREFERENCE_ENVELOPE_TYPE,
						"0" //$NON-NLS-1$
					)
				)
			)
		);

		this.singAlgorithm.setSelectedItem(
			PreferencesManager.get(
				PreferencesManager.PREFERENCE_ENVELOPE_CIPHER_ALGORITHM,
				"SHA1withRSA" //$NON-NLS-1$
			)
		);
	}

	void saveConfiguration() {
		PreferencesManager.put(
			PreferencesManager.PREFERENCE_ENVELOPE_TYPE,
			Integer.toString(getSelectedType().getIndex())
		);
		PreferencesManager.put(PreferencesManager.PREFERENCE_ENVELOPE_CIPHER_ALGORITHM, getSignAlgorithm());
	}

}