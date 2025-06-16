package vgp.tutor.lsystem;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import jv.object.PsDebug;
import jv.object.PsPanel;
import jv.object.PsUpdateIf;
import jv.project.PjProject_IP;

/**
 * Info panel for L-system.
 *
 * @author		Konrad Polthier
 * @version		06.02.03, 1.10 revised (kp) Additional checkbox to switch off state information.<br>
 *					30.10.01, 1.00 created (kp)
 */
public class PjLSystem_IP extends PjProject_IP implements ActionListener, ItemListener {

    protected	PjLSystem				m_pjLSystem;
    protected	Button					m_bReset;
    protected	PsPanel					m_pBounds;
    protected	PsPanel					m_pSystem;
    protected	PsPanel					m_pRules;

    /** Number of letters in the alphabet. */
    protected	int						m_numLetters;
    /** Text field with alphabet of the L-system. */
    protected	TextField				m_tAlphabet;
    /** Text field with initial configuration of the L-system. */
    protected	TextField				m_tAxiom;
    /** Production rule of each character of the alphabet. */
    protected	TextField []			m_tRule;
    /** Current tree. */
    protected	TextArea					m_tTree;
    /** Enable auto fit. */
    protected	Checkbox					m_cAutoFit;
    /** Enable printing of current state. */
    protected	Checkbox					m_cCurrentState;

    public PjLSystem_IP() {
        super();

        if (getClass() == PjLSystem_IP.class) {
            init();
        }
    }

    public void init() {
        super.init();
        PsPanel title = new PsPanel();
        title.setLayout(new GridLayout(1, 2));
        title.addTitle("L-System");
        {
            m_cAutoFit = new Checkbox("Auto Fit");
            m_cAutoFit.addItemListener(this);
            title.add(m_cAutoFit);
        }
        add(title);

        m_pSystem = new PsPanel();
        add(m_pSystem);
        {
            m_tAlphabet = new TextField();
            m_tAlphabet.addActionListener(this);
            m_tAlphabet.setEditable(false);
            m_pSystem.addLabelComponent("Alphabet", m_tAlphabet);

            m_tAxiom = new TextField();
            m_tAxiom.addActionListener(this);
            m_pSystem.addLabelComponent("Axiom", m_tAxiom);

            m_pRules = new PsPanel();
            m_pSystem.add(m_pRules);
        }

        m_pBounds = new PsPanel();
        add(m_pBounds);

        PsPanel state = new PsPanel();
        state.setLayout(new GridLayout(1, 2));
        state.addSubTitle("Current State");
        {
            m_cCurrentState = new Checkbox("Show State");
            m_cCurrentState.addItemListener(this);
            state.add(m_cCurrentState);
        }
        add(state);

        m_tTree = new TextArea("", 5, 20, TextArea.SCROLLBARS_VERTICAL_ONLY);
        m_tTree.setEditable(false);
        add(m_tTree);

        // buttons at bottom
        Panel m_pBottomButtons = new Panel();
        m_pBottomButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        m_bReset = new Button("Reset");
        m_bReset.addActionListener(this);
        m_pBottomButtons.add(m_bReset);

        add(m_pBottomButtons);
    }
    /**
     * Set parent of panel which supplies the data inspected by the panel.
     */
    public void setParent(PsUpdateIf parent) {
        super.setParent(parent);
        m_pjLSystem = (PjLSystem) parent;
        m_pBounds.removeAll();
        m_pBounds.add(m_pjLSystem.m_numIterations.getInfoPanel());
        m_pBounds.add(m_pjLSystem.m_delta.getInfoPanel());

        char [] alphabet = m_pjLSystem.m_lsystem.m_alphabet;
        m_numLetters = alphabet.length;
        {
            m_pRules.removeAll();
            m_pRules.addSubTitle("Replacement Rules");
            m_tRule = new TextField[m_numLetters];
            for (int i=0; i<m_numLetters; i++) {
                m_tRule[i] = new TextField();
                m_tRule[i].addActionListener(this);
                m_pRules.addLabelComponent(String.valueOf(alphabet[i]), m_tRule[i]);
            }
            m_pRules.validate();
        }
        PsPanel.setText(m_tTree, m_pjLSystem.m_lsystem.getTree());
    }
    /**
     * Update the panel whenever the parent has changed somewhere else.
     * Method is invoked from the parent or its superclasses.
     */
    public boolean update(Object event) {
        if (PsDebug.NOTIFY) PsDebug.notify("isShowing = "+isShowing());
        if (m_pjLSystem == event) {
            PsPanel.setText(m_tAlphabet, new String(m_pjLSystem.m_lsystem.m_alphabet));
            PsPanel.setText(m_tAxiom, m_pjLSystem.m_lsystem.m_axiom);
            for (int i=0; i<m_numLetters; i++)
                PsPanel.setText(m_tRule[i], m_pjLSystem.m_lsystem.m_rule[i]);
            m_cCurrentState.setState(m_pjLSystem.m_bCurrentState);
            if (m_cCurrentState.getState())
                PsPanel.setText(m_tTree, m_pjLSystem.m_lsystem.getTree());
            else
                PsPanel.setText(m_tTree, "");
            m_cAutoFit.setState(m_pjLSystem.m_bAutoFit);
            return true;
        }
        return super.update(event);
    }
    /**
     * Handle action events invoked from buttons, menu items, text fields.
     */
    public void actionPerformed(ActionEvent event) {
        if (m_pjLSystem==null)
            return;
        Object source = event.getSource();
        if (source == m_tAlphabet) {
            // m_pjLSystem.m_lsystem.alphabet = PuString.(m_tAxiom.getText());
        } else if (source == m_tAxiom) {
            m_pjLSystem.m_lsystem.m_axiom = m_tAxiom.getText();
        } else if (source == m_bReset) {
            m_pjLSystem.m_lsystem.init();
            m_pjLSystem.init();
            m_pjLSystem.start();
        } else {
            for (int i=0; i<m_numLetters; i++) {
                if (source == m_tRule[i]) {
                    m_pjLSystem.m_lsystem.m_rule[i] = m_tRule[i].getText();
                    break;
                }
            }
        }
        m_pjLSystem.recompute();
        m_pjLSystem.update(m_pjLSystem);
    }
    public void itemStateChanged(ItemEvent event) {
        if (m_project==null)
            return;
        Object source = event.getSource();
        if (source == m_cAutoFit) {
            m_pjLSystem.m_bAutoFit = m_cAutoFit.getState();
            if (m_cAutoFit.getState())
                m_pjLSystem.fitDisplays();
            m_pjLSystem.m_bAutoFit = m_cAutoFit.getState();
            return;
        } else if (source == m_cCurrentState) {
            if (m_cCurrentState.getState())
                PsPanel.setText(m_tTree, m_pjLSystem.m_lsystem.getTree());
            else
                PsPanel.setText(m_tTree, "");
            m_pjLSystem.m_bCurrentState = m_cCurrentState.getState();
        }
    }
}