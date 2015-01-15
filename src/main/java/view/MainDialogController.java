package view;

import MSR605.MSR605;
import main.MainApp;
import Team.Team;
import Team.Member;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import jfxtras.scene.layout.GridPane;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 * Created by Admin on 10/29/2014.
 */
public class MainDialogController {

    private MainApp app;

    final TextField fName = new TextField();
    final TextField lName = new TextField();
    final TextField studentId = new TextField();

    final PasswordField txPassword = new PasswordField();
    final Action actionLogin = new AbstractAction("Login") {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }

        // This method is called when the login button is clicked...
        @Override
        public void handle(ActionEvent ae) {
            Dialog dlg = (Dialog) ae.getSource();
            boolean ok = app.doReadOk(txPassword.getText());
            if (!ok) {
                Dialogs.create()
                        .title("Whops!")
                        .masthead("Who is that?")
                        .message("Looks like you typed your student id in wrong.")
                        .showError();
            }
            dlg.hide();
        }

    };


    // This method is called when the user types into the username / password fields
    private void validate() {
        actionLogin.disabledProperty().set(
                txPassword.getText().trim().isEmpty());
    }

    // Imagine that this method is called somewhere in your codebase
    private void showLoginDialog() {
        Dialog dlg = new Dialog(null, "Login");

        // listen to user input on dialog (to enable / disable the login button)
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> validate();
        txPassword.textProperty().addListener(changeListener);

        // layout a custom GridPane containing the input fields and labels
        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);

        content.add(new Label("StudentID"), 0, 1);
        content.add(txPassword, 1, 1);
        GridPane.setHgrow(txPassword, Priority.ALWAYS);

        // create the dialog with a custom graphic and the gridpane above as the
        // main content region
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        dlg.setGraphic(new ImageView(MainApp.class.getResource("/resources/login.png").toString()));
        dlg.setContent(content);
        dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
        validate();

        Platform.runLater(txPassword::requestFocus);

        dlg.show();
    }

    final Action actionCreate = new AbstractAction("Create") {
        {
            ButtonBar.setType(this, ButtonBar.ButtonType.OK_DONE);
        }

        // This method is called when the login button is clicked...
        @Override
        public void handle(ActionEvent ae) {
            Dialog dlg = (Dialog) ae.getSource();
            Team team930 = app.getTeam930();
            team930.addMember(new Member(studentId.getText(), fName.getText(), lName.getText()));
            MSR605 msr = app.getMsr();
            msr.reset();
            msr.write(studentId.getText(), "", "");
            Dialogs.create()
                    .title("Slide card!")
                    .masthead("Slide now")
                    .message("Once card is slid press ok.")
                    .showInformation();
        }

    };

    private void validateCreate() {
        actionCreate.disabledProperty().set(
                fName.getText().trim().isEmpty() || lName.getText().trim().isEmpty() || studentId.getText().trim().isEmpty());
    }

    private void showCreationDialog() {
        Dialog dlg = new Dialog(null, "Create");

        // listen to user input on dialog (to enable / disable the login button)
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> validateCreate();
        fName.textProperty().addListener(changeListener);
        lName.textProperty().addListener(changeListener);
        studentId.textProperty().addListener(changeListener);

        // layout a custom GridPane containing the input fields and labels
        final GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);

        content.add(new Label("StudentID"), 0, 1);
        content.add(studentId, 1, 1);
        GridPane.setHgrow(studentId, Priority.ALWAYS);
        content.add(new Label("First Name"), 0, 2);
        content.add(fName, 1, 2);
        GridPane.setHgrow(fName, Priority.ALWAYS);
        content.add(new Label("Last Name"), 0, 3);
        content.add(lName, 1, 3);
        GridPane.setHgrow(lName, Priority.ALWAYS);

        // create the dialog with a custom graphic and the gridpane above as the
        // main content region
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        //dlg.setGraphic(new ImageView(MainApp.class.getResource("/resources/login.png").toString()));
        dlg.setContent(content);
        dlg.getActions().addAll(actionCreate, Dialog.Actions.CANCEL);
        validateCreate();

        Platform.runLater(studentId::requestFocus);

        dlg.show();
    }


    @FXML
    private void handleManualEnter() {
        showLoginDialog();
    }

    @FXML
    private void handleCreateEnter() {
        showCreationDialog();
    }

    @FXML
    private void edit() {}

    public void setMainApp(MainApp app) {
        this.app = app;
    }

    @FXML
    private void initialize() {

    }

}
