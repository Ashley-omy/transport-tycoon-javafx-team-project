package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SaveService {
    private static final String SAVE_EXTENSION = ".sav";
    private final Path saveDirectory;

    public SaveService() {
        this(Path.of("saves"));
    }

    public SaveService(Path saveDirectory) {
        if (saveDirectory == null) throw new IllegalArgumentException("saveDirectory cannot be null");
        this.saveDirectory = saveDirectory;
    }

    public void save(Game game, String saveName) throws IOException {
        if (game == null) throw new IllegalArgumentException("game cannot be null");
        String normalizedName = normalizeSaveName(saveName);

        Files.createDirectories(saveDirectory);
        Path saveFile = saveDirectory.resolve(normalizedName + SAVE_EXTENSION);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(saveFile))) {
            out.writeObject(game);
        }
    }

    public Game load(String saveName) throws IOException, ClassNotFoundException {
        String normalizedName = normalizeSaveName(saveName);
        Path saveFile = saveDirectory.resolve(normalizedName + SAVE_EXTENSION);

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(saveFile))) {
            Game game = (Game) in.readObject();
            reconnectLoadedGame(game);
            return game;
        }
    }

    public List<String> listSaves() throws IOException {
        if (!Files.exists(saveDirectory)) {
            return List.of();
        }

        try (var stream = Files.list(saveDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(SAVE_EXTENSION))
                    .map(name -> name.substring(0, name.length() - SAVE_EXTENSION.length()))
                    .sorted()
                    .toList();
        }
    }

    private String normalizeSaveName(String saveName) {
        if (saveName == null || saveName.isBlank()) {
            throw new IllegalArgumentException("saveName cannot be blank");
        }
        String normalized = saveName.trim().replaceAll("[^A-Za-z0-9._-]", "_");
        if (normalized.isBlank() || normalized.equals(".") || normalized.equals("..")) {
            throw new IllegalArgumentException("saveName is invalid");
        }
        return normalized;
    }

    private void reconnectLoadedGame(Game game) {
        World world = game.getWorld();
        Company company = game.getCompany();

        company.setWorld(world);
        world.getRoadNetwork().rebuild(world.getMap());

        for (Vehicle vehicle : company.getFleet()) {
            vehicle.setOwner(company);
            vehicle.setWorld(world);
        }
    }
}
