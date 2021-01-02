package application;

import java.io.File;
import java.io.IOException;

import org.fxmisc.richtext.CustomStyleableProperty;
import org.fxmisc.richtext.StyleClassedTextArea;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.StyleableObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class MainController {
	@FXML
	protected StyleClassedTextArea textArea;

	@FXML
	private ToolBar toolBar;

	@FXML
	private HBox infoBar;

	@FXML
	private ComboBox<String> fontFamBox;

	@FXML
	private ComboBox<String> hColorBox;

	@FXML
	private Spinner sizeSpinner;

	private File currentFile;
	private String currentContent;
	private String fileContent = "";
	private boolean modified = false;
	private static final String DEFAULT_TITLE = "Untitled.txt";
	private static final int DEFAULT_TEXT_SIZE = 14;
	private static final String RTFX_FILE_EXTENSION = ".rtfx";
	private static final String RTF_FILE_EXTENSION = ".rtf";

	BooleanProperty selectedToolBar;
	BooleanProperty selectedInfoBar;

	public void initialize() {
		selectedToolBar = new SimpleBooleanProperty(true);
		toolBar.managedProperty().bind(selectedToolBar);
		selectedInfoBar = new SimpleBooleanProperty(true);
		infoBar.managedProperty().bind(selectedInfoBar);
		Main.getStage().setTitle("Untitled.txt");

		ObservableList fonts = FXCollections.observableArrayList(Font.getFamilies());
		fontFamBox.getItems().addAll(fonts);

		// colorPicker.valueProperty().addListener((observable, oldColor, newColor) ->
		// updateTextColor(newColor));
		ObservableList<String> highlights = FXCollections.observableArrayList("transparent", "yellow", "lime",
				"orangered", "orange", "cyan");
		hColorBox.setItems(highlights);
		Callback<ListView<String>, ListCell<String>> factory = new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> list) {
				return new ColorRectCell();
			}
		};
		hColorBox.setCellFactory(factory);
		hColorBox.setButtonCell(factory.call(null));

		textArea.setStyle("-fx-font-size: " + DEFAULT_TEXT_SIZE + ";");

		sizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50));
		sizeSpinner.getValueFactory().setValue(DEFAULT_TEXT_SIZE);
		// sizeSpinner.valueProperty().addListener((obs, oldValue, newValue) ->
		// updateFontSize((int) newValue));
		sizeSpinner.valueProperty()
				.addListener((obs, oldValue, newValue) -> textArea.setStyle("-fx-font-size: " + newValue + ";"));
	}

	public void newFile(ActionEvent event) {
		updateModified();
		if (modified == true) {
			// Dialog warning
		} else {
			currentFile = null;
			currentContent = "";
			setDefaultTitle();
			clearAll();
		}
	}

	public void openFile() throws IOException {
		String initialDir = System.getProperty("user.dir");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load document");
		fileChooser.setInitialDirectory(new File(initialDir));
		fileChooser.setSelectedExtensionFilter(
				new FileChooser.ExtensionFilter("*" + RTFX_FILE_EXTENSION, "*" + RTF_FILE_EXTENSION));
		File selectedFile = fileChooser.showOpenDialog(Main.getStage());
		if (selectedFile != null) {
			textArea.clear();
			FileOperations.open(selectedFile, textArea);
		}
		setFileTitle(selectedFile.getName());
		setFile(selectedFile);
	}

	public void saveFile() throws IOException {
		String initialDir = System.getProperty("user.dir");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save document");
		fileChooser.setInitialDirectory(new File(initialDir));
		fileChooser.setInitialFileName("Untitled" + RTFX_FILE_EXTENSION);
		File selectedFile = fileChooser.showSaveDialog(Main.getStage());
		if (selectedFile != null) {
			FileOperations.save(selectedFile, textArea);
		}
	}

	public void saveFileAs() throws IOException {
		setFile(FileOperations.create(textArea.getText()));
	}

	public void clearAll() {
		textArea.clear();
	}

	public void undo() {
		textArea.undo();
	}

	public void redo() {
		textArea.redo();
	}

	public void selectAll() {
		textArea.selectAll();
	}

	public void closeApp(ActionEvent event) {
		Platform.exit(); // Exit from JavaFx
		System.exit(0);
	}

	// Should I update content to match

	public void toggleBold() {
		StyleOperations.updateStyleInSelection(textArea, "bold", textArea.getSelection());
	}

	public void toggleItalic() {
		StyleOperations.updateStyleInSelection(textArea, "italic", textArea.getSelection());
	}

	public void toggleUnderline() {
		StyleOperations.updateStyleInSelection(textArea, "underline", textArea.getSelection());
	}

	public void toggleStrike() {
		StyleOperations.updateStyleInSelection(textArea, "strike", textArea.getSelection());
	}

	public void toggleAlignLeft() {
		StyleOperations.updateParagraphStyleInSelection(textArea, "align-left", textArea.getSelection());
	}

	public void toggleAlignCenter() {
		StyleOperations.updateParagraphStyleInSelection(textArea, "align-center", textArea.getSelection());
	}

	public void toggleAlignRight() {
		StyleOperations.updateParagraphStyleInSelection(textArea, "align-right", textArea.getSelection());
	}

	public void toggleJustify() {
		StyleOperations.updateParagraphStyleInSelection(textArea, "justify", textArea.getSelection());
	}

	public void updateFontFam() {

	}

	public void updateFontSize(int newSize) {
		StyleOperations.updateSizeInSelection(textArea, newSize, textArea.getSelection());
	}

	public void updateHColor() {
		String newHColor = hColorBox.getValue();
		StyleOperations.updateColorInSelection(textArea, "h-" + newHColor, textArea.getSelection());
	}

	public void toolBarVisible(ActionEvent event) {
		selectedToolBar.set(!selectedToolBar.get());
	}

	public void infoBarVisible(ActionEvent event) {
		selectedInfoBar.set(!selectedInfoBar.get());
	}

	private void setFileTitle(String title) {
		Main.getStage().setTitle(title);
	}

	private void setFile(File newCurrentFile) throws IOException {
		currentFile = newCurrentFile;
		modified = false;
		setTitle(newCurrentFile.getName());
		updateContent(FileOperations.readFile(newCurrentFile));
	}

	private void setTitle(String newTitle) {
		Main.getStage().setTitle(newTitle);
	}

	private void setDefaultTitle() {
		Main.getStage().setTitle(DEFAULT_TITLE);
	}

	private void updateModified() {
		updateContent();
		modified = !currentContent.equals(currentContent);
	}

	private void updateContent() {
		currentContent = textArea.getText();
	}

	private void updateContent(String content) {
		currentContent = content;
	}

	static String colorToCss(Color color) {
		int red = (int) (color.getRed() * 255);
		int green = (int) (color.getGreen() * 255);
		int blue = (int) (color.getBlue() * 255);
		return "rgb(" + red + ", " + green + ", " + blue + ")";
	}

	static class ColorRectCell extends ListCell<String> {
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			Rectangle rect = new Rectangle(13, 15);
			if (item != null) {
				rect.setFill(Color.web(item));
				setGraphic(rect);
			}
		}
	}

}
