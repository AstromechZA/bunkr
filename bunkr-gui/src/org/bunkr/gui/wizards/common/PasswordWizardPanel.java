/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.gui.wizards.common;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.PasswordRequirements;
import org.bunkr.gui.Icons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created At: 2016-01-19
 */
public class PasswordWizardPanel extends VBox
{
    private static final String PW_NOTE_DEFAULT = "Please enter a password";
    private static final String PW_NOTE_CONFIRM = "Please confirm password";
    private static final String PW_NOTE_MATCH = "Confirmation matches password";
    private static final String PW_NOTE_NO_MATCH = "Confirmation does not match password";
    private static final String PW_NOTE_CLASS_OK = "pw-note-success";
    private static final String PW_NOTE_CLASS_NOT_OK = "pw-note-failure";

    private static final String DESCRIPTION_TEXT = "Pick a new password to protect this archive.";

    private final PasswordField passwordBox = new PasswordField();
    private final PasswordField passwordConfirmBox = new PasswordField();
    private final TextField passwordFilePathBox = new TextField();
    private final Label passwordNote = new Label("");

    public PasswordWizardPanel()
    {
        this.passwordBox.setPromptText("Enter a password");
        this.passwordBox.setMaxWidth(Double.MAX_VALUE);
        this.passwordConfirmBox.setPromptText("Enter the password again");
        this.passwordConfirmBox.setMaxWidth(Double.MAX_VALUE);
        this.passwordConfirmBox.setDisable(true);
        this.passwordFilePathBox.setEditable(false);
        this.passwordFilePathBox.setFocusTraversable(false);
        this.passwordFilePathBox.getStyleClass().add("no-focus-style");
        this.passwordNote.setId("pw-note-field");
        Label descriptionLabel = new Label(DESCRIPTION_TEXT);
        descriptionLabel.setWrapText(true);
        Button pickPasswordFileButton = Icons.buildIconButton("Select", Icons.ICON_ELLIPSIS);
        this.getChildren().addAll(descriptionLabel,
                                  new Label("Password:"), passwordBox, passwordConfirmBox, passwordNote,
                                  new Label("Password File:"),
                                  new HBox(10, passwordFilePathBox, pickPasswordFileButton));
        HBox.setHgrow(this.passwordFilePathBox, Priority.ALWAYS);
        this.setSpacing(10);
        this.setMaxWidth(Double.MAX_VALUE);

        this.passwordBox.textProperty().addListener((observable, oldValue, newValue) -> {
            this.passwordConfirmBox.setText("");
            this.passwordConfirmBox.setDisable(true);
            this.passwordNote.getStyleClass().clear();
            this.passwordFilePathBox.clear();

            if (this.passwordBox.getText().equals(""))
            {
                this.passwordNote.setText(PW_NOTE_DEFAULT);
            }
            else
            {
                try
                {
                    PasswordRequirements.checkPasses(this.passwordBox.getText().getBytes());
                    this.passwordConfirmBox.setDisable(false);
                    this.passwordNote.setText(PW_NOTE_CONFIRM);
                }
                catch (IllegalPasswordException e)
                {
                    this.passwordNote.setText(e.getMessage());
                }
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_NOT_OK);
            }
        });

        this.passwordConfirmBox.textProperty().addListener((observable, oldValue, newValue) -> {
            this.passwordNote.getStyleClass().clear();
            this.passwordFilePathBox.clear();
            if (this.passwordConfirmBox.getText().equals(this.passwordBox.getText()))
            {
                this.passwordNote.setText(PW_NOTE_MATCH);
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_OK);
            }
            else if (this.passwordConfirmBox.getText().equals(""))
            {
                this.passwordNote.setText(PW_NOTE_CONFIRM);
            }
            else
            {
                this.passwordNote.setText(PW_NOTE_NO_MATCH);
                this.passwordNote.getStyleClass().add(PW_NOTE_CLASS_NOT_OK);
            }
        });

        pickPasswordFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pick password file");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
            File selectedPath = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (selectedPath != null)
            {
                this.passwordBox.clear();
                this.passwordConfirmBox.clear();
                this.passwordConfirmBox.setDisable(true);
                this.passwordFilePathBox.setText(selectedPath.getAbsolutePath());
            }
        });
    }

    public String getPasswordValue() throws IllegalPasswordException
    {
        if (passwordBox.getText() != null && passwordBox.getText().length() > 0)
        {
            if (! passwordBox.getText().equals(passwordConfirmBox.getText()))
                throw new IllegalPasswordException("Password confirmation does not match");
            return passwordBox.getText();
        }
        if (passwordFilePathBox.getText() != null && passwordFilePathBox.getText().length() > 0)
        {
            try(BufferedReader br = Files.newBufferedReader(Paths.get(passwordFilePathBox.getText())))
            {
                return br.readLine();
            }
            catch (IOException e)
            {
                throw new IllegalPasswordException("Cannot read password from %s", passwordFilePathBox.getText());
            }
        }

        return "";
    }
}
