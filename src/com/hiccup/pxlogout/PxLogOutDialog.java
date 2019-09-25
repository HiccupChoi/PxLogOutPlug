package com.hiccup.pxlogout;

import com.hiccup.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author hiccup
 */
public class PxLogOutDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel Name;
    private JTextField nameField;
    private JPanel Passward;
    private JPasswordField passwordField;
    private JPanel Service;
    private JComboBox ServiceComboBox;
    private JPanel Version;
    private JLabel errorHint;
    private JRadioButton oldVersionRadio;
    private JRadioButton newVersionRadio;
    private boolean newVersion;

    public PxLogOutDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        oldVersionRadio.addItemListener(e -> newVersion = false);

        newVersionRadio.addItemListener(e -> newVersion = true);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        String serviceAddress = (String) ServiceComboBox.getSelectedItem();
        String name = nameField.getText();
        if (name.isEmpty()){
            errorHint.setText("用户名不能为空");
            return;
        }
        String password = String.valueOf(passwordField.getPassword());
        if (password.isEmpty()){
            errorHint.setText("密码不能为空");
            return;
        }


        try {
            Result result;
            if (newVersion){
                result = LogOutSocket.doLogOut(serviceAddress, name, password, "2.41.00");
            } else {
                result = OldLogoutSocket.doLogOut(serviceAddress, name, password, "2.30.02");
            }
            boolean success = result.success;
            if (success){
                errorHint.setForeground(new Color(0, 178, 0));
                errorHint.setText("注销成功");
            } else {
                errorHint.setForeground(new Color(187, 0, 10));
                String errMsg = result.message;
                errorHint.setText(errMsg);
            }
        } catch (Exception e) {
            errorHint.setText(e.getMessage());
        }


    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        PxLogOutDialog dialog = new PxLogOutDialog();
        dialog.setTitle("注销登录");
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
