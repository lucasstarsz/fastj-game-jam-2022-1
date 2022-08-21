package tech.fastj.gj.scenes.editor;

import tech.fastj.engine.FastJEngine;
import tech.fastj.gameloop.CoreLoopState;
import tech.fastj.gameloop.event.EventObserver;
import tech.fastj.gj.gameobjects.KeyCircle;
import tech.fastj.gj.rhythm.Conductor;
import tech.fastj.gj.rhythm.ConductorFinishedEvent;
import tech.fastj.gj.rhythm.EditableSongInfo;
import tech.fastj.gj.rhythm.EditorInputMatcher;
import tech.fastj.gj.rhythm.RecordedNote;
import tech.fastj.gj.scenes.game.MainGame;
import tech.fastj.gj.ui.ContentBox;
import tech.fastj.gj.ui.Notice;
import tech.fastj.gj.util.Colors;
import tech.fastj.gj.util.Fonts;
import tech.fastj.gj.util.RhythmUtil;
import tech.fastj.gj.util.SceneNames;
import tech.fastj.gj.util.SpringUtilities;
import tech.fastj.graphics.dialog.DialogConfig;
import tech.fastj.graphics.dialog.DialogMessageTypes;
import tech.fastj.graphics.dialog.DialogOptions;
import tech.fastj.graphics.dialog.DialogUtil;
import tech.fastj.graphics.display.FastJCanvas;
import tech.fastj.graphics.display.SimpleDisplay;
import tech.fastj.input.keyboard.Keys;
import tech.fastj.logging.Log;
import tech.fastj.math.Pointf;
import tech.fastj.systems.control.Scene;
import tech.fastj.systems.control.SceneManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;

public class SongEditor extends Scene implements EventObserver<ConductorFinishedEvent> {

    private EditorState editorState;
    private EditableSongInfo songInfo;
    private Conductor conductor;
    private EditorInputMatcher inputMatcher;

    private ContentBox songNameBox;
    private ContentBox beatBox;
    private final List<KeyCircle> keyCircles;

    private static final EditableSongInfo MainMenuSwitch = new EditableSongInfo();

    public SongEditor() {
        super(SceneNames.SongEditor);
        keyCircles = new ArrayList<>();
    }

    @Override
    public void load(FastJCanvas canvas) {
        Log.debug(SongEditor.class, "loading {}", getSceneName());

        createUI();
        changeState(EditorState.Setup);

        Log.debug(SongEditor.class, "loaded {}", getSceneName());
    }

    private void createUI() {
        songNameBox = new ContentBox(this, "Now Recording", "???");
        songNameBox.setTranslation(new Pointf(30f));
        songNameBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
        songNameBox.getStatDisplay().setFill(Colors.Snowy);

        beatBox = new ContentBox(this, "Beat", "???");
        beatBox.setTranslation(new Pointf(30f, 50f));
        beatBox.getStatDisplay().setFont(Fonts.MonoStatTextFont);
        beatBox.getStatDisplay().setFill(Colors.Snowy);
    }

    private void createListeners() {
        inputMatcher.setOnLaneKeyPressed((event, beat) -> {
            for (KeyCircle keyCircle : keyCircles) {
                if (keyCircle.getKey() == event.getKey()) {
                    keyCircle.setFill(Color.white, false);

                    if (beat != -1) {
                        Notice notice = new Notice("'" + event.getKey()
                            .name() + "' key at beat " + beat, new Pointf(100f, 50f), this);
                        notice.setFill(Color.black);
                        notice.setFont(Fonts.StatTextFont);
                        drawableManager().addGameObject(notice);
                    }
                    return;
                }
            }
        });
    }

    private void resetConductor(FastJCanvas canvas) {
        conductor = RhythmUtil.createConductor(this, songInfo, canvas);
        inputMatcher = new EditorInputMatcher(conductor, songInfo);
    }

    @Override
    public void unload(FastJCanvas canvas) {
        Log.debug(MainGame.class, "unloading {}", getSceneName());
        editorState = null;

        keyCircles.clear();

        setInitialized(false);
        Log.debug(MainGame.class, "unloaded {}", getSceneName());
    }

    @Override
    public void update(FastJCanvas canvas) {
        if (editorState == EditorState.Recording || editorState == EditorState.Review) {
            double inputBeatPosition = conductor.songPositionInBeats;
            beatBox.setContent("" + RhythmUtil.adjustBeatPosition(inputBeatPosition));
        }
    }

    public void changeState(EditorState next) {
        Log.info(SongEditor.class, "changing state from {} to {}", editorState, next);

        switch (next) {
            case Setup -> {
                if (conductor != null) {
                    conductor.setPaused(true);
                }

                songNameBox.setShouldRender(false);
                beatBox.setShouldRender(false);

                SwingUtilities.invokeLater(() -> {
                    EditableSongInfo editableSongInfo;

                    do {
                        editableSongInfo = setupSongInfo();

                        if (editableSongInfo == null && !FastJEngine.<SceneManager>getLogicManager().getCurrentScene().getSceneName()
                            .equals(this.getSceneName())) {
                            return;
                        } else if (editableSongInfo != null && editableSongInfo.equals(MainMenuSwitch)) {
                            return;
                        }
                    } while (editableSongInfo == null);

                    songInfo = editableSongInfo;

                    if (editorState == EditorState.Results) {
                        return;
                    }

                    FastJEngine.runLater(() -> changeState(EditorState.Recording), CoreLoopState.Update);
                });
            }
            case Recording -> {
                resetConductor(FastJEngine.getCanvas());
                createListeners();

                songNameBox.setName("Now Recording");
                songNameBox.setContent(songInfo.songName);
                songNameBox.setShouldRender(true);
                beatBox.setShouldRender(true);

                RhythmUtil.createLaneKeys(this, conductor, keyCircles);
                inputManager().addKeyboardActionListener(inputMatcher);
                songInfo.resetNextIndex();

                FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);
            }
            case Review -> {
                resetConductor(FastJEngine.getCanvas());
                createListeners();

                songNameBox.setName("Now Reviewing");
                songNameBox.setShouldRender(true);
                beatBox.setShouldRender(true);

                RhythmUtil.createLaneKeys(this, conductor, keyCircles);
                inputManager().addKeyboardActionListener(inputMatcher);
                songInfo.resetNextIndex();

                FastJEngine.getGameLoop().addEventObserver(this, ConductorFinishedEvent.class);
            }
            case Results -> {
                if (editorState == EditorState.Recording || editorState == EditorState.Review) {
                    if (editorState == EditorState.Recording) {
                        inputManager().removeKeyboardActionListener(inputMatcher);
                    }

                    for (KeyCircle keyCircle : keyCircles) {
                        keyCircle.destroy(this);
                    }
                    keyCircles.clear();

                    songNameBox.setShouldRender(false);
                    beatBox.setShouldRender(false);

                    conductor.setPaused(true);
                    conductor.destroy(this);

                    FastJEngine.getGameLoop().removeEventObserver(this, ConductorFinishedEvent.class);
                }

                if (editorState == EditorState.Recording) {
                    SwingUtilities.invokeLater(() -> {
                        List<RecordedNote> recordedNotes = inputMatcher.getRecordedNotes();
                        double[] notes = recordedNotes.stream().mapToDouble(RecordedNote::note).toArray();
                        int[] noteLanes = recordedNotes.stream().mapToInt(RecordedNote::noteLane).toArray();

                        editRecordedNotes(notes, noteLanes);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> editRecordedNotes(songInfo.notes, songInfo.noteLanes));
                }
            }
        }

        editorState = next;
    }

    private void editRecordedNotes(double[] notes, int[] noteLanes) {
        JPanel recordingDataPanel = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        recordingDataPanel.setLayout(springLayout);

        LabelCombo infoLabelCombo = setupDoubleLabelCombo(recordingDataPanel, "Note Beat", "Note Lane");
        List<FieldCombo> doubleInputCombos = new ArrayList<>();
        for (int i = 0; i < notes.length; i++) {
            doubleInputCombos.add(setupDoubleInputCombo(recordingDataPanel, notes[i], noteLanes[i]));
        }

        SpringUtilities.makeCompactGrid(recordingDataPanel, notes.length + 1, 2, 5, 5, 5, 5);

        JLabel resultsText = new JLabel("Recording was successful. Edit note beats and lanes below as needed.");
        JScrollPane scrollableDataPanel = new JScrollPane(recordingDataPanel);
        scrollableDataPanel.setPreferredSize(new Dimension(
            infoLabelCombo.left().getWidth() + infoLabelCombo.right().getWidth(),
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
            String[] resultOptions = {"Export", "Review Playback", "Reset Notes", "Exit"};

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
                        notes[i] = Double.parseDouble(doubleInputCombos.get(i).left().getText());
                        noteLanes[i] = Integer.parseInt(doubleInputCombos.get(i).right().getText());
                    }
                    songInfo.notes = notes;
                    songInfo.noteLanes = noteLanes;
                    songInfo.resetNextIndex();

                    Gson gson = new Gson();
                    String songInfoJson = gson.toJson(songInfo);

                    String path = browseForPath("Choose a location to save your song file.", FileDialog.SAVE,
                        songInfo.musicPath.substring(Math.max(0, songInfo.musicPath.lastIndexOf(File.separator)), songInfo.musicPath.lastIndexOf(".")),
                        (directory, file) -> {
                            if (file == null) {
                                return null;
                            }

                            return directory + file + (file.endsWith(".json") ? "" : ".json");
                        }
                    );

                    if (path != null) {
                        try {
                            path = path.replace('\\', '/');
                            Files.writeString(Path.of(path), songInfoJson, StandardCharsets.UTF_8);
                            DialogUtil.showMessageDialog(
                                DialogConfig.create()
                                    .withTitle("Song info successfully saved")
                                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                    .withPrompt("Your song was successfully saved at: \"" + path + "\".")
                                    .build()
                            );
                        } catch (IOException exception) {
                            displayException("Failed to save song info", exception);
                        }
                    }
                }
                case 1 -> {
                    for (int i = 0; i < doubleInputCombos.size(); i++) {
                        notes[i] = Double.parseDouble(doubleInputCombos.get(i).left().getText());
                        noteLanes[i] = Integer.parseInt(doubleInputCombos.get(i).right().getText());
                    }
                    songInfo.notes = notes;
                    songInfo.noteLanes = noteLanes;
                    FastJEngine.runLater(() -> changeState(EditorState.Review), CoreLoopState.Update);
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
                        FastJEngine.runLater(() -> changeState(EditorState.Setup), CoreLoopState.Update);
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
                        FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager()
                            .switchScenes(SceneNames.MainMenu), CoreLoopState.Update);
                        return;
                    }
                }
            }
        }
    }

    private EditableSongInfo setupSongInfo() {
        JPanel songConfigPanel = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        songConfigPanel.setLayout(springLayout);
        AtomicBoolean hasJsonFile = new AtomicBoolean(false);
        AtomicReference<EditableSongInfo> editableSongInfoRef = new AtomicReference<>();

        LabeledField songNameCombo = setupInputCombo(songConfigPanel, "Song Name:");
        LabeledField bpmCombo = setupInputCombo(songConfigPanel, "BPM:");
        LabeledField beatPeekCombo = setupInputCombo(songConfigPanel, "Beats shown in advance:");
        LabeledField beatOffsetCombo = setupInputCombo(songConfigPanel, "Song Beat Offset:");
        LabeledField laneKeysCombo = setupInputCombo(songConfigPanel, "Lane Keys:");
        SpringUtilities.makeCompactGrid(songConfigPanel, 5, 2, 5, 5, 5, 5);

        JLabel musicPathLabel = new JLabel("Music Location:", JLabel.TRAILING);
        JTextField musicPathInput = new JTextField("", 25);
        JButton findMusicPathButton = new JButton("Browse...");
        findMusicPathButton.setActionCommand("browse");
        findMusicPathButton.addActionListener(event -> {
            if ("browse".equals(event.getActionCommand())) {
                String[] fileTypes = new String[] {".wav", ".ogg", ".mp3", ".json"};
                String path = browseForPath("Choose a song to load.", FileDialog.LOAD, null,
                    (directory, file) -> {
                        if (file == null) {
                            return null;
                        }

                        boolean fileTypeMatch = false;

                        for (String otherFileType : fileTypes) {
                            if (file.endsWith(otherFileType)) {
                                fileTypeMatch = true;
                                break;
                            }
                        }

                        if (!fileTypeMatch) {
                            DialogUtil.showMessageDialog(
                                DialogConfig.create()
                                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                    .withTitle("Invalid file format")
                                    .withPrompt("Song must be of .wav, .ogg, .mp3, or .json format.")
                                    .build()
                            );
                            directory = "";
                            file = "";
                        }

                        return directory + file;
                    }
                );

                System.out.println("result: " + path);
                if (path != null) {
                    if (path.endsWith(".json")) {
                        boolean fillJsonData = DialogUtil.showConfirmationDialog(
                            DialogConfig.create()
                                .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                                .withTitle("JSON file detected.")
                                .withPrompt("Detected JSON file. Load JSON data?")
                                .build(),
                            DialogOptions.YesNo
                        );

                        if (fillJsonData) {
                            try {
                                Gson gson = new Gson();
                                String songInfoJson = Files.readString(Path.of(path));

                                EditableSongInfo editableSongInfo = gson.fromJson(songInfoJson, EditableSongInfo.class);
                                editableSongInfoRef.set(editableSongInfo);
                                hasJsonFile.set(true);

                                fillUIWithJson(songNameCombo, bpmCombo, beatPeekCombo, beatOffsetCombo, laneKeysCombo, musicPathInput, editableSongInfo);
                            } catch (IOException exception) {
                                displayException("Error while trying to load JSON file at \"" + path + "\"", exception);
                            }
                        }
                    } else {
                        musicPathInput.setText(path);
                    }
                }
            }
        });

        if (songInfo != null) {
            editableSongInfoRef.set(songInfo);
            fillUIWithJson(songNameCombo, bpmCombo, beatPeekCombo, beatOffsetCombo, laneKeysCombo, musicPathInput, songInfo);
        }

        musicPathLabel.setLabelFor(musicPathInput);
        songConfigPanel.add(musicPathLabel);
        songConfigPanel.add(musicPathInput);
        songConfigPanel.add(findMusicPathButton);

        constrainMusicPathUI(songConfigPanel, springLayout, laneKeysCombo, musicPathLabel, musicPathInput, findMusicPathButton);

        DecimalVerifier decimalVerifier = new DecimalVerifier();
        IntegerVerifier integerVerifier = new IntegerVerifier();

        bpmCombo.right().setInputVerifier(decimalVerifier);
        beatOffsetCombo.right().setInputVerifier(decimalVerifier);
        beatPeekCombo.right().setInputVerifier(integerVerifier);

        while (true) {
            boolean confirmation = DialogUtil.showConfirmationDialog(
                DialogConfig.create()
                    .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                    .withTitle("Song Configuration")
                    .withPrompt(songConfigPanel)
                    .build(),
                DialogOptions.OkCancel
            );

            if (!confirmation) {
                boolean confirmReturn = DialogUtil.showConfirmationDialog(
                    DialogConfig.create()
                        .withTitle("Exit Song Editor")
                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                        .withPrompt("Return to main menu? Any unsaved work will be lost.")
                        .build(),
                    DialogOptions.YesNoCancel
                );

                if (confirmReturn) {
                    FastJEngine.runLater(() -> FastJEngine.<SceneManager>getLogicManager()
                        .switchScenes(SceneNames.MainMenu), CoreLoopState.Update);
                    return MainMenuSwitch;
                } else {
                    continue;
                }
            }

            if (hasJsonFile.get()) {
                String[] options = {"Record New Notes", "Skip to Editing"};
                int skipRecord = DialogUtil.showOptionDialog(
                    DialogConfig.create()
                        .withParentComponent(FastJEngine.<SimpleDisplay>getDisplay().getWindow())
                        .withTitle("Detected JSON file.")
                        .withPrompt("JSON file was detected. Would you like to skip to the editing process?")
                        .build(),
                    DialogMessageTypes.Question,
                    options,
                    "Skip to Editing"
                );

                if (skipRecord == 1) {
                    changeState(EditorState.Results);
                    return editableSongInfoRef.get();
                } else if (skipRecord == -1) {
                    continue;
                }
            }

            String songName = songNameCombo.right().getText().strip();

            double bpm;
            try {
                bpm = Double.parseDouble(bpmCombo.right().getText().strip());
            } catch (NumberFormatException exception) {
                DialogUtil.showMessageDialog(DialogConfig.create().withPrompt("Couldn't parse BPM: " + exception.getMessage()).build());
                return null;
            }

            int beatPeekCount;
            try {
                beatPeekCount = Integer.parseInt(beatPeekCombo.right().getText().strip());
            } catch (NumberFormatException exception) {
                DialogUtil.showMessageDialog(DialogConfig.create()
                    .withPrompt("Couldn't parse beats shown in advance: " + exception.getMessage()).build());
                return null;
            }

            double songBeatOffset;
            try {
                songBeatOffset = Double.parseDouble(beatOffsetCombo.right().getText().strip());
            } catch (NumberFormatException exception) {
                DialogUtil.showMessageDialog(DialogConfig.create().withPrompt("Couldn't parse song beat offset: " + exception.getMessage())
                    .build());
                return null;
            }

            String laneKeysUnformatted = laneKeysCombo.right().getText().strip();
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
        }
    }

    private void fillUIWithJson(LabeledField songNameCombo, LabeledField bpmCombo, LabeledField beatPeekCombo, LabeledField beatOffsetCombo,
                                LabeledField laneKeysCombo, JTextField musicPathInput, EditableSongInfo songInfo) {
        songNameCombo.right().setText(songInfo.songName);
        bpmCombo.right().setText("" + songInfo.bpm);
        beatPeekCombo.right().setText("" + songInfo.beatPeekCount);
        beatOffsetCombo.right().setText("" + songInfo.firstBeatOffset);
        musicPathInput.setText(songInfo.musicPath);

        String laneKeysString = songInfo.getLaneKeys().stream()
            .map(Keys::name)
            .collect(Collectors.joining(","));
        laneKeysCombo.right().setText(laneKeysString);
    }

    private String browseForPath(String title, int fileDialogType, String initialName, BiFunction<String, String, String> fileAction) {
        String file;

        do {
            FileDialog songDialog = new FileDialog(FastJEngine.<SimpleDisplay>getDisplay().getWindow(), title, fileDialogType);

            if (fileDialogType == FileDialog.LOAD) {
                songDialog.setFilenameFilter((dir, name) -> name.endsWith(".wav")
                    || name.endsWith(".json")
                    || name.endsWith(".ogg")
                    || name.endsWith(".mp3")
                );
            }

            songDialog.setDirectory(System.getProperty("user.home"));
            songDialog.setMultipleMode(false);
            songDialog.setFile(initialName);
            songDialog.setVisible(true);

            file = fileAction.apply(songDialog.getDirectory(), songDialog.getFile());

            if (file == null) {
                return null;
            }
        } while (file.isBlank());

        return file;
    }

    private void constrainMusicPathUI(JPanel panel, SpringLayout layout, LabeledField laneKeysCombo, JLabel label, JTextField input, JButton button) {
        layout.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.NORTH, label, 5, SpringLayout.SOUTH, laneKeysCombo.left());
        layout.putConstraint(SpringLayout.EAST, label, 0, SpringLayout.EAST, laneKeysCombo.left());

        layout.putConstraint(SpringLayout.WEST, input, 5, SpringLayout.EAST, label);
        layout.putConstraint(SpringLayout.NORTH, input, 5, SpringLayout.SOUTH, laneKeysCombo.right());
        layout.putConstraint(SpringLayout.EAST, input, 0, SpringLayout.EAST, laneKeysCombo.right());

        layout.putConstraint(SpringLayout.WEST, button, 5, SpringLayout.EAST, input);
        layout.putConstraint(SpringLayout.NORTH, button, 0, SpringLayout.NORTH, input);

        layout.putConstraint(SpringLayout.SOUTH, panel, 5, SpringLayout.SOUTH, label);
        layout.putConstraint(SpringLayout.SOUTH, panel, 5, SpringLayout.SOUTH, input);
        layout.putConstraint(SpringLayout.EAST, panel, 5, SpringLayout.EAST, button);
    }

    private LabeledField setupInputCombo(JPanel songConfigPanel, String labelText) {
        JLabel label = new JLabel(labelText, javax.swing.SwingConstants.TRAILING);
        JTextField input = new JTextField("", 25);
        label.setLabelFor(input);
        songConfigPanel.add(label);
        songConfigPanel.add(input);

        return new LabeledField(label, input);
    }

    private LabelCombo setupDoubleLabelCombo(JPanel songConfigPanel, String labelText1, String labelText2) {
        JLabel label1 = new JLabel(labelText1, SwingConstants.CENTER);
        JLabel label2 = new JLabel(labelText2, javax.swing.SwingConstants.CENTER);
        songConfigPanel.add(label1);
        songConfigPanel.add(label2);

        return new LabelCombo(label1, label2);
    }

    private FieldCombo setupDoubleInputCombo(JPanel songConfigPanel, double note, int noteLane) {
        JTextField noteField = new JTextField("" + note, 3);
        JTextField laneField = new JTextField("" + noteLane, 3);
        songConfigPanel.add(noteField);
        songConfigPanel.add(laneField);

        return new FieldCombo(noteField, laneField);
    }

    @Override
    public void eventReceived(ConductorFinishedEvent event) {
        FastJEngine.log("Conductor finished. Processing results...");
        FastJEngine.runLater(() -> changeState(EditorState.Results));
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

    record LabelCombo(JLabel left, JLabel right) {}

    record FieldCombo(JTextField left, JTextField right) {}

    record LabeledField(JLabel left, JTextField right) {}
}
