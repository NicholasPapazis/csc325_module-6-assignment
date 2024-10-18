package com.example.csc325_firebase_webview_auth.view;//package modelview;

import com.example.csc325_firebase_webview_auth.model.FirestoreContext;
import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.google.firebase.auth.ActionCodeSettings;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javafx.scene.image.ImageView;



public class AccessFBView {


    @FXML
    private TextField nameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField ageField;
    @FXML
    private Button writeButton;
    @FXML
    private Button readButton;
    @FXML
    private TextArea outputField;

    //column output fields
    @FXML
    private TableView table;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn majorColumn;
    @FXML
    private TableColumn ageColumn;


    //register credentials
    @FXML
    private TextField emailInput;
    @FXML
    private TextField passwordInput;
    @FXML
    private TextField phoneNumberInput;
    @FXML
    private TextField displayNameInput;
    @FXML
    private TextField imageUrlInput;

    //login credentials
    @FXML
    private TextField emailInputLogin;
    @FXML
    private TextField passwordInputLogin;
    //profile pic
    @FXML
    private ImageView profilepic;



    //firebase authentication object
    private FirebaseAuth auth;




    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }

    void initialize() {

        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        nameField.textProperty().bindBidirectional(accessDataViewModel.userNameProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        writeButton.disableProperty().bind(accessDataViewModel.isWritePossibleProperty().not());
    }

    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

        @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

            @FXML
    private void regRecord(ActionEvent event) {
        registerUser();
    }

     @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("/files/WebContainer.fxml");
    }

    public void addData() {

        DocumentReference docRef = App.fstore.collection("References").document(UUID.randomUUID().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("Name", nameField.getText());
        data.put("Major", majorField.getText());
        data.put("Age", Integer.parseInt(ageField.getText()));
        //asynchronously write data
        ApiFuture<WriteResult> result = docRef.set(data);
    }

        public boolean readFirebase()
         {
             listOfUsers.clear(); //prevents displaying same data over and over when read button is clicked

             key = false;

        //asynchronously retrieve all documents
        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();
        // future.get() blocks on response
        List<QueryDocumentSnapshot> documents;
        try
        {
            documents = future.get().getDocuments();
            if(documents.size()>0)
            {
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents)
                {
                    //outputs data on top section
                    /*outputField.setText(outputField.getText()+ document.getData().get("Name")+ " , Major: "+
                            document.getData().get("Major")+ " , Age: "+
                            document.getData().get("Age")+ " \n ");*/
                    System.out.println(document.getId() + " => " + document.getData().get("Name"));
                    person  = new Person(String.valueOf(document.getData().get("Name")),
                            document.getData().get("Major").toString(),
                            Integer.parseInt(document.getData().get("Age").toString()));
                    listOfUsers.add(person); //adds users to observable list

                    //associate the data with the table columns
                    nameColumn.setCellValueFactory(
                            new PropertyValueFactory<Person, String>("Name")
                    );
                    majorColumn.setCellValueFactory(
                            new PropertyValueFactory<Person, String>("Major")
                    );
                    ageColumn.setCellValueFactory(
                            new PropertyValueFactory<Person, String>("Age")
                    );
                    table.setItems(listOfUsers); //add the data to the table
                }
            }
            else
            {
               System.out.println("No data");
            }
            key=true;

        }
        catch (InterruptedException | ExecutionException ex)
        {
             ex.printStackTrace();
        }
        return key;
    }

        public void sendVerificationEmail() {
            try {
                UserRecord user = App.fauth.getUser("name");
                //String url = user.getPassword();

            } catch (Exception e) {
            }
    }


    public boolean registerUser() {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                /*.setEmail("user@example.com")
                .setEmailVerified(false)
                .setPassword("secretPassword")
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setDisabled(false);*/
                .setEmail(emailInput.getText())
                .setEmailVerified(false)
                .setPassword(passwordInput.getText())
                .setPhoneNumber(phoneNumberInput.getText())
                .setDisplayName(displayNameInput.getText())
                .setDisabled(false)
                .setPhotoUrl(imageUrlInput.getText());

        UserRecord userRecord;
        try {
            userRecord = App.fauth.createUser(request);
            System.out.println("Successfully created new user: " + userRecord.getUid());
            return true;

        } catch (FirebaseAuthException ex) {
           Logger.getLogger(FirestoreContext.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }


    }


    public void switchToRegisterPage(ActionEvent actionEvent) {

        try {
            //load the fxml file for the popup window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/files/register.fxml"));
            Parent popupRoot = loader.load();

            //create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Register");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(popupRoot));
            popupStage.show();
        } catch (IOException ex) {
            Logger.getLogger(WebContainerController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }



    public void logInUser(ActionEvent actionEvent) {


        try{
            String userEmail = emailInputLogin.getText();
            //UserRecord user = App.fauth.getUser("papane@farmingdale.edu");
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(userEmail);


            String email = user.getEmail();
            if(email == null){
                System.out.println("user not found");
                return;
            }
            String userImgUrl = user.getPhotoUrl();
            System.out.print(userImgUrl);
            Image image = new Image(userImgUrl);
            profilepic.setImage(image);






        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }



    public void switchToLoginPage(ActionEvent actionEvent) {
        try {
            //load the fxml file for the popup window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/files/login.fxml"));
            Parent popupRoot = loader.load();

            //create a new stage for the popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Login");
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(popupRoot));
            popupStage.show();
        } catch (IOException ex) {
            Logger.getLogger(WebContainerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
