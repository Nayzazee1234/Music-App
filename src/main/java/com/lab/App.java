package com.lab;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class Song {
    protected File file;
    protected MediaPlayer mediaPlayer;

    public Song(File file) {
        this.file = file;
    }

    public void play() {
        stop();
        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
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
    public PopSong(File file) {
        super(file);
    }
    @Override
    public String getSongType() {
        return "Pop Song";
    }
}

class RockSong extends Song {
    public RockSong(File file) {
        super(file);
    }
    @Override
    public String getSongType() {
        return "Rock Song";
    }
}

class JazzSong extends Song {
    public JazzSong(File file) {
        super(file);
    }
    @Override
    public String getSongType() {
        return "Jazz Song";
    }
}

public class App extends Application {
    private Song currentSong;
    private List<Song> playlist = new ArrayList<>();
    private int currentTrackIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        // Controls
        Button openButton = new Button("Open Music Files");
        Button playPauseButton = new Button("Play");
        Button stopButton = new Button("Stop");
        Button nextButton = new Button("Next");
        Button prevButton = new Button("Previous");
        Label songLabel = new Label("No Song Playing");
        Label songInfoLabel = new Label("Song Info");

        // Set CSS styles
        openButton.getStyleClass().add("button");
        playPauseButton.getStyleClass().add("play-pause");
        stopButton.getStyleClass().add("button");
        nextButton.getStyleClass().add("nav-buttons");
        prevButton.getStyleClass().add("nav-buttons");
        songLabel.getStyleClass().add("label");
        songInfoLabel.getStyleClass().add("song-info");

        // Album cover (image placeholder)
        StackPane albumCover = new StackPane();
        albumCover.setPrefSize(200, 200);
        albumCover.getStyleClass().add("album-cover");

        // File chooser and music loading
        openButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac"));
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
            if (files != null && !files.isEmpty()) {
                playlist.clear();
                for (File file : files) {
                    playlist.add(assignSongType(file));
                }
                currentTrackIndex = 0;
                playTrack(songLabel, songInfoLabel);
            }
        });

        // Play or Pause song
        playPauseButton.setOnAction(e -> {
            if (currentSong != null) {
                if (currentSong.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    currentSong.pause();
                    playPauseButton.setText("Play");
                } else {
                    currentSong.play();
                    playPauseButton.setText("Pause");
                }
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
            if (!playlist.isEmpty() && currentTrackIndex < playlist.size() - 1) {
                currentTrackIndex++;
                playTrack(songLabel, songInfoLabel);
            }
        });

        // Previous song
        prevButton.setOnAction(e -> {
            if (!playlist.isEmpty() && currentTrackIndex > 0) {
                currentTrackIndex--;
                playTrack(songLabel, songInfoLabel);
            }
        });

        // Layout
        VBox controlsLayout = new VBox(15, prevButton, playPauseButton, nextButton);
        controlsLayout.getStyleClass().add("controls");

        VBox mainLayout = new VBox(10, songLabel, songInfoLabel, albumCover, controlsLayout, openButton, stopButton);
        mainLayout.setSpacing(20);
        mainLayout.getStyleClass().add("root");

        Scene scene = new Scene(mainLayout, 400, 400);
        scene.getStylesheets().add(getClass().getResource("music_player.css").toExternalForm());

        primaryStage.setTitle("Music Player");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void playTrack(Label songLabel, Label songInfoLabel) {
        if (!playlist.isEmpty()) {
            if (currentSong != null) {
                currentSong.stop();
            }
            currentSong = playlist.get(currentTrackIndex);
            currentSong.play();
            songLabel.setText("Playing: " + currentSong.getFileName());
            songInfoLabel.setText("Song Info: " + currentSong.getSongType());
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
