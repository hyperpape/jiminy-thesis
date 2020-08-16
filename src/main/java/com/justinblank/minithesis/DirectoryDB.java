package com.justinblank.minithesis;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

class DirectoryDB {

    private final File directory;

    DirectoryDB(Path path) {
        this.directory = path.toFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Failed to initialize directory");
        }
    }

    public List<Integer> get(String name) {
        try {
            var file = fromKey(name);
            var bytes = Files.readAllBytes(file);
            var list = new ArrayList<Integer>(bytes.length);
            for (byte b : bytes) {
                list.add((int) b);
            }
            return list;
        }
        catch (NoSuchFileException e) {
            return null;
        }
        catch (Exception e) {
            throw new RuntimeException(e); // YOLO;
        }
    }

    public void delete(String name) {
        try {
            var file = fromKey(name);
            Files.delete(file);
        }
        catch (NoSuchFileException e) {
            // do nothing, intentionally
        }
        catch (Exception e) {
            throw new RuntimeException(e); // YOLO;
        }
    }

    private Path fromKey(String key) throws NoSuchAlgorithmException {
        String hashed = String.valueOf(key.hashCode());
        var messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(key.getBytes());
        var digest = messageDigest.digest();
        for (var i = 0; i < digest.length; i++) {
            if (digest[i] == 0) {
                digest[i] = 1;
            }
        }
        return new File(directory, new String(digest)).toPath();
    }

    public void set(String name, int[] choices) {
        try {
            var file = fromKey(name);
            // TODO: care enough to avoid copy of byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            for (var i = 0; i < choices.length; i++) {
                dos.writeInt(choices[i]);
            }
            Files.write(file, bos.toByteArray(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (NoSuchFileException e) {
            // YOLO
            System.out.println("Failed to write to db for file: " + name);
        }
        catch (Exception e) {
            throw new RuntimeException(e); // YOLO;
        }
    }
}
