package main;

import MSR605.MSR605;
import MSR605.MSR605Event;
import MSR605.MSR605EventListener;
import Team.Member;
import Team.Team;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import view.MainDialogController;
import view.TerminalController;

import java.io.IOException;


/**
 * Created by Admin on 9/3/2014.
 */
public class MainApp extends Application implements MSR605EventListener {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private MSR605 msr;
    private Team team930;
    private SCREENSTATE screen;

    private enum SCREENSTATE {
        MAIN, TERMINAL
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        msr = new MSR605();
        msr.reset();
        msr.addEventListener(this);
        team930 = new Team("team930");
        initStage();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (screen == SCREENSTATE.TERMINAL) {
                    try {
                        reloadStage();
                        initStage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    msr.reset();
                    msr.All_LED_Off();
                    msr.exitComm();
                }

            }
        });

    }

    public void reloadStage() {
        msr.reset();
        msr.All_LED_On();
        primaryStage.close();
        primaryStage = new Stage();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (screen == SCREENSTATE.TERMINAL) {
                    try {
                        reloadStage();
                        initStage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    msr.exitComm();
                }

            }
        });
    }

    public void initStage() throws  Exception {
        primaryStage.setTitle("930 Time Clock");
        initRootLayout();
        msr.read();
        screen = SCREENSTATE.MAIN;
        initMainDialog();
    }

    public void initMainDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/MainDialogLayout.fxml"));
            AnchorPane mainDialog = (AnchorPane) loader.load();

            rootLayout.setCenter(mainDialog);

            MainDialogController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showTerminal(Member member) {
        try {
            FXMLLoader loader= new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/view/TerminalLayout.fxml"));
            AnchorPane terminal = (AnchorPane) loader.load();

            rootLayout.setCenter(terminal);

            TerminalController controller = loader.getController();
            controller.setMember(member);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return  primaryStage;
    }

    public static void main(String [] args) {
        launch(args);
    }

    @Override
    public void MSR605EventFire(MSR605Event event) {
        switch (event.getType()) {
            case READ_OK: doReadOK(); break;
            case READ_FAIL: doReadFail(); break;
        }
    }

    public void doReadOK() {
        String[] tracks = msr.parseTrackData();
        if (tracks[0] != null) {
            Member read = team930.getMember(tracks[0]);
            screen = SCREENSTATE.TERMINAL;
            if (read != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showTerminal(read);
                    }
                });
            }

        }
    }

    public boolean doReadOk(String id) {
        if (id != null) {
            Member read = team930.getMember(id);
            screen = SCREENSTATE.TERMINAL;
            if (read != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showTerminal(read);
                    }
                });
                msr.reset();
                return true;
            }
        }
        return false;
    }

    public void doReadFail() {
        msr.reset();
        msr.read();
    }

    public Team getTeam930() {
        return team930;
    }

    public MSR605 getMsr() {
        return msr;
    }
}


