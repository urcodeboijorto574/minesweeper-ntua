package com.example.minesweeper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.ResourceBundle;

public class CreateController implements Initializable {
    @FXML private TextField fileNameTextField;
    @FXML private Spinner<Integer> minesNumberSpinner;
    @FXML private Spinner<Integer> secondsNumberSpinner;
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        SpinnerValueFactory<Integer> mineValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(9, 45);
        mineValueFactory.setValue(11);
        minesNumberSpinner.setValueFactory(mineValueFactory);

        SpinnerValueFactory<Integer> secondValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(120, 360);
        secondValueFactory.setValue(240);
        secondsNumberSpinner.setValueFactory(secondValueFactory);
    }

    @FXML private RadioButton radioButtonEasy;

    @FXML private ToggleButton supermineToggleButton;
    @FXML
    private void setSupermine() {
        supermineToggleButton.setText(supermineToggleButton.getText().equals("No") ? "Yes" : "No");
    }

    @FXML private void createFile() {
        String fileName = ".\\medialab\\" + fileNameTextField.getText() + ".txt";
        int secondsTotalNumber = secondsNumberSpinner.getValue();
        int minesTotalNumber = minesNumberSpinner.getValue();
        int difficulty = (radioButtonEasy.isSelected() ? 1 : 2);
        int supermine = supermineToggleButton.getText().equals("No") ? 0 : 1;

        try {
            File scenarioFile = new File(fileName);
            if (!scenarioFile.createNewFile())
                throw new FileAlreadyExistsException(
                        "The file specified already exists!"
                );
            else
                System.out.println("The file specified just got created.");
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write(difficulty + "\n" + minesTotalNumber + "\n" + secondsTotalNumber + "\n" + supermine + "\n");
            fileWriter.close();
        } catch (FileAlreadyExistsException e) {
            System.err.println("A file with the given name already exists in this directory. " +
                    "Re-enter the create-file prompt to insert a unique file name and your configurations.");
            // TODO_task: don't continue and ask for a file name again
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(13);
        }

        MinesweeperApp.createStage.close();
    }
}
