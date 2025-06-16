package vgp.tutor.lsystem;

import java.util.Stack;

import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.number.PuInteger;
import jv.project.PjProject;
import jv.project.PvCameraIf;
import jv.project.PvDisplayIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

/**
 * Generate a tree with a context-free L-system.
 *
 * @author		Konrad Polthier
 * @version		06.02.03, 2.50 revised (kp) Optimize memory allocation in makePolygonSet.<br>
 *					30.10.01, 2.00 revised (kp) Converted into a project.<br>
 *					16.02.00, 1.00 created (kp)
 */
public class PjLSystem extends PjProject {
    /** Production of the L-system encoded with characters from alphabet {F,+,-,[,]}. */
    protected		String					m_descr;
    /** Angle parameter used in L-system. */
    protected		PuDouble					m_delta;
    /** Number of iterations of the L-system. */
    protected		PuInteger				m_numIterations;
    /** LSystem as collection of polygons. */
    protected		PgPolygonSet			m_polySet;
    /** LSystem base class. */
    protected		LSystem					m_lsystem;
    /** Auto fit within the display. */
    protected		boolean					m_bAutoFit;
    /** Show current state of L-System in text field. */
    protected		boolean					m_bCurrentState;

    public PjLSystem() {
        super("L-System");
        m_lsystem = new LSystem();

        m_polySet = new PgPolygonSet(2);
        m_polySet.setName("My L-System");

        // Create a double value with slider, and register applet as parent.
        m_delta				= new PuDouble("Angle", this);
        // Create a slider to adjust the number of iterations.
        m_numIterations	= new PuInteger("Num Iterations", this);

        if (getClass() == PjLSystem.class)
            init();
    }
    public void init() {
        m_delta.setDefBounds(-180., 180., 1., 10.);
        m_delta.setDefValue(90.);
        m_delta.init();

        m_numIterations.setDefBounds(0, 10, 1, 1);
        m_numIterations.setDefValue(1);
        m_numIterations.init();

        m_bAutoFit			= true;
        m_bCurrentState	= false;
    }
    public void start() {
        recompute();
        // Register geometry in display, and make it active.
        // For more advanced applications it is advisable to create a separate project
        // and register geometries in the project via project.addGeometry(geom) calls.
        addGeometry(m_polySet);
        selectGeometry(m_polySet);

        PvDisplayIf disp = getDisplay();
        disp.selectCamera(PvCameraIf.CAMERA_ORTHO_XY);		// project onto xy-plane
        if (m_bAutoFit)
            disp.fit();

        // Tree must be updated in info panel after the iteration.
        update(this);
    }
    /**
     * The variable m_delta sends update events to its parent to notify the parent
     * whenever its value has changed by user interaction.
     * Method must be implemented to fulfill interface PsUpdateIf.
     */
    public boolean update(Object event) {
        if (event == null) {
            return true;
        } else if (event == m_delta) {
            makePolygonSet(m_delta.getValue());
            m_polySet.update(m_polySet);
            return true;
        } else if (event == m_numIterations) {
            m_lsystem.iterate(m_numIterations.getValue());
            m_descr = m_lsystem.getTree();
            makePolygonSet(m_delta.getValue());
            m_polySet.update(m_polySet);
            if (m_bAutoFit)
                fitDisplays();
            return super.update(this);
        }
        return super.update(event);
    }
    /**
     * Recompute the polygon based on the current L-System.
     * The polygon is updated in the display.
     */
    public void recompute() {
        m_lsystem.iterate(m_numIterations.getValue());
        m_descr = m_lsystem.getTree();
        makePolygonSet(m_delta.getValue());
        m_polySet.update(m_polySet);
    }
    /**
     * Recompute the polygon set by translating the string given in m_descr.
     * This method resets the polygonSet and fills it again.
     *
     * @param		delta		angle in the turtle graphics used when rotating '+' or '-'
     * @version		06.05.03, 1.50 revised (kp) maxNum of polygons implemented to avoid allocations.<br>
     *					06.05.03, 1.10 revised (kp) Bug removed when +/- after closing bracket appeared.<br>
     *					16.02.00, 1.00 created (kp)
     */
    private void makePolygonSet(double delta) {
        int defMaxNumVertices	= 1000;
        int defMaxNumPolygons	= 10;
        int defMaxPolygonLength	= 50;
        m_polySet.setNumPolygons(defMaxNumPolygons);
        m_polySet.setNumPolygons(0);
        m_polySet.setNumVertices(defMaxNumVertices);
        m_polySet.setNumVertices(0);

        delta *= Math.PI/180.;

        Stack branchPos	= new Stack(); // Store the current position when branching
        Stack branchPoly	= new Stack(); // Store the current polygon when branching
        Stack branchCnt	= new Stack(); // Store the number of vertices per polygon when branching
        int vertInd, len	= m_descr.length();
        double x, y, a;
        double size			= 0.2;
        PdVector pos		= new PdVector(3);
        PiVector pg			= new PiVector(defMaxPolygonLength);

        x = 0.;
        y = 0.;
        a = Math.PI/2.;
        pos.set(x, y, a);
        vertInd = m_polySet.addVertex(pos);
        int polyInd = 0;
        pg.setEntry(polyInd++, vertInd);
        for (int i=0; i<len; i++) {
            switch (m_descr.charAt(i)) {
                case 'F':
                    a = pos.getEntry(2);
                    x = pos.getEntry(0)+size*Math.cos(a);
                    y = pos.getEntry(1)+size*Math.sin(a);
                    pos.set(x, y, a);
                    vertInd = m_polySet.addVertex(pos);
                    if (polyInd >= pg.getSize())
                        pg.setSize(2*polyInd);
                    pg.setEntry(polyInd++, vertInd);
                    break;
                case '[':
                    branchPos.push(pos);
                    pos	= PdVector.copyNew(pos);
                    branchPoly.push(pg);
                    branchCnt.push(new Integer(polyInd));
                    pg		= new PiVector(defMaxPolygonLength);
                    polyInd = 0;
                    pg.setEntry(polyInd++, vertInd);
                    break;
                case ']':
                    pg.setSize(polyInd);
                    m_polySet.addPolygon(pg);
                    pos	= (PdVector)branchPos.pop();
                    pg		= (PiVector)branchPoly.pop();
                    Integer cnt = (Integer)branchCnt.pop();
                    polyInd = cnt.intValue();
                    vertInd = pg.getEntry(polyInd-1);
                    break;
                case '+':
                    pos.setEntry(2, pos.getEntry(2)+delta);
                    break;
                case '-':
                    pos.setEntry(2, pos.getEntry(2)-delta);
                    break;
                default:
            }
        }
        pg.setSize(polyInd);
        m_polySet.addPolygon(pg);
    }
}