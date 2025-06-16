package vgp.tutor.lsystem;

import java.awt.*;
import java.applet.*;

import jv.object.PsConfig;
import jv.object.PsViewerIf;
import jv.project.PvCameraIf;
import jv.project.PvDisplayIf;
import jv.viewer.PvViewer;

/**
 * Generate a tree with an L-system and display using turtle graphics.
 * Additionally, this subclass implements the interface PsUpdateIf
 * to catch events from a parameter slider.
 *
 * @author		Konrad Polthier
 * @version		30.10.01, 2.00 revised (kp) L-System converted into a project.<br>
 *					17.02.00, 1.10 revised (kp) Slider added.<br>
 *					16.02.00, 1.00 created (kp)
 */
public class PaLSystem extends Applet implements Runnable {
    /** Frame to allow applet to run as application too. */
    protected		Frame			m_frame;
    /** 3D-viewer window for graphics output and which is embedded into the applet */
    protected	PvViewer			m_viewer;
    /** Message string drawn in applet while loading. Modify string with drawMessage(). */
    private		String			m_drawString	= "Initializing ...";

    /**
     * Create thread that configures and initializes the viewer, loads system and user projects.
     */
    public void init() {
        Thread thread = new Thread(this, "JavaView: inititialize applet");
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }
    /**
     * Configure and initialize the viewer, load system and user projects.
     * One of the user projects may be selected here.
     */
    public void run() {
        drawMessage("Loading viewer ...");
        m_viewer = new PvViewer(this, m_frame);	// at first initalize the viewer

        drawMessage("Loading project ...");

        // Create and load a project
        // PjHeight prj = new PjHeight(PsConfig.getCodeBase()+scalar);
        PjLSystem prj = new PjLSystem();
        m_viewer.addProject(prj);
        m_viewer.selectProject(prj);

        // Get 3d display from viewer and add it to applet
        setLayout(new BorderLayout());
        PvDisplayIf disp = m_viewer.getDisplay();
        disp.selectCamera(PvCameraIf.CAMERA_ORTHO_XY);		// project onto xy-plane
        add((Component)disp, BorderLayout.CENTER);
        add(m_viewer.getPanel(PsViewerIf.PROJECT), BorderLayout.EAST);
        validate();

        // Choose initial panel in control window (must press F1 inside the applet)
        m_viewer.showPanel(PsViewerIf.MATERIAL);

        // Explicitly start the applet
        startFromThread();
    }
    /**
     * Standalone application support. The main() method acts as the applet's
     * entry point when it is run as a standalone application. It is ignored
     * if the applet is run from within an HTML page.
     */
    public static void main(String args[]) {
        PaLSystem va	= new PaLSystem();
        // Create toplevel window of application containing the applet
        Frame frame	= new jv.object.PsMainFrame(va, args);
        frame.pack();
        va.m_frame = frame;
        va.init();
        frame.setBounds(new Rectangle(220, 5, 800, 550));
        frame.setVisible(true);
    }

    /** Set message string to be drawn in apllet while loading. */
    private void drawMessage(String message) {
        m_drawString = message;
        repaint();
    }
    /** Print info while initializing applet and viewer. */
    public void paint(Graphics g) {
        g.setColor(Color.blue);
        g.drawString(PsConfig.getProgramAndVersion(), 20, 40);
        g.drawString(m_drawString, 20, 60);
    }
    /**
     * Does clean-up when applet is destroyed by the browser.
     * Here we just close and dispose all our control windows.
     */
    public void destroy()	{ if (m_viewer != null) m_viewer.destroy(); }

    /** Stop viewer, e.g. stop animation if requested */
    public void stop()		{ if (m_viewer != null) m_viewer.stop(); }
    /**
     * Start viewer, e.g. start animation if requested.
     * Necessary, if initialization is done in a separate thread. In this case the original
     * applet.start() has no effect.
     */
    public void startFromThread()	{ if (m_viewer!=null) m_viewer.start(); }
}