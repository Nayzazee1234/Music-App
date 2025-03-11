package com.lab;
 
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.*;
import javafx.util.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.MapChangeListener;
 
public class App extends Application {
    private Song currentSong;
    private Map<String, List<Song>> playlists = new HashMap<>();
    private String currentPlaylist = "All";
    private int currentTrackIndex = 0;
    private ComboBox<String> playlistSelector;
 
    @Override
    public void start(Stage primaryStage) {
 
        playlists.put("All", new ArrayList<>());
        playlists.put("Pop", new ArrayList<>());
        playlists.put("Rock", new ArrayList<>());
        playlists.put("Jazz", new ArrayList<>());
 
        // UI Components
        Button openButton = new Button("Open Music Files");
        Button playPauseButton = new Button("▶");
        Button stopButton = new Button("Stop");
        Button nextButton = new Button("⏭");
        Button prevButton = new Button("⏮");
        Label songLabel = new Label("No Song Playing");
        Label songInfoLabel = new Label("Song Info");
 
        // Dropdown เลือก Playlist
        playlistSelector = new ComboBox<>();
        playlistSelector.getItems().addAll("All", "Pop", "Rock", "Jazz");
        playlistSelector.setValue("All");
 
        // Sliders
        Slider progressSlider = new Slider(0, 100, 0);
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setOrientation(javafx.geometry.Orientation.VERTICAL);
 
 
        // Album cover
        StackPane albumCover = new StackPane();
        albumCover.setPrefSize(200, 200);
        albumCover.setStyle("-fx-background-color: gray;");
 
        // File chooser
        openButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac"));
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
            if (files != null) {
                for (File file : files) {
                    Song song = assignSongType(file);
                    playlists.get("All").add(song);
       
                    // 🛠️ แก้ไขให้แน่ใจว่ามี key ก่อนเรียก add()
                    playlists.computeIfAbsent(song.getSongType(), k -> new ArrayList<>()).add(song);
                }
                currentTrackIndex = 0;
                playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton);
            }
        });
 
        playlistSelector.setOnAction(e -> {
            currentPlaylist = playlistSelector.getValue();
            currentTrackIndex = 0;
        });
 
 
        // Play or Pause song
        playPauseButton.setOnAction(e -> {
            if (currentSong != null && currentSong.mediaPlayer != null) {
                MediaPlayer.Status status = currentSong.mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    currentSong.pause();
                    playPauseButton.setText("▶");
                } else {
                    currentSong.play();
                    playPauseButton.setText("⏸");
                }
            }
        });
 
        // Seek track when user interacts with progress slider
        progressSlider.setOnMouseReleased(e -> {
            if (currentSong != null && currentSong.mediaPlayer != null) {
                currentSong.mediaPlayer.seek(Duration.millis(progressSlider.getValue()));
            }
        });
 
        // Stop song
        stopButton.setOnAction(e -> {
            if (currentSong != null) {
                currentSong.stop();
                playPauseButton.setText("Play");
            }
        });
 
       
        // Next song
        nextButton.setOnAction(e -> {
            List<Song> playlist = playlists.get(currentPlaylist);
            if (!playlist.isEmpty() && currentTrackIndex < playlist.size() - 1) {
                currentTrackIndex++;
                playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton);
            }
        });
 
        // Previous song
        prevButton.setOnAction(e -> {
            List<Song> playlist = playlists.get(currentPlaylist);
            if (!playlist.isEmpty() && currentTrackIndex > 0) {
                currentTrackIndex--;
                playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton);
            }
        });
       
        // Volume slider
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentSong != null && currentSong.mediaPlayer != null) {
                currentSong.mediaPlayer.setVolume(newValue.doubleValue());
            }
        });
     
        // Layout
        VBox controlsLayout = new VBox(15, prevButton, playPauseButton, nextButton);
        controlsLayout.getStyleClass().add("controls");
       
        VBox volumeLayout = new VBox(10, new Label("Volume"), volumeSlider);
        volumeLayout.setAlignment(Pos.CENTER);
       
        HBox mainControls = new HBox(15, controlsLayout, volumeLayout);
        mainControls.setAlignment(Pos.CENTER);
 
        VBox mainLayout = new VBox(15, songLabel, songInfoLabel, playlistSelector, albumCover, mainControls, openButton, stopButton, progressSlider);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-padding: 20px;");
        Scene scene = new Scene(mainLayout, 450, 500);
        scene.getStylesheets().add(getClass().getResource("music_player.css").toExternalForm());
           
        primaryStage.setTitle("Music Player");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void playTrack(Label songLabel, Label songInfoLabel, Slider progressSlider, StackPane albumCover, Button playPauseButton) {
        List<Song> playlist = playlists.get(currentPlaylist);
        if (!playlist.isEmpty()) {
            if (currentSong != null) {
                currentSong.stop();
                currentSong.mediaPlayer.dispose();
            }
   
            currentSong = playlist.get(currentTrackIndex);
   
            // 🛠️ แก้ไขให้สร้าง MediaPlayer ใหม่ และกำหนดค่าให้ currentSong.mediaPlayer
            Media media = new Media(currentSong.file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            currentSong.mediaPlayer = mediaPlayer;  
            mediaPlayer.play();
           
            playPauseButton.setText("Pause");
            songLabel.setText("Playing: " + currentSong.getFileName());
            songInfoLabel.setText("Song Info: " + currentSong.getSongType());
   
            // 🛠️ เช็คว่า Metadata มีข้อมูลก่อน
            if (mediaPlayer.getMedia() != null && mediaPlayer.getMedia().getMetadata() != null) {
                mediaPlayer.getMedia().getMetadata().addListener(new MapChangeListener<String, Object>() {
                    @Override
                    public void onChanged(Change<? extends String, ? extends Object> change) {
                    if (change.wasAdded() && "image".equals(change.getKey())) {
                        Object imageData = change.getValueAdded();
                        if (imageData instanceof Image) {
                            ImageView albumImageView = new ImageView((Image) imageData);
                            albumImageView.setFitWidth(200);
                            albumImageView.setFitHeight(200);
                            albumImageView.setPreserveRatio(true);
   
                            albumCover.getChildren().clear();
                            albumCover.getChildren().add(albumImageView);
                        }
                    }
                }
            });
        }
   
            // Update progress bar
            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = mediaPlayer.getTotalDuration();
                if (totalDuration != null && !totalDuration.isUnknown()) {
                    progressSlider.setMax(totalDuration.toMillis());
                }
            });
   
            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!progressSlider.isPressed()) {
                    progressSlider.setValue(newValue.toMillis());
                }
            });
   
            // Next song when finished
            mediaPlayer.setOnEndOfMedia(() -> {
                if (currentTrackIndex < playlist.size() - 1) {
                    currentTrackIndex++;
                    playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton);
                }
            });
   
            // Seek track
            progressSlider.setOnMouseReleased(e -> {
                if (currentSong != null) {
                    currentSong.mediaPlayer.seek(Duration.millis(progressSlider.getValue()));
                }
            });
        }
    }
   
 
    private Song assignSongType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.contains("pop")) {
            return new PopSong(file);
        } else if (fileName.contains("rock")) {
            return new RockSong(file);
        } else if (fileName.contains("jazz")) {
            return new JazzSong(file);
        } else {
            return new Song(file);
        }
    }
   
    public static void main(String[] args) {
        launch(args);
    }
}
 
// Song Classes
class Song {
    protected File file;
    protected MediaPlayer mediaPlayer;
 
    public Song(File file) {
        this.file = file;
        Media media = new Media(file.toURI().toString());
        this.mediaPlayer = new MediaPlayer(media);
    }
 
    public void play() {
        if (mediaPlayer == null) {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
        }
        MediaPlayer.Status status = mediaPlayer.getStatus();
        if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
            mediaPlayer.play();
        }
    }
 
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
 
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        }
    }
 
    public String getSongType() {
        return "Generic Song";
    }
 
    public String getFileName() {
        return file.getName();
    }
}
 
class PopSong extends Song {
    public PopSong(File file) { super(file); }
    @Override public String getSongType() { return "Pop Song"; }
}
 
class RockSong extends Song {
    public RockSong(File file) { super(file); }
    @Override public String getSongType() { return "Rock Song"; }
}
 
class JazzSong extends Song {
    public JazzSong(File file) { super(file); }
    @Override public String getSongType() { return "Jazz Song"; }
}