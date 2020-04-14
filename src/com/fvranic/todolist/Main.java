package com.fvranic.todolist;
	
import com.fvranic.datamodel.TodoData;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
		
	@Override
	public void start(Stage primaryStage) throws Exception {

		Parent root = FXMLLoader.load(getClass().getResource("mainwindow.fxml"));
		Scene scene = new Scene(root,900,500);
		primaryStage.setTitle("TodoList by fvranic");
		primaryStage.setScene(scene);
		primaryStage.show();
	}	
	
	@Override
	public void init() throws Exception {

		TodoData.getInstance().loadTodoItems();
	}
	
	@Override
	public void stop() throws Exception {
		
		TodoData.getInstance().storeTodoItems();	
	}
	
	public static void main(String[] args) {
		
		launch(args);
	}
}
