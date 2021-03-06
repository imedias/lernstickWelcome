/*
 * Copyright (C) 2017 FHNW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.fhnw.lernstickwelcome.controller.binder.exam;

import ch.fhnw.lernstickwelcome.controller.WelcomeApplication;
import ch.fhnw.lernstickwelcome.controller.WelcomeController;
import ch.fhnw.lernstickwelcome.controller.exception.ProcessingException;
import ch.fhnw.lernstickwelcome.fxmlcontroller.exam.PasswordChangeController;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Binder class to init binings between view components and backend (model)
 * properties
 *
 * @author sschw
 */
public class PasswordChangeBinder {

    private final PasswordChangeController password;
    private final WelcomeController controller;

    /**
     * Constructor of ExamInformationBinder class
     *
     * @param controller is needed to provide access to the backend properties
     * @param password FXML controller which prviedes the view properties
     */
    public PasswordChangeBinder(WelcomeController controller,
            PasswordChangeController password) {
        this.password = password;
        this.controller = controller;
    }

    /**
     * Method to initialize the handlers for this class.
     *
     */
    public void initHandlers() {

        password.getOkButton().setOnAction(evt -> {

            controller.getSystemConfigTask().passwordProperty().setValue(
                    password.getNewPasswordField().getText());
            controller.getSystemConfigTask().passwordRepeatProperty().setValue(
                    password.getRepeatPasswordField().getText());

            try {
                controller.getSystemConfigTask().changePassword();
                controller.getPropertiesTask().newTask().run();
                ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close();
            } catch (ProcessingException ex) {
                WelcomeApplication.showThrowable(ex);
            }
        });

        // If user clicks on ignore remove the already tried passwords.
        password.getCancelButton().setOnAction(evt -> {
            controller.getSystemConfigTask().passwordProperty().setValue(null);
            controller.getSystemConfigTask().passwordRepeatProperty().setValue(null);
            ((Stage) ((Node) evt.getSource()).getScene().getWindow()).close();
        });
    }
}
