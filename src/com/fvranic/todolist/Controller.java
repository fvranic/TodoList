package com.fvranic.todolist;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import com.fvranic.datamodel.TodoData;
import com.fvranic.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class Controller {
	
	@FXML
	private ListView<TodoItem> todoListView;
	@FXML
	private TextArea itemDetailsTextArea;
	@FXML
	private Label deadlineLabel;
	@FXML
	private BorderPane mainBorderPane;
	@FXML
	private ContextMenu listContextMenu;
	@FXML
	private ToggleButton filterToggleButton;
	
	private FilteredList<TodoItem> filteredList;

	private Predicate<TodoItem> wantAllItems;
	private Predicate<TodoItem> wantTodayItems;
	
	public void initialize() {
		
		listContextMenu = new ContextMenu();
		
		//event handler za Delete iz Context Menu-a kad se na listi ide desni klik na odreðeni element
		MenuItem deleteMenuItem = new MenuItem("Delete");
		deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				TodoItem item = todoListView.getSelectionModel().getSelectedItem();
				deleteItem(item);
			}
		});		
		listContextMenu.getItems().addAll(deleteMenuItem);
		
		//event handler za Delete iz Context Menu-a kad se na listi ide desni klik na odreðeni element		
		MenuItem editMenuItem = new MenuItem("Edit");
		editMenuItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				showEditItemDialog();				
			}
		});
		listContextMenu.getItems().addAll(editMenuItem);
		
		//za selektirani item postavljaj detalje i deadline 
		todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
			@Override
			public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
				
				if(newValue != null) {
					
					TodoItem item = todoListView.getSelectionModel().getSelectedItem();
					itemDetailsTextArea.setText(item.getDetails());
					DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
					deadlineLabel.setText(df.format(item.getDeadline()));
				}
			}
		});
		
		//predikat za filter listu za sve dane (kad se otpusti)
		wantAllItems = new Predicate<TodoItem>() {

			@Override
			public boolean test(TodoItem todoItem) {
				//ako je prošao test, vraæa true što znaèi da æe se prikazati
				return true;
			}
		};
		
		//predikat za filter listu za današnji dan (kad se stisne)
		wantTodayItems = new Predicate<TodoItem>() {

			@Override
			public boolean test(TodoItem todoItem) {
				//ovisno o današnjem datumu æe se prikazati
				return todoItem.getDeadline().equals(LocalDate.now());
			}
		};
		
		//postavi filter listu za sve dane - dohvati listu 
		filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(), wantAllItems);
		
		//sortiranje filtirane liste po deadline-u
		SortedList<TodoItem> sortedList = new SortedList<TodoItem>(filteredList, new Comparator<TodoItem>() {

				@Override
				public int compare(TodoItem todoItem1, TodoItem todoItem2) {
	
					return todoItem1.getDeadline().compareTo(todoItem2.getDeadline());
				}
			
			});
		
		//data binding
		//todoListView.setItems(TodoData.getInstance().getTodoItems()); //dohvaæa listu iz TodoItema za load/store podatak iz/u fajl
		//postavi sortiranu listu u ListView
		todoListView.setItems(sortedList);
		//omoguæi odabir samo jednog elementa u listi
		todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		//selectiraj po defaultu prvi element u listi
		todoListView.getSelectionModel().selectFirst();
		
		//postavlja nove æelije za listu tipa TodoItem
		todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {	
			
			@Override
			public ListCell<TodoItem> call(ListView<TodoItem> param) {
				
				ListCell<TodoItem> cell = new ListCell<TodoItem>() {
					
					//customiziranje svake æelije iz ListViewa
					@Override
					protected void updateItem(TodoItem item, boolean empty) {
						
						super.updateItem(item, empty);
						
						if(empty) {
							//ako je prazan postavi null
							setText(null);
						}else {
							//ako nije postavi kratki opis u æeliju
							setText(item.getShortDescription());	
							
							//ovisno o datumu primjeni boju i stil
							if(item.getDeadline().isBefore(LocalDate.now())) {
								setTextFill(Color.ORANGE);
								setStyle("-fx-underline: false;");
							}else if(item.getDeadline().equals(LocalDate.now()) || item.getDeadline().isAfter(LocalDate.now())) {
								setTextFill(Color.RED);
								setStyle("-fx-underline: true;");
							}
						}
					}
				};			
				
				//ako je lista prazna onemoguæi iskljuèi Context menua (desnog klika za Delete/Edit) 
				cell.emptyProperty().addListener(
					(obs, wasEmpty, isNowEmpty) ->  {
						if(isNowEmpty) {
							cell.setContextMenu(null);
						}else {
							cell.setContextMenu(listContextMenu);
						}
				});
				
				return cell;
			}
		});
	}
	
	@FXML
	public void showNewItemDialog() {
		
		//Po defaultu dialog je mobilan
		Dialog<ButtonType> dialog = new Dialog<>();
		//vlasnik tog dialoga
		dialog.initOwner(mainBorderPane.getScene().getWindow());
		dialog.setTitle("Add New Todo Item");
		dialog.setHeaderText("Users dialog to create new todo item");
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
		
		try {
			
			dialog.getDialogPane().setContent(fxmlLoader.load());
			
		}catch(IOException e) {
			System.out.println("Couldnt load the dialog.");
			e.printStackTrace();
			return;
		}
		
		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		
		Optional<ButtonType> result = dialog.showAndWait();
		
		if(result.isPresent() && result.get() == ButtonType.OK) {
			DialogController controller = fxmlLoader.getController();
			TodoItem newItem = controller.processResults();
			todoListView.getSelectionModel().select(newItem);
		}
		
	}
	
	@FXML
	public void showEditItemDialog() {
		
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.initOwner(mainBorderPane.getScene().getWindow());
		dialog.setTitle("Edit Todo Item");
		dialog.setHeaderText("Users dialog to edit current todo item");
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
		
		try{ 
			dialog.getDialogPane().setContent(fxmlLoader.load());
		}catch(IOException e) {
			System.out.println("Couldnt load the dialog.");
			e.printStackTrace();
			return;
		}

		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
		
		DialogController controller = fxmlLoader.getController();
		TodoItem currentItemForEdit = todoListView.getSelectionModel().getSelectedItem();
		controller.selectItemForEdit(currentItemForEdit);
		
		Optional<ButtonType> result = dialog.showAndWait();
		if(result.isPresent() && result.get() == ButtonType.OK) {
			TodoItem editedItem = controller.processEdit();
			TodoData.getInstance().editTodoItem(currentItemForEdit,editedItem);
		}
	}
	
	@FXML
	public void showDeleteItemAlert() {

		if(!todoListView.getSelectionModel().isEmpty()) {
			TodoItem currentItemForEdit = todoListView.getSelectionModel().getSelectedItem();
			
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Delete Todo Item");	
			alert.setHeaderText("Delete item: "+currentItemForEdit.getShortDescription());
			alert.setContentText("Are you sure? Press OK to confirm, or cancel to back out");
			
			Optional<ButtonType> result = alert.showAndWait();
			
			if(result.isPresent() && result.get() == ButtonType.OK) {		
				TodoData.getInstance().deleteTodoItem(currentItemForEdit);
			}
		}
	}
	
	//pokreni prozorèiæ za brisanje elementa kad se stisne DELETE tipka na odabrani elementm u listi
		@FXML
		public void handleKeyPressed(KeyEvent keyEvent) {
			
			TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
			if(selectedItem != null) {
				if(keyEvent.getCode().equals(KeyCode.DELETE)) {
					deleteItem(selectedItem);
				}
			}
		}
	
	//postavi alert prozorèiæ kad se briše element
	public void deleteItem(TodoItem item) {

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Delete Todo Item");	
		alert.setHeaderText("Delete item: "+item.getShortDescription());
		alert.setContentText("Are you sure? Press OK to confirm, or cancel to back out");
		
		Optional<ButtonType> result = alert.showAndWait();
		
		if(result.isPresent() && result.get() == ButtonType.OK) {
			
			TodoData.getInstance().deleteTodoItem(item);
		}
	}
			
	//postavlja detalje i deadline kad se selektira element iz liste
	@FXML
	public void handleClickListView() {
		
		TodoItem item = todoListView.getSelectionModel().getSelectedItem();
		itemDetailsTextArea.setText(item.getDetails());
		deadlineLabel.setText(item.getDeadline().toString());
				
	}
		
	//hendlaj predikate za sve elemente i današnje elemente u filter listi kad se klikne na toggle bottun
	@FXML
	public void handleFilterButton() {
		
		TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
		
		if(filterToggleButton.isSelected()) {
			
			filteredList.setPredicate(wantTodayItems);
			if(filteredList.isEmpty()) {
				itemDetailsTextArea.clear();
				deadlineLabel.setText("");
			}else if(filteredList.contains(selectedItem)) {
				todoListView.getSelectionModel().select(selectedItem);
			}else {
				todoListView.getSelectionModel().selectFirst();
			}
		}else {
			filteredList.setPredicate(wantAllItems);
			todoListView.getSelectionModel().select(selectedItem);
		}
	}
	
	//izlaz iz aplikacije
	@FXML
	public void handleExit() {

		Platform.exit();
	}
	
	
}
