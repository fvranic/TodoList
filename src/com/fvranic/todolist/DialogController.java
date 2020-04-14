package com.fvranic.todolist;

import java.time.LocalDate;

import com.fvranic.datamodel.TodoData;
import com.fvranic.datamodel.TodoItem;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class DialogController {

	@FXML
	private TextField shortDescriptionField;
	@FXML
	private TextArea detailsArea;
	@FXML 
	private DatePicker deadlinePicker;
	
	public TodoItem processResults(){
		
		String shortDescription = shortDescriptionField.getText().trim();
		String details = detailsArea.getText().trim();
		LocalDate dedalineValue = deadlinePicker.getValue();
		
		TodoItem newItem = new TodoItem(shortDescription, details, dedalineValue);
		TodoData.getInstance().addTodoItem(newItem);
		return newItem;
		
	}
	
	public void selectItemForEdit(TodoItem item) {
		
		if(item != null) {
			shortDescriptionField.setText(item.getShortDescription());
			detailsArea.setText(item.getDetails());
			deadlinePicker.setValue(item.getDeadline());
		}
	}
	
	public TodoItem processEdit() {
		
		String shortDescription = shortDescriptionField.getText().trim();
		String details = detailsArea.getText().trim();
		LocalDate dedalineValue = deadlinePicker.getValue();
		return new TodoItem(shortDescription, details, dedalineValue);
	}
	
	
}
