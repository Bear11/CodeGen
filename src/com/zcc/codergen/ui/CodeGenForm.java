package com.zcc.codergen.ui;

import com.intellij.openapi.util.text.StringUtil;
import com.zcc.codergen.CodeGenSettings;
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
        CodeMakerAction.calssPath = classPath.getText();
        CodeMakerAction.prefix = prefixText.getText();
        CodeMakerAction.suffix = suffixText.getText();
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

//    public static void main(String[] args) {
//        JFrame jFrame= new JFrame("CodeGenFrame");
//        JPanel rootPane=new CodeGenForm().contentPane;
//        //CodeGenForm dialog = new CodeGenForm();
//        jFrame.setContentPane(rootPane);
//        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        jFrame.pack();
//        jFrame.setSize(600, 200);
//        jFrame.setLocationRelativeTo(rootPane);//居中
//        jFrame.setVisible(true);
//
////        CodeGenForm dialog = new CodeGenForm();
////        dialog.pack();
////        dialog.setVisible(true);
////        dialog.setSize(800, 250);
////        dialog.setLocationRelativeTo(rootPane);//居中
//    }
    public JPanel getMainPane() {
        return contentPane;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
