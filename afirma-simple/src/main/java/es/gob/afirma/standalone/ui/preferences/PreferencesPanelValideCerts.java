package es.gob.afirma.standalone.ui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.gob.afirma.standalone.SimpleAfirmaMessages;

/** Panel de preferencias para la validaci&oacute;n de certificados. */
public class PreferencesPanelValideCerts extends JPanel {

	private static final long serialVersionUID = 3776016646875294106L;

	private final JTextField vaURI = new JTextField();
	String getVaURI() {
		return this.vaURI.getText();
	}

	private final JTextField crlURI = new JTextField();
	String getCrlURI() {
		return this.crlURI.getText();
	}

	private final JCheckBox pssdef = new JCheckBox(
		SimpleAfirmaMessages.getString("PreferencesPanelValideCerts.3"), //$NON-NLS-1$
		false
	);

	private final JTextField pssdefURI = new JTextField();
	String getPssdefURI() {
		return this.pssdefURI.getText();
	}

	PreferencesPanelValideCerts(final KeyListener keyListener,
								  final ModificationListener modificationListener,
								  final boolean unprotected) {

		createUI(keyListener, modificationListener, unprotected);
	}

	void createUI(final KeyListener keyListener,
				  final ModificationListener modificationListener,
				  final boolean unprotected) {

		getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanelValideCerts.0") //$NON-NLS-1$
		);

		setLayout(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;

		final JPanel mdefPanel = new JPanel(new GridBagLayout());
		mdefPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanelValideCerts.1")) //$NON-NLS-1$
			)
		);

		final GridBagConstraints mdpc = new GridBagConstraints();
		mdpc.fill = GridBagConstraints.HORIZONTAL;
		mdpc.weightx = 1.0;
		mdpc.gridx = 0;
		mdpc.gridy = 0;
		mdpc.insets = new Insets(5, 0, 0, 7);

		final JLabel vaUriLabel = new JLabel(
			SimpleAfirmaMessages.getString("PreferencesPanelValideCerts.4") //$NON-NLS-1$
		);
		vaUriLabel.setLabelFor(this.vaURI);

		final JLabel crlUriLabel = new JLabel(
			SimpleAfirmaMessages.getString("PreferencesPanelValideCerts.5") //$NON-NLS-1$
		);
		crlUriLabel.setLabelFor(this.crlURI);

		final JLabel pssdefUriLabel = new JLabel(
			SimpleAfirmaMessages.getString("PreferencesPanelValideCerts.6") //$NON-NLS-1$
		);
		pssdefUriLabel.setLabelFor(this.pssdefURI);


		mdefPanel.add(vaUriLabel, mdpc);
		mdpc.gridy++;
		mdefPanel.add(this.vaURI, mdpc);
		mdpc.gridy++;
		mdefPanel.add(crlUriLabel, mdpc);
		mdpc.gridy++;
		mdefPanel.add(this.crlURI, mdpc);
		mdpc.gridy++;
		mdefPanel.add(this.pssdef, mdpc);
		mdpc.gridy++;
		mdefPanel.add(pssdefUriLabel, mdpc);
		mdpc.gridy++;
		mdefPanel.add(this.pssdefURI, mdpc);

		final JPanel noMdefPanel = new JPanel(new GridBagLayout());
		noMdefPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanelValideCerts.2")) //$NON-NLS-1$
			)
		);

		final GridBagConstraints nmdpc = new GridBagConstraints();
		nmdpc.fill = GridBagConstraints.HORIZONTAL;
		nmdpc.weightx = 1.0;
		nmdpc.gridx = 0;
		nmdpc.gridy = 0;
		nmdpc.insets = new Insets(5, 0, 0, 7);

		add(mdefPanel, gbc);
		gbc.gridy++;
		add(noMdefPanel, gbc);
		gbc.gridy++;
		gbc.weighty = 1.0;
		add(new JPanel(), gbc);


	}
}
