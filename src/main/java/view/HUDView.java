/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author asuna
 */

import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import model.Company;

public class HUDView extends HBox {

    private Label moneyLabel = new Label();
    private Label timeLabel = new Label();

    public HUDView() {

        setSpacing(20);

        Button pause = new Button("||");
        Button play = new Button("▶");
        Button fast = new Button(">>");

        getChildren().addAll(moneyLabel, timeLabel, pause, play, fast);
    }

    public void render(Company company, double time) {
//        if (company != null) {
//            moneyLabel.setText("Money: " + company.getEconomy().getMoney());
//        }
//        timeLabel.setText("Time: " + time);
    }
}
