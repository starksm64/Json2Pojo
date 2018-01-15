package net.hexar.json2pojo;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * A custom dialog which allows the user to input a JSON text.
 */
public class JsonEntryDialog extends JDialog {

    //region PUBLIC INTERFACES -----------------------------------------------------------------------------------------

    /**
     * A listener to be invoked when the user has clicked the OK button.
     */
    interface OnOkListener {
        /**
         * A callback to be invoked when the user has clicked the OK button.
         *
         * @param className the class name entered into the dialog.
         * @param jsonText the JSON text entered into the dialog.
         * @param generateBuilders true if the generated classes should omit setters and generate builders.
         * @param useMPrefix true if the generated fields should have an 'm' prefix.
         */
        void onOk(String className, String jsonText, boolean generateBuilders, boolean useMPrefix, boolean useDoubleValueGetters);
    }

    //endregion

    //region CONSTANTS -------------------------------------------------------------------------------------------------

    private static final String CLASS_NAME_REGEX = "[A-Za-z][A-Za-z0-9]*";

    //endregion

    //region MEMBER FIELDS ---------------------------------------------------------------------------------------------

    // Data / State
    private OnOkListener mListener;

    // UI
    private JButton mButtonCancel;
    private JButton mButtonOK;
    private JTextField mClassName;
    private JPanel mContentPane;
    private RSyntaxTextArea mJsonText;
    private JCheckBox mUseMPrefix;
    private JCheckBox mGenerateBuilders;
    private JCheckBox mDoubleValueGetter;

    //endregion

    //region CONSTRUCTOR -----------------------------------------------------------------------------------------------

    JsonEntryDialog(OnOkListener listener) {
        // Set the listener
        mListener = listener;

        // Set up the main content
        setContentPane(mContentPane);
        setModal(true);
        getRootPane().setDefaultButton(mButtonOK);

        // Set the minimum dialog size
        setMinimumSize(new Dimension(420, 200));

        // Add button listeners
        mButtonOK.addActionListener(e -> onOK());
        mButtonCancel.addActionListener(e -> onCancel());

        // Call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // Call onCancel() on ESCAPE
        mContentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Enable/disable OK button
        mButtonOK.setEnabled(false);
        mClassName.getDocument().addDocumentListener(new TextChangedListener());
        mJsonText.getDocument().addDocumentListener(new TextChangedListener());

        // Set up syntax highlighting
        mJsonText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/themes/dark.xml"));
            theme.apply(mJsonText);
        } catch (IOException ignored) {
        }
        mJsonText.setCodeFoldingEnabled(false);
    }

    //endregion

    //region PUBLIC METHODS --------------------------------------------------------------------------------------------
    //endregion

    //region PRIVATE METHODS -------------------------------------------------------------------------------------------

    private void onCancel() {
        dispose();
    }

    private void onOK() {
        mListener.onOk(
                mClassName.getText(),
                mJsonText.getText(),
                mGenerateBuilders.isSelected(),
                mUseMPrefix.isSelected(),
                mDoubleValueGetter.isSelected()
            );
        dispose();
    }

    //region INNER CLASSES ---------------------------------------------------------------------------------------------

    /**
     * Gets called when the JSON text or root class text has changed.
     */
    private class TextChangedListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validate();
        }

        /**
         * Validates the class name and JSON text and enables the OK button if validation passes.
         */
        private void validate() {
            String className = mClassName.getText();
            String jsonText = mJsonText.getText();

            if (className.matches(CLASS_NAME_REGEX) && !jsonText.isEmpty()) {
                mButtonOK.setEnabled(true);
            } else {
                mButtonOK.setEnabled(false);
            }
        }
    }

    //endregion
}
