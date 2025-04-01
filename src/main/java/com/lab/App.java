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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class App extends Application {
    private Song currentSong;
    private Map<String, List<Song>> playlists = new HashMap<>();
    private String currentPlaylist = "All";
    private int currentTrackIndex = 0;
    private ComboBox<String> playlistSelector;
    private TextField newPlaylistField;
    private ListView<String> songListView;

    @Override
    public void start(Stage primaryStage) {

        playlists.put("All", new ArrayList<>());

        // UI Components
        Button openButton = new Button("Open Music Files");
        Button playPauseButton = new Button("▶");
        Button stopButton = new Button("Stop");
        Button nextButton = new Button("⏭");
        Button prevButton = new Button("⏮");
        Label songLabel = new Label("No Song Playing");
        Label songInfoLabel = new Label("Song Info");
        Label timeLabel = new Label("00:00 / 00:00");

        // Playlist Management
        HBox playlistManagement = new HBox(10);
        newPlaylistField = new TextField();
        newPlaylistField.setPromptText("ชื่อ Playlist ใหม่");
        Button createPlaylistButton = new Button("สร้าง Playlist");
        Button addToPlaylistButton = new Button("เพิ่มเพลงลง Playlist");
        Button deletePlaylistButton = new Button("ลบ Playlist");
        ComboBox<String> targetPlaylistSelector = new ComboBox<>();
        targetPlaylistSelector.setPromptText("เลือก Playlist ที่ต้องการเพิ่มเพลง");
        targetPlaylistSelector.getItems().add("All");

        playlistManagement.getChildren().addAll(newPlaylistField, createPlaylistButton, targetPlaylistSelector, addToPlaylistButton, deletePlaylistButton);
        playlistManagement.setAlignment(Pos.CENTER);

        // Dropdown เลือก Playlist
        playlistSelector = new ComboBox<>();
        playlistSelector.getItems().add("All");
        playlistSelector.setValue("All");

        // สร้าง Playlist ใหม่
        createPlaylistButton.setOnAction(e -> {
            String newPlaylistName = newPlaylistField.getText().trim();
            if (!newPlaylistName.isEmpty() && !playlists.containsKey(newPlaylistName)) {
                playlists.put(newPlaylistName, new ArrayList<>());
                playlistSelector.getItems().add(newPlaylistName);
                targetPlaylistSelector.getItems().add(newPlaylistName);
                newPlaylistField.clear();
            }
        });

        // เพิ่มเพลงลง Playlist
        addToPlaylistButton.setOnAction(e -> {
            String selectedPlaylist = targetPlaylistSelector.getValue();
            if (selectedPlaylist != null && !selectedPlaylist.equals("All")) {
                ObservableList<Integer> selectedIndices = songListView.getSelectionModel().getSelectedIndices();
                if (!selectedIndices.isEmpty()) {
                    List<Song> allSongs = playlists.get(currentPlaylist);
                    int addedCount = 0;
                    for (int index : selectedIndices) {
                        Song selectedSong = allSongs.get(index);
                        if (!playlists.get(selectedPlaylist).contains(selectedSong)) {
                            playlists.get(selectedPlaylist).add(selectedSong);
                            addedCount++;
                        }
                    }
                    updateSongList(currentPlaylist);
                    if (addedCount > 0) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("เพิ่มเพลงสำเร็จ");
                        alert.setHeaderText(null);
                        alert.setContentText("เพิ่มเพลงจำนวน " + addedCount + " เพลงเรียบร้อยแล้ว");
                        alert.show();
                    }
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("คำเตือน");
                alert.setHeaderText(null);
                alert.setContentText("กรุณาเลือก Playlist ที่ต้องการเพิ่มเพลง");
                alert.show();
            }
        });

        // ลบ Playlist
        deletePlaylistButton.setOnAction(e -> {
            String selectedPlaylist = playlistSelector.getValue();
            if (!selectedPlaylist.equals("All")) {
                playlists.remove(selectedPlaylist);
                playlistSelector.getItems().remove(selectedPlaylist);
                targetPlaylistSelector.getItems().remove(selectedPlaylist);
                playlistSelector.setValue("All");
                currentPlaylist = "All";
            }
        });

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
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac"));
                List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
                if (files != null && !files.isEmpty()) {
                    int addedCount = 0;
                    for (File file : files) {
                        if (file.exists() && file.canRead()) {
                            Song song = assignSongType(file);
                            playlists.get("All").add(song);
                            playlists.computeIfAbsent(song.getSongType(), k -> new ArrayList<>()).add(song);
                            addedCount++;
                        }
                    }
                    if (addedCount > 0) {
                        currentTrackIndex = playlists.get("All").size() - addedCount;
                        playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton, timeLabel);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("เพิ่มเพลงสำเร็จ");
                        alert.setHeaderText(null);
                        alert.setContentText("เพิ่มเพลงจำนวน " + addedCount + " เพลงเรียบร้อยแล้ว");
                        alert.show();
                    }
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("เกิดข้อผิดพลาด");
                alert.setHeaderText(null);
                alert.setContentText("ไม่สามารถเพิ่มเพลงได้: " + ex.getMessage());
                alert.show();
            }
        });

        playlistSelector.setOnAction(e -> {
            currentPlaylist = playlistSelector.getValue();
            currentTrackIndex = 0;
            updateSongList(currentPlaylist);
            if (!playlists.get(currentPlaylist).isEmpty()) {
                playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton, timeLabel);
            }
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
                playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton, timeLabel);
            }
        });

        // Previous song
        prevButton.setOnAction(e -> {
            List<Song> playlist = playlists.get(currentPlaylist);
            if (!playlist.isEmpty() && currentTrackIndex > 0) {
                currentTrackIndex--;
                playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton, timeLabel);
            }
        });

        // Volume slider
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentSong != null && currentSong.mediaPlayer != null) {
                currentSong.mediaPlayer.setVolume(newValue.doubleValue());
            }
        });
     
        // Layout
        HBox controlsLayout = new HBox(15, prevButton, playPauseButton, nextButton);
        controlsLayout.setAlignment(Pos.CENTER);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        VBox volumeLayout = new VBox(10, new Label("Volume"), volumeSlider);
        volumeLayout.setAlignment(Pos.CENTER);

        HBox mainControls = new HBox(15, leftSpacer, controlsLayout, rightSpacer, volumeLayout);
        mainControls.setAlignment(Pos.CENTER);

        // Song List View
        Label songListLabel = new Label("รายการเพลง");
        songListLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        songListView = new ListView<>();
        songListView.setPrefHeight(150); // ลดความสูงลงจาก 300 เป็น 150
        songListView.setStyle("-fx-control-inner-background: #f4f4f4;");
        songListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        


        VBox mainLayout = new VBox(15, songLabel, songInfoLabel, playlistSelector, playlistManagement, songListLabel, songListView, albumCover, mainControls, openButton, stopButton, timeLabel, progressSlider);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-padding: 20px;");
        Scene scene = new Scene(mainLayout, 450, 500);
        scene.getStylesheets().add(getClass().getResource("music_player.css").toExternalForm());
           
        primaryStage.setTitle("Music Player");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void playTrack(Label songLabel, Label songInfoLabel, Slider progressSlider, StackPane albumCover, Button playPauseButton, Label timeLabel) {
        List<Song> playlist = playlists.get(currentPlaylist);
        if (!playlist.isEmpty()) {
            if (currentSong != null) {
                currentSong.stop();
                currentSong.mediaPlayer.dispose();
            }
    
            currentSong = playlist.get(currentTrackIndex);
    
            Media media = new Media(currentSong.file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            currentSong.mediaPlayer = mediaPlayer;  
            mediaPlayer.play();
            
            playPauseButton.setText("⏸");
            songLabel.setText("Playing: " + currentSong.getFileName());
            songInfoLabel.setText("Song Info: " + currentSong.getSongType());
    
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
    
            mediaPlayer.setOnReady(() -> {
                Duration totalDuration = mediaPlayer.getTotalDuration();
                if (totalDuration != null && !totalDuration.isUnknown()) {
                    progressSlider.setMax(totalDuration.toMillis());
                    String totalTime = formatTime(totalDuration);
                    timeLabel.setText("00:00 / " + totalTime);
                }
            });

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) return;
                if (!progressSlider.isPressed()) {
                    progressSlider.setValue(newValue.toMillis());
                    String currentTime = formatTime(newValue);
                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    if (totalDuration == null || totalDuration.isUnknown()) return;
                    String totalTime = formatTime(totalDuration);
                    timeLabel.setText(currentTime + " / " + totalTime);
                }
            });
    
            mediaPlayer.setOnEndOfMedia(() -> {
                if (currentTrackIndex < playlist.size() - 1) {
                    currentTrackIndex++;
                    playTrack(songLabel, songInfoLabel, progressSlider, albumCover, playPauseButton, timeLabel);
                }
            });
    
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
    
    private void updateSongList(String playlist) {
        if (playlists.containsKey(playlist)) {
            List<Song> songs = playlists.get(playlist);
            ObservableList<String> songItems = FXCollections.observableArrayList();
            for (Song song : songs) {
                songItems.add(song.getFileName() + " [" + song.getSongType() + "]");
            }
            songListView.setItems(songItems);
            songListView.refresh();
        }
    }

    private String formatTime(Duration duration) {
        int minutes = (int) Math.floor(duration.toMinutes());
        int seconds = (int) Math.floor(duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
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