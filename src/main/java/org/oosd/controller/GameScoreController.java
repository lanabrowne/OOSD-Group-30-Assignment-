package org.oosd.controller;


import org.oosd.Main;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;




public class GameScoreController {

    @FXML
    private TableView<PlayerScore> scoreTable;
    @FXML
    private TableColumn<PlayerScore, Integer> scoreColumn;
    @FXML
    private TableColumn<PlayerScore, String> nameColumn;

    @FXML
    private Button btnBack;  // connect the Back button in FXML
    
        Main main = new Main();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        scoreTable.setItems(FXCollections.observableArrayList(
                new PlayerScore(73462, "Tom"),
                new PlayerScore(49462, "Anna"),
                new PlayerScore(74093, "Jerry"),
                new PlayerScore(95287, "Mia"),
                new PlayerScore(24890, "John"),
                new PlayerScore(57493, "Larry"),
                new PlayerScore(54581, "Alice"),
                new PlayerScore(73376, "Mike"),
                new PlayerScore(14880, "Oliver"),
                new PlayerScore(13456, "Pole"),
                new PlayerScore(53356, "Kosuke"),
                new PlayerScore(83482, "Ikkei"),
                new PlayerScore(33003, "Lana"),
                new PlayerScore(62101, "Ria"),
                new PlayerScore(99345, "Taylor"),
                new PlayerScore(44498, "Yui"),
                new PlayerScore(76674, "Ben"),
                new PlayerScore(60904, "Eric"),
                new PlayerScore(77432, "Lisa"),
                new PlayerScore(90965, "Lucy")
        ));}
        // Link Back button to callback
   @FXML
public void backClicked(ActionEvent e) 
{
    // stop the game loop
    

    main.showMainScreen();
 
}



}
