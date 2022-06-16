package tech.fastj.gj.scenes.editor;

import tech.fastj.engine.FastJEngine;
import tech.fastj.logging.Log;
import tech.fastj.math.Maths;
import tech.fastj.math.Pointf;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogMessageTypes;
import tech.fastj.graphics.dialog.DialogOptions;
import tech.fastj.graphics.dialog.DialogUtil;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.SimpleDisplay;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.input.keyboard.Keys;
import tech.fastj.systems.collections.Pair;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SceneManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.Gson;
import tech.fastj.gameloop.event.GameEventObserver;
import tech.fastj.gj.gameobjects.KeyCircle;
import tech.fastj.gj.gameobjects.MusicNote;
import tech.fastj.gj.rhythm.Conductor;
import tech.fastj.gj.rhythm.ConductorFinishedEvent;
import tech.fastj.gj.rhythm.EditableSongInfo;
import tech.fastj.gj.rhythm.EditorInputMatcher;
import tech.fastj.gj.scenes.game.MainGame;
import tech.fastj.gj.scripts.MusicNoteMovement;
import tech.fastj.gj.ui.ContentBox;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.Shapes;
import tech.fastj.gj.util.SpringUtilities;

public class SongEditor extends Scene implements GameEventObserver<ConductorFinishedEvent> {

    private EditorState editorState;
    private EditableSongInfo songInfo;
    private Conductor conductor;
    private EditorInputMatcher inputMatcher;

    private ContentBox songNameBox;
    private ContentBox beatBox;
    private List<KeyCircle> keyCircles;
    private List<MusicNote> musicNotes;

    public SongEditor() {
        super(SceneNames.SongEditor);
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(SongEditor.class, "loading {}", getSceneName());
        changeState(EditorState.Setup);
        Log.debug(SongEditor.class, "loaded {}", getSceneName());
    }

    @Override
    public void unload(FastJCanvas canvas) {
        Log.debug(MainGame.class, "unloading {}", getSceneName());
        editorState = null;

        if (songNameBox != null) {
            songNameBox.destroy(this);
            songNameBox = null;
        }

        if (beatBox != null) {
            beatBox.destroy(this);
            beatBox = null;
        }

        if (keyCircles != null) {
            for (KeyCircle keyCircle : keyCircles) {
                keyCircle.destroy(this);
            }
            keyCircles.clear();
            keyCircles = null;
        }

        if (musicNotes != null) {
            for (MusicNote musicNote : musicNotes) {
                musicNote.destroy(this);
            }
            musicNotes.clear();
            musicNotes = null;
        }

        setInitialized(false);
        Log.debug(MainGame.class, "unloaded {}", getSceneName());
    }

    @Override
    public void fixedUpdate(FastJCanvas canvas) {
    }

    @Override
    public void update(FastJCanvas canvas) {
        if (editorState == EditorState.Recording) {
            double inputBeatPosition = conductor.songPositionInBeats;
            int cutBeatPosition = (int) inputBeatPosition;

            double adjustedBeatPosition;
            if (inputBeatPosition <= cutBeatPosition + 0.25d) {
                adjustedBeatPosition = Maths.snap((float) inputBeatPosition, cutBeatPosition, (float) (cutBeatPosition + 0.25d));
            } else if (inputBeatPosition <= cutBeatPosition + 0.5d) {
                adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.25d), (float) (cutBeatPosition + 0.5d));
            } else if (inputBeatPosition <= cutBeatPosition + 0.75d) {
                adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.5d), (float) (cutBeatPosition + 0.75d));
            } else {
                adjustedBeatPosition = Maths.snap((float) inputBeatPosition, (float) (cutBeatPosition + 0.75d), cutBeatPosition + 1);
            }

            beatBox.setContent("" + adjustedBeatPosition);
        }
    }

    public void changeState(EditorState next) {
        Log.info(SongEditor.class, "changing state from {} to {}", editorState, next);

        switch (next) {
            case Setup -> {
                if (conductor != null) {
                    conductor.setPaused(true);
                }

                SwingUtilities.invokeLater(() -> {
                    EditableSongInfo editableSongInfo;
                    do {
                        editableSongInfo = setupSongInfo();
                        if (editableSongInfo == null && !FastJEngine.<SceneManager>getLogicManager().getCurrentScene().getSceneName().equals(this.getSceneName())) {
                            return;
                        }
                    } while (editableSongInfo == null);

                    songInfo = editableSongInfo;

                    FastJEngine.runAfterRender(() -> changeState(EditorState.Recording));
                });
            }
            case Recording -> {
                conductor = new Conductor(songInfo, this, true);
                FastJCanvas canvas = FastJEngine.getCanvas();

                songNameBox = new ContentBox(this, "Now Playing", "" + conductor.musicInfo.getSongName());
                songNameBox.setTranslation(new Pointf(30f));
                songNameBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                drawableManager.addUIElement(songNameBox);

                beatBox = new ContentBox(this, "Beat", "" + conductor.songPositionInBeats);
                beatBox.setTranslation(new Pointf(30f, 50f));
                beatBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                drawableManager.addUIElement(beatBox);

                Collection<Keys> laneKeys = conductor.musicInfo.getLaneKeys();
                int laneKeyIncrement = 1;
                keyCircles = new ArrayList<>();
                for (Keys laneKey : laneKeys) {
                    Pointf laneStartingLocation = new Pointf((canvas.getCanvasCenter().x) + (laneKeyIncrement * Shapes.NoteSize * 2.5f), canvas.getResolution().y - (Shapes.NoteSize * 4f));
                    KeyCircle keyCircle = (KeyCircle) new KeyCircle(laneKey, Shapes.NoteSize, "Tahoma")
                            .setFill(Color.yellow)
                            .setOutline(KeyCircle.DefaultOutlineStroke, KeyCircle.DefaultOutlineColor)
                            .setTranslation(laneStartingLocation);
                    keyCircles.add(keyCircle);
                    drawableManager.addGameObject(keyCircle);
                    laneKeyIncrement++;
                }

                drawableManager.addGameObject(conductor);

                inputMatcher = new EditorInputMatcher(conductor, songInfo);
                inputManager.addKeyboardActionListener(inputMatcher);
                songInfo.resetNextIndex();
                FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);
            }
            case Review -> {
                conductor = new Conductor(songInfo, this, true);
                FastJCanvas canvas = FastJEngine.getCanvas();

                musicNotes = new ArrayList<>();
                conductor.setSpawnMusicNote((note, noteLane) -> {
                    FastJEngine.log("spawn music note");
                    Pointf noteStartingLocation = new Pointf((canvas.getCanvasCenter().x) + (noteLane * Shapes.NoteSize * 2.5f), -Shapes.NoteSize / 2f);
                    MusicNote musicNote = new MusicNote(noteStartingLocation, Shapes.NoteSize)
                            .setFill(DrawUtil.randomColor())
                            .setOutline(MusicNote.DefaultOutlineStroke, DrawUtil.randomColor());

                    double noteTravelDistance = canvas.getResolution().y - (Shapes.NoteSize * 4f);
                    MusicNoteMovement musicNoteMovement = new MusicNoteMovement(conductor, note, noteTravelDistance);
                    musicNote.addLateBehavior(musicNoteMovement, this);
                    musicNotes.add(musicNote);
                    drawableManager.addGameObject(musicNote);
                });

                songNameBox = new ContentBox(this, "Now Playing", "" + conductor.musicInfo.getSongName());
                songNameBox.setTranslation(new Pointf(30f));
                songNameBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                drawableManager.addUIElement(songNameBox);

                beatBox = new ContentBox(this, "Beat", "" + conductor.songPositionInBeats);
                beatBox.setTranslation(new Pointf(30f, 50f));
                beatBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
                drawableManager.addUIElement(beatBox);

                Collection<Keys> laneKeys = conductor.musicInfo.getLaneKeys();
                int laneKeyIncrement = 1;
                keyCircles = new ArrayList<>();
                for (Keys laneKey : laneKeys) {
                    Pointf laneStartingLocation = new Pointf((canvas.getCanvasCenter().x) + (laneKeyIncrement * Shapes.NoteSize * 2.5f), canvas.getResolution().y - (Shapes.NoteSize * 4f));
                    KeyCircle keyCircle = (KeyCircle) new KeyCircle(laneKey, Shapes.NoteSize, "Tahoma")
                            .setFill(Color.yellow)
                            .setOutline(KeyCircle.DefaultOutlineStroke, KeyCircle.DefaultOutlineColor)
                            .setTranslation(laneStartingLocation);
                    keyCircles.add(keyCircle);
                    drawableManager.addGameObject(keyCircle);
                    laneKeyIncrement++;
                }

                drawableManager.addGameObject(conductor);
                songInfo.resetNextIndex();
                FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);
            }
            case Results -> {
                if (editorState == EditorState.Recording) {
                    inputManager.removeKeyboardActionListener(inputMatcher);
                }
                if (editorState == EditorState.Recording || editorState == EditorState.Review) {
                    if (keyCircles != null) {
                        for (KeyCircle keyCircle : keyCircles) {
                            keyCircle.destroy(this);
                        }
                        keyCircles.clear();
                        keyCircles = null;
                    }

                    if (musicNotes != null) {
                        for (MusicNote musicNote : musicNotes) {
                            musicNote.destroy(this);
                        }
                        musicNotes.clear();
                        musicNotes = null;
                    }

                    if (songNameBox != null) {
                        songNameBox.destroy(this);
                        songNameBox = null;
                    }

                    if (beatBox != null) {
                        beatBox.destroy(this);
                        beatBox = null;
                    }

                    drawableManager.removeGameObject(conductor);
                    conductor.setPaused(true);
                    conductor.destroy(this);
                    conductor = null;

                    FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class);
                }

                SwingUtilities.invokeLater(() -> {
                    List<Pair<Double, Integer>> recordedNotes = inputMatcher.getRecordedNotes();
                    double[] notes = recordedNotes.stream().mapToDouble(Pair::getLeft).toArray();
                    int[] noteLanes = recordedNotes.stream().mapToInt(Pair::getRight).toArray();

                    JPanel recordingDataPanel = new JPanel();
                    SpringLayout springLayout = new SpringLayout();
                    recordingDataPanel.setLayout(springLayout);

                    Pair<JLabel, JLabel> infoLabelCombo = setupDoubleLabelCombo(recordingDataPanel, "Note Beat", "Note Lane");
                    List<Pair<JTextField, JTextField>> doubleInputCombos = new ArrayList<>();
                    for (int i = 0; i < notes.length; i++) {
                        doubleInputCombos.add(setupDoubleInputCombo(recordingDataPanel, notes[i], noteLanes[i]));
                    }

                    SpringUtilities.makeCompactGrid(recordingDataPanel, notes.length + 1, 2, 5, 5, 5, 5);

                    JLabel resultsText = new JLabel("Recording was successful. Edit note beats and lanes below as needed.");
                    JScrollPane scrollableDataPanel = new JScrollPane(recordingDataPanel);
                    scrollableDataPanel.setPreferredSize(new Dimension(
                            infoLabelCombo.getLeft().getWidth() + infoLabelCombo.getRight().getWidth(),
                            200
                    ));

                    JPanel resultsPanel = new JPanel();
                    resultsPanel.add(resultsText);
                    resultsPanel.add(scrollableDataPanel);

                    BorderLayout borderLayout = new BorderLayout();
                    resultsPanel.setLayout(borderLayout);
                    borderLayout.addLayoutComponent(resultsText, BorderLayout.PAGE_START);
                    borderLayout.addLayoutComponent(scrollableDataPanel, BorderLayout.PAGE_END);

                    while (true) {
                        String[] resultOptions = {"Export", "Review Playback", "Retry", "Cancel"};

                        int resultChoice = DialogUtil.showOptionDialog(
                                DialogConfig.create()
                                        .withTitle("Recording Successful")
                                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                        .withPrompt(resultsPanel)
                                        .build(),
                                DialogMessageTypes.Question,
                                resultOptions,
                                resultOptions[0]
                        );

                        switch (resultChoice) {
                            case 0 -> {
                                for (int i = 0; i < doubleInputCombos.size(); i++) {
                                    notes[i] = Double.parseDouble(doubleInputCombos.get(i).getLeft().getText());
                                    noteLanes[i] = Integer.parseInt(doubleInputCombos.get(i).getRight().getText());
                                }
                                songInfo.notes = notes;
                                songInfo.noteLanes = noteLanes;

                                Gson gson = new Gson();
                                String songInfoJson = gson.toJson(songInfo);

                                String path = browseForPath(
                                        "Choose a location to save your song info file.",
                                        FileDialog.SAVE,
                                        songInfo.musicPath.substring(Math.max(0, songInfo.musicPath.lastIndexOf(File.separator)))
                                );

                                if (path != null) {
                                    try {
                                        Files.writeString(Path.of(path), songInfoJson, StandardCharsets.UTF_8);
                                        DialogUtil.showMessageDialog(
                                                DialogConfig.create()
                                                        .withTitle("Song info successfully saved")
                                                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                                        .withPrompt("Your song was successfully saved.")
                                                        .build()
                                        );

                                    } catch (IOException exception) {
                                        displayException("Failed to save song info", exception);
                                    }
                                }
                            }
                            case 1 -> {
                                for (int i = 0; i < doubleInputCombos.size(); i++) {
                                    notes[i] = Double.parseDouble(doubleInputCombos.get(i).getLeft().getText());
                                    noteLanes[i] = Integer.parseInt(doubleInputCombos.get(i).getRight().getText());
                                }
                                songInfo.notes = notes;
                                songInfo.noteLanes = noteLanes;
                                FastJEngine.runAfterRender(() -> changeState(EditorState.Review));
                                return;
                            }
                            case 2 -> {
                                boolean confirmRetry = DialogUtil.showConfirmationDialog(
                                        DialogConfig.create()
                                                .withTitle("Retry notes for better accuracy?")
                                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                                .withPrompt("Retrying clears your previous notes. Are you sure you want to retry?")
                                                .build(),
                                        DialogOptions.YesNoCancel
                                );

                                if (confirmRetry) {
                                    FastJEngine.runAfterRender(() -> changeState(EditorState.Setup));
                                    return;
                                }
                            }
                            default -> {
                                boolean confirmReturn = DialogUtil.showConfirmationDialog(
                                        DialogConfig.create()
                                                .withTitle("Exit Song Editor")
                                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                                .withPrompt("Return to main menu? Any unsaved work will be lost.")
                                                .build(),
                                        DialogOptions.YesNoCancel
                                );

                                if (confirmReturn) {
                                    FastJEngine.runAfterRender(() -> FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu));
                                    return;
                                }
                            }
                        }
                    }
                });
            }
        }

        editorState = next;
    }

    private EditableSongInfo setupSongInfo() {
        JPanel songConfigPanel = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        songConfigPanel.setLayout(springLayout);

        Pair<JLabel, JTextField> songNameCombo = setupInputCombo(songConfigPanel, "Song Name:");
        Pair<JLabel, JTextField> bpmCombo = setupInputCombo(songConfigPanel, "BPM:");
        Pair<JLabel, JTextField> beatPeekCombo = setupInputCombo(songConfigPanel, "Beats shown in advance:");
        Pair<JLabel, JTextField> beatOffsetCombo = setupInputCombo(songConfigPanel, "Song Beat Offset:");
        Pair<JLabel, JTextField> laneKeysCombo = setupInputCombo(songConfigPanel, "Lane Keys:");
        SpringUtilities.makeCompactGrid(songConfigPanel, 5, 2, 5, 5, 5, 5);

        JLabel musicPathLabel = new JLabel("Music Location:", JLabel.TRAILING);
        JTextField musicPathInput = new JTextField("", 25);
        JButton findMusicPathButton = new JButton("Browse...");
        findMusicPathButton.setActionCommand("browse");
        findMusicPathButton.addActionListener(event -> {
            if ("browse".equals(event.getActionCommand())) {
                String path = browseForPath(
                        "Choose a song file to load.",
                        FileDialog.LOAD,
                        (dir, name) -> name.endsWith(".wav"),
                        ".wav",
                        "Song must be of WAV (.wav) format."
                );

                if (path != null) {
                    musicPathInput.setText(path);
                }
            }
        });

        musicPathLabel.setLabelFor(musicPathInput);
        songConfigPanel.add(musicPathLabel);
        songConfigPanel.add(musicPathInput);
        songConfigPanel.add(findMusicPathButton);

        constrainMusicPathUI(songConfigPanel, springLayout, laneKeysCombo, musicPathLabel, musicPathInput, findMusicPathButton);

        DecimalVerifier decimalVerifier = new DecimalVerifier();
        IntegerVerifier integerVerifier = new IntegerVerifier();

        bpmCombo.getRight().setInputVerifier(decimalVerifier);
        beatOffsetCombo.getRight().setInputVerifier(decimalVerifier);
        beatPeekCombo.getRight().setInputVerifier(integerVerifier);

        boolean confirmation = DialogUtil.showConfirmationDialog(
                DialogConfig.create()
                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                        .withTitle("Song Configuration")
                        .withPrompt(songConfigPanel)
                        .build(),
                DialogOptions.OkCancel
        );

        if (confirmation) {
            String songName = songNameCombo.getRight().getText().strip();

            double bpm;
            try {
                bpm = Double.parseDouble(bpmCombo.getRight().getText().strip());
            } catch (NumberFormatException exception) {
                DialogUtil.showMessageDialog(DialogConfig.create().withPrompt("Couldn't parse BPM: " + exception.getMessage()).build());
                return null;
            }

            int beatPeekCount;
            try {
                beatPeekCount = Integer.parseInt(beatPeekCombo.getRight().getText().strip());
            } catch (NumberFormatException exception) {
                DialogUtil.showMessageDialog(DialogConfig.create().withPrompt("Couldn't parse beats shown in advance: " + exception.getMessage()).build());
                return null;
            }

            double songBeatOffset;
            try {
                songBeatOffset = Double.parseDouble(beatOffsetCombo.getRight().getText().strip());
            } catch (NumberFormatException exception) {
                DialogUtil.showMessageDialog(DialogConfig.create().withPrompt("Couldn't parse song beat offset: " + exception.getMessage()).build());
                return null;
            }

            String laneKeysUnformatted = laneKeysCombo.getRight().getText().strip();
            TreeMap<Integer, Keys> laneKeys = new TreeMap<>();

            try {
                String[] laneKeysSplit = laneKeysUnformatted.split(",");
                for (int i = 0; i < laneKeysSplit.length; i++) {
                    Keys key = Keys.valueOf(laneKeysSplit[i].strip());
                    laneKeys.put(i + 1, key);
                }
            } catch (Exception exception) {
                DialogUtil.showMessageDialog(DialogConfig.create().withPrompt("Couldn't parse lane keys.").build());
                return null;
            }

            String musicPath = musicPathInput.getText();
            return new EditableSongInfo(songName, bpm, beatPeekCount, songBeatOffset, laneKeys, musicPath);
        } else {
            boolean returnToMainMenu = DialogUtil.showConfirmationDialog(
                    DialogConfig.create()
                            .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                            .withPrompt("Song info editing was cancelled. Exit?")
                            .build(),
                    DialogOptions.YesNo
            );

            if (returnToMainMenu) {
                FastJEngine.<SceneManager>getLogicManager().switchScenes(SceneNames.MainMenu);
            }
            return null;
        }
    }

    private String browseForPath(String title, int fileDialogType, FilenameFilter filter, String fileType, String invalidFormatMessage) {
        String file = null;
        String directory = null;

        while (file == null) {
            FileDialog songLoaderDialog = new FileDialog(FastJEngine.<SimpleDisplay>getDisplay().getWindow(), title, fileDialogType);
            songLoaderDialog.setDirectory(System.getProperty("user.home"));
            songLoaderDialog.setFilenameFilter(filter);
            songLoaderDialog.setFile("*" + fileType);
            songLoaderDialog.setMultipleMode(false);
            songLoaderDialog.setVisible(true);
            file = songLoaderDialog.getFile();
            directory = songLoaderDialog.getDirectory();

            if (file == null) {
                return null;
            }

            if (!file.endsWith(fileType)) {
                DialogUtil.showMessageDialog(
                        DialogConfig.create()
                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                .withTitle("Invalid file format")
                                .withPrompt(invalidFormatMessage)
                                .build()
                );
                file = null;
            }
        }

        return directory + file;
    }

    private String browseForPath(String title, int fileDialogType, String initialName) {
        String file = null;
        String directory = null;

        while (file == null) {
            FileDialog songSaverDialog = new FileDialog(FastJEngine.<SimpleDisplay>getDisplay().getWindow(), title, fileDialogType);
            songSaverDialog.setDirectory(System.getProperty("user.home"));
            songSaverDialog.setMultipleMode(false);
            songSaverDialog.setFile(initialName + ".json");
            songSaverDialog.setVisible(true);
            directory = songSaverDialog.getDirectory();
            file = songSaverDialog.getFile();

            if (file == null) {
                return null;
            }
        }

        return directory + file;
    }

    private void constrainMusicPathUI(JPanel panel, SpringLayout layout, Pair<JLabel, JTextField> laneKeysCombo, JLabel label, JTextField input, JButton button) {
        layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.SOUTH, laneKeysCombo.getLeft());
        layout.putConstraint(SpringLayout.EAST, label, 0, SpringLayout.EAST, laneKeysCombo.getLeft());

        layout.putConstraint(SpringLayout.WEST, input, 5, SpringLayout.EAST, label);
        layout.putConstraint(SpringLayout.NORTH, input, 5, SpringLayout.SOUTH, laneKeysCombo.getRight());
        layout.putConstraint(SpringLayout.EAST, input, 0, SpringLayout.EAST, laneKeysCombo.getRight());

        layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.EAST, input);
        layout.putConstraint(SpringLayout.NORTH, button, -3, SpringLayout.NORTH, input);

        layout.putConstraint(SpringLayout.SOUTH, panel, 5, SpringLayout.SOUTH, label);
        layout.putConstraint(SpringLayout.SOUTH, panel, 5, SpringLayout.SOUTH, input);
        layout.putConstraint(SpringLayout.EAST, panel, 5, SpringLayout.EAST, button);
    }

    private Pair<JLabel, JTextField> setupInputCombo(JPanel songConfigPanel, String labelText) {
        JLabel label = new JLabel(labelText, javax.swing.SwingConstants.TRAILING);
        JTextField input = new JTextField("", 25);
        label.setLabelFor(input);
        songConfigPanel.add(label);
        songConfigPanel.add(input);

        return Pair.of(label, input);
    }

    private Pair<JLabel, JLabel> setupDoubleLabelCombo(JPanel songConfigPanel, String labelText1, String labelText2) {
        JLabel label1 = new JLabel(labelText1, SwingConstants.CENTER);
        JLabel label2 = new JLabel(labelText2, javax.swing.SwingConstants.CENTER);
        songConfigPanel.add(label1);
        songConfigPanel.add(label2);

        return Pair.of(label1, label2);
    }

    private Pair<JTextField, JTextField> setupDoubleInputCombo(JPanel songConfigPanel, double note, int noteLane) {
        JTextField noteField = new JTextField("" + note, 3);
        JTextField laneField = new JTextField("" + noteLane, 3);
        songConfigPanel.add(noteField);
        songConfigPanel.add(laneField);

        return Pair.of(noteField, laneField);
    }

    @Override
    public void eventReceived(ConductorFinishedEvent event) {
        FastJEngine.log("Conductor finished. Processing results...");
        FastJEngine.runAfterUpdate(() -> changeState(EditorState.Results));
    }

    public static void displayException(String message, Exception exception) {
        StringBuilder formattedException = new StringBuilder(exception.getClass().getName() + ": " + exception.getMessage());
        Throwable currentException = exception;
        do {
            formattedException.append(System.lineSeparator())
                    .append("Caused by: ")
                    .append(currentException.getClass().getName())
                    .append(": ")
                    .append(currentException.getMessage())
                    .append(System.lineSeparator())
                    .append(formatStackTrace(currentException));
        } while ((currentException = currentException.getCause()) != null);

        JTextArea textArea = new JTextArea(formattedException.toString());
        textArea.setBackground(new Color(238, 238, 238));
        textArea.setEditable(false);
        textArea.setFont(Fonts.notoSansMono(Font.BOLD, 13));

        DialogUtil.showMessageDialog(
                DialogConfig.create()
                        .withParentComponent(null)
                        .withTitle(exception.getClass().getName() + (message != null ? (": " + message) : ""))
                        .withPrompt(textArea)
                        .build()
        );
    }

    private static String formatStackTrace(Throwable exception) {
        return Arrays.stream(exception.getStackTrace())
                .map(stackTraceElement -> "at " + stackTraceElement.toString() + "\n")
                .toList()
                .toString()
                .replaceFirst("\\[", "")
                .replaceAll("](.*)\\[", "")
                .replaceAll("(, )?at ", "    at ")
                .replace("]", "")
                .trim();
    }
}
