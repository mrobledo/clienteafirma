package es.gob.afirma.standalone.trayicon;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

/**
 * Clase para la gesti&oacute;n de los eventos de clic y doble clic.
 */
public class ClickListener extends MouseAdapter implements ActionListener {

    private static int clickInterval;
    static {
    	final Integer interval = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval"); //$NON-NLS-1$
   		clickInterval = interval == null ? 500 : interval.intValue();
    }

    MouseEvent lastEvent;
    Timer timer;

    public ClickListener() {
    	this(clickInterval);
    }

    public ClickListener(final int delay) {
    	this.timer = new Timer(delay, this);
    }

    @Override
	public void mouseClicked (final MouseEvent e) {

        if (e.getClickCount() > 2) {
			return;
		}

        this.lastEvent = e;

        if (this.timer.isRunning()) {
            this.timer.stop();
            doubleClick( this.lastEvent );
        }
        else {
            this.timer.restart();
        }
    }

    @Override
	public void actionPerformed(final ActionEvent e) {
        this.timer.stop();
        singleClick( this.lastEvent );
    }

    @SuppressWarnings("unused")
	public void singleClick(final MouseEvent e) { /* Por defecto no hacemos nada */ }
    @SuppressWarnings("unused")
	public void doubleClick(final MouseEvent e) { /* Por defecto no hacemos nada */ }
}