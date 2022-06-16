package tech.fastj.gj.scenes.editor;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.awt.Color;

public class DecimalVerifier extends InputVerifier {

    @Override
    public boolean verify(JComponent input) {
        if (input instanceof JTextField inputField) {
            try {
                Double.parseDouble(inputField.getText());
            } catch (NumberFormatException exception) {
                return false;
            }

            return true;
        }

        throw new IllegalStateException("Tried to verify a non-textfield component " + input);
    }

    @Override
    public boolean shouldYieldFocus(JComponent source, JComponent target) {
        if (verify(source)) {
            source.setForeground(Color.black);
        } else {
            source.setForeground(Color.red);
        }

        return true;
    }
}
