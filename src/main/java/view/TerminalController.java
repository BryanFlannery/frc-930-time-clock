package view;

import Team.Entry;
import Team.Member;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Created by Admin on 9/3/2014.
 */
public class TerminalController {

    @FXML
    private TableView<Entry> entryTable;
    @FXML
    private TableColumn<Entry, String> date;
    @FXML
    private TableColumn<Entry, String> timeIn;
    @FXML
    private TableColumn<Entry, String> timeOut;
    @FXML
    private TableColumn<Entry, String> type;
    @FXML
    private Label name;

    private Member member;

    @FXML
    private void initialize() {
        date.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        timeIn.setCellValueFactory(cellData -> cellData.getValue().timeInProperty());
        timeOut.setCellValueFactory(cellData -> cellData.getValue().timeOutProperty());
        type.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
    }

    @FXML
    private void handleDeleteEntry() {
        Entry toBeDeleted = entryTable.getSelectionModel().getSelectedItem();
        if (toBeDeleted != null) {
            Action choice = Dialogs.create()
                    .title("Delete")
                    .masthead("Are you sure?")
                    .message("Are you sure you want to delete this entry?")
                    .showConfirm();
            System.out.println(choice.toString());
            if (choice.equals(Dialog.Actions.YES)) {
                int index = entryTable.getSelectionModel().getSelectedIndex();
                entryTable.getItems().remove(index);
                member.removeEntry(toBeDeleted.getId());
            }

        }else {
            Dialogs.create()
                    .title("No Selection")
                    .masthead("No Entry Selected")
                    .message("Please select an entry in the table.")
                    .showWarning();
        }
    }

    @FXML
    private void handleEdit() {

    }

    @FXML
    private void handleNew() {

    }

    @FXML
    private void handleClockIn() {
        if (!member.getClockedIn()) {
            LocalDateTime in = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, ZoneOffset.ofHours(-5));
            Action choice = Dialogs.create()
                    .title("Clock in?")
                    .masthead("Clock in for shop hours?")
                    .message("Clock in at " + in.format(DateTimeFormatter.ofPattern("hh:mm a")))
                    .showConfirm();
            if (choice.equals(Dialog.Actions.YES)) {
                member.clockIn(in.toEpochSecond(ZoneOffset.ofHours(0)), Entry.TIME_TYPE.SHOP_HOURS);
                member.setClockedIn(true);
                ObservableList<Entry> entries = entryTable.getItems();
                ResultSet rs = member.getClockIn();
                try {
                    if (rs.next()) {
                        entries.add(new Entry(
                                        rs.getInt("id"),
                                        in.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                        in.format(DateTimeFormatter.ofPattern("hh:mm a")),
                                        "N/A",
                                        rs.getString("type"))
                        );
                    }
                } catch(SQLException e) {
                    e.printStackTrace();
                }

            }

        } else {
            Action choice = Dialogs.create()
                    .title("Whops!")
                    .masthead("You are still clocked in.")
                    .message("You need to clock out before you can do that.")
                    .showError();

            if (choice.equals(Dialog.Actions.YES)) {

            } else if(choice.equals(Dialog.Actions.NO)) {

            }
        }

    }

    @FXML
    private void handleClockOut() {
        if (member.getClockedIn()) {
            ResultSet rs = member.getClockIn();
            String timeIn = "";
            try {
                if (rs.next()) {
                    LocalDateTime in = LocalDateTime.ofEpochSecond(rs.getLong("inEpoch"), 0, ZoneOffset.ofHours(0));
                    timeIn =  in.format(DateTimeFormatter.ofPattern("hh:mm a"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            LocalDateTime out = LocalDateTime.ofEpochSecond(System.currentTimeMillis() / 1000, 0, ZoneOffset.ofHours(-5));
            String timeOut =  out.format(DateTimeFormatter.ofPattern("hh:mm a"));
            Action choice = Dialogs.create()
                    .title("Clock out?")
                    .masthead("Do you want to clock out.")
                    .message("You last clocked in at " + timeIn + ". Clock out now at " + timeOut + "?")
                    .showConfirm();
            if (choice.equals(Dialog.Actions.YES)) {
                member.clockOut(out.toEpochSecond(ZoneOffset.ofHours(0)));
                member.setClockedIn(false);
                ObservableList<Entry> entries = entryTable.getItems();

                int index = entries.size()-1;
                Entry edit = entries.get(index);
                edit.setTimeOut(timeOut);
                entries.set(index, edit);
            }

        } else {
            Dialogs.create()
                    .title("Whops!")
                    .masthead("You can't do that")
                    .message("Looks like you forgot to clock in.")
                    .showError();
        }
    }

    public void setMember(Member member) {
        this.member = member;
        entryTable.setItems(member.getEntries());
        ResultSet rs = member.getClockIn();
        try {
            float outEpoch = 1;
            if (rs.next()) {
                outEpoch = rs.getFloat("outEpoch");
            }
            if (outEpoch == 0) {
                member.setClockedIn(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        name.setText(member.getFirstName()+ " " + member.getLastName());
    }

    /*
    private void installEventHandler(final Node keyNode) {
        // handler for enter key press / release events, other keys are
        // handled by the parent (keyboard) node handler
        final EventHandler<KeyEvent> keyEventHandler =
                new EventHandler<KeyEvent>() {
                    public void handle(final KeyEvent keyEvent) {
                        if (keyEvent.getCode() == KeyCode.ENTER) {
                            setPressed(keyEvent.getEventType()
                                    == KeyEvent.KEY_PRESSED);

                            keyEvent.consume();
                        }
                    }
                };

        keyNode.setOnKeyPressed(keyEventHandler);
        keyNode.setOnKeyReleased(keyEventHandler);
    }
    */

}

