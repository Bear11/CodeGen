package com.zcc.codergen.ui;

import com.zcc.codergen.action.CodeGenAction;
import com.zcc.codergen.action.CodeMakerAction;

import javax.swing.*;
import java.awt.event.*;

public class CodeGenForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField classPath;
    private JTextField prefixText;
    private JTextField suffixText;

    public CodeGenForm() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        CodeGenAction.targetPath = classPath.getText();
        CodeGenAction.prefix = prefixText.getText();
        CodeGenAction.suffix = suffixText.getText();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        CodeGenAction.cancelStatus = true;
        dispose();
    }

    public JPanel getMainPane() {
        return contentPane;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
