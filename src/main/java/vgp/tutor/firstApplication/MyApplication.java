package vgp.tutor.firstApplication;

import java.awt.*;

import jv.geom.PgElementSet;
import jv.object.PsMainFrame;
import jv.project.PvDisplayIf;
import jv.viewer.PvViewer;

/**
 * Demo application shows how to include JavaView display in own code.
 *
 * @see			jv.viewer.PvViewer
 * @author		Konrad Polthier
 * @version		10.05.03, 2.00 revised (kp) Renamed to MyApplication from main.<br>
 *					25.12.99, 1.10 revised (kp) Geometry is added directly to display rather than to default project.<br>
 *					04.08.99, 1.00 created (kp)
 */
public class MyApplication {
    /**
     * Standalone application support. The main() method acts as the applet's
     * entry point when it is run as a standalone application. It is ignored
     * if the applet is run from within an HTML page.
     */
    public static void main(String args[]) {
        // Create toplevel window of application containing the applet
        PsMainFrame frame	= new PsMainFrame("First Application with JavaView", args);

        // Create viewer for viewing 3d geometries, and register frame.
        PvViewer viewer = new PvViewer(null, frame);

        // Create a simple geometry
        PgElementSet geom = new PgElementSet(3);
        geom.setName("Torus");
        // Compute coordinates and mesh of a geometry
        geom.computeTorus(10, 10, 2., 1.);

        // Get default display from viewer
        PvDisplayIf disp = viewer.getDisplay();
        // Register geometry in display, and make it active.
        // For more advanced applications it is advisable to create a separate project
        // and register geometries in the project via project.addGeometry(geom) calls.
        disp.addGeometry(geom);
        disp.selectGeometry(geom);

        // Add display to frame
        frame.add((Component)disp, BorderLayout.CENTER);
        frame.pack();
        // Position of left upper corner and size of frame when run as application.
        frame.setBounds(new Rectangle(420, 5, 640, 550));
        frame.setVisible(true);
    }
}