package org.exeinspector.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.exeinspector.controller.Adapter;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;

public class FileTabApp extends Application {

  private BorderPane root;
  private TabPane tabPane;
  private Label messageLabel;
  private Text placeholderText;


  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("ExeInspector");

    tabPane = new TabPane();
    Button helpButton = new Button("Справка");
    Button addButton = new Button("Добавить");

    String helpButtonDefaultStyle = "-fx-background-color: transparent; -fx-font-size: 16px; -fx-padding: 6px 25px; -fx-font-style: italic";
    helpButton.setStyle(helpButtonDefaultStyle);
    helpButton.setOnMouseEntered(e -> helpButton.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);" +
        " -fx-font-size: 16px;" +
        " -fx-padding: 6px 25px;" +
        " -fx-font-style: italic"));
    helpButton.setOnMouseExited(e -> helpButton.setStyle(helpButtonDefaultStyle));

    String addButtonDefaultStyle = "-fx-background-color: transparent; -fx-font-size: 16px; -fx-padding: 6px 25px; -fx-font-weight: bold";
    addButton.setStyle(addButtonDefaultStyle);
    addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1);" +
        "-fx-font-size: 16px;" +
        "-fx-padding: 6px 25px;" +
        "-fx-font-weight: bold "));
    addButton.setOnMouseExited(e -> addButton.setStyle(addButtonDefaultStyle));

    helpButton.setOnAction(e -> showHelp());
    addButton.setOnAction(e -> addFile());

    HBox buttonBox = new HBox(addButton, helpButton);
    buttonBox.setSpacing(10);
    buttonBox.setAlignment(Pos.TOP_LEFT);

    root = new BorderPane();
    root.setTop(buttonBox);
    root.setCenter(tabPane);

    // Добавление текстовой метки
    messageLabel = new Label("Перенесите файл или добавьте его через кнопку \"Добавить\"");
    messageLabel.setFont(new Font(20));
    messageLabel.setAlignment(Pos.CENTER);
    root.setCenter(messageLabel);


    // Добавление функциональности Drag and Drop
    root.setOnDragOver(event -> {
      if (event.getGestureSource() != root && event.getDragboard().hasFiles()) {
        event.acceptTransferModes(TransferMode.COPY);
      }
      event.consume();
    });

    root.setOnDragDropped(event -> {
      Dragboard db = event.getDragboard();
      boolean success = false;
      if (db.hasFiles()) {
        try {
          success = true;
          for (File file : db.getFiles()) {
            addNewTab(file);
          }
          messageLabel.setVisible(false);
          root.setCenter(tabPane);
        } catch (Exception e) {
          event.setDropCompleted(false);
          event.consume();
          showError(e.getMessage());
          return;
        }
      }
      event.setDropCompleted(success);
      event.consume();
    });

    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.show();

  }

  private void addNewTab(File file) throws IOException, NoSuchAlgorithmException {

    LinkedHashMap<String, String> information = Adapter.sendFileToHandler(file);
    Tab tab = new Tab(file.getName());
    TableView<FileDetails> tableView = new TableView<>();

    TableColumn<FileDetails, String> attributeCol = new TableColumn<>("Характеристика");
    attributeCol.setCellValueFactory(new PropertyValueFactory<>("attribute"));

    TableColumn<FileDetails, String> valueCol = new TableColumn<>("Значение характеристики");
    valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

    attributeCol.setMinWidth(300);
    attributeCol.setMaxWidth(300);

    tableView.getColumns().addAll(attributeCol, valueCol);

    // Добавляем данные в таблицу
    for (var pair : information.entrySet()) {
      tableView.getItems().add(new FileDetails(pair.getKey(), pair.getValue()));
    }

    // Обработчик для копирования значений из таблицы при нажатии Ctrl+C
    tableView.setOnKeyPressed(event -> {
      if (event.isShortcutDown() && event.getCode().toString().equals("C")) {
        TablePosition<FileDetails, ?> pos = tableView.getSelectionModel().getSelectedCells().get(0);
        int row = pos.getRow();
        TableColumn<FileDetails, ?> col = pos.getTableColumn();
        String cellData = (String) col.getCellObservableValue(row).getValue();
        if (cellData != null) {
          final Clipboard clipboard = Clipboard.getSystemClipboard();
          final ClipboardContent content = new ClipboardContent();
          content.putString(cellData);
          clipboard.setContent(content);
        }
      }
    });


    // Устанавливаем стили для таблицы и колонок
    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    tableView.setStyle("-fx-font-size: 16px; -fx-background-color: rgba(255, 255, 255, 0.5); -fx-border-color: rgba(83,78,78,0.5)");
    attributeCol.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
    valueCol.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");
    tab.setStyle("-fx-font-size: 16px;");

    VBox.setVgrow(tableView, Priority.ALWAYS);
    VBox vbox = new VBox(tableView);
    vbox.setPadding(new Insets(10));
    vbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5);");
    tab.setContent(vbox);
    tab.setOnClosed(event -> {
      if (tabPane.getTabs().isEmpty()) {
        messageLabel.setVisible(true); // Показать текст, если все вкладки закрыты
        root.setCenter(messageLabel);
      }
    });
    tabPane.getTabs().add(tab);
  }

  private void addFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      try {
        addNewTab(file);
        messageLabel.setVisible(false);
        root.setCenter(tabPane);
      } catch (Exception e) {
        showError(e.getMessage());
      }
    }
  }

  private void showHelp() {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Справка");
    alert.setHeaderText(null);
    alert.setContentText("Чтобы использовать это приложение:\n" +
        "- Перетащите файлы в окно, чтобы добавить их в виде вкладок.\n" +
        "- Альтернативно, нажмите \"Добавить\", чтобы выбрать файл с помощью проводника.\n" +
        "- Внутри каждой вкладки будет отображаться информация о соответствующем файле.\n" +
        "- Чтобы скопировать содержимое ячейки, нажмите на неё, а затем на сочетание клавиш Ctrl+C.\n" +
        "\n");
    alert.showAndWait();
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Ошибка");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.show();
  }

  public static class FileDetails {
    private final String attribute;
    private final String value;

    public FileDetails(String attribute, String value) {
      this.attribute = attribute;
      this.value = value;
    }

    public String getAttribute() {
      return attribute;
    }

    public String getValue() {
      return value;
    }
  }
}
