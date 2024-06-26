package controller;

// Java imports
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

// JavaFX imports
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

// Project imports
import model.Album;
import model.Photo;
import model.User;
import model.Photos;

/**
 * HomepageController class to control the homepage view
 * This class is responsible for displaying the albums in the homepage
 * and handling the selection of an album.
 * It also allows the user to delete, rename, create, and open albums.
 * It also allows the user to logout and quit the application.
 * It also allows the user to search for photos.
 *
 * @author jacobjude
 * @author ks1686
 */
public class HomepageController {
  public AnchorPane albumList;
  public Button deleteAlbumButton;
  public Button logoutButton;
  public Button renameAlbumButton;
  public Button createAlbumButton;
  public Button openAlbumButton;
  public Button quitButton;
    @FXML
    protected AlbumListController albumListController;

    @FXML protected TextField searchBarTextField;
    private Photos app;

    // private user object
    private User user;

    /**
     * Method to start the homepage controller
     * @param user: the user object
     * @param app: the Photos application
     */
    public void start(User user, Photos app) { // TODO: make a User object and pass that instead
        albumListController.start(user, app);
        this.app = app;
        this.user = user;
    }

    /**
     * Method to delete an album
     */
    @FXML
    private void deleteAlbum() {
        String albumName = albumListController.getSelectedAlbum();

        // fix the album name
        if (albumName == null) {
            Photos.errorAlert("Error", "No album selected", "Please select an album to delete.");
            return;
        }
        albumName = albumListController.fixAlbumName(albumName);

        // if album is not null, delete the album
        if (albumName != null) {
            albumListController.deleteAlbum(albumName);
        }


    }

    /**
     * Method to logout of the application
     */
    @FXML
    private void logout() {
        app.logout(app);
        // close the current window
        Stage stage = (Stage) albumListController.albumListView.getScene().getWindow();
        stage.close();
    }

    /**
     * Method to rename an album
     */
    @FXML
    private void renameAlbum() {
        // open a text input dialog box
        String albumName = albumListController.getSelectedAlbum();

        if (albumName == null) {
            Photos.errorAlert("Error", "No album selected", "Please select an album to rename.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename Album");
        dialog.setHeaderText("Enter the new album name:");
        dialog.setContentText("New album name:");

        // get the new album name
        String newAlbumName;
        try {
            newAlbumName = dialog.showAndWait().get();
        } catch (NoSuchElementException e) {
            return;
        }

        // get the selected album
        albumName = albumListController.fixAlbumName(albumListController.getSelectedAlbum());

        // if the album name is not null, rename the album
        if (albumName != null) {
            albumListController.renameAlbum(albumName, newAlbumName);
        } else {
            Photos.errorAlert("Error", "Invalid Album Name", "The album name is invalid.");
        }
    }

    /**
     * Method to create an album
     */
    @FXML public void createAlbum() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Album");
        dialog.setHeaderText("Enter the name of the new album:");
        dialog.setContentText("Album name:");

        String albumName;
        try {
            albumName = dialog.showAndWait().get();
        } catch (NoSuchElementException e) {
            return;
        }
        albumListController.createAlbum(albumName);

    }

    /**
     * Method to check if a search query is valid
     * @param query: the search query
     * @return true if the search query is valid, false otherwise
     */
    private boolean isValidSearchQuery(String query) {
        if (query == null || query.isEmpty()) {
            return false;
        }

        // query must be of the form MM/DD/YYYY-MM/DD/YYYY OR a tag in the form "key=value" OR a conjunction/disjunction of tags like "key=value AND key=value"m "key=value OR key=value"
        return query.matches("\\d{2}/\\d{2}/\\d{4}-\\d{2}/\\d{2}/\\d{4}") || query.matches("\\w+=\\w+") || query.matches("\\w+=\\w+ (AND|OR) \\w+=\\w+");
    }

    /**
     * Method to search for photos
     * @throws IOException: if the search results page cannot be loaded
     */
    @FXML public void searchPhotos() throws IOException {
        // string in the text bar
        String query = searchBarTextField.getText();
        if (!(isValidSearchQuery(query))){
            Photos.errorAlert("Invalid Search Query", "Invalid Search Query", "Invalid Search Query");
            return;
        }
        List<Photo> photos = user.searchAlbums(query);

        if (photos == null) {
            Photos.errorAlert("Search Error", "Search query is invalid. ", "Hover over search bar and see the tooltip for more information.");
            return;
        }
        
        

        String tempAlbumName = "Search Results";
        // go through the user's albums. if the album name is the same as the temp album name, keep appending a number to the end until it's unique
        int count = 1;
        String uniqueAlbumName = tempAlbumName;
        while (user.getAlbum(uniqueAlbumName) != null) {
            uniqueAlbumName = tempAlbumName + count;
            count++;
        }

        // create a new, temporary album to store the search results
        Album searchResults = new Album(uniqueAlbumName, photos);
        // load the gallery controller
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/searchresults.fxml"));
        Pane root = loader.load();
        SearchResultsController searchResultsController = loader.getController();

        // get the current stage
        Stage stage = (Stage) albumListController.albumListView.getScene().getWindow();
        // start the gallery controller
        
        
        // set the scene
        Scene scene = new Scene(root, 800, 600);
        searchResultsController.start(app, user, searchResults);
        stage.setScene(scene);
        stage.show();


    }

    /**
     * Method to quit the application
     */
    @FXML public void quit() {
        // quit application, but save it also
        app.quit();
    }

    /**
     * Method to open an album
     * @throws IOException: if the album cannot be opened
     */
    @FXML public void openAlbum() throws IOException {
        // open the selected album
        String albumName = albumListController.getSelectedAlbum();
        if (albumName != null) {
            albumName = albumListController.fixAlbumName(albumName);
        } else {
            Photos.errorAlert("Open Album", "Failed to open album", "No album selected");
            return;
        }
        

        // get the album object
        Album album = user.getAlbum(albumName);

        // if the album is not null, open the album
        if (album != null) {
            // load the gallery controller
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/gallery.fxml"));
            Pane root = loader.load();
            GalleryController galleryController = loader.getController();

            // get the current stage
            Stage stage = (Stage) albumListController.albumListView.getScene().getWindow();
            // start the gallery controller
            
            galleryController.start(app, album, user);
            // set the scene
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
        } else {
            // show an error alert that the album could not be opened
            Photos.errorAlert("Open Album", "Failed to open album", "Failed to open album");
        }

    }


}
